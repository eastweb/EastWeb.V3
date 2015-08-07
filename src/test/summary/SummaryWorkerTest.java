/**
 *
 */
package test.summary;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
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

import version2.prototype.Config;
import version2.prototype.Process;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.summary.SummaryWorker;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.EASTWebResult;
import version2.prototype.util.EASTWebResults;
import version2.prototype.util.IndicesFileMetaData;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class SummaryWorkerTest {
    private static Config configInstance;
    private static String projectName = "Test_Summary";
    private static String pluginName = "Test_TRMM3B42RT";
    private static String globalSchema = "Test_EASTWeb";
    private static ProjectInfoFile projectInfoFile;
    private static LocalDate startDate;
    private static ProjectInfoPlugin pluginInfo;
    private static PluginMetaData pluginMetaData;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        configInstance = Config.getAnInstance("src\\test\\config.xml");
        globalSchema = configInstance.getGlobalSchema();
        projectInfoFile = new ProjectInfoFile("src\\test\\summary\\Test_Project.xml");
        startDate = projectInfoFile.GetStartDate();
        pluginInfo = projectInfoFile.GetPlugins().get(0);
        pluginMetaData = PluginMetaDataCollection.getInstance(new File("plugins\\Plugin_TRMM3B42RT.xml")).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());

        Connection con = PostgreSQLConnection.getConnection();
        Statement stmt = con.createStatement();
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

        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), globalSchema, projectName, pluginName, null, null, pluginMetaData.DaysPerInputData,
                pluginMetaData.Download.filesPerDay, pluginMetaData.IndicesMetaData.size(), projectInfoFile.GetSummaries(), false);
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
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.summary.SummaryWorker#call()}.
     * @throws Exception
     */
    @Test
    public final void testCall() throws Exception {

        // Setup test files
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm1.tif", startDate.getYear(), startDate.minusDays(6).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm2.tif", startDate.getYear(), startDate.minusDays(5).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm3.tif", startDate.getYear(), startDate.minusDays(4).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm4.tif", startDate.getYear(), startDate.minusDays(3).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm5.tif", startDate.getYear(), startDate.minusDays(2).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm6.tif", startDate.getYear(), startDate.minusDays(1).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm7.tif", startDate.getYear(), startDate.getDayOfYear(), "TRMM3B42RTIndex")));

        SummaryWorker worker = new SummaryWorker(configInstance, null, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, null);
        worker.call();

        // Verify results
        ArrayList<EASTWebResult> results = EASTWebResults.GetEASTWebResults(EASTWebResults.GetEASTWebQuery(globalSchema, projectInfoFile.GetProjectName(), pluginInfo.GetName()));
        assertTrue("Results empty.", results.size() > 0);
    }

    //    protected class MyDatabaseCache extends DatabaseCache
    //    {
    //        public MyDatabaseCache(String globalSchema, String projectName, String pluginName, ProcessName dataComingFrom, ArrayList<String> extraDownloadFiles) throws ParseException {
    //            super(globalSchema, projectName, pluginName, dataComingFrom, extraDownloadFiles);
    //        }
    //
    //        @Override
    //        public void CacheFiles(ArrayList<DataFileMetaData> filesForASingleComposite) throws SQLException, ParseException, ClassNotFoundException,
    //        ParserConfigurationException, SAXException, IOException {
    //            for(DataFileMetaData data : filesForASingleComposite)
    //            {
    //                System.out.println(data.ReadMetaDataForSummary().dataFilePath);
    //            }
    //        }
    //    }
}
