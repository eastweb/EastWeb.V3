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
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

public class Test_ProcessorWorker_TRMM3B42RT {

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
        ProjectInfoFile projectInfoFile = new ProjectInfoFile("C:\\Users\\yi.liu.JACKS\\git\\EastWeb.V2\\src\\version2\\prototype\\ProjectInfoMetaData\\Project_Amhara.xml");
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File("C:\\Users\\yi.liu.JACKS\\git\\EastWeb.V2\\src\\version2\\prototype\\pluginMetaData\\Plugin_TRMM3B42RT.xml")).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());
        //ArrayList<String> extraDownloadFiles;
        //extraDownloadFiles.add("QC");
        //System.out.println(pluginMetaData.DaysPerInputData);
        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), "Test_EASTWeb", "Test_Project", "Test_Plugin", null, null, null,
                pluginMetaData.DaysPerInputData, pluginMetaData.Download.filesPerDay, pluginMetaData.IndicesMetaData.size(), projectInfoFile.GetSummaries(), false);

        // Setup test files
        ArrayList<DownloadFileMetaData> extraDownloads = new ArrayList<DownloadFileMetaData>(1);
        // extraDownloads.add(new DownloadFileMetaData("QC", "QC download file path", year, day, null));
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "D:\\project\\download\\TRMM2\\2015\\118\\3B42_daily.2015.04.29.7.bin", 2015, 118, null)));

        //"Blah", "Project_Amhara", "TRMM3B42RT", ProcessName.DOWNLOAD, null

        DatabaseCache outputCache = new MyDatabaseCache("project_amhara_trmm3b42.ProcessorCache", projectInfoFile.GetProjectName(), pluginInfo.GetName(), ProcessName.PROCESSOR, null);
        ProcessorWorker worker = new ProcessorWorker(process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);

        // Verify results
        //ArrayList<DataFileMetaData> result = outputCache.GetUnprocessedCacheFiles();
        worker.call();
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
                System.out.println(data.ReadMetaDataForIndices().dataFilePath);
            }
        }
    }

}
