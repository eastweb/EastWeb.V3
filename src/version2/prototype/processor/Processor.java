package version2.prototype.processor;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import version2.prototype.Process;
import version2.prototype.ThreadState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.summary.SummaryWorker;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GeneralUIEventObject;

public class Processor<V> extends Process<V> {
    private ArrayList<ProcessorWorker> workers;

    protected Processor(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            ThreadState state, String inputTableName, ExecutorService executor)
    {
        super(projectInfoFile, pluginInfo, pluginMetaData, scheduler, state, ProcessName.PROCESSOR, inputTableName, executor);
        workers = new ArrayList<ProcessorWorker>(0);
    }

    @Override
    public V call() throws Exception {
        ProcessorWorker worker;

        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles = DatabaseCache.GetAvailableFiles(projectInfoFile.GetProjectName(), pluginInfo.GetName(), mInputTableName);

        if(cachedFiles.size() > 0)
        {
            worker = new ProcessorWorker(this, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles);
            workers.add(worker);
            executor.submit(worker);
        }

        // TODO: Need to define when "finished" state has been reached as this doesn't work with asynchronous.
        scheduler.NotifyUI(new GeneralUIEventObject(this, "Summary Finished", 100));
        return null;
    }

}
