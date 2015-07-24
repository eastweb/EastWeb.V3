package version2.prototype.Scheduler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.DataDate;
import version2.prototype.EASTWebManager;
import version2.prototype.GenericProcess;
import version2.prototype.Process;
import version2.prototype.TaskState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.GenericGlobalDownloader;
import version2.prototype.download.GenericGlobalDownloaderFactory;
import version2.prototype.download.GenericLocalDownloader;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.LocalDownloader;
import version2.prototype.indices.IndicesWorker;
import version2.prototype.processor.ProcessorWorker;
import version2.prototype.summary.Summary;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GeneralListener;
import version2.prototype.util.GeneralUIEventObject;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

public class Scheduler {
    public final SchedulerData data;
    public final ProjectInfoFile projectInfoFile;
    public final PluginMetaDataCollection pluginMetaDataCollection;

    private final int ID;
    private final EASTWebManager manager;
    private SchedulerStatus status;
    private TaskState mState;
    private ArrayList<LocalDownloader> localDownloaders;
    private ArrayList<Process> processorProcesses;
    private ArrayList<Process> indicesProcesses;
    private ArrayList<Process> summaryProcesses;
    private ArrayList<DatabaseCache> downloadCaches;
    private ArrayList<DatabaseCache> processorCaches;
    private ArrayList<DatabaseCache> indicesCaches;

    /**
     * Creates and sets up a Scheduler instance with the given project data. Does not start the Scheduler and Processes.
     * To start processing call start().
     *
     * @param data  - SchedulerData describing the project to setup for
     * @param myID  - a unique ID for this Scheduler instance
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Scheduler(SchedulerData data, int myID, EASTWebManager manager) throws ParserConfigurationException, SAXException, IOException
    {
        this(data, myID, TaskState.STOPPED, manager);
    }

    /**
     * Creates and sets up a Scheduler instance with the given project data. Sets the Scheduler's running state to the given TaskState.
     * Call Start() eventually, if initState is TaskState.STOPPED, to start the project processing.
     *
     * @param data  - SchedulerData describing the project to setup for
     * @param myID  - a unique ID for this Scheduler instance
     * @param initState  - Initial TaskState to set this Scheduler to. Process ob
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public Scheduler(SchedulerData data, int myID, TaskState initState, EASTWebManager manager) throws ParserConfigurationException, SAXException, IOException
    {
        this.data = data;
        projectInfoFile = data.projectInfoFile;
        pluginMetaDataCollection = data.pluginMetaDataCollection;

        status = new SchedulerStatus(myID, projectInfoFile.GetProjectName(), data.projectInfoFile.GetPlugins(), data.projectInfoFile.GetSummaries(), initState);
        mState = initState;
        localDownloaders = new ArrayList<LocalDownloader>(1);
        processorProcesses = new ArrayList<Process>(1);
        indicesProcesses = new ArrayList<Process>(1);
        summaryProcesses = new ArrayList<Process>(1);
        downloadCaches = new ArrayList<DatabaseCache>(1);
        processorCaches = new ArrayList<DatabaseCache>(1);
        indicesCaches = new ArrayList<DatabaseCache>(1);

        ID = myID;
        this.manager = manager;
        PluginMetaData pluginMetaData;
        for(ProjectInfoPlugin item: data.projectInfoFile.GetPlugins())
        {
            try
            {
                pluginMetaData = pluginMetaDataCollection.pluginMetaDataMap.get(item.GetName());
                // Total Input Units = (((#_of_days_since_start_date / #_of_days_in_a_single_input_unit) * #_of_days_to_interpolate_out) / #_of_days_in_temporal_composite)
                Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), Config.getInstance().getGlobalSchema(), projectInfoFile.GetProjectName(), item.GetName(), Config.getInstance().getSummaryCalculations(),
                        pluginMetaData.ExtraDownloadFiles, projectInfoFile.GetStartDate(), pluginMetaData.DaysPerInputData, item.GetIndicies().size(), projectInfoFile.GetSummaries(), true);
                SetupProcesses(item);
            }
            catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException | ParseException | ConfigReadException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns this Scheduler's ID number that acts similar to a system process ID.
     *
     * @author michael.devos
     * @return unique ID number
     */
    public int GetID() { return ID; }

    /**
     * Returns the current status of this Scheduler.
     *
     * @author michael.devos
     * @return SchedulerStatus - represents status of this Scheduler instance at that point in time
     */
    public SchedulerStatus GetSchedulerStatus()
    {
        synchronized (status)
        {
            return status;
        }
    }

    /**
     * Updates the Scheduler's {@link TaskState TaskState} to RUNNING notifying all observers of said state of the change.
     *
     * @author michael.devos
     */
    public void Start()
    {
        mState = TaskState.RUNNING;
        for(DatabaseCache cache : downloadCaches)
        {
            cache.NotifyObserversToCheckForPastUpdates();
        }
        for(DatabaseCache cache : processorCaches)
        {
            cache.NotifyObserversToCheckForPastUpdates();
        }
        for(DatabaseCache cache : indicesCaches)
        {
            cache.NotifyObserversToCheckForPastUpdates();
        }
    }

    /**
     * Updates the Scheduler's {@link TaskState TaskState} to STOPPED notifying all observers of said state of the change.
     *
     * @author michael.devos
     *
     */
    public void Stop()
    {
        mState = TaskState.STOPPED;
    }

    /**
     * Gets this scheduler's thread state.
     *
     * @author michael.devos
     * @return scheduler's current thread state
     */
    public TaskState GetState()
    {
        return mState;
    }

    /**
     * Used by the executed frameworks ({@link Process Process} objects) to send information up to the GUI.
     *
     * @param e  - GUI update information
     */
    public void NotifyUI(GeneralUIEventObject e)
    {
        Process process = (Process)e.getSource();

        synchronized (status)
        {
            switch(process.processName)
            {
            case DOWNLOAD:
                status.UpdateDownloadProgress(e.getProgress(), e.getPluginName());
                break;
            case PROCESSOR:
                status.UpdateProcessorProgress(e.getProgress(), e.getPluginName());
                break;
            case INDICES:
                status.UpdateIndicesProgress(e.getProgress(), e.getPluginName());
                break;
            default:    // SUMMARY
                status.UpdateSummaryProgress(e.getProgress(), e.getPluginName());
                break;
            }

            status.AddToLog(e.getStatus());
        }

        EASTWebManager.NotifyUI(this);
    }

    /**
     * Checks for new work from associated GlobalDownloaders using the stored LocalDownloaders which gets the number of available files to process from each of them and updates their local caches to
     * start processing the new files.
     *
     * @return String - the plugin name, Integer - number of new files to process for that plugin
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void AttemptUpdate() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        TreeMap<String, TreeMap<Integer, Integer>> results = new TreeMap<String, TreeMap<Integer, Integer>>();
        for(LocalDownloader dl : localDownloaders)
        {
            results.put(dl.pluginInfo.GetName(), dl.AttemptUpdate());
        }
        status.UpdateUpdatesBeingProcessed(results);
    }

    /**
     * Sets up a set of Process extending classes to act as ProcesWorker Managers for each of the four frameworks for each ProjectInfoPlugin given.
     *
     * @author michael.devos
     *
     * @param pluginInfo  - plugin information
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParseException
     * @throws IOException
     */
    private void SetupProcesses(ProjectInfoPlugin pluginInfo) throws NoSuchMethodException, SecurityException, ClassNotFoundException,
    InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, IOException
    {
        PluginMetaData plMeta = pluginMetaDataCollection.pluginMetaDataMap.get(pluginInfo.GetName());
        DatabaseCache downloadCache = new DatabaseCache(data.projectInfoFile.GetProjectName(),  pluginInfo.GetName(), ProcessName.DOWNLOAD);
        DatabaseCache processorCache = new DatabaseCache(data.projectInfoFile.GetProjectName(),  pluginInfo.GetName(), ProcessName.PROCESSOR);
        DatabaseCache indicesCache = new DatabaseCache(data.projectInfoFile.GetProjectName(),  pluginInfo.GetName(), ProcessName.INDICES);

        localDownloaders.add(SetupDownloadProcess(pluginInfo, plMeta, downloadCache));
        processorProcesses.add(SetupProcessorProcess(pluginInfo, plMeta, downloadCache, processorCache));
        indicesProcesses.add(SetupIndicesProcess(pluginInfo, plMeta, processorCache, indicesCache));
        summaryProcesses.add(SetupSummaryProcess(pluginInfo, plMeta, indicesCache));

        downloadCaches.add(downloadCache);
        processorCaches.add(processorCache);
        indicesCaches.add(indicesCache);
    }

    /**
     * Sets up a {@link GenericFrameworkProcess GenericFrameworkProcess} object to manage DownloadWorkers.
     *
     * @author michael.devos
     *
     * @param pluginInfo  - plugin information
     * @param pluginMetaData  - plugin information gotten from a PluginMetaData.*.xml
     * @param outputCache  - DatabaseCache object used to cache output files from a LocaldDownloader
     * @return general concrete Process object for managing ProcessWorkers
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    private LocalDownloader SetupDownloadProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache outputCache) throws ClassNotFoundException, NoSuchMethodException,
    SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, IOException
    {
        // Create "data" ListDatesFiles instance
        Class<?> listDatesFilesClass = Class.forName("version2.prototype.download" + pluginInfo.GetName() + pluginMetaData.Download.listDatesFilesClassName);
        Constructor<?> listDatesFilesCtor = listDatesFilesClass.getConstructor(DataDate.class, DownloadMetaData.class);
        ListDatesFiles listDatesFiles = (ListDatesFiles) listDatesFilesCtor.newInstance(new DataDate(pluginMetaData.Download.originDate), pluginMetaData.Download);

        // Create "data" GenericGlobalDownloader
        EASTWebManager.StartGlobalDownloader(new GenericGlobalDownloaderFactory(pluginInfo.GetName(), pluginMetaData.Download, listDatesFiles, pluginMetaData.Download.downloaderClassName));

        for(DownloadMetaData dlMetaData : pluginMetaData.Download.extraDownloads)
        {
            // Create extra ListDatesFiles instance
            listDatesFilesClass = Class.forName("version2.prototype.download" + pluginInfo.GetName() + pluginMetaData.Download.listDatesFilesClassName);
            listDatesFilesCtor = listDatesFilesClass.getConstructor(DataDate.class, DownloadMetaData.class);
            listDatesFiles = (ListDatesFiles) listDatesFilesCtor.newInstance(new DataDate(pluginMetaData.Download.originDate), pluginMetaData.Download);

            // Create extra GenericGlobalDownloader
            EASTWebManager.StartGlobalDownloader(new GenericGlobalDownloaderFactory(pluginInfo.GetName(), pluginMetaData.Download, listDatesFiles, dlMetaData.downloaderClassName));
        }

        return manager.getLocalDownloader(projectInfoFile, pluginInfo, pluginMetaData, this, outputCache);
    }

    /**
     * Sets up a {@link GenericFrameworkProcess GenericFrameworkProcess} object to manage ProcessorWorkers.
     *
     * @author michael.devos
     *
     * @param pluginInfo  - plugin information
     * @param pluginMetaData  - plugin information gotten from a PluginMetaData.*.xml
     * @param inputCache  - DatabaseCache object used to acquire files available for processor processing
     * @param outputCache  - DatabaseCache object used to cache output files from processor processing
     * @return general concrete Process object for managing ProcessWorkers
     * @throws ClassNotFoundException
     */
    private Process SetupProcessorProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache,
            DatabaseCache outputCache) throws ClassNotFoundException {
        // If desired, GenericFrameworkProcess can be replaced with a custom Process extending class.
        Process process = new GenericProcess<ProcessorWorker>(ProcessName.PROCESSOR, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache);
        //        inputCache.addObserver(process);
        return process;
    }

    /**
     * Sets up a {@link GenericFrameworkProcess GenericFrameworkProcess} object to manage IndicesWorkers.
     *
     * @author michael.devos
     *
     * @param pluginInfo  - plugin information
     * @param pluginMetaData  - plugin information gotten from a PluginMetaData.*.xml
     * @param inputCache  - DatabacheCache object used to acquire files available for indices processing
     * @param outputCache  - DatabaseCache object used to cache output files from indices processing
     * @return general concrete Process object for managing ProcessWorkers
     * @throws ClassNotFoundException
     */
    private Process SetupIndicesProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache,
            DatabaseCache outputCache) throws ClassNotFoundException {
        // If desired, GenericFrameworkProcess can be replaced with a custom Process extending class.
        Process process = new GenericProcess<IndicesWorker>(ProcessName.INDICES, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache);
        //        inputCache.addObserver(process);
        return process;
    }

    /**
     * Sets up a {@link version2.prototype.summary.Summary Summary} object to manage SummaryWorkers.
     *
     * @author michael.devos
     *
     * @param pluginInfo  - plugin information
     * @param pluginMetaData  - plugin information gotten from a PluginMetaData.*.xml
     * @return {@link Summary Summary}  - the manager object of SummaryWorkers for the current project and given plugin
     * @param inputCache  - the DatabaseCache object used to acquire files available for summary processing
     */
    private Summary SetupSummaryProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache)
    {
        Summary process = new Summary(projectInfoFile, pluginInfo, pluginMetaData, this, inputCache);
        //        inputCache.addObserver(process);
        return process;
    }

    /**
     * @deprecated
     *
     * @param plugin
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    @Deprecated
    public void RunDownloader(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        // uses reflection
        Class<?> clazzDownloader = Class.forName("version2.prototype.download."
                + pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Title
                + pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Download.downloaderClassName);
        Constructor<?> ctorDownloader = clazzDownloader.getConstructor(DataDate.class, DownloadMetaData.class, GeneralListener.class);
        Object downloader =  ctorDownloader.newInstance(new Object[] {
                data.projectInfoFile.GetStartDate(),
                pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Download});
        Method methodDownloader = downloader.getClass().getMethod("run");
        methodDownloader.invoke(downloader);

        //        DownloadProgress = 100;
        //        log.add("Download Finish");
    }

    /**
     * @deprecated
     *
     * @param plugin
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    @Deprecated
    public void RunProcess(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        //        ProcessorMetaData temp = pluginMetaDataCollection.pluginMetaDataMap.get(plugin.GetName()).Projection;
        // TODO: revise the "date"
        //        PrepareProcessTask prepareProcessTask;
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
        //        ProcessorProgress = 100;
        //        log.add("Process Finish");
    }

    /**
     * @deprecated
     *
     * @param plugin
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    @Deprecated
    public void RunIndicies(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
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
                            indicie});
            Method methodIndicies = indexCalculator.getClass().getMethod("calculate");
            methodIndicies.invoke(indexCalculator);
        }
        //        IndiciesProgress = 100;
        //        log.add("Indicies Finish");
    }
}

