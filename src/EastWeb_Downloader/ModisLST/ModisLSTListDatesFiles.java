package EastWeb_Downloader.ModisLST;

import java.io.IOException;

import EastWeb_Downloader.ModisDownloadUtils.ModisListDatesFiles;
import PluginMetaData.DownloadMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import Utilies.DataDate;

public class ModisLSTListDatesFiles extends ModisListDatesFiles
{
    public ModisLSTListDatesFiles(DataDate startDate, DownloadMetaData data, ProjectInfoFile project) throws IOException
    {
        super(startDate, data, project);
    }

}
