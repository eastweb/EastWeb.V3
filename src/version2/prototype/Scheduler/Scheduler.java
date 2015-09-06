package version2.prototype.Scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.EASTWebManagerI;
import version2.prototype.ErrorLog;
import version2.prototype.GenericProcess;
import version2.prototype.Process;
import version2.prototype.TaskState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.LocalDownloader;
import version2.prototype.indices.IndicesWorker;
import version2.prototype.processor.ProcessorWorker;
import version2.prototype.summary.Summary;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.DatabaseConnection;
import version2.prototype.util.FileSystem;
import version2.prototype.util.GeneralUIEventObject;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.ProgressUpdater;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class Scheduler {
    /**
     * SchedulerData object this Scheduler was created with.
     */
    public final SchedulerData data;
    /**
     * ProjectInfoFile object gotten from SchedulerData during Scheduler creation.
     */
    public final ProjectInfoFile projectInfoFile;
    /**
     * PluginMetaDataCollection object gotten from SchedulerData during Scheduler creation.
     */
    public final PluginMetaDataCollection pluginMetaDataCollection;

    private final int ID;
    private final Config configInstance;
    private final EASTWebManagerI manager;
    protected SchedulerStatusContainer statusContainer;
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
     * @param configInstance
     */
    public Scheduler(SchedulerData data, int myID, EASTWebManagerI manager, Config configInstance)
    {
        this(data, myID, TaskState.STOPPED, manager, configInstance, new ProgressUpdater(configInstance, data.projectInfoFile, data.pluginMetaDataCollection));
    }

    /**
     * Creates and sets up a Scheduler instance with the given project data. Sets the Scheduler's running state to the given TaskState.
     * Call Start() eventually, if initState is TaskState.STOPPED, to start the project processing.
     *
     * @param data  - SchedulerData describing the project to setup for
     * @param myID  - a unique ID for this Scheduler instance
     * @param initState  - Initial TaskState to set this Scheduler to.
     * @param manager  - reference to the EASTWebManager creating this Scheduler
     * @param configInstance
     * @param progressUpdater
     */
    public Scheduler(SchedulerData data, int myID, TaskState initState, EASTWebManagerI manager, Config configInstance, ProgressUpdater progressUpdater)
    {
        this.data = data;
        projectInfoFile = data.projectInfoFile;
        pluginMetaDataCollection = data.pluginMetaDataCollection;

        statusContainer = new SchedulerStatusContainer(configInstance, myID, progressUpdater, projectInfoFile, pluginMetaDataCollection, initState);
        localDownloaders = new ArrayList<LocalDownloader>(1);
        processorProcesses = new ArrayList<Process>(1);
        indicesProcesses = new ArrayList<Process>(1);
        summaryProcesses = new ArrayList<Process>(1);
        downloadCaches = new ArrayList<DatabaseCache>(1);
        processorCaches = new ArrayList<DatabaseCache>(1);
        indicesCaches = new ArrayList<DatabaseCache>(1);
        ID = myID;
        this.configInstance = configInstance;
        this.manager = manager;

        // Make sure directory layout is set up
        File temp;
        try{
            temp = new File(FileSystem.GetRootDirectoryPath(projectInfoFile));
            temp.mkdirs();
            if(!temp.exists()) {
                throw new FileNotFoundException("Directory structure \"" + temp.getCanonicalPath() + "\" doesn't exist");
            }
            temp = new File(FileSystem.GetProjectDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName()));
            temp.mkdirs();
            if(!temp.exists()) {
                throw new FileNotFoundException("Directory structure \"" + temp.getCanonicalPath() + "\" doesn't exist");
            }
        } catch(NullPointerException | SecurityException | IOException e) {
            ErrorLog.add("Problem while setting up directories.", e);
        } catch(Exception e){
            ErrorLog.add("Problem while setting up directories.", e);
        }

        PluginMetaData pluginMetaData;
        DatabaseConnection con = null;
        try {
            // Setup project
            con = DatabaseConnector.getConnection();
            for(ProjectInfoPlugin item: data.projectInfoFile.GetPlugins())
            {
                pluginMetaData = pluginMetaDataCollection.pluginMetaDataMap.get(item.GetName());
                new File(FileSystem.GetGlobalDownloadDirectory(configInstance, item.GetName())).mkdirs();

                Schemas.CreateProjectPluginSchema(con, configInstance.getGlobalSchema(), projectInfoFile, item.GetName(), configInstance.getSummaryCalculations(), pluginMetaData.DaysPerInputData,
                        pluginMetaData.Download.filesPerDay, item.GetIndices().size(), true);
                SetupProcesses(item, pluginMetaData);

                // Update status in EASTWebManager
                SchedulerStatus status = null;
                synchronized (statusContainer)
                {
                    status = statusContainer.GetStatus();
                }
                if(status != null) {
                    manager.NotifyUI(status);
                }
            }
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | ParseException | ParserConfigurationException | SAXException | IOException e) {
            ErrorLog.add(this, "Problem setting up Scheduler.", e);
        } catch (Exception e) {
            ErrorLog.add(this, "Problem setting up Scheduler.", e);
        }
        finally {
            if(con != null) {
                con.close();
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
        SchedulerStatus status = null;
        synchronized (statusContainer)
        {
            try {
                status = statusContainer.GetStatus();
            } catch (SQLException e) {
                ErrorLog.add(this, "Problem while getting SchedulerStatus instance.", e);
            }
        }
        return status;
    }

    /**
     * Updates the Scheduler's {@link TaskState TaskState} to RUNNING notifying all observers of said state of the change.
     *
     * @author michael.devos
     */
    public void Start()
    {
        synchronized (statusContainer)
        {
            statusContainer.UpdateSchedulerTaskState(TaskState.RUNNING);
        }
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
        synchronized (statusContainer)
        {
            statusContainer.UpdateSchedulerTaskState(TaskState.STOPPED);
        }
    }

    /**
     * Gets this scheduler's thread state.
     *
     * @author michael.devos
     * @return scheduler's current thread state
     */
    public TaskState GetState()
    {
        synchronized (statusContainer)
        {
            return statusContainer.GetState();
        }
    }

    /**
     * Used by the executed frameworks ({@link Process Process} objects) to send information up to the GUI.
     *
     * @param event  - GUI update information
     */
    public void NotifyUI(GeneralUIEventObject event)
    {
        SchedulerStatus status = null;
        synchronized (statusContainer)
        {
            if(event.getStatus() != null) {
                statusContainer.AddToLog(event.getStatus());
            }

            try {
                status = statusContainer.GetStatus();
            } catch (SQLException e) {
                ErrorLog.add(this, "Problem while getting SchedulerStatus instance.", e);
            }
        }
        if(status != null) {
            manager.NotifyUI(status);
        }
    }

    /**
     * Updates the download progress for the given plugin and data name.
     * @param dataName  - the name of the file type being downloaded (e.g. "Data" or "Qc")
     * @param pluginName  - the plugin title gotten from the plugin metadata to calculate progress in relation to
     * @param listDatesFiles  - reference to the ListDatesFiles object to use
     * @param modisTileNames  - list of modis tiles included
     * @param stmt  - Statement object to reuse
     * @throws SQLException
     */
    public synchronized void UpdateDownloadProgressByData(String dataName, String pluginName, ListDatesFiles listDatesFiles, ArrayList<String> modisTileNames, Statement stmt) throws SQLException
    {
        synchronized (statusContainer)
        {
            statusContainer.UpdateDownloadProgressByData(dataName, pluginName, listDatesFiles, modisTileNames, stmt);
        }
    }

    /**
     * Updates the processor progress for the given plugin.
     * @param pluginName  - name of plugin whose processor progress to change
     * @param stmt  - Statement object to reuse
     * @throws SQLException
     */
    public synchronized void UpdateProcessorProgress(String pluginName, Statement stmt) throws SQLException
    {
        synchronized (statusContainer)
        {
            statusContainer.UpdateProcessorProgress(pluginName, stmt);
        }
    }

    /**
     * Updates the indices progress for the given plugin.
     * @param pluginName  - name of plugin whose processor progress to change
     * @param stmt  - Statement object to reuse
     * @throws SQLException
     */
    public synchronized void UpdateIndicesProgress(String pluginName, Statement stmt) throws SQLException
    {
        synchronized (statusContainer)
        {
            statusContainer.UpdateIndicesProgress(pluginName, stmt);
        }
    }

    /**
     * Updates the summary progress for the summary attributed to the given ID for the named plugin.
     * @param summaryIDNum  - ID attribute value to calculate progress for gotten from project metadata
     * @param compStrategy  - TemporalSummaryCompositionStrategy object to use in calculating total expecting results in temporal summary cases
     * @param daysPerInputData  - number of days each input file represents
     * @param pluginInfo  - reference to a ProjectInfoPlugin object to use
     * @param stmt  - Statement object to reuse
     * @throws SQLException
     */
    public synchronized void UpdateSummaryProgress(int summaryIDNum, TemporalSummaryCompositionStrategy compStrategy, int daysPerInputData, ProjectInfoPlugin pluginInfo, Statement stmt)
            throws SQLException
    {
        synchronized (statusContainer)
        {
            statusContainer.UpdateSummaryProgress(summaryIDNum, compStrategy, daysPerInputData, pluginInfo, stmt);
        }
    }

    /**
     * Checks for new work from associated GlobalDownloaders using the stored LocalDownloaders which gets the number of available files to process from each of them and updates their local caches to
     * start processing the new files.
     *
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
        Start();

        SchedulerStatus status = null;
        synchronized (statusContainer)
        {
            status = statusContainer.GetStatus();
        }

        if(status != null) {
            manager.NotifyUI(status);
        }
    }

    protected Scheduler(ProjectInfoFile projectInfoFile, PluginMetaDataCollection pluginMetaDataCollection, int myID, Config configInstance, EASTWebManagerI manager)
    {
        data = new SchedulerData(projectInfoFile, pluginMetaDataCollection);
        this.projectInfoFile = projectInfoFile;
        this.pluginMetaDataCollection = pluginMetaDataCollection;
        ID = myID;
        this.configInstance = configInstance;
        this.manager = manager;
    }

    protected Scheduler(ProjectInfoFile projectInfoFile, PluginMetaDataCollection pluginMetaDataCollection, int myID, Config configInstance, EASTWebManagerI manager, SchedulerStatusContainer statusContainer)
    {
        data = new SchedulerData(projectInfoFile, pluginMetaDataCollection);
        this.projectInfoFile = projectInfoFile;
        this.pluginMetaDataCollection = pluginMetaDataCollection;
        ID = myID;
        this.configInstance = configInstance;
        this.manager = manager;
        this.statusContainer = statusContainer;
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
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    protected void SetupProcesses(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException,
    IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException, IOException, ParserConfigurationException, SAXException
    {
        //Scheduler scheduler, String globalSchema, String projectName, String pluginName, String workingDir, ProcessName processCachingFor, ArrayList<String> extraDownloadFiles
        DatabaseCache downloadCache = new DatabaseCache(this, configInstance.getGlobalSchema(), data.projectInfoFile.GetProjectName(), pluginInfo, pluginMetaData, data.projectInfoFile.GetWorkingDir(),
                ProcessName.DOWNLOAD);
        DatabaseCache processorCache = new DatabaseCache(this, configInstance.getGlobalSchema(), data.projectInfoFile.GetProjectName(), pluginInfo, pluginMetaData, data.projectInfoFile.GetWorkingDir(),
                ProcessName.PROCESSOR);
        DatabaseCache indicesCache = new DatabaseCache(this, configInstance.getGlobalSchema(), data.projectInfoFile.GetProjectName(), pluginInfo, pluginMetaData, data.projectInfoFile.GetWorkingDir(),
                ProcessName.INDICES);
        DatabaseCache outputCache = new DatabaseCache(this, configInstance.getGlobalSchema(), data.projectInfoFile.GetProjectName(), pluginInfo, pluginMetaData, data.projectInfoFile.GetWorkingDir(),
                ProcessName.SUMMARY);

        localDownloaders.addAll(SetupDownloadProcess(pluginInfo, pluginMetaData, downloadCache));
        processorProcesses.add(SetupProcessorProcess(pluginInfo, pluginMetaData, downloadCache, processorCache));
        indicesProcesses.add(SetupIndicesProcess(pluginInfo, pluginMetaData, processorCache, indicesCache));
        summaryProcesses.add(SetupSummaryProcess(pluginInfo, pluginMetaData, indicesCache, outputCache));

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
    protected ArrayList<LocalDownloader> SetupDownloadProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache outputCache) throws ClassNotFoundException, NoSuchMethodException,
    SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, IOException
    {
        ArrayList<LocalDownloader> lDownloders = new ArrayList<LocalDownloader>();

        // Create "data" DownloadFactory
        Class<?> downloadFactoryClass = Class.forName("version2.prototype.download." + pluginInfo.GetName() + "." + pluginMetaData.Download.downloadFactoryClassName);
        Constructor<?> downloadFactoryCtor = downloadFactoryClass.getConstructor(EASTWebManagerI.class, Config.class, ProjectInfoFile.class, ProjectInfoPlugin.class, PluginMetaData.class, Scheduler.class,
                DatabaseCache.class, LocalDate.class, DownloadMetaData.class);
        DownloadFactory downloadFactory = (DownloadFactory) downloadFactoryCtor.newInstance(manager, configInstance, projectInfoFile, pluginInfo, pluginMetaData, this, outputCache,
                projectInfoFile.GetStartDate(), pluginMetaData.Download);

        // Create "data" GenericGlobalDownloader
        lDownloders.add(manager.StartGlobalDownloader(downloadFactory));

        for(DownloadMetaData dlMetaData : pluginMetaData.Download.extraDownloads)
        {
            // Create extra ListDatesFiles instance
            downloadFactoryClass = Class.forName("version2.prototype.download." + pluginInfo.GetName() + "." + dlMetaData.downloadFactoryClassName);
            downloadFactoryCtor = downloadFactoryClass.getConstructor(EASTWebManagerI.class, Config.class, ProjectInfoFile.class, ProjectInfoPlugin.class, PluginMetaData.class, Scheduler.class,
                    DatabaseCache.class, LocalDate.class, DownloadMetaData.class);
            downloadFactory = (DownloadFactory) downloadFactoryCtor.newInstance(manager, configInstance, projectInfoFile, pluginInfo, pluginMetaData, this, outputCache,
                    projectInfoFile.GetStartDate(), dlMetaData);

            // Create extra GenericGlobalDownloader
            lDownloders.add(manager.StartGlobalDownloader(downloadFactory));
        }

        return lDownloders;
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
    protected Process SetupProcessorProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException {

        // If desired, GenericFrameworkProcess can be replaced with a custom Process extending class.
        Process process = new GenericProcess<ProcessorWorker>(manager, configInstance, ProcessName.PROCESSOR, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache,
                "version2.prototype.processor.ProcessorWorker");
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
    protected Process SetupIndicesProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException {
        // If desired, GenericFrameworkProcess can be replaced with a custom Process extending class.
        Process process = new GenericProcess<IndicesWorker>(manager, configInstance, ProcessName.INDICES, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache,
                "version2.prototype.indices.IndicesWorker");
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
    protected Process SetupSummaryProcess(ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException
    {
        //        Summary process = new Summary(manager, configInstance, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache);
        Process process = new GenericProcess<IndicesWorker>(manager, configInstance, ProcessName.SUMMARY, projectInfoFile, pluginInfo, pluginMetaData, this, inputCache, outputCache,
                "version2.prototype.summary.SummaryWorker");
        //        inputCache.addObserver(process);
        return process;
    }
}

