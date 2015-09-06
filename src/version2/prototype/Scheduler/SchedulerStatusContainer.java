/**
 *
 */
package version2.prototype.Scheduler;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import version2.prototype.Config;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.util.DatabaseConnection;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.ProgressUpdater;

/**
 * @author michael.devos
 *
 */
public class SchedulerStatusContainer {
    /**
     * Scheduler instance assigned unique identifier.
     */
    public final int SchedulerID;
    /**
     * Project name associated with the Scheduler.
     */
    public final ProjectInfoFile projectMetaData;
    /**
     * Plugin metadata objects use.
     */
    public final PluginMetaDataCollection pluginMetaDataCollection;

    private final Config configInstance;
    private ProgressUpdater progressUpdater;
    private TreeMap<String, TreeMap<String, Double>> downloadProgressesByData;
    private TreeMap<String, Double> processorProgresses;
    private TreeMap<String, Double> indicesProgresses;
    private TreeMap<String, TreeMap<Integer, Double>> summaryProgresses;
    private List<String> log;
    private TaskState state;
    private boolean projectUpToDate;
    private LocalDateTime lastModifiedTime;

    /**
     * Creates a SchedulerStatusContainer
     * @param configInstance
     * @param SchedulerID
     * @param progressUpdater
     * @param projectMetaData
     * @param pluginMetaDataCollection
     * @param log
     * @param state
     * @param downloadProgressesByData
     * @param processorProgresses
     * @param indicesProgresses
     * @param summaryProgresses
     * @param projectUpToDate
     * @param lastModifiedTime
     */
    public SchedulerStatusContainer(Config configInstance, int SchedulerID, ProgressUpdater progressUpdater, ProjectInfoFile projectMetaData, PluginMetaDataCollection pluginMetaDataCollection,
            List<String> log, TaskState state, TreeMap<String, TreeMap<String, Double>> downloadProgressesByData, TreeMap<String, Double> processorProgresses,
            TreeMap<String, Double> indicesProgresses, TreeMap<String, TreeMap<Integer, Double>> summaryProgresses, boolean projectUpToDate,
            LocalDateTime lastModifiedTime)
    {
        this.configInstance = configInstance;
        this.SchedulerID = SchedulerID;
        this.progressUpdater = progressUpdater;
        this.projectMetaData = projectMetaData;
        this.pluginMetaDataCollection = pluginMetaDataCollection;
        this.downloadProgressesByData = downloadProgressesByData;
        this.processorProgresses = processorProgresses;
        this.indicesProgresses = indicesProgresses;
        this.summaryProgresses = summaryProgresses;
        this.log = log;
        this.state = state;
        this.projectUpToDate = projectUpToDate;
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * Creates a default SchedulerStatusContainer with everything set to either the current time, 0, false, or the associated value given.
     * @param configInstance
     * @param SchedulerID
     * @param progressUpdater
     * @param projectMetaData
     * @param pluginMetaDataCollection
     * @param state
     */
    public SchedulerStatusContainer(Config configInstance, int SchedulerID, ProgressUpdater progressUpdater, ProjectInfoFile projectMetaData, PluginMetaDataCollection pluginMetaDataCollection, TaskState state)
    {
        this.configInstance = configInstance;
        this.SchedulerID = SchedulerID;
        this.progressUpdater = progressUpdater;
        this.projectMetaData = projectMetaData;
        this.pluginMetaDataCollection = pluginMetaDataCollection;
        ArrayList<ProjectInfoSummary> summariesTemp = new ArrayList<ProjectInfoSummary>();
        for(ProjectInfoSummary pfs : projectMetaData.GetSummaries()) {
            summariesTemp.add(new ProjectInfoSummary(pfs.GetZonalSummary(), null, pfs.GetTemporalSummaryCompositionStrategyClassName(), pfs.GetID()));
        }
        this.state = state;
        log = Collections.synchronizedList(new ArrayList<String>(1));
        projectUpToDate = false;


        downloadProgressesByData = new TreeMap<String, TreeMap<String, Double>>();
        TreeMap<String, Double> downloadDataProgressInit;
        processorProgresses = new TreeMap<String, Double>();
        indicesProgresses = new TreeMap<String, Double>();
        summaryProgresses = new TreeMap<String, TreeMap<Integer, Double>>();
        TreeMap<Integer, Double> summaryProgressesPluginResults;
        TreeMap<Integer, Boolean> resultsUpToDatePluginResults;
        String pluginName;
        for(ProjectInfoPlugin plugin : projectMetaData.GetPlugins())
        {
            pluginName = plugin.GetName();

            // Setup Download progresses
            downloadDataProgressInit = new TreeMap<String, Double>();
            downloadDataProgressInit.put("data", 0.0);
            for(String dataName : pluginMetaDataCollection.pluginMetaDataMap.get(pluginName).ExtraDownloadFiles)
            {
                dataName = dataName.toLowerCase();
                downloadDataProgressInit.put(dataName, 0.0);
            }
            downloadProgressesByData.put(pluginName, downloadDataProgressInit);

            // Setup Processor progresses
            processorProgresses.put(pluginName, 0.0);

            // Setup Indices progresses
            indicesProgresses.put(pluginName, 0.0);

            // Setup Summary progresses
            summaryProgressesPluginResults = new TreeMap<Integer, Double>();
            resultsUpToDatePluginResults = new TreeMap<Integer, Boolean>();
            for(ProjectInfoSummary summary : projectMetaData.GetSummaries())
            {
                summaryProgressesPluginResults.put(summary.GetID(), 0.0);
                resultsUpToDatePluginResults.put(summary.GetID(), false);
            }
            summaryProgresses.put(pluginName, summaryProgressesPluginResults);
        }

        updateLastModifiedTime();
    }

    /**
     * Get last modified time of this container.
     * @return LocalDateTime object representing the last time something in this container was modified
     */
    public LocalDateTime GetLastModifiedTime() { return lastModifiedTime; }

    /**
     * Gets the stored state of the Scheduler.
     * @return current TaskState of Scheduler
     */
    public TaskState GetState() { return state; }

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
        progressUpdater.UpdateDBDownloadExpectedCount(pluginName, dataName, listDatesFiles, modisTileNames, stmt);
        updateLastModifiedTime();
    }

    /**
     * Updates the processor progress for the given plugin.
     * @param pluginName  - name of plugin whose processor progress to change
     * @param stmt  - Statement object to reuse
     * @throws SQLException
     */
    public synchronized void UpdateProcessorProgress(String pluginName, Statement stmt) throws SQLException
    {
        progressUpdater.UpdateDBProcessorExpectedCount(pluginName, stmt);
        updateLastModifiedTime();
    }

    /**
     * Updates the indices progress for the given plugin.
     * @param pluginName  - name of plugin whose processor progress to change
     * @param stmt  - Statement object to reuse
     * @throws SQLException
     */
    public synchronized void UpdateIndicesProgress(String pluginName, Statement stmt) throws SQLException
    {
        progressUpdater.UpdateDBIndicesExpectedCount(pluginName, stmt);
        updateLastModifiedTime();
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
        progressUpdater.UpdateDBSummaryExpectedCount(summaryIDNum, compStrategy, daysPerInputData, pluginInfo, stmt);
        updateLastModifiedTime();
    }

    /**
     * Updates the stored state of the Scheduler.
     * @param state  - TaskState to assign to the SchedulerStatueContainer
     */
    public synchronized void UpdateSchedulerTaskState(TaskState state)
    {
        this.state = state;
        updateLastModifiedTime();
    }

    /**
     * Adds a string to the log.
     * @param logText  - the string to add to the log
     */
    public synchronized void AddToLog(String logText)
    {
        log.add(logText);
        updateLastModifiedTime();
    }

    /**
     * Gets the current state of this SchedulerStatusContainer as a SchedulerStatus object which erases the log as it instantiates the returned SchedulerStatus object.
     * @return SchedulerStatus representation of this object
     * @throws SQLException
     */
    public SchedulerStatus GetStatus() throws SQLException
    {
        // Update progresses
        String pluginName;
        double progress;
        DatabaseConnection con = DatabaseConnector.getConnection(configInstance);
        Statement stmt = con.createStatement();
        for(ProjectInfoPlugin pluginInfo : projectMetaData.GetPlugins())
        {
            pluginName = pluginInfo.GetName();

            // Setup Download progresses
            progress = progressUpdater.GetCurrentDownloadProgress("data", pluginName, stmt);
            downloadProgressesByData.get(pluginName).put("data", progress);
            for(String dataName : pluginMetaDataCollection.pluginMetaDataMap.get(pluginName).ExtraDownloadFiles)
            {
                dataName = dataName.toLowerCase();
                progress = progressUpdater.GetCurrentDownloadProgress(dataName, pluginName, stmt);
                downloadProgressesByData.get(pluginName).put(dataName, progress);
            }

            // Setup Processor progresses
            progress = progressUpdater.GetCurrentProcessorProgress(pluginName, stmt);
            processorProgresses.put(pluginName, progress);

            // Setup Indices progresses
            progress = progressUpdater.GetCurrentIndicesProgress(pluginName, stmt);
            indicesProgresses.put(pluginName, progress);

            // Setup Summary progresses
            for(ProjectInfoSummary summary : projectMetaData.GetSummaries())
            {
                progress = progressUpdater.GetCurrentSummaryProgress(summary.GetID(), pluginInfo, stmt);
                summaryProgresses.get(pluginName).put(summary.GetID(), progress);
            }
        }

        // Clone progresses if necessary
        TreeMap<String, TreeMap<String, Double>> downloadProgressesByDataTemp;
        synchronized(downloadProgressesByData){
            downloadProgressesByDataTemp = cloneTreeMapStringStringDouble(downloadProgressesByData);
        }

        TreeMap<String, TreeMap<Integer, Double>> summaryProgressesTemp;
        synchronized(summaryProgresses){
            summaryProgressesTemp = cloneTreeMapStringIntegerDouble(summaryProgresses);
        }

        ArrayList<String> newLog;
        synchronized(log){
            newLog = new ArrayList<String>(log);
        }

        CheckIfProjectIsUpToDate();

        return new SchedulerStatus(SchedulerID, projectMetaData, downloadProgressesByDataTemp, processorProgresses, indicesProgresses, summaryProgressesTemp, newLog, state, projectUpToDate, lastModifiedTime,
                LocalDateTime.now());
    }

    /**
     * Checks if the whole project is up to date or if any summary for any of the plugins being processed still has work to accomplish with the current information in this container.
     */
    private synchronized void CheckIfProjectIsUpToDate()
    {
        boolean isUpToDate = true;
        Iterator<String> pluginsIt = downloadProgressesByData.keySet().iterator();
        String pluginName;
        while(isUpToDate && pluginsIt.hasNext())
        {
            pluginName = pluginsIt.next();

            // Check download progresses
            for(Double value : downloadProgressesByData.get(pluginName).values())
            {
                if(value.intValue() != 100)
                {
                    isUpToDate = false;
                }
            }

            // Check Summary progresses
            for(Double value : summaryProgresses.get(pluginName).values())
            {
                if(value.intValue() != 100)
                {
                    isUpToDate = false;
                }
            }
        }

        // Check Processor progresses
        for(Double value : processorProgresses.values())
        {
            if(value.intValue() != 100){
                isUpToDate = false;
            }
        }

        // Check Indices progresses
        for(Double value : indicesProgresses.values())
        {
            if(value.intValue() != 100){
                isUpToDate = false;
            }
        }

        projectUpToDate = isUpToDate;
        updateLastModifiedTime();
    }

    private TreeMap<String, TreeMap<Integer, Double>> cloneTreeMapStringIntegerDouble(TreeMap<String, TreeMap<Integer, Double>> input)
    {
        TreeMap<String, TreeMap<Integer, Double>> clone = new TreeMap<String, TreeMap<Integer, Double>>();
        Iterator<String> pluginsIt = input.keySet().iterator();
        Iterator<Integer> summaryProgressesIt;
        TreeMap<Integer, Double> pluginResults;
        String plugin;
        Integer summaryID;
        while(pluginsIt.hasNext())
        {
            plugin = pluginsIt.next();
            summaryProgressesIt = input.get(plugin).keySet().iterator();
            pluginResults = new TreeMap<Integer, Double>();
            while(summaryProgressesIt.hasNext())
            {
                summaryID = new Integer(summaryProgressesIt.next());
                pluginResults.put(summaryID, input.get(plugin).get(new Integer(summaryID)));
            }
            clone.put(new String(plugin), pluginResults);
        }
        return clone;
    }

    private TreeMap<String, TreeMap<String, Double>> cloneTreeMapStringStringDouble(TreeMap<String, TreeMap<String, Double>> input)
    {
        TreeMap<String, TreeMap<String, Double>> clone = new TreeMap<String, TreeMap<String, Double>>();
        Iterator<String> pluginsIt = input.keySet().iterator();
        Iterator<String> downloadProgressesIt;
        TreeMap<String, Double> downloadResults;
        String plugin;
        String dataName;
        while(pluginsIt.hasNext())
        {
            plugin = pluginsIt.next();
            downloadProgressesIt = input.get(plugin).keySet().iterator();
            downloadResults = new TreeMap<String, Double>();
            while(downloadProgressesIt.hasNext())
            {
                dataName = new String(downloadProgressesIt.next());
                downloadResults.put(dataName, new Double(input.get(plugin).get(dataName)));
            }
            clone.put(new String(plugin), downloadResults);
        }
        return clone;
    }

    private synchronized void updateLastModifiedTime() {
        lastModifiedTime = LocalDateTime.now();
    }
}
