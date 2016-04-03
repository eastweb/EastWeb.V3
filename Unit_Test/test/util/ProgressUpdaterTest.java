/**
 *
 */
package test.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.CompositionStrategies.CDCWeeklyStrategy;
import version2.prototype.util.DatabaseConnection;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.ProgressUpdater;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class ProgressUpdaterTest {
    private static DatabaseConnection con;
    private static Statement stmt;
    private static Config configInstance;
    private static String projectName = "Test_Project";
    private static String pluginName = "Test_Plugin";
    private static String projectSchemaName = Schemas.getSchemaName(projectName, pluginName);
    private static int daysPerInputData;
    private static LocalDate day1;
    private static LocalDate day2;
    private static LocalDate day3;
    private static LocalDate day4;
    private static LocalDate day5;
    private static LocalDate day6;
    private static LocalDate day7;
    private static LocalDate day8;
    private static ProjectInfoPlugin pluginInfo;
    private static ArrayList<ProjectInfoPlugin> plugins;
    private static ArrayList<ProjectInfoSummary> summaries;
    private static ProjectInfoFile projectMetaData;
    private static PluginMetaDataCollection pluginMetaDataCollection;
    private static PluginMetaData pluginData;
    private static ProgressUpdater progressUpdater;
    private static TemporalSummaryCompositionStrategy compStrategy = new CDCWeeklyStrategy();

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        con = DatabaseConnector.getConnection();
        stmt = con.createStatement();
        configInstance = Config.getAnInstance("src/test/config.xml");
        day1 = compStrategy.getStartDate(LocalDate.now().minusDays(7));
        day2 = day1.minusDays(8);
        day3 = day1.minusDays(9);
        day4 = day1.minusDays(10);
        day5 = day1.minusDays(11);
        day6 = day1.minusDays(12);
        day7 = day1.minusDays(13);
        day8 = day1.minusDays(14);
        ArrayList<String> indices = new ArrayList<String>(2);
        indices.add("Index1");
        indices.add("Index2");
        pluginInfo = new ProjectInfoPlugin(pluginName, indices, null, null);
        plugins = new ArrayList<ProjectInfoPlugin>();
        plugins.add(pluginInfo);
        summaries = new ArrayList<ProjectInfoSummary>();
        summaries.add(new ProjectInfoSummary(new ZonalSummary("shape file", "area code field", "area name field"),
                null,
                1));
        projectMetaData = new ProjectInfoFile(plugins, day1, projectName, "C:/Users/michael.devos/Desktop/EASTWeb", "", null, "", ZoneId.systemDefault().getId(), null, null, null, null, null, null, summaries);
        pluginMetaDataCollection = PluginMetaDataCollection.getInstance("src/test/util/" + pluginName + ".xml");
        pluginData = pluginMetaDataCollection.pluginMetaDataMap.get(pluginName);
        daysPerInputData = pluginData.DaysPerInputData;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                configInstance.getGlobalSchema()
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
                configInstance.getGlobalSchema()
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(projectName, pluginName)
                );
        stmt.execute(query);
        Schemas.CreateProjectPluginSchema(con, configInstance.getGlobalSchema(), projectMetaData, pluginName, configInstance.getSummaryCalculations(), null, daysPerInputData,
                pluginData.Download.filesPerDay, false);
        progressUpdater = new ProgressUpdater(configInstance, projectMetaData, pluginMetaDataCollection);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                configInstance.getGlobalSchema()
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(projectName, pluginName)
                );
        stmt.execute(query);
    }

    /**
     * Test method for {@link version2.prototype.util.ProgressUpdater#GetCurrentDownloadProgress(java.lang.String, java.lang.String, java.sql.Statement)}.
     * @throws SQLException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws PatternSyntaxException
     */
    @Test
    public final void testGetCurrentDownloadProgress() throws SQLException, PatternSyntaxException, IOException, ParserConfigurationException, SAXException {
        String data1 = "data";
        String data2 = "qc";
        ListDatesFiles listDatesFiles = new MyListDatesFiles();
        ArrayList<String> modisTileNames = new ArrayList<String>();

        progressUpdater.UpdateDBDownloadExpectedCount(pluginName, data1, listDatesFiles, modisTileNames, stmt);
        progressUpdater.UpdateDBDownloadExpectedCount(pluginName, data2, listDatesFiles, modisTileNames, stmt);

        assertEquals("Initial data download progress incorrect.", 0.0, progressUpdater.GetCurrentDownloadProgress(data1, pluginName,
                LocalDate.ofYearDay(LocalDate.now().getYear()-1, 1), modisTileNames, stmt), 0.0);
        assertEquals("Initial qc download progress incorrect.", 0.0, progressUpdater.GetCurrentDownloadProgress(data2, pluginName,
                LocalDate.ofYearDay(LocalDate.now().getYear()-1, 1), modisTileNames, stmt), 0.0);

        String insertDataUpdate = "INSERT INTO \"%s\".\"DownloadCache\" (\"DataFilePath\", \"DownloadID\", \"DateGroupID\") VALUES (?,?,?);";
        PreparedStatement dataPStmt = con.prepareStatement(String.format(insertDataUpdate, projectSchemaName));
        dataPStmt.setString(1, "Path 1");
        dataPStmt.setInt(2, 1);
        dataPStmt.setInt(3, 1);
        dataPStmt.execute();

        String insertQCUpdate = "INSERT INTO \"%s\".\"DownloadCacheExtra\" (\"DataName\", \"FilePath\", \"DownloadExtraID\", \"DateGroupID\") VALUES (?,?,?,?);";
        PreparedStatement qcPStmt = con.prepareStatement(String.format(insertQCUpdate, projectSchemaName));
        qcPStmt.setString(1, data2);
        qcPStmt.setString(2, "Path 1");
        qcPStmt.setInt(3, 1);
        qcPStmt.setInt(4, 1);
        qcPStmt.execute();

        assertEquals("Partial data download progress incorrect.", 50.0, progressUpdater.GetCurrentDownloadProgress(data1, pluginName,
                LocalDate.ofYearDay(LocalDate.now().getYear()-1, 1), modisTileNames, stmt), 0.0);
        assertEquals("Partial qc download progress incorrect.", 50.0, progressUpdater.GetCurrentDownloadProgress(data2, pluginName,
                LocalDate.ofYearDay(LocalDate.now().getYear()-1, 1), modisTileNames, stmt), 0.0);
        modisTileNames.add("ModisTile1");
        progressUpdater.UpdateDBDownloadExpectedCount(pluginName, data1, listDatesFiles, modisTileNames, stmt);
        assertEquals("Completed Download progress with modis tile incorrect.", 100.0, progressUpdater.GetCurrentDownloadProgress(data1, pluginName,
                LocalDate.ofYearDay(LocalDate.now().getYear()-1, 1), modisTileNames, stmt), 0.0);
        modisTileNames.clear();
        progressUpdater.UpdateDBDownloadExpectedCount(pluginName, data1, listDatesFiles, modisTileNames, stmt);

        dataPStmt.setString(1, "Path 2");
        dataPStmt.setInt(2, 2);
        dataPStmt.setInt(3, 2);
        dataPStmt.execute();
        qcPStmt.setString(1, data2);
        qcPStmt.setString(2, "Path 2");
        qcPStmt.setInt(3, 2);
        qcPStmt.setInt(4, 2);
        qcPStmt.execute();

        assertEquals("Completed data download progress incorrect.", 100.0, progressUpdater.GetCurrentDownloadProgress(data1, pluginName,
                LocalDate.ofYearDay(LocalDate.now().getYear()-1, 1), modisTileNames, stmt), 0.0);
        assertEquals("Completed qc download progress incorrect.", 100.0, progressUpdater.GetCurrentDownloadProgress(data2, pluginName,
                LocalDate.ofYearDay(LocalDate.now().getYear()-1, 1), modisTileNames, stmt), 0.0);

        // Test persistence
        progressUpdater = new ProgressUpdater(configInstance, projectMetaData, pluginMetaDataCollection);
        assertEquals("Completed data download progress incorrect.", 100.0, progressUpdater.GetCurrentDownloadProgress(data1, pluginName,
                LocalDate.ofYearDay(LocalDate.now().getYear()-1, 1), modisTileNames, stmt), 0.0);
        assertEquals("Completed qc download progress incorrect.", 100.0, progressUpdater.GetCurrentDownloadProgress(data2, pluginName,
                LocalDate.ofYearDay(LocalDate.now().getYear()-1, 1), modisTileNames, stmt), 0.0);
    }

    /**
     * Test method for {@link version2.prototype.util.ProgressUpdater#GetCurrentProcessorProgress(java.lang.String, java.sql.Statement)}.
     * @throws SQLException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws PatternSyntaxException
     */
    @Test
    public final void testGetCurrentProcessorProgress() throws SQLException, PatternSyntaxException, IOException, ParserConfigurationException, SAXException {
        ListDatesFiles listDatesFiles = new MyListDatesFiles();
        ArrayList<String> modisTileNames = new ArrayList<String>();

        // Setup for expectedCount calculation
        String insertDataUpdate = "INSERT INTO \"%s\".\"DownloadCache\" (\"DataFilePath\", \"DownloadID\", \"DateGroupID\") VALUES (?,?,?);";
        PreparedStatement dataPStmt = con.prepareStatement(String.format(insertDataUpdate, projectSchemaName));
        dataPStmt.setString(1, "Path 1");
        dataPStmt.setInt(2, 1);
        dataPStmt.setInt(3, 1);
        dataPStmt.addBatch();
        dataPStmt.setString(1, "Path 2");
        dataPStmt.setInt(2, 2);
        dataPStmt.setInt(3, 2);
        dataPStmt.executeBatch();

        progressUpdater.UpdateDBDownloadExpectedCount(pluginName, "data", listDatesFiles, modisTileNames, stmt);
        progressUpdater.UpdateDBProcessorExpectedCount(pluginName, stmt);

        // Start testing
        assertEquals("Initial Processor progress incorrect.", 0.0, progressUpdater.GetCurrentProcessorProgress(pluginName, stmt), 0.0);

        String insertUpdate = "INSERT INTO \"%s\".\"ProcessorCache\" (\"DataFilePath\", \"DateGroupID\") VALUES (?,?);";
        PreparedStatement insertPStmt = con.prepareStatement(String.format(insertUpdate, projectSchemaName));
        insertPStmt.setString(1, "Path 1");
        insertPStmt.setInt(2, 1);
        insertPStmt.execute();

        assertEquals("Partial Processor progress incorrect.", 25.0, progressUpdater.GetCurrentProcessorProgress(pluginName, stmt), 0.0);

        insertPStmt.setString(1, "Path 2");
        insertPStmt.setInt(2, 2);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 3");
        insertPStmt.setInt(2, 3);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 4");
        insertPStmt.setInt(2, 4);
        insertPStmt.addBatch();
        insertPStmt.executeBatch();

        assertEquals("Completed Processor progress incorrect.", 100.0, progressUpdater.GetCurrentProcessorProgress(pluginName, stmt), 0.0);

        // Test persistence
        progressUpdater = new ProgressUpdater(configInstance, projectMetaData, pluginMetaDataCollection);
        assertEquals("Completed Processor progress incorrect.", 100.0, progressUpdater.GetCurrentProcessorProgress(pluginName, stmt), 0.0);
    }

    /**
     * Test method for {@link version2.prototype.util.ProgressUpdater#GetCurrentIndicesProgress(java.lang.String, java.sql.Statement)}.
     * @throws SQLException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws PatternSyntaxException
     */
    @Test
    public final void testGetCurrentIndicesProgress() throws SQLException, PatternSyntaxException, IOException, ParserConfigurationException, SAXException {
        ListDatesFiles listDatesFiles = new MyListDatesFiles();
        ArrayList<String> modisTileNames = new ArrayList<String>();

        // Setup for expectedCount calculation
        String insertDataUpdate = "INSERT INTO \"%s\".\"DownloadCache\" (\"DataFilePath\", \"DownloadID\", \"DateGroupID\") VALUES (?,?,?);";
        PreparedStatement dataPStmt = con.prepareStatement(String.format(insertDataUpdate, projectSchemaName));
        dataPStmt.setString(1, "Path 1");
        dataPStmt.setInt(2, 1);
        dataPStmt.setInt(3, 1);
        dataPStmt.addBatch();
        dataPStmt.setString(1, "Path 2");
        dataPStmt.setInt(2, 2);
        dataPStmt.setInt(3, 2);
        dataPStmt.executeBatch();
        String insertUpdate = "INSERT INTO \"%s\".\"ProcessorCache\" (\"DataFilePath\", \"DateGroupID\") VALUES (?,?);";
        PreparedStatement insertPStmt = con.prepareStatement(String.format(insertUpdate, projectSchemaName));
        insertPStmt.setString(1, "Path 1");
        insertPStmt.setInt(2, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 2");
        insertPStmt.setInt(2, 2);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 3");
        insertPStmt.setInt(2, 3);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 4");
        insertPStmt.setInt(2, 4);
        insertPStmt.addBatch();
        insertPStmt.executeBatch();
        progressUpdater.GetCurrentProcessorProgress(pluginName, stmt);

        progressUpdater.UpdateDBDownloadExpectedCount(pluginName, "data", listDatesFiles, modisTileNames, stmt);
        progressUpdater.UpdateDBProcessorExpectedCount(pluginName, stmt);
        progressUpdater.UpdateDBIndicesExpectedCount(pluginName, stmt);

        // Start testing
        assertEquals("Initial Indices progress incorrect.", 0.0, progressUpdater.GetCurrentIndicesProgress(pluginName, stmt), 0.0);

        insertUpdate = "INSERT INTO \"%s\".\"IndicesCache\" (\"DataFilePath\", \"DateGroupID\", \"IndexID\") VALUES (?,?,?);";
        insertPStmt = con.prepareStatement(String.format(insertUpdate, projectSchemaName));
        insertPStmt.setString(1, "Path 1");
        insertPStmt.setInt(2, 1);
        insertPStmt.setInt(3, 1);
        insertPStmt.execute();

        assertEquals("Partial Indices progress incorrect.", 12.5, progressUpdater.GetCurrentIndicesProgress(pluginName, stmt), 0.0);

        insertPStmt.setString(1, "Path 2");
        insertPStmt.setInt(2, 2);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 3");
        insertPStmt.setInt(2, 3);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 4");
        insertPStmt.setInt(2, 4);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 5");
        insertPStmt.setInt(2, 5);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 6");
        insertPStmt.setInt(2, 6);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 7");
        insertPStmt.setInt(2, 7);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 8");
        insertPStmt.setInt(2, 8);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.executeBatch();

        assertEquals("Completed Indices progress incorrect.", 100.0, progressUpdater.GetCurrentIndicesProgress(pluginName, stmt), 0.0);

        // Test persistence
        progressUpdater = new ProgressUpdater(configInstance, projectMetaData, pluginMetaDataCollection);
        assertEquals("Completed Indices progress incorrect.", 100.0, progressUpdater.GetCurrentIndicesProgress(pluginName, stmt), 0.0);
    }

    /**
     * Test method for {@link version2.prototype.util.ProgressUpdater#GetCurrentSummaryProgress(int, ProjectInfoPlugin, Statement)}.
     * @throws SQLException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws PatternSyntaxException
     */
    @Test
    public final void testGetCurrentSummaryProgress() throws SQLException, PatternSyntaxException, IOException, ParserConfigurationException, SAXException {
        ListDatesFiles listDatesFiles = new MyListDatesFiles();
        ArrayList<String> modisTileNames = new ArrayList<String>();
        int summaryIDNum = 1;
        int projectSummaryID = 1;

        Schemas.getDateGroupID(configInstance.getGlobalSchema(), day1, stmt);
        Schemas.getDateGroupID(configInstance.getGlobalSchema(), day2, stmt);
        Schemas.getDateGroupID(configInstance.getGlobalSchema(), day3, stmt);
        Schemas.getDateGroupID(configInstance.getGlobalSchema(), day4, stmt);
        Schemas.getDateGroupID(configInstance.getGlobalSchema(), day5, stmt);
        Schemas.getDateGroupID(configInstance.getGlobalSchema(), day6, stmt);
        Schemas.getDateGroupID(configInstance.getGlobalSchema(), day7, stmt);
        Schemas.getDateGroupID(configInstance.getGlobalSchema(), day8, stmt);

        // Setup for expectedCount calculation
        String insertDataUpdate = "INSERT INTO \"%s\".\"DownloadCache\" (\"DataFilePath\", \"DownloadID\", \"DateGroupID\") VALUES (?,?,?);";
        PreparedStatement dataPStmt = con.prepareStatement(String.format(insertDataUpdate, projectSchemaName));
        dataPStmt.setString(1, "Path 1");
        dataPStmt.setInt(2, 1);
        dataPStmt.setInt(3, 1);
        dataPStmt.addBatch();
        dataPStmt.setString(1, "Path 2");
        dataPStmt.setInt(2, 2);
        dataPStmt.setInt(3, 2);
        dataPStmt.executeBatch();
        String insertUpdate = "INSERT INTO \"%s\".\"ProcessorCache\" (\"DataFilePath\", \"DateGroupID\") VALUES (?,?);";
        PreparedStatement insertPStmt = con.prepareStatement(String.format(insertUpdate, projectSchemaName));
        insertPStmt.setString(1, "Path 1");
        insertPStmt.setInt(2, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 2");
        insertPStmt.setInt(2, 2);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 3");
        insertPStmt.setInt(2, 3);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 4");
        insertPStmt.setInt(2, 4);
        insertPStmt.addBatch();
        insertPStmt.executeBatch();
        insertUpdate = "INSERT INTO \"%s\".\"IndicesCache\" (\"DataFilePath\", \"DateGroupID\", \"IndexID\") VALUES (?,?,?);";
        insertPStmt = con.prepareStatement(String.format(insertUpdate, projectSchemaName));
        insertPStmt.setString(1, "Path 1");
        insertPStmt.setInt(2, 1);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 2");
        insertPStmt.setInt(2, 2);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 3");
        insertPStmt.setInt(2, 3);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 4");
        insertPStmt.setInt(2, 4);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 5");
        insertPStmt.setInt(2, 5);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 6");
        insertPStmt.setInt(2, 6);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 7");
        insertPStmt.setInt(2, 7);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.setString(1, "Path 8");
        insertPStmt.setInt(2, 8);
        insertPStmt.setInt(3, 1);
        insertPStmt.addBatch();
        insertPStmt.executeBatch();

        progressUpdater.UpdateDBDownloadExpectedCount(pluginName, "data", listDatesFiles, modisTileNames, stmt);
        progressUpdater.UpdateDBProcessorExpectedCount(pluginName, stmt);
        progressUpdater.UpdateDBIndicesExpectedCount(pluginName, stmt);
        progressUpdater.UpdateDBSummaryExpectedCount(summaryIDNum, compStrategy, daysPerInputData, pluginInfo, stmt);

        // Start testing temporal summary progress calculations
        assertEquals("Initial Summary, with temporal summary, progress incorrect.", 0.0, progressUpdater.GetCurrentSummaryProgress(summaryIDNum, compStrategy, daysPerInputData, pluginInfo, stmt), 0.0);

        // Insert values
        insertUpdate = "INSERT INTO \"%s\".\"ZonalStat\" (\"ProjectSummaryID\", \"DateGroupID\", \"IndexID\", \"AreaCode\", \"AreaName\", \"FilePath\", " +
                "\"Count\", \"Max\", \"Mean\", \"Min\", \"SqrSum\", \"StdDev\", \"Sum\") VALUES " +
                "(?" +  // 1. ProjectSummaryID
                ",?" +  // 2. DateGroupID
                ",?" +  // 3. IndexID
                ",?" +  // 4. AreaCode
                ",?" +  // 5. AreaName
                ",?" +  // 6. FilePath
                ",?" +  // 7. Count
                ",?" +  // 8. Max
                ",?" +  // 9. Mean
                ",?" +  // 10. Min
                ",?" +  // 11. SqrSum
                ",?" +  // 12. StdDev
                ",?" +  // 13. Sum
                ");";
        insertPStmt = con.prepareStatement(String.format(insertUpdate, projectSchemaName));
        insertPStmt.setInt(1, projectSummaryID);
        insertPStmt.setInt(2, 1);
        insertPStmt.setInt(3, 1);
        insertPStmt.setInt(4, 1);
        insertPStmt.setString(5, "AreaName");
        insertPStmt.setString(6, "File Path1");
        insertPStmt.setDouble(7, 1);
        insertPStmt.setDouble(8, 2);
        insertPStmt.setDouble(9, 3);
        insertPStmt.setDouble(10, 4);
        insertPStmt.setDouble(11, 5);
        insertPStmt.setDouble(12, 6);
        insertPStmt.setDouble(13, 7);
        insertPStmt.addBatch();
        insertPStmt.setInt(1, projectSummaryID);
        insertPStmt.setInt(2, 2);
        insertPStmt.setInt(3, 1);
        insertPStmt.setInt(4, 1);
        insertPStmt.setString(5, "AreaName");
        insertPStmt.setString(6, "File Path2");
        insertPStmt.setDouble(7, 1);
        insertPStmt.setDouble(8, 2);
        insertPStmt.setDouble(9, 3);
        insertPStmt.setDouble(10, 4);
        insertPStmt.setDouble(11, 5);
        insertPStmt.setDouble(12, 6);
        insertPStmt.setDouble(13, 7);
        insertPStmt.addBatch();
        insertPStmt.executeBatch();

        // Project has two indices. So expecting 2 composites * 2 indices = 4 expected total results
        assertEquals("Partial Summary, with temporal summary, progress incorrect.", 50.0, progressUpdater.GetCurrentSummaryProgress(summaryIDNum, compStrategy, daysPerInputData, pluginInfo, stmt), 0.0);

        insertPStmt.setInt(1, projectSummaryID);
        insertPStmt.setInt(2, 3);
        insertPStmt.setInt(3, 1);
        insertPStmt.setInt(4, 1);
        insertPStmt.setString(5, "AreaName");
        insertPStmt.setString(6, "File Path3");
        insertPStmt.setDouble(7, 1);
        insertPStmt.setDouble(8, 2);
        insertPStmt.setDouble(9, 3);
        insertPStmt.setDouble(10, 4);
        insertPStmt.setDouble(11, 5);
        insertPStmt.setDouble(12, 6);
        insertPStmt.setDouble(13, 7);
        insertPStmt.addBatch();
        insertPStmt.setInt(1, projectSummaryID);
        insertPStmt.setInt(2, 4);
        insertPStmt.setInt(3, 1);
        insertPStmt.setInt(4, 1);
        insertPStmt.setString(5, "AreaName");
        insertPStmt.setString(6, "File Path4");
        insertPStmt.setDouble(7, 1);
        insertPStmt.setDouble(8, 2);
        insertPStmt.setDouble(9, 3);
        insertPStmt.setDouble(10, 4);
        insertPStmt.setDouble(11, 5);
        insertPStmt.setDouble(12, 6);
        insertPStmt.setDouble(13, 7);
        insertPStmt.addBatch();
        insertPStmt.executeBatch();

        assertEquals("Completed Summary, with temporal summary, progress incorrect.", 100.0, progressUpdater.GetCurrentSummaryProgress(summaryIDNum, compStrategy, daysPerInputData, pluginInfo, stmt), 0.0);

        // Start testing progress calculations without temporal summary (expected total results = number of indices results
        progressUpdater.UpdateDBSummaryExpectedCount(summaryIDNum, null, daysPerInputData, pluginInfo, stmt);
        assertEquals("Partial Summary, without temporal summary, progress incorrect.", 50.0, progressUpdater.GetCurrentSummaryProgress(summaryIDNum, null, daysPerInputData, pluginInfo, stmt), 0.0);

        insertPStmt.setInt(1, projectSummaryID);
        insertPStmt.setInt(2, 5);
        insertPStmt.setInt(3, 1);
        insertPStmt.setInt(4, 1);
        insertPStmt.setString(5, "AreaName");
        insertPStmt.setString(6, "File Path5");
        insertPStmt.setDouble(7, 1);
        insertPStmt.setDouble(8, 2);
        insertPStmt.setDouble(9, 3);
        insertPStmt.setDouble(10, 4);
        insertPStmt.setDouble(11, 5);
        insertPStmt.setDouble(12, 6);
        insertPStmt.setDouble(13, 7);
        insertPStmt.addBatch();
        insertPStmt.setInt(1, projectSummaryID);
        insertPStmt.setInt(2, 6);
        insertPStmt.setInt(3, 1);
        insertPStmt.setInt(4, 1);
        insertPStmt.setString(5, "AreaName");
        insertPStmt.setString(6, "File Path6");
        insertPStmt.setDouble(7, 1);
        insertPStmt.setDouble(8, 2);
        insertPStmt.setDouble(9, 3);
        insertPStmt.setDouble(10, 4);
        insertPStmt.setDouble(11, 5);
        insertPStmt.setDouble(12, 6);
        insertPStmt.setDouble(13, 7);
        insertPStmt.addBatch();
        insertPStmt.setInt(1, projectSummaryID);
        insertPStmt.setInt(2, 7);
        insertPStmt.setInt(3, 1);
        insertPStmt.setInt(4, 1);
        insertPStmt.setString(5, "AreaName");
        insertPStmt.setString(6, "File Path7");
        insertPStmt.setDouble(7, 1);
        insertPStmt.setDouble(8, 2);
        insertPStmt.setDouble(9, 3);
        insertPStmt.setDouble(10, 4);
        insertPStmt.setDouble(11, 5);
        insertPStmt.setDouble(12, 6);
        insertPStmt.setDouble(13, 7);
        insertPStmt.addBatch();
        insertPStmt.setInt(1, projectSummaryID);
        insertPStmt.setInt(2, 8);
        insertPStmt.setInt(3, 1);
        insertPStmt.setInt(4, 1);
        insertPStmt.setString(5, "AreaName");
        insertPStmt.setString(6, "File Path8");
        insertPStmt.setDouble(7, 1);
        insertPStmt.setDouble(8, 2);
        insertPStmt.setDouble(9, 3);
        insertPStmt.setDouble(10, 4);
        insertPStmt.setDouble(11, 5);
        insertPStmt.setDouble(12, 6);
        insertPStmt.setDouble(13, 7);
        insertPStmt.addBatch();
        insertPStmt.executeBatch();

        assertEquals("Completed Summary, without temporal summary, progress incorrect.", 100.0, progressUpdater.GetCurrentSummaryProgress(summaryIDNum, null, daysPerInputData, pluginInfo, stmt), 0.0);

        // Test persistence
        progressUpdater = new ProgressUpdater(configInstance, projectMetaData, pluginMetaDataCollection);
        assertEquals("Completed Summary, without temporal summary, progress incorrect.", 100.0, progressUpdater.GetCurrentSummaryProgress(summaryIDNum, null, daysPerInputData, pluginInfo, stmt), 0.0);
    }

    private class MyListDatesFiles extends ListDatesFiles
    {
        public MyListDatesFiles() throws IOException, PatternSyntaxException, ParserConfigurationException, SAXException {
            super(null, new DownloadMetaData(null, null, null, null, null, "FTP", null, null, null, null, 1, null, null, null), null);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Map<DataDate, ArrayList<String>> ListDatesFilesFTP() {
            Map<DataDate, ArrayList<String>> mapDatesFiles = new HashMap<DataDate, ArrayList<String>>();
            ArrayList<String> dataFilePaths = new ArrayList<String>();
            dataFilePaths.add("Path 1");
            mapDatesFiles.put(new DataDate(day1), (ArrayList<String>) dataFilePaths.clone());
            dataFilePaths = new ArrayList<String>();
            dataFilePaths.add("ModisTile1");
            mapDatesFiles.put(new DataDate(day2), (ArrayList<String>) dataFilePaths.clone());
            return mapDatesFiles;
        }

        @Override
        protected Map<DataDate, ArrayList<String>> ListDatesFilesHTTP() {
            return null;
        }

    }
}
