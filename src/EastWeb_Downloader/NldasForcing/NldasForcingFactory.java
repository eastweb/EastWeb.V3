package EastWeb_Downloader.NldasForcing;

import java.io.IOException;
import java.time.LocalDate;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_Downloader.DownloadFactory;
import EastWeb_Downloader.DownloaderFactory;
import EastWeb_Downloader.ListDatesFiles;
import EastWeb_Downloader.LocalStorageDownloadFactory;
import EastWeb_Scheduler.Scheduler;
import PluginMetaData.DownloadMetaData;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;
import Utilies.DataDate;

/**
 * @author michael.devos
 *
 */
public final class NldasForcingFactory extends DownloadFactory {

    public NldasForcingFactory(Config configInstance, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, DownloadMetaData downloadMetaData, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache, LocalDate startDate) {
        super(configInstance, projectInfoFile, pluginInfo, downloadMetaData, pluginMetaData, scheduler, outputCache,  startDate);
    }

    @Override
    public DownloaderFactory CreateDownloaderFactory(ListDatesFiles listDatesFiles) {
        return new LocalStorageDownloadFactory(configInstance, "NldasForcingDownloader", projectInfoFile, pluginInfo, downloadMetaData, pluginMetaData, scheduler, outputCache, listDatesFiles, startDate);
    }

    @Override
    public ListDatesFiles CreateListDatesFiles() throws IOException {
        return new NldasForcingListDatesFiles(new DataDate(startDate), downloadMetaData, projectInfoFile);
    }
}
