package test.indices;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.Process;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;

import version2.prototype.indices.IndicesWorker;

import version2.prototype.util.DataFileMetaData;

import version2.prototype.util.ProcessorFileMetaData;


public class TestInd_TRMM3B42 {

    public TestInd_TRMM3B42() {
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testCall() throws Exception {
        // Set up parameters
        Process process = null;
        ProjectInfoFile projectInfoFile = new ProjectInfoFile("C:\\Users\\yi.liu.JACKS\\git\\EastWeb.V2\\src\\version2\\prototype\\ProjectInfoMetaData\\Project_TW.xml");
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File("C:\\Users\\yi.liu.JACKS\\git\\EastWeb.V2\\src\\version2\\prototype\\PluginMetaData\\Plugin_TRMM3B42.xml")).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());


        // Setup test files

        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles.add(new DataFileMetaData(new ProcessorFileMetaData("D:\\testProjects\\TW\\Projects\\Project_TW\\TRMM3B42\\Processor\\Output\\2015\\118\\3B42_daily.2015.04.29.7.tif", 2015, 118)));


        IndicesWorker worker = new IndicesWorker(process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, null);

        worker.call();
    }

}
