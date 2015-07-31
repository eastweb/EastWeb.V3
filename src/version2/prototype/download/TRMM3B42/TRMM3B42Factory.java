package version2.prototype.download.TRMM3B42;

import java.io.IOException;
import java.time.LocalDate;

import version2.prototype.Config;
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
import version2.prototype.download.TRMM3B42RT.TRMM3B42RTListDatesFiles;
import version2.prototype.util.DatabaseCache;

public class TRMM3B42Factory extends DownloadFactory {

    public TRMM3B42Factory(EASTWebManager manager, Config configInstance, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache outputCache, LocalDate startDate, DownloadMetaData dData) {
        super(manager, configInstance, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache, startDate, dData);
    }

    @Override
    public DownloaderFactory CreateDownloadFactory(ListDatesFiles listDatesFiles) {
        return new LocalStorageDownloadFactory(manager, configInstance, "TRMM3B42Downloader", projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache, listDatesFiles,
                startDate);
    }

    @Override
    public ListDatesFiles CreateListDatesFiles() throws IOException {
        return new TRMM3B42ListDatesFiles(new DataDate(startDate), dData);
    }
}
