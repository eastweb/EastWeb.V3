/**
 *
 */
package test.indices;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
public class IndicesWorkerTest {
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
     * Test method for {@link version2.prototype.indices.IndicesWorker#call()}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParseException
     */
    @Test
    public final void testCall() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException, SQLException, ParseException {
        // Set up parameters
        String globalSchema = "";
        String xmlLocation = "";
        String pluginMetaDataFile = "";
        int day = 0;
        int year = 0;
        Process process = null;
        ProjectInfoFile projectInfoFile = new ProjectInfoFile(xmlLocation);
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File(pluginMetaDataFile)).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());
        Schemas.CreateProjectPluginSchema(DatabaseConnector.getConnection(), "Test_EASTWeb", "Test_Project", "Test_Plugin", null, null,
                pluginMetaData.DaysPerInputData, pluginMetaData.Download.filesPerDay, pluginMetaData.Indices.indicesNames.size(), projectInfoFile.GetSummaries(), false);

        // Setup test files
        ArrayList<DownloadFileMetaData> extraDownloads = new ArrayList<DownloadFileMetaData>(1);
        extraDownloads.add(new DownloadFileMetaData("QC", "QC download file path", year, day));
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "Data download file path", year, day)));

        DatabaseCache outputCache = new MyDatabaseCache(globalSchema, projectInfoFile.GetProjectName(), pluginInfo, ProcessName.INDICES, pluginMetaData.ExtraDownloadFiles);
        ProcessorWorker worker = new ProcessorWorker(configInstance, process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);

        // Verify results
        ArrayList<DataFileMetaData> result = outputCache.GetUnprocessedCacheFiles();
        fail("Not yet implemented"); // TODO
    }

    protected class MyDatabaseCache extends DatabaseCache
    {
        public MyDatabaseCache(String globalSchema, String projectName, ProjectInfoPlugin pluginInfo, ProcessName dataComingFrom, ArrayList<String> extraDownloadFiles)
                throws ParseException, ParserConfigurationException, SAXException, IOException {
            super(new MyScheduler(), globalSchema, projectName, pluginInfo, PluginMetaDataCollection.CreatePluginMetaData(null, null, null, null, null, null, null, null, extraDownloadFiles), null,
                    dataComingFrom);
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

    private class MyScheduler extends Scheduler
    {
        public MyScheduler() {
            super(null, null, 0, null, null);
        }

        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            // Do nothing
        }
    }

}
