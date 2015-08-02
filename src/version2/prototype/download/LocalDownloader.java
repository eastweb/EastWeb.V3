package version2.prototype.download;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.EASTWebManager;
import version2.prototype.Process;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public abstract class LocalDownloader extends Process {
    protected final GlobalDownloader gdl;
    protected final String dataName;
    protected final Config configInstance;
    protected LocalDate currentStartDate;

    protected LocalDownloader(EASTWebManager manager, Config configInstance, GlobalDownloader gdl, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache) {
        super(manager, ProcessName.DOWNLOAD, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
        this.gdl = gdl;
        dataName = gdl.metaData.name;
        this.configInstance = configInstance;
        currentStartDate = projectInfoFile.GetStartDate();

        if(gdl != null) {
            gdl.addObserver(this);
        }
    }

    /**
     * Gets the date this LocalDownloader will/has started downloading from.
     *
     * @return start date for downloading
     */
    public final LocalDate GetStartDate() { return currentStartDate; }

    /**
     * Changes the start date for this LocalDownloader and causes it to start downloading from the given date. Does not cause the LocalDownloader to redownload anything already
     * downloaded but if the date is earlier than the current start date then it will download those now missing, or if it's later than the current start date then it is simply
     * ignored and the original start date is kept.
     *
     * @param newStartDate  - new date to state downloading from
     */
    public final void SetStartDate(LocalDate newStartDate)
    {
        if(currentStartDate.isAfter(newStartDate)) {
            currentStartDate = newStartDate;
        }
    }

    /**
     * Checks for new work from associated GlobalDownloader in the Download table, calculates the available files to process, and updates the local cache to start processing them.
     *
     * @return number of new files to process per the ordered Summaries defined by the project metadata Summaries section
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public TreeMap<Integer, Integer> AttemptUpdate() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        TreeMap<Integer, Integer> newFiles = new TreeMap<Integer, Integer>();
        if(scheduler.GetSchedulerStatus().GetState() == TaskState.RUNNING)
        {
            final Connection conn = PostgreSQLConnection.getConnection();
            newFiles = Schemas.updateExpectedResults(configInstance.getGlobalSchema(), projectInfoFile.GetProjectName(), pluginInfo.GetName(), currentStartDate, pluginMetaData.DaysPerInputData,
                    pluginInfo.GetIndices().size(), projectInfoFile.GetSummaries(), conn);
            conn.close();

            outputCache.LoadUnprocessedGlobalDownloadsToLocalDownloader(configInstance.getGlobalSchema(), projectInfoFile.GetProjectName(), pluginInfo.GetName(), dataName, currentStartDate,
                    pluginMetaData.ExtraDownloadFiles, projectInfoFile.GetModisTiles());
        }
        return newFiles;
    }
}
