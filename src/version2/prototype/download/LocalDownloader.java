package version2.prototype.download;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Observable;

import version2.prototype.ConfigReadException;
import version2.prototype.Process;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.PostgreSQLConnection;

/**
 * @author michael.devos
 *
 */
public abstract class LocalDownloader extends Process {
    private boolean updateAvailable;
    private final int globalDLID;

    protected LocalDownloader(int globalDLID, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache outputCache) {
        super(ProcessName.DOWNLOAD, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);
        updateAvailable = true;
        this.globalDLID = globalDLID;
    }

    public void AttemptUpdate()
    {
        if(scheduler.GetSchedulerStatus().GetState() == TaskState.RUNNING)
        {
            Connection con = PostgreSQLConnection.getConnection();
            Statement stmt = con.createStatement();
            stmt.executeUpdate(sql);
            updateAvailable = false;
        }
    }

    protected void ValidateDownloadCache()
    {

    }

    @Override
    public void update(Observable o, Object arg) {
        super.update(o, arg);

        if(o instanceof GlobalDownloader)
        {
            updateAvailable = true;
        }
    }
}
