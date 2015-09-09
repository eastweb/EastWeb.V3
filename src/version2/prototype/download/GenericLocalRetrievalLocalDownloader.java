/**
 *
 */
package version2.prototype.download;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.EASTWebManagerI;
import version2.prototype.ErrorLog;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;

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
     * @param manager
     * @param configInstance
     * @param gdl
     * @param projectInfoFile
     * @param pluginInfo
     * @param pluginMetaData
     * @param scheduler
     * @param outputCache
     * @param listDatesFiles
     */
    public GenericLocalRetrievalLocalDownloader(EASTWebManagerI manager, Config configInstance, GlobalDownloader gdl, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache, ListDatesFiles listDatesFiles) {
        super(manager, configInstance, gdl, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache, listDatesFiles);
    }

    @Override
    public void process(ArrayList<DataFileMetaData> cachedFiles) {
        try {
            if(scheduler.GetState() == TaskState.RUNNING)
            {
                outputCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(configInstance.getGlobalSchema(), projectInfoFile.GetProjectName(), pluginInfo.GetName(), dataName, projectInfoFile.GetStartDate(), pluginMetaData.ExtraDownloadFiles, projectInfoFile.GetModisTiles(), listDatesFiles);
            }
        } catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException e) {
            ErrorLog.add(processName, scheduler, "GenericLocalRetrievalLocalDownloader.process error.", e);
        } catch (Exception e) {
            ErrorLog.add(processName, scheduler, "GenericLocalRetrievalLocalDownloader.process error.", e);
        }
    }

}
