/**
 *
 */
package version2.prototype.download;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.EASTWebManager;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DatabaseCache;

/**
 * @author michael.devos
 *
 */
public final class LocalStorageDownloadFactory implements DownloaderFactory {
    private final EASTWebManager manager;
    private final String downloaderClassName;
    private final ProjectInfoFile projectInfoFile;
    private final ProjectInfoPlugin pluginInfo;
    private final PluginMetaData pluginMetaData;
    private final Scheduler scheduler;
    private final DatabaseCache outputCache;
    private final ListDatesFiles listDatesFiles;

    /**
     * @param manager
     * @param downloaderClassName
     * @param projectInfoFile
     * @param pluginInfo
     * @param pluginMetaData
     * @param scheduler
     * @param outputCache
     * @param listDatesFiles
     */
    public LocalStorageDownloadFactory(EASTWebManager manager, String downloaderClassName, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache, ListDatesFiles listDatesFiles)
    {
        this.manager = manager;
        this.downloaderClassName = downloaderClassName;
        this.projectInfoFile = projectInfoFile;
        this.pluginInfo = pluginInfo;
        this.pluginMetaData = pluginMetaData;
        this.scheduler = scheduler;
        this.outputCache = outputCache;
        this.listDatesFiles = listDatesFiles;
    }

    /* (non-Javadoc)
     * @see version2.prototype.download.DownloaderFactory#CreateLocalDownloader(int, version2.prototype.ProjectInfoMetaData.ProjectInfoFile, version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin, version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData, version2.prototype.Scheduler.Scheduler, version2.prototype.util.DatabaseCache)
     */
    @Override
    public LocalDownloader CreateLocalDownloader(int globalDLID) {
        return new GenericLocalRetrievalLocalDownloader(manager, globalDLID, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
    }

    /* (non-Javadoc)
     * @see version2.prototype.download.DownloaderFactory#CreateGlobalDownloader(int, java.lang.String, version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData, version2.prototype.download.ListDatesFiles)
     */
    @Override
    public GlobalDownloader CreateGlobalDownloader(int myID) {
        GlobalDownloader downloader = null;
        try {
            downloader = new GenericLocalStorageGlobalDownloader(myID, pluginInfo.GetName(), pluginMetaData.Download, listDatesFiles, downloaderClassName);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | ParserConfigurationException | SAXException | IOException | SQLException e) {
            e.printStackTrace();
        }
        return downloader;
    }

}
