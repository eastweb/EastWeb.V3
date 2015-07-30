/**
 *
 */
package version2.prototype.download.ModisNBAR;

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
import version2.prototype.util.DatabaseCache;

/**
 * @author michael.devos
 *
 */
public class ModisNBARFactory extends DownloadFactory {

    protected ModisNBARFactory(EASTWebManager manager, Config configInstance, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache outputCache, LocalDate startDate, DownloadMetaData dData) {
        super(manager, configInstance, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache, startDate, dData);
    }

    /* (non-Javadoc)
     * @see version2.prototype.download.DownloadFactory#CreateDownloadFactory()
     */
    @Override
    public DownloaderFactory CreateDownloadFactory(ListDatesFiles listDatesFiles) {
        return new LocalStorageDownloadFactory(manager, configInstance, "ModisNBARDownloader", projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache, listDatesFiles,
                startDate);
    }

    /* (non-Javadoc)
     * @see version2.prototype.download.DownloadFactory#CreateListDatesFiles(version2.prototype.DataDate, version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData)
     */
    @Override
    public ListDatesFiles CreateListDatesFiles() throws IOException {
        return new ModisNBARListDatesFiles(new DataDate(startDate), dData);
    }

}
