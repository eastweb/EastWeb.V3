package EastWeb_Downloader.ModisNBAR;

import java.io.IOException;

import EastWeb_Downloader.ModisDownloadUtils.ModisListDatesFiles;
import PluginMetaData.DownloadMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import Utilies.DataDate;

public class ModisNBARQCListDatesFiles extends ModisListDatesFiles
{
    public ModisNBARQCListDatesFiles(DataDate startDate, DownloadMetaData data, ProjectInfoFile project) throws IOException
    {
        super(startDate, data, project);
    }

}
