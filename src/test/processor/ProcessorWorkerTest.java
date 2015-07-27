/**
 *
 */
package test.processor;

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
import version2.prototype.processor.ProcessorWorker;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.DownloadFileMetaData;

/**
 * @author michael.devos
 *
 */
public class ProcessorWorkerTest {

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
     * @throws ParseException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testCall() throws ParseException, ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        Process process = null;
        ProjectInfoFile projectInfoFile = new ProjectInfoFile(xmlLocation);
        ProjectInfoPlugin pluginInfo = projectInfoFile.GetPlugins().get(0);
        PluginMetaData pluginMetaData = PluginMetaDataCollection.getInstance(new File(pluginMetaDataFile)).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());

        // Setup test files
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        ArrayList<DownloadFileMetaData> extraDownloads = new ArrayList<DownloadFileMetaData>(1);
        extraDownloads.add(new DownloadFileMetaData("QC", "QC download file path", year, day, null));
        cachedFiles.add(new DataFileMetaData(new DownloadFileMetaData("Data", "Data download file path", year, day, extraDownloads)));

        DatabaseCache outputCache = new DatabaseCache(projectInfoFile.GetProjectName(), pluginInfo.GetName(), ProcessName.PROCESSOR, pluginMetaData.ExtraDownloadFiles);
        ProcessorWorker worker = new ProcessorWorker(process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);

        // Verify results
        ArrayList<DataFileMetaData> result = outputCache.GetUnprocessedCacheFiles();
        fail("Not yet implemented"); // TODO
    }

}
