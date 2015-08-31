package version2.prototype.summary;

import java.util.ArrayList;

import version2.prototype.Config;
import version2.prototype.EASTWebManagerI;
import version2.prototype.Process;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;

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
     * @param manager  - EASTWebManager reference to use and pass on
     * @param configInstance  - Config reference to use and pass on
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param scheduler  - reference to the controlling Scheduler object
     * @param inputCache
     * @param outputCache
     */
    public Summary(EASTWebManagerI manager, Config configInstance, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache inputCache, DatabaseCache outputCache)
    {
        super(manager, configInstance, ProcessName.SUMMARY, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
        inputCache.addObserver(this);
    }

    @Override
    public void process(ArrayList<DataFileMetaData> cachedFiles) {
        manager.StartNewProcessWorker(new SummaryWorker(configInstance, this, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache));
    }

}