package test.indices;

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
import version2.prototype.indices.IndicesWorker;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.ProcessorFileMetaData;

public class TestInd_TRMM3B42RT {
    private static Config configInstance = Config.getAnInstance("src/test/config.xml");

    public TestInd_TRMM3B42RT() {
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
        ProjectInfoFile projectInfoFile = new ProjectInfoFile("C:\\Users\\yi.liu\\git\\EastWeb.V2\\src\\version2\\prototype\\ProjectInfoMetaData\\Project_TW_TRMMrt.xml");
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File("C:\\Users\\yi.liu\\git\\EastWeb.V2\\src\\version2\\prototype\\PluginMetaData\\Plugin_TRMM3B42RT.xml")).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());


        // Setup test files

        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        //cachedFiles.add(new DataFileMetaData(new ProcessorFileMetaData("D:\\testProjects\\TW_trmmRT\\Projects\\Project_TW\\TRMM3B42RT\\Processor\\Output\\2014\\077\\3B42RT_daily.2014.03.18.tif", 2014, 77)));
        cachedFiles.add(new DataFileMetaData(new ProcessorFileMetaData("D:\\testProjects\\TW\\Projects\\Project_TW\\TRMM3B42RT\\Processor\\Output\\2014\\077\\3B42RT_daily.2014.03.18.tif", 1, 2014, 77)));


        IndicesWorker worker = new IndicesWorker(configInstance, process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, null);

        worker.call();
    }

}
