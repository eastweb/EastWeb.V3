package version2.prototype;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GeneralUIEventObject;

/**
 * A generic concrete Process extending class. Can be used in place of a custom built framework Process class to handle managing worker threads for the framework.
 *
 * @author michael.devos
 *
 * @param <ReturnType>  - return type of the Callable
 * @param <WorkerType>  - the ProcessWorker extending class the spawned worker threads should be created as
 */
public class GenericFrameworkProcess<ReturnType, WorkerType extends ProcessWorker<ReturnType>> extends Process<ReturnType> {
    private ArrayList<Future<ReturnType>> futures;
    private Class<?> processWorkerClass;

    /**
     * Creates a GenericFrameworkProcess object with the defined initial ThreadState, owned by the given Scheduler, labeled by the given processName, and acquiring
     * its input from the specified process (inputProcessName).
     *
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param scheduler  - reference to the controlling Scheduler object
     * @param state  - ThreadState to initialize this object to
     * @param processName  - name of this threaded process
     * @param inputProcessName  - name of process to use the output of for its input
     * @param executor  - executor service to use to spawn worker threads
     */
    public GenericFrameworkProcess(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            ThreadState state, ProcessName processName, ProcessName inputProcessName, ExecutorService executor)
    {
        super(projectInfoFile, pluginInfo, pluginMetaData, scheduler, state, processName, inputProcessName, executor);
        futures = new ArrayList<Future<ReturnType>>(0);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @SuppressWarnings("unchecked")
    @Override
    public ReturnType call() throws Exception {
        // General get data files
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles = DatabaseCache.GetAvailableFiles(projectInfoFile.GetProjectName(), pluginInfo.GetName(), inputProcessName);
        if(cachedFiles.size() > 0)
        {
            if(mState == ThreadState.RUNNING)
            {
                Constructor<?> cstr = processWorkerClass.getConstructor(Process.class, ProjectInfoFile.class, ProjectInfoPlugin.class, PluginMetaData.class,
                        ArrayList.class);
                futures.add(executor.submit((WorkerType) cstr.newInstance(this, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles)));
            }
        }

        // TODO: Need to define when "finished" state has been reached as this doesn't work with asynchronous.
        String name = null;
        switch(processName)
        {
        case DOWNLOAD: name = "Download"; break;
        case INDICES: name = "Indices"; break;
        case PROCESSOR: name = "Processor"; break;
        case SUMMARY: name = "Summary"; break;
        }
        scheduler.NotifyUI(new GeneralUIEventObject(this, name + " Finished", 100));
        return null;
    }

}
