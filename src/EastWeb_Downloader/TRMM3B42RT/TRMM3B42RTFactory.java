/**
 *
 */
package EastWeb_Downloader.TRMM3B42RT;

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
public class TRMM3B42RTFactory extends DownloadFactory {

    public TRMM3B42RTFactory(Config configInstance, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, DownloadMetaData downloadMetaData, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache, LocalDate startDate) {
        super(configInstance, projectInfoFile, pluginInfo, downloadMetaData, pluginMetaData, scheduler, outputCache, startDate);
    }

    @Override
    public DownloaderFactory CreateDownloaderFactory(ListDatesFiles listDatesFiles) {
        return new LocalStorageDownloadFactory(configInstance, "TRMM3B42RTDownloader", projectInfoFile, pluginInfo, downloadMetaData, pluginMetaData, scheduler, outputCache, listDatesFiles, startDate);
    }

    @Override
    public ListDatesFiles CreateListDatesFiles() throws IOException {
        return new TRMM3B42RTListDatesFiles(new DataDate(startDate), downloadMetaData, projectInfoFile);
    }
}
