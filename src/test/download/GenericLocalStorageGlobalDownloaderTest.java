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
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.FTP;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.HTTP;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.download.GenericLocalStorageGlobalDownloader;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.TRMM3B42RT.TRMM3B42RTListDatesFiles;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class GenericLocalStorageGlobalDownloaderTest {
    private static Config testConfig;
    private static String testProjectName = "Test_Project";
    private static String testPluginName = "TRMM3B42RT";
    private static String testGlobalSchema;        // Test_EASTWeb
    private static Connection con;
    private static ArrayList<String> extraDownloadFiles;
    private static int year = 2015;
    private static int day = 1;
    private static int daysPerInputFile = -1;
    private static int numOfIndices = 3;
    private static int filesPerDay = 1;
    private static LocalDate startDate;
    private static DownloadMetaData dData;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        testConfig = Config.getAnInstance("src/test/config.xml");
        testGlobalSchema = testConfig.getGlobalSchema();        // Test_EASTWeb
        con = PostgreSQLConnection.getConnection();
        extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");
        startDate = LocalDate.ofYearDay(year, day);

        String mode = "FTP";// the protocol type: ftp or http
        FTP myFtp = PluginMetaDataCollection.CreateFTP("disc2.nascom.nasa.gov",
                "/data/TRMM/Gridded/Derived_Products/3B42RT/Daily/", "anonymous", "anonymous");
        HTTP myHttp = null;
        String className = null;
        String timeZone = null;
        int filesPerDay = 1;
        String datePatternStr = "\\d{4}";
        String fileNamePatternStr = "3B42RT_daily\\.(\\d{4})\\.(\\d{2})\\.(\\d{2})\\.bin";
        LocalDate ld = LocalDate.parse("Jul 01 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("MMM dd HH:mm:ss zzz uuuu"));

        dData = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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
        stmt.close();

        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), testGlobalSchema, testProjectName, testPluginName, null, extraDownloadFiles,
                LocalDate.ofYearDay(year, day), daysPerInputFile, filesPerDay, numOfIndices, null, true);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.download.GenericLocalStorageGlobalDownloader#GenericLocalStorageGlobalDownloader(int, version2.prototype.Config, java.lang.String, version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData, version2.prototype.download.ListDatesFiles, java.time.LocalDate, java.lang.String)}.
     * @throws Exception
     */
    @Test
    public final void testGenericLocalStorageGlobalDownloader() throws Exception {
        // Setup
        ListDatesFiles listDatesFiles = new TRMM3B42RTListDatesFiles(new DataDate(startDate), dData);
        String downloaderClassName = "TRMM3B42RTDownloader";
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
        rs.close();
        stmt.close();

        // Test GlobalDownloader inits
        assertEquals("GDL running state incorrect.",TaskState.STOPPED, gdl.GetRunningState());
        assertEquals("GDL start date not as expected.", startDate, gdl.GetStartDate());
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
        int testYear = 2015;
        int testDay = 182;

        FileUtils.deleteDirectory(new File(testConfig.getDownloadDir() + testPluginName));

        // Setup
        ListDatesFiles listDatesFiles = new TRMM3B42RTListDatesFiles(new DataDate(dData.originDate), dData);
        String downloaderClassName = "TRMM3B42RTDownloader";
        GenericLocalStorageGlobalDownloader gdl = new GenericLocalStorageGlobalDownloader(1, testConfig, testPluginName, dData, listDatesFiles, startDate, downloaderClassName);
        MyObserver watcher = new MyObserver();
        gdl.addObserver(watcher);

        // Run GenericLocalStorageGlobalDownloader
        gdl.run();

        // Test Run. results were written and stored
        String testFilePath = testConfig.getDownloadDir() + testPluginName+ "/" + testYear + "/" + testDay + "/3B42RT_daily.2015.07.01.bin";
        File temp = new File(testFilePath);
        assertTrue("Expected file doesn't exist at '" + temp.getCanonicalPath() + "'.", temp.exists());
        Connection con = PostgreSQLConnection.getConnection();
        Statement stmt = con.createStatement();
        int gdlID = Schemas.getGlobalDownloaderID(testConfig.getGlobalSchema(), testPluginName, stmt);
        int dateGroupId = Schemas.getDateGroupID(testConfig.getGlobalSchema(), LocalDate.ofYearDay(testYear, testDay), stmt);
        int downloadId = Schemas.getDownloadID(testConfig.getGlobalSchema(), gdlID, dateGroupId, stmt);
        assertTrue("Download ID not found.", downloadId > -1);
        stmt.close();

        // Test PerformUpdates.
        gdl.PerformUpdates();
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
        PluginMetaDataCollection pluginMetaDataCol = PluginMetaDataCollection.getInstance();
        PluginMetaData pluginMetaData = pluginMetaDataCol.pluginMetaDataMap.get("TRMM3B42RT");
        ListDatesFiles listDatesFiles = new TRMM3B42RTListDatesFiles(new DataDate(startDate), pluginMetaData.Download);
        String downloaderClassName = "TRMM3B42RTDownloader";
        GenericLocalStorageGlobalDownloader gdl = new GenericLocalStorageGlobalDownloader(1, testConfig, testPluginName, pluginMetaData.Download, listDatesFiles, startDate, downloaderClassName);

        assertEquals("GDL start date not as expected.", startDate, gdl.GetStartDate());
        gdl.SetStartDate(startDate.plusDays(1));
        assertEquals("GDL start date not as expected.", startDate, gdl.GetStartDate());
        gdl.SetStartDate(startDate.minusDays(1));
        assertEquals("GDL start date not as expected.", startDate.minusDays(1), gdl.GetStartDate());
    }

    /**
     * Test method for {@link version2.prototype.download.GlobalDownloader#GetAllDownloadedFiles()}.
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

        // Setup
        PluginMetaDataCollection pluginMetaDataCol = PluginMetaDataCollection.getInstance();
        PluginMetaData pluginMetaData = pluginMetaDataCol.pluginMetaDataMap.get("TRMM3B42RT");
        ListDatesFiles listDatesFiles = new TRMM3B42RTListDatesFiles(new DataDate(startDate), pluginMetaData.Download);
        String downloaderClassName = "TRMM3B42RTDownloader";
        GenericLocalStorageGlobalDownloader gdl = new GenericLocalStorageGlobalDownloader(1, testConfig, testPluginName, pluginMetaData.Download, listDatesFiles, startDate, downloaderClassName);

        query = String.format("INSERT INTO \"%1$s\".\"DateGroup\" (\"DayOfYear\", \"Year\") VALUES (" + LocalDate.now().getDayOfYear() + ", " + LocalDate.now().getYear() + ")",
                testGlobalSchema);
        stmt.execute(query);
        query = String.format("INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\") VALUES (1, 1, '" + dateFilePath1 + "'), (1, 2, '" + dateFilePath2 +
                "');",
                testGlobalSchema);
        stmt.execute(query);
        query = String.format("INSERT INTO \"%1$s\".\"ExtraDownload\" (\"DownloadID\", \"DataName\", \"FilePath\") VALUES (1, 'QC', '" + qcFilePath1 + "'), (2, 'QC', '" + qcFilePath2 + "');",
                testGlobalSchema);
        stmt.execute(query);

        ArrayList<DataFileMetaData> testResults = gdl.GetAllDownloadedFiles();

        assertTrue("Test Results contains " + testResults.size() + " rows.", testResults.size() == 2);

        DownloadFileMetaData dData;
        for(int i=0; i < testResults.size(); i++)
        {
            if(i == 0)
            {
                dData = testResults.get(i).ReadMetaDataForProcessor();
                assertTrue("Data " + i + " dataName is " + dData.dataName, dData.dataName.equals("Data"));
                assertTrue("Data " + i + " dataFilePath is " + dData.dataFilePath, dData.dataFilePath.equals(dateFilePath1));
                assertTrue("Data " + i + " day is " + dData.day, dData.day == startDate.getDayOfYear());
                assertTrue("Data " + i + " year is " + dData.year, dData.year == startDate.getYear());
                assertTrue("Data " + i + ".ExtraDownloads size is " + dData.extraDownloads.size(), dData.extraDownloads.size() == 1);
                dData = dData.extraDownloads.get(0);
                assertTrue("Data " + i + ".ExtraDownloads[0].dataName is " + dData.dataName, dData.dataName.equals("QC"));
                assertTrue("Data " + i + ".ExtraDownloads[0].dataFilePath is " + dData.dataFilePath, dData.dataFilePath.equals(qcFilePath1));
                assertTrue("Data " + i + ".ExtraDownloads[0].day is " + dData.day + " not " + startDate.getDayOfYear(), dData.day == startDate.getDayOfYear());
                assertTrue("Data " + i + ".ExtraDownloads[0].year is " + dData.year + " not " + startDate.getYear(), dData.year == startDate.getYear());
            }
            else if(i == 1)
            {
                dData = testResults.get(i).ReadMetaDataForProcessor();
                assertTrue("Data " + i + " dataName is " + dData.dataName, dData.dataName.equals("Data"));
                assertTrue("Data " + i + " dataFilePath is " + dData.dataFilePath, dData.dataFilePath.equals(dateFilePath2));
                assertTrue("Data " + i + " day is " + dData.day, dData.day == LocalDate.now().getDayOfYear());
                assertTrue("Data " + i + " year is " + dData.year, dData.year == LocalDate.now().getYear());
                assertTrue("Data " + i + ".ExtraDownloads size is " + dData.extraDownloads.size(), dData.extraDownloads.size() == 1);
                dData = dData.extraDownloads.get(0);
                assertTrue("Data " + i + ".ExtraDownloads[0].dataName is " + dData.dataName, dData.dataName.equals("QC"));
                assertTrue("Data " + i + ".ExtraDownloads[0].dataFilePath is " + dData.dataFilePath, dData.dataFilePath.equals(qcFilePath2));
                assertTrue("Data " + i + ".ExtraDownloads[0].day is " + dData.day + " not " + LocalDate.now().getDayOfYear(), dData.day == LocalDate.now().getDayOfYear());
                assertTrue("Data " + i + ".ExtraDownloads[0].year is " + dData.year + " not " + LocalDate.now().getYear(), dData.year == LocalDate.now().getYear());
            }
            else{
                fail("TestResults size is " + testResults.size());
            }
        }
        stmt.close();
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
