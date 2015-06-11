package version2.prototype.Scheduler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.DataDate;
import version2.prototype.Process;
import version2.prototype.ThreadState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.ProcessMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.download.Download;
import version2.prototype.indices.Indices;
import version2.prototype.processor.Processor;
import version2.prototype.projection.PrepareProcessTask;
import version2.prototype.projection.ProcessData;
import version2.prototype.summary.temporal.AvgGdalRasterFileMerge;
import version2.prototype.summary.Summary;
import version2.prototype.summary.SummaryData;
import version2.prototype.summary.temporal.TemporalSummaryCalculator;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.zonal.ZonalSummaryCalculator;
import version2.prototype.util.GeneralListener;
import version2.prototype.util.GeneralUIEventObject;

public class Scheduler implements Runnable {

    public int DownloadProgress;
    public int ProcessProgress;
    public int IndiciesProgress;
    public int SummaryProgress;
    public ArrayList<String> Log;

    public SchedulerData data;
    public ProjectInfoFile projectInfoFile;
    public PluginMetaDataCollection pluginMetaDataCollection;

    private SchedulerState mState;
    private ArrayList<Future<?>> futures;
    private ExecutorService executor;

    public Scheduler(SchedulerData data, ExecutorService executor)
    {
        DownloadProgress = 0;
        ProcessProgress = 0;
        IndiciesProgress = 0;
        SummaryProgress = 0;
        Log = new ArrayList<String>();

        this.data = data;
        projectInfoFile = data.projectInfoFile;
        pluginMetaDataCollection = data.pluginMetaDataCollection;
        mState = new SchedulerState();
        this.executor = executor;
    }

    @Override
    public void run()
    {
        for(ProjectInfoPlugin item: data.projectInfoFile.GetPlugins())
        {
            try {
                RunProcesses(item);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void RunProcesses(ProjectInfoPlugin pluginInfo) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException,
    IllegalArgumentException, InvocationTargetException
    {
        PluginMetaData plMeta = pluginMetaDataCollection.pluginMetaDataMap.get(pluginInfo.GetName());
        futures.add(executor.submit(SetupDownloadProcess(pluginInfo, plMeta)));
        futures.add(executor.submit(SetupProcessorProcess(pluginInfo, plMeta)));
        futures.add(executor.submit(SetupIndicesProcess(pluginInfo, plMeta)));
        futures.add(executor.submit(SetupSummaryProcess(pluginInfo, plMeta)));
    }

    private Process<Void> SetupDownloadProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData)
    {
        Process<Void> process = new Download(projectInfoFile, pluginInfo, pluginMetaData, this, ThreadState.RUNNING, ProcessName.DOWNLOAD, null, executor);
        mState.addObserver(process);
        return process;
    }

    private Process<Void> SetupProcessorProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData)
    {
        Process<Void> process = new Processor(projectInfoFile, pluginInfo, pluginMetaData, this, ThreadState.RUNNING, ProcessName.DOWNLOAD, null, executor);
        mState.addObserver(process);
        return process;
    }

    private Process<Void> SetupIndicesProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData)
    {
        Process<Void> process = new Indices(projectInfoFile, pluginInfo, pluginMetaData, this, ThreadState.RUNNING, ProcessName.DOWNLOAD, null, executor);
        mState.addObserver(process);
        return process;
    }

    private Process<Void> SetupSummaryProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData)
    {
        Process<Void> process = new Summary(projectInfoFile, pluginInfo, pluginMetaData, this, ThreadState.RUNNING, ProcessName.DOWNLOAD, null, executor);
        mState.addObserver(process);
        return process;
    }

    public void RunDownloader(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        // uses reflection
        Class<?> clazzDownloader = Class.forName("version2.prototype.download."
                + pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Title
                + pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Download.className);
        Constructor<?> ctorDownloader = clazzDownloader.getConstructor(DataDate.class, DownloadMetaData.class, GeneralListener.class);
        Object downloader =  ctorDownloader.newInstance(new Object[] {
                data.projectInfoFile.GetStartDate(),
                pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Download,
                new downloaderListener()});
        Method methodDownloader = downloader.getClass().getMethod("run");
        methodDownloader.invoke(downloader);

        DownloadProgress = 100;
        Log.add("Download Finish");
    }

    public void RunProcess(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        ProcessMetaData temp = pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Projection;
        // TODO: revise the "date"
        PrepareProcessTask prepareProcessTask;
        // TODO: initiate it with each plugin's implementation
        //prepareProcessTask= new PrepareProcessTask(projectInfoFile, plugin.GetName(), projectInfoFile.startDate, new processListener());

        /* will move to the Projection framework
        for (int i = 1; i <= temp.processStep.size(); i++) {
            if(temp.processStep.get(i) != null && !temp.processStep.get(i).isEmpty())
            {
                Class<?> clazzProcess = Class.forName("version2.prototype.projection."
                        + pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Title
                        + temp.processStep.get(i));
                Constructor<?> ctorProcess = clazzProcess.getConstructor(ProcessData.class);
                Object process =  ctorProcess.newInstance(new Object[] {new ProcessData(
                        prepareProcessTask.getInputFolders(i),
                        prepareProcessTask.getOutputFolder(i),
                        prepareProcessTask.getQC(),
                        prepareProcessTask.getShapeFile(),
                        prepareProcessTask.getMaskFile(),
                        prepareProcessTask.getDataBands(),
                        prepareProcessTask.getQCBands(),
                        prepareProcessTask.getProjection(),
                        prepareProcessTask.getListener())});
                Method methodProcess = process.getClass().getMethod("run");
                methodProcess.invoke(process);
            }
        }
         */
        ProcessProgress = 100;
        Log.add("Process Finish");
    }

    public void RunIndicies(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        for(String indicie: plugin.GetIndicies())
        {
            Class<?> clazzIndicies = Class.forName(String.format("version2.prototype.indices.%S.%S", plugin.GetName(), indicie));
            Constructor<?> ctorIndicies = clazzIndicies.getConstructor(String.class, DataDate.class, String.class, String.class, GeneralListener.class);
            Object indexCalculator =  ctorIndicies.newInstance(
                    new Object[] {
                            plugin.GetName(),
                            data.projectInfoFile.GetStartDate(),
                            new File(indicie).getName().split("\\.")[0],
                            indicie,
                            new indiciesListener()});
            Method methodIndicies = indexCalculator.getClass().getMethod("calculate");
            methodIndicies.invoke(indexCalculator);
        }
        IndiciesProgress = 100;
        Log.add("Indicies Finish");
    }

    public void NotifyUI(GeneralUIEventObject e)
    {
        ProcessName processName = ((Process<?>)e.getSource()).processName;
        Log.add(e.getStatus());
    }

    public void Stop()
    {
        mState.ChangeState(ThreadState.STOPPED);
    }

    public void Start()
    {
        mState.ChangeState(ThreadState.RUNNING);
    }
}

