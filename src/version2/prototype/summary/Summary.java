package version2.prototype.summary;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

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

public class Summary<V> extends Process<V> {
    private TemporalSummaryRasterFileStore fileStore;
    private ArrayList<SummaryWorker> workers;

    public Summary(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, ThreadState state, ProcessName processName, String inputTableName, ExecutorService executor)
    {
        super(projectInfoFile, pluginInfo, pluginMetaData, scheduler, state, processName, inputTableName, executor);
        workers = new ArrayList<SummaryWorker>(0);
    }

    @Override
    public V call() throws Exception {
        SummaryWorker worker;

        // Custom to Summary
        Class<?> strategyClass = Class.forName(pluginMetaData.Summary.CompositionStrategyClassName);
        Constructor<?> ctorStrategy = strategyClass.getConstructor();
        TemporalSummaryCompositionStrategy tempSummaryCompStrategy = (TemporalSummaryCompositionStrategy)ctorStrategy.newInstance();
        fileStore = new TemporalSummaryRasterFileStore(tempSummaryCompStrategy);

        // General get data files
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles = DatabaseCache.GetAvailableFiles(projectInfoFile.GetProjectName(), pluginInfo.GetName(), mInputTableName);
        if(cachedFiles.size() > 0)
        {
            worker = new SummaryWorker(this, projectInfoFile, pluginInfo, pluginMetaData, fileStore, cachedFiles);
            workers.add(worker);
            executor.submit(worker);
        }

        // TODO: Need to define when "finished" state has been reached as this doesn't work with asynchronous.
        scheduler.NotifyUI(new GeneralUIEventObject(this, "Summary Finished", 100));
        return null;
    }

}