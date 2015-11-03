package version2.prototype;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.Scheduler.SchedulerData;
import version2.prototype.Scheduler.SchedulerStatus;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.DownloaderFactory;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.LocalDownloader;
import version2.prototype.util.DatabaseConnection;
import version2.prototype.util.DatabaseConnectionPoolA;
import version2.prototype.util.DatabaseConnector;

/**
 * Threading management class for EASTWeb. All spawning, executing, and stopping of threads is handled through this class in order for it to manage
 * the number of currently processing threads for each resource group. This class throttles execution where necessary to allow fair processing time
 * to all threads and to manage memory usage across the system. All communication between the presentation layer (GUI) and EASTWeb processes and other
 * are handled by this class as well.
 *
 * @author michael.devos
 *
 */
public class EASTWebManager implements Runnable, EASTWebManagerI{
    protected static EASTWebManager instance = null;
    protected static ExecutorService executor;
    protected static int defaultNumOfSimultaneousGlobalDLs = 1;
    //    protected static int defaultMSBeetweenUpdates = 300000;       // 5 minutes
    protected static int defaultMSBeetweenUpdates = 5000;       // 5 seconds

    // Logged requests from other threads
    protected static List<SchedulerData> startNewSchedulerRequests;
    protected static List<SchedulerData> loadNewSchedulerRequests;
    protected static List<Integer> stopExistingSchedulerRequests;
    protected static List<String> stopExistingSchedulerRequestsNames;
    protected static List<Integer> deleteExistingSchedulerRequests;
    protected static List<String> deleteExistingSchedulerRequestsNames;
    protected static List<Integer> startExistingSchedulerRequests;
    protected static List<String> startExistingSchedulerRequestsNames;
    protected static HashMap<GUIUpdateHandler, Boolean> guiHandlers = new HashMap<GUIUpdateHandler, Boolean>(0);     // Boolean - TRUE if flagged for removal

    // EASTWebManager state
    public final Config configInstance;
    protected static Integer numOfCreatedGDLs = 0;
    protected static Integer numOfCreatedSchedulers = 0;
    protected static List<SchedulerStatus> schedulerStatuses = new ArrayList<SchedulerStatus>(1);
    protected static BitSet schedulerIDs = new BitSet(100000);
    protected static BitSet globalDLIDs = new BitSet(1000);
    protected static Boolean schedulerStatesChanged = false;
    protected boolean manualUpdate;
    protected boolean justCreateNewSchedulers;
    protected final int msBeetweenUpdates;
    protected final DatabaseConnectionPoolA connectionPool;

    // Object references of EASTWeb components
    protected HashMap<Integer, GlobalDownloader> globalDLs;
    protected HashMap<Integer, Scheduler> schedulers;
    protected HashMap<Integer, ScheduledFuture<?>> globalDLFutures;
    protected final ScheduledExecutorService globalDLExecutor;
    protected final ExecutorService processWorkerExecutor;

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
        try{
            if(instance == null)
            {
                int numOfWorkerThreads = Runtime.getRuntime().availableProcessors() < 4 ?
                        1 : ((Runtime.getRuntime().availableProcessors() - 2 - numOfSimultaneousGlobalDLs) < 0 ?
                                1 : Runtime.getRuntime().availableProcessors() - 2 - numOfSimultaneousGlobalDLs);
                instance = new EASTWebManager(
                        numOfSimultaneousGlobalDLs,  // Number of Global Downloaders allowed to be simultaneously active
                        numOfWorkerThreads, // Number of ProcessWorkers allowed to be simultaneously active
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
                if(instance != null) {
                    System.out.println("EASTWeb started. Threads being used: " + (2 + numOfSimultaneousGlobalDLs + numOfWorkerThreads)
                            + " {GUI threads: 1 reserved, EASTWebManagement threads: 1, "
                            + "GlobalDownload threads: " + numOfSimultaneousGlobalDLs
                            + ", ProcessWorker threads: " + numOfWorkerThreads
                            + ", " + (msBeetweenUpdates > 0 ? "ms between updates: " + msBeetweenUpdates : "no automatic GUI updating") + "}");
                }
            }
        } catch(Exception e)
        {
            ErrorLog.add(instance.configInstance, "Problem while starting EASTWebManager.", e);
        }
    }

    /**
     * Safely handles the closing of EASTWeb and all obtained resources before returning.
     */
    public static void Close()
    {
        ArrayList<Integer> runningSchedulerIDs = new ArrayList<Integer>();

        for (int i = schedulerIDs.nextSetBit(0); i >= 0; i = schedulerIDs.nextSetBit(i+1)) {
            runningSchedulerIDs.add(i);
            if (i == Integer.MAX_VALUE) {
                break; // or (i+1) would overflow
            }
        }

        for(Integer ID : runningSchedulerIDs) {
            StopExistingScheduler(ID, false);
        }

        UpdateState();
        DatabaseConnector.Close();

        //TODO: close executor services
    }

    /**
     * Retrieves the names of the registered TemporalSummaryCompositionStrategy implementing classes available for use in temporal summaries.
     * @return ArrayList of class name strings of the TemporalSummaryCompositionStrategy implementing classes usable for temporal summary calculation
     */
    public static ArrayList<String> GetRegisteredTemporalSummaryCompositionStrategies()
    {
        ArrayList<String> strategyNames = new ArrayList<String>();
        if(instance != null)
        {
            strategyNames = instance.configInstance.getSummaryTempCompStrategies();
        }
        else
        {
            strategyNames = Config.getInstance().getSummaryTempCompStrategies();
        }
        strategyNames.add(0,"(No Selection)");

        return strategyNames;
    }

    /**
     * If {@link #Start Start(int, int)} has been previously called then forces EASTWebManager to process any and all currently logged requests and
     * update state information. Note, that if Start(int, int) was called with value of 0 or less for msBeetweenUpdates then this method MUST be called
     * in order to process any of the requests made to EASTWebManager via its public methods and in order for it to show updated state information.
     */
    public static void UpdateState()
    {
        if(instance != null) {
            instance.manualUpdate = true;
            instance.run();
        }
    }

    /**
     * Requests for a new {@link version2.prototype.Scheduler#Scheduler Scheduler} to be started and keeps its reference for later status retrieval and stopping.
     * The created Scheduler can be identified via its {@link version2.prototype.Scheduler#SchedulerStatus SchedulerStatus} object gotten from calling
     * {@link #GetSchedulerStatus GetSchedulerStatus()}.
     *
     * @param data - {@link version2.prototype.Scheduler#SchedulerData SchedulerData} to create the Scheduler instance from
     * @param forceUpdate  - forces an immediate update to start the new scheduler before returning.
     */
    public static void StartNewScheduler(SchedulerData data, boolean forceUpdate)
    {
        if(instance == null)
        {
            EASTWebManager.Start(defaultNumOfSimultaneousGlobalDLs, defaultMSBeetweenUpdates);
        }

        synchronized (startNewSchedulerRequests) {
            startNewSchedulerRequests.add(data);
        }

        if(forceUpdate)
        {
            if(instance != null) {
                instance.justCreateNewSchedulers = true;
            }
            UpdateState();
        }
    }

    /**
     * Requests for a new {@link version2.prototype.Scheduler#Scheduler Scheduler} to be created but the project not set to RUNNING status and keeps its reference
     * for later status retrieval and stopping. The created Scheduler can be identified via its {@link version2.prototype.Scheduler#SchedulerStatus SchedulerStatus}
     * object gotten from calling {@link #GetSchedulerStatus GetSchedulerStatus()}.
     *
     * @param data - {@link version2.prototype.Scheduler#SchedulerData SchedulerData} to create the Scheduler instance from
     * @param forceUpdate  - forces an immediate update to load the new scheduler before returning.
     */
    public static void LoadNewScheduler(SchedulerData data, boolean forceUpdate)
    {
        if(instance == null)
        {
            EASTWebManager.Start(defaultNumOfSimultaneousGlobalDLs, defaultMSBeetweenUpdates);
        }

        synchronized (loadNewSchedulerRequests) {
            loadNewSchedulerRequests.add(data);
        }

        if(forceUpdate)
        {
            if(instance != null) {
                instance.justCreateNewSchedulers = true;
            }
            UpdateState();
        }
    }

    /**
     * Requests for the {@link version2.prototype.Scheduler#Scheduler Scheduler} with the specified unique schedulerID to be stopped. Sets the
     * {@link version2#TaskState TaskState} value for that Scheduler to STOPPED effectively stopping all associated Process objects. Causes a graceful
     * shutdown of a project since it only keeps Processes from spawning more ProcessWorkers.
     *
     * @param schedulerID  - targeted Scheduler's ID
     * @param forceUpdate  - forces an immediate update to start the new scheduler before returning.
     */
    public static void StopExistingScheduler(int schedulerID, boolean forceUpdate)
    {
        if(schedulerIDs.get(schedulerID))
        {
            synchronized (stopExistingSchedulerRequests) {
                stopExistingSchedulerRequests.add(schedulerID);
            }

            if(forceUpdate)
            {
                UpdateState();
            }
        }
    }

    /**
     * Requests for the {@link version2.prototype.Scheduler#Scheduler Scheduler} with the specified unique schedulerID to be stopped. Sets the
     * {@link version2.prototype.TaskState#TaskState TaskState} value for that Scheduler to STOPPED effectively stopping all associated Process objects. Causes a graceful
     * shutdown of a project since it only keeps Processes from spawning more ProcessWorkers.
     *
     * @param projectName  - targeted Scheduler's project name
     * @param forceUpdate  - forces an immediate update to start the new scheduler before returning.
     */
    public static void StopExistingScheduler(String projectName, boolean forceUpdate)
    {
        synchronized (stopExistingSchedulerRequestsNames) {
            stopExistingSchedulerRequestsNames.add(projectName);

            if(forceUpdate)
            {
                UpdateState();
            }
        }
    }

    /**
     * Requests for the {@link version2.prototype.Scheduler#Scheduler Scheduler} with the specified unique schedulerID to be deleted from the EASTWebManager's list
     * and stopped. This does what {@link #StopExistingScheduler StopScheStopExistingSchedulerduler(int)} does with the added effect of removing it all references to the Scheduler
     * from this manager. This may or may not remove any GlobalDownloaders currently existing. A
     * {@link version2.prototype.download#GlobalDownloader GlobalDownloader} is only removed when it no longer has any projects currently using it
     * (GlobalDownloader objects are shared amongst Schedulers).
     *
     * @param schedulerID  - targeted Scheduler's ID
     * @param forceUpdate  - forces an immediate update to start the new scheduler before returning.
     */
    public static void DeleteScheduler(int schedulerID, boolean forceUpdate)
    {
        if(schedulerIDs.get(schedulerID))
        {
            synchronized (deleteExistingSchedulerRequests) {
                deleteExistingSchedulerRequests.add(schedulerID);

                if(forceUpdate)
                {
                    UpdateState();
                }
            }
        }
    }

    /**
     * Requests for the {@link version2.prototype.Scheduler#Scheduler Scheduler} with the specified project name to be deleted from the EASTWebManager's list
     * and stopped. This does what {@link #StopExistingScheduler StopExistingScheduler(int)} does with the added effect of removing it all references to the Scheduler
     * from this manager. This may or may not remove any GlobalDownloaders currently existing. A
     * {@link version2.prototype.download#GlobalDownloader GlobalDownloader} is only removed when it no longer has any projects currently using it
     * (GlobalDownloader objects are shared amongst Schedulers).
     *
     * @param projectName  - targeted Scheduler's project name
     * @param forceUpdate  - forces an immediate update to start the new scheduler before returning.
     */
    public static void DeleteScheduler(String projectName, boolean forceUpdate)
    {
        synchronized (deleteExistingSchedulerRequestsNames) {
            deleteExistingSchedulerRequestsNames.add(projectName);

            if(forceUpdate)
            {
                UpdateState();
            }
        }
    }

    /**
     * Requests for the {@link version2.prototype.Scheduler#Scheduler Scheduler} with the specified unique schedulerID to have its
     * {@link version2#TaskState TaskState} value set to RUNNING. Starts a currently stopped Scheduler picking up where it stopped at according to the
     * cache information in the database.
     *
     * @param schedulerID  - targeted Scheduler's ID
     * @param forceUpdate  - forces an immediate update to start the new scheduler before returning.
     */
    public static void StartExistingScheduler(int schedulerID, boolean forceUpdate)
    {
        if(schedulerIDs.get(schedulerID))
        {
            synchronized (startExistingSchedulerRequests) {
                startExistingSchedulerRequests.add(schedulerID);

                if(forceUpdate)
                {
                    UpdateState();
                }
            }
        }
    }

    /**
     * Requests for the {@link version2.prototype.Scheduler#Scheduler Scheduler} with the specified specified project name to have its
     * {@link version2#TaskState TaskState} value set to RUNNING. Starts a currently stopped Scheduler picking up where it stopped at according to the
     * cache information in the database.
     *
     * @param projectName  - targeted Scheduler's project name
     * @param forceUpdate  - forces an immediate update to start the new scheduler before returning.
     */
    public static void StartExistingScheduler(String projectName, boolean forceUpdate)
    {
        synchronized (startExistingSchedulerRequestsNames) {
            startExistingSchedulerRequestsNames.add(projectName);

            if(forceUpdate)
            {
                UpdateState();
            }
        }
    }

    /**
     * Gets the number of {@link version2.prototype.download#GlobalDownloader GlobalDownloader} objects currently created.
     *
     * @return number of {@link version2.prototype.download#GlobalDownloader GlobalDownloader} instances stored
     */
    public static int GetNumberOfGlobalDownloaders()
    {
        if(instance == null)
        {
            EASTWebManager.Start(defaultNumOfSimultaneousGlobalDLs, defaultMSBeetweenUpdates);
        }
        int num;
        synchronized (numOfCreatedGDLs) {
            num = numOfCreatedGDLs;
        }
        return num;
    }

    /**
     * Gets the number of {@link version2.prototype.Scheduler#Scheduler Scheduler} objects currently created.
     *
     * @return number of {@link version2.prototype.Scheduler#Scheduler Scheduler} instances stored
     */
    public static int GetNumberOfSchedulerResources()
    {
        if(instance == null)
        {
            EASTWebManager.Start(defaultNumOfSimultaneousGlobalDLs, defaultMSBeetweenUpdates);
        }
        int num;
        synchronized (numOfCreatedSchedulers) {
            num = numOfCreatedSchedulers;
        }
        return num;
    }

    /**
     * Returns the list of {@link version2.prototype.Scheduler#SchedulerStatus SchedulerStatus} objects relevant to all currently known active
     * {@link version2.prototype.Scheduler#Scheduler Scheduler} instances. This information is updated passively by EASTWebManager's background thread if
     * {@link #Start Start(int, int)} was called with a value greater than 0 for msBeetweenUpdates, otherwise it is updated actively by calling
     * {@link #UpdateState UpdateState()}.
     *
     * @return list of SchedulerStatus objects for the currently created Scheduler instances
     */
    public static ArrayList<SchedulerStatus> GetSchedulerStatuses()
    {
        if(instance == null)
        {
            EASTWebManager.Start(defaultNumOfSimultaneousGlobalDLs, defaultMSBeetweenUpdates);
        }
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
     * Gets the {@link version2.prototype.Scheduler#SchedulerStatus SchedulerStatus} currently known for a {@link version2.prototype.Scheduler#Scheduler Scheduler} with the
     * given unique ID, if it exists. If not, then returns null.
     *
     * @param schedulerID  - unique ID of the target {@link version2.prototype.Scheduler#Scheduler Scheduler} instance
     * @return the currently known {@link version2.prototype.Scheduler#SchedulerStatus SchedulerStatus} for the target Scheduler if found, otherwise null returned.
     */
    public static SchedulerStatus GetSchedulerStatus(int schedulerID)
    {
        SchedulerStatus status = null;

        if(instance == null)
        {
            EASTWebManager.Start(defaultNumOfSimultaneousGlobalDLs, defaultMSBeetweenUpdates);
        }
        synchronized (schedulerStatuses)
        {
            for(SchedulerStatus aStatus : schedulerStatuses)
            {
                if(aStatus.SchedulerID == schedulerID) {
                    status = aStatus;
                    break;
                }
            }
        }

        return status;
    }

    /**
     * Gets the {@link version2.prototype.Scheduler#SchedulerStatus SchedulerStatus} currently known for a {@link version2.prototype.Scheduler#Scheduler Scheduler} with the
     * given unique ID, if it exists. If not, then returns null.
     *
     * @param projectName  - targeted Scheduler's project name
     * @return the currently known {@link version2.prototype.Scheduler#SchedulerStatus SchedulerStatus} for the target Scheduler if found, otherwise null returned.
     */
    public static SchedulerStatus GetSchedulerStatus(String projectName)
    {
        SchedulerStatus status = null;

        if(instance == null)
        {
            EASTWebManager.Start(defaultNumOfSimultaneousGlobalDLs, defaultMSBeetweenUpdates);
        }
        synchronized (schedulerStatuses)
        {
            for(SchedulerStatus aStatus : schedulerStatuses)
            {
                if(aStatus.ProjectName.equals(projectName)) {
                    status = aStatus;
                    break;
                }
            }
        }

        if(status != null) {
            return new SchedulerStatus(status);
        } else {
            return null;
        }
    }

    /**
     * Adds a {@link version2#GUIUpdateHandler GUIUpdateHandler} object to the list of registered GUIUpdateHandlers that the EASTWebManager's background
     * thread will run once an update is detected to one of the {@link version2.prototype.Scheduler#SchedulerStatus SchedulerStatus} objects. The thread runs as
     * often as specified when calling the {@link #Start Start(int, int)} method.
     *
     * @param handler  - GUIUpdateHandler object execute when updates to the UI are triggered.
     */
    public static void RegisterGUIUpdateHandler(GUIUpdateHandler handler)
    {
        synchronized (guiHandlers)
        {
            guiHandlers.put(handler, new Boolean(false));
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
            guiHandlers.put(handler, new Boolean(true));
        }
    }

    /**
     * Checks if the specified GUIUpdateHandler instance is registered.
     * @param handler  - instance to search for
     * @return boolean - TRUE if registered, FALSE otherwise
     */
    public static boolean IsRegistered(GUIUpdateHandler handler)
    {
        boolean registered = false;
        synchronized(guiHandlers)
        {
            if(guiHandlers.get(handler) != null) {
                registered = true;
            }
        }
        return registered;
    }

    /* (non-Javadoc)
     * @see version2.prototype.EASTWebManagerI#run()
     */
    @Override
    public void run() {
        do
        {
            try
            {
                if(!manualUpdate) {
                    Thread.sleep(msBeetweenUpdates);
                }

                // Handle new Scheduler requests
                if(startNewSchedulerRequests.size() > 0)
                {
                    List<SchedulerData> tempRequestsList = new ArrayList<SchedulerData>();
                    synchronized (startNewSchedulerRequests)
                    {
                        while(startNewSchedulerRequests.size() > 0)
                        {
                            tempRequestsList.add(startNewSchedulerRequests.remove(0));
                        }
                    }

                    while(tempRequestsList.size() > 0)
                    {
                        handleStartNewSchedulerRequests(tempRequestsList.remove(0));
                    }
                }

                if(loadNewSchedulerRequests.size() > 0)
                {
                    List<SchedulerData> tempRequestsList = new ArrayList<SchedulerData>();
                    synchronized (loadNewSchedulerRequests)
                    {
                        while(loadNewSchedulerRequests.size() > 0)
                        {
                            tempRequestsList.add(loadNewSchedulerRequests.remove(0));
                        }
                    }

                    while(tempRequestsList.size() > 0)
                    {
                        handleLoadNewSchedulerRequests(tempRequestsList.remove(0));
                    }
                }

                if(!justCreateNewSchedulers)
                {
                    // Handle stop scheduler requests
                    if(stopExistingSchedulerRequests.size() > 0)
                    {
                        List<Integer> tempRequestsList = new ArrayList<Integer>();
                        synchronized (stopExistingSchedulerRequests)
                        {
                            while(stopExistingSchedulerRequests.size() > 0)
                            {
                                tempRequestsList.add(stopExistingSchedulerRequests.remove(0));
                            }
                        }

                        while(tempRequestsList.size() > 0)
                        {
                            handleStopSchedulerRequests(tempRequestsList.remove(0));
                        }
                    }

                    if(stopExistingSchedulerRequestsNames.size() > 0)
                    {
                        List<String> tempRequestsList = new ArrayList<String>();
                        synchronized (stopExistingSchedulerRequestsNames)
                        {
                            while(stopExistingSchedulerRequestsNames.size() > 0)
                            {
                                tempRequestsList.add(stopExistingSchedulerRequestsNames.remove(0));
                            }
                        }

                        Map<String,Integer> schedulerNamesAndIDs = getClonedSchedulersIDList();

                        int schedulerId;
                        String projectName;
                        while(tempRequestsList.size() > 0)
                        {
                            projectName = tempRequestsList.remove(0);
                            schedulerId = getSchedulerIDFromClonedList(schedulerNamesAndIDs, projectName);
                            if(schedulerId != -1) {
                                handleStopSchedulerRequests(schedulerId);
                            }
                        }
                    }

                    // Handle delete scheduler requests
                    if(deleteExistingSchedulerRequests.size() > 0)
                    {
                        List<Integer> tempRequestsList = new ArrayList<Integer>();
                        synchronized (deleteExistingSchedulerRequests)
                        {
                            while(deleteExistingSchedulerRequests.size() > 0)
                            {
                                tempRequestsList.add(deleteExistingSchedulerRequests.remove(0));
                            }
                        }

                        while(tempRequestsList.size() > 0)
                        {
                            handleDeleteSchedulerRequests(tempRequestsList.remove(0));
                        }
                    }

                    if(deleteExistingSchedulerRequestsNames.size() > 0)
                    {
                        List<String> tempRequestsList = new ArrayList<String>();
                        synchronized (deleteExistingSchedulerRequestsNames)
                        {
                            while(deleteExistingSchedulerRequestsNames.size() > 0)
                            {
                                tempRequestsList.add(deleteExistingSchedulerRequestsNames.remove(0));
                            }
                        }

                        Map<String,Integer> schedulerNamesAndIDs = getClonedSchedulersIDList();

                        int schedulerId;
                        String projectName;
                        while(tempRequestsList.size() > 0)
                        {
                            projectName = tempRequestsList.remove(0);
                            schedulerId = getSchedulerIDFromClonedList(schedulerNamesAndIDs, projectName);
                            if(schedulerId != -1) {
                                handleDeleteSchedulerRequests(schedulerId);
                            }
                        }
                    }

                    // Handle start back up existing Scheduler requests
                    if(startExistingSchedulerRequests.size() > 0)
                    {
                        List<Integer> tempRequestsList = new ArrayList<Integer>();
                        synchronized (startExistingSchedulerRequests)
                        {
                            while(startExistingSchedulerRequests.size() > 0)
                            {
                                tempRequestsList.add(startExistingSchedulerRequests.remove(0));
                            }
                        }

                        while(tempRequestsList.size() > 0)
                        {
                            handleStartExistingSchedulerRequests(tempRequestsList.remove(0));
                        }
                    }

                    if(startExistingSchedulerRequestsNames.size() > 0)
                    {
                        List<String> tempRequestsList = new ArrayList<String>();
                        synchronized (startExistingSchedulerRequestsNames)
                        {
                            while(startExistingSchedulerRequestsNames.size() > 0)
                            {
                                tempRequestsList.add(startExistingSchedulerRequestsNames.remove(0));
                            }
                        }

                        Map<String,Integer> schedulerNamesAndIDs = getClonedSchedulersIDList();

                        int schedulerId;
                        String projectName;
                        while(tempRequestsList.size() > 0)
                        {
                            projectName = tempRequestsList.remove(0);
                            schedulerId = getSchedulerIDFromClonedList(schedulerNamesAndIDs, projectName);
                            if(schedulerId != -1) {
                                handleStartExistingSchedulerRequests(schedulerId);
                            }
                        }
                    }

                    // Handle stopping and deleting GlobalDownloaders whose using projects are all stopped.
                    // Handle stopping and deleting GlobalDownloaders that don't have any currently existing projects using them.
                    // Handle restarting stopped GlobalDownloaders for which a using project has been started.
                    if(globalDLs.size() > 0)
                    {
                        synchronized (globalDLs)
                        {
                            if(schedulers.size() > 0)
                            {
                                Map<String,TaskState> pluginNamesAndRunningState = getRunningStateForProjectPluginsList();

                                Collection<GlobalDownloader> gdlList = globalDLs.values();
                                for(GlobalDownloader gdl : gdlList)
                                {
                                    if(!pluginNamesAndRunningState.containsKey(gdl.pluginName)
                                            || pluginNamesAndRunningState.get(gdl.pluginName) == TaskState.STOPPED)
                                    {
                                        if(gdl.GetRunningState() == TaskState.RUNNING)
                                        {
                                            System.out.println("Stopping GlobalDownloader '" + gdl.pluginName + "':'" + gdl.metaData.name + "'.");
                                            gdl.Stop();
                                            globalDLFutures.remove(gdl.ID).cancel(false);
                                        }
                                        //                                        gdl.deleteObservers();
                                        //                                        globalDLs.remove(gdl.ID);
                                        //                                        releaseGlobalDLID(gdl.ID);
                                    }
                                    else if(gdl.GetRunningState() == TaskState.STOPPED)
                                    {
                                        System.out.println("Starting GlobalDownloader '" + gdl.pluginName + "':'" + gdl.metaData.name + "'.");
                                        gdl.Start();
                                        globalDLFutures.put(gdl.ID, globalDLExecutor.scheduleWithFixedDelay(gdl, 0, 1, TimeUnit.DAYS));
                                    }
                                }
                            }
                            else
                            {
                                Collection<GlobalDownloader> gdlList = globalDLs.values();
                                for(GlobalDownloader gdl : gdlList)
                                {
                                    if(gdl.GetRunningState() == TaskState.RUNNING)
                                    {
                                        System.out.println("Stopping GlobalDownloader '" + gdl.pluginName + "':'" + gdl.metaData.name + "'.");
                                        gdl.Stop();
                                        globalDLFutures.remove(gdl.ID).cancel(false);
                                    }
                                    //                                    gdl.deleteObservers();
                                    //                                    globalDLs.remove(gdl.ID);
                                    //                                    releaseGlobalDLID(gdl.ID);
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
                            System.out.println("Running GUI Update Handlers");
                            runGUIUpdateHandlers();
                            schedulerStatesChanged = false;
                            System.out.println("Done with GUI Update Handlers");
                        }
                    }
                }
            }
            catch (InterruptedException | ConcurrentModificationException e) {
                ErrorLog.add(configInstance, "EASTWebManager.run error.", e);
            } catch (Exception e) {
                ErrorLog.add(configInstance, "EASTWebManager.run error.", e);
            }
        }while((msBeetweenUpdates > 0) && !manualUpdate);
        manualUpdate = false;
        justCreateNewSchedulers = false;
    }

    /*
     * (non-Javadoc)
     * @see version2.prototype.EASTWebManagerI#NotifyUI(version2.prototype.Scheduler.SchedulerStatus)
     */
    @Override
    public void NotifyUI(SchedulerStatus updatedStatus)
    {
        synchronized (schedulerStatesChanged)
        {
            System.out.println("Updating Scheduler status in EASTWeb Manager.");
            schedulerStatesChanged = true;

            synchronized (schedulerStatuses)
            {
                for(int i=0; i < schedulerStatuses.size(); i++)
                {
                    if(schedulerStatuses.get(i).SchedulerID == updatedStatus.SchedulerID)
                    {
                        schedulerStatuses.set(i, updatedStatus);
                        break;
                    }
                }
            }
            System.out.println("Done updating Scheduler status in EASTWeb Manager.");
        }
    }

    /* (non-Javadoc)
     * @see version2.prototype.EASTWebManagerI#StartGlobalDownloader(version2.prototype.download.DownloadFactory)
     */
    @Override
    public LocalDownloader StartGlobalDownloader(DownloadFactory dlFactory)
    {
        synchronized (globalDLs)
        {
            int id = getLowestAvailableGlobalDLID();
            if(IsIDValid(id, globalDLIDs))
            {
                GlobalDownloader gdl;
                LocalDownloader localDl;
                int currentGDLIdx = -1;
                String tempDownloadFactoryClassName;
                for(int i=0; i < globalDLs.size(); i++)
                {
                    tempDownloadFactoryClassName = globalDLs.get(i).metaData.downloadFactoryClassName;
                    if(tempDownloadFactoryClassName.equals(dlFactory.downloadMetaData.downloadFactoryClassName))
                    {
                        currentGDLIdx = i;
                        break;
                    }
                }

                DownloaderFactory factory = null;
                try {
                    factory = dlFactory.CreateDownloaderFactory(dlFactory.CreateListDatesFiles());
                } catch (IOException e) {
                    ErrorLog.add(configInstance, "EASTWebManager.StartGlobalDownloader error while creating DownloadFactory or ListDatesFiles.", e);
                } catch (Exception e) {
                    ErrorLog.add(configInstance, "EASTWebManager.StartGlobalDownloader error while creating DownloadFactory or ListDatesFiles.", e);
                }

                if(currentGDLIdx >= 0)
                {
                    releaseGlobalDLID(id);
                    gdl = globalDLs.get(currentGDLIdx);
                }
                else {
                    System.out.println("Creating new GlobalDownloader for '" + dlFactory.downloadMetaData.name + "' for plugin '" + dlFactory.downloadMetaData.Title + "'.");
                    gdl = factory.CreateGlobalDownloader(id);
                    if(gdl == null) {
                        return null;
                    }
                    System.out.println("GlobalDownloader for '" + dlFactory.downloadMetaData.name + "' for plugin '" + dlFactory.downloadMetaData.Title + "' created.");

                    globalDLs.put(id, gdl);
                    globalDLFutures.put(id, globalDLExecutor.scheduleWithFixedDelay(gdl, 0, 1, TimeUnit.DAYS));
                }

                synchronized (numOfCreatedGDLs) {
                    numOfCreatedGDLs = globalDLs.size();
                }

                if(gdl.GetRunningState() == TaskState.RUNNING) {
                    System.out.println("GlobalDownloader already running for '" + dlFactory.downloadMetaData.name + "' for plugin '" + dlFactory.downloadMetaData.Title + "'.");
                } else if(currentGDLIdx >= 0) {
                    System.out.println("Restarting GlobalDownloader for '" + dlFactory.downloadMetaData.name + "' for plugin '" + dlFactory.downloadMetaData.Title + "'.");
                    gdl.Start();
                    globalDLFutures.put(gdl.ID, globalDLExecutor.scheduleWithFixedDelay(gdl, 0, 1, TimeUnit.DAYS));
                } else {
                    System.out.println("Starting GlobalDownloader for '" + dlFactory.downloadMetaData.name + "' for plugin '" + dlFactory.downloadMetaData.Title + "'.");
                    gdl.Start();
                }

                System.out.println("Creating new LocalDownloader for '" + dlFactory.downloadMetaData.name + "' for plugin '" + dlFactory.downloadMetaData.Title + "'.");
                localDl = factory.CreateLocalDownloader(gdl);
                System.out.println("LocalDownloader for '" + dlFactory.downloadMetaData.name + "' for plugin '" + dlFactory.downloadMetaData.Title + "' created.");
                return localDl;
            }
            else
            {
                return null;
            }
        }
    }

    /* (non-Javadoc)
     * @see version2.prototype.EASTWebManagerI#StopGlobalDownloader(int)
     */
    @Override
    public void StopGlobalDownloader(int gdlID)
    {
        synchronized (globalDLs)
        {
            if(globalDLs.size() > gdlID)
            {
                globalDLs.get(gdlID).Stop();
            }
        }
    }

    /* (non-Javadoc)
     * @see version2.prototype.EASTWebManagerI#StartExistingGlobalDownloader(int)
     */
    @Override
    public void StartExistingGlobalDownloader(int gdlID)
    {
        synchronized (globalDLs)
        {
            if(globalDLs.size() > gdlID)
            {
                globalDLs.get(gdlID).Start();
            }
        }
    }

    /* (non-Javadoc)
     * @see version2.prototype.EASTWebManagerI#StartNewProcessWorker(version2.prototype.ProcessWorker)
     */
    @Override
    public Future<ProcessWorkerReturn> StartNewProcessWorker(ProcessWorker worker)
    {
        return processWorkerExecutor.submit(worker);
    }

    @Override
    public DatabaseConnection GetConnection() {
        return connectionPool.getConnection();
    }

    /**
     * Empty instance just to allow usage of private classes.
     */
    protected EASTWebManager()
    {
        //        manualUpdate = false;
        //        justCreateNewSchedulers = false;
        //        msBeetweenUpdates = Integer.MAX_VALUE;
        //
        //        startNewSchedulerRequests = null;
        //        stopExistingSchedulerRequests = null;
        //        stopExistingSchedulerRequestsNames = null;
        //        deleteExistingSchedulerRequests = null;
        //        deleteExistingSchedulerRequestsNames = null;
        //        startExistingSchedulerRequests = null;
        //        startExistingSchedulerRequestsNames = null;
        //
        //        // Setup for handling executing GlobalDownloaders
        //        globalDLs = null;
        //        globalDLExecutor = null;
        //        globalDLFutures = null;
        //
        //        // Setup for handling executing Schedulers
        //        schedulers = null;
        //
        //        // Setup for handling executing ProcessWorkers
        //        processWorkerExecutor = null;

        this(1, 1, 1000);
    }

    protected EASTWebManager(int numOfGlobalDLResourses, int numOfProcessWorkerResourses, int msBeetweenUpdates)
    {
        manualUpdate = false;
        justCreateNewSchedulers = false;
        this.msBeetweenUpdates = msBeetweenUpdates;
        configInstance = Config.getInstance();
        //        connectionPool = new C3P0ConnectionPool(configInstance);
        connectionPool = null;

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
        startNewSchedulerRequests = Collections.synchronizedList(new ArrayList<SchedulerData>(0));
        loadNewSchedulerRequests = Collections.synchronizedList(new ArrayList<SchedulerData>(0));
        stopExistingSchedulerRequests = Collections.synchronizedList(new ArrayList<Integer>(0));
        stopExistingSchedulerRequestsNames = Collections.synchronizedList(new ArrayList<String>(0));
        deleteExistingSchedulerRequests = Collections.synchronizedList(new ArrayList<Integer>(0));
        deleteExistingSchedulerRequestsNames = Collections.synchronizedList(new ArrayList<String>(0));
        startExistingSchedulerRequests = Collections.synchronizedList(new ArrayList<Integer>(0));
        startExistingSchedulerRequestsNames = Collections.synchronizedList(new ArrayList<String>(0));

        // Setup for handling executing GlobalDownloaders
        globalDLs = new HashMap<Integer, GlobalDownloader>();
        globalDLExecutor = Executors.newScheduledThreadPool(numOfGlobalDLResourses, gDLFactory);
        globalDLFutures = new HashMap<Integer, ScheduledFuture<?>>();

        // Setup for handling executing Schedulers
        schedulers = new HashMap<Integer, Scheduler>();

        // Setup for handling executing ProcessWorkers
        processWorkerExecutor = Executors.newFixedThreadPool(numOfProcessWorkerResourses, pwFactory);
    }

    protected void handleStartNewSchedulerRequests(SchedulerData data)
    {
        synchronized (schedulers)
        {
            int id = getLowestAvailableSchedulerID();
            if(IsIDValid(id, schedulerIDs))
            {
                System.out.println("Handling start request of a new Scheduler for project '" + data.projectInfoFile.GetProjectName() + "'.");
                //                schedulerStatuses.add(new SchedulerStatus(id, data.projectInfoFile.GetProjectName(), data.projectInfoFile.GetPlugins(), data.projectInfoFile.GetSummaries(), TaskState.STOPPED));
                Scheduler scheduler = null;
                scheduler = new Scheduler(data, id, TaskState.RUNNING, this, configInstance);
                schedulerStatuses.add(scheduler.GetSchedulerStatus());
                schedulers.put(id, scheduler);

                synchronized (numOfCreatedSchedulers) {
                    numOfCreatedSchedulers = schedulers.size();
                }
                System.out.println("Start request of a new Scheduler for project '" + data.projectInfoFile.GetProjectName() + "' handled.");
            }
            else
            {
                startNewSchedulerRequests.add(data);
            }
        }
    }

    protected void handleLoadNewSchedulerRequests(SchedulerData data)
    {
        synchronized (schedulers)
        {
            int id = getLowestAvailableSchedulerID();
            if(IsIDValid(id, schedulerIDs))
            {
                System.out.println("Handling load request of a new Scheduler for project '" + data.projectInfoFile.GetProjectName() + "'.");
                Scheduler scheduler = null;
                scheduler = new Scheduler(data, id, TaskState.STOPPED, this, configInstance);
                schedulerStatuses.add(scheduler.GetSchedulerStatus());
                schedulers.put(id, scheduler);

                synchronized (numOfCreatedSchedulers) {
                    numOfCreatedSchedulers = schedulers.size();
                }
                System.out.println("Load request of a new Scheduler for project '" + data.projectInfoFile.GetProjectName() + "' handled.");
            }
            else
            {
                loadNewSchedulerRequests.add(data);
            }
        }
    }

    protected void handleStopSchedulerRequests(int schedulerID)
    {
        synchronized (schedulers)
        {
            if(schedulers.size() > schedulerID)
            {
                System.out.println("Handling stop request of the Scheduler for project '" + schedulers.get(schedulerID).projectInfoFile.GetProjectName() + "'.");
                schedulers.get(schedulerID).Stop();
                System.out.println("Stop request of the Scheduler for project '" + schedulers.get(schedulerID).projectInfoFile.GetProjectName() + "' handled.");
            }
        }
    }

    protected void handleDeleteSchedulerRequests(int schedulerID)
    {
        synchronized (schedulers)
        {
            if(schedulers.size() > schedulerID)
            {
                System.out.println("Handling delete request of the project '" + schedulers.get(schedulerID).projectInfoFile.GetProjectName() + "'.");
                schedulers.get(schedulerID).Delete();
                schedulers.remove(schedulerID);
                releaseSchedulerID(schedulerID);

                synchronized (numOfCreatedSchedulers) {
                    numOfCreatedSchedulers = schedulers.size();
                }
                System.out.println("Delete request of the project '" + schedulers.get(schedulerID).projectInfoFile.GetProjectName() + "' handled.");
            }
        }
    }

    protected void handleStartExistingSchedulerRequests(int schedulerID)
    {
        synchronized (schedulers)
        {
            if(schedulers.size() > schedulerID)
            {
                System.out.println("Handling request to start back up the Scheduler for project '" + schedulers.get(schedulerID).projectInfoFile.GetProjectName() + "'.");
                schedulers.get(schedulerID).Start();
                System.out.println("Restart request of the Scheduler for project '" + schedulers.get(schedulerID).projectInfoFile.GetProjectName() + "' handled.");
            }
        }
    }

    protected void runGUIUpdateHandlers()
    {
        synchronized (guiHandlers)
        {
            ArrayList<GUIUpdateHandler> flaggedForRemoval = new ArrayList<GUIUpdateHandler>();
            Set<GUIUpdateHandler> handlers = guiHandlers.keySet();
            for(GUIUpdateHandler handler : handlers)
            {
                handler.run();
                if(guiHandlers.get(handler)) {
                    flaggedForRemoval.add(handler);
                }
            }

            for(GUIUpdateHandler handler : flaggedForRemoval)
            {
                guiHandlers.remove(handler);
            }
        }
    }

    protected int getLowestAvailableSchedulerID()
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

    protected void releaseSchedulerID(int id)
    {
        synchronized (schedulerIDs)
        {
            if(IsIDValid(id, schedulerIDs))
            {
                schedulerIDs.clear(id);
            }
        }
    }

    protected int getLowestAvailableGlobalDLID()
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

    protected void releaseGlobalDLID(int id)
    {
        synchronized (globalDLIDs)
        {
            if(IsIDValid(id, globalDLIDs))
            {
                globalDLIDs.clear(id);
            }
        }
    }

    protected boolean IsIDValid(int id, BitSet set)
    {
        if((id >= 0) && (id < set.size())) {
            return true;
        } else {
            return false;
        }
    }

    protected Map<String,Integer> getClonedSchedulersIDList() {
        Map<String,Integer> schedulerNameAndID = new HashMap<String,Integer>(0);
        synchronized(schedulers)
        {
            Collection<Scheduler> schedulerList = schedulers.values();
            for(Scheduler scheduler : schedulerList)
            {
                schedulerNameAndID.put(scheduler.projectInfoFile.GetProjectName(), scheduler.GetID());
            }
        }
        return schedulerNameAndID;
    }

    protected Map<String,TaskState> getRunningStateForProjectPluginsList() {
        Map<String,TaskState> pluginNamesAndRunningState = new HashMap<String,TaskState>(0);
        synchronized(schedulers)
        {
            Collection<Scheduler> schedulerList = schedulers.values();
            for(Scheduler scheduler : schedulerList)
            {
                for(ProjectInfoPlugin pluginInfo : scheduler.projectInfoFile.GetPlugins())
                {
                    if(pluginNamesAndRunningState.containsKey(pluginInfo.GetName()))
                    {
                        if(pluginNamesAndRunningState.get(pluginInfo.GetName()) == TaskState.STOPPED)
                        {
                            pluginNamesAndRunningState.put(pluginInfo.GetName(), scheduler.GetState());
                        }
                    }
                    else
                    {
                        pluginNamesAndRunningState.put(pluginInfo.GetName(), scheduler.GetState());
                    }
                }
            }
        }
        return pluginNamesAndRunningState;
    }

    protected int getSchedulerIDFromClonedList(
            Map<String, Integer> schedulerNamesAndIDs, String projectName) {
        int schedulerId;
        schedulerId = -1;
        Set<String> projectNames = schedulerNamesAndIDs.keySet();
        for(String name : projectNames)
        {
            if(name.equals(projectName))
            {
                schedulerId = schedulerNamesAndIDs.get(name);
                break;
            }
        }
        return schedulerId;
    }
}
