package version2.prototype.Scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private final TreeMap<String, Double> downloadProgresses;
    private final TreeMap<String, Double> processorProgresses;
    private final TreeMap<String, Double> indicesProgresses;
    private final TreeMap<String, TreeMap<Integer, Double>> summaryProgresses;
    private List<String> log;
    private final TreeMap<String, TreeMap<Integer, Boolean>> resultsUpToDate;
    private final TreeMap<String, Integer> numOfFilesDownloaded;
    private final TreeMap<String, TreeMap<Integer, Integer>> numOfResultsPublished;

    /**
     *
     * @param SchedulerID
     * @param ProjectName
     * @param PluginInfo
     * @param Summaries
     * @param downloadProgresses
     * @param processorProgresses
     * @param indicesProgresses
     * @param summaryProgresses
     * @param log
     * @param State
     * @param resultsUpToDate
     * @param numOfFilesDownloaded
     * @param numOfResultsPublished
     * @param ProjectUpToDate
     * @param LastModifiedTime
     * @param RetrievedTime
     */
    public SchedulerStatus(int SchedulerID, String ProjectName, ArrayList<ProjectInfoPlugin> PluginInfo, ArrayList<ProjectInfoSummary> Summaries, TreeMap<String, Double> downloadProgresses,
            TreeMap<String, Double> processorProgresses, TreeMap<String, Double> indicesProgresses, TreeMap<String, TreeMap<Integer, Double>> summaryProgresses, List<String> log,
            TaskState State, TreeMap<String, TreeMap<Integer, Boolean>> resultsUpToDate, TreeMap<String, Integer> numOfFilesDownloaded,
            TreeMap<String, TreeMap<Integer, Integer>> numOfResultsPublished, boolean ProjectUpToDate, LocalDateTime LastModifiedTime, LocalDateTime RetrievedTime)
    {
        this.SchedulerID = SchedulerID;
        this.ProjectName = ProjectName;
        this.PluginInfo = PluginInfo;
        this.Summaries = Summaries;
        this.downloadProgresses = downloadProgresses;
        this.processorProgresses = processorProgresses;
        this.indicesProgresses = indicesProgresses;
        this.summaryProgresses = summaryProgresses;
        this.log = log;
        this.State = State;
        this.resultsUpToDate = resultsUpToDate;
        this.numOfFilesDownloaded = numOfFilesDownloaded;
        this.numOfResultsPublished = numOfResultsPublished;
        this.ProjectUpToDate = ProjectUpToDate;
        this.LastModifiedTime = LastModifiedTime;
        this.RetrievedTime = RetrievedTime;
    }

    /**
     * Gets the download progress of all the plugins being processed in this project.
     * @return a map of plugin names to their download progress at the time specified by lastModifiedTime
     */
    public TreeMap<String, Double> GetDownloadProgress() { return downloadProgresses; }

    /**
     * Gets the processor progress of all the plugins being processed in this project.
     * @return a map of plugin names to their processor progress at the time specified by lastModifiedTime
     */
    public TreeMap<String, Double> GetProcessorProgress() { return processorProgresses; }

    /**
     * Gets the indices progress of all the plugins being processed in this project.
     * @return a map of plugin names to their indices progress at the time specified by lastModifiedTime
     */
    public TreeMap<String, Double> GetIndicesProgress() { return indicesProgresses; }

    /**
     * Gets the summary progress of all the plugins being processed in this project.
     * @return a map of plugin names to their maps of summary IDs to their summary progress at the time specified by lastModifiedTime
     */
    public TreeMap<String, TreeMap<Integer, Double>> GetSummaryProgress() { return summaryProgresses; }

    /**
     * Get the additions to the log.
     * @return List of new strings to add to the log
     */
    public List<String> GetAndClearLog()
    {
        List<String> output;
        synchronized (log)
        {
            output = new ArrayList<String>(log);
            log = new ArrayList<String>(0);
            return output;
        }
    }

    /**
     * Gets the number of files downloaded per plugin used in this project.
     * @return a map of plugin names to their total number of files downloaded
     */
    public TreeMap<String, Integer> GetNumOfFilesDownloaded() { return numOfFilesDownloaded; }

    /**
     * Gets the number of results published for each summary for each plugin used in this project.
     * @return a map of plugin names to their maps of summary IDs to their total number of results published
     */
    public TreeMap<String, TreeMap<Integer, Integer>> GetNumOfResultsPublished() { return numOfResultsPublished; }

    /**
     * Gets a mapping of boolean values denoting whether summaries listed in the project metadata, per each plugin, are up to date with processing loaded downloads.
     * @return a map of plugin names to their maps of summary IDs to their status of up to date (TRUE) or still processing data (FALSE)
     */
    public TreeMap<String, TreeMap<Integer, Boolean>> GetResultsUpToDate() { return resultsUpToDate; }

    /**
     * Gets the last modified time for this SchedulerStatus.
     * @return LocalDateTime of last time something in the SchedulerStatus object was modified
     */
    public LocalDateTime GetLastModifiedTime() { return LastModifiedTime; }

}
