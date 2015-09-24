package version2.prototype.summary.zonal;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Transformer;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.xml.sax.SAXException;

import version2.prototype.ErrorLog;
import version2.prototype.Process;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GdalUtils;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.GeneralUIEventObject;
import version2.prototype.util.IndicesFileMetaData;
import version2.prototype.util.Schemas;

/**
 * Calculates the zonal summary for a given raster file using the defined SummariesCollection object.
 *
 * @author michael.devos
 *
 */
public class ZonalSummaryCalculator {
    private final Process process;
    @SuppressWarnings("unused")
    private final String workingDir;
    private final IndicesFileMetaData inputFile;
    private final String shapeFilePath;
    private final File mTableFile;
    private final String areaCodeField;
    private final String areaNameField;
    private final SummariesCollection summariesCollection;
    private final String globalSchema;
    private final String projectName;
    private final String pluginName;
    private final int daysPerInputData;
    private final ProjectInfoSummary summary;
    private final DatabaseCache outputCache;

    private Map<Integer, Boolean> zoneReceivedValidData;
    private static Integer invalidZoneCountTot = 0;
    private static Integer oldInvalidZoneCountTot = 0;

    /**
     * Creates a ZonalSummaryCalculator.
     * @param process
     * @param globalSchema
     * @param workingDir
     * @param projectName  - current project's name
     * @param pluginName
     * @param daysPerInputData
     * @param inputFile
     * @param outTableFile  - where to write output to
     * @param summariesCollection  - collection of summary calculations to run on raster data
     * @param summary
     * @param outputCache
     */
    public ZonalSummaryCalculator(Process process, String globalSchema, String workingDir, String projectName, String pluginName, int daysPerInputData, IndicesFileMetaData inputFile, File outTableFile,
            SummariesCollection summariesCollection, ProjectInfoSummary summary, DatabaseCache outputCache)
    {
        this.process = process;
        this.workingDir = workingDir;
        this.inputFile = inputFile;
        mTableFile = outTableFile;
        shapeFilePath = summary.GetZonalSummary().GetShapeFile();
        areaCodeField = summary.GetZonalSummary().GetAreaCodeField();
        areaNameField = summary.GetZonalSummary().GetAreaNameField();
        this.summariesCollection = summariesCollection;
        this.globalSchema = globalSchema;
        this.projectName = projectName;
        this.pluginName = pluginName;
        this.daysPerInputData = daysPerInputData;
        this.summary = summary;
        this.outputCache = outputCache;

        zoneReceivedValidData = new HashMap<Integer, Boolean>();
    }

    /**
     * Run ZonalSummaryCalculator.
     * @throws Exception
     */
    public void calculate() throws Exception {
        GdalUtils.register();

        synchronized (GdalUtils.lockObject) {
            Dataset raster = null;
            DataSource layerSource = null;
            Layer layer = null;
            Dataset zoneRaster = null;
            try {
                // Open inputs
                raster = gdal.Open(inputFile.dataFilePath);
                GdalUtils.errorCheck();
                layerSource = ogr.Open(shapeFilePath);
                GdalUtils.errorCheck();
                layer = layerSource.GetLayer(0);

                // Validate inputs
                if (!isSameProjection(raster, layer)) {
                    throw new IOException("\"" + inputFile.dataFilePath + "\" isn't in same projection as \"" + shapeFilePath + "\"");
                }

                if (!isLayerSubsetOfRaster(layer, raster)) {
                    throw new IOException("\"" + shapeFilePath + "\" isn't a subset of \"" + inputFile.dataFilePath + "\".");
                }

                // Create the zone raster
                zoneRaster = rasterize(layer, raster.GetGeoTransform());

                assert(raster.GetRasterXSize() == zoneRaster.GetRasterXSize());
                assert(raster.GetRasterYSize() == zoneRaster.GetRasterYSize());

                // Calculate statistics
                Map<Integer, Double> countMap = calculateStatistics(raster, zoneRaster);

                // Write the table
                writeTable(layer, countMap);

                // Write to database
                uploadResultsToDb(mTableFile, layer, areaCodeField, areaNameField, inputFile.indexNm, summary, summariesCollection, inputFile.year, inputFile.day, process);
            }
            catch (SQLException | IllegalArgumentException | UnsupportedOperationException | IOException | ClassNotFoundException | ParserConfigurationException | SAXException e)
            {
                ErrorLog.add(process, "Problem with calculating zonal summaries.", e);
            }
            finally
            {
                try {
                    if (raster != null) {
                        raster.delete();
                        GdalUtils.errorCheck();
                    }
                    if (layer != null) {
                        layer.delete(); GdalUtils.errorCheck();
                    }
                    if (layerSource != null) {
                        layerSource.delete(); GdalUtils.errorCheck();
                    }
                    if (zoneRaster != null) {
                        zoneRaster.delete(); GdalUtils.errorCheck();
                    }
                } catch (IllegalArgumentException | UnsupportedOperationException | IOException e) {
                    ErrorLog.add(process, "Problem with deleting Gdal related resources.", e);
                }
            }
        }
    }

    /**
     * Checks whether the given raster and layer share the same projection.
     *
     * @param raster  - input raster file
     * @param layer  -  input shape file
     * @return true if projections are the same
     * @throws IOException
     * @throws UnsupportedOperationException
     * @throws IllegalArgumentException
     */
    private boolean isSameProjection(Dataset raster, Layer layer) throws IllegalArgumentException, UnsupportedOperationException, IOException {
        SpatialReference rasterRef = new SpatialReference(raster.GetProjection()); GdalUtils.errorCheck();
        boolean same = layer.GetSpatialRef().IsSame(rasterRef) != 0; GdalUtils.errorCheck();
        return same;
    }

    private boolean isLayerSubsetOfRaster(Layer layer, Dataset raster)
            throws IllegalArgumentException, UnsupportedOperationException, IOException {
        double[] extent = layer.GetExtent(true); GdalUtils.errorCheck();

        Vector<String> options = new Vector<String>();
        options.add("SRC_DS=" + layer.GetSpatialRef().ExportToWkt()); GdalUtils.errorCheck();

        Transformer transformer = new Transformer(null, raster, options); GdalUtils.errorCheck();

        double[] min = new double[] {Math.min(extent[0], extent[1]), Math.min(extent[2], extent[3]), 0};
        double[] max = new double[] {Math.max(extent[0], extent[1]), Math.max(extent[2], extent[3]), 0};

        transformer.TransformPoint(0, min); GdalUtils.errorCheck();
        transformer.TransformPoint(0, max); GdalUtils.errorCheck();

        int layerMinX = (int) Math.round(Math.min(min[0], max[0]));
        int layerMaxX = (int) Math.round(Math.max(min[0], max[0]));
        int layerMinY = (int) Math.round(Math.min(min[1], max[1]));
        int layerMaxY = (int) Math.round(Math.max(min[1], max[1]));

        int rasterMinX = 0;
        int rasterMaxX = raster.GetRasterXSize(); GdalUtils.errorCheck();
        int rasterMinY = 0;
        int rasterMaxY = raster.GetRasterYSize(); GdalUtils.errorCheck();

        if (layerMinX < rasterMinX) {
            return false;
        } else if (layerMaxX > rasterMaxX) {
            return false;
        } else if (layerMinY < rasterMinY) {
            return false;
        } else if (layerMaxY > rasterMaxY) {
            return false;
        }

        return true;
    }


    /**
     * Rasterizes data creating Dataset object. Uses GDal library to do calculation.
     *
     * @param layer  - shape file
     * @param transform  - raster data in single double array
     * @return all data after rasterization
     * @throws IOException
     * @throws UnsupportedOperationException
     * @throws IllegalArgumentException
     * @throws Exception
     */
    private Dataset rasterize(Layer layer, double[] transform) throws IllegalArgumentException, UnsupportedOperationException, IOException {

        // Create the raster to burn values into
        double[] layerExtent = layer.GetExtent(); GdalUtils.errorCheck();

        Dataset zoneRaster = gdal.GetDriverByName("MEM").Create(
                "",
                (int) Math.ceil((layerExtent[1]-layerExtent[0]) / Math.abs(transform[1])),
                (int) Math.ceil((layerExtent[3]-layerExtent[2]) / Math.abs(transform[5])),
                1,
                gdalconst.GDT_UInt32
                );
        GdalUtils.errorCheck();

        zoneRaster.SetProjection(layer.GetSpatialRef().ExportToWkt()); GdalUtils.errorCheck();
        zoneRaster.SetGeoTransform(new double[] {
                layerExtent[0], transform[1], 0,
                layerExtent[2] + zoneRaster.GetRasterYSize()*Math.abs(transform[5]), 0, transform[5]
        });
        GdalUtils.errorCheck();

        // Burn the values
        Vector<String> options = new Vector<String>();
        options.add("ATTRIBUTE=" + areaCodeField);

        gdal.RasterizeLayer(zoneRaster, new int[] {1}, layer, new double[] {}, options); GdalUtils.errorCheck();

        return zoneRaster;
    }


    private Map<Integer, Double> calculateStatistics(Dataset rasterDS, Dataset featureDS) throws IllegalArgumentException, UnsupportedOperationException, IOException {
        // Calculate zonal statistics
        Band zoneBand = featureDS.GetRasterBand(1); GdalUtils.errorCheck();
        Band rasterBand = rasterDS.GetRasterBand(1); GdalUtils.errorCheck();

        final int WIDTH = zoneBand.GetXSize(); GdalUtils.errorCheck();
        final int HEIGHT = zoneBand.GetYSize(); GdalUtils.errorCheck();

        int[] zoneArray = new int[WIDTH];
        double[] rasterArray = new double[WIDTH];

        Double[] noData = new Double[1];
        //FIXME: Can't get the no data value from NLDAS reprojected file, manually set it to 0. It will affect the zonal result.
        rasterBand.GetNoDataValue(noData);
        final ArrayList<Double> NO_DATA = new ArrayList<Double>(2);
        NO_DATA.add(new Double(0));
        NO_DATA.add(new Double(-3.4028234663852886E38));

        for (int y=0; y<HEIGHT; y++) {
            zoneBand.ReadRaster(0, y, WIDTH, 1, zoneArray); GdalUtils.errorCheck();
            rasterBand.ReadRaster(0, y, WIDTH, 1, rasterArray); GdalUtils.errorCheck();

            for (int i=0; i<WIDTH; i++) {
                int zone = zoneArray[i];
                Double value = rasterArray[i];
                if (!NO_DATA.contains(Double.valueOf(zone)) && !NO_DATA.contains(value)) { // Neither are no data values
                    summariesCollection.add(zone, value);
                    zoneReceivedValidData.put(zone, true);
                }
                else if(zone != 0 && zoneReceivedValidData.get(zone) == null) {
                    zoneReceivedValidData.put(zone, false);
                }
            }
        }

        ArrayList<SummaryNameResultPair> results = summariesCollection.getResults();
        Map<Integer, Double> countMap = new HashMap<Integer, Double>(1);
        for(SummaryNameResultPair pair : results){
            if(pair.getSimpleName().equalsIgnoreCase("count")) {
                countMap = pair.getResult();
            }
        }

        return countMap;
    }


    private void writeTable(Layer layer, Map<Integer, Double> countMap) throws IllegalArgumentException, UnsupportedOperationException, IOException {
        // Write the table
        String directory = mTableFile.getCanonicalPath().substring(0, mTableFile.getCanonicalPath().lastIndexOf("\\"));
        new File(directory).mkdirs();
        PrintWriter writer = new PrintWriter(mTableFile);

        layer.ResetReading(); GdalUtils.errorCheck();
        Feature feature = layer.GetNextFeature(); GdalUtils.errorCheck();
        ArrayList<SummaryNameResultPair> results = summariesCollection.getResults();

        writer.print("Area Name, Area Code, Value Count, ");
        for(int i=0; i < results.size(); i++)
        {
            String name = results.get(i).getSimpleName();
            if(i < results.size() - 1) {
                writer.print(name + ", ");
            } else {
                writer.println(name);
            }
        }

        int areaCode;
        String areaName;
        while (feature != null) {
            areaCode = feature.GetFieldAsInteger(areaCodeField);
            GdalUtils.errorCheck();
            areaName = feature.GetFieldAsString(areaNameField);
            GdalUtils.errorCheck();
            if (countMap.get(areaCode) != null && countMap.get(areaCode) != 0) {
                writer.print(areaName + ", " + areaCode + ", ");
                for(int i=0; i < results.size(); i++){
                    if(i < results.size() - 1) {
                        writer.print(results.get(i).getResult().get(areaCode) + ", ");
                    } else {
                        writer.println(results.get(i).getResult().get(areaCode));
                    }
                }
            }
            feature = layer.GetNextFeature(); GdalUtils.errorCheck();
        }

        writer.close();
    }

    private void uploadResultsToDb(File mTableFile, Layer layer, String areaCodeField, String areaNameField, String indexNm, ProjectInfoSummary summary, SummariesCollection summariesCollection, int year,
            int day, Process process) throws IllegalArgumentException, UnsupportedOperationException, IOException, ClassNotFoundException, ParserConfigurationException, SAXException, SQLException {
        final Connection conn = DatabaseConnector.getConnection();
        Statement stmt = conn.createStatement();
        ArrayList<SummaryResult> newResults = new ArrayList<SummaryResult>();
        ArrayList<SummaryNameResultPair> results = summariesCollection.getResults();
        Map<Integer, Double> countMap = null;
        Map<Integer, String> zoneNameMap = new HashMap<Integer, String>();

        for(SummaryNameResultPair result : results)
        {
            if(result.getSimpleName().equals("Count"))
            {
                countMap = result.getResult();
                break;
            }
        }

        layer.ResetReading(); GdalUtils.errorCheck();
        Feature feature = layer.GetNextFeature(); GdalUtils.errorCheck();
        int indexID = Schemas.getIndexID(globalSchema, indexNm, stmt);
        int areaCode;
        int projectSummaryID;
        int dateGroupID;
        double value;
        String areaName;
        String filePath = mTableFile.getCanonicalPath();
        SummaryNameResultPair pair;
        Map<Integer, Double> result;
        Map<String, Double> summaryAreaResult;
        try{
            while (feature != null)
            {
                summaryAreaResult = new HashMap<String, Double>();
                areaCode = feature.GetFieldAsInteger(areaCodeField);
                GdalUtils.errorCheck();
                areaName = feature.GetFieldAsString(areaNameField);
                GdalUtils.errorCheck();
                if(zoneNameMap.get(areaCode) == null) {
                    zoneNameMap.put(areaCode, areaName);
                }

                if (countMap.get(areaCode) != null && countMap.get(areaCode) != 0)
                {
                    // Insert values
                    projectSummaryID = Schemas.getProjectSummaryID(globalSchema, projectName, summary.GetID(), stmt);
                    dateGroupID = Schemas.getDateGroupID(globalSchema, LocalDate.ofYearDay(year, day), stmt);
                    for(int i=0; i < results.size(); i++)
                    {
                        pair = results.get(i);
                        result = pair.getResult();
                        value = result.get(areaCode);
                        summaryAreaResult.put(pair.getSimpleName(), value);
                    }
                    newResults.add(new SummaryResult(projectSummaryID, areaName, areaCode, dateGroupID, indexID, filePath, summaryAreaResult));
                }

                feature = layer.GetNextFeature(); GdalUtils.errorCheck();
            }
        } finally {
            stmt.close();
            conn.close();
        }

        TemporalSummaryCompositionStrategy compStrategy = null;
        if(summary.GetTemporalFileStore() != null) {
            compStrategy = summary.GetTemporalFileStore().compStrategy;
        }

        Set<Integer> zones = zoneReceivedValidData.keySet();
        int invalidCount = 0;
        for(Integer zone : zones)
        {
            if(!zoneReceivedValidData.get(zone)) {
                ++invalidCount;
                System.out.println("Invalid zone (" + zone + ":" + zoneNameMap.get(zone) +") for dateGroupID: " + inputFile.dateGroupID);
            }
        }
        invalidZoneCountTot += invalidCount;
        if(invalidZoneCountTot > 0 && invalidZoneCountTot != oldInvalidZoneCountTot) {
            System.out.println("invalidZoneCountTot: " + invalidZoneCountTot);
            oldInvalidZoneCountTot = invalidZoneCountTot;
        }
        if(invalidCount > 0) {
            process.NotifyUI(new GeneralUIEventObject(this, invalidCount + " zone" + ((invalidCount > 1) ? "s" : "") + " had no valid data read in for plugin='" + pluginName + "', index='" + indexNm
                    + "', Summary ID="+ summary.GetID() + ", date={year: " + year + ", day of year: " + day + "}"));
        }

        outputCache.UploadResultsToDb(newResults, summary.GetID(), compStrategy, year, day, process, daysPerInputData);
    }

}
