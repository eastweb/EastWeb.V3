package test.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ZonalSummary;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.Schemas;

@SuppressWarnings("javadoc")
public class SchemasTest {
    private static Connection con;
    private static String testProjectName;
    private static String testPluginName;
    private static String testGlobalSchema;
    private static String testSchemaName;
    private static String shapeFile;
    private static ArrayList<String> summaryNames;
    private static String areaValueField;
    private static String areaNameField;
    private static TemporalSummaryCompositionStrategy compStrategy;
    private static ArrayList<ProjectInfoSummary> summaries;
    private static ArrayList<String> extraDownloadFiles;
    private static LocalDate startDate;
    private static int daysPerInputFile;
    //private static int numOfIndices;
    private static int filesPerDay;
    private static ProjectInfoFile projectMetaData;

    @BeforeClass
    public static void setUpBeforeClass() throws SQLException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException {
        con = DatabaseConnector.getConnection();
        testProjectName = "Test_Project";
        testPluginName = "Test_Plugin";
        testGlobalSchema = "Test_EASTWeb";
        testSchemaName = "test_project_test_plugin";
        shapeFile = "C:\\Users\\sufi\\Desktop\\shapefile\\shapefile.shp";
        areaValueField = "COUNTYNS10";
        areaNameField = "NAME10";
        compStrategy = new GregorianWeeklyStrategy();
        startDate = LocalDate.now().minusDays(8);
        daysPerInputFile = 8;
        //numOfIndices = 3;
        filesPerDay = 1;

        summaryNames = new ArrayList<String>();
        summaryNames.add("Count");
        summaryNames.add("Sum");
        summaryNames.add("Mean");
        summaryNames.add("StdDev");

        summaries = new ArrayList<ProjectInfoSummary>(0);
        summaries.add(new ProjectInfoSummary(new ZonalSummary(shapeFile, areaValueField, areaNameField), compStrategy.getClass().getCanonicalName(), 1));

        extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");

        projectMetaData = new ProjectInfoFile(null, startDate, testProjectName, null, null, null, shapeFile, null, null, null, null, null, null, null, summaries);
    }

    @Before
    public void setUpBeforeTests() throws SQLException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException {
        // Remove test schemas if they exist
        Statement stmt = con.createStatement();
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testGlobalSchema
                ));
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testSchemaName
                ));

        // Run method under test - defined for MODIS plugin
        ArrayList<String> tempCompNames = new ArrayList<String>(1);
        tempCompNames.add(compStrategy.getClass().getCanonicalName());
        Schemas.CreateProjectPluginSchema(DatabaseConnector.getConnection(), testGlobalSchema, projectMetaData, testPluginName, summaryNames, tempCompNames, daysPerInputFile, filesPerDay,
                true);
        stmt.close();
    }

    @After
    public void tearDownAfterTests() throws SQLException {
    }

    @AfterClass
    public static void tearDownAfterClass() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testGlobalSchema
                ));
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testSchemaName
                ));
        stmt.close();
        con.close();
    }

    @Test
    public final void testGetSchemaName() {
        String schemaName = Schemas.getSchemaName(testProjectName, testPluginName);
        assertTrue("Schema name is " + schemaName, schemaName.equals("test_project_test_plugin"));

        schemaName = Schemas.getSchemaName("1Test_Project", testPluginName);
        assertTrue("Schema name is " + schemaName, schemaName.equals("_test_project_test_plugin"));

        schemaName = Schemas.getSchemaName(" ", " ");
        assertTrue("Schema name is " + schemaName, schemaName.equals("___"));
    }

    @Test
    public final void testCreateProjectPluginSchema() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = null;

        // Check the created test schemas
        String query = "select n.nspname as \"Name\", count(*) over() as \"RowCount\" " +
                "from pg_catalog.pg_namespace n " +
                "where (n.nspname = '" + testGlobalSchema + "' OR n.nspname = '" + testSchemaName + "') " +
                "AND pg_catalog.pg_get_userbyid(n.nspowner) = '" + Config.getInstance().getDatabaseUsername() + "' " +
                "order by \"RowCount\" desc;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertTrue("Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 2);
        } else {
            fail("ResultSet is null from querying schemas");
        }

        // Check the existence of the test global schema tables
        query = "SELECT c.relname as \"Name\", count(*) over() as \"RowCount\" " +
                "FROM pg_catalog.pg_class c LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                "WHERE c.relkind = 'r' AND n.nspname = '" + testGlobalSchema + "' " +
                "AND pg_catalog.pg_get_userbyid(c.relowner) = '" + Config.getInstance().getDatabaseUsername() + "' " +
                "ORDER BY \"RowCount\" desc;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertEquals("Not expected number of tables in the created Global Schema.", 13, rs.getInt("RowCount"));

            // Check global schema table names
            do{
                switch(rs.getString("Name"))
                {
                case "DateGroup": break;
                case "Index": break;
                case "Project": break;
                case "Plugin": break;
                case "GlobalDownloader": break;
                case "Download": break;
                case "DownloadExtra": break;
                case "TemporalSummaryCompositionStrategy": break;
                case "ProjectSummary": break;
                case "DownloadExpectedTotalOutput": break;
                case "ProcessorExpectedTotalOutput": break;
                case "IndicesExpectedTotalOutput": break;
                case "SummaryExpectedTotalOutput": break;
                default: fail("Unknown table in test global schema: " + rs.getString("Name"));
                }
            } while(rs.next());
        } else {
            fail("ResultSet is null from querying test global schema tables");
        }

        // Check the existence of the test schema tables
        query = "SELECT c.relname as \"Name\", count(*) over() as \"RowCount\" " +
                "FROM pg_catalog.pg_class c LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                "WHERE c.relkind = 'r' AND n.nspname = '" + testSchemaName.toLowerCase() + "' " +
                "AND pg_catalog.pg_get_userbyid(c.relowner) = '" + Config.getAnInstance("src/test/config.xml").getDatabaseUsername() + "' " +
                "ORDER BY \"RowCount\" desc;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertTrue("Test Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 5);

            // Check schema table names
            do{
                switch(rs.getString("Name"))
                {
                case "ZonalStat": break;
                case "DownloadCache": break;
                case "DownloadCacheExtra": break;
                case "ProcessorCache": break;
                case "IndicesCache": break;
                default: fail("Unknown table in test schema: " + rs.getString("Name"));
                }
            } while(rs.next());
        } else {
            fail("ResultSet is null from querying test schema tables");
        }

        stmt.close();
        rs.close();
    }
}
