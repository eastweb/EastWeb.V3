package version2.prototype.download.ModisLST;

import java.io.IOException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.download.ModisDownloadUtils.ModisListDatesFiles;

public class ModisLSTListDatesFiles extends ModisListDatesFiles
{
    public ModisLSTListDatesFiles(DataDate startDate, DownloadMetaData data, ProjectInfoFile project) throws IOException
    {
        super(startDate, data, project);
    }

}
