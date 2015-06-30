package version2.prototype;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.util.DataFileMetaData;

/**
 * Abstract framework worker class. Frameworks are to use a concrete class that extends this class to handle doing their required processing work.
 *
 * @author michael.devos
 *
 * @param <V>  - return type of the Callable
 */
public abstract class ProcessWorker implements Callable<ProcessWorkerReturn> {
    public String processWorkerName;
    protected Process process;
    protected ProjectInfoFile projectInfoFile;
    protected ProjectInfoPlugin pluginInfo;
    protected PluginMetaData pluginMetaData;
    protected ArrayList<DataFileMetaData> cachedFiles;

    /**
     * Creates a ProcessWorker object labeled by the given processWorkerName, owned by the given process, and set to work on the given cachedFiles.
     *
     * @param processWorkerName  - name of this worker
     * @param process  - reference to the owning process object
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param cachedFiles  - the files to process
     */
    protected ProcessWorker(String processWorkerName, Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles)
    {
        this.processWorkerName = processWorkerName;
        this.process = process;
        this.projectInfoFile = projectInfoFile;
        this.pluginInfo = pluginInfo;
        this.pluginMetaData = pluginMetaData;
        this.cachedFiles = cachedFiles;
    }
}
