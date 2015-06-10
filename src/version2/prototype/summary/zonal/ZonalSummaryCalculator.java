package version2.prototype.summary.zonal;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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

import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.summary.SummaryData;
import version2.prototype.summary.summaries.SummariesCollection;
import version2.prototype.summary.summaries.SummaryNameResultPair;
import version2.prototype.util.CachedDataFile;
import version2.prototype.util.GdalUtils;
import version2.prototype.util.PostgreSQLConnection;



public class ZonalSummaryCalculator extends ProcessWorker<CachedDataFile> {
    private File mRasterFile;
    private File mLayerFile;
    private File mTableFile;
    private String mField;
    private SummariesCollection summariesCollection;
    private String projectName;

    public ZonalSummaryCalculator(SummaryData data, String processWorkerName, Process<?> process)
    {
        super(processWorkerName, process);
        mRasterFile = data.inRasterFile;
        mLayerFile = data.inShapeFile;
        mTableFile = data.outTableFile;
        mField = data.zoneField;
        summariesCollection = data.summariesCollection;
        projectName = data.projectName;
    }

    @Override
    public CachedDataFile call() throws Exception {
        GdalUtils.register();

        synchronized (GdalUtils.lockObject) {
            Dataset raster = null;
            DataSource layerSource = null;
            Layer layer = null;
            Dataset zoneRaster = null;
            try {
                // Open inputs
                raster = gdal.Open(mRasterFile.getPath()); GdalUtils.errorCheck();
                layerSource = ogr.Open(mLayerFile.getPath());
                if (layerSource == null) {
                    throw new IOException("Could not load " + mLayerFile.getPath());
                }
                layer = layerSource.GetLayer(0);
                if (layer == null) {
                    throw new IOException("Could not load layer 0 of " + mLayerFile.getPath());
                }

                // Validate inputs
                if (!isSameProjection(raster, layer)) {
                    throw new IOException("\"" + mRasterFile.getPath() + "\" isn't in same projection as \"" + mLayerFile.getPath() + "\"");
                }

                if (!isLayerSubsetOfRaster(layer, raster)) {
                    throw new IOException("\"" + mLayerFile.getPath() + "\" isn't a subset of \"" + mRasterFile.getPath() + "\".");
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
                uploadResultsToDb(projectName, mRasterFile.getPath(), layer, mLayerFile.getPath(), mField, countMap);
            } finally { // Clean up
                if (raster != null) {
                    raster.delete(); GdalUtils.errorCheck();
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
            }
        }
        return null;
    }

    /**
     * Checks whether the given raster and layer share the same projection.
     *
     * @param raster
     * @param layer
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

        System.out.format(
                "Layer extent: %d %d %d %d\n",
                layerMinX,
                layerMaxX,
                layerMinY,
                layerMaxY
                );


        System.out.format(
                "%d %d\n",
                raster.GetRasterXSize(),
                raster.GetRasterYSize()
                );

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
     *
     *
     * @param layer
     * @param transform
     * @return
     * @throws Exception
     */
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


    private Map<Integer, Double> calculateStatistics(Dataset rasterDS, Dataset featureDS) throws Exception {
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
                if (zone != 0 && value != NO_DATA) { // Neither are no data values
                    summariesCollection.put(zone, value);
                }
            }
        }

        ArrayList<SummaryNameResultPair> results = summariesCollection.getResults();
        Map<Integer, Double> countMap = new HashMap<Integer, Double>(1);
        for(SummaryNameResultPair pair : results){
            System.out.println(pair.toString());
            if(pair.getSimpleName().equalsIgnoreCase("count")) {
                countMap = pair.getResult();
            }
        }

        return countMap;
    }


    private void writeTable(Layer layer, Map<Integer, Double> countMap) throws Exception {
        // Write the table
        PrintWriter writer = new PrintWriter(mTableFile);

        layer.ResetReading(); GdalUtils.errorCheck();
        Feature feature = layer.GetNextFeature(); GdalUtils.errorCheck();
        ArrayList<SummaryNameResultPair> results = summariesCollection.getResults();

        while (feature != null) {
            int zone = feature.GetFieldAsInteger(mField); GdalUtils.errorCheck();
            if (countMap.get(zone) != null && countMap.get(zone) != 0) {
                writer.print(zone + ",");
                for(int i=0; i < results.size(); i++){
                    if(i < results.size() - 1) {
                        System.out.println(results.get(i).toString(",") + ", ");
                    } else {
                        System.out.println(results.get(i).toString(","));
                    }
                }
                writer.println();
            }
            feature = layer.GetNextFeature(); GdalUtils.errorCheck();
        }

        writer.close();
    }

    private void uploadResultsToDb(String mSchemaName, String rasterFilePath, Layer layer, String shapefilePath, String field,
            Map<Integer, Double> countMap) throws SQLException, IllegalArgumentException, UnsupportedOperationException, IOException,
            ClassNotFoundException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final boolean previousAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        ArrayList<SummaryNameResultPair> results = summariesCollection.getResults();
        String index = "";
        int year = -1;
        int day = -1;

        // C:\Users\michael.devos\Desktop\eastweb-data\projects\tw_test\indices\trmm\2013\204\TW_DIS_F_P_Dis_REGION
        // Get position of index folder found after project name folder and index folder name
        int pos = rasterFilePath.indexOf("\\", rasterFilePath.indexOf(projectName) + projectName.length() + 1) + 1;
        if(pos == -1) {
            pos = rasterFilePath.indexOf("/", rasterFilePath.indexOf(projectName) + projectName.length() + 1) + 1;
        }

        index = rasterFilePath.substring(pos, rasterFilePath.indexOf(File.separator, pos));
        year = Integer.parseInt(rasterFilePath.substring(pos + index.length() + 1, rasterFilePath.indexOf(File.separator, pos + index.length() + 1)));
        day = Integer.parseInt(rasterFilePath.substring(pos + index.length() + 6, rasterFilePath.indexOf(File.separator, pos + index.length() + 6)));

        try {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            int indexKey = -1;
            String _query = String.format(
                    "SELECT \"index\"\n" +
                            "FROM \"%1$s\".\"Indicies\"\n" +
                            "WHERE \"name\" = ?\n",
                            mSchemaName
                    );
            final PreparedStatement ps = conn.prepareStatement(_query);
            ps.setString(1, index);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                indexKey = rs.getInt(1);
                rs.close();
            }
            else{
                _query = String.format(
                        "INSERT INTO \"%1$s\".\"Indicies\" (\n" +
                                "  \"name\"\n" +
                                ") VALUES (\n" +
                                "  ?\n" +
                                ")",
                                mSchemaName
                        );
                final PreparedStatement psInsert = conn.prepareStatement(_query);
                psInsert.setString(1, index);
                psInsert.executeQuery();

                _query = String.format(
                        "SELECT \"index\"\n" +
                                "FROM \"%1$s\".\"Indicies\"\n" +
                                "WHERE \"name\" = ?\n",
                                mSchemaName
                        );
                final PreparedStatement psInserted = conn.prepareStatement(_query);
                psInserted.setString(1, index);
                final ResultSet rsInserted = ps.executeQuery();
                indexKey = rsInserted.getInt(1);
                rsInserted.close();
            }

            layer.ResetReading(); GdalUtils.errorCheck();
            Feature feature = layer.GetNextFeature(); GdalUtils.errorCheck();
            Map<Integer, Double> sumMap = new HashMap<Integer, Double>();
            Map<Integer, Double> meanMap = new HashMap<Integer, Double>();
            Map<Integer, Double> stdDevMap = new HashMap<Integer, Double>();
            while (feature != null)
            {
                int zone = feature.GetFieldAsInteger(mField); GdalUtils.errorCheck();
                if (countMap.get(zone) != null && countMap.get(zone) != 0)
                {
                    for(SummaryNameResultPair pair : results){
                        System.out.println(pair.toString());
                        switch(pair.getSimpleName())
                        {
                        case "Sum": sumMap = pair.getResult(); break;
                        case "Mean": meanMap = pair.getResult(); break;
                        case "StdDev": stdDevMap = pair.getResult(); break;
                        }

                        if(pair.getSimpleName().equalsIgnoreCase("Count")) {
                            countMap = pair.getResult();
                        }
                    }

                    final int zoneId = getZoneId(conn, mSchemaName, getZoneFieldId(conn, mSchemaName, shapefilePath, field), zone);

                    _query = String.format(
                            "SELECT COUNT(*)\n" +
                                    "FROM \"%1$s\".\"ZonalStats\"\n" +
                                    "WHERE\n" +
                                    "  \"index\" = ? AND\n" +
                                    "  \"year\" = ? AND\n" +
                                    "  \"day\" = ? AND\n" +
                                    "  \"zoneID\" = ?\n",
                                    mSchemaName
                            );
                    final PreparedStatement psExists = conn.prepareStatement(_query);
                    psExists.setInt(1, indexKey);
                    psExists.setInt(2, year);
                    psExists.setInt(3, day);
                    psExists.setInt(4, zoneId);
                    final ResultSet rsExists = psExists.executeQuery();
                    final int numMatchingRows;
                    try {
                        if (!rsExists.next()) {
                            throw new SQLException("Expected one result row");
                        }
                        numMatchingRows = rsExists.getInt(1);
                    } finally {
                        rsExists.close();
                    }

                    if (numMatchingRows == 0) {
                        _query = String.format(
                                "INSERT INTO \"%1$s\".\"ZonalStats\" (\n" +
                                        "  \"index\",\n" +
                                        "  \"year\",\n" +
                                        "  \"day\",\n" +
                                        "  \"zoneID\",\n" +
                                        "  \"Count\",\n" +
                                        "  \"Sum\",\n" +
                                        "  \"Mean\",\n" +
                                        "  \"StdDev\"\n" +
                                        ") VALUES (\n" +
                                        "  ?,\n" +
                                        "  ?,\n" +
                                        "  ?,\n" +
                                        "  ?,\n" +
                                        "  ?,\n" +
                                        "  ?,\n" +
                                        "  ?,\n" +
                                        "  ?\n" +
                                        ")",
                                        mSchemaName
                                );
                        final PreparedStatement psInsert = conn.prepareStatement(_query);
                        psInsert.setInt(1, indexKey);
                        psInsert.setInt(2, year);
                        psInsert.setInt(3, day);
                        psInsert.setInt(4, zoneId);
                        psInsert.setDouble(5, countMap.get(zone));
                        psInsert.setDouble(6, sumMap.get(zone));
                        psInsert.setDouble(7, meanMap.get(zone));
                        psInsert.setDouble(8, stdDevMap.get(zone));
                        psInsert.executeUpdate();
                    } else {
                        _query = String.format(
                                "UPDATE \"%1$s\".\"ZonalStats\"\n" +
                                        "SET\n" +
                                        "  \"count\" = ?,\n" +
                                        "  \"sum\" = ?,\n" +
                                        "  \"mean\" = ?,\n" +
                                        "  \"stdev\" = ?\n" +
                                        "WHERE\n" +
                                        "  \"index\" = ? AND\n" +
                                        "  \"year\" = ? AND\n" +
                                        "  \"day\" = ? AND\n" +
                                        "  \"zoneID\" = ?\n",
                                        mSchemaName
                                );
                        final PreparedStatement psUpdate = conn.prepareStatement(_query);
                        psUpdate.setDouble(1, countMap.get(zone));
                        psUpdate.setDouble(2, sumMap.get(zone));
                        psUpdate.setDouble(3, meanMap.get(zone));
                        psUpdate.setDouble(4, stdDevMap.get(zone));
                        psUpdate.setInt(5, indexKey);
                        psUpdate.setInt(6, year);
                        psUpdate.setInt(7, day);
                        psUpdate.setInt(8, zoneId);
                        psUpdate.executeUpdate();
                    }
                }

                feature = layer.GetNextFeature(); GdalUtils.errorCheck();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(previousAutoCommit);
        }
    }

    /**
     * Looks up the zoneFieldID for the specified (shapefile, field) pair.
     * Returns null if there is no matching record.
     * @throws SQLException
     */
    public Integer lookupZoneFieldId(Connection conn, String mSchemaName, String shapefile, String field) throws SQLException {
        String _query = String.format(
                "SELECT \"zoneFieldID\"\n" +
                        "FROM \"%1$s\".\"ZoneFields\"\n" +
                        "WHERE \"shapefile\" = ? AND \"field\" = ?",
                        mSchemaName
                );
        final PreparedStatement ps = conn.prepareStatement(_query);
        ps.setString(1, shapefile);
        ps.setString(2, field);

        final ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return null;
        }
    }

    /**
     * Looks up the zoneFieldID for the specified (shapefile, field) pair.
     * A new zoneFieldID is created if there is no matching record.
     * Do this in a transaction!
     * @throws SQLException
     */
    public int getZoneFieldId(Connection conn, String mSchemaName, String shapefile, String field) throws SQLException {
        Integer lookup = lookupZoneFieldId(conn, mSchemaName, shapefile, field);
        if (lookup != null) {
            return lookup;
        }

        String _query = String.format(
                "INSERT INTO \"%1$s\".\"ZoneFields\" (\n" +
                        "  \"shapefile\",\n" +
                        "  \"field\"\n" +
                        ") VALUES (\n" +
                        "  ?,\n" +
                        "  ?\n" +
                        ")",
                        mSchemaName
                );
        final PreparedStatement ps = conn.prepareStatement(_query);
        ps.setString(1, shapefile);
        ps.setString(2, field);
        ps.executeUpdate();

        _query = String.format(
                "SELECT currval(\"%1$s\".\"ZoneFields_zoneFieldID_seq\")",
                mSchemaName
                );
        final ResultSet rs = conn.prepareStatement(_query).executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return 0;
        }
    }

    /**
     * Looks up the ZoneID for the specified (zoneFieldID, zone) pair.
     * Returns null if there is no matching record.
     * @throws SQLException
     */
    public Integer lookupZoneId(Connection conn, String mSchemaName, int zoneFieldId, int zone) throws SQLException {
        String _query = String.format(
                "SELECT \"zoneID\"\n" +
                        "FROM \"%1$s\".\"Zones\"\n" +
                        "WHERE \"zoneFieldID\" = ? AND \"name\" = ?",
                        mSchemaName
                );
        final PreparedStatement ps = conn.prepareStatement(_query);
        ps.setInt(1, zoneFieldId);
        ps.setInt(2, zone);

        final ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return null;
        }
    }

    /**
     * Looks up the zoneID for the specified (zoneFieldID, zone) pair.
     * A new zoneID is created if there is no matching record.
     * Do this in a transaction!
     * @throws SQLException
     */
    public int getZoneId(Connection conn, String mSchemaName, int zoneFieldId, int zone) throws SQLException {
        Integer lookup = lookupZoneId(conn, mSchemaName, zoneFieldId, zone);
        if (lookup != null) {
            return lookup;
        }

        String _query = String.format(
                "INSERT INTO \"%1$s\".\"Zones\" (\n" +
                        "  \"zoneFieldID\",\n" +
                        "  \"fieldID\"\n" +
                        ") VALUES (\n" +
                        "  ?,\n" +
                        "  ?,\n" +
                        "  ?\n" +
                        ")",
                        mSchemaName
                );
        final PreparedStatement ps = conn.prepareStatement(_query);
        ps.setInt(1, zoneFieldId);
        ps.setInt(2, zone);
        ps.executeUpdate();

        _query = String.format(
                "SELECT currval(\"%1$s\".\"Zones_zoneID_seq\")",
                mSchemaName
                );
        final ResultSet rs = conn.prepareStatement(_query).executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return 0;
        }
    }
}
