/**
 *
 */
package test.download;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.EASTWebI;
import version2.prototype.TaskState;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.Scheduler.SchedulerData;
import version2.prototype.download.GenericLocalRetrievalLocalDownloader;
import version2.prototype.download.GenericLocalStorageGlobalDownloader;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class GenericLocalRetrievalLocalDownloaderTest {
    private static Config testConfig;
    private static String testProjectName = "Test_Project";
    private static String testPluginName = "TRMM3B42RT";
    private static String testGlobalSchema;
    private static Connection con;
    private static ArrayList<String> extraDownloadFiles;
    private static int year = LocalDate.now().minusDays(7).getYear();
    private static int day = LocalDate.now().minusDays(7).getDayOfYear();
    private static int daysPerInputFile = 1;
    private static int numOfIndices = 1;
    private static int filesPerDay = 1;
    private static LocalDate startDate;
    private static String temporalSummaryCompositionStrategyClass = "GregorianWeeklyStrategy";
    private static long expectedTotalResults = 0;
    private static ArrayList<ProjectInfoSummary> summaries;
    private static ProjectInfoPlugin pluginInfo;
    private static PluginMetaData pluginMetaData;
    private static ProjectInfoFile projectInfoFile;
    private static Scheduler scheduler;
    private static DatabaseCache outputCache;
    private static String downloaderClassName;

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
        ArrayList<String> indices  = new ArrayList<String>();
        indices.add("TRMM3B42RTCalculator");
        pluginInfo = new ProjectInfoPlugin(testPluginName, indices, "Level 1");
        pluginMetaData = PluginMetaDataCollection.CreatePluginMetaData(null, null, null, null, null, null, null, 1, null, extraDownloadFiles);
        ArrayList<ProjectInfoPlugin> plugins = new ArrayList<ProjectInfoPlugin>();
        plugins.add(pluginInfo);
        summaries = new ArrayList<ProjectInfoSummary>();
        Class<?>strategyClass = Class.forName("version2.prototype.summary.temporal.CompositionStrategies." + temporalSummaryCompositionStrategyClass);
        Constructor<?> ctorStrategy = strategyClass.getConstructor();
        summaries.add(new ProjectInfoSummary(new ZonalSummary("", "", ""),
                new TemporalSummaryRasterFileStore((TemporalSummaryCompositionStrategy)ctorStrategy.newInstance()),
                temporalSummaryCompositionStrategyClass,
                1));

        projectInfoFile = new ProjectInfoFile(plugins, startDate, testProjectName, "C:/Users/michael.devos/Desktop/EASTWeb", "", null, "", ZoneId.systemDefault().getId(), null,
                0, null, null, null, null, null, null, summaries);

        expectedTotalResults = summaries.get(0).GetTemporalFileStore().compStrategy.getNumberOfCompleteCompositesInRange(startDate, LocalDate.now().plusDays(1), daysPerInputFile) * numOfIndices;

        SchedulerData data = new SchedulerData(projectInfoFile, null);
        GenericLocalRetrievalLocalDownloaderTest tester = new GenericLocalRetrievalLocalDownloaderTest();
        PluginMetaDataCollection pluginMetaDataCollection = PluginMetaDataCollection.getInstance(new File("src/test/download/Test_" + testPluginName + ".xml"));
        scheduler = tester.new MyScheduler(data, 1, projectInfoFile, pluginMetaDataCollection, null, testConfig, TaskState.RUNNING);
        outputCache = new DatabaseCache(testGlobalSchema, testProjectName, testPluginName, ProcessName.DOWNLOAD, indices);
        downloaderClassName = "TRMM3B42RTDownloader";
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
                LocalDate.ofYearDay(year, day), daysPerInputFile, filesPerDay, numOfIndices, summaries, true);
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
     */
    @Test
    public final void testAttemptUpdate() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException, NoSuchMethodException, SecurityException {
        Statement stmt = con.createStatement();
        ResultSet rs;
        GenericLocalRetrievalLocalDownloader ldl = new GenericLocalRetrievalLocalDownloader(null, testConfig,
                new GenericLocalStorageGlobalDownloader(1, testConfig, testPluginName,
                        PluginMetaDataCollection.CreateDownloadMetaData("Data", null, null, null, null, filesPerDay, "", "", null, null),
                        null, startDate, downloaderClassName), projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);

        // Setup and precheck
        int projectID = Schemas.getProjectID(testGlobalSchema, testProjectName, stmt);
        int pluginID = Schemas.getPluginID(testGlobalSchema, testPluginName, stmt);
        int temporalStrategyId = Schemas.getTemporalSummaryCompositionStrategyID(testGlobalSchema, temporalSummaryCompositionStrategyClass, stmt);
        int expectedResultsId = Schemas.getExpectedResultsID(testGlobalSchema, projectID, pluginID, temporalStrategyId, stmt);
        String selectResultsQuery = "SELECT E.*, T.\"Name\" FROM \"" + testGlobalSchema + "\".\"ExpectedResults\" E " +
                "INNER JOIN \"" + testGlobalSchema + "\".\"TemporalSummaryCompositionStrategy\" T ON E.\"TemporalSummaryCompositionStrategyID\" = T.\"TemporalSummaryCompositionStrategyID\" " +
                "WHERE \"ExpectedResultsID\" = " + expectedResultsId + ";";
        rs = stmt.executeQuery(selectResultsQuery);
        if(rs != null && rs.next())
        {
            assertEquals("TemporalSummaryCompositionStrategyClass is incorrect.", temporalSummaryCompositionStrategyClass, rs.getString("Name"));
            assertEquals("ExpectedTotalResults incorrect.", expectedTotalResults, rs.getInt("ExpectedTotalResults"));
        } else {
            fail("Didn't find anything in " + testGlobalSchema + ".ExpectedResults table.");
        }

        ldl.SetStartDate(startDate.minusDays(7));
        long newExpectedTotalResults = summaries.get(0).GetTemporalFileStore().compStrategy.getNumberOfCompleteCompositesInRange(startDate.minusDays(7), LocalDate.now().plusDays(1), daysPerInputFile)
                * numOfIndices;
        assertNotEquals("newExpectedTotalResults is equal to the old value", newExpectedTotalResults, expectedTotalResults);

        // Run AttemptUpdate
        ldl.AttemptUpdate();

        // Test results
        rs = stmt.executeQuery(selectResultsQuery);
        if(rs != null && rs.next())
        {
            assertEquals("TemporalSummaryCompositionStrategyClass is incorrect.", temporalSummaryCompositionStrategyClass, rs.getString("Name"));
            assertEquals("ExpectedTotalResults incorrect.", newExpectedTotalResults, rs.getInt("ExpectedTotalResults"));
        } else {
            fail("Didn't fine anything in " + testGlobalSchema + ".ExpectedResults table.");
        }

        rs.close();
        stmt.close();
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
        GenericLocalRetrievalLocalDownloader ldl = new GenericLocalRetrievalLocalDownloader(null, testConfig,
                new GenericLocalStorageGlobalDownloader(1, testConfig, testPluginName,
                        PluginMetaDataCollection.CreateDownloadMetaData("Data", null, null, null, null, filesPerDay, "", "", null, null),
                        null, startDate, downloaderClassName), projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);

        assertEquals("LDL start date not as expected.", startDate, ldl.GetStartDate());
        ldl.SetStartDate(startDate.plusDays(1));
        assertEquals("LDL start date not as expected.", startDate, ldl.GetStartDate());
        ldl.SetStartDate(startDate.minusDays(1));
        assertEquals("LDL start date not as expected.", startDate.minusDays(1), ldl.GetStartDate());
    }

    private class MyScheduler extends Scheduler
    {

        public MyScheduler(SchedulerData data, int myID, ProjectInfoFile projectInfoFile, PluginMetaDataCollection pluginMetaDataCollection, EASTWebI manager, Config configInstance,
                TaskState initState) throws ParserConfigurationException, SAXException, IOException {
            super(data, projectInfoFile, pluginMetaDataCollection, myID, configInstance, manager, initState);
        }

        @Override
        protected void SetupProcesses(ProjectInfoPlugin pluginInfo) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException,
        IllegalArgumentException, InvocationTargetException, ParseException, IOException, ParserConfigurationException, SAXException {
            // Do nothing
        }
    }

}
