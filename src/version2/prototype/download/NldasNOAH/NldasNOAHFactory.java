package version2.prototype.download.NldasNOAH;

import java.io.IOException;

import version2.prototype.DataDate;
import version2.prototype.EASTWebManager;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.download.DownloadFactory;
import version2.prototype.download.DownloaderFactory;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.LocalStorageDownloadFactory;
import version2.prototype.util.DatabaseCache;

/**
 * @author michael.devos
 *
 */
public class NldasNOAHFactory extends DownloadFactory {

    protected NldasNOAHFactory(EASTWebManager manager, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache outputCache, DataDate startDate, DownloadMetaData dData) {
        super(manager, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache, startDate, dData);
    }

    @Override
    public DownloaderFactory CreateDownloadFactory(ListDatesFiles listDatesFiles) {
        return new LocalStorageDownloadFactory(manager, "NldasNOAHDownloader", projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache, listDatesFiles);
    }

    @Override
    public ListDatesFiles CreateListDatesFiles() throws IOException {
        return new NldasNOAHListDatesFiles(startDate, dData);
    }
}
