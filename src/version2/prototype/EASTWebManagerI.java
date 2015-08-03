package version2.prototype;

import java.io.IOException;
import java.util.concurrent.Future;

import version2.prototype.Scheduler.Scheduler;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.LocalDownloader;

public interface EASTWebManagerI {

    public void run();

    /**
     * Updates the local SchedulerStatus listing and sets a boolean flag to signal that the UI needs to be updated.
     *
     * @param updatedScheduler  - reference to the Scheduler instance for whom the stored SchedulerStatus object is now out of date
     */
    public void NotifyUI(Scheduler updatedScheduler);

    /**
     * Creates a {@link version2.download#GlobalDownloader GlobalDownloader} using the given factory and schedules it to run daily starting now.
     * GlobalDownloader objects are singletons and are shared amongst Schedulers. If the GlobalDownloader associated pluginName is not currently
     * associated with an existing GlobalDownloader then the given one is stored and added to the running list of them.
     *
     * @param dlFactory  - factory to use to create the GlobalDownloader
     * @param createLocalDownloader  - True if the factory should be used to create a LocalDownloader, set it to observe on the created GlobalDownloader, and return it.
     * @return LocalDownloader if specified to create one, otherwise NULL
     * @throws IOException
     */
    public LocalDownloader StartGlobalDownloader(DownloadFactory dlFactory)
            throws IOException;

    /**
     * Requests that a {@link version2.download#GlobalDownloader GlobalDownloader} with the specified unique ID to have its
     * {@link version2#TaskState TaskState} set to STOPPED and to cease execution. If other GlobalDownloaders are waiting to execute then the next
     * oldest one is started up.
     *
     * @param gdlID  - targeted GlobalDownloader's ID
     */
    public void StopGlobalDownloader(int gdlID);

    /**
     * Requests that a {@link version2.download#GlobalDownloader GlobalDownloader} with the specified unique ID to have its
     * {@link version2#TaskState TaskState} set to RUNNING and to continue downloading new data files when its given a turn to run again.
     *
     * @param gdlID  - targeted GlobalDownloader's ID
     */
    public void StartExistingGlobalDownloader(int gdlID);

    /**
     * Requests that the given {@link version2#ProcesWorker ProcesWorker} be managed and executed.
     *
     * @param worker  - {@link version2#ProcesWorker ProcesWorker} to execute on a separate available thread
     * @return Future object representing the return object of the submitted ProcessWorker which is of type ProcessWorkerReturn
     */
    public Future<ProcessWorkerReturn> StartNewProcessWorker(
            ProcessWorker worker);

}