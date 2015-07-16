package version2.prototype.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.TreeMap;

import version2.prototype.TaskState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;

/**
 * Records the status of a Scheduler instance at the time of SchedulerStatus creation.
 *
 * @author michael.devos
 *
 */
public class SchedulerStatus extends Observable {
    public final int schedulerID;
    public final String projectName;
    public final ArrayList<ProjectInfoPlugin> pluginInfo;
    public final ArrayList<ProjectInfoSummary> summaries;

    private TreeMap<String, TreeMap<Integer, Integer>> newUpdatesBeingProcessed;
    private List<Integer> downloadProgresses;
    private List<Integer> processorProgresses;
    private List<Integer> indicesProgresses;
    private List<Integer> summaryProgresses;
    private List<String> log;
    private TaskState state;

    public SchedulerStatus(int schedulerID, String projectName, ArrayList<ProjectInfoPlugin> pluginInfo, ArrayList<ProjectInfoSummary> summaries, TaskState state)
    {
        this.schedulerID = schedulerID;
        this.projectName = projectName;
        this.pluginInfo = pluginInfo;
        this.summaries = summaries;
        downloadProgresses = Collections.synchronizedList(new ArrayList<Integer>(1));
        processorProgresses = Collections.synchronizedList(new ArrayList<Integer>(1));
        indicesProgresses = Collections.synchronizedList(new ArrayList<Integer>(1));
        summaryProgresses = Collections.synchronizedList(new ArrayList<Integer>(1));
        log = Collections.synchronizedList(new ArrayList<String>(1));
        this.state = state;

        newUpdatesBeingProcessed = new TreeMap<String, TreeMap<Integer, Integer>>();
        TreeMap<Integer, Integer> tempSummaries;
        for(ProjectInfoPlugin plugin : pluginInfo)
        {
            tempSummaries = new TreeMap<Integer, Integer>();
            for(ProjectInfoSummary summary : summaries)
            {
                tempSummaries.put(summary.GetID(), 0);
            }
            newUpdatesBeingProcessed.put(plugin.GetName(), tempSummaries);
        }
    }

    public TreeMap<String, TreeMap<Integer, Integer>> GetNewUpdatesBeingProcessed() { synchronized (newUpdatesBeingProcessed) { return newUpdatesBeingProcessed; } }

    public List<Integer> GetDownloadProgress() { synchronized (downloadProgresses) { return downloadProgresses; } }

    public List<Integer> GetProcessorProgress() { synchronized (processorProgresses) { return processorProgresses; } }

    public List<Integer> GetIndicesProgress() { synchronized (indicesProgresses) { return indicesProgresses; } }

    public List<Integer> GetSummaryProgress() { synchronized (summaryProgresses) { return summaryProgresses; } }

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

    public TaskState GetState() { return state; }

    public void UpdateUpdatesBeingProcessed(TreeMap<String, TreeMap<Integer, Integer>> summaryUpdates)
    {
        synchronized (newUpdatesBeingProcessed)
        {
            newUpdatesBeingProcessed = summaryUpdates;
        }
    }

    public void UpdateUpdatesBeingProcessed(String pluginName, TreeMap<Integer, Integer> summaryUpdates)
    {
        synchronized (newUpdatesBeingProcessed)
        {
            newUpdatesBeingProcessed.put(pluginName, summaryUpdates);
        }
    }

    public void UpdateDownloadProgress(int progress, String pluginName)
    {
        for(int i=0; i < pluginInfo.size(); i++)
        {
            if(pluginInfo.get(i).GetName().equals(pluginName))
            {
                downloadProgresses.set(i, progress);
                i = pluginInfo.size();
            }
        }
        setChanged();
        notifyObservers();
    }

    public void UpdateProcessorProgress(int progress, String pluginName)
    {
        for(int i=0; i < pluginInfo.size(); i++)
        {
            if(pluginInfo.get(i).GetName().equals(pluginName))
            {
                processorProgresses.set(i, progress);
                i = pluginInfo.size();
            }
        }
        setChanged();
        notifyObservers();
    }

    public void UpdateIndicesProgress(int progress, String pluginName)
    {
        for(int i=0; i < pluginInfo.size(); i++)
        {
            if(pluginInfo.get(i).GetName().equals(pluginName))
            {
                indicesProgresses.set(i, progress);
                i = pluginInfo.size();
            }
        }
        setChanged();
        notifyObservers();
    }

    public void UpdateSummaryProgress(int progress, String pluginName)
    {
        for(int i=0; i < pluginInfo.size(); i++)
        {
            if(pluginInfo.get(i).GetName().equals(pluginName))
            {
                summaryProgresses.set(i, progress);
                i = pluginInfo.size();
            }
        }
        setChanged();
        notifyObservers();
    }

    public void UpdateSchedulerTaskState(TaskState state)
    {
        this.state = state;
        setChanged();
        notifyObservers();
    }

    public void AddToLog(String logText)
    {
        log.add(logText);
        setChanged();
        notifyObservers();
    }
}
