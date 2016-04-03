/**
 *
 */
package EastWeb_Downloader.ModisLST;

import java.io.IOException;
import java.time.LocalDate;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_Downloader.DownloadFactory;
import EastWeb_Downloader.DownloaderFactory;
import EastWeb_Downloader.ListDatesFiles;
import EastWeb_Downloader.ModisDownloadUtils.ModisLocalStorageDownloadFactory;
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
public class ModisLSTFactory extends DownloadFactory {

    public ModisLSTFactory(Config configInstance, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, DownloadMetaData downloadMetaData, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache, LocalDate startDate) {
        super(configInstance, projectInfoFile, pluginInfo, downloadMetaData, pluginMetaData, scheduler, outputCache, startDate);
    }

    /* (non-Javadoc)
     * @see version2.prototype.download.DownloadFactory#CreateDownloadFactory()
     */
    @Override
    public DownloaderFactory CreateDownloaderFactory(ListDatesFiles listDatesFiles) {
        return new ModisLocalStorageDownloadFactory(configInstance, "ModisLSTDownloader", projectInfoFile, pluginInfo, downloadMetaData, pluginMetaData, scheduler, outputCache, listDatesFiles, startDate);
    }

    /* (non-Javadoc)
     * @see version2.prototype.download.DownloadFactory#CreateListDatesFiles(version2.prototype.DataDate, version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData)
     */
    @Override
    public ListDatesFiles CreateListDatesFiles() throws IOException {
        return new ModisLSTListDatesFiles(new DataDate(startDate), downloadMetaData, projectInfoFile);
    }

}
