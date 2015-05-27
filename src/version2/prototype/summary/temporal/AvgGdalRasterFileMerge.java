package version2.prototype.summary;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

import version2.prototype.util.GdalUtils;

public class AvgGdalRasterFileMerge implements MergeStrategy {

    @Override
    public File Merge(GregorianCalendar firstDate, File shapeFile, File... rasterFiles) throws Exception {
        GdalUtils.register();


        synchronized (GdalUtils.lockObject) {
            Map<Integer, Double> outMap = null;
            ArrayList<Dataset> rasters = new ArrayList<Dataset>(1);
            ArrayList<Dataset> zoneRasters = new ArrayList<Dataset>(1);
            DataSource layerSource = null;
            Layer layer = null;

            // Open inputs
            for(File raster : rasterFiles)
            {
                // Prepare raster file data
                rasters.add(gdal.Open(raster.getPath()));
                GdalUtils.errorCheck();

                // Prepare shape file data
                layerSource = ogr.Open(shapeFile.getPath());
                layer = layerSource.GetLayer(0);

                // Create the zone raster
                zoneRasters.add(rasterize(layer, rasters.get(rasters.size()-1).GetGeoTransform()));
            }

            // Calculate statistics
            outMap = calculateStatistics(rasters, zoneRasters, layer);

            // Write the table
            writeTable(firstDate, layer);
        }
        return null;
    }

    private Dataset rasterize(Layer layer, double[] transform) throws Exception {

        // Create the raster to burn values into
        double[] layerExtent = layer.GetExtent(); GdalUtils.errorCheck();
        System.out.format("Feature extent: %s\n", Arrays.toString(layerExtent));
        System.out.println(Arrays.toString(transform));

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
        options.add("ATTRIBUTE=" + mField);

        gdal.RasterizeLayer(zoneRaster, new int[] {1}, layer, null, options); GdalUtils.errorCheck();


        return zoneRaster;
    }

    private Map<Integer, Double> calculateStatistics(Dataset rasterDS, Dataset featureDS, Layer layer) throws Exception {
        Map<Integer, Double> outMap = new HashMap<Integer, Double>();

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
        //final float NO_DATA = noData[0].floatValue();
        final float NO_DATA=0;
        for (int y=0; y<HEIGHT; y++) {
            zoneBand.ReadRaster(0, y, WIDTH, 1, zoneArray); GdalUtils.errorCheck();
            rasterBand.ReadRaster(0, y, WIDTH, 1, rasterArray); GdalUtils.errorCheck();

            for (int i=0; i<WIDTH; i++) {
                int zone = zoneArray[i];
                double value = rasterArray[i];
                if (zone != 0 && value != NO_DATA)
                    outMap.put(zone, value);
            }
        }
        return outMap;
    }

    private void writeTable(GregorianCalendar firstDate, Layer layer, Map<Integer, Double> outMap) throws Exception {
        // Write the table
        PrintWriter writer = new PrintWriter(mTableFile);

        layer.ResetReading(); GdalUtils.errorCheck();
        Feature feature = layer.GetNextFeature(); GdalUtils.errorCheck();
        ArrayList<SummaryNameResultPair> results = summaries.getResults();
        Map<Integer, Double> countMap = new HashMap<Integer, Double>(1);
        for(SummaryNameResultPair pair : results)
            if(pair.getSimpleName().equalsIgnoreCase("count")){
                countMap = pair.getResult();
                break;
            }
        while (feature != null) {
            int zone = feature.GetFieldAsInteger(mField); GdalUtils.errorCheck();
            if (countMap.get(zone) != null && countMap.get(zone) != 0) {
                writer.print(zone + ",");
                for(int i=0; i < results.size(); i++)
                    if(i < results.size() - 1)
                        System.out.println(results.get(i).toString(",") + ", ");
                    else
                        System.out.println(results.get(i).toString(","));
                writer.println();
            }
            feature = layer.GetNextFeature(); GdalUtils.errorCheck();
        }

        writer.close();
    }
}
