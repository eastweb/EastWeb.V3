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

import test.PluginMetaData.PluginMetaDataCollectionTester;
import version2.prototype.Config;
import version2.prototype.EASTWebManager;
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
    private static int year = 2015;
    private static int day = 1;
    private static int daysPerInputFile = -1;
    private static int numOfIndices = 3;
    private static int filesPerDay = 1;
    private static LocalDate startDate;
    private static String temporalSummaryCompositionStrategyClass = "GregorianWeeklyStrategy";
    private static int expectedTotalResults = 0;
    private static ArrayList<ProjectInfoSummary> summaries;
    private static GenericLocalRetrievalLocalDownloader ldl;

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
        ProjectInfoPlugin pluginInfo = new ProjectInfoPlugin(testPluginName, indices, "Level 1");
        PluginMetaData pluginMetaData = PluginMetaDataCollection.CreatePluginMetaData(null, null, null, null, null, null, null, 1, null, extraDownloadFiles);
        ArrayList<ProjectInfoPlugin> plugins = new ArrayList<ProjectInfoPlugin>();
        plugins.add(pluginInfo);
        summaries = new ArrayList<ProjectInfoSummary>();
        Class<?>strategyClass = Class.forName("version2.prototype.summary.temporal.CompositionStrategies." + temporalSummaryCompositionStrategyClass);
        Constructor<?> ctorStrategy = strategyClass.getConstructor();
        summaries.add(new ProjectInfoSummary(new ZonalSummary("", "", ""),
                new TemporalSummaryRasterFileStore((TemporalSummaryCompositionStrategy)ctorStrategy.newInstance()),
                temporalSummaryCompositionStrategyClass,
                1));
        ProjectInfoFile projectInfoFile = new ProjectInfoFile(plugins, startDate, testProjectName, "C:/Users/michael.devos/Desktop/EASTWeb", "", null, "", ZoneId.systemDefault().getId(), null,
                0, null, null, null, null, null, null, summaries);

        expectedTotalResults = summaries.get(0).GetTemporalFileStore().compStrategy.getNumberOfCompleteCompositesInRange(startDate, LocalDate.now().plusDays(1), daysPerInputFile) * numOfIndices;

        SchedulerData data = new SchedulerData(projectInfoFile, null);
        GenericLocalRetrievalLocalDownloaderTest tester = new GenericLocalRetrievalLocalDownloaderTest();
        PluginMetaDataCollection pluginMetaDataCollection = PluginMetaDataCollection.getInstance(new File("Test_" + testPluginName + ".xml"));
        Scheduler scheduler = tester.new MyScheduler(data, 1, projectInfoFile, pluginMetaDataCollection, null, testConfig, TaskState.STOPPED);
        DatabaseCache outputCache = new DatabaseCache(testGlobalSchema, testProjectName, testPluginName, ProcessName.DOWNLOAD, indices);
        ldl = new GenericLocalRetrievalLocalDownloader(null, testConfig, null, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
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
     * Test method for {@link version2.prototype.download.GenericLocalRetrievalLocalDownloader#process(java.util.ArrayList)}.
     */
    @Test
    public final void testProcess() {
        // Test updating ExpectedResults
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.download.LocalDownloader#AttemptUpdate()}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testAttemptUpdate() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        Statement stmt = con.createStatement();
        ResultSet rs;

        // Setup and precheck
        int projectID = Schemas.getProjectID(testGlobalSchema, testProjectName, stmt);
        int pluginID = Schemas.getPluginID(testGlobalSchema, testPluginName, stmt);
        int temporalStrategyId = Schemas.getTemporalSummaryCompositionStrategyID(testGlobalSchema, temporalSummaryCompositionStrategyClass, stmt);
        int expectedResultsId = Schemas.getExpectedResultsID(testGlobalSchema, projectID, pluginID, temporalStrategyId, stmt);
        String query = "SELECT * FROM \"" + testGlobalSchema + "\".\"ExpectedResults\" WHERE \"ExpectedResultsID\" = " + expectedResultsId + ";";
        rs = stmt.executeQuery(query);
        if(rs != null && rs.next())
        {
            assertEquals("TemporalSummaryCompositionStrategyClass is incorrect.", temporalSummaryCompositionStrategyClass, rs.getString("TemporalSummaryCompositionStrategyClass"));
            assertEquals("ExpectedTotalResults incorrect.", expectedTotalResults, rs.getInt("ExpectedTotalResults"));
        }

        // Run AttemptUpdate
        ldl.AttemptUpdate();

        // Test results

    }

    private class MyScheduler extends Scheduler
    {

        public MyScheduler(SchedulerData data, int myID, ProjectInfoFile projectInfoFile, PluginMetaDataCollection pluginMetaDataCollection, EASTWebManager manager, Config configInstance,
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
