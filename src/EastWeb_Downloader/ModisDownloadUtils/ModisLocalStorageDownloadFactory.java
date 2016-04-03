/**
 *
 */
package EastWeb_Downloader.ModisDownloadUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_Database.DatabaseConnection;
import EastWeb_Database.DatabaseConnector;
import EastWeb_Downloader.DownloaderFactory;
import EastWeb_Downloader.GenericLocalRetrievalLocalDownloader;
import EastWeb_Downloader.GlobalDownloader;
import EastWeb_Downloader.ListDatesFiles;
import EastWeb_Downloader.LocalDownloader;
import EastWeb_Downloader.RegistrationException;
import EastWeb_ErrorHandling.ErrorLog;
import EastWeb_Scheduler.Scheduler;
import PluginMetaData.DownloadMetaData;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;

/**
 * @author michael.devos
 *
 */
public final class ModisLocalStorageDownloadFactory implements DownloaderFactory {
    private final Config configInstance;
    private final String downloaderClassName;
    private final ProjectInfoFile projectInfoFile;
    private final ProjectInfoPlugin pluginInfo;
    private final DownloadMetaData downloadMetaData;
    private final PluginMetaData pluginMetaData;
    private final Scheduler scheduler;
    private final DatabaseCache outputCache;
    private final ListDatesFiles listDatesFiles;
    private final LocalDate startDate;

    /**
     * @param manager
     * @param configInstance
     * @param downloaderClassName
     * @param projectInfoFile
     * @param pluginInfo
     * @param downloadMetaData
     * @param pluginMetaData
     * @param scheduler
     * @param outputCache
     * @param listDatesFiles
     * @param startDate
     */
    public ModisLocalStorageDownloadFactory(Config configInstance, String downloaderClassName, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            DownloadMetaData downloadMetaData, PluginMetaData pluginMetaData, Scheduler scheduler, DatabaseCache outputCache, ListDatesFiles listDatesFiles, LocalDate startDate)
    {
        this.configInstance = configInstance;
        this.downloaderClassName = downloaderClassName;
        this.projectInfoFile = projectInfoFile;
        this.pluginInfo = pluginInfo;
        this.downloadMetaData = downloadMetaData;
        this.pluginMetaData = pluginMetaData;
        this.scheduler = scheduler;
        this.outputCache = outputCache;
        this.listDatesFiles = listDatesFiles;
        this.startDate = startDate;
    }

    /* (non-Javadoc)
     * @see version2.prototype.download.DownloaderFactory#CreateLocalDownloader(version2.prototype.download.GlobalDownloader)
     */
    @Override
    public LocalDownloader CreateLocalDownloader(GlobalDownloader gdl) {
        return new GenericLocalRetrievalLocalDownloader(configInstance, gdl, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache, listDatesFiles);
    }

    /* (non-Javadoc)
     * @see version2.prototype.download.DownloaderFactory#CreateGlobalDownloader(int)
     */
    @Override
    public GlobalDownloader CreateGlobalDownloader(int myID) {
        GlobalDownloader gdl = null;
        DatabaseConnection con = null;
        Statement stmt = null;
        try {
            gdl = new ModisLocalStorageGlobalDownloader(myID, configInstance, projectInfoFile, pluginInfo.GetName(), downloadMetaData, listDatesFiles, startDate, downloaderClassName);
            con = DatabaseConnector.getConnection(configInstance);
            if(con == null) {
                return null;
            }
            stmt = con.createStatement();
            gdl.RegisterGlobalDownloader();
        } catch (RegistrationException e) {
            return null;
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | ParserConfigurationException | SAXException | IOException | SQLException e) {
            ErrorLog.add(Config.getInstance(), pluginInfo.GetName(), downloadMetaData.name, "LocalStorageDownloadFactory.CreateGlobalDownloader problem with creating new GenericLocalStorageGlobalDownloader.", e);
        } catch (Exception e) {
            ErrorLog.add(Config.getInstance(), pluginInfo.GetName(), downloadMetaData.name, "LocalStorageDownloadFactory.CreateGlobalDownloader problem with creating new GenericLocalStorageGlobalDownloader.", e);
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) { /* do nothing */}
            }
            con.close();
        }
        return gdl;
    }

}
