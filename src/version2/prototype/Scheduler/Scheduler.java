package version2.prototype.Scheduler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
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
                RunDownloader(item);
                RunProcess(item);
                RunIndicies(item);
                //RunSummary(item);
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
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void RunProcesses() throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException,
    IllegalArgumentException, InvocationTargetException
    {
        for(ProjectInfoPlugin pluginInfo: data.projectInfoFile.GetPlugins())
        {
            PluginMetaData plMeta = pluginMetaDataCollection.pluginMetaDataMap.get(pluginInfo.GetName());
            futures.add(executor.submit(SetupProcess(ProcessName.DOWNLOAD, pluginInfo, plMeta, null)));
            futures.add(executor.submit(SetupProcess(ProcessName.PROCESSOR, pluginInfo, plMeta, ProcessName.DOWNLOAD)));
            futures.add(executor.submit(SetupProcess(ProcessName.INDICES, pluginInfo, plMeta, ProcessName.PROCESSOR)));
            futures.add(executor.submit(SetupProcess(ProcessName.SUMMARY, pluginInfo, plMeta, ProcessName.INDICES)));
        }
    }

    public Process<?> SetupProcess(ProcessName processName, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, ProcessName previousProcess) throws NoSuchMethodException,
    SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Class<?> classProcess;
        String inputTableName;

        switch(processName)
        {
        case DOWNLOAD:
            classProcess = Class.forName(String.format("version2.prototype.download.Download"));
            break;
        case INDICES:
            classProcess = Class.forName(String.format("version2.prototype.indices.Indices"));
            break;
        case PROCESSOR:
            classProcess = Class.forName(String.format("version2.prototype.processor.Processor"));
            break;
        default:    // SUMMARY
            classProcess = Class.forName(String.format("version2.prototype.summary.Summary"));
            break;
        }

        switch(previousProcess)
        {
        case DOWNLOAD:
            inputTableName = "Download";
            break;
        case PROCESSOR:
            inputTableName = "Processor";
            break;
        case INDICES:
            inputTableName = "Indices";
            break;
        default:    // SUMMARY
            inputTableName = null;
            break;
        }

        Constructor<?> ctorProcess = classProcess.getConstructor(ProjectInfoFile.class, ProjectInfoPlugin.class, PluginMetaData.class,
                Scheduler.class, ThreadState.class, ProcessName.class, String.class, ExecutorService.class);
        Process<?> process = (Process<?>)ctorProcess.newInstance(projectInfoFile, pluginInfo, pluginMetaData, this, ThreadState.RUNNING,
                processName, inputTableName, executor);
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
            Class<?> clazzIndicies;
            try{
                clazzIndicies = Class.forName(String.format("version2.prototype.indices.%S.%S", plugin.GetName(), indicie));
            }catch(Exception e){
                try{
                    clazzIndicies = Class.forName(String.format("version2.prototype.indices.%S", indicie));
                }catch(Exception ex){
                    throw new EmptyStackException(); // class not found
                }
            }
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
        switch(processName)
        {
        case DOWNLOAD:
            DownloadProgress =e.getProgress();
            break;
        case PROCESSOR:
            ProcessProgress = e.getProgress();
            break;
        case INDICES:
            IndiciesProgress = e.getProgress();
            break;
        default:    // SUMMARY
            SummaryProgress = e.getProgress();
            break;
        }
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

    class downloaderListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            DownloadProgress = e.getProgress();
            Log.add(e.getStatus());
        }
    }

    class processListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            ProcessProgress = e.getProgress();
            Log.add(e.getStatus());
        }
    }

    class indiciesListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            IndiciesProgress = e.getProgress();
            Log.add(e.getStatus());
        }
    }

    class summaryListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            SummaryProgress = e.getProgress();
            Log.add(e.getStatus());
        }
    }
}

