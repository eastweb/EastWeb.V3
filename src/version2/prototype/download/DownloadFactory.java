/**
 *
 */
package version2.prototype.download;

import java.io.IOException;

import version2.prototype.DataDate;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DatabaseCache;

/**
 * @author michael.devos
 *
 */
public interface DownloadFactory {
    public LocalDownloader CreateLocalDownloader(int myID, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache outputCache);
    public GlobalDownloader CreateGlobalDownloader(int myID, String pluginName, TaskState initialState, DownloadMetaData metaData, ListDatesFiles listDatesFiles);
    public ListDatesFiles CreateListDatesFiles(DataDate startDate, DownloadMetaData data) throws IOException;
}
