package version2.prototype.download;

import java.util.ArrayList;

import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.ProcessWorkerReturn;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;


/**
 * An implementation of a ProcessWorker to handle the work for the Download framework and to be used by a Process extending class.
 *
 * @author michael.devos
 *
 */
public class DownloadWorker extends ProcessWorker {
    /**
     * An implementation of ProcessWorker that handles the "caching" of a list of raster files after being handled by the global downloaders. Does not download
     * files from the server. Only sets up a listing of files that have been downloaded and are available to be processed by the other frameworks for the running
     * project and plugin. Output used by the Processor framework. Meant to be ran on its own thread.
     *
     * @param process  - the parent Process object to this threaded worker.
     * @param projectInfoFile  - information about the project gotten from the project's info xml.
     * @param pluginInfo  - information about the plugin being used for the acquired data files.
     * @param pluginMetaData  - information relevant to this ProcessWorker about the plugin being used gotten from the plugin's info xml.
     * @param cachedFiles  - the list of files to process in this ProcessWorker.
     */
    public DownloadWorker(Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles, DatabaseCache outputCache)
    {
        super("DownloadWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ProcessWorkerReturn call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}
