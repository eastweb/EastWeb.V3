package version2.prototype.Scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import version2.prototype.ConfigReadException;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;

public class SchedulerData {
    public ProjectInfoFile projectInfoFile;
    public PluginMetaDataCollection pluginMetaDataCollection;
    public ArrayList<String> SummarySingletonNames;

    public SchedulerData(ProjectInfoFile projectInfoFile) throws ConfigReadException, Exception
    {
        this.projectInfoFile= projectInfoFile;
        pluginMetaDataCollection = PluginMetaDataCollection.getInstance();
        SummarySingletonNames = new ArrayList<String>(Arrays.asList("Count", "Sum", "Mean", "StdDev"));
    }
}
