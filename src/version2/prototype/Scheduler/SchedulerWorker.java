package version2.prototype.Scheduler;

import java.util.concurrent.Callable;

import version2.prototype.ProcessWorker;
import version2.prototype.ProcessWorkerReturn;
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

    @Override
    public ProcessWorkerReturn call() throws Exception {
        ProcessWorkerReturn theReturn = null;
        synchronized(statusContainer) {
            statusContainer.AddActiveWorker(worker.process.processName);
        }
        theReturn = worker.call();
        synchronized(statusContainer) {
            statusContainer.SubtractActiveWorker(worker.process.processName);
            statusContainer.SubtractWorker(worker.process.processName);
        }
        scheduler.NotifyUI(new GeneralUIEventObject(this, null));
        return theReturn;
    }

}
