/**
 *
 */
package test.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.FileSystem;
import version2.prototype.util.IndicesFileMetaData;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.ProcessorFileMetaData;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class DatabaseCacheTest {
    private static DatabaseCache testDownloadCache;
    private static DatabaseCache testProcessorCache;
    private static DatabaseCache testIndicesCache;
    private static ArrayList<String> extraDownloadFiles;
    private static String testProjectName = "Test_Project";
    private static String testPluginName = "Test_Plugin";
    private static String testGlobalSchema = "Test_EASTWeb";
    private static String testWorkingDir = "C:/";
    private static Connection con;
    private static int year = 2015;
    private static int day = 100;
    private static int daysPerInputFile = -1;
    private static int numOfIndices = 3;
    private static int filesPerDay = 1;
    private static String data1FilePath = "Data file path";
    private static String qc1FilePath = "QC file path";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");
        testDownloadCache = new DatabaseCache(testGlobalSchema, testProjectName, testPluginName, ProcessName.DOWNLOAD, null);
        testProcessorCache = new DatabaseCache(testGlobalSchema, testProjectName, testPluginName, ProcessName.PROCESSOR, null);
        testIndicesCache = new DatabaseCache(testGlobalSchema, testProjectName, testPluginName, ProcessName.INDICES, null);

        con = PostgreSQLConnection.getConnection();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
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
     * Test method for both {@link version2.prototype.util.DatabaseCache#CacheFiles(java.util.ArrayList)} and {@link version2.prototype.util.DatabaseCache#GetUnprocessedCacheFiles()}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws ParseException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testCachingFilesAndRetrievingCachedFiles() throws ClassNotFoundException, SQLException, ParseException, ParserConfigurationException, SAXException, IOException {
        // Setup input
        ArrayList<DataFileMetaData> filesForASingleComposite = new ArrayList<DataFileMetaData>();
        filesForASingleComposite.add(new DataFileMetaData("Data", "Data file path", 2015, 100, "Index"));

        Statement stmt = con.createStatement();
        stmt.execute("INSERT INTO \"" + testGlobalSchema + "\".\"GlobalDownloader\" (\"PluginID\") VALUES (1);");
        String query = String.format(
                "INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\") VALUES\n" +
                        "(1, 1, '" + data1FilePath + "');",
                        testGlobalSchema
                );
        stmt.executeUpdate(query);
        query = String.format(
                "INSERT INTO \"%1$s\".\"ExtraDownload\" (\"DownloadID\", \"DataName\", \"FilePath\") VALUES\n" +
                        "(1, 'QC', '" + qc1FilePath + "');",
                        testGlobalSchema
                );
        stmt.executeUpdate(query);
        stmt.close();

        // Cache to ProcessorCache
        filesForASingleComposite = new ArrayList<DataFileMetaData>();
        filesForASingleComposite.add(new DataFileMetaData("Data", "Data file path", 2015, 100, "Index"));
        testProcessorCache.CacheFiles(filesForASingleComposite);
        // Cache to IndicesCache
        testIndicesCache.CacheFiles(filesForASingleComposite);

        // Test getting from ProcessorCache
        ArrayList<DataFileMetaData> result = testProcessorCache.GetUnprocessedCacheFiles();
        assertTrue("Number of results returned is " + result.size(), result.size() == 1);
        ProcessorFileMetaData pData1 = result.get(0).ReadMetaDataForIndices();
        assertTrue("First result Data file path is '" + pData1.dataFilePath + "'", pData1.dataFilePath.equals(data1FilePath));
        assertTrue("First result year is " + pData1.year, pData1.year == year);
        assertTrue("First result day is " + pData1.day, pData1.day == day);
        result = testProcessorCache.GetUnprocessedCacheFiles();
        assertTrue("Number of results returned is " + result.size(), result.size() == 0);

        // Test getting from IndicesCache
        result = testIndicesCache.GetUnprocessedCacheFiles();
        assertTrue("Number of results returned is " + result.size(), result.size() == 1);
        IndicesFileMetaData iData1 = result.get(0).ReadMetaDataForSummary();
        assertTrue("First result Data file path is '" + iData1.dataFilePath + "'", iData1.dataFilePath.equals(data1FilePath));
        assertTrue("First result year is " + iData1.year, iData1.year == year);
        assertTrue("First result day is " + iData1.day, iData1.day == day);
        result = testIndicesCache.GetUnprocessedCacheFiles();
        assertTrue("Number of results returned is " + result.size(), result.size() == 0);
    }

    /**
     * Test method for {@link version2.prototype.util.DatabaseCache#LoadUnprocessedGlobalDownloadsToLocalDownloader(java.lang.String, java.lang.String, java.lang.String, java.time.LocalDate, java.util.ArrayList)}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testLoadUnprocessedGlobalDownloadsToLocalDownloader() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        String schemaName = Schemas.getSchemaName(testProjectName, testPluginName);
        String dateFilePath1 = "path to data file1";
        String dateFilePath2 = "path to data file2";
        String qcFilePath1 = "path to qc file1";
        String qcFilePath2 = "path to qc file2";
        String query;
        LocalDate startDate = LocalDate.now().minusDays(8);;

        query = String.format("INSERT INTO \"%1$s\".\"DateGroup\" (\"DayOfYear\", \"Year\") VALUES (" + LocalDate.now().getDayOfYear() + ", " + LocalDate.now().getYear() + ")",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"GlobalDownloader\" (\"PluginID\") VALUES (1);",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\", \"Complete\") VALUES " +
                "(1, 1, '" + dateFilePath1 + "', TRUE), " +
                "(1, 2, '" + dateFilePath2 + "', TRUE), " +
                "(1, 2, 'blah', FALSE);",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"ExtraDownload\" (\"DownloadID\", \"DataName\", \"FilePath\") VALUES (1, 'QC', '" + qcFilePath1 + "'), (2, 'QC', '" + qcFilePath2 + "');",
                testGlobalSchema);
        stmt.execute(query);

        testDownloadCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(testGlobalSchema, testProjectName, testPluginName, startDate, extraDownloadFiles);

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
            if(rs.next())
            {
                fail("More than 2 files loaded into DownloadCache.");
            }
        }
        rs.close();
        stmt.close();
    }

    //    /**
    //     * Test method for {@link version2.prototype.util.DatabaseCache#CacheFiles(java.util.ArrayList)}.
    //     */
    //    @Test
    //    public final void testCacheFiles() {
    //        fail("Not yet implemented"); // TODO
    //    }

}
