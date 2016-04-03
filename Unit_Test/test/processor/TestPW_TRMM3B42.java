package test.processor;


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
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

public class TestPW_TRMM3B42 {
    private static Config configInstance = Config.getAnInstance("src/test/config.xml");

    public TestPW_TRMM3B42() {
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testCall() throws Exception
    {
        Process process = null;
        ProjectInfoFile projectInfoFile = new ProjectInfoFile(configInstance, "C:\\Users\\yi.liu\\git\\EastWeb.V2\\projects\\Project_TW.xml");
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File("C:\\Users\\yi.liu\\git\\EastWeb.V2\\plugins\\Plugin_TRMM3B42.xml")).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());

        //ArrayList<String> extraDownloadFiles;
        //extraDownloadFiles.add("QC");
        //        Schemas.CreateProjectPluginSchema(DatabaseConnector.getConnection(), "Test_EASTWeb", "Test_Project", "Test_Plugin", null, null, null,
        //                pluginMetaData.DaysPerInputData, pluginMetaData.Download.filesPerDay,
        //                pluginMetaData.IndicesMetaData.size(), projectInfoFile.GetSummaries(), false);

        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "D:\\project\\download\\TRMM2\\2015\\118\\3B42_daily.2015.04.29.7.bin", 1, 2015, 118)));

        //"Blah", "Project_TW", "TRMM3B42", ProcessName.Processor, null

        // DatabaseCache outputCache = new MyDatabaseCache("project_tw_trmm3b42.ProcessorCache", projectInfoFile.GetProjectName(), pluginInfo.GetName(), ProcessName.PROCESSOR, null);
        ProcessorWorker worker = new ProcessorWorker(null, process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, null);

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
