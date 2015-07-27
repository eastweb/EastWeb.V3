package version2.prototype.download;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Observable;
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
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public abstract class LocalDownloader extends Process {
    private boolean updateAvailable;
    protected final int globalDLID;

    protected LocalDownloader(EASTWebManager manager, int globalDLID, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler, DatabaseCache outputCache) {
        super(manager, ProcessName.DOWNLOAD, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
        updateAvailable = false;
        this.globalDLID = globalDLID;
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
            newFiles = Schemas.udpateExpectedResults(Config.getInstance().getGlobalSchema(), projectInfoFile.GetProjectName(), pluginInfo.GetName(), projectInfoFile.GetStartDate(), pluginMetaData.DaysPerInputData,
                    pluginInfo.GetIndicies().size(), projectInfoFile.GetSummaries());
            updateAvailable = true;
        } else {
            updateAvailable = false;
        }
        return newFiles;
    }

    @Override
    public void update(Observable o, Object arg) {
        super.update(o, arg);

        if(o instanceof GlobalDownloader)
        {
            updateAvailable = true;
        }
    }

    protected void ValidateDownloadCache()
    {

    }
}
