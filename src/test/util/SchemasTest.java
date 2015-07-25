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
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.ZonalSummary;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.ProcessorFileMetaData;
import version2.prototype.util.IndicesFileMetaData;
import version2.prototype.util.PostgreSQLConnection;
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
    private static int numOfIndices;

    /**
     * Defined for executable jar file to be used in setting up EASTWeb database for testing purposes. The config.xml file is needed for the database connection information but nothing else is used from
     * in it. The summary calculation fields created are "Count", "Sum", "Mean", and "StdDev". The "extra download file" fields created are just "QC" which effects global Download table and the caches.
     * Tables are created so that foreign key fields are not referencing their counterparts and foreign key rules are not required to be respected when using them.
     *
     * @param args  - 1. Global schema name, 2. Project name, 3. Plugin name, 4. True/False if foreign keys should reference their associated tables. Two schemas are created:
     * 1. The global schema and 2. A schema named by combining project name and plugin name separated by an '_'.
     * @throws ConfigReadException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void main(String[] args) throws ConfigReadException, ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        setUpBeforeClass();
        boolean createTablesWithForeignKeyReferences = false;
        if(args.length > 3) {
            createTablesWithForeignKeyReferences = Boolean.parseBoolean(args[3]);
        }
        System.out.println("Setting up PostgreSQL Schema...");
        System.out.println("Global schema to use or create: " + args[0]);
        System.out.println("Project name to use: " + args[1]);
        System.out.println("Plugin name to use: " + args[2]);
        System.out.println("Project schema to create or recreate: " + Schemas.getSchemaName(args[1], args[2]));
        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), args[0], args[1], args[2], summaryNames, extraDownloadFiles, LocalDate.now().minusDays(8), 8, 3, null,
                createTablesWithForeignKeyReferences);
        System.out.println("DONE");
    }

    @BeforeClass
    public static void setUpBeforeClass() throws SQLException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException {
        con = PostgreSQLConnection.getConnection();
        testProjectName = "Test_Project";
        testPluginName = "Test_Plugin";
        testGlobalSchema = "Test_EASTWeb";
        testSchemaName = testProjectName + "_" + testPluginName;
        shapeFile = "C:\\Users\\sufi\\Desktop\\shapefile\\shapefile.shp";
        areaValueField = "COUNTYNS10";
        areaNameField = "NAME10";
        compStrategy = new GregorianWeeklyStrategy();
        startDate = LocalDate.now().minusDays(8);
        daysPerInputFile = 8;
        numOfIndices = 3;

        summaryNames = new ArrayList<String>();
        summaryNames.add("Count");
        summaryNames.add("Sum");
        summaryNames.add("Mean");
        summaryNames.add("StdDev");

        summaries = new ArrayList<ProjectInfoSummary>(0);
        summaries.add(new ProjectInfoSummary(new ZonalSummary(shapeFile, areaValueField, areaNameField), new TemporalSummaryRasterFileStore(compStrategy), compStrategy.getClass().getCanonicalName(), 1));

        extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");

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
    }

    @After
    public void tearDownAfterTests() throws SQLException {
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
    }

    @AfterClass
    public static void tearDownAfterClass() throws SQLException {
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

        // Run method under test - defined for MODIS plugin
        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), testGlobalSchema, testProjectName, testPluginName, summaryNames, extraDownloadFiles, startDate, daysPerInputFile,
                numOfIndices, summaries, true);

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
            assertTrue("Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 1);
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
            assertTrue("Test Global Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 11);

            // Check global schema table names
            do{
                switch(rs.getString("Name"))
                {
                case "DateGroup": break;
                case "Index": break;
                case "ZoneEW": break;
                case "ZoneVar": break;
                case "ExpectedResults": break;
                case "Project": break;
                case "Plugin": break;
                case "GDExpectedResults": break;
                case "GlobalDownloader": break;
                case "Download": break;
                case "ExtraDownload": break;
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
                "AND pg_catalog.pg_get_userbyid(c.relowner) = '" + Config.getAnInstance("Config.xml").getDatabaseUsername() + "' " +
                "ORDER BY \"RowCount\" desc;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertTrue("Test Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 6);

            // Check schema table names
            do{
                switch(rs.getString("Name"))
                {
                case "ZoneMapping": break;
                case "ZoneField": break;
                case "ZonalStat": break;
                case "DownloadCache": break;
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

    @Test
    public final void testLoadUnprocessedDownloadsToLocalDownloader() throws ConfigReadException, ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        String schemaName = Schemas.getSchemaName(testProjectName, testPluginName);
        String dateFilePath1 = "path to data file1";
        String dateFilePath2 = "path to data file2";
        String qcFilePath1 = "path to qc file1";
        String qcFilePath2 = "path to qc file2";
        String query;

        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), testGlobalSchema, testProjectName, testPluginName, summaryNames, extraDownloadFiles, startDate, daysPerInputFile,
                numOfIndices, summaries, true);

        query = String.format("INSERT INTO \"%1$s\".\"DateGroup\" (\"DayOfYear\", \"Year\") VALUES (" + LocalDate.now().getDayOfYear() + ", " + LocalDate.now().getYear() + ")",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"GlobalDownloader\" (\"PluginID\", \"UniqueInstanceNum\") VALUES (1, 1);",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\") VALUES (1, 1, '" + dateFilePath1 + "'), (1, 2, '" + dateFilePath2 +
                "');",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"ExtraDownload\" (\"DownloadID\", \"DataName\", \"FilePath\") VALUES (1, 'QC', '" + qcFilePath1 + "'), (2, 'QC', '" + qcFilePath2 + "');",
                testGlobalSchema);
        stmt.execute(query);

        Schemas.loadUnprocessedDownloadsToLocalDownloader(testGlobalSchema, testProjectName, testPluginName, 1, startDate, extraDownloadFiles, daysPerInputFile);

        query = "SELECT * FROM \"" + schemaName + "\".\"DownloadCache\"";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            if(rs.next())
            {
                assertTrue(schemaName + ".DownloadCache Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheID") + ", " + rs.getString("DataFilePath") + ", " + rs.getString("QCFilePath") + ", " +
                        rs.getInt("DownloadID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                        rs.getInt("DownloadCacheID") == rs.getRow() &&
                        rs.getString("DataFilePath").equals(dateFilePath1) &&
                        rs.getString("QCFilePath").equals(qcFilePath1) &&
                        rs.getInt("DownloadID") == 1 &&
                        rs.getInt("DateGroupID") == 1 &&
                        rs.getBoolean("Retrieved") == false &&
                        rs.getBoolean("Processed") == false);
            }
            if(rs.next())
            {
                assertTrue(schemaName + ".DownloadCache Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheID") + ", " + rs.getString("DataFilePath") + ", " + rs.getString("QCFilePath") + ", " +
                        rs.getInt("DownloadID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                        rs.getInt("DownloadCacheID") == rs.getRow() &&
                        rs.getString("DataFilePath").equals(dateFilePath2) &&
                        rs.getString("QCFilePath").equals(qcFilePath2) &&
                        rs.getInt("DownloadID") == 2 &&
                        rs.getInt("DateGroupID") == 2 &&
                        rs.getBoolean("Retrieved") == false &&
                        rs.getBoolean("Processed") == false);
            }
        }
    }

    @Test
    public final void testGetAllDownloadedFiles() throws ConfigReadException, ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        String schemaName = Schemas.getSchemaName(testProjectName, testPluginName);
        String dateFilePath1 = "path to data file1";
        String dateFilePath2 = "path to data file2";
        String qcFilePath1 = "path to qc file1";
        String qcFilePath2 = "path to qc file2";
        String query;

        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), testGlobalSchema, testProjectName, testPluginName, summaryNames, extraDownloadFiles, startDate, daysPerInputFile,
                numOfIndices, summaries, true);

        query = String.format("INSERT INTO \"%1$s\".\"DateGroup\" (\"DayOfYear\", \"Year\") VALUES (" + LocalDate.now().getDayOfYear() + ", " + LocalDate.now().getYear() + ")",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"GlobalDownloader\" (\"PluginID\", \"UniqueInstanceNum\") VALUES (1, 1);",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\") VALUES (1, 1, '" + dateFilePath1 + "'), (1, 2, '" + dateFilePath2 +
                "');",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"ExtraDownload\" (\"DownloadID\", \"DataName\", \"FilePath\") VALUES (1, 'QC', '" + qcFilePath1 + "'), (2, 'QC', '" + qcFilePath2 + "');",
                testGlobalSchema);
        stmt.execute(query);

        ArrayList<DataFileMetaData> testResults = Schemas.getAllDownloadedFiles(testGlobalSchema, testPluginName, 1, daysPerInputFile);

        assertTrue("Test Results contains " + testResults.size() + " rows.", testResults.size() == 2);

        DownloadFileMetaData dData;
        ProcessorFileMetaData pData;
        IndicesFileMetaData iData;
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
    }

    @Test
    public final void testRegisterGlobalDownloader() throws SQLException, ConfigReadException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException {
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        final int globalDownloaderInstanceID = 10;

        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), testGlobalSchema, testProjectName, testPluginName, summaryNames, extraDownloadFiles, startDate, daysPerInputFile,
                numOfIndices, summaries, true);

        String query = String.format(
                "SELECT \"GlobalDownloaderID\" FROM \"%1$s\".\"GlobalDownloader\" WHERE \n" +
                        "\"UniqueInstanceNum\" = " + globalDownloaderInstanceID + " ORDER BY \"GlobalDownloaderID\" DESC;",
                        testGlobalSchema
                );

        // Verify no GlobalDownloaders are registered with instance ID 10
        rs = stmt.executeQuery(query);
        assertTrue("Multiple GlobalDownloaders registered with instance ID " + globalDownloaderInstanceID, rs.next() == false);

        // Register new GlobalDownloader ID
        Schemas.registerGlobalDownloader(testGlobalSchema, testPluginName, daysPerInputFile, globalDownloaderInstanceID);
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertTrue("More than one GlobalDownloader with Instance ID " + globalDownloaderInstanceID, rs.next() == false);
        } else {
            fail("Failed to register GlobalDownloader");
        }

        // Register GlobalDownloader again for Instance ID 10 (mimic behavior of stopping and restarting GlobalDownloader)
        Schemas.registerGlobalDownloader(testGlobalSchema, testPluginName, daysPerInputFile, globalDownloaderInstanceID);
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            rs.next();
            assertTrue("More than two GlobalDownloader records with Instance ID " + globalDownloaderInstanceID, rs.next() == false);
        } else {
            fail("Failed to register GlobalDownloader");
        }
    }

    @Test
    public final void testInsertIntoExtraDownloadTable() throws ConfigReadException, ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        final int globalDownloaderInstanceID = 10;
        String dateFilePath1 = "path to data file1";
        String qcFilePath1 = "path to qc file1";

        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), testGlobalSchema, testProjectName, testPluginName, summaryNames, extraDownloadFiles, startDate, daysPerInputFile,
                numOfIndices, summaries, true);

        Schemas.registerGlobalDownloader(testGlobalSchema, testPluginName, daysPerInputFile, globalDownloaderInstanceID);
        Schemas.insertIntoDownloadTable(testGlobalSchema, testPluginName, globalDownloaderInstanceID, startDate, dateFilePath1, daysPerInputFile);
        Schemas.insertIntoExtraDownloadTable(testGlobalSchema, testPluginName, globalDownloaderInstanceID, startDate, "QC", qcFilePath1, daysPerInputFile);

        ArrayList<DataFileMetaData> testResults = Schemas.getAllDownloadedFiles(testGlobalSchema, testPluginName, globalDownloaderInstanceID, daysPerInputFile);
        assertTrue("TestResults size is " + testResults.size(), testResults.size() == 1);
        DownloadFileMetaData dData = testResults.get(0).ReadMetaDataForProcessor();
        assertTrue("TestResults[0].dataName is " + dData.dataName, dData.dataName.equals("Data"));
        assertTrue("TestResults[0].dataFilePath is " + dData.dataFilePath, dData.dataFilePath.equals(dateFilePath1));
        assertTrue("TestResults[0].day is " + dData.day, dData.day == startDate.getDayOfYear());
        assertTrue("TestResults[0].year is " + dData.year, dData.year == startDate.getYear());
        assertTrue("TestResults[0].ExtraDownloads size is " + dData.extraDownloads.size(), dData.extraDownloads.size() == 1);
        dData = dData.extraDownloads.get(0);
        assertTrue("TestResults[0].ExtraDownloads[0].dataName is " + dData.dataName, dData.dataName.equals("QC"));
        assertTrue("TestResults[0].ExtraDownloads[0].dataFilePath is " + dData.dataFilePath, dData.dataFilePath.equals(qcFilePath1));
        assertTrue("TestResults[0].ExtraDownloads[0].day is " + dData.day + " not " + startDate.getDayOfYear(), dData.day == startDate.getDayOfYear());
        assertTrue("TestResults[0].ExtraDownloads[0].year is " + dData.year + " not " + startDate.getYear(), dData.year == startDate.getYear());
    }

    //    @Test
    //    public final void testUdpateExpectedResults() throws ConfigReadException, ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
    //        Statement stmt = con.createStatement();
    //        ResultSet rs = null;
    //
    //        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), testGlobalSchema, testProjectName, testPluginName, summaryNames, extraDownloadFiles, startDate, daysPerInputFile,
    //                numOfIndices, summaries, true);
    //
    //    }
}
