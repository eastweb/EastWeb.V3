package version2.prototype.download;

import java.util.ArrayList;

import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.util.DataFileMetaData;

public class DownloadWorker extends ProcessWorker<Void> {
    public DownloadWorker(Process<?> process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles)
    {
        super("DownloadWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Void call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
