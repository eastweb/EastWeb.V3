/**
 *
 */
package test.summary;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

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
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class SummaryWorkerTest {
    private static String testProjectName = "Test_Project";
    private static String testPluginName = "Test_Plugin";
    private static String testGlobalSchema = "Test_EASTWeb";
    private static String projectInfoFilePath = "";
    private static String pluginMetaDataFilePath = "";
    private static int year;
    private static int day;
    ;
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
        // Set up parameters
        String globalSchema = "";
        Process process = null;
        ProjectInfoFile projectInfoFile = new ProjectInfoFile(projectInfoFilePath);
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File(pluginMetaDataFilePath)).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());
        ArrayList<String> extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");
        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), "Test_EASTWeb", "Test_Project", "Test_Plugin", null, null, pluginMetaData.DaysPerInputData, pluginMetaData.Download.filesPerDay, pluginMetaData.IndicesMetaData.size(), projectInfoFile.GetSummaries(), false);

        // Setup test files
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        ArrayList<DownloadFileMetaData> extraDownloads = new ArrayList<DownloadFileMetaData>(1);
        extraDownloads.add(new DownloadFileMetaData("QC", "QC download file path", year, day));
        cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "Data download file path", year, day)));

        DatabaseCache outputCache = new MyDatabaseCache(testGlobalSchema, projectInfoFile.GetProjectName(), pluginInfo.GetName(), ProcessName.INDICES, pluginMetaData.ExtraDownloadFiles);
        SummaryWorker worker = new SummaryWorker(process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, null);

        // Verify results
        ArrayList<EASTWebResult> results = EASTWebResults.GetEASTWebResults(EASTWebResults.GetEASTWebQuery(globalSchema, projectInfoFile.GetProjectName(), pluginInfo.GetName()));
        fail("Not yet implemented"); // TODO
    }

    protected class MyDatabaseCache extends DatabaseCache
    {
        public MyDatabaseCache(String globalSchema, String projectName, String pluginName, ProcessName dataComingFrom, ArrayList<String> extraDownloadFiles) throws ParseException {
            super(globalSchema, projectName, pluginName, dataComingFrom, extraDownloadFiles);
        }

        @Override
        public void CacheFiles(ArrayList<DataFileMetaData> filesForASingleComposite) throws SQLException, ParseException, ClassNotFoundException,
        ParserConfigurationException, SAXException, IOException {
            for(DataFileMetaData data : filesForASingleComposite)
            {
                System.out.println(data.ReadMetaDataForSummary().dataFilePath);
            }
        }
    }
}
