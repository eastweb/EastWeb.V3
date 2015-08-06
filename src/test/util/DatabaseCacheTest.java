/**
 *
 */
package test.util;

import static org.junit.Assert.*;

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

import version2.prototype.Scheduler.ProcessName;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
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
    private static String testProjectName = "Test_Project1";
    private static String testPluginName = "Test_Plugin1";
    private static String testGlobalSchema = "Test_EASTWeb1";
    private static Connection con;
    private static int year = 2015;
    private static int day = 100;
    private static int daysPerInputFile = 1;
    private static int numOfIndices = 3;
    private static int filesPerDay = 1;
    private static String data1FilePath = "Data file path";

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
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                testGlobalSchema
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(testProjectName, testPluginName)
                );
        stmt.execute(query);
        stmt.close();

        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), testGlobalSchema, testProjectName, testPluginName, null, LocalDate.ofYearDay(year, day), daysPerInputFile,
                filesPerDay, numOfIndices, null, true);
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
        filesForASingleComposite.add(new DataFileMetaData("Data file path", 2015, 100, "Index"));

        Statement stmt = con.createStatement();
        stmt.execute("INSERT INTO \"" + testGlobalSchema + "\".\"GlobalDownloader\" (\"PluginID\", \"DataName\") VALUES " +
                "(1, 'Data')," +
                "(1, 'QC');");
        stmt.close();

        // Cache to ProcessorCache
        filesForASingleComposite = new ArrayList<DataFileMetaData>();
        filesForASingleComposite.add(new DataFileMetaData(data1FilePath, 2015, 100, "Index"));
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
     * Test method for {@link version2.prototype.util.DatabaseCache#LoadUnprocessedGlobalDownloadsToLocalDownloader(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.time.LocalDate,
     * java.util.ArrayList, java.util.ArrayList)}.
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
        ArrayList<String> modisTileNames = new ArrayList<String>();
        String modisTile1 = "v12H42";
        modisTileNames.add(modisTile1);
        String dateFilePath1 = "path to data file1 " + modisTile1;
        String dateFilePath2 = "path to data file2 " + modisTile1;
        String dateFilePath3 = "path to data file3";
        String qcFilePath1 = "path to qc file1 " + modisTile1;
        String qcFilePath2 = "path to qc file2 " + modisTile1;
        String qcFilePath3 = "path to qc file3";
        String query;
        LocalDate startDate = LocalDate.now().minusDays(8);;

        query = String.format("INSERT INTO \"%1$s\".\"DateGroup\" (\"DayOfYear\", \"Year\") VALUES " +
                "(" + LocalDate.now().getDayOfYear() + ", " + LocalDate.now().getYear() + ")",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"GlobalDownloader\" (\"PluginID\", \"DataName\") VALUES " +
                "(1, 'Data')," +
                "(1, 'QC');",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\", \"Complete\") VALUES " +
                "(1, 1, '" + dateFilePath1 + "', TRUE), " +
                "(1, 2, '" + dateFilePath2 + "', TRUE), " +
                "(1, 2, '" + dateFilePath3 + "', TRUE), " +
                "(1, 2, 'blah', FALSE);",
                testGlobalSchema);
        stmt.execute(query);

        query = String.format("INSERT INTO \"%1$s\".\"DownloadExtra\" (\"GlobalDownloaderID\", \"DataName\", \"FilePath\", \"DateGroupID\", \"Complete\") VALUES " +
                "(2, 'QC', '" + qcFilePath1 + "', 1, TRUE), " +
                "(2, 'QC', '" + qcFilePath2 + "', 2, TRUE), " +
                "(2, 'QC', '" + qcFilePath3 + "', 2, TRUE);",
                testGlobalSchema);
        stmt.execute(query);

        testDownloadCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(testGlobalSchema, testProjectName, testPluginName, "Data", startDate, extraDownloadFiles, modisTileNames);
        testDownloadCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(testGlobalSchema, testProjectName, testPluginName, "QC", startDate, extraDownloadFiles, modisTileNames);

        query = "SELECT * FROM \"" + schemaName + "\".\"DownloadCache\"";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            String path1, path2;
            int downloadID1, downloadID2;
            int dateGroupID1, dateGroupID2;

            if(rs.next())
            {
                if(rs.getInt("DateGroupID") == 1)
                {
                    path1 = dateFilePath1;
                    downloadID1 = 1;
                    dateGroupID1 = 1;
                    path2 = dateFilePath2;
                    downloadID2= 2;
                    dateGroupID2 = 2;
                }
                else
                {
                    path1 = dateFilePath2;
                    downloadID1 = 2;
                    dateGroupID1 = 2;
                    path2 = dateFilePath1;
                    downloadID2= 1;
                    dateGroupID2 = 1;
                }

                assertTrue(schemaName + ".DownloadCache Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheID") + ", " + rs.getString("DataFilePath") + ", " +
                        rs.getInt("DownloadID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                        rs.getInt("DownloadCacheID") == rs.getRow() &&
                        rs.getString("DataFilePath").equals(path1) &&
                        rs.getInt("DownloadID") == downloadID1 &&
                        rs.getInt("DateGroupID") == dateGroupID1 &&
                        rs.getBoolean("Retrieved") == false &&
                        rs.getBoolean("Processed") == false);

                if(rs.next())
                {
                    assertTrue(schemaName + ".DownloadCache Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheID") + ", " + rs.getString("DataFilePath") + ", " +
                            rs.getInt("DownloadID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                            rs.getInt("DownloadCacheID") == rs.getRow() &&
                            rs.getString("DataFilePath").equals(path2) &&
                            rs.getInt("DownloadID") == downloadID2 &&
                            rs.getInt("DateGroupID") == dateGroupID2 &&
                            rs.getBoolean("Retrieved") == false &&
                            rs.getBoolean("Processed") == false);

                    if(rs.next())
                    {
                        fail("More than 2 files loaded into DownloadCache.");
                    }
                }
            }
        }
        rs.close();

        query = "SELECT * FROM \"" + schemaName + "\".\"DownloadCacheExtra\"";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            if(rs.next())
            {
                String path1, path2;
                int downloadID1, downloadID2;
                int dateGroupID1, dateGroupID2;

                if(rs.getInt("DateGroupID") == 1)
                {
                    path1 = qcFilePath1;
                    downloadID1 = 1;
                    dateGroupID1 = 1;
                    path2 = qcFilePath2;
                    downloadID2= 2;
                    dateGroupID2 = 2;
                }
                else
                {
                    path1 = qcFilePath2;
                    downloadID1 = 2;
                    dateGroupID1 = 2;
                    path2 = qcFilePath1;
                    downloadID2= 1;
                    dateGroupID2 = 1;
                }
                assertTrue(schemaName + ".DownloadCacheExtra Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheExtraID") + ", " + rs.getString("FilePath") + ", " +
                        rs.getInt("DownloadExtraID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                        rs.getInt("DownloadCacheExtraID") == rs.getRow() &&
                        rs.getString("FilePath").equals(path1) &&
                        rs.getInt("DownloadExtraID") == downloadID1 &&
                        rs.getInt("DateGroupID") == dateGroupID1 &&
                        rs.getBoolean("Retrieved") == false &&
                        rs.getBoolean("Processed") == false);

                if(rs.next())
                {
                    assertTrue(schemaName + ".DownloadCacheExtra Row " + rs.getRow() + " contains: (" + rs.getInt("DownloadCacheExtraID") + ", " + rs.getString("FilePath") + ", " +
                            rs.getInt("DownloadExtraID") + ", " + rs.getInt("DateGroupID") + ", " + rs.getBoolean("Retrieved") + ", " + rs.getBoolean("Processed"),
                            rs.getInt("DownloadCacheExtraID") == rs.getRow() &&
                            rs.getString("FilePath").equals(path2) &&
                            rs.getInt("DownloadExtraID") == downloadID2 &&
                            rs.getInt("DateGroupID") == dateGroupID2 &&
                            rs.getBoolean("Retrieved") == false &&
                            rs.getBoolean("Processed") == false);

                    if(rs.next())
                    {
                        fail("More than 2 files loaded into DownloadCacheExtra.");
                    }
                }
            }
        }
        rs.close();
        stmt.close();
    }

}
