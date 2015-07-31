package version2.prototype.Scheduler;

import java.io.File;

import version2.prototype.ConfigReadException;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;

public class SchedulerData {
    public ProjectInfoFile projectInfoFile;
    public PluginMetaDataCollection pluginMetaDataCollection;

    /**
     * Creates a SchedulerData object containing the project's metadata and the collection of available plugin metadata.
     *
     * @param projectInfoFile  - the project metadata to use within the Scheduler this object is sent to
     * @throws ConfigReadException
     * @throws Exception
     */
    public SchedulerData(ProjectInfoFile projectInfoFile) throws ConfigReadException, Exception
    {
        this.projectInfoFile= projectInfoFile;
        pluginMetaDataCollection = PluginMetaDataCollection.getInstance();
    }

    /**
     * Creates a SchedulerData object containing the given project metadata and the plugin metadata from the given file path.
     *
     * @param projectInfoFile  - the project metadata to use within the Scheduler this object is sent to
     * @param pluginMetaDataFile  - the path to the plugin metadata xml to use within the Scheduler this object is sent to
     * @throws ConfigReadException
     * @throws Exception
     */
    public SchedulerData(ProjectInfoFile projectInfoFile, String pluginMetaDataFile) throws ConfigReadException, Exception
    {
        this.projectInfoFile = projectInfoFile;
        if(pluginMetaDataFile != null) {
            pluginMetaDataCollection = PluginMetaDataCollection.getInstance(new File(pluginMetaDataFile));
        }
    }
}
