package version2.prototype.download;

import java.util.Observable;

import version2.prototype.Process;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DatabaseCache;

/**
 * @author michael.devos
 *
 */
public abstract class LocalDownloader extends Process {

    protected LocalDownloader(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, TaskState state, ProcessName processName, DatabaseCache outputCache) {
        super(projectInfoFile, pluginInfo, pluginMetaData, scheduler, state, processName, outputCache);
        // TODO Auto-generated constructor stub
    }

    public abstract GlobalDownloader CreateGlobalDownloader();

    @Override
    public void update(Observable o, Object arg) {
        if(arg instanceof DatabaseCache)
        {
            DatabaseCache state = (DatabaseCache) arg;

        }
        super.update(o, arg);
    }
}
