/**
 *
 */
package test.download;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.TaskState;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.Scheduler.SchedulerStatusContainer;
import version2.prototype.download.GenericLocalRetrievalLocalDownloader;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.RegistrationException;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GeneralUIEventObject;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class GenericLocalRetrievalLocalDownloaderTest {
    private static Config testConfig;
    private static String testProjectName = "Test_Project";
    private static String testPluginName = "ModisNBAR";
    private static String testGlobalSchema;
    private static Connection con;
    private static ArrayList<String> extraDownloadFiles;
    private static int year = LocalDate.now().minusDays(8).getYear();
    private static int day = LocalDate.now().minusDays(8).getDayOfYear();
    private static int daysPerInputFile = 1;
    private static int numOfIndices = 1;
    private static int filesPerDay = 1;
    private static LocalDate startDate;
    private static ArrayList<ProjectInfoSummary> summaries;
    private static ProjectInfoPlugin pluginInfo;
    private static PluginMetaData pluginMetaData;
    private static ProjectInfoFile projectInfoFile;
    private static Scheduler scheduler;
    private static MyDatabaseCache outputCache;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        testConfig = Config.getAnInstance("src/test/config.xml");
        testGlobalSchema = testConfig.getGlobalSchema();        // Test_EASTWeb
        con = DatabaseConnector.getConnection();
        extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");
        startDate = LocalDate.ofYearDay(year, day);
        ArrayList<String> indices  = new ArrayList<String>();
        indices.add("TRMM3B42RTCalculator");
        ArrayList<String> modis  = new ArrayList<String>();
        indices.add("h01v01");
        pluginMetaData = PluginMetaDataCollection.CreatePluginMetaData(null, 1, null, extraDownloadFiles, null, null, null, null, null, null);
        pluginInfo = new ProjectInfoPlugin(testPluginName, indices, "Level 1", modis);
        ArrayList<ProjectInfoPlugin> plugins = new ArrayList<ProjectInfoPlugin>();
        plugins.add(pluginInfo);
        summaries = new ArrayList<ProjectInfoSummary>();
        summaries.add(new ProjectInfoSummary(new ZonalSummary("", "", ""),
                null,
                1));

        projectInfoFile = new ProjectInfoFile(plugins, startDate, testProjectName, "C:/Users/michael.devos/Desktop/EASTWeb", "", null, "", ZoneId.systemDefault().getId(), null,
                null, null, null, null, null, summaries);

        GenericLocalRetrievalLocalDownloaderTest tester = new GenericLocalRetrievalLocalDownloaderTest();
        scheduler = tester.new MyScheduler(1, testConfig);
        outputCache = tester.new MyDatabaseCache(scheduler, testGlobalSchema, testProjectName, pluginInfo, pluginMetaData, null, ProcessName.DOWNLOAD);
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
        outputCache.expectedStartDate = startDate;
        Statement stmt = con.createStatement();
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testGlobalSchema
                ));
        stmt.close();

        Schemas.CreateProjectPluginSchema(con, testGlobalSchema, projectInfoFile, testPluginName, null, null, daysPerInputFile, filesPerDay, true);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.download.LocalDownloader#AttemptUpdate()}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws RegistrationException
     * @throws PatternSyntaxException
     */
    @Test
    public final void testAttemptUpdate() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException, NoSuchMethodException, SecurityException, PatternSyntaxException, RegistrationException {
        LocalDate startDate2 = LocalDate.ofYearDay(year, day).minusDays(8);
        MyGlobalDownloader gdl = new MyGlobalDownloader(1, testConfig, testPluginName, new DownloadMetaData(null, null, null, null, null, "Data", null, null, null, null, filesPerDay, "", "", null, null),
                null, startDate);
        GenericLocalRetrievalLocalDownloader ldlData = new GenericLocalRetrievalLocalDownloader(testConfig, gdl, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache, null);

        // Test Global Downloader performed updates and Local Downloader loaded new updates with correct start date
        ldlData.AttemptUpdate();
        assertEquals("GlobalDownloader performed updates", true, gdl.performedUpdates);
        gdl.performedUpdates = false;

        // Update start date
        ldlData.SetStartDate(startDate2);
        outputCache.expectedStartDate = startDate2;

        // Test Global Downloader performed updates and Local Downloader loaded new updates with correct start date
        ldlData.AttemptUpdate();
        assertEquals("GlobalDownloader performed updates", true, gdl.performedUpdates);
    }

    /**
     * Test method for {@link version2.prototype.download.LocalDownloader#SetStartDate(java.time.LocalDate)}.
     * @throws Exception
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @Test
    public final void testSetStartDate() throws ParserConfigurationException, SAXException, IOException, Exception {
        GenericLocalRetrievalLocalDownloader ldl = new GenericLocalRetrievalLocalDownloader(testConfig,
                new MyGlobalDownloader(1, testConfig, testPluginName,
                        new DownloadMetaData(null, null, null, null, null, "Data", null, null, null, null, filesPerDay, "", "", null, null),
                        null, startDate), projectInfoFile, pluginInfo, pluginMetaData, null, outputCache, null);

        assertEquals("LDL start date not as expected.", startDate, ldl.GetStartDate());
        ldl.SetStartDate(startDate.plusDays(1));
        assertEquals("LDL start date not as expected.", startDate, ldl.GetStartDate());
        ldl.SetStartDate(startDate.minusDays(1));
        assertEquals("LDL start date not as expected.", startDate.minusDays(1), ldl.GetStartDate());
    }

    private class MyScheduler extends Scheduler
    {
        public MyScheduler(int myID, Config configInstance) throws ParserConfigurationException, SAXException, IOException {
            super(null, null, false, myID, configInstance, null, new SchedulerStatusContainer(configInstance, 1, null, null, null, null, null, TaskState.RUNNING, null, null, null, null, false, null));
        }

        @Override
        protected void SetupProcesses(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException,
        IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, IOException, ParserConfigurationException, SAXException {
            // Do nothing
        }

        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            // Do nothing
        }
    }

    private class MyGlobalDownloader extends GlobalDownloader
    {
        public boolean performedUpdates;

        public MyGlobalDownloader(int myID, Config configInstance, String pluginName, DownloadMetaData metaData, ListDatesFiles listDatesFiles, LocalDate startDate) throws ClassNotFoundException,
        ParserConfigurationException, SAXException, IOException, SQLException, RegistrationException {
            super(myID, configInstance, pluginName, metaData, listDatesFiles, startDate);
            performedUpdates = false;
        }

        @Override
        public void run() {
            // do nothing
        }

        @Override
        protected void RegisterGlobalDownloader(Statement stmt) throws SQLException {
            // do nothing
        }

        @Override
        public void SetCompleted() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
            performedUpdates = true;
        }
    }

    private class MyDatabaseCache extends DatabaseCache
    {
        public LocalDate expectedStartDate;

        public MyDatabaseCache(Scheduler scheduler, String globalSchema, String projectName, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, String workingDir, ProcessName processCachingFor)
                throws ParseException {
            super(scheduler, globalSchema, projectName, pluginInfo, pluginMetaData, workingDir, processCachingFor);
        }

        @Override
        public int LoadUnprocessedGlobalDownloadsToLocalDownloader(String globalEASTWebSchema, String projectName, String pluginName, String dataName, LocalDate startDate,
                ArrayList<String> extraDownloadFiles, ArrayList<String> modisTileNames, ListDatesFiles listDatesFiles) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException,
                IOException {
            assertEquals("StartDate incorrect.", expectedStartDate, startDate);
            return 1;
        }
    }
}
