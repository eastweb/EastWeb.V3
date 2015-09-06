package version2.prototype;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.Scheduler.SchedulerData;
import version2.prototype.Scheduler.SchedulerStatus;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.DownloaderFactory;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.LocalDownloader;

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
    protected static List<SchedulerData> newSchedulerRequests;
    protected static List<Integer> stopExistingSchedulerRequests;
    protected static List<String> stopExistingSchedulerRequestsNames;
    protected static List<Integer> deleteExistingSchedulerRequests;
    protected static List<String> deleteExistingSchedulerRequestsNames;
    protected static List<Integer> startExistingSchedulerRequests;
    protected static List<String> startExistingSchedulerRequestsNames;
    protected static HashMap<GUIUpdateHandler, Boolean> guiHandlers = new HashMap<GUIUpdateHandler, Boolean>(0);     // Boolean - TRUE if flagged for removal

    // EASTWebManager state
    protected static Integer numOfCreatedGDLs = 0;
    protected static Integer numOfCreatedSchedulers = 0;
    protected static List<SchedulerStatus> schedulerStatuses = new ArrayList<SchedulerStatus>(1);
    protected static BitSet schedulerIDs = new BitSet(100000);
    protected static BitSet globalDLIDs = new BitSet(1000);
    protected static Boolean schedulerStatesChanged = false;
    protected boolean manualUpdate;
    protected boolean justCreateNewSchedulers;
    protected final int msBeetweenUpdates;

    // Object references of EASTWeb components
    protected List<GlobalDownloader> globalDLs;
    protected List<Scheduler> schedulers;
    protected List<ScheduledFuture<?>> globalDLFutures;
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
        } catch(Exception e)
        {
            ErrorLog.add(Config.getInstance(), "Problem while starting EASTWebManager.", e);
        }
    }

    public static ArrayList<String> GetRegisteredTemporalSummaryCompositionStrategies()
    {
        ArrayList<String> strategyNames = new ArrayList<String>();
        String selectQuery = "SELECT \"Name\" FROM \"" + Config.getInstance().getGlobalSchema() + "\".\"TemporalSummaryCompositionStrategy\"";
        ResultSet rs;
        Statement stmt;

        // Add missing TemporalCompositionStrategies
        // TODO: Need to set this up to look in a predefined directory for these java files at runtime, compile them, and then load them before adding them to this list. Should also remove newly missing ones.
        // SEE PluginMetaDataCollection.getXMLFiles for possibility.
        // SEE: http://stackoverflow.com/questions/21544446/how-do-you-dynamically-compile-and-load-external-java-classes
        // SEE: http://stackoverflow.com/questions/2946338/how-do-i-programmatically-compile-and-instantiate-a-java-class
        // SEE: http://docs.oracle.com/javase/7/docs/api/javax/tools/JavaCompiler.html

        //        try {
        //            stmt = DatabaseConnector.getConnection().createStatement();
        //
        //            Schemas.addTemporalSummaryCompositionStrategy(Config.getInstance().getGlobalSchema(), "GregorianWeeklyStrategy", stmt);
        //            Schemas.addTemporalSummaryCompositionStrategy(Config.getInstance().getGlobalSchema(), "GregorianMonthlyStrategy", stmt);
        //            Schemas.addTemporalSummaryCompositionStrategy(Config.getInstance().getGlobalSchema(), "CDCWeeklyStrategy", stmt);
        //            Schemas.addTemporalSummaryCompositionStrategy(Config.getInstance().getGlobalSchema(), "WHOWeeklyStrategy", stmt);
        //
        //            rs = stmt.executeQuery(selectQuery);
        //            if(rs != null)
        //            {
        //                while(rs.next())
        //                {
        //                    strategyNames.add(rs.getString("Name"));
        //                }
        //            }
        //        } catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException e) {
        //            ErrorLog.add(Config.getInstance(), "Problem while getting list of registered TemporalSummaryCompositionStragies.", e);
        //        }
        strategyNames.add("");
        strategyNames.add("GregorianWeeklyStrategy");
        strategyNames.add("GregorianMonthlyStrategy");
        strategyNames.add("CDCWeeklyStrategy");
        strategyNames.add("WHOWeeklyStrategy");
        return strategyNames;
    }

    /**
     * If {@link Start Start(int, int)} has been previously called then forces EASTWebManager to process any and all currently logged requests and
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
     * Requests for a new {@link version2.Scheduler#Scheduler Scheduler} to be started and keeps its reference for later status retrieval and stopping.
     * The created Scheduler can be identified via its {@link version2.Scheduler#SchedulerStatus SchedulerStatus} object gotten from calling
     * {@link GetSchedulerStatus GetSchedulerStatus()}.
     *
     * @param data - {@link version2.Scheduler#SchedulerData SchedulerData} to create the Scheduler instance from
     * @param manualUpdate  - forces an immediate update to start the new scheduler before returning.
     */
    public static void StartNewScheduler(SchedulerData data, boolean manualUpdate)
    {
        if(instance == null)
        {
            EASTWebManager.Start(defaultNumOfSimultaneousGlobalDLs, defaultMSBeetweenUpdates);
        }

        synchronized (newSchedulerRequests) {
            newSchedulerRequests.add(data);
        }

        if(manualUpdate)
        {
            if(instance != null) {
                instance.justCreateNewSchedulers = true;
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
     * Gets the number of {@link version2.download#GlobalDownloader GlobalDownloader} objects currently created.
     *
     * @return number of {@link version2.download#GlobalDownloader GlobalDownloader} instances stored
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
     * Gets the number of {@link version2.Scheduler#Scheduler Scheduler} objects currently created.
     *
     * @return number of {@link version2.Scheduler#Scheduler Scheduler} instances stored
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
     * Returns the list of {@link version2.Scheduler#SchedulerStatus SchedulerStatus} objects relevant to all currently known active
     * {@link version2.Scheduler#Scheduler Scheduler} instances. This information is updated passively by EASTWebManager's background thread if
     * {@link Start Start(int, int)} was called with a value greater than 0 for msBeetweenUpdates, otherwise it is updated actively by calling
     * {@link UpdateStatus UpdateStatus()}.
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
     * Gets the {@link version2.Scheduler#SchedulerStatus SchedulerStatus} currently known for a {@link version2.Scheduler#Scheduler Scheduler} with the
     * given unique ID, if it exists. If not, then returns null.
     *
     * @param schedulerID  - unique ID of the target {@link version2.Scheduler#Scheduler Scheduler} instance
     * @return the currently known {@link version2.Scheduler#SchedulerStatus SchedulerStatus} for the target Scheduler if found, otherwise null returned.
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
     * Gets the {@link version2.Scheduler#SchedulerStatus SchedulerStatus} currently known for a {@link version2.Scheduler#Scheduler Scheduler} with the
     * given unique ID, if it exists. If not, then returns null.
     *
     * @param schedulerID  - unique ID of the target {@link version2.Scheduler#Scheduler Scheduler} instance
     * @return the currently known {@link version2.Scheduler#SchedulerStatus SchedulerStatus} for the target Scheduler if found, otherwise null returned.
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
     * thread will run once an update is detected to one of the {@link version2.Scheduler#SchedulerStatus SchedulerStatus} objects. The thread runs as
     * often as specified when calling the {@link Start Start(int, int)} method.
     *
     * @param handler
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
            catch (InterruptedException | ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException | ConcurrentModificationException e) {
                ErrorLog.add(Config.getInstance(), "EASTWebManager.run error.", e);
            } catch (Exception e) {
                ErrorLog.add(Config.getInstance(), "EASTWebManager.run error.", e);
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
                DownloaderFactory factory = null;
                try {
                    factory = dlFactory.CreateDownloadFactory(dlFactory.CreateListDatesFiles());
                } catch (IOException e) {
                    ErrorLog.add(Config.getInstance(), "EASTWebManager.StartGlobalDownloader error whlie creating DownloadFactory or ListDatesFiles.", e);
                } catch (Exception e) {
                    ErrorLog.add(Config.getInstance(), "EASTWebManager.StartGlobalDownloader error whlie creating DownloadFactory or ListDatesFiles.", e);
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
                    if(globalDLs.size() == 0)
                    {
                        globalDLs.add(id, gdl);
                        globalDLFutures.add(id, globalDLExecutor.scheduleWithFixedDelay(gdl, 0, 1, TimeUnit.DAYS));
                    }
                    else
                    {
                        GlobalDownloader temp = globalDLs.get(id);
                        if(temp == null)
                        {
                            globalDLs.add(id, gdl);
                            globalDLFutures.add(id, globalDLExecutor.scheduleWithFixedDelay(gdl, 0, 1, TimeUnit.DAYS));
                        }
                        else{
                            globalDLs.set(id, gdl);
                            globalDLFutures.set(id, globalDLExecutor.scheduleWithFixedDelay(gdl, 0, 1, TimeUnit.DAYS));
                        }
                    }
                }

                synchronized (numOfCreatedGDLs) {
                    numOfCreatedGDLs = globalDLs.size();
                }

                gdl.Start();
                LocalDownloader localDl = factory.CreateLocalDownloader(gdl);
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

    /**
     * Empty instance just to allow usage of private classes.
     */
    protected EASTWebManager()
    {
        //        manualUpdate = false;
        //        justCreateNewSchedulers = false;
        //        msBeetweenUpdates = Integer.MAX_VALUE;
        //
        //        newSchedulerRequests = null;
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

        // Setup for handling executing GlobalDownloaders
        globalDLs = Collections.synchronizedList(new ArrayList<GlobalDownloader>(1));
        globalDLExecutor = Executors.newScheduledThreadPool(numOfGlobalDLResourses, gDLFactory);
        globalDLFutures = Collections.synchronizedList(new ArrayList<ScheduledFuture<?>>(1));

        // Setup for handling executing Schedulers
        schedulers = Collections.synchronizedList(new ArrayList<Scheduler>(1));

        // Setup for handling executing ProcessWorkers
        processWorkerExecutor = Executors.newFixedThreadPool(numOfProcessWorkerResourses, pwFactory);
    }

    protected void handleNewSchedulerRequests(SchedulerData data)
    {
        synchronized (schedulers)
        {
            int id = getLowestAvailableSchedulerID();
            if(IsIDValid(id, schedulerIDs))
            {
                //                schedulerStatuses.add(new SchedulerStatus(id, data.projectInfoFile.GetProjectName(), data.projectInfoFile.GetPlugins(), data.projectInfoFile.GetSummaries(), TaskState.STOPPED));
                Scheduler scheduler = null;
                scheduler = new Scheduler(data, id, this, Config.getInstance());
                schedulerStatuses.add(scheduler.GetSchedulerStatus());
                if(schedulers.size() == 0 || id >= schedulers.size() || schedulers.get(id) == null) {
                    schedulers.add(id, scheduler);
                }
                else {
                    schedulers.set(id, scheduler);
                }
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

    protected void handleStopSchedulerRequests(int schedulerID)
    {
        synchronized (schedulers)
        {
            if(schedulers.size() > schedulerID)
            {
                schedulers.get(schedulerID).Stop();
            }
        }
    }

    protected void handleDeleteSchedulerRequests(int schedulerID)
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

    protected void handleStartExistingSchedulerRequests(int schedulerID)
    {
        synchronized (schedulers)
        {
            if(schedulers.size() > schedulerID)
            {
                schedulers.get(schedulerID).Start();
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
}
