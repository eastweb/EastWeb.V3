package version2.prototype.processor;

import java.util.ArrayList;

import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.util.DataFileMetaData;


/**
 * An implementation of a ProcessWorker to handle the work for the Processor framework and to be used by a Process extending class.
 *
 * @author michael.devos
 *
 */
public class ProcessorWorker extends ProcessWorker<Void> {

    /**
     * An implementation of ProcessWorker that handles the major processing of a list of raster files after being downloaded and handled by the Download framework.
     * Output is used by the Indices framework. Meant to be ran on its own thread.
     *
     * @param process  - the parent Process object to this threaded worker.
     * @param projectInfoFile  - information about the project gotten from the project's info xml.
     * @param pluginInfo  - information about the plugin being used for the acquired data files.
     * @param pluginMetaData  - information relevant to this ProcessWorker about the plugin being used gotten from the plugin's info xml.
     * @param cachedFiles  - the list of files to process in this ProcessWorker.
     */
    protected ProcessorWorker(Process<?> process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles)
    {
        super("ProcessorWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Void call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}
