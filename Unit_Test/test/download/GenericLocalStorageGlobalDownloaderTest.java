/**
 *
 */
package test.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.FTP;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.PluginMetaData.HTTP;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.download.GenericLocalStorageGlobalDownloader;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.TRMM3B42RT.TRMM3B42RTListDatesFiles;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class GenericLocalStorageGlobalDownloaderTest {
    private static Config testConfig;
    private static String testProjectName = "Test_Project";
    private static String testPluginName = "TRMM3B42RT";
    private static String testGlobalSchema;        // Test_EASTWeb1
    private static Connection con;
    private static ArrayList<String> extraDownloadFiles;
    private static LocalDate startDate;
    private static DownloadMetaData dData;
    private static PluginMetaData pluginMetaData;
    private static ListDatesFiles listDatesFiles;
    private static ListDatesFiles listDatesFilesQC;
    private static ProjectInfoFile projectMetaData;

    // For testing with ModisNBAR plugin
    //    private static String testPluginName = "ModisNBAR";
    //    private static int year = 2015;
    //    private static int day = 1;
    //    private static int daysPerInputFile = 8;
    //    private static int numOfIndices = 5;
    //    private static int filesPerDay = 322;

    // For testing with TRMM3B42RT plugin
    private static int daysPerInputFile = -1;
    private static int numOfIndices = 3;
    private static int filesPerDay = 1;

    private static boolean hasQC;


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        testConfig = Config.getAnInstance("src/test/config.xml");
        testGlobalSchema = testConfig.getGlobalSchema();        // Test_EASTWeb1
        con = DatabaseConnector.getConnection();
        extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");
        startDate = LocalDate.now().minusDays(3);

        String className = null;
        String timeZone = null;
        String datePatternStr = "\\d{4}";

        // For testing with ModisNBAR plugin
        //        hasQC = true;
        //        String mode = "HTTP";// the protocol type: ftp or http
        //        HTTP myHttp = new HTTP("http://e4ftl01.cr.usgs.gov/MOTA/MCD43B4.005/");
        //        FTP myFtp = null;
        //        String fileNamePatternStr = "MCD43B4.A(\\d{7}).h(\\d{2})v(\\d{2}).005.(\\d{13}).hdf";
        //        LocalDate ld = LocalDate.parse("Feb 18 00:00:01 CDT 2000", DateTimeFormatter.ofPattern("MMM dd HH:mm:ss zzz uuuu"));

        // For testing with TRMM3B42RT plugin
        hasQC = false;
        String mode = "FTP";// the protocol type: ftp or http
        FTP myFtp = new FTP("disc2.nascom.nasa.gov",
                "/data/TRMM/Gridded/Derived_Products/3B42RT/Daily/", "anonymous", "anonymous");
        HTTP myHttp = null;
        int filesPerDay = 1;
        String fileNamePatternStr = "3B42RT_daily\\.(\\d{4})\\.(\\d{2})\\.(\\d{2})\\.bin";
        LocalDate ld = LocalDate.parse("Jul 01 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("MMM dd HH:mm:ss zzz uuuu"));

        dData = new DownloadMetaData(null, null, null, null, null, mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);

        PluginMetaDataCollection pluginMetaDataCol = PluginMetaDataCollection.getInstance("plugins/Plugin_TRMM3B42RT.xml");
        pluginMetaData = pluginMetaDataCol.pluginMetaDataMap.get(testPluginName);
        // For testing with ModisNBAR plugin
        //        listDatesFiles = new ModisNBARListDatesFiles(new DataDate(startDate), pluginMetaData.Download);
        //        listDatesFilesQC = new ModisNBARQCListDatesFiles(new DataDate(startDate), pluginMetaData.Download);
        // For testing with TRMM3B42RT plugin
        listDatesFiles = new TRMM3B42RTListDatesFiles(new DataDate(startDate), dData, null);
        projectMetaData = new ProjectInfoFile(null, startDate, testProjectName, null, null, null, timeZone, null, null, null, null, null, null, null, null);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Statement stmt = con.createStatement();
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testGlobalSchema
                ));
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                Schemas.getSchemaName(testProjectName, testPluginName)
                ));
        stmt.close();
        con.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Statement stmt = con.createStatement();
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testGlobalSchema
                ));
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                Schemas.getSchemaName(testProjectName, testPluginName)
                ));
        stmt.close();

        Schemas.CreateProjectPluginSchema(con, testGlobalSchema, projectMetaData, testPluginName, null, null, daysPerInputFile, filesPerDay, true);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /*@Test
        public final void testManual() throws Exception
        {
            String pluginName = "TRMM3B42RT";
            FileUtils.deleteDirectory(new File(Config.getInstance().getDownloadDir() + pluginName));

            // Setup
            PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File("src/test/download/Test_" + pluginName + ".xml")).pluginMetaDataMap.get("Test_" + pluginName);
            DownloadMetaData downloadData = pluginMetaData.Download;
            ListDatesFiles listDatesFiles = new TRMM3B42RTListDatesFiles(new DataDate(downloadData.originDate), downloadData);
            String downloaderClassName = pluginName + "Downloader";
            GenericLocalStorageGlobalDownloader gdl = new GenericLocalStorageGlobalDownloader(1, Config.getInstance(), pluginName, downloadData, listDatesFiles, startDate, downloaderClassName);

            // Run GenericLocalStorageGlobalDownloader
            gdl.run();

            // Test Run. results were written and stored
            String testFilePath = testConfig.getDownloadDir() + testPluginName+ "/" + 2015 + "/" + 182 + "/3B42RT_daily.2015.07.01.bin";
            File temp = new File(testFilePath);
            assertTrue("Expected file doesn't exist at '" + temp.getCanonicalPath() + "'.", temp.exists());
        }*/

    /**
     * Test method for {@link version2.prototype.download.GenericLocalStorageGlobalDownloader#GenericLocalStorageGlobalDownloader(int, version2.prototype.Config, java.lang.String,
     * version2.prototype.PluginMetaData.DownloadMetaData, version2.prototype.download.ListDatesFiles, java.time.LocalDate, java.lang.String)}.
     * @throws Exception
     */
    @Test
    public final void testGenericLocalStorageGlobalDownloader() throws Exception {
        // Setup
        String downloaderClassName = testPluginName + "Downloader";
        GenericLocalStorageGlobalDownloader gdl = new GenericLocalStorageGlobalDownloader(1, testConfig, testPluginName, dData, listDatesFiles, startDate, downloaderClassName);

        // Verify registering GlobalDownloader
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        int pluginID = Schemas.getPluginID(testGlobalSchema, testPluginName, stmt);
        String query = String.format(
                "SELECT \"GlobalDownloaderID\" FROM \"%1$s\".\"GlobalDownloader\" WHERE \"PluginID\"=" + pluginID + ";",
                testGlobalSchema
                );
        rs = stmt.executeQuery(query);
        assertNotNull("ResultSet returned null.", rs);
        if(rs != null)
        {
            rs.next();
            assertTrue("More than one GlobalDownloader with the same plugin '" + testPluginName + "'", rs.next() == false);
        }

        // Test GlobalDownloader inits
        assertEquals("GDL running state incorrect.",TaskState.STOPPED, gdl.GetRunningState());
        assertEquals("GDL start date not as expected.", startDate, gdl.GetStartDate());

        // Test for creating duplicate GDL
        new GenericLocalStorageGlobalDownloader(1, testConfig, testPluginName, dData, listDatesFiles, startDate, downloaderClassName);
        query = String.format(
                "SELECT \"GlobalDownloaderID\" FROM \"%1$s\".\"GlobalDownloader\" WHERE \"PluginID\"=" + pluginID + ";",
                testGlobalSchema
                );
        rs = stmt.executeQuery(query);
        assertNotNull("ResultSet returned null.", rs);
        if(rs != null)
        {
            rs.next();
            assertTrue("More than one GlobalDownloader with the same plugin '" + testPluginName + "'", rs.next() == false);
            rs.close();
        }
        stmt.close();
    }

    /**
     * Test method for {@link version2.prototype.download.GenericLocalStorageGlobalDownloader#run()}.
     * @throws Exception
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @Test
    public final void testRunAndPerformUpdates() throws ParserConfigurationException, SAXException, IOException, Exception {
        File temp = new File(testConfig.getDownloadDir() + testPluginName);
        if(temp.exists()) {
            FileUtils.deleteDirectory(temp);
        }

        // Setup
        String downloaderClassName = testPluginName + "Downloader";
        GenericLocalStorageGlobalDownloader gdl = new GenericLocalStorageGlobalDownloader(1, testConfig, testPluginName, dData, listDatesFiles, startDate, downloaderClassName);
        MyObserver watcher = new MyObserver();
        gdl.addObserver(watcher);

        // Run GenericLocalStorageGlobalDownloader
        gdl.run();

        // Test Run. results were written and stored
        // For testing with ModisNBAR plugin
        //        String testFilePath = testConfig.getDownloadDir() + testPluginName+ "/" + testYear + "/" + testDay + "/h00v08";
        // For testing with TRMM3B42RT plugin
        //        String testFilePath = testConfig.getDownloadDir() + testPluginName+ "/" + testYear + "/" + testDay + "/3B42RT_daily.2015.07.01.bin";
        String testFilePath = testConfig.getDownloadDir() + testPluginName+ "/data/" + startDate.getYear() + "/" + startDate.getDayOfYear() + "/3B42RT_daily." + startDate.getYear() + "." + String.format("%02d", startDate.getMonthValue())
                + "." + String.format("%02d", startDate.getDayOfMonth()) + ".bin";
        temp = new File(testFilePath);
        assertTrue("Expected file doesn't exist at '" + temp.getCanonicalPath() + "'.", temp.exists());
        Statement stmt = con.createStatement();
        int gdlID = Schemas.getGlobalDownloaderID(testConfig.getGlobalSchema(), testPluginName, dData.name, stmt);
        int dateGroupId = Schemas.getDateGroupID(testConfig.getGlobalSchema(), LocalDate.ofYearDay(startDate.getYear(), startDate.getDayOfYear()), stmt);
        int downloadId = Schemas.getDownloadID(testConfig.getGlobalSchema(), gdlID, dateGroupId, stmt);
        assertTrue("Download ID not found.", downloadId > -1);
        stmt.close();

        // Test PerformUpdates.
        gdl.SetCompleted();
        assertTrue("Observer wasn't called.", watcher.success);
    }

    /**
     * Test method for {@link version2.prototype.download.GlobalDownloader#SetStartDate(java.time.LocalDate)}.
     * @throws Exception
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @Test
    public final void testSetStartDate() throws ParserConfigurationException, SAXException, IOException, Exception {
        // Setup
        String downloaderClassName = testPluginName + "Downloader";
        GenericLocalStorageGlobalDownloader gdl = new GenericLocalStorageGlobalDownloader(1, testConfig, testPluginName, pluginMetaData.Download, listDatesFiles, startDate, downloaderClassName);

        assertEquals("GDL start date not as expected.", startDate, gdl.GetStartDate());
        gdl.SetStartDate(startDate.plusDays(1));
        assertEquals("GDL start date not as expected.", startDate, gdl.GetStartDate());
        gdl.SetStartDate(startDate.minusDays(1));
        assertEquals("GDL start date not as expected.", startDate.minusDays(1), gdl.GetStartDate());
    }

    /**
     * Test method for {@link version2.prototype.download.GlobalDownloader#GetAllDownloadedFiles()} and {@link version2.prototype.download.GlobalDownloader#GetAllDownloadedFiles(java.time.LocalDate)}.
     * @throws Exception
     */
    @Test
    public final void testGetAllDownloadedFiles() throws Exception {
        Statement stmt = con.createStatement();
        String dateFilePath1 = "path to data file1";
        String dateFilePath2 = "path to data file2";
        String qcFilePath1 = "path to qc file1";
        String qcFilePath2 = "path to qc file2";
        String query;
        GenericLocalStorageGlobalDownloader gdlQC = null;
        ArrayList<DataFileMetaData> testResultsQC1 = null;
        ArrayList<DataFileMetaData> testResultsQC2 = null;

        // Setup Data GlobalDownloader
        String downloaderClassName = testPluginName + "Downloader";
        GenericLocalStorageGlobalDownloader gdlData = new GenericLocalStorageGlobalDownloader(1, testConfig, testPluginName, pluginMetaData.Download, listDatesFiles, startDate, downloaderClassName);

        // Setup QC GlobalDownloader
        if(hasQC)
        {
            downloaderClassName = testPluginName + "QC" + "Downloader";
            gdlQC = new GenericLocalStorageGlobalDownloader(2, testConfig, testPluginName, pluginMetaData.Download.extraDownloads.get(0), listDatesFilesQC, startDate,
                    downloaderClassName);
        }

        // Setup database entries
        query = String.format("INSERT INTO \"%1$s\".\"DateGroup\" (\"DayOfYear\", \"Year\") VALUES " +
                "(" + LocalDate.now().getDayOfYear() + ", " + LocalDate.now().getYear() + ")",
                testGlobalSchema);
        stmt.execute(query);
        query = String.format("INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\", \"Complete\") VALUES " +
                "(1, 1, '" + dateFilePath1 + "', TRUE), " +
                "(1, 2, '" + dateFilePath2 + "', FALSE);",
                testGlobalSchema);
        stmt.execute(query);
        if(hasQC)
        {
            query = String.format("INSERT INTO \"%1$s\".\"DownloadExtra\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataName\", \"FilePath\", \"Complete\") VALUES " +
                    "(2, 1, 'QC', '" + qcFilePath1 + "', TRUE), " +
                    "(2, 2, 'QC', '" + qcFilePath2 + "', FALSE);",
                    testGlobalSchema);
            stmt.execute(query);
        }
        stmt.close();

        // Get results
        ArrayList<DataFileMetaData> testResultsData1 = gdlData.GetAllDownloadedFiles();
        ArrayList<DataFileMetaData> testResultsData2 = gdlData.GetAllDownloadedFiles(LocalDate.now().minusDays(1));
        if(hasQC)
        {
            testResultsQC1 = gdlQC.GetAllDownloadedFiles();
            testResultsQC2 = gdlQC.GetAllDownloadedFiles(LocalDate.now().minusDays(1));
        }

        // Test number of rows returned
        assertEquals("testResultsData1 incorrect.", 2, testResultsData1.size());
        assertEquals("testResultsData2 incorrect.", 1, testResultsData2.size());
        if(hasQC)
        {
            assertEquals("testResultsQC1 incorrect.", 2, testResultsQC1.size());
            assertEquals("testResultsQC2 incorrect.", 1, testResultsQC2.size());
        }

        // Test values returned for gdlData
        DownloadFileMetaData dData;
        for(int i=0; i < testResultsData1.size(); i++)
        {
            dData = testResultsData1.get(i).ReadMetaDataForProcessor();
            // DateGroupID = 1
            if(dData.day == startDate.getDayOfYear())
            {
                assertTrue("testResultsData1[" + i + "] dataName is " + dData.dataName, dData.dataName.equals("Data"));
                assertTrue("testResultsData1[" + i + "] dataFilePath is " + dData.dataFilePath, dData.dataFilePath.equals(dateFilePath1));
                assertTrue("testResultsData1[" + i + "] day is " + dData.day, dData.day == startDate.getDayOfYear());
                assertTrue("testResultsData1[" + i + "] year is " + dData.year, dData.year == startDate.getYear());
            }
            // DateGroupID = 2
            else
            {
                assertTrue("testResultsData1[" + i + "] dataName is " + dData.dataName, dData.dataName.equals("Data"));
                assertTrue("testResultsData1[" + i + "] dataFilePath is " + dData.dataFilePath, dData.dataFilePath.equals(dateFilePath2));
                assertTrue("testResultsData1[" + i + "] day is " + dData.day, dData.day == LocalDate.now().getDayOfYear());
                assertTrue("testResultsData1[" + i + "] year is " + dData.year, dData.year == LocalDate.now().getYear());
            }
        }
        dData = testResultsData2.get(0).ReadMetaDataForProcessor();
        assertEquals("testResultsData2[0] dataName is incorrect", "Data", dData.dataName);
        assertEquals("testResultsData2[0] dataFilePath is incorrect", dateFilePath2, dData.dataFilePath);
        assertEquals("testResultsData2[0] day is incorrect", LocalDate.now().getDayOfYear(), dData.day);
        assertEquals("testResultsData2[0] year is incorrect", LocalDate.now().getYear(), dData.year);

        // Test values returned for gdlQC
        if(hasQC)
        {
            for(int i=0; i < testResultsQC1.size(); i++)
            {
                dData = testResultsQC1.get(i).ReadMetaDataForProcessor();
                // DateGroupID = 1
                if(dData.day == startDate.getDayOfYear())
                {
                    assertTrue("testResultsQC1[" + i + "] dataName is " + dData.dataName, dData.dataName.equals("QC"));
                    assertTrue("testResultsQC1[" + i + "] dataFilePath is " + dData.dataFilePath, dData.dataFilePath.equals(qcFilePath1));
                    assertTrue("testResultsQC1[" + i + "] day is " + dData.day, dData.day == startDate.getDayOfYear());
                    assertTrue("testResultsQC1[" + i + "] year is " + dData.year, dData.year == startDate.getYear());
                }
                // DateGroupID = 2
                else
                {
                    assertTrue("testResultsQC1[" + i + "] dataName is " + dData.dataName, dData.dataName.equals("QC"));
                    assertTrue("testResultsQC1[" + i + "] dataFilePath is " + dData.dataFilePath, dData.dataFilePath.equals(qcFilePath2));
                    assertTrue("testResultsQC1[" + i + "] day is " + dData.day, dData.day == LocalDate.now().getDayOfYear());
                    assertTrue("testResultsQC1[" + i + "] year is " + dData.year, dData.year == LocalDate.now().getYear());
                }
            }
            dData = testResultsQC2.get(0).ReadMetaDataForProcessor();
            assertEquals("testResultsQC2[0] dataName is incorrect", "QC", dData.dataName);
            assertEquals("testResultsQC2[0] dataFilePath is incorrect", qcFilePath2, dData.dataFilePath);
            assertEquals("testResultsQC2[0] day is incorrect", LocalDate.now().getDayOfYear(), dData.day);
            assertEquals("testResultsQC2[0] year is incorrect", LocalDate.now().getYear(), dData.year);
        }
    }

    private class MyObserver implements Observer
    {
        public boolean success;

        public MyObserver() { success = false; }

        @Override
        public void update(Observable o, Object arg1) {
            if(o instanceof GenericLocalStorageGlobalDownloader)
            {
                success = true;
            } else {
                fail("Didn't understand who sent update notification.");
            }

        }
    }
}
