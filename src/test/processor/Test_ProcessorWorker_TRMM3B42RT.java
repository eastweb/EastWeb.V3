package test.processor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import test.processor.ProcessorWorkerTest.MyDatabaseCache;
import version2.prototype.Config;
import version2.prototype.Process;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.processor.ProcessorWorker;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.Schemas;

public class Test_ProcessorWorker_TRMM3B42RT {
    private static Config configInstance = Config.getAnInstance("src/test/config.xml");

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
        ProjectInfoFile projectInfoFile = new ProjectInfoFile(configInstance, "C:\\Users\\yi.liu\\git\\EastWeb.V2\\src\\version2\\prototype\\ProjectInfoMetaData\\Project_TW2.xml");
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File("C:\\Users\\yi.liu\\git\\EastWeb.V2\\src\\version2\\prototype\\pluginMetaData\\Plugin_ModisLST.xml")).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());
        //ArrayList<String> extraDownloadFiles;
        //extraDownloadFiles.add("QC");
        //System.out.println(pluginMetaData.DaysPerInputData);

        // Setup test files
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        //cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "D:\\project\\download\\TRMM\\2014\\020\\3B42RT_daily.2014.01.20.bin", 2014, 020)));

        cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "D:\\project\\download\\MODISLST\\2014\\081\\h28v06.hdf", 1, 2015, 201)));
        cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "D:\\project\\download\\MODISLST\\2014\\081\\h29v06.hdf", 1, 2015, 201)));

        //"Blah", "Project_Amhara", "TRMM3B42RT", ProcessName.DOWNLOAD, null

        ProcessorWorker worker = new ProcessorWorker(configInstance, process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, null);

        // Verify results
        //ArrayList<DataFileMetaData> result = outputCache.GetUnprocessedCacheFiles();
        worker.call();
    }


}
