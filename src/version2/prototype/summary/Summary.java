package version2.prototype.summary;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observable;

import version2.prototype.ConfigReadException;
import version2.prototype.EASTWebManager;
import version2.prototype.Process;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GeneralUIEventObject;

/**
 * The custom Summary framework, Process extending class. Manages SummaryWorker objects.
 *
 * @author michael.devos
 *
 */
public class Summary extends Process {

    /**
     * Creates a Summary object with the defined initial TaskState, owned by the given Scheduler, and acquiring its input from the specified
     * process, inputProcessName.
     *
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param scheduler  - reference to the controlling Scheduler object
     * @param state  - TaskState to initialize this object to
     * @param inputProcessName  - name of process to use the output of for its input
     * @param executor  - executor service to use to spawn worker threads
     */
    public Summary(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler)
    {
        super(projectInfoFile, pluginInfo, pluginMetaData, scheduler, ProcessName.SUMMARY, null);
    }

    @Override
    public void start() {

    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        super.update(o, arg);
        if(arg instanceof DatabaseCache)
        {
            ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
            DatabaseCache inputCache = (DatabaseCache) arg;
            try {
                cachedFiles = inputCache.GetUnprocessedCacheFiles();

                if(cachedFiles.size() > 0)
                {
                    if(scheduler.GetSchedulerStatus().GetState() == TaskState.RUNNING)
                    {
                        EASTWebManager.StartNewProcessWorker(new SummaryWorker(this, projectInfoFile, pluginInfo, pluginMetaData,
                                cachedFiles, outputCache));
                    }
                }

                // TODO: Need to define when "finished" state has been reached as this doesn't work with asynchronous.
                scheduler.NotifyUI(new GeneralUIEventObject(this, "Summary Finished", 100, pluginInfo.GetName()));
            }
            catch (ConfigReadException | ClassNotFoundException | SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}