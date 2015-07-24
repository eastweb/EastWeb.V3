package version2.prototype;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.Scheduler.SchedulerData;
import version2.prototype.Scheduler.SchedulerStatus;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.GenericLocalDownloader;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.GlobalDownloaderFactory;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.LocalDownloader;
import version2.prototype.util.DatabaseCache;

/**
 * Threading management class for EASTWeb. All spawning, executing, and stopping of threads is handled through this class in order for it to manage
 * the number of currently processing threads for each resource group. This class throttles execution where necessary to allow fair processing time
 * to all threads and to manage memory usage across the system. All communication between the presentation layer (GUI) and EASTWeb processes and other
 * are handled by this class as well.
 *
 * @author michael.devos
 *
 */
public class EASTWebManager implements Runnable{
    private static EASTWebManager instance = null;
    private static ExecutorService executor;

    // Logged requests from other threads
    private static List<SchedulerData> newSchedulerRequests;
    private static List<Integer> stopExistingSchedulerRequests;
    private static List<String> stopExistingSchedulerRequestsNames;
    private static List<Integer> deleteExistingSchedulerRequests;
    private static List<String> deleteExistingSchedulerRequestsNames;
    private static List<Integer> startExistingSchedulerRequests;
    private static List<String> startExistingSchedulerRequestsNames;
    private static List<NewGlobalDownloaderRequestsParams> newGlobalDownloaderRequests;
    private static List<Integer> stopGlobalDownloaderRequests;
    private static List<Integer> startExistingGlobalDownloaderRequests;
    private static List<ProcessWorker> newProcessWorkerRequests;
    private static List<GUIUpdateHandler> guiHandlers;

    // EASTWebManager state
    private static Integer numOfCreatedGDLs;
    private static Integer numOfCreatedSchedulers;
    private static List<SchedulerStatus> schedulerStatuses;
    private static BitSet schedulerIDs;
    private static BitSet globalDLIDs;
    private static Boolean schedulerStatesChanged;
    private boolean manualUpdate;
    private boolean justCreateNewSchedulers;
    private final int msBeetweenUpdates;

    // Object references of EASTWeb components
    private List<GlobalDownloader> globalDLs;
    private List<Scheduler> schedulers;
    private List<ScheduledFuture<?>> globalDLFutures;
    private final ScheduledExecutorService globalDLExecutor;
    private final ExecutorService processWorkerExecutor;

    /**
     *  If first time calling, starts up the EASTWebManager background processing thread to handle passively processing logged requests and updating
     *  its state.
     *
     * @param numOfSimultaneousGlobalDLs  - number of threads to create for GlobalDownloader objects to use (recommended value: 1)
     * @param msBeetweenUpdates  - milliseconds to sleep between state updates and requests processing. If 0 then EASTWebManager won't passively update
     *  its state variables and will require UpdateState() to be manually called in whenever it is desired for requests to be processed and state
     *  variables to be updated.
     */
    public static void Start(int numOfSimultaneousGlobalDLs, int msBeetweenUpdates)
    {
        if(instance == null)
        {
            instance = new EASTWebManager(
                    numOfSimultaneousGlobalDLs,  // Number of Global Downloaders allowed to be simultaneously active
                    ((Runtime.getRuntime().availableProcessors() < 4) ?
                            1 : (Runtime.getRuntime().availableProcessors() - 3)), // Number of ProcessWorkers allowed to be simultaneously active
                            msBeetweenUpdates
                    );

            // If passive updating desired
            if(msBeetweenUpdates > 0)
            {
                executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable target) {
                        final Thread thread = new Thread(target);
                        //                log.debug("Creating new worker thread");
                        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread t, Throwable e) {
                                //                        log.error("Uncaught Exception", e);
                            }
                        });
                        return thread;
                    }
                });
                executor.execute(instance);
            }
        }
    }

    @Override
    public void run() {
        do
        {
            try
            {
                Thread.sleep(msBeetweenUpdates);

                // Tell Schedulers to attempt updating their projects
                for(Scheduler scheduler : schedulers)
                {
                    scheduler.AttemptUpdate();
                }

                // Handle new Scheduler requests
                if(newSchedulerRequests.size() > 0)
                {
                    synchronized (newSchedulerRequests) {
                        while(newSchedulerRequests.size() > 0)
                        {
                            handleNewSchedulerRequests(newSchedulerRequests.remove(0));
                        }
                    }
                }

                if(!justCreateNewSchedulers)
                {
                    // Handle stop scheduler requests
                    if(stopExistingSchedulerRequests.size() > 0)
                    {
                        synchronized (stopExistingSchedulerRequests) {
                            while(stopExistingSchedulerRequests.size() > 0)
                            {
                                handleStopSchedulerRequests(stopExistingSchedulerRequests.remove(0));
                            }
                        }
                    }
                    if(stopExistingSchedulerRequestsNames.size() > 0)
                    {
                        synchronized (stopExistingSchedulerRequestsNames) {
                            int schedulerId = -1;
                            for(String projectName : stopExistingSchedulerRequestsNames)
                            {
                                for(Scheduler scheduler : schedulers)
                                {
                                    if(scheduler.projectInfoFile.GetProjectName().equals(projectName))
                                    {
                                        schedulerId = scheduler.GetID();
                                        break;
                                    }
                                }
                                if(schedulerId != -1) {
                                    stopExistingSchedulerRequestsNames.remove(projectName);
                                    handleStopSchedulerRequests(schedulerId);
                                }
                            }
                        }
                    }

                    // Handle delete scheduler requests
                    if(deleteExistingSchedulerRequests.size() > 0)
                    {
                        synchronized (deleteExistingSchedulerRequests) {
                            while(deleteExistingSchedulerRequests.size() > 0)
                            {
                                handleDeleteSchedulerRequests(deleteExistingSchedulerRequests.remove(0));
                            }
                        }
                    }
                    if(deleteExistingSchedulerRequestsNames.size() > 0)
                    {
                        synchronized (deleteExistingSchedulerRequestsNames) {
                            int schedulerId = -1;
                            for(String projectName : deleteExistingSchedulerRequestsNames)
                            {
                                for(Scheduler scheduler : schedulers)
                                {
                                    if(scheduler.projectInfoFile.GetProjectName().equals(projectName))
                                    {
                                        schedulerId = scheduler.GetID();
                                        break;
                                    }
                                }
                                if(schedulerId != -1) {
                                    deleteExistingSchedulerRequestsNames.remove(projectName);
                                    handleDeleteSchedulerRequests(schedulerId);
                                }
                            }
                        }
                    }

                    // Handle start back up existing Scheduler requests
                    if(startExistingSchedulerRequests.size() > 0)
                    {
                        synchronized (startExistingSchedulerRequests) {
                            while(startExistingSchedulerRequests.size() > 0)
                            {
                                handleStartExistingSchedulerRequests(startExistingSchedulerRequests.remove(0));
                            }
                        }
                    }
                    if(startExistingSchedulerRequestsNames.size() > 0)
                    {
                        synchronized (startExistingSchedulerRequestsNames) {
                            int schedulerId = -1;
                            for(String projectName : startExistingSchedulerRequestsNames)
                            {
                                for(Scheduler scheduler : schedulers)
                                {
                                    if(scheduler.projectInfoFile.GetProjectName().equals(projectName))
                                    {
                                        schedulerId = scheduler.GetID();
                                        break;
                                    }
                                }
                                if(schedulerId != -1) {
                                    startExistingSchedulerRequestsNames.remove(projectName);
                                    handleStartExistingSchedulerRequests(schedulerId);
                                }
                                schedulerId = -1;
                            }
                        }
                    }

                    // Handle starting up new GlobalDownload requests requests
                    if(newGlobalDownloaderRequests.size() > 0)
                    {
                        synchronized (newGlobalDownloaderRequests) {
                            while(newGlobalDownloaderRequests.size() > 0)
                            {
                                handleNewGlobalDownloaderRequests(newGlobalDownloaderRequests.remove(0));
                            }
                        }
                    }

                    // Handle starting existing GlobalDownloader requests
                    if(startExistingGlobalDownloaderRequests.size() > 0)
                    {
                        synchronized (startExistingGlobalDownloaderRequests) {
                            while(startExistingGlobalDownloaderRequests.size() > 0)
                            {
                                handleStartExistingGlobalDownloaderRequests(startExistingGlobalDownloaderRequests.remove(0));
                            }
                        }
                    }

                    // Handle stopping GlobalDownloader requests
                    if(stopGlobalDownloaderRequests.size() > 0)
                    {
                        synchronized (stopGlobalDownloaderRequests) {
                            while(stopGlobalDownloaderRequests.size() > 0)
                            {
                                handleStopGlobalDownloaderRequests(stopGlobalDownloaderRequests.remove(0));
                            }
                        }
                    }

                    // Handle make new ProcessWorker requests
                    if(newProcessWorkerRequests.size() > 0)
                    {
                        synchronized (newProcessWorkerRequests) {
                            while(newProcessWorkerRequests.size() > 0)
                            {
                                handleNewProcessWorkerRequests(newProcessWorkerRequests.remove(0));
                            }
                        }
                    }

                    // Handle stopping GlobalDownloaders whose using projects are all stopped.
                    // Handle deleting GlobalDownloaders that don't have any currently existing projects using them.
                    if(globalDLs.size() > 0)
                    {
                        synchronized (globalDLs)
                        {
                            boolean allStopped;
                            boolean noneExisting;

                            if(schedulers.size() > 0)
                            {
                                synchronized (schedulers)
                                {
                                    for(GlobalDownloader gdl : globalDLs)
                                    {
                                        allStopped = true;
                                        noneExisting = true;

                                        for(Scheduler scheduler : schedulers)
                                        {
                                            for(ProjectInfoPlugin pluginInfo : scheduler.projectInfoFile.GetPlugins())
                                            {
                                                if(pluginInfo.GetName().equals(gdl.GetPluginName()))
                                                {
                                                    noneExisting = false;
                                                    if(scheduler.GetState() == TaskState.RUNNING) {
                                                        allStopped = false;
                                                    }
                                                    break;
                                                }
                                            }

                                            if(!noneExisting && !allStopped) {
                                                break;
                                            }
                                        }

                                        if(noneExisting)
                                        {
                                            gdl.Stop();
                                            globalDLs.remove(gdl.GetID());
                                            releaseGlobalDLID(gdl.GetID());
                                            globalDLFutures.get(gdl.GetID()).cancel(false);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                for(GlobalDownloader gdl : globalDLs)
                                {
                                    gdl.Stop();
                                    globalDLs.remove(gdl.GetID());
                                    releaseGlobalDLID(gdl.GetID());
                                    globalDLFutures.get(gdl.GetID()).cancel(false);
                                }
                            }

                            synchronized (numOfCreatedGDLs) {
                                numOfCreatedGDLs = globalDLs.size();
                            }
                        }
                    }

                    if(schedulerStatesChanged)
                    {
                        synchronized (schedulerStatesChanged)
                        {
                            runGUIUpdateHandlers();
                            schedulerStatesChanged = false;
                        }
                    }
                }
            }
            catch (InterruptedException | ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }while((msBeetweenUpdates > 0) && !manualUpdate);
        manualUpdate = false;
        justCreateNewSchedulers = false;
    }

    /**
     * If {@link Start Start(int, int)} has been previously called then forces EASTWebManager to process any and all currently logged requests and
     * update state information. Note, that if Start(int, int) was called with value of 0 or less for msBeetweenUpdates then this method MUST be called
     * in order to process any of the requests made to EASTWebManager via its public methods and in order for it to show updated state information.
     */
    public static void UpdateState()
    {
        if(EASTWebManager.instance != null) {
            EASTWebManager.instance.manualUpdate = true;
            EASTWebManager.instance.run();
        }
    }

    /**
     * Requests for a new {@link version2.Scheduler#Scheduler Scheduler} to be started and keeps its reference for later status retrieval and stopping.
     * The created Scheduler can be identified via its {@link version2.Scheduler#SchedulerStatus SchedulerStatus} object gotten from calling
     * {@link GetSchedulerStatus GetSchedulerStatus()}.
     *
     * @param data - {@link version2.Scheduler#SchedulerData SchedulerData} to create the Scheduler instance from
     * @param manualUpdate  - forces an immediate update to start the new scheduler before returning.
     */
    public static void StartNewScheduler(SchedulerData data, boolean manualUpdate)
    {
        synchronized (newSchedulerRequests) {
            newSchedulerRequests.add(data);
        }
        if(manualUpdate)
        {
            if(EASTWebManager.instance != null) {
                EASTWebManager.instance.justCreateNewSchedulers = true;
            }
            UpdateState();
        }
    }

    /**
     * Requests for the {@link version2.Scheduler#Scheduler Scheduler} with the specified unique schedulerID to be stopped. Sets the
     * {@link version2#TaskState TaskState} value for that Scheduler to STOPPED effectively stopping all associated Process objects. Causes a graceful
     * shutdown of a project since it only keeps Processes from spawning more ProcessWorkers.
     *
     * @param schedulerID  - targeted Scheduler's ID
     */
    public static void StopExistingScheduler(int schedulerID)
    {
        if(schedulerIDs.get(schedulerID))
        {
            synchronized (stopExistingSchedulerRequests) {
                stopExistingSchedulerRequests.add(schedulerID);
            }
        }
    }

    /**
     * Requests for the {@link version2.Scheduler#Scheduler Scheduler} with the specified unique schedulerID to be stopped. Sets the
     * {@link version2#TaskState TaskState} value for that Scheduler to STOPPED effectively stopping all associated Process objects. Causes a graceful
     * shutdown of a project since it only keeps Processes from spawning more ProcessWorkers.
     *
     * @param projectName  - targeted Scheduler's project name
     */
    public static void StopExistingScheduler(String projectName)
    {
        synchronized (stopExistingSchedulerRequestsNames) {
            stopExistingSchedulerRequestsNames.add(projectName);
        }
    }

    /**
     * Requests for the {@link version2.Scheduler#Scheduler Scheduler} with the specified unique schedulerID to be deleted from the EASTWebManager's list
     * and stopped. This does what {@link StopScheduler StopScheduler(int)} does with the added effect of removing it all references to the Scheduler
     * from this manager. This may or may not remove any GlobalDownloaders currently existing. A
     * {@link version2.download#GlobalDownloader GlobalDownloader} is only removed when it no longer has any projects currently using it
     * (GlobalDownloader objects are shared amongst Schedulers).
     *
     * @param schedulerID  - targeted Scheduler's ID
     */
    public static void DeleteScheduler(int schedulerID)
    {
        if(schedulerIDs.get(schedulerID))
        {
            synchronized (deleteExistingSchedulerRequests) {
                deleteExistingSchedulerRequests.add(schedulerID);
            }
        }
    }

    /**
     * Requests for the {@link version2.Scheduler#Scheduler Scheduler} with the specified project name to be deleted from the EASTWebManager's list
     * and stopped. This does what {@link StopScheduler StopScheduler(int)} does with the added effect of removing it all references to the Scheduler
     * from this manager. This may or may not remove any GlobalDownloaders currently existing. A
     * {@link version2.download#GlobalDownloader GlobalDownloader} is only removed when it no longer has any projects currently using it
     * (GlobalDownloader objects are shared amongst Schedulers).
     *
     * @param projectName  - targeted Scheduler's project name
     */
    public static void DeleteScheduler(String projectName)
    {
        synchronized (deleteExistingSchedulerRequestsNames) {
            deleteExistingSchedulerRequestsNames.add(projectName);
        }
    }

    /**
     * Requests for the {@link version2.Scheduler#Scheduler Scheduler} with the specified unique schedulerID to have its
     * {@link version2#TaskState TaskState} value set to RUNNING. Starts a currently stopped Scheduler picking up where it stopped at according to the
     * cache information in the database.
     *
     * @param schedulerID  - targeted Scheduler's ID
     */
    public static void StartExistingScheduler(int schedulerID)
    {
        if(schedulerIDs.get(schedulerID))
        {
            synchronized (startExistingSchedulerRequests) {
                startExistingSchedulerRequests.add(schedulerID);
            }
        }
    }

    /**
     * Requests for the {@link version2.Scheduler#Scheduler Scheduler} with the specified specified project name to have its
     * {@link version2#TaskState TaskState} value set to RUNNING. Starts a currently stopped Scheduler picking up where it stopped at according to the
     * cache information in the database.
     *
     * @param projectName  - targeted Scheduler's project name
     */
    public static void StartExistingScheduler(String projectName)
    {
        synchronized (startExistingSchedulerRequestsNames) {
            startExistingSchedulerRequestsNames.add(projectName);
        }
    }

    /**
     * Requests that a {@link version2.download#GlobalDownloader GlobalDownloader} of the given type to be started up. GlobalDownloader objects are
     * singletons and are shared amongst Schedulers. If the GlobalDownloader associated pluginName is not currently associated with an existing
     * GlobalDownloader then the given one is stored and added to the running list of them.
     *
     * @param gdl  - {@link version2.download#GlobalDownloader GlobalDownloader} object to manage and run
     */
    public static void StartGlobalDownloader(GlobalDownloaderFactory dlFactory)
    {
        synchronized (newGlobalDownloaderRequests) {
            newGlobalDownloaderRequests.add(new EASTWebManager().new NewGlobalDownloaderRequestsParams(dlFactory));
        }
    }

    /**
     * Requests that a {@link version2.download#GlobalDownloader GlobalDownloader} with the specified unique ID to have its
     * {@link version2#TaskState TaskState} set to STOPPED and to cease execution. If other GlobalDownloaders are waiting to execute then the next
     * oldest one is started up.
     *
     * @param gdlID  - targeted GlobalDownloader's ID
     */
    public static void StopGlobalDownloader(int gdlID)
    {
        if(globalDLIDs.get(gdlID))
        {
            synchronized (stopGlobalDownloaderRequests) {
                stopGlobalDownloaderRequests.add(gdlID);
            }
        }
    }

    /**
     * Requests that a {@link version2.download#GlobalDownloader GlobalDownloader} with the specified unique ID to have its
     * {@link version2#TaskState TaskState} set to RUNNING and to continue downloading new data files when its given a turn to run again.
     *
     * @param gdlID  - targeted GlobalDownloader's ID
     */
    public static void StartExistingGlobalDownloader(int gdlID)
    {
        if(globalDLIDs.get(gdlID))
        {
            synchronized (startExistingGlobalDownloaderRequests) {
                startExistingGlobalDownloaderRequests.add(gdlID);
            }
        }
    }

    /**
     * Requests that the given {@link version2#ProcesWorker ProcesWorker} be managed and executed.
     *
     * @param worker  - {@link version2#ProcesWorker ProcesWorker} to execute on a separate available thread
     */
    public static void StartNewProcessWorker(ProcessWorker worker)
    {
        synchronized (newProcessWorkerRequests) {
            newProcessWorkerRequests.add(worker);
        }
    }

    /**
     * Gets the number of {@link version2.download#GlobalDownloader GlobalDownloader} objects currently created.
     *
     * @return number of {@link version2.download#GlobalDownloader GlobalDownloader} instances stored
     */
    public static int GetNumberOfGlobalDownloaders()
    {
        int num;
        synchronized (numOfCreatedGDLs) {
            num = numOfCreatedGDLs;
        }
        return num;
    }

    /**
     * Gets the number of {@link version2.Scheduler#Scheduler Scheduler} objects currently created.
     *
     * @return number of {@link version2.Scheduler#Scheduler Scheduler} instances stored
     */
    public static int GetNumberOfSchedulerResources()
    {
        int num;
        synchronized (numOfCreatedSchedulers) {
            num = numOfCreatedSchedulers;
        }
        return num;
    }

    /**
     * Returns the list of {@link version2.Scheduler#SchedulerStatus SchedulerStatus} objects relevant to all currently known active
     * {@link version2.Scheduler#Scheduler Scheduler} instances. This information is updated passively by EASTWebManager's background thread if
     * {@link Start Start(int, int)} was called with a value greater than 0 for msBeetweenUpdates, otherwise it is updated actively by calling
     * {@link UpdateStatus UpdateStatus()}.
     *
     * @return list of SchedulerStatus objects for the currently created Scheduler instances
     */
    public static ArrayList<SchedulerStatus> GetSchedulerStatuses()
    {
        ArrayList<SchedulerStatus> output = new ArrayList<SchedulerStatus>(0);
        synchronized (schedulerStatuses)
        {
            for(SchedulerStatus status : schedulerStatuses) {
                output.add(status);
            }
        }
        return output;
    }

    /**
     * Gets the {@link version2.Scheduler#SchedulerStatus SchedulerStatus} currently known for a {@link version2.Scheduler#Scheduler Scheduler} with the
     * given unique ID, if it exists. If not, then returns null.
     *
     * @param schedulerID  - unique ID of the target {@link version2.Scheduler#Scheduler Scheduler} instance
     * @return the currently known {@link version2.Scheduler#SchedulerStatus SchedulerStatus} for the target Scheduler if found, otherwise null returned.
     */
    public static SchedulerStatus GetSchedulerStatus(int schedulerID)
    {
        SchedulerStatus status = null;

        synchronized (schedulerStatuses)
        {
            for(SchedulerStatus aStatus : schedulerStatuses)
            {
                if(aStatus.schedulerID == schedulerID) {
                    status = aStatus;
                    break;
                }
            }
        }

        return status;
    }

    /**
     * Gets the {@link version2.Scheduler#SchedulerStatus SchedulerStatus} currently known for a {@link version2.Scheduler#Scheduler Scheduler} with the
     * given unique ID, if it exists. If not, then returns null.
     *
     * @param schedulerID  - unique ID of the target {@link version2.Scheduler#Scheduler Scheduler} instance
     * @return the currently known {@link version2.Scheduler#SchedulerStatus SchedulerStatus} for the target Scheduler if found, otherwise null returned.
     */
    public static SchedulerStatus GetSchedulerStatus(String projectName)
    {
        SchedulerStatus status = null;

        synchronized (schedulerStatuses)
        {
            for(SchedulerStatus aStatus : schedulerStatuses)
            {
                if(aStatus.projectName.equals(projectName)) {
                    status = aStatus;
                    break;
                }
            }
        }

        return status;
    }

    /**
     * Adds a {@link version2#GUIUpdateHandler GUIUpdateHandler} object to the list of registered GUIUpdateHandlers that the EASTWebManager's background
     * thread will run once an update is detected to one of the {@link version2.Scheduler#SchedulerStatus SchedulerStatus} objects. The thread runs as
     * often as specified when calling the {@link Start Start(int, int)} method.
     *
     * @param handler
     */
    public static void RegisterGUIUpdateHandler(GUIUpdateHandler handler)
    {
        synchronized (guiHandlers)
        {
            guiHandlers.add(handler);
        }
    }

    /**
     * Removes a specified {@link version2#GUIUpdateHandler GUIUpdateHandler} instance from the registered handler list.
     *
     * @param handler  - {@link version2#GUIUpdateHandler GUIUpdateHandler} to unregister
     */
    public static void RemoveGUIUpdateHandler(GUIUpdateHandler handler)
    {
        synchronized (guiHandlers)
        {
            guiHandlers.remove(handler);
        }
    }

    /**
     * Updates the local SchedulerStatus listing and sets a boolean flag to signal that the UI needs to be updated.
     *
     * @param updatedScheduler  - reference to the Scheduler instance for whom the stored SchedulerStatus object is now out of date
     */
    public static void NotifyUI(Scheduler updatedScheduler)
    {
        synchronized (schedulerStatesChanged)
        {
            schedulerStatesChanged = true;

            synchronized (schedulerStatuses)
            {
                for(int i=0; i < schedulerStatuses.size(); i++)
                {
                    if(schedulerStatuses.get(i).schedulerID == updatedScheduler.GetID())
                    {
                        schedulerStatuses.set(i, updatedScheduler.GetSchedulerStatus());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Creates a LocalDownloader for requesting Scheduler to use. LocalDownloaders are only needed for GlobalDownloaders created to download the main "Data" files not the extra downloads.
     *
     * @param projectInfoFile  - parsed project information to initialize LocalDownloader with
     * @param pluginInfo  - ProjectInfoPlugin object describing the plugin to use listed from within the project metadata file
     * @param pluginMetaData  - the PluginMetaData object parsed from the plugin metadata file associated with the given ProjectInfoPlugin object
     * @param scheduler  - the requesting Scheduler instance
     * @param outputCache  - the cache the LocalDownloader is to use for its output
     * @return A concrete LocalDownloader object
     */
    public LocalDownloader getLocalDownloader(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler, DatabaseCache outputCache)
    {
        LocalDownloader localDl = null;

        synchronized (globalDLs)
        {
            for(int i=0; i < globalDLs.size(); i++)
            {
                if(globalDLs.get(i).GetPluginName().equals(pluginInfo.GetName()))
                {
                    localDl = new GenericLocalDownloader(globalDLs.get(i).GetID(), projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
                    globalDLs.get(i).addObserver(localDl);
                    break;
                }
            }
        }
        return localDl;
    }

    /**
     * Empty instance just to allow usage of private classes.
     */
    private EASTWebManager()
    {
        manualUpdate = false;
        justCreateNewSchedulers = false;
        msBeetweenUpdates = Integer.MAX_VALUE;
        schedulerStatesChanged = false;

        newSchedulerRequests = null;
        stopExistingSchedulerRequests = null;
        stopExistingSchedulerRequestsNames = null;
        deleteExistingSchedulerRequests = null;
        deleteExistingSchedulerRequestsNames = null;
        startExistingSchedulerRequests = null;
        startExistingSchedulerRequestsNames = null;
        newGlobalDownloaderRequests = null;
        stopGlobalDownloaderRequests = null;
        startExistingGlobalDownloaderRequests = null;
        newProcessWorkerRequests = null;
        guiHandlers = null;

        // Setup for handling executing GlobalDownloaders
        globalDLs = null;
        globalDLExecutor = null;
        numOfCreatedGDLs = -1;
        globalDLIDs = null;
        globalDLFutures = null;

        // Setup for handling executing Schedulers
        schedulers = null;
        schedulerStatuses = null;
        numOfCreatedSchedulers = -10;
        schedulerIDs = null;

        // Setup for handling executing ProcessWorkers
        processWorkerExecutor = null;
    }

    private EASTWebManager(int numOfGlobalDLResourses, int numOfProcessWorkerResourses, int msBeetweenUpdates)
    {
        manualUpdate = false;
        justCreateNewSchedulers = false;
        this.msBeetweenUpdates = msBeetweenUpdates;
        schedulerStatesChanged = false;

        ThreadFactory gDLFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable target) {
                final Thread thread = new Thread(target);
                //                log.debug("Creating new worker thread");
                thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        //                        log.error("Uncaught Exception", e);
                    }
                });
                return thread;
            }
        };
        ThreadFactory pwFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable target) {
                final Thread thread = new Thread(target);
                //                log.debug("Creating new worker thread");
                thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        //                        log.error("Uncaught Exception", e);
                    }
                });
                return thread;
            }
        };

        // Setup request lists
        newSchedulerRequests = Collections.synchronizedList(new ArrayList<SchedulerData>(1));
        stopExistingSchedulerRequests = Collections.synchronizedList(new ArrayList<Integer>(0));
        stopExistingSchedulerRequestsNames = Collections.synchronizedList(new ArrayList<String>(0));
        deleteExistingSchedulerRequests = Collections.synchronizedList(new ArrayList<Integer>(0));
        deleteExistingSchedulerRequestsNames = Collections.synchronizedList(new ArrayList<String>(0));
        startExistingSchedulerRequests = Collections.synchronizedList(new ArrayList<Integer>(0));
        startExistingSchedulerRequestsNames = Collections.synchronizedList(new ArrayList<String>(0));
        newGlobalDownloaderRequests = Collections.synchronizedList(new ArrayList<NewGlobalDownloaderRequestsParams>(1));
        stopGlobalDownloaderRequests = Collections.synchronizedList(new ArrayList<Integer>(0));
        startExistingGlobalDownloaderRequests = Collections.synchronizedList(new ArrayList<Integer>(0));
        newProcessWorkerRequests = Collections.synchronizedList(new ArrayList<ProcessWorker>(10));
        guiHandlers = Collections.synchronizedList(new ArrayList<GUIUpdateHandler>(0));

        // Setup for handling executing GlobalDownloaders
        globalDLs = Collections.synchronizedList(new ArrayList<GlobalDownloader>(1));
        globalDLExecutor = Executors.newScheduledThreadPool(numOfGlobalDLResourses, gDLFactory);
        numOfCreatedGDLs = 0;
        globalDLIDs = new BitSet(1000);
        globalDLFutures = Collections.synchronizedList(new ArrayList<ScheduledFuture<?>>(1));

        // Setup for handling executing Schedulers
        schedulers = Collections.synchronizedList(new ArrayList<Scheduler>(1));
        schedulerStatuses = new ArrayList<SchedulerStatus>(1);
        numOfCreatedSchedulers = 0;
        schedulerIDs = new BitSet(100000);

        // Setup for handling executing ProcessWorkers
        processWorkerExecutor = Executors.newFixedThreadPool(numOfProcessWorkerResourses, pwFactory);
    }

    private void handleNewSchedulerRequests(SchedulerData data) throws ParserConfigurationException, SAXException, IOException
    {
        synchronized (schedulers)
        {
            int id = getLowestAvailableSchedulerID();
            if(IsIDValid(id, schedulerIDs))
            {
                Scheduler scheduler = new Scheduler(data, id, this);
                schedulers.set(id, scheduler);
                scheduler.Start();

                synchronized (numOfCreatedSchedulers) {
                    numOfCreatedSchedulers = schedulers.size();
                }
            }
            else
            {
                newSchedulerRequests.add(data);
            }
        }
    }

    private void handleStopSchedulerRequests(int schedulerID)
    {
        synchronized (schedulers)
        {
            if(schedulers.size() > schedulerID)
            {
                schedulers.get(schedulerID).Stop();
            }
        }
    }

    private void handleDeleteSchedulerRequests(int schedulerID)
    {
        synchronized (schedulers)
        {
            if(schedulers.size() > schedulerID)
            {
                schedulers.remove(schedulerID);
                releaseSchedulerID(schedulerID);

                synchronized (numOfCreatedSchedulers) {
                    numOfCreatedSchedulers = schedulers.size();
                }
            }
        }
    }

    private void handleStartExistingSchedulerRequests(int schedulerID)
    {
        synchronized (schedulers)
        {
            if(schedulers.size() > schedulerID)
            {
                schedulers.get(schedulerID).Start();
            }
        }
    }

    private void handleNewGlobalDownloaderRequests(NewGlobalDownloaderRequestsParams gdlParams)
    {
        synchronized (globalDLs)
        {
            int id = getLowestAvailableGlobalDLID();
            if(IsIDValid(id, globalDLIDs))
            {
                GlobalDownloader gdl = gdlParams.dlFactory.createGlobalDownloader(id);
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
                }
                else {
                    globalDLs.set(id, gdl);
                    globalDLFutures.set(id, globalDLExecutor.scheduleWithFixedDelay(gdl, 0, 1, TimeUnit.DAYS));
                }

                synchronized (numOfCreatedGDLs) {
                    numOfCreatedGDLs = globalDLs.size();
                }
            }
            else
            {
                newGlobalDownloaderRequests.add(gdlParams);
            }
        }
    }

    private void handleStopGlobalDownloaderRequests(int gdlID)
    {
        synchronized (globalDLs)
        {
            if(globalDLs.size() > gdlID)
            {
                globalDLs.get(gdlID).Stop();
            }
        }
    }

    private void handleStartExistingGlobalDownloaderRequests(int gdlID)
    {
        synchronized (globalDLs)
        {
            if(globalDLs.size() > gdlID)
            {
                globalDLs.get(gdlID).Start();
            }
        }
    }

    private void handleNewProcessWorkerRequests(ProcessWorker worker)
    {
        processWorkerExecutor.submit(worker);
    }

    private void runGUIUpdateHandlers()
    {
        synchronized (guiHandlers)
        {
            for(GUIUpdateHandler handler : guiHandlers)
            {
                handler.run();
            }
        }
    }

    private int getLowestAvailableSchedulerID()
    {
        int id = -1;

        synchronized (schedulerIDs)
        {
            id = schedulerIDs.nextClearBit(0);

            if(IsIDValid(id, schedulerIDs))
            {
                schedulerIDs.set(id);
            }
            else
            {
                id = -1;
            }
        }

        return id;
    }

    private void releaseSchedulerID(int id)
    {
        synchronized (schedulerIDs)
        {
            if(IsIDValid(id, schedulerIDs))
            {
                schedulerIDs.clear(id);
            }
        }
    }

    private int getLowestAvailableGlobalDLID()
    {
        int id = -1;

        synchronized (globalDLIDs)
        {
            id = globalDLIDs.nextClearBit(0);

            if(IsIDValid(id, globalDLIDs))
            {
                globalDLIDs.set(id);
            }
            else
            {
                id = -1;
            }
        }

        return id;
    }

    private void releaseGlobalDLID(int id)
    {
        synchronized (globalDLIDs)
        {
            if(IsIDValid(id, globalDLIDs))
            {
                globalDLIDs.clear(id);
            }
        }
    }

    private boolean IsIDValid(int id, BitSet set)
    {
        if((id >= 0) && (id < set.size())) {
            return true;
        } else {
            return false;
        }
    }

    private class NewGlobalDownloaderRequestsParams {
        public final GlobalDownloaderFactory dlFactory;

        public NewGlobalDownloaderRequestsParams(GlobalDownloaderFactory dlFactory)
        {
            this.dlFactory = dlFactory;
        }
    }
}
