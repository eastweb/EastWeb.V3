/**
 *
 */
package version2.prototype.download;

import java.io.IOException;
import java.time.LocalDate;

import version2.prototype.Config;
import version2.prototype.EASTWebManager;
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
public abstract class DownloadFactory {
    protected final EASTWebManager manager;
    protected final Config configInstance;
    protected final ProjectInfoFile projectInfoFile;
    protected final ProjectInfoPlugin pluginInfo;
    protected final PluginMetaData pluginMetaData;
    protected final Scheduler scheduler;
    protected final DatabaseCache outputCache;
    protected final LocalDate startDate;
    protected final DownloadMetaData dData;

    protected DownloadFactory(EASTWebManager manager, Config configInstance, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache outputCache, LocalDate startDate, DownloadMetaData dData) {
        this.manager = manager;
        this.configInstance = configInstance;
        this.projectInfoFile = projectInfoFile;
        this.pluginInfo = pluginInfo;
        this.pluginMetaData = pluginMetaData;
        this.scheduler = scheduler;
        this.outputCache = outputCache;
        this.startDate = startDate;
        this.dData = dData;
    }

    public abstract DownloaderFactory CreateDownloadFactory(ListDatesFiles listDatesFiles);
    public abstract ListDatesFiles CreateListDatesFiles() throws IOException;
}
