/**
 *
 */
package test.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.DataDate;
import version2.prototype.Process;
import version2.prototype.TaskState;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.IndicesMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.PluginMetaData.ProcessorMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.Scheduler.SchedulerStatusContainer;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.summary.temporal.CompositionStrategies.CDCWeeklyStrategy;
import version2.prototype.summary.zonal.SummaryResult;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GeneralUIEventObject;
import version2.prototype.util.IndicesFileMetaData;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.ProcessorFileMetaData;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class DatabaseCacheTest {
    private static DatabaseCache testDownloadCache;
    private static DatabaseCache testProcessorCache;
    private static DatabaseCache testIndicesCache;
    private static DatabaseCache testSummaryCache;
    private static ArrayList<String> extraDownloadFiles;
    private static String testProjectName = "Test_Project1";
    private static String testPluginName = "ModisNBAR";
    private static String testGlobalSchema = "Test_EASTWeb1";
    private static String testProjectSchema;
    private static String workingDir = "C:\\eastweb-data-test";
    private static Connection con;
    private static Statement stmt;
    private static TemporalSummaryCompositionStrategy compStrategy = new CDCWeeklyStrategy();
    private static LocalDate startDate;
    private static int year;
    private static int day;
    private static LocalDate earlierStartDate;
    //    private static int daysPerInputFile = 1;
    //    private static int numOfIndices = 3;
    //    private static int filesPerDay = 1;
    private static int daysPerInputFile = 8;
    private static int numOfIndices = 5;
    private static int filesPerDay = 322;
    private static String modisTile = "v12H42";
    private static MyScheduler scheduler;
    private static ArrayList<String> summaryNames;
    private static ProjectInfoSummary projectInfoSummary1;
    private static ProjectInfoFile projectMetaData;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        startDate = compStrategy.getStartDate(LocalDate.now().minusDays(7));
        year = startDate.getYear();
        day = startDate.getDayOfYear();
        earlierStartDate = startDate.minusDays(14);

        extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");

        testProjectSchema = Schemas.getSchemaName(testProjectName, testPluginName);

        DatabaseCacheTest tester = new DatabaseCacheTest();
        ArrayList<String> indicesNames = new ArrayList<String>();
        indicesNames.add("ModisNBARNDVI");
        ArrayList<ProjectInfoPlugin> plugins = new ArrayList<ProjectInfoPlugin>();
        plugins.add(new ProjectInfoPlugin(testPluginName, indicesNames, null));
        TreeMap<String, Integer> downloadExpectedDataFiles = new TreeMap<String, Integer>();
        downloadExpectedDataFiles.put(testPluginName, 2);
        TreeMap<String, Integer> processorExpectedNumOfOutputs = new TreeMap<String, Integer>();
        processorExpectedNumOfOutputs.put(testPluginName, 2);
        SchedulerStatusContainer statusContainer = new SchedulerStatusContainer(null, 1, null, null, null, new ArrayList<String>(), TaskState.RUNNING, new TreeMap<String, TreeMap<String, Double>>(),
                new TreeMap<String, Double>(), new TreeMap<String, Double>(), new TreeMap<String, TreeMap<Integer, Double>>(), false, null);
        scheduler = tester.new MyScheduler(1, statusContainer);
        ArrayList<String> extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");
        DownloadMetaData downloadMD = new DownloadMetaData(null, null, null, null, null, null, null, null, null, null, filesPerDay, null, null, null);
        ProcessorMetaData processorMD = new ProcessorMetaData(null, null, null, null, null, null, 1);
        IndicesMetaData indicesMD = new IndicesMetaData(null, null, null, null, null, indicesNames);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.CreatePluginMetaData(null, null, null, extraDownloadFiles, downloadMD, processorMD, indicesMD, null, null, null);
        testDownloadCache = new DatabaseCache(scheduler, testGlobalSchema, testProjectName, plugins.get(0), pluginMetaData, workingDir, ProcessName.DOWNLOAD);
        testProcessorCache = new DatabaseCache(scheduler, testGlobalSchema, testProjectName, plugins.get(0), pluginMetaData, workingDir, ProcessName.PROCESSOR);
        testIndicesCache = new DatabaseCache(scheduler, testGlobalSchema, testProjectName, plugins.get(0), pluginMetaData, workingDir, ProcessName.INDICES);
        testSummaryCache = new DatabaseCache(scheduler, testGlobalSchema, testProjectName, plugins.get(0), pluginMetaData, workingDir, ProcessName.SUMMARY);

        summaryNames = new ArrayList<String>();
        summaryNames.add("Count");
        summaryNames.add("Max");
        summaryNames.add("Mean");
        summaryNames.add("Min");
        summaryNames.add("SqrSum");
        summaryNames.add("StdDev");
        summaryNames.add("Sum");

        projectInfoSummary1 = new ProjectInfoSummary(new ZonalSummary("ShapeFile1", "AreaCodeField1", "AreaNameField1"),
                new TemporalSummaryRasterFileStore(compStrategy), "MyTemporalSummaryCompositionStrategy", 1);
        ArrayList<ProjectInfoSummary> summaries = new ArrayList<ProjectInfoSummary>();
        summaries.add(projectInfoSummary1);
        projectMetaData = new ProjectInfoFile(plugins, startDate, testProjectName, null, null, null, null, null, null, null, null, null, null, null, null, null, summaries);

        con = DatabaseConnector.getConnection();
        stmt = con.createStatement();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                testGlobalSchema
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(testProjectName, testPluginName)
                );
        stmt.execute(query);
        stmt.close();
        con.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                testGlobalSchema
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(testProjectName, testPluginName)
                );
        stmt.execute(query);

        ArrayList<ProjectInfoSummary> summaries = new ArrayList<ProjectInfoSummary>();
        Schemas.CreateProjectPluginSchema(con, testGlobalSchema, projectMetaData, testPluginName, summaryNames, daysPerInputFile, filesPerDay, numOfIndices, true);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for both {@link version2.prototype.util.DatabaseCache#CacheFiles(java.util.ArrayList)} and {@link version2.prototype.util.DatabaseCache#GetUnprocessedCacheFiles()}.
     * Test Requirements:
     *  1) Capable of caching and loading files to for both Indices and Processor caches.
     *  2) Capable of selectively only loading files that have a date on or past the given start date.
     *  3) Capable of correctly computing the progress update.
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws ParseException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testCachingFilesAndRetrievingCachedFiles() throws ClassNotFoundException, SQLException, ParseException, ParserConfigurationException, SAXException, IOException {
        // Setup input
        String data1FilePath = "Data file path1";
        ArrayList<DataFileMetaData> filesForASingleComposite = new ArrayList<DataFileMetaData>();
        filesForASingleComposite.add(new DataFileMetaData(data1FilePath, 2, earlierStartDate.getYear(), earlierStartDate.getDayOfYear(), "Index"));

        // Cache to ProcessorCache
        testProcessorCache.CacheFiles(filesForASingleComposite);

        // Cache to IndicesCache
        testIndicesCache.CacheFiles(filesForASingleComposite);

        // Test getting from ProcessorCache
        ArrayList<DataFileMetaData> result = testProcessorCache.GetUnprocessedCacheFiles();
        assertTrue("Number of results returned is " + result.size(), result.size() == 1);
        ProcessorFileMetaData pData1 = result.get(0).ReadMetaDataForIndices();
        assertTrue("First result Data file path is '" + pData1.dataFilePath + "'", pData1.dataFilePath.equals(data1FilePath));
        assertEquals("First result year is incorrect.", earlierStartDate.getYear(), pData1.year);
        assertEquals("First result day is incorrect.", earlierStartDate.getDayOfYear(), pData1.day);
        result = testProcessorCache.GetUnprocessedCacheFiles();
        assertEquals("Number of results returned is incorrect.", 0, result.size());

        // Test getting from IndicesCache
        result = testIndicesCache.GetUnprocessedCacheFiles();
        assertTrue("Number of results returned is " + result.size(), result.size() == 1);
        IndicesFileMetaData iData1 = result.get(0).ReadMetaDataForSummary();
        assertTrue("First result Data file path is '" + iData1.dataFilePath + "'", iData1.dataFilePath.equals(data1FilePath));
        assertEquals("First result year is incorrect.", earlierStartDate.getYear(), iData1.year);
        assertEquals("First result day is incorrect.", earlierStartDate.getDayOfYear(), iData1.day);
        result = testIndicesCache.GetUnprocessedCacheFiles();
        assertEquals("Number of results returned is incorrect.", 0, result.size());
    }

    /**
     * Test method for {@link version2.prototype.util.DatabaseCache#LoadUnprocessedGlobalDownloadsToLocalDownloader(String, String, String, String, LocalDate, ArrayList, ArrayList, ListDatesFiles)}.
     * Test Requirements:
     *  1) Capable of loading files to LocalDownloader cache.
     *  2) Capable of selectively loading only the modis tiles files that are specified.
     *  3) Capable of selectively only loading files that have a date on or past the given start date.
     *  4) Capable of correctly computing the progress update.
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testLoadUnprocessedGlobalDownloadsToLocalDownloader() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        ResultSet rs = null;
        String schemaName = Schemas.getSchemaName(testProjectName, testPluginName);
        ArrayList<String> modisTileNames = new ArrayList<String>();
        modisTileNames.add(modisTile);
        String dataFilePath1 = "path to data file1 " + modisTile;
        String dataFilePath2 = "path to data file2 " + modisTile;
        String dataFilePath3 = "path to data file3";
        String dataFilePath4 = "path to data file4 " + modisTile;
        String qcFilePath1 = "path to qc file1 " + modisTile;
        String qcFilePath2 = "path to qc file2 " + modisTile;
        String qcFilePath3 = "path to qc file3";
        String qcFilePath4 = "path to qc file4 " + modisTile;
        String query;

        int insertDateGroupID1 = Schemas.getDateGroupID(testGlobalSchema, startDate, stmt);
        int insertDateGroupID2 = Schemas.getDateGroupID(testGlobalSchema, earlierStartDate, stmt);

        query = String.format("INSERT INTO \"%1$s\".\"GlobalDownloader\" (\"PluginID\", \"DataName\") VALUES " +
                "(1, 'Data')," +
                "(1, 'QC');",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\", \"Complete\") VALUES " +
                "(1, " + insertDateGroupID1 + ", '" + dataFilePath1 + "', TRUE), " +
                "(1, " + insertDateGroupID2 + ", '" + dataFilePath2 + "', TRUE), " +
                "(1, " + insertDateGroupID2 + ", '" + dataFilePath3 + "', TRUE), " +
                "(1, " + insertDateGroupID2 + ", 'blah', FALSE);",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"DownloadExtra\" (\"GlobalDownloaderID\", \"DataName\", \"FilePath\", \"DateGroupID\", \"Complete\") VALUES " +
                "(2, 'QC', '" + qcFilePath1 + "', " + insertDateGroupID1 + ", TRUE), " +
                "(2, 'QC', '" + qcFilePath2 + "', " + insertDateGroupID2 + ", TRUE), " +
                "(2, 'QC', '" + qcFilePath3 + "', " + insertDateGroupID2 + ", TRUE);",
                testGlobalSchema);
        stmt.execute(query);

        ListDatesFiles ldf = new MyListDatesFiles(startDate);
        testDownloadCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(testGlobalSchema, testProjectName, testPluginName, "Data", startDate, extraDownloadFiles, modisTileNames, ldf);

        ldf = new MyListDatesFiles(earlierStartDate);
        testDownloadCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(testGlobalSchema, testProjectName, testPluginName, "Data", earlierStartDate, extraDownloadFiles, modisTileNames, ldf);

        query = String.format("INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\", \"Complete\") VALUES " +
                "(1, " + insertDateGroupID2 + ", '" + dataFilePath4 + "', TRUE);",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"DownloadExtra\" (\"GlobalDownloaderID\", \"DataName\", \"FilePath\", \"DateGroupID\", \"Complete\") VALUES " +
                "(2, 'QC', '" + qcFilePath4 + "', " + insertDateGroupID2 + ", TRUE);",
                testGlobalSchema);
        stmt.execute(query);

        testDownloadCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(testGlobalSchema, testProjectName, testPluginName, "Data", earlierStartDate, extraDownloadFiles, modisTileNames, ldf);
        testDownloadCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(testGlobalSchema, testProjectName, testPluginName, "QC", earlierStartDate, extraDownloadFiles, modisTileNames, ldf);

        query = "SELECT * FROM \"" + schemaName + "\".\"DownloadCache\" ORDER BY \"DownloadCacheID\" ASC;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            String path1, path2, path3;
            int downloadID1, downloadID2, downloadID3;
            int dateGroupID1, dateGroupID2, dateGroupID3;

            if(rs.next())
            {
                path3 = dataFilePath4;
                downloadID3= 5;
                dateGroupID3 = 2;

                if(rs.getInt("DateGroupID") == 1)
                {
                    path1 = dataFilePath1;
                    downloadID1 = 1;
                    dateGroupID1 = 1;
                    path2 = dataFilePath2;
                    downloadID2= 2;
                    dateGroupID2 = 2;
                }
                else
                {
                    path1 = dataFilePath2;
                    downloadID1 = 2;
                    dateGroupID1 = 2;
                    path2 = dataFilePath1;
                    downloadID2= 1;
                    dateGroupID2 = 1;
                }

                assertTrue(schemaName + ".DownloadCache Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheID") + ", " + rs.getString("DataFilePath") + ", " +
                        rs.getInt("DownloadID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                        rs.getInt("DownloadCacheID") == rs.getRow() &&
                        rs.getString("DataFilePath").equals(path1) &&
                        rs.getInt("DownloadID") == downloadID1 &&
                        rs.getInt("DateGroupID") == dateGroupID1 &&
                        rs.getBoolean("Retrieved") == false &&
                        rs.getBoolean("Processed") == false);

                if(rs.next())
                {
                    assertTrue(schemaName + ".DownloadCache Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheID") + ", " + rs.getString("DataFilePath") + ", " +
                            rs.getInt("DownloadID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                            rs.getInt("DownloadCacheID") == rs.getRow() &&
                            rs.getString("DataFilePath").equals(path2) &&
                            rs.getInt("DownloadID") == downloadID2 &&
                            rs.getInt("DateGroupID") == dateGroupID2 &&
                            rs.getBoolean("Retrieved") == false &&
                            rs.getBoolean("Processed") == false);

                    if(rs.next())
                    {
                        assertTrue(schemaName + ".DownloadCache Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheID") + ", " + rs.getString("DataFilePath") + ", " +
                                rs.getInt("DownloadID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                                rs.getInt("DownloadCacheID") == rs.getRow() &&
                                rs.getString("DataFilePath").equals(path3) &&
                                rs.getInt("DownloadID") == downloadID3 &&
                                rs.getInt("DateGroupID") == dateGroupID3 &&
                                rs.getBoolean("Retrieved") == false &&
                                rs.getBoolean("Processed") == false);

                        if(rs.next()) {
                            fail("More than 3 files loaded into DownloadCache.");
                        }
                    }
                }
            }
        }
        rs.close();

        query = "SELECT * FROM \"" + schemaName + "\".\"DownloadCacheExtra\" ORDER BY \"DownloadCacheExtraID\" ASC;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            if(rs.next())
            {
                String path1, path2, path3;
                int downloadID1, downloadID2, downloadID3;
                int dateGroupID1, dateGroupID2, dateGroupID3;

                path3 = qcFilePath4;
                downloadID3= 4;
                dateGroupID3 = 2;

                if(rs.getInt("DateGroupID") == 1)
                {
                    path1 = qcFilePath1;
                    downloadID1 = 1;
                    dateGroupID1 = 1;
                    path2 = qcFilePath2;
                    downloadID2= 2;
                    dateGroupID2 = 2;
                }
                else
                {
                    path1 = qcFilePath2;
                    downloadID1 = 2;
                    dateGroupID1 = 2;
                    path2 = qcFilePath1;
                    downloadID2= 1;
                    dateGroupID2 = 1;
                }
                assertTrue(schemaName + ".DownloadCacheExtra Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheExtraID") + ", " + rs.getString("FilePath") + ", " +
                        rs.getInt("DownloadExtraID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                        rs.getInt("DownloadCacheExtraID") == rs.getRow() &&
                        rs.getString("FilePath").equals(path1) &&
                        rs.getInt("DownloadExtraID") == downloadID1 &&
                        rs.getInt("DateGroupID") == dateGroupID1 &&
                        rs.getBoolean("Retrieved") == false &&
                        rs.getBoolean("Processed") == false);

                if(rs.next())
                {
                    assertTrue(schemaName + ".DownloadCacheExtra Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheExtraID") + ", " + rs.getString("FilePath") + ", " +
                            rs.getInt("DownloadExtraID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                            rs.getInt("DownloadCacheExtraID") == rs.getRow() &&
                            rs.getString("FilePath").equals(path2) &&
                            rs.getInt("DownloadExtraID") == downloadID2 &&
                            rs.getInt("DateGroupID") == dateGroupID2 &&
                            rs.getBoolean("Retrieved") == false &&
                            rs.getBoolean("Processed") == false);

                    if(rs.next())
                    {
                        assertTrue(schemaName + ".DownloadCacheExtra Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheExtraID") + ", " + rs.getString("FilePath") + ", " +
                                rs.getInt("DownloadExtraID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                                rs.getInt("DownloadCacheExtraID") == rs.getRow() &&
                                rs.getString("FilePath").equals(path3) &&
                                rs.getInt("DownloadExtraID") == downloadID3 &&
                                rs.getInt("DateGroupID") == dateGroupID3 &&
                                rs.getBoolean("Retrieved") == false &&
                                rs.getBoolean("Processed") == false);

                        if(rs.next())
                        {
                            fail("More than 3 files loaded into DownloadCacheExtra.");
                        }
                    }
                }
            }
        }
        rs.close();
    }

    /**
     * Test method for {@link version2.prototype.util.DatabaseCache#UploadResultsToDb(ArrayList, int, TemporalSummaryCompositionStrategy, int, int, Process, int)}.
     * Test Requirements:
     *  1) Capable of uploading zonal summary results to database.
     *  2) Capable of correctly computing the progress update.
     *
     * @throws SQLException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws UnsupportedOperationException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public final void testUploadResultsToDb() throws SQLException, IllegalArgumentException, UnsupportedOperationException, ClassNotFoundException, IOException, ParserConfigurationException, SAXException,
    NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        int startDateGroupID = Schemas.getDateGroupID(testGlobalSchema, startDate, stmt);
        String indexNm = "ModisNBARNDVI";
        int indexID = Schemas.getIndexID(testGlobalSchema, indexNm, stmt);
        String filePath1 = "Result File 1";

        // Setup for upload 1
        int areaCode = 11;
        String areaName = "Area1";
        double count = 11.0;
        double max = 21.0;
        double min = 1.0;
        double mean = 7.0;
        double sqrSum = 6.0;
        double stdDev = 3.43;
        double sum = 72.0;
        Process process = new MyProcess();
        int projectSummaryID = Schemas.getProjectSummaryID(testGlobalSchema, testProjectName, 1, stmt);
        Map<String, Double> summaryResults = new HashMap<String, Double>();
        summaryResults.put("Count", count);
        summaryResults.put("Max", max);
        summaryResults.put("Min", min);
        summaryResults.put("Mean", mean);
        summaryResults.put("SqrSum", sqrSum);
        summaryResults.put("StdDev", stdDev);
        summaryResults.put("Sum", sum);
        ArrayList<SummaryResult> newResults = new ArrayList<SummaryResult>();
        newResults.add(new SummaryResult(projectSummaryID, areaName, areaCode, startDateGroupID, indexID, filePath1, summaryResults));

        // Upload result
        testSummaryCache.UploadResultsToDb(newResults, 1, compStrategy, startDate.getYear(), startDate.getDayOfYear(), process, 1);

        // Test upload
        String progressQuery = "SELECT \"ProjectSummaryID\", \"DateGroupID\", \"IndexID\", \"AreaCode\", \"AreaName\", \"FilePath\", " +
                "\"Count\", \"Max\", \"Min\", \"Mean\", \"SqrSum\", \"StdDev\", \"Sum\" FROM \"" + testProjectSchema + "\".\"ZonalStat\";";
        ResultSet rs = stmt.executeQuery(progressQuery);
        if(rs != null && rs.next())
        {
            assertEquals("ProjectSummaryID incorrect.", projectSummaryID, rs.getInt("ProjectSummaryID"));
            assertEquals("DateGroupID incorrect.", startDateGroupID, rs.getInt("DateGroupID"));
            assertEquals("IndexID incorrect.", indexID, rs.getInt("IndexID"));
            assertEquals("AreaCode incorrect.", areaCode, rs.getInt("AreaCode"));
            assertEquals("AreaName incorrect.", areaName, rs.getString("AreaName"));
            assertEquals("Count incorrect.", count, rs.getDouble("Count"), 0.0);
            assertEquals("Max incorrect.", max, rs.getDouble("Max"), 0.0);
            assertEquals("Min incorrect.", min, rs.getDouble("Min"), 0.0);
            assertEquals("Mean incorrect.", mean, rs.getDouble("Mean"), 0.0);
            assertEquals("SqrSum incorrect.", sqrSum, rs.getDouble("SqrSum"), 0.0);
            assertEquals("StdDev incorrect.", stdDev, rs.getDouble("StdDev"), 0.0);
            assertEquals("Sum incorrect.", sum, rs.getDouble("Sum"), 0.0);

            assertFalse("More than one record added to ZonalStat.", rs.next());
            rs.close();
        }
    }

    private class MyProcess extends Process
    {
        protected MyProcess() {
            super(null, null, null, null, null, null, null, null);
        }

        @Override
        public void process(ArrayList<DataFileMetaData> cachedFiles) {
            // Do nothing
        }

        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            // Do nothing
        }
    }

    private class MyScheduler extends Scheduler
    {
        public MyScheduler(int myID, SchedulerStatusContainer statusContainer) throws ParserConfigurationException, SAXException, IOException {
            super(null, null, myID, null, null, statusContainer);
        }

        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            // Do nothing
        }

        @Override
        public synchronized void UpdateDownloadProgressByData(String dataName, String pluginName, ListDatesFiles listDatesFiles, ArrayList<String> modisTileNames, Statement stmt) throws SQLException {
            // Do Nothing
        }

        @Override
        public synchronized void UpdateProcessorProgress(String pluginName, Statement stmt) throws SQLException {
            // Do nothing
        }

        @Override
        public synchronized void UpdateIndicesProgress(String pluginName, Statement stmt) throws SQLException {
            // Do nothing
        }

        @Override
        public synchronized void UpdateSummaryProgress(int summaryIDNum, TemporalSummaryCompositionStrategy compStrategy, int daysPerInputData, ProjectInfoPlugin pluginInfo, Statement stmt) throws SQLException {
            // Do nothing
        }
    }

    private class MyListDatesFiles extends ListDatesFiles
    {
        public LocalDate myStartDate;

        public MyListDatesFiles(LocalDate myStartDate) throws IOException, PatternSyntaxException, ParserConfigurationException, SAXException {
            super(new DataDate(myStartDate), new DownloadMetaData(null, null, null, null, null, "FTP", null, null, null, null, filesPerDay, null, null, null), null);
            this.myStartDate = myStartDate;
        }

        @Override
        public Map<DataDate, ArrayList<String>> CloneListDatesFiles() {
            HashMap<DataDate, ArrayList<String>> tempMap = new HashMap<DataDate, ArrayList<String>>();
            ArrayList<String> filesTemp;
            if(myStartDate.isEqual(startDate) || myStartDate.isBefore(startDate))
            {
                filesTemp = new ArrayList<String>();
                filesTemp.add("Blah1 " + modisTile);
                filesTemp.add("Blah3");
                tempMap.put(new DataDate(startDate), filesTemp);
            }
            if(myStartDate.isEqual(earlierStartDate) || myStartDate.isBefore(earlierStartDate))
            {
                filesTemp = new ArrayList<String>();
                filesTemp.add("Blah2 " + modisTile);
                filesTemp.add("Blah5 " + modisTile);
                filesTemp.add("Blah4");
                tempMap.put(new DataDate(earlierStartDate), filesTemp);
            }
            return tempMap;
        }

        @Override
        protected Map<DataDate, ArrayList<String>> ListDatesFilesFTP() {
            return null;
        }

        @Override
        protected Map<DataDate, ArrayList<String>> ListDatesFilesHTTP() {
            return null;
        }
    }
}
