/**
 *
 */
package test.Scheduler;

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
 * @author michael.devos
 *
 */
public final class IndicesWorkerTest extends ProcessWorker {

    protected IndicesWorkerTest(String processWorkerName, Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            ArrayList<DataFileMetaData> cachedFiles, DatabaseCache outputCache) {
        super("IndicesWorkerTest", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ProcessWorkerReturn call() throws Exception {
        System.out.println("IndicesWorkerTest executed.");
        return null;
    }

}
