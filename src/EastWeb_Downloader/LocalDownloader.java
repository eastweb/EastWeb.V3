package EastWeb_Downloader;


import java.time.LocalDate;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_Scheduler.ProcessName;
import EastWeb_Scheduler.Scheduler;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;
import EastWeb_ProcessWorker.Process;

/**
 * @author michael.devos
 *
 */
public abstract class LocalDownloader extends Process {
    public final String dataName;
    public final ListDatesFiles listDatesFiles;
    protected GlobalDownloader gdl;
    protected LocalDate currentStartDate;

    protected LocalDownloader(Config configInstance, GlobalDownloader gdl, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache, ListDatesFiles listDatesFiles) {
        super(configInstance, ProcessName.DOWNLOAD, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
        this.gdl = gdl;
        dataName = gdl.metaData.name;
        currentStartDate = projectInfoFile.GetStartDate();
        this.listDatesFiles = listDatesFiles;
        gdl.addObserver(this);
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
        synchronized(currentStartDate) {
            if(currentStartDate.isAfter(newStartDate)) {
                currentStartDate = newStartDate;
            }
        }
    }

    /**
     * Checks for new work from associated GlobalDownloader in the Download table, calculates the available files to process, and updates the local cache to start processing them.
     *
     */
    public void AttemptUpdate()
    {
        scheduler.StartNewProcessWorker(new DownloadWorker(gdl, configInstance, this, projectInfoFile, pluginInfo, pluginMetaData, null, outputCache));
    }
}
