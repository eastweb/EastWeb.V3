/**
 *
 */
package EastWeb_Downloader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_ErrorHandling.ErrorLog;
import EastWeb_ProcessWorker.ProcessWorker;
import EastWeb_ProcessWorker.ProcessWorkerReturn;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;
import Utilies.DataFileMetaData;
import EastWeb_ProcessWorker.Process;

/**
 * @author Michael DeVos
 *
 */
public class DownloadWorker extends ProcessWorker {
    protected final GlobalDownloader gdl;

    /**
     * @param gdl
     * @param configInstance
     * @param process
     * @param projectInfoFile
     * @param pluginInfo
     * @param pluginMetaData
     * @param cachedFiles
     * @param outputCache
     */
    public DownloadWorker(GlobalDownloader gdl, Config configInstance, Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles, DatabaseCache outputCache) {
        super(configInstance, "DownloadWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);
        this.gdl = gdl;
    }

    @Override
    public ProcessWorkerReturn process() {
        try {
            gdl.SetCompleted();
            outputCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(configInstance.getGlobalSchema(), projectInfoFile.GetProjectName(), pluginInfo.GetName(),
                    gdl.metaData.name, projectInfoFile.GetStartDate(), pluginMetaData.ExtraDownloadFiles, pluginInfo.GetModisTiles(), gdl.listDatesFiles);
        } catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException e) {
            ErrorLog.add(process, "LocalDownloader.AttemptUpdate error", e);
        } catch (Exception e) {
            ErrorLog.add(process, "LocalDownloader.AttemptUpdate error", e);
        }
        return null;
    }

}
