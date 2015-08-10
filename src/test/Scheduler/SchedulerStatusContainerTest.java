/**
 *
 */
package test.Scheduler;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.EASTWebManager;
import version2.prototype.TaskState;
import version2.prototype.ZonalSummary;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.Scheduler.SchedulerStatus;
import version2.prototype.Scheduler.SchedulerStatusContainer;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class SchedulerStatusContainerTest {
    private static SchedulerStatusContainer container;
    private static Config configInstance;
    private static ArrayList<ProjectInfoSummary> summaries;
    private static String pluginName1 = "ModisNBAR";
    private static String pluginName2 = "TRMM3B42RT";
    private static int pluginID1;
    private static int pluginID2;
    private static int summaryID1 = 1;
    private static int summaryID2 = 2;
    private static String projectName = "SchedulerStatusContainerTest";
    private static String globalSchema;
    private static String projectSchema1 = Schemas.getSchemaName(projectName, pluginName1);
    private static String projectSchema2 = Schemas.getSchemaName(projectName, pluginName2);
    private static Connection con;
    private static Statement stmt;
    private static int year = 2015;
    private static int day = 100;
    private static int daysPerInputFile = 1;
    private static int numOfIndices = 3;
    private static int filesPerDay = 1;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        configInstance = Config.getAnInstance("src\\test\\Scheduler\\config.xml");
        globalSchema = configInstance.getGlobalSchema();
        int schedulerID = 1;
        ArrayList<ProjectInfoPlugin> pluginInfo = new ArrayList<ProjectInfoPlugin>();
        ArrayList<String> indices = new ArrayList<String>();
        indices.add("ModisNBARNDVI");
        indices.add("ModisNBAREVI");
        indices.add("ModisNBARNDWI5");
        pluginInfo.add(new ProjectInfoPlugin(pluginName1, indices, "Highest"));
        indices = new ArrayList<String>();
        indices.add("TRMM3B42RTIndex");
        pluginInfo.add(new ProjectInfoPlugin(pluginName2, indices, null));
        summaries = new ArrayList<ProjectInfoSummary>();
        ZonalSummary zonalSummary = new ZonalSummary("a shape file path", "areaValueField", "areaNameField");
        String temporalSummaryCompositionStrategyClassName = "GregorianWeeklyStrategy";
        TemporalSummaryCompositionStrategy compStrategy = new GregorianWeeklyStrategy();
        TemporalSummaryRasterFileStore fileStore = new TemporalSummaryRasterFileStore(compStrategy);
        summaries.add(new ProjectInfoSummary(zonalSummary, fileStore, temporalSummaryCompositionStrategyClassName, summaryID1));
        summaries.add(new ProjectInfoSummary(zonalSummary, fileStore, temporalSummaryCompositionStrategyClassName, summaryID2));
        TaskState state = TaskState.STOPPED;

        SchedulerStatusContainerTest temp = new SchedulerStatusContainerTest();
        container = new SchedulerStatusContainer(temp.new MyEASTWebManager(), configInstance, schedulerID, projectName, pluginInfo, summaries, state);

        con = PostgreSQLConnection.getConnection();
        stmt = con.createStatement();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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
                Schemas.getSchemaName(projectName, pluginName1)
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(projectName, pluginName2)
                );
        stmt.execute(query);

        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), globalSchema, projectName, pluginName1, configInstance.getSummaryCalculations(),
                LocalDate.ofYearDay(year, day), daysPerInputFile, filesPerDay, numOfIndices, summaries, false);
        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), globalSchema, projectName, pluginName2, configInstance.getSummaryCalculations(),
                LocalDate.ofYearDay(year, day), daysPerInputFile, filesPerDay, numOfIndices, summaries, false);

        pluginID1 = Schemas.getPluginID(globalSchema, pluginName1, stmt);
        pluginID2 = Schemas.getPluginID(globalSchema, pluginName2, stmt);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateDownloadProgress(double, java.lang.String)}.
     */
    @Test
    public final void testUpdateDownloadProgress() {
        SchedulerStatus status = container.GetStatus();
        TreeMap<String, Double> temp = status.GetDownloadProgress();
        assertEquals("Download progress for '" + pluginName1 + "' incorrect.", new Double(0), temp.get(pluginName1));
        assertEquals("Download progress for '" + pluginName2 + "' incorrect.", new Double(0), temp.get(pluginName2));

        container.UpdateDownloadProgress(25, pluginName1);
        container.UpdateDownloadProgress(50, pluginName2);
        assertEquals("Download progress for '" + pluginName1 + "' incorrect.", new Double(25), temp.get(pluginName1));
        assertEquals("Download progress for '" + pluginName2 + "' incorrect.", new Double(50), temp.get(pluginName2));
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateProcessorProgress(double, java.lang.String)}.
     */
    @Test
    public final void testUpdateProcessorProgress() {
        SchedulerStatus status = container.GetStatus();
        TreeMap<String, Double> temp = status.GetProcessorProgress();
        assertEquals("Processor progress for '" + pluginName1 + "' incorrect.", new Double(0), temp.get(pluginName1));
        assertEquals("Processor progress for '" + pluginName2 + "' incorrect.", new Double(0), temp.get(pluginName2));

        container.UpdateProcessorProgress(25, pluginName1);
        container.UpdateProcessorProgress(50, pluginName2);
        assertEquals("Processor progress for '" + pluginName1 + "' incorrect.", new Double(25), temp.get(pluginName1));
        assertEquals("Processor progress for '" + pluginName2 + "' incorrect.", new Double(50), temp.get(pluginName2));
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateIndicesProgress(double, java.lang.String)}.
     */
    @Test
    public final void testUpdateIndicesProgress() {
        SchedulerStatus status = container.GetStatus();
        TreeMap<String, Double> temp = status.GetIndicesProgress();
        assertEquals("Indices progress for '" + pluginName1 + "' incorrect.", new Double(0), temp.get(pluginName1));
        assertEquals("Indices progress for '" + pluginName2 + "' incorrect.", new Double(0), temp.get(pluginName2));

        container.UpdateIndicesProgress(25, pluginName1);
        container.UpdateIndicesProgress(50, pluginName2);
        assertEquals("Indices progress for '" + pluginName1 + "' incorrect.", new Double(25), temp.get(pluginName1));
        assertEquals("Indices progress for '" + pluginName2 + "' incorrect.", new Double(50), temp.get(pluginName2));
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateSummaryProgress(double, java.lang.String, int)}.
     */
    @Test
    public final void testUpdateSummaryProgress() {
        SchedulerStatus status = container.GetStatus();
        TreeMap<String, TreeMap<Integer, Double>> temp = status.GetSummaryProgress();
        TreeMap<Integer, Double> tempPlugin1 = temp.get(pluginName1);
        TreeMap<Integer, Double> tempPlugin2 = temp.get(pluginName2);
        assertEquals("Summary progress for '" + pluginName1 + "' summary ID = " + summaryID1 + " incorrect.", new Double(0), tempPlugin1.get(summaryID1));
        assertEquals("Summary progress for '" + pluginName1 + "' summary ID = " + summaryID2 + " incorrect.", new Double(0), tempPlugin1.get(summaryID2));
        assertEquals("Summary progress for '" + pluginName2 + "' summary ID = " + summaryID1 + " incorrect.", new Double(0), tempPlugin2.get(summaryID1));
        assertEquals("Summary progress for '" + pluginName2 + "' summary ID = " + summaryID2 + " incorrect.", new Double(0), tempPlugin2.get(summaryID2));

        container.UpdateSummaryProgress(25, pluginName1, summaryID1);
        container.UpdateSummaryProgress(50, pluginName1, summaryID2);
        container.UpdateSummaryProgress(75, pluginName2, summaryID1);
        container.UpdateSummaryProgress(100, pluginName2, summaryID2);
        status = container.GetStatus();
        temp = status.GetSummaryProgress();
        tempPlugin1 = temp.get(pluginName1);
        tempPlugin2 = temp.get(pluginName2);
        assertEquals("Summary progress for '" + pluginName1 + "' summary ID = " + summaryID1 + " incorrect.", new Double(25), tempPlugin1.get(summaryID1));
        assertEquals("Summary progress for '" + pluginName1 + "' summary ID = " + summaryID2 + " incorrect.", new Double(50), tempPlugin1.get(summaryID2));
        assertEquals("Summary progress for '" + pluginName2 + "' summary ID = " + summaryID1 + " incorrect.", new Double(75), tempPlugin2.get(summaryID1));
        assertEquals("Summary progress for '" + pluginName2 + "' summary ID = " + summaryID2 + " incorrect.", new Double(100), tempPlugin2.get(summaryID2));
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateNumOfFilesLoaded()}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testUpdateNumOfFilesLoaded() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        // Initial check
        container.UpdateNumOfFilesLoaded();
        SchedulerStatus status = container.GetStatus();
        TreeMap<String, Integer> temp = status.GetNumOfFilesDownloaded();
        assertEquals("Number of files loaded for '" + pluginName1 + "'.", new Integer(0), temp.get(pluginName1));
        assertEquals("Number of files loaded for '" + pluginName2 + "'.", new Integer(0), temp.get(pluginName2));

        // Insert values
        Connection con = PostgreSQLConnection.getConnection(Config.getAnInstance("config.xml"));
        String insertUpdate = "INSERT INTO \"%s\".\"DownloadCache\" (\"DataFilePath\", \"DownloadID\", \"DateGroupID\") VALUES (?,?,?);";
        PreparedStatement pStmt = con.prepareStatement(String.format(insertUpdate, projectSchema1));
        pStmt.setString(1, "Data File Path 1");
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.addBatch();
        pStmt.setString(1, "Data File Path 2");
        pStmt.setInt(2, 2);
        pStmt.setInt(3, 2);
        pStmt.addBatch();
        pStmt.executeBatch();

        pStmt = con.prepareStatement(String.format(insertUpdate, projectSchema2));
        pStmt.setString(1, "Data File Path 1");
        pStmt.setInt(2, 3);
        pStmt.setInt(3, 3);
        pStmt.execute();

        // Test new values
        container.UpdateNumOfFilesLoaded();
        status = container.GetStatus();
        temp = status.GetNumOfFilesDownloaded();
        assertEquals("Number of files loaded for '" + pluginName1 + "'.", new Integer(2), temp.get(pluginName1));
        assertEquals("Number of files loaded for '" + pluginName2 + "'.", new Integer(1), temp.get(pluginName2));
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateNumOfResultsPublished()}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testUpdateNumOfResultsPublished() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        int projectSummaryID1 = Schemas.getProjectSummaryID(globalSchema, projectName, summaryID1, stmt);
        int projectSummaryID2 = Schemas.getProjectSummaryID(globalSchema, projectName, summaryID2, stmt);

        // Initial check
        container.UpdateNumOfResultsPublished();
        SchedulerStatus status = container.GetStatus();
        TreeMap<String, TreeMap<Integer, Integer>> temp = status.GetNumOfResultsPublished();
        TreeMap<Integer, Integer> tempPlugin1 = temp.get(pluginName1);
        TreeMap<Integer, Integer> tempPlugin2 = temp.get(pluginName2);
        assertEquals("Number of results for '" + pluginName1 + "' summary ID = " + summaryID1 + " incorrect.", new Integer(0), tempPlugin1.get(summaryID1));
        assertEquals("Number of results for '" + pluginName1 + "' summary ID = " + summaryID2 + " incorrect.", new Integer(0), tempPlugin1.get(summaryID2));
        assertEquals("Number of results for '" + pluginName2 + "' summary ID = " + summaryID1 + " incorrect.", new Integer(0), tempPlugin2.get(summaryID1));
        assertEquals("Number of results for '" + pluginName2 + "' summary ID = " + summaryID2 + " incorrect.", new Integer(0), tempPlugin2.get(summaryID2));

        // Insert values
        String insertUpdate = "INSERT INTO \"%s\".\"ZonalStat\" (\"ProjectSummaryID\", \"DateGroupID\", \"IndexID\", \"ExpectedResultsID\", " +
                "\"AreaCode\", \"AreaName\", \"FilePath\", \"Count\", \"Max\", \"Mean\", \"Min\", \"SqrSum\", \"StdDev\", \"Sum\") VALUES " +
                "(?" +  // 1. ProjectSummaryID
                ",?" +  // 2. DateGroupID
                ",?" +  // 3. IndexID
                ",?" +  // 4. ExpectedResultsID
                ",?" +  // 5. AreaCode
                ",?" +  // 6. AreaName
                ",?" +  // 7. FilePath
                ",?" +  // 8. Count
                ",?" +  // 9. Max
                ",?" +  // 10. Mean
                ",?" +  // 11. Min
                ",?" +  // 12. SqrSum
                ",?" +  // 13. StdDev
                ",?" +  // 14. Sum
                ");";
        PreparedStatement pStmt = con.prepareStatement(String.format(insertUpdate, projectSchema1));
        // Add to projectSchema1, summaryID1
        pStmt.setInt(1, projectSummaryID1);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path1");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();

        // Add to projectSchema1, summaryID2
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path2");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path3");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();

        pStmt.executeBatch();

        pStmt = con.prepareStatement(String.format(insertUpdate, projectSchema2));
        // Add to projectSchema2, summaryID1
        pStmt.setInt(1, projectSummaryID1);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path4");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID1);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path5");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID1);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path6");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();

        // Add to projectSchema1, summaryID2
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path7");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path8");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path9");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path10");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();

        pStmt.executeBatch();

        // Test new values
        container.UpdateNumOfResultsPublished();
        status = container.GetStatus();
        temp = status.GetNumOfResultsPublished();
        tempPlugin1 = temp.get(pluginName1);
        tempPlugin2 = temp.get(pluginName2);
        assertEquals("Number of results for '" + pluginName1 + "' summary ID = " + summaryID1 + " incorrect.", new Integer(1), tempPlugin1.get(summaryID1));
        assertEquals("Number of results for '" + pluginName1 + "' summary ID = " + summaryID2 + " incorrect.", new Integer(2), tempPlugin1.get(summaryID2));
        assertEquals("Number of results for '" + pluginName2 + "' summary ID = " + summaryID1 + " incorrect.", new Integer(3), tempPlugin2.get(summaryID1));
        assertEquals("Number of results for '" + pluginName2 + "' summary ID = " + summaryID2 + " incorrect.", new Integer(4), tempPlugin2.get(summaryID2));
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#CheckIfResultsUpToDate(boolean)}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testCheckIfResultsUpToDateAndCheckIfProjectIsUpToDate() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        int projectSummaryID1 = Schemas.getProjectSummaryID(globalSchema, projectName, summaryID1, stmt);
        int projectSummaryID2 = Schemas.getProjectSummaryID(globalSchema, projectName, summaryID2, stmt);

        // Initial check
        container.CheckIfResultsUpToDate(false);
        container.CheckIfProjectIsUpToDate(false, false);
        SchedulerStatus status = container.GetStatus();
        TreeMap<String, TreeMap<Integer, Boolean>> temp = status.GetResultsUpToDate();
        boolean isEntirelyNotUpToDate = true;
        Iterator<String> pluginIt = temp.keySet().iterator();
        Iterator<Integer> summaryIt;
        TreeMap<Integer, Boolean> pluginMap;
        while(pluginIt.hasNext())
        {
            pluginMap = temp.get(pluginIt.next());
            summaryIt = pluginMap.keySet().iterator();
            while(summaryIt.hasNext())
            {
                if(pluginMap.get(summaryIt.next())) {
                    isEntirelyNotUpToDate = false;
                }
            }
        }
        assertTrue("Project is partially or entirely up to date.", isEntirelyNotUpToDate);
        assertFalse("Project is initially up to date.", status.ProjectUpToDate);


        // Insert values into ZonalStat
        String insertUpdate = "INSERT INTO \"%s\".\"ZonalStat\" (\"ProjectSummaryID\", \"DateGroupID\", \"IndexID\", \"ExpectedResultsID\", " +
                "\"AreaCode\", \"AreaName\", \"FilePath\", \"Count\", \"Max\", \"Mean\", \"Min\", \"SqrSum\", \"StdDev\", \"Sum\") VALUES " +
                "(?" +  // 1. ProjectSummaryID
                ",?" +  // 2. DateGroupID
                ",?" +  // 3. IndexID
                ",?" +  // 4. ExpectedResultsID
                ",?" +  // 5. AreaCode
                ",?" +  // 6. AreaName
                ",?" +  // 7. FilePath
                ",?" +  // 8. Count
                ",?" +  // 9. Max
                ",?" +  // 10. Mean
                ",?" +  // 11. Min
                ",?" +  // 12. SqrSum
                ",?" +  // 13. StdDev
                ",?" +  // 14. Sum
                ");";
        PreparedStatement pStmt = con.prepareStatement(String.format(insertUpdate, projectSchema1));
        // Add to projectSchema1, summaryID1
        pStmt.setInt(1, projectSummaryID1);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path1");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();

        // Add to projectSchema1, summaryID2
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path2");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path3");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();

        pStmt.executeBatch();

        pStmt = con.prepareStatement(String.format(insertUpdate, projectSchema2));
        // Add to projectSchema2, summaryID1
        pStmt.setInt(1, projectSummaryID1);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path4");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID1);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path5");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID1);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path6");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();

        // Add to projectSchema1, summaryID2
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path7");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path8");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path9");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.setInt(1, projectSummaryID2);
        pStmt.setInt(2, 1);
        pStmt.setInt(3, 1);
        pStmt.setInt(4, 1);
        pStmt.setInt(5, 11);
        pStmt.setString(6, "AreaName");
        pStmt.setString(7, "File Path10");
        pStmt.setDouble(8, 1);
        pStmt.setDouble(9, 2);
        pStmt.setDouble(10, 3);
        pStmt.setDouble(11, 4);
        pStmt.setDouble(12, 5);
        pStmt.setDouble(13, 6);
        pStmt.setDouble(14, 7);
        pStmt.addBatch();
        pStmt.executeBatch();


        pStmt = con.prepareStatement(String.format("UPDATE \"%1$s\".\"ExpectedResults\" SET \"ExpectedTotalResults\" = ? WHERE \"ExpectedResultsID\" = ?", globalSchema));
        pStmt.setInt(1, 1);
        pStmt.setInt(2, Schemas.getExpectedResultsID(globalSchema, projectSummaryID1, pluginID1, stmt));
        pStmt.addBatch();
        pStmt.setInt(1, 2);
        pStmt.setInt(2, Schemas.getExpectedResultsID(globalSchema, projectSummaryID2, pluginID1, stmt));
        pStmt.addBatch();
        pStmt.setInt(1, 3);
        pStmt.setInt(2, Schemas.getExpectedResultsID(globalSchema, projectSummaryID1, pluginID2, stmt));
        pStmt.addBatch();
        pStmt.setInt(1, 4);
        pStmt.setInt(2, Schemas.getExpectedResultsID(globalSchema, projectSummaryID2, pluginID2, stmt));
        pStmt.addBatch();
        pStmt.executeBatch();

        container.CheckIfResultsUpToDate(true);
        status = container.GetStatus();
        temp = status.GetResultsUpToDate();
        boolean isUpToDate = true;
        pluginIt = temp.keySet().iterator();
        while(isUpToDate && pluginIt.hasNext())
        {
            pluginMap = temp.get(pluginIt.next());
            summaryIt = pluginMap.keySet().iterator();
            while(isUpToDate && summaryIt.hasNext())
            {
                if(!pluginMap.get(summaryIt.next())) {
                    isUpToDate = false;
                }
            }
        }
        assertTrue("Project is not up to date.", isUpToDate);

        container.CheckIfProjectIsUpToDate(true, true);
        status = container.GetStatus();
        assertTrue("Project is not up to date.", status.ProjectUpToDate);
    }


    private class MyEASTWebManager extends EASTWebManager
    {
        public MyEASTWebManager(){

        }

        @Override
        public void NotifyUI(SchedulerStatus updatedStatus) {

        }

    }
}
