/**
 *
 */
package EastWeb_Downloader;

import java.io.IOException;
import java.time.LocalDate;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_Scheduler.Scheduler;
import PluginMetaData.DownloadMetaData;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;



/**
 * @author michael.devos
 *
 */
public abstract class DownloadFactory {
    /**
     * Description of the data to be handled by the created GlobalDownloader and LocalDownloaders.
     */
    public final DownloadMetaData downloadMetaData;

    public final Config configInstance;
    public final ProjectInfoFile projectInfoFile;
    public final ProjectInfoPlugin pluginInfo;
    public final PluginMetaData pluginMetaData;
    public final LocalDate startDate;

    protected final Scheduler scheduler;
    protected final DatabaseCache outputCache;

    protected DownloadFactory(Config configInstance, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, DownloadMetaData downloadMetaData, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache, LocalDate startDate) {
        this.configInstance = configInstance;
        this.projectInfoFile = projectInfoFile;
        this.pluginInfo = pluginInfo;
        this.downloadMetaData = downloadMetaData;
        this.pluginMetaData = pluginMetaData;
        this.scheduler = scheduler;
        this.outputCache = outputCache;
        this.startDate = startDate;
    }

    /**
     * Creates the ListDatesFiles object to be used within the DownloaderFactory.
     * @return custom ListDatesFiles object
     * @throws IOException
     */
    public abstract ListDatesFiles CreateListDatesFiles() throws IOException;

    /**
     * Creates the DownloaderFactory to use for creating the LocalDownloader and GlobalDownloader for the plugin.
     * @param listDatesFiles
     * @return custom DownloaderFactory object
     */
    public abstract DownloaderFactory CreateDownloaderFactory(ListDatesFiles listDatesFiles);
}
