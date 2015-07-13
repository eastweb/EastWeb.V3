package version2.prototype.Scheduler;

import version2.prototype.ConfigReadException;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;

public class SchedulerData {
    public ProjectInfoFile projectInfoFile;
    public PluginMetaDataCollection pluginMetaDataCollection;

    public SchedulerData(ProjectInfoFile projectInfoFile) throws ConfigReadException, Exception
    {
        this.projectInfoFile= projectInfoFile;
        pluginMetaDataCollection = PluginMetaDataCollection.getInstance();
    }
}
