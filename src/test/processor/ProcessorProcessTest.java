/**
 *
 */
package test.processor;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.GenericProcess;
import version2.prototype.Process;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.Scheduler.SchedulerData;
import version2.prototype.processor.ProcessorWorker;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;

/**
 * @author michael.devos
 *
 */
public class ProcessorProcessTest {

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

    @Test
    public final void test() {
        ProjectInfoPlugin pluginInfo = null;
        PluginMetaData pluginMetaData = null;
        DatabaseCache inputCache = null;
        DatabaseCache outputCache = null;
        ProjectInfoFile projectInfoFile = null;
        String pluginMetaDataFilePath = "";
        SchedulerData sData;
        try {
            sData = new SchedulerData(projectInfoFile, pluginMetaDataFilePath);
            Scheduler scheduler = new Scheduler(sData, 1, TaskState.STOPPED, null);

            Process process = new GenericProcess<ProcessorWorker>(ProcessName.PROCESSOR, projectInfoFile, pluginInfo, pluginMetaData, scheduler, inputCache, outputCache);

            DataFileMetaData newData = new DataFileMetaData("Data", "filepath", dataGroupID, year, day);
            inputCache.CacheFile(newData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
