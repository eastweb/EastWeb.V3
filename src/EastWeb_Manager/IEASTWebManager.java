package EastWeb_Manager;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import EastWeb_Database.DatabaseConnection;
import EastWeb_Downloader.DownloadFactory;
import EastWeb_Downloader.LocalDownloader;
import EastWeb_ProcessWorker.ProcessWorkerReturn;
import EastWeb_Scheduler.SchedulerStatus;
/**
 * @author michael.devos
 *
 */
public interface IEASTWebManager {

    /**
     * Background thread execution.
     */
    public void run();

    /**
     * Updates the local SchedulerStatus listing and sets a boolean flag to signal that the UI needs to be updated.
     *
     * @param updatedStatus  - updated SchedulerStatus that contains the unique SchedulerID to use to map the status to its associated Scheduler instance
     */
    public void NotifyUI(SchedulerStatus updatedStatus);

    /**
     * Creates a {@link version2.prototype.download#DownloadFactory DownloadFactory} using the given factory and schedules it to run daily starting now.
     * GlobalDownloader objects are singletons and are shared amongst Schedulers. If the GlobalDownloader associated pluginName is not currently
     * associated with an existing GlobalDownloader then the given one is stored and added to the running list of them.
     *
     * @param dlFactory  - factory to use to create the GlobalDownloader
     * @return LocalDownloader if specified to create one, otherwise NULL
     * @throws IOException
     */
    public LocalDownloader StartGlobalDownloader(DownloadFactory dlFactory) throws IOException;

    /**
     * Requests that the given {@link version2#ProcesWorker ProcesWorker} be managed and executed.
     *
     * @param worker  - {@link java.util.concurrent.Callable Callable} to execute on a separate available thread
     * @return Future object representing the return object of the submitted ProcessWorker which is of type ProcessWorkerReturn
     */
    public Future<ProcessWorkerReturn> StartNewProcessWorker(Callable<ProcessWorkerReturn> worker);

    /**
     * Gets a database connection from the connection pool.
     * @return DatabseConnection instance to use
     */
    public DatabaseConnection GetConnection();

}