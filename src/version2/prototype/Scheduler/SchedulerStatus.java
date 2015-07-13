package version2.prototype.Scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import version2.prototype.TaskState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;

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

    private List<Integer> downloadProgresses;
    private List<Integer> processorProgresses;
    private List<Integer> indicesProgresses;
    private List<Integer> summaryProgresses;
    private List<String> log;
    private TaskState state;

    public SchedulerStatus(int schedulerID, String projectName, ArrayList<ProjectInfoPlugin> pluginInfo, TaskState state)
    {
        this.schedulerID = schedulerID;
        this.projectName = projectName;
        this.pluginInfo = pluginInfo;
        downloadProgresses = Collections.synchronizedList(new ArrayList<Integer>(1));
        processorProgresses = Collections.synchronizedList(new ArrayList<Integer>(1));
        indicesProgresses = Collections.synchronizedList(new ArrayList<Integer>(1));
        summaryProgresses = Collections.synchronizedList(new ArrayList<Integer>(1));
        log = Collections.synchronizedList(new ArrayList<String>(1));
        this.state = state;
    }

    public List<Integer> GetDownloadProgress() { synchronized (downloadProgresses) {return downloadProgresses; } }

    public List<Integer> GetProcessorProgress() { synchronized (processorProgresses) {return processorProgresses; } }

    public List<Integer> GetIndicesProgress() { synchronized (indicesProgresses) {return indicesProgresses; } }

    public List<Integer> GetSummaryProgress() { synchronized (summaryProgresses) {return summaryProgresses; } }

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
