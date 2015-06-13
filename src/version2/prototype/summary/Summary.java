package version2.prototype.summary;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import version2.prototype.Process;
import version2.prototype.ThreadState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GeneralUIEventObject;

/**
 * The custom Summary framework, Process extending class. Manages SummaryWorker objects.
 *
 * @author michael.devos
 *
 */
public class Summary extends Process<Void> {
    private TemporalSummaryRasterFileStore fileStore;
    private ArrayList<Future<Void>> futures;

    /**
     * Creates a Summary object with the defined initial ThreadState, owned by the given Scheduler, and acquiring its input from the specified process,
     * inputProcessName.
     *
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param scheduler  - reference to the controlling Scheduler object
     * @param state  - ThreadState to initialize this object to
     * @param inputProcessName  - name of process to use the output of for its input
     * @param executor  - executor service to use to spawn worker threads
     */
    public Summary(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler, ThreadState state,
            ProcessName inputProcessName, ExecutorService executor)
    {
        super(projectInfoFile, pluginInfo, pluginMetaData, scheduler, state, ProcessName.SUMMARY, inputProcessName, executor);
        futures = new ArrayList<Future<Void>>(0);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Void call() throws Exception {
        // Custom to Summary
        Class<?> strategyClass = Class.forName(pluginMetaData.Summary.CompositionStrategyClassName);
        Constructor<?> ctorStrategy = strategyClass.getConstructor();
        TemporalSummaryCompositionStrategy tempSummaryCompStrategy = (TemporalSummaryCompositionStrategy)ctorStrategy.newInstance();
        fileStore = new TemporalSummaryRasterFileStore(tempSummaryCompStrategy);

        // General get data files
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles = DatabaseCache.GetAvailableFiles(projectInfoFile.GetProjectName(), pluginInfo.GetName(), inputProcessName);
        if(cachedFiles.size() > 0)
        {
            if(mState == ThreadState.RUNNING)
            {
                futures.add(executor.submit(new SummaryWorker(this, projectInfoFile, pluginInfo, pluginMetaData, fileStore, cachedFiles)));
            }
        }

        // TODO: Need to define when "finished" state has been reached as this doesn't work with asynchronous.
        scheduler.NotifyUI(new GeneralUIEventObject(this, "Summary Finished", 100));
        return null;
    }

}