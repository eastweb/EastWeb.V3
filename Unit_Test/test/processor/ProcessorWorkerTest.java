/**
 *
 */
package test.processor;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
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
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.processor.ProcessorWorker;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.GeneralUIEventObject;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class ProcessorWorkerTest {
    private static Config configInstance = Config.getAnInstance("src/test/config.xml");

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
     * Test method for {@link version2.prototype.processor.ProcessorWorker#call()}.
     * @throws Exception
     */
    @Test
    public final void testCall() throws Exception {
        Process process = null;
        ProjectInfoFile projectInfoFile = new ProjectInfoFile(configInstance, "C:\\Users\\yi.liu.JACKS\\git\\EastWeb.V2\\src\\version2\\prototype\\ProjectInfoMetaData\\Project_Amhara.xml");
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File("C:\\Users\\yi.liu.JACKS\\git\\EastWeb.V2\\src\\version2\\prototype\\pluginMetaData\\Plugin_TRMM3B42RT.xml"))
                .pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());
        //ArrayList<String> extraDownloadFiles;
        //extraDownloadFiles.add("QC");
        Schemas.CreateProjectPluginSchema(DatabaseConnector.getConnection(), "Test_EASTWeb", projectInfoFile, "Test_Plugin", null, null, pluginMetaData.DaysPerInputData, pluginMetaData.Download.filesPerDay,
                false);

        // Setup test files
        ArrayList<DownloadFileMetaData> extraDownloads = new ArrayList<DownloadFileMetaData>(1);
        // extraDownloads.add(new DownloadFileMetaData("QC", "QC download file path", year, day, null));
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "D:\\testProjects\\Amhara\\downloads\\2014\\002\\3B42RT_daily.2014.01.02.bin", 1, 2014, 002)));

        //"Blah", "Project_Amhara", "TRMM3B42RT", ProcessName.DOWNLOAD, null

        DatabaseCache outputCache = new MyDatabaseCache("project_amhara_trmm3b42rt.ProcessorCache", projectInfoFile.GetProjectName(), pluginInfo, ProcessName.PROCESSOR, null);
        ProcessorWorker worker = new ProcessorWorker(configInstance, process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);

        // Verify results
        //ArrayList<DataFileMetaData> result = outputCache.GetUnprocessedCacheFiles();
        worker.call();
    }

    protected class MyDatabaseCache extends DatabaseCache
    {
        public MyDatabaseCache(String globalSchema, String projectName, ProjectInfoPlugin pluginInfo, ProcessName dataComingFrom, ArrayList<String> extraDownloadFiles) throws ParseException,
        ParserConfigurationException, SAXException, IOException {
            super(new MyScheduler(), globalSchema, projectName, pluginInfo, PluginMetaDataCollection.CreatePluginMetaData(null, null, null, null, extraDownloadFiles, null, null, null, null, null, null), null,
                    dataComingFrom);
        }

        @Override
        public void CacheFiles(Statement stmt, ArrayList<DataFileMetaData> filesForASingleComposite) throws SQLException, ParseException, ClassNotFoundException,
        ParserConfigurationException, SAXException, IOException {
            for(DataFileMetaData data : filesForASingleComposite)
            {
                System.out.println(data.ReadMetaDataForIndices().dataFilePath);
            }
        }
    }

    private class MyScheduler extends Scheduler
    {
        public MyScheduler() {
            super(null, null, false, 0, null, null);
        }

        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            // Do nothing
        }
    }

}
