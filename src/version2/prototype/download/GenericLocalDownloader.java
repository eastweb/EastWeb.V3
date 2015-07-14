/**
 *
 */
package version2.prototype.download;

import java.util.ArrayList;

import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;

/**
 * @author michael.devos
 *
 */
public class GenericLocalDownloader extends LocalDownloader {

    public GenericLocalDownloader(int globalDLID, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache outputCache) {
        super(globalDLID, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
    }

    @Override
    public void process(ArrayList<DataFileMetaData> cachedFiles) {
        // TODO Auto-generated method stub

    }

}
