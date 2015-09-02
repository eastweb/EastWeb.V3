package version2.prototype.Scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import version2.prototype.TaskState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;

/**
 * Records the status of a Scheduler instance at a single point in time.
 *
 * @author michael.devos
 *
 */
public class SchedulerStatus {
    /**
     * Scheduler instance assigned unique identifier.
     */
    public final int SchedulerID;
    /**
     * Project name associated with the Scheduler.
     */
    public final String ProjectName;
    /**
     * ProjectInfoPlugin list originally parsed from project info metadata file the Scheduler was instantiated to run with.
     */
    public final ArrayList<ProjectInfoPlugin> PluginInfo;
    /**
     * ProjectInfoSummary list originally parsed from project info metadata file the Scheduler was instantiated to run with. Each have an ID attributed to them within the project metadata file.
     */
    public final ArrayList<ProjectInfoSummary> Summaries;
    /**
     * The running TaskState of the Scheduler.
     */
    public final TaskState State;
    /**
     * True if number of results published equals number of expected results calculated from project metadata and plugin metadata. False, otherwise.
     */
    public final boolean ProjectUpToDate;
    /**
     * A timestamp depicting the last time this SchedulerStatus object was updated by its Scheduler.
     */
    public final LocalDateTime LastModifiedTime;
    /**
     * A timestamp depicting the time this SchedulerStatus object was created/retrieved.
     */
    public final LocalDateTime RetrievedTime;
    /**
     * A map of plugin names to their named data to their download progress at the time specified by lastModifiedTime.
     */
    public final TreeMap<String, TreeMap<String, Double>> downloadProgressesByData;
    /**
     * A map of plugin names to their expected number of data files.
     */
    public final TreeMap<String, Integer> downloadExpectedDataFiles;
    /**
     * A map of plugin names to their indices progress at the time specified by lastModifiedTime.
     */
    public final TreeMap<String, Double> processorProgresses;
    /**
     * A map of plugin names to their expected number of outputs from the Processor step.
     */
    public final TreeMap<String, Integer> processorExpectedNumOfOutputs;
    /**
     * A map of plugin names to their indices progress at the time specified by lastModifiedTime.
     */
    public final TreeMap<String, Double> indicesProgresses;
    /**
     * A map of plugin names to their expected number of outputs from the Indices step.
     */
    public final TreeMap<String, Integer> indicesExpectedNumOfOutputs;
    /**
     * A map of plugin names to their maps of summary IDs to their summary progress at the time specified by lastModifiedTime.
     */
    public final TreeMap<String, TreeMap<Integer, Double>> summaryProgresses;
    /**
     * A map of plugin names to their maps of summary IDs to their expected number of outputs from the Summary step.
     */
    public final TreeMap<String, TreeMap<Integer, Integer>> summaryExpectedNumOfOutputsTemp;
    /**
     * A map of plugin names to their total number of files downloaded.
     */
    public final TreeMap<String, Integer> numOfFilesDownloaded;

    private final List<String> log;
    private int logReaderPos;

    /**
     *
     * @param SchedulerID
     * @param ProjectName
     * @param PluginInfo
     * @param Summaries
     * @param downloadProgressesByData
     * @param downloadExpectedDataFiles
     * @param processorProgresses
     * @param processorExpectedNumOfOutputs
     * @param indicesProgresses
     * @param indicesExpectedNumOfOutputs
     * @param summaryProgresses
     * @param summaryExpectedNumOfOutputsTemp
     * @param log
     * @param State
     * @param numOfFilesDownloaded
     * @param ProjectUpToDate
     * @param LastModifiedTime
     * @param RetrievedTime
     */
    public SchedulerStatus(int SchedulerID, String ProjectName, ArrayList<ProjectInfoPlugin> PluginInfo, ArrayList<ProjectInfoSummary> Summaries,
            TreeMap<String, TreeMap<String, Double>> downloadProgressesByData, TreeMap<String, Integer> downloadExpectedDataFiles, TreeMap<String, Double> processorProgresses,
            TreeMap<String, Integer> processorExpectedNumOfOutputs, TreeMap<String, Double> indicesProgresses, TreeMap<String, Integer> indicesExpectedNumOfOutputs,
            TreeMap<String, TreeMap<Integer, Double>> summaryProgresses, TreeMap<String, TreeMap<Integer, Integer>> summaryExpectedNumOfOutputsTemp, List<String> log, TaskState State,
            TreeMap<String, Integer> numOfFilesDownloaded, boolean ProjectUpToDate, LocalDateTime LastModifiedTime, LocalDateTime RetrievedTime)
    {
        this.SchedulerID = SchedulerID;
        this.ProjectName = ProjectName;
        this.PluginInfo = PluginInfo;
        this.Summaries = Summaries;
        this.downloadProgressesByData = downloadProgressesByData;
        this.downloadExpectedDataFiles = downloadExpectedDataFiles;
        this.processorProgresses = processorProgresses;
        this.processorExpectedNumOfOutputs = processorExpectedNumOfOutputs;
        this.indicesProgresses = indicesProgresses;
        this.indicesExpectedNumOfOutputs = indicesExpectedNumOfOutputs;
        this.summaryProgresses = summaryProgresses;
        this.summaryExpectedNumOfOutputsTemp = summaryExpectedNumOfOutputsTemp;
        this.log = log;
        logReaderPos = 0;
        this.State = State;
        this.numOfFilesDownloaded = numOfFilesDownloaded;
        this.ProjectUpToDate = ProjectUpToDate;
        this.LastModifiedTime = LastModifiedTime;
        this.RetrievedTime = RetrievedTime;
    }

    /**
     * Copy constructor.
     * @param statusToCopy
     */
    public SchedulerStatus(SchedulerStatus statusToCopy)
    {
        SchedulerID = new Integer(statusToCopy.SchedulerID);
        ProjectName = new String(statusToCopy.ProjectName);
        PluginInfo = new ArrayList<ProjectInfoPlugin>(statusToCopy.PluginInfo);
        Summaries = new ArrayList<ProjectInfoSummary>(statusToCopy.Summaries);
        downloadProgressesByData = cloneTreeMapStringStringDouble(statusToCopy.downloadProgressesByData);
        downloadExpectedDataFiles = statusToCopy.downloadExpectedDataFiles;
        processorProgresses = statusToCopy.processorProgresses;
        processorExpectedNumOfOutputs = statusToCopy.processorExpectedNumOfOutputs;
        indicesProgresses = statusToCopy.indicesProgresses;
        indicesExpectedNumOfOutputs = statusToCopy.indicesExpectedNumOfOutputs;
        summaryProgresses = cloneTreeMapStringIntegerDouble(statusToCopy.summaryProgresses);
        summaryExpectedNumOfOutputsTemp = cloneTreeMapStringIntegerInteger(statusToCopy.summaryExpectedNumOfOutputsTemp);
        log = statusToCopy.log;
        logReaderPos = 0;
        State = statusToCopy.State;
        numOfFilesDownloaded = statusToCopy.numOfFilesDownloaded;
        ProjectUpToDate = statusToCopy.ProjectUpToDate;
        LastModifiedTime = statusToCopy.LastModifiedTime;
        RetrievedTime = statusToCopy.RetrievedTime;
    }

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

    /**
     * Gets status of new log entries list.
     * @return boolean - TRUE if new log entries are within this status, FALSE otherwise is list is null or empty
     */
    public boolean HasLogEntries() {
        if(log != null && log.size() > 0){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Reads the next log entry. Incrementing position counter.
     * @return String - next log entry if there is one, otherwise null if empty or all log entries read.
     */
    public String ReadNextLogEntry()
    {
        if(logReaderPos < log.size()) {
            return log.get(logReaderPos++);
        } else {
            return null;
        }
    }

    /**
     * Returns a String list of log entries either as a subset of the total list of the whole list depending on the current reader position in the list. After calling the reader will return null until
     * it is reset.
     * @return list of log entries strings from the current reader position. If reader hasn't been used or has been reset immediately prior to this call then this list will contain the entire list of new
     * log entries.
     */
    public List<String> ReadAllRemainingLogEntries()
    {
        if(logReaderPos < log.size())
        {
            ArrayList<String> logEntries = new ArrayList<String>();
            for(;logReaderPos < log.size(); logReaderPos++)
            {
                logEntries.add(log.get(logReaderPos));
            }
            return logEntries;
        } else {
            return new ArrayList<String>(0);
        }
    }

    /**
     * Resets the log reader to start position.
     */
    public void ResetLogReader()
    {
        logReaderPos = 0;
    }

}
