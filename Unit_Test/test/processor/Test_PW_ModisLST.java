package test.processor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import version2.prototype.processor.ProcessorWorker;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;

public class Test_PW_ModisLST {
    private static Config configInstance = Config.getAnInstance("src/test/config.xml");

    public Test_PW_ModisLST() {
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testCall() throws Exception {
        Process process = null;
        ProjectInfoFile projectInfoFile = new ProjectInfoFile(configInstance, "C:\\Users\\yi.liu\\git\\EastWeb.V2\\projects\\Project_TW3.xml");
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File("C:\\Users\\yi.liu\\git\\EastWeb.V2\\plugins\\Plugin_ModisLST.xml")).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());
        //ArrayList<String> extraDownloadFiles;
        //extraDownloadFiles.add("QC");
        //        Schemas.CreateProjectPluginSchema(DatabaseConnector.getConnection(), "Test_EASTWeb", "Test_Project", "Test_Plugin", null, null, null,
        //                pluginMetaData.DaysPerInputData, pluginMetaData.Download.filesPerDay,
        //                pluginMetaData.IndicesMetaData.size(), projectInfoFile.GetSummaries(), false);
        //
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "D:\\project\\download\\MODISLST\\2014\\081\\h28v06.hdf", 1, 2014, 81)));
        cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "D:\\project\\download\\MODISLST\\2014\\081\\h29v06.hdf", 1, 2014, 81)));
        //"Blah", "Project_TW", "TRMM3B42", ProcessName.Processor, null

        // DatabaseCache outputCache = new MyDatabaseCache("project_tw_trmm3b42.ProcessorCache", projectInfoFile.GetProjectName(), pluginInfo.GetName(), ProcessName.PROCESSOR, null);
        ProcessorWorker worker = new ProcessorWorker(null, process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, null);

        // Verify results
        //ArrayList<DataFileMetaData> result = outputCache.GetUnprocessedCacheFiles();
        worker.call();
    }

}
