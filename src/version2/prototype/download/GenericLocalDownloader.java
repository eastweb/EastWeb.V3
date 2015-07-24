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
public class GenericLocalDownloader extends LocalDownloader {

    /**
     * Creates a GenericLocalDownloader that expects to finds the GlobalDownloader download records in a locally and globally accessible table in the database.
     *
     * @param globalDLID
     * @param projectInfoFile
     * @param pluginInfo
     * @param pluginMetaData
     * @param scheduler
     * @param outputCache
     */
    public GenericLocalDownloader(int globalDLID, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler, DatabaseCache outputCache) {
        super(globalDLID, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
    }

    @Override
    public void process(ArrayList<DataFileMetaData> cachedFiles) {
        try {
            outputCache.loadUnprocessedDownloadsToLocalDownloader(Config.getInstance().getGlobalSchema(), projectInfoFile.GetProjectName(), pluginInfo.GetName(), globalDLID, projectInfoFile.GetStartDate(),
                    pluginMetaData.ExtraDownloadFiles, pluginMetaData.DaysPerInputData);
        } catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

}
