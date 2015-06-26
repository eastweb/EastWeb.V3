package version2.prototype.Scheduler;

import java.util.ArrayList;

import version2.prototype.ThreadState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;

/**
 * Records the status of a Scheduler instance at the time of SchedulerStatus creation.
 *
 * @author michael.devos
 *
 */
public class SchedulerStatus {
    public final int schedulerID;
    public final String projectName;
    public final ArrayList<ProjectInfoPlugin> pluginInfo;
    public final ArrayList<Integer> downloadProgresses;
    public final ArrayList<Integer> processorProgresses;
    public final ArrayList<Integer> indiciesProgresses;
    public final ArrayList<Integer> summaryProgresses;
    public final ArrayList<String> log;
    public final ThreadState schedulerState;


    public SchedulerStatus(int schedulerID, String projectName, ArrayList<ProjectInfoPlugin> pluginInfo, ArrayList<Integer> downloadProgresses,
            ArrayList<Integer> processorProgresses, ArrayList<Integer> indiciesProgresses, ArrayList<Integer> summaryProgresses, ArrayList<String> log,
            ThreadState schedulerState)
    {
        this.schedulerID = schedulerID;
        this.projectName = projectName;
        this.pluginInfo = pluginInfo;
        this.downloadProgresses = downloadProgresses;
        this.processorProgresses = processorProgresses;
        this.indiciesProgresses = indiciesProgresses;
        this.summaryProgresses = summaryProgresses;
        this.log = log;
        this.schedulerState = schedulerState;
    }
}
