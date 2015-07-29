package version2.prototype.Scheduler;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
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
import version2.prototype.download.LocalDownloader;
import version2.prototype.indices.IndicesWorker;
import version2.prototype.processor.ProcessorWorker;
import version2.prototype.summary.Summary;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GeneralUIEventObject;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
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
     * @param manager  - reference to the EASTWebManager creating this Scheduler
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
     * @param initState  - Initial TaskState to set this Scheduler to.
     * @param manager  - reference to the EASTWebManager creating this Scheduler
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
                        pluginMetaData.ExtraDownloadFiles, projectInfoFile.GetStartDate(), pluginMetaData.DaysPerInputData, pluginMetaData.Download.filesPerDay, item.GetIndicies().size(), projectInfoFile.GetSummaries(), true);
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

        manager.NotifyUI(this);
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
        DatabaseCache downloadCache = new DatabaseCache(data.projectInfoFile.GetProjectName(), pluginInfo.GetName(), ProcessName.DOWNLOAD, plMeta.ExtraDownloadFiles);
        DatabaseCache processorCache = new DatabaseCache(data.projectInfoFile.GetProjectName(), pluginInfo.GetName(), ProcessName.PROCESSOR, plMeta.ExtraDownloadFiles);
        DatabaseCache indicesCache = new DatabaseCache(data.projectInfoFile.GetProjectName(), pluginInfo.GetName(), ProcessName.INDICES, plMeta.ExtraDownloadFiles);

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
        // Create "data" DownloadFactory
        Class<?> downloadFactoryClass = Class.forName("version2.prototype.download" + pluginInfo.GetName() + pluginMetaData.Download.downloadFactoryClassName);
        Constructor<?> downloadFactoryCtor = downloadFactoryClass.getConstructor(EASTWebManager.class, ProjectInfoFile.class, ProjectInfoPlugin.class, PluginMetaData.class, Scheduler.class,
                DatabaseCache.class, DataDate.class, DownloadMetaData.class);
        DownloadFactory downloadFactory = (DownloadFactory) downloadFactoryCtor.newInstance(manager, projectInfoFile, pluginInfo, pluginMetaData, this, outputCache,
                new DataDate(pluginMetaData.Download.originDate), pluginMetaData.Download);

        // Create "data" GenericGlobalDownloader
        LocalDownloader lDownloder = manager.StartGlobalDownloader(downloadFactory, true);

        for(DownloadMetaData dlMetaData : pluginMetaData.Download.extraDownloads)
        {
            // Create extra ListDatesFiles instance
            downloadFactoryClass = Class.forName("version2.prototype.download" + pluginInfo.GetName() + dlMetaData.downloadFactoryClassName);
            downloadFactoryCtor = downloadFactoryClass.getConstructor(EASTWebManager.class, ProjectInfoFile.class, ProjectInfoPlugin.class, PluginMetaData.class, Scheduler.class,
                    DatabaseCache.class, DataDate.class);
            downloadFactory = (DownloadFactory) downloadFactoryCtor.newInstance(manager, projectInfoFile, pluginInfo, pluginMetaData, this, outputCache,
                    new DataDate(pluginMetaData.Download.originDate), dlMetaData);

            // Create extra GenericGlobalDownloader
            manager.StartGlobalDownloader(downloadFactory, false);
        }

        return lDownloder;
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
    private Process SetupProcessorProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException {
        // If desired, GenericFrameworkProcess can be replaced with a custom Process extending class.
        Process process = new GenericProcess<ProcessorWorker>(manager, ProcessName.PROCESSOR, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache);
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
    private Process SetupIndicesProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException {
        // If desired, GenericFrameworkProcess can be replaced with a custom Process extending class.
        Process process = new GenericProcess<IndicesWorker>(manager, ProcessName.INDICES, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache);
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
        Summary process = new Summary(manager, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache);
        //        inputCache.addObserver(process);
        return process;
    }
}