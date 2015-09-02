/**
 *
 */
package version2.prototype.Scheduler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class SchedulerStatusContainer {
    /**
     * Scheduler instance assigned unique identifier.
     */
    public final int schedulerID;
    /**
     * Project name associated with the Scheduler.
     */
    public final String projectName;
    /**
     * ProjectInfoPlugin list originally parsed from project info metadata file the Scheduler was instantiated to run with.
     */
    public final ArrayList<ProjectInfoPlugin> pluginInfo;
    /**
     * ProjectInfoSummary list originally parsed from project info metadata file the Scheduler was instantiated to run with. Each have an ID attributed to them within the project metadata file.
     */
    public final ArrayList<ProjectInfoSummary> summaries;

    private final Config configInstance;
    private TreeMap<String, TreeMap<String, Double>> downloadProgressesByData;
    private TreeMap<String, Integer> downloadExpectedDataFiles;
    private TreeMap<String, Double> processorProgresses;
    private TreeMap<String, Integer> processorExpectedNumOfOutputs;
    private TreeMap<String, Double> indicesProgresses;
    private TreeMap<String, Integer> indicesExpectedNumOfOutputs;
    private TreeMap<String, TreeMap<Integer, Double>> summaryProgresses;
    private TreeMap<String, TreeMap<Integer, Integer>> summaryExpectedNumOfOutputs;
    private List<String> log;
    private TaskState state;
    private boolean projectUpToDate;
    private TreeMap<String, Integer> numOfFilesLoaded;
    private LocalDateTime lastModifiedTime;

    /**
     * Creates a SchedulerStatusContainer
     * @param configInstance
     * @param schedulerID
     * @param projectName
     * @param pluginInfo
     * @param summaries
     * @param log
     * @param state
     * @param downloadProgressesByData
     * @param downloadExpectedDataFiles
     * @param processorProgresses
     * @param processorExpectedNumOfOutputs
     * @param indicesProgresses
     * @param indicesExpectedNumOfOutputs
     * @param summaryProgresses
     * @param summaryExpectedNumOfOutputs
     * @param projectUpToDate
     * @param numOfFilesLoaded
     * @param lastModifiedTime
     */
    public SchedulerStatusContainer(Config configInstance, int schedulerID, String projectName, ArrayList<ProjectInfoPlugin> pluginInfo, ArrayList<ProjectInfoSummary> summaries,
            List<String> log, TaskState state, TreeMap<String, TreeMap<String, Double>> downloadProgressesByData, TreeMap<String, Integer> downloadExpectedDataFiles,
            TreeMap<String, Double> processorProgresses, TreeMap<String, Integer> processorExpectedNumOfOutputs, TreeMap<String, Double> indicesProgresses,
            TreeMap<String, Integer> indicesExpectedNumOfOutputs, TreeMap<String, TreeMap<Integer, Double>> summaryProgresses, TreeMap<String, TreeMap<Integer, Integer>> summaryExpectedNumOfOutputs,
            boolean projectUpToDate, TreeMap<String, Integer> numOfFilesLoaded, LocalDateTime lastModifiedTime)
    {
        this.configInstance = configInstance;
        this.schedulerID = schedulerID;
        this.projectName = projectName;
        this.pluginInfo = pluginInfo;
        this.summaries = summaries;
        this.downloadProgressesByData = downloadProgressesByData;
        this.downloadExpectedDataFiles = downloadExpectedDataFiles;
        this.processorProgresses = processorProgresses;
        this.processorExpectedNumOfOutputs = processorExpectedNumOfOutputs;
        this.indicesProgresses = indicesProgresses;
        this.indicesExpectedNumOfOutputs = indicesExpectedNumOfOutputs;
        this.summaryProgresses = summaryProgresses;
        this.summaryExpectedNumOfOutputs = summaryExpectedNumOfOutputs;
        this.log = log;
        this.state = state;
        this.projectUpToDate = projectUpToDate;
        this.numOfFilesLoaded = numOfFilesLoaded;
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * Creates a default SchedulerStatusContainer with everything set to either the current time, 0, false, or the associated value given.
     * @param configInstance
     * @param schedulerID
     * @param projectName
     * @param pluginInfo
     * @param summaries
     * @param pluginMetaDataCollection
     * @param state
     */
    public SchedulerStatusContainer(Config configInstance, int schedulerID, String projectName, ArrayList<ProjectInfoPlugin> pluginInfo, ArrayList<ProjectInfoSummary> summaries,
            PluginMetaDataCollection pluginMetaDataCollection, TaskState state)
    {
        this.configInstance = configInstance;
        this.schedulerID = schedulerID;
        this.projectName = projectName;
        this.pluginInfo = pluginInfo;
        ArrayList<ProjectInfoSummary> summariesTemp = new ArrayList<ProjectInfoSummary>();
        for(ProjectInfoSummary pfs : summaries) {
            summariesTemp.add(new ProjectInfoSummary(pfs.GetZonalSummary(), null, pfs.GetTemporalSummaryCompositionStrategyClassName(), pfs.GetID()));
        }
        this.summaries = summariesTemp;
        this.state = state;
        log = Collections.synchronizedList(new ArrayList<String>(1));
        projectUpToDate = false;

        downloadProgressesByData = new TreeMap<String, TreeMap<String, Double>>();
        downloadExpectedDataFiles = new TreeMap<String, Integer>();
        TreeMap<String, Double> downloadDataProgressInit;
        processorProgresses = new TreeMap<String, Double>();
        processorExpectedNumOfOutputs = new TreeMap<String, Integer>();
        indicesProgresses = new TreeMap<String, Double>();
        indicesExpectedNumOfOutputs = new TreeMap<String, Integer>();
        summaryProgresses = new TreeMap<String, TreeMap<Integer, Double>>();
        summaryExpectedNumOfOutputs = new TreeMap<String, TreeMap<Integer, Integer>>();
        numOfFilesLoaded = new TreeMap<String, Integer>();
        TreeMap<Integer, Double> summaryProgressesPluginResults;
        TreeMap<Integer, Boolean> resultsUpToDatePluginResults;
        TreeMap<Integer, Integer> summaryExpectedNumOfOutputsPluginResults;
        String pluginName;
        for(ProjectInfoPlugin plugin : pluginInfo)
        {
            pluginName = plugin.GetName();

            // Setup Download progresses
            downloadDataProgressInit = new TreeMap<String, Double>();
            downloadDataProgressInit.put("Data", 0.0);
            for(String dataName : pluginMetaDataCollection.pluginMetaDataMap.get(pluginName).ExtraDownloadFiles)
            {
                downloadDataProgressInit.put(dataName, 0.0);
            }
            downloadProgressesByData.put(pluginName, downloadDataProgressInit);
            downloadExpectedDataFiles.put(pluginName, 0);

            // Setup Processor progresses
            processorProgresses.put(pluginName, 0.0);
            processorExpectedNumOfOutputs.put(pluginName, 0);

            // Setup Indices progresses
            indicesProgresses.put(pluginName, 0.0);
            indicesExpectedNumOfOutputs.put(pluginName, 0);

            // Setup Summary progresses
            summaryProgressesPluginResults = new TreeMap<Integer, Double>();
            resultsUpToDatePluginResults = new TreeMap<Integer, Boolean>();
            summaryExpectedNumOfOutputsPluginResults = new TreeMap<Integer, Integer>();
            for(ProjectInfoSummary summary : summaries)
            {
                summaryProgressesPluginResults.put(summary.GetID(), 0.0);
                resultsUpToDatePluginResults.put(summary.GetID(), false);
                summaryExpectedNumOfOutputsPluginResults.put(summary.GetID(), 0);
            }
            summaryProgresses.put(pluginName, summaryProgressesPluginResults);
            summaryExpectedNumOfOutputs.put(pluginName, summaryExpectedNumOfOutputsPluginResults);
            numOfFilesLoaded.put(pluginName, 0);
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
     * @param progress  - percentage of completion to set the download progress to
     * @param pluginName  - name of plugin whose download progress to change
     * @param data  - name of globally download data
     * @param downloadExpectedDataFiles  - number of expected data files. Ignored if pluginName.equalsIgnoreCase("Data") is not true.
     */
    public synchronized void UpdateDownloadProgressByData(double progress, String pluginName, String data, Integer downloadExpectedDataFiles)
    {
        downloadProgressesByData.get(pluginName).put(data, progress);
        if(data.equalsIgnoreCase("Data"))
        {
            this.downloadExpectedDataFiles.put(pluginName, downloadExpectedDataFiles);
        }
        updateLastModifiedTime();
    }

    /**
     * Updates the processor progress for the given plugin.
     * @param progress  - percentage of completion to set the processor progress to
     * @param pluginName  - name of plugin whose processor progress to change
     * @param expectedNumOfOutputs
     */
    public synchronized void UpdateProcessorProgress(double progress, String pluginName, int expectedNumOfOutputs)
    {
        processorProgresses.put(pluginName, progress);
        processorExpectedNumOfOutputs.put(pluginName, expectedNumOfOutputs);
        updateLastModifiedTime();
    }

    /**
     * Updates the indices progress for the given plugin.
     * @param progress  - percentage of completion to set the indices progress to
     * @param pluginName  - name of plugin whose download progress to change
     * @param expectedNumOfOutputs
     */
    public synchronized void UpdateIndicesProgress(double progress, String pluginName, int expectedNumOfOutputs)
    {
        indicesProgresses.put(pluginName, progress);
        indicesExpectedNumOfOutputs.put(pluginName, expectedNumOfOutputs);
        updateLastModifiedTime();
    }

    /**
     * Updates the summary progress for the summary attributed to the given ID for the named plugin.
     * @param progress  - percentage of completion to set the summary progress to
     * @param pluginName  - name of plugin whose summary progress to change
     * @param summaryID  - summary ID whose progress to change
     * @param expectedNumOfOutputs
     */
    public synchronized void UpdateSummaryProgress(double progress, String pluginName, int summaryID, int expectedNumOfOutputs)
    {
        summaryProgresses.get(pluginName).put(summaryID, progress);
        summaryExpectedNumOfOutputs.get(pluginName).put(summaryID, expectedNumOfOutputs);
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
     * Updates the number of files loaded map object.
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void UpdateNumOfFilesLoaded() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        synchronized(numOfFilesLoaded)
        {
            Connection con = DatabaseConnector.getConnection(configInstance);
            Statement stmt = con.createStatement();
            String formatStringDownloadsLoaded = "SELECT Count(\"DownloadCacheID\") AS \"DownloadCacheIDCount\" FROM \"%s\".\"DownloadCache\";";
            ResultSet rs = null;
            String projectPluginSchema;

            numOfFilesLoaded = new TreeMap<String, Integer>();
            for(ProjectInfoPlugin info : pluginInfo)
            {
                projectPluginSchema = Schemas.getSchemaName(projectName, info.GetName());
                rs = stmt.executeQuery(String.format(formatStringDownloadsLoaded, projectPluginSchema));
                if(rs != null && rs.next()) {
                    numOfFilesLoaded.put(info.GetName(), rs.getInt("DownloadCacheIDCount"));
                }
                else {
                    numOfFilesLoaded.put(info.GetName(), 0);
                }
            }

            stmt.close();
            if(rs != null) {
                rs.close();
            }
            con.close();
            updateLastModifiedTime();
        }
    }

    /**
     * Checks if the whole project is up to date or if any summary for any of the plugins being processed still has work to accomplish with the current information in this container.
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public synchronized void CheckIfProjectIsUpToDate() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
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
                if(value != 100)
                {
                    isUpToDate = false;
                }
            }

            // Check Summary progresses
            for(Double value : summaryProgresses.get(pluginName).values())
            {
                if(value != 100)
                {
                    isUpToDate = false;
                }
            }
        }

        // Check Processor progresses
        for(Double value : processorProgresses.values())
        {
            if(value != 100){
                isUpToDate = false;
            }
        }

        // Check Indices progresses
        for(Double value : indicesProgresses.values())
        {
            if(value != 100){
                isUpToDate = false;
            }
        }

        projectUpToDate = isUpToDate;
        updateLastModifiedTime();
    }

    /**
     * Gets the current state of this SchedulerStatusContainer as a SchedulerStatus object which erases the log as it instantiates the returned SchedulerStatus object.
     * @return SchedulerStatus representation of this object
     */
    public SchedulerStatus GetStatus()
    {
        TreeMap<String, TreeMap<String, Double>> downloadProgressesByDataTemp;
        synchronized(downloadProgressesByData){
            downloadProgressesByDataTemp = cloneTreeMapStringStringDouble(downloadProgressesByData);
        }

        TreeMap<String, TreeMap<Integer, Double>> summaryProgressesTemp;
        synchronized(summaryProgresses){
            summaryProgressesTemp = cloneTreeMapStringIntegerDouble(summaryProgresses);
        }

        TreeMap<String, TreeMap<Integer, Integer>> summaryExpectedNumOfOutputsTemp;
        synchronized(summaryExpectedNumOfOutputs)
        {
            summaryExpectedNumOfOutputsTemp = cloneTreeMapStringIntegerInteger(summaryExpectedNumOfOutputs);
        }

        ArrayList<String> newLog;
        synchronized(log){
            newLog = new ArrayList<String>(log);
        }

        return new SchedulerStatus(schedulerID, projectName, pluginInfo, summaries, downloadProgressesByDataTemp, downloadExpectedDataFiles, processorProgresses, processorExpectedNumOfOutputs,
                indicesProgresses, indicesExpectedNumOfOutputs, summaryProgressesTemp, summaryExpectedNumOfOutputsTemp, newLog, state, numOfFilesLoaded, projectUpToDate,
                lastModifiedTime, LocalDateTime.now());
    }

    //    private TreeMap<String, TreeMap<Integer, Boolean>> cloneTreeMapStringIntegerBoolean(TreeMap<String, TreeMap<Integer, Boolean>> input)
    //    {
    //        TreeMap<String, TreeMap<Integer, Boolean>> clone = new TreeMap<String, TreeMap<Integer, Boolean>>();
    //        Iterator<String> pluginsIt = input.keySet().iterator();
    //        Iterator<Integer> summaryProgressesIt;
    //        TreeMap<Integer, Boolean> pluginResults;
    //        String plugin;
    //        int summaryID;
    //        while(pluginsIt.hasNext())
    //        {
    //            plugin = pluginsIt.next();
    //            summaryProgressesIt = input.get(plugin).keySet().iterator();
    //            pluginResults = new TreeMap<Integer, Boolean>();
    //            while(summaryProgressesIt.hasNext())
    //            {
    //                summaryID = summaryProgressesIt.next();
    //                pluginResults.put(summaryID, input.get(plugin).get(summaryID));
    //            }
    //            clone.put(plugin, pluginResults);
    //        }
    //        return clone;
    //    }

    private TreeMap<String, TreeMap<Integer, Integer>> cloneTreeMapStringIntegerInteger(TreeMap<String, TreeMap<Integer, Integer>> input)
    {
        TreeMap<String, TreeMap<Integer, Integer>> clone = new TreeMap<String, TreeMap<Integer, Integer>>();
        Iterator<String> pluginsIt = input.keySet().iterator();
        Iterator<Integer> summaryProgressesIt;
        TreeMap<Integer, Integer> pluginResults;
        String plugin;
        Integer summaryID;
        while(pluginsIt.hasNext())
        {
            plugin = pluginsIt.next();
            summaryProgressesIt = input.get(plugin).keySet().iterator();
            pluginResults = new TreeMap<Integer, Integer>();
            while(summaryProgressesIt.hasNext())
            {
                summaryID = new Integer(summaryProgressesIt.next());
                pluginResults.put(summaryID, new Integer(input.get(plugin).get(summaryID)));
            }
            clone.put(new String(plugin), pluginResults);
        }
        return clone;
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
