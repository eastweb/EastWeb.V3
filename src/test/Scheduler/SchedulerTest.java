/**
 *
 */
package test.Scheduler;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.concurrent.Future;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.EASTWebManagerI;
import version2.prototype.EASTWebManager;
import version2.prototype.GenericProcess;
import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.ProcessWorkerReturn;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.Scheduler.SchedulerData;
import version2.prototype.Scheduler.SchedulerStatus;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.DownloaderFactory;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.LocalDownloader;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.FileSystem;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class SchedulerTest {
    private static String testProjectName = "Test_Project";
    private static String testPluginName = "TRMM3B42RT";
    private static MyEASTWebManager manager;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        SchedulerTest temp = new SchedulerTest();
        manager = temp.new MyEASTWebManager();
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
        Connection con = PostgreSQLConnection.getConnection();
        Statement stmt = con.createStatement();
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Config.getAnInstance("src/test/Scheduler/config.xml").getGlobalSchema()
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(testProjectName, testPluginName)
                );
        stmt.execute(query);
        stmt.close();
        con.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.Scheduler#Scheduler(version2.prototype.Scheduler.SchedulerData, int, version2.prototype.TaskState, version2.prototype.EASTWebManager,
     * version2.prototype.Config)}.
     * @throws Exception
     */
    @Test
    public final void testSchedulerSchedulerDataIntTaskStateEASTWebManager() throws Exception {
        FileUtils.deleteDirectory(new File(Config.getAnInstance("src/test/Scheduler/config.xml").getDownloadDir() + testPluginName));
        ProjectInfoFile projectInfoFile = new ProjectInfoFile("src/test/Scheduler/Test_Project.xml");
        SchedulerData sData = new SchedulerData(projectInfoFile);
        MyScheduler scheduler = new MyScheduler(sData, 1, TaskState.STOPPED, manager, Config.getAnInstance("src/test/Scheduler/config.xml"));

        scheduler.Start();
        assertEquals("Scheduler state is STOPPED.", TaskState.RUNNING, scheduler.GetState());
        assertEquals("SchedulerStatus state is STOPPED.", TaskState.RUNNING, scheduler.GetSchedulerStatus().State);

        LocalDate startDate = projectInfoFile.GetStartDate();
        String testFilePath = Config.getAnInstance("src/test/Scheduler/config.xml").getDownloadDir() + testPluginName+ "\\" + startDate.getYear() + "\\" + startDate.getDayOfYear() +
                "\\3B42RT_daily." + startDate.getYear() + "." + new DecimalFormat("00").format(startDate.getMonthValue()) + "." + new DecimalFormat("00").format(startDate.getDayOfMonth()) + ".bin";
        File temp = new File(testFilePath);
        for(int i=0; !temp.exists() && i < 6; i++)
        {
            Thread.sleep(10000);
        }
        scheduler.AttemptUpdate();
        assertTrue("processorWorkerSuccess", manager.processorWorkerSuccess);
        assertTrue("indicesWorkerSuccess", manager.indicesWorkerSuccess);
        assertTrue("summaryWorkerSuccess", manager.summaryWorkerSuccess);

        scheduler.Stop();
        assertEquals("Scheduler state is RUNNING.", TaskState.STOPPED, scheduler.GetState());
        assertEquals("SchedulerStatus state is RUNNING.", TaskState.STOPPED, scheduler.GetSchedulerStatus().State);
    }

    private class MyScheduler extends Scheduler
    {
        public MyScheduler(SchedulerData data, int myID, EASTWebManagerI manager) throws ParserConfigurationException, SAXException, IOException {
            super(data, myID, manager);
        }

        public MyScheduler(SchedulerData data, int myID, TaskState initState, EASTWebManagerI manager, Config configInstance) throws ParserConfigurationException, SAXException, IOException
        {
            super(data, myID, initState, manager, configInstance);
        }

        @Override protected Process SetupProcessorProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException {
            // Setup directory layout if not existing already
            new File(FileSystem.GetProcessDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetTimeZone(), pluginInfo.GetName(), ProcessName.PROCESSOR)).mkdirs();
            new File(FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetTimeZone(), pluginInfo.GetName(), ProcessName.PROCESSOR)).mkdirs();
            new File(FileSystem.GetProcessWorkerTempDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetTimeZone(), pluginInfo.GetName(), ProcessName.PROCESSOR)).mkdirs();

            Process process = new GenericProcess<ProcessorWorkerTest>(manager, ProcessName.PROCESSOR, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache, "test.Scheduler.ProcessorWorkerTest");
            return process;
        }

        @Override
        protected Process SetupIndicesProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException {
            // Setup directory layout if not existing already
            new File(FileSystem.GetProcessDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetTimeZone(), pluginInfo.GetName(), ProcessName.INDICES)).mkdirs();
            new File(FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetTimeZone(), pluginInfo.GetName(), ProcessName.INDICES)).mkdirs();
            new File(FileSystem.GetProcessWorkerTempDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetTimeZone(), pluginInfo.GetName(), ProcessName.INDICES)).mkdirs();

            Process process = new GenericProcess<IndicesWorkerTest>(manager, ProcessName.INDICES, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache, "test.Scheduler.IndicesWorkerTest");
            return process;
        }

        @Override
        protected Process SetupSummaryProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache) throws ClassNotFoundException {
            // Setup directory layout if not existing already
            new File(FileSystem.GetProcessDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetTimeZone(), pluginInfo.GetName(), ProcessName.SUMMARY)).mkdirs();
            new File(FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetTimeZone(), pluginInfo.GetName(), ProcessName.SUMMARY)).mkdirs();
            new File(FileSystem.GetProcessWorkerTempDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetTimeZone(), pluginInfo.GetName(), ProcessName.SUMMARY)).mkdirs();

            Process process = new GenericProcess<SummaryWorkerTest>(manager, ProcessName.SUMMARY, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, null, "test.Scheduler.SummaryWorkerTest");
            return process;
        }
    }

    private class MyEASTWebManager extends EASTWebManager
    {
        public boolean processorWorkerSuccess;
        public boolean indicesWorkerSuccess;
        public boolean summaryWorkerSuccess;

        public MyEASTWebManager(){

        }

        @Override
        public void NotifyUI(SchedulerStatus updatedStatus) {

        }

        @Override
        public void run() {
        }

        @Override
        public void StartExistingGlobalDownloader(int gdlID) {
        }

        @Override
        public LocalDownloader StartGlobalDownloader(DownloadFactory dlFactory)
        {
            int id = getLowestAvailableGlobalDLID();
            LocalDownloader localDl = null;
            if(IsIDValid(id, globalDLIDs))
            {
                DownloaderFactory factory = null;
                try {
                    factory = dlFactory.CreateDownloadFactory(dlFactory.CreateListDatesFiles());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                GlobalDownloader gdl = factory.CreateGlobalDownloader(id);
                int currentGDLIdx = -1;
                for(int i=0; i < globalDLs.size(); i++)
                {
                    if(globalDLs.get(i).GetPluginName().equals(gdl.GetPluginName()))
                    {
                        currentGDLIdx = i;
                        break;
                    }
                }

                if(currentGDLIdx >= 0)
                {
                    releaseGlobalDLID(id);
                    gdl = globalDLs.get(currentGDLIdx);
                }
                else {
                    localDl = factory.CreateLocalDownloader(gdl);
                    if(globalDLs.size() == 0)
                    {
                        globalDLs.add(id, gdl);
                        //                        globalDLFutures.add(id, globalDLExecutor.scheduleWithFixedDelay(gdl, 0, 1, TimeUnit.DAYS));
                        gdl.run();
                    }
                    else
                    {
                        GlobalDownloader temp = globalDLs.get(id);
                        if(temp == null)
                        {
                            globalDLs.add(id, gdl);
                            //                            globalDLFutures.add(id, globalDLExecutor.scheduleWithFixedDelay(gdl, 0, 1, TimeUnit.DAYS));
                            gdl.run();
                        }
                        else{
                            globalDLs.set(id, gdl);
                            //                            globalDLFutures.set(id, globalDLExecutor.scheduleWithFixedDelay(gdl, 0, 1, TimeUnit.DAYS));
                            gdl.run();
                        }
                    }
                }

                synchronized (numOfCreatedGDLs) {
                    numOfCreatedGDLs = globalDLs.size();
                }

                return localDl;
            }
            else
            {
                return null;
            }
        }

        @Override
        public void StopGlobalDownloader(int gdlID) {
        }

        @Override
        public Future<ProcessWorkerReturn> StartNewProcessWorker(ProcessWorker worker) {
            switch(worker.processWorkerName)
            {
            case "ProcessorWorkerTest": processorWorkerSuccess = true; break;
            case "IndicesWorkerTest": indicesWorkerSuccess = true; break;
            case "SummaryWorkerTest": summaryWorkerSuccess = true; break;
            }

            try {
                worker.call();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }

}
