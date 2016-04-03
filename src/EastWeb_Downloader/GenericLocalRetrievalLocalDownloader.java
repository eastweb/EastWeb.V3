/**
 *
 */
package EastWeb_Downloader;

import java.util.ArrayList;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_Scheduler.Scheduler;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;
import Utilies.DataFileMetaData;

/**
 * GenericLocalDownloader used for getting files from a locally used GlobalDownloader for the DatabaseCache this LocalDownloader uses as its output cache.
 *
 * @author michael.devos
 *
 */
public class GenericLocalRetrievalLocalDownloader extends LocalDownloader {

    /**
     * Creates a GenericLocalDownloader that expects to finds the GlobalDownloader download records in a locally and globally accessible table in the database.
     *
     * @param configInstance
     * @param gdl
     * @param projectInfoFile
     * @param pluginInfo
     * @param pluginMetaData
     * @param scheduler
     * @param outputCache
     * @param listDatesFiles
     */
    public GenericLocalRetrievalLocalDownloader(Config configInstance, GlobalDownloader gdl, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, Scheduler scheduler, DatabaseCache outputCache, ListDatesFiles listDatesFiles) {
        super(configInstance, gdl, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache, listDatesFiles);
    }

    @Override
    public void process(ArrayList<DataFileMetaData> cachedFiles) {
        scheduler.StartNewProcessWorker(new DownloadWorker(gdl, configInstance, this, projectInfoFile, pluginInfo, pluginMetaData, null, outputCache));
    }

}
