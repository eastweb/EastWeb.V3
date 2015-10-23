/**
 *
 */
package test.Scheduler;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.Config;
import version2.prototype.TaskState;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.Scheduler.SchedulerStatus;
import version2.prototype.Scheduler.SchedulerStatusContainer;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy;
import version2.prototype.util.DatabaseConnection;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.ProgressUpdater;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class SchedulerStatusContainerTest {
    private static SchedulerStatusContainer container;
    private static Config configInstance;
    private static DatabaseConnection con;
    private static Statement stmt;
    private static String globalSchema;
    private static String projectName = "SchedulerStatusContainerTest";
    private static String pluginName = "TRMM3B42RT";
    private static LocalDate startDate = LocalDate.now();
    private static ArrayList<ProjectInfoPlugin> plugins;
    private static ArrayList<ProjectInfoSummary> summaries;
    private static ProjectInfoFile projectMetaData;
    private static PluginMetaDataCollection pluginMetaDataCollection;
    private static double currentDataDownloadProgress;
    private static double currentQCDownloadProgress;
    private static double currentProcessorProgress;
    private static double currentIndicesProgress;
    private static double currentSummaryProgress;
    private static String temporalSummaryCompositionStrategyClassName = "GregorianWeeklyStrategy";
    //    private static double currentExpectedDownloadCount;
    //    private static double currentExpectedProcessorCount;
    //    private static double currentExpectedIndicesCount;
    //    private static double currentExpectedSummaryCount;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        configInstance = Config.getAnInstance("src\\test\\Scheduler\\config.xml");
        globalSchema = configInstance.getGlobalSchema();
        con = DatabaseConnector.getConnection();
        stmt = con.createStatement();

        plugins = new ArrayList<ProjectInfoPlugin>();
        ArrayList<String> indices = new ArrayList<String>();
        indices.add("TRMM3B42RTIndex");
        plugins.add(new ProjectInfoPlugin(pluginName, indices, null, null));
        summaries = new ArrayList<ProjectInfoSummary>();
        summaries.add(new ProjectInfoSummary(new ZonalSummary("a shape file path", "areaValueField", "areaNameField"), temporalSummaryCompositionStrategyClassName, 1));
        projectMetaData = new ProjectInfoFile(plugins, startDate, projectName, null, null, null, null, null, null, null, null, null, null, null, null, summaries);
        pluginMetaDataCollection = PluginMetaDataCollection.getInstance("src/test/Scheduler/" + pluginName + ".xml");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                globalSchema
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(projectName, pluginName)
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
                globalSchema
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(projectName, pluginName)
                );
        stmt.execute(query);
        ArrayList<String> tempCompNames = new ArrayList<String>(1);
        tempCompNames.add(temporalSummaryCompositionStrategyClassName);
        Schemas.CreateProjectPluginSchema(con, globalSchema, projectMetaData, pluginName, configInstance.getSummaryCalculations(), tempCompNames, 1, 1, false);
        SchedulerStatusContainerTest tester = new SchedulerStatusContainerTest();
        container = new SchedulerStatusContainer(configInstance, 1, tester.new MyProgressUpdater(configInstance, projectMetaData, pluginMetaDataCollection), projectMetaData, pluginMetaDataCollection,
                TaskState.STOPPED);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateDownloadProgressByData(java.lang.String, java.lang.String, version2.prototype.download.ListDatesFiles, java.util.ArrayList, java.sql.Statement)}.
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    public final void testUpdateDownloadProgressByData() throws SQLException, InterruptedException {
        // Initial setup
        SchedulerStatus status = container.GetStatus();
        LocalDateTime originalLastModifiedTime = status.LastModifiedTime;
        Thread.sleep(1000);
        currentDataDownloadProgress = 10.0;
        container.UpdateDownloadProgressByData("data", pluginName, null, null, null);
        currentQCDownloadProgress = 15.0;
        container.UpdateDownloadProgressByData("qc", pluginName, null, null, null);

        // Test
        status = container.GetStatus();
        assertEquals("Current Data Download progress incorrect.", currentDataDownloadProgress, status.GetDownloadProgressesByData().get(pluginName).get("data"), 0.0);
        assertEquals("Current QC Download progress incorrect.", currentQCDownloadProgress, status.GetDownloadProgressesByData().get(pluginName).get("qc"), 0.0);
        assertNotEquals("LastModifiedTime has not changed.", originalLastModifiedTime, status.LastModifiedTime);
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateProcessorProgress(java.lang.String, java.sql.Statement)}.
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    public final void testUpdateProcessorProgress() throws SQLException, InterruptedException {
        // Initial setup
        SchedulerStatus status = container.GetStatus();
        LocalDateTime originalLastModifiedTime = status.LastModifiedTime;
        Thread.sleep(1000);
        currentProcessorProgress = 20.0;
        container.UpdateProcessorProgress(pluginName, null);

        // Test
        status = container.GetStatus();
        assertEquals("Current Processor progress incorrect.", currentProcessorProgress, status.GetProcessorProgresses().get(pluginName), 0.0);
        assertNotEquals("LastModifiedTime has not changed.", originalLastModifiedTime, status.LastModifiedTime);
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateIndicesProgress(java.lang.String, java.sql.Statement)}.
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    public final void testUpdateIndicesProgress() throws SQLException, InterruptedException {
        // Initial setup
        SchedulerStatus status = container.GetStatus();
        LocalDateTime originalLastModifiedTime = status.LastModifiedTime;
        Thread.sleep(1000);
        currentIndicesProgress = 30.0;
        container.UpdateIndicesProgress(pluginName, null);

        // Test
        status = container.GetStatus();
        assertEquals("Current Indices progress incorrect.", currentIndicesProgress, status.GetIndicesProgresses().get(pluginName), 0.0);
        assertNotEquals("LastModifiedTime has not changed.", originalLastModifiedTime, status.LastModifiedTime);
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateSummaryProgress(int, TemporalSummaryCompositionStrategy, int, ProjectInfoPlugin, Statement)}.
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    public final void testUpdateSummaryProgress() throws SQLException, InterruptedException {
        // Initial setup
        SchedulerStatus status = container.GetStatus();
        LocalDateTime originalLastModifiedTime = status.LastModifiedTime;
        Thread.sleep(1000);
        currentSummaryProgress = 40.0;
        int summaryID = 1;
        container.UpdateSummaryProgress(summaryID, null, -1, plugins.get(0), null);

        // Test
        status = container.GetStatus();
        assertEquals("Current Summary progress incorrect.", currentSummaryProgress, status.GetSummaryProgresses().get(pluginName).get(summaryID), 0.0);
        assertNotEquals("LastModifiedTime has not changed.", originalLastModifiedTime, status.LastModifiedTime);
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateSchedulerTaskState(version2.prototype.TaskState)}.
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public final void testUpdateSchedulerTaskState() throws InterruptedException, SQLException {
        // Initial setup
        SchedulerStatus status = container.GetStatus();
        LocalDateTime originalLastModifiedTime = status.LastModifiedTime;
        Thread.sleep(1000);
        container.UpdateSchedulerTaskState(TaskState.RUNNING);

        // Test
        status = container.GetStatus();
        assertEquals("TaskState incorrect.", TaskState.RUNNING, status.State);
        assertNotEquals("LastModifiedTime has not changed.", originalLastModifiedTime, status.LastModifiedTime);
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#AddToLog(java.lang.String)}.
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public final void testAddToLog() throws InterruptedException, SQLException {
        // Initial setup
        SchedulerStatus status = container.GetStatus();
        LocalDateTime originalLastModifiedTime = status.LastModifiedTime;
        Thread.sleep(1000);
        String newLog = "New Message!";
        container.AddToLog(newLog);

        // Test
        status = container.GetStatus();
        assertTrue("Log not added to.", status.HasLogEntries());
        assertEquals("Log message incorrect.", newLog, status.ReadNextLogEntry());
        assertNull("Log reader didn't report end of log correctly.", status.ReadNextLogEntry());
        assertNotEquals("LastModifiedTime has not changed.", originalLastModifiedTime, status.LastModifiedTime);
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#CheckIfProjectIsUpToDate()}.
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public final void testCheckIfProjectIsUpToDate() throws InterruptedException, SQLException {
        // Initial setup and test
        SchedulerStatus status = container.GetStatus();
        LocalDateTime originalLastModifiedTime = status.LastModifiedTime;
        Thread.sleep(1000);
        assertFalse("Project is initially up to date.", status.ProjectUpToDate);

        currentDataDownloadProgress = 100.0;
        currentQCDownloadProgress = 100.0;
        currentProcessorProgress = 100.0;
        currentIndicesProgress = 100.0;
        currentSummaryProgress = 100.0;
        container.UpdateDownloadProgressByData("data", pluginName, null, null, null);
        container.UpdateDownloadProgressByData("qc", pluginName, null, null, null);
        container.UpdateProcessorProgress(pluginName, null);
        container.UpdateIndicesProgress(pluginName, null);
        int summaryID = 1;
        container.UpdateSummaryProgress(summaryID, null, -1, plugins.get(0), null);

        // Test
        status = container.GetStatus();
        assertTrue("Project isn't up to date.", status.ProjectUpToDate);
        assertNotEquals("LastModifiedTime has not changed.", originalLastModifiedTime, status.LastModifiedTime);
    }

    private class MyProgressUpdater extends ProgressUpdater
    {

        public MyProgressUpdater(Config configInstance, ProjectInfoFile projectMetaData, PluginMetaDataCollection pluginMetaDataCollection) {
            super(configInstance, projectMetaData, pluginMetaDataCollection);
        }

        @Override
        public double GetCurrentDownloadProgress(String dataName, String pluginName, Statement stmt) throws SQLException {
            if(dataName.toLowerCase().equals("data")) {
                return currentDataDownloadProgress;
            } else {
                return currentQCDownloadProgress;
            }
        }

        @Override
        public double GetCurrentProcessorProgress(String pluginName, Statement stmt) throws SQLException {
            return currentProcessorProgress;
        }

        @Override
        public double GetCurrentIndicesProgress(String pluginName, Statement stmt) throws SQLException {
            return currentIndicesProgress;
        }

        @Override
        public double GetCurrentSummaryProgress(int summaryIDNum, TemporalSummaryCompositionStrategy compStrategy, int daysPerInputData, ProjectInfoPlugin pluginInfo, Statement stmt) throws SQLException {
            return currentSummaryProgress;
        }

        @Override
        public void UpdateDBDownloadExpectedCount(String pluginName, String dataName, ListDatesFiles listDatesFiles, ArrayList<String> modisTileNames, Statement stmt) throws SQLException {
            // Do nothing
        }

        @Override
        public void UpdateDBProcessorExpectedCount(String pluginName, Statement stmt) throws SQLException {
            // Do Nothing
        }

        @Override
        public void UpdateDBIndicesExpectedCount(String pluginName, Statement stmt) throws SQLException {
            // Do nothing
        }

        @Override
        protected int getStoredDownloadExpectedTotalOutput(String projectName, String pluginName, String dataName, Statement stmt) throws SQLException {
            return 0;
        }

        @Override
        protected int getStoredProcessorExpectedTotalOutput(String projectName, String pluginName, Statement stmt) throws SQLException {
            return 0;
        }

        @Override
        protected int getStoredIndicesExpectedTotalOutput(String projectName, String pluginName, Statement stmt) throws SQLException {
            return 0;
        }

        @Override
        protected int getStoredSummaryExpectedTotalOutput(String projectName, String pluginName, int summaryIDNum, Statement stmt) throws SQLException {
            return 0;
        }
    }

}
