package version2.prototype.Scheduler;

import java.util.concurrent.Callable;

import version2.prototype.ProcessWorker;
import version2.prototype.ProcessWorkerReturn;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.util.GeneralUIEventObject;

/**
 * Handles monitoring the status of running workers.
 * @author michael.devos
 */
public class SchedulerWorker implements Callable<ProcessWorkerReturn> {
    private final ProcessWorker worker;
    private final SchedulerStatusContainer statusContainer;
    private final Scheduler scheduler;

    /**
     * Creates a SchedulerWorker that will run the given Callable once executed.
     * @param worker
     * @param statusContainer
     * @param scheduler
     */
    public SchedulerWorker(ProcessWorker worker, SchedulerStatusContainer statusContainer, Scheduler scheduler)
    {
        this.worker = worker;
        this.statusContainer = statusContainer;
        this.scheduler = scheduler;
        synchronized(statusContainer) {
            statusContainer.AddWorker(worker.process.processName);
        }
    }

    public ProjectInfoFile GetProjectInfo()
    {
        return worker.projectInfoFile;
    }

    public String GetWorkerName()
    {
        return worker.processWorkerName;
    }

    @Override
    public ProcessWorkerReturn call() throws Exception {
        ProcessWorkerReturn theReturn = null;

        synchronized(statusContainer) {
            statusContainer.AddActiveWorker(worker.process.processName);
        }
        scheduler.NotifyUI(new GeneralUIEventObject(this, null));

        String oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(oldThreadName + "-" + worker. projectInfoFile.GetProjectName() + "-" + worker.processWorkerName + "");
        theReturn = worker.call();
        Thread.currentThread().setName(oldThreadName + "-Updating-Scheduler-Status");

        synchronized(statusContainer) {
            statusContainer.SubtractActiveWorker(worker.process.processName);
            statusContainer.SubtractWorker(worker.process.processName);
        }
        scheduler.NotifyUI(new GeneralUIEventObject(this, null));

        Thread.currentThread().setName(oldThreadName);
        return theReturn;
    }

}