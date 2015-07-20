package version2.prototype.download.NldasForcing;

import java.io.IOException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.GenericLocalDownloader;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.LocalDownloader;
import version2.prototype.util.DatabaseCache;

public class NldasForcingFactory implements DownloadFactory {

    @Override
    public LocalDownloader CreateLocalDownloader(int globalDLID,
            ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache outputCache) {
        return new GenericLocalDownloader(globalDLID, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
    }

    @Override
    public GlobalDownloader CreateGlobalDownloader(int myID, String pluginName,
            DownloadMetaData metaData, ListDatesFiles listDatesFiles) {
        return new NldasForcingGlobalDownloader(myID, "NldasForcing",  metaData, listDatesFiles);
    }

    @Override
    public ListDatesFiles CreateListDatesFiles(DataDate startDate,
            DownloadMetaData data) throws IOException {
        return new NldasForcingListDatesFiles(startDate, data);
    }

}
