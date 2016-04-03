package EastWeb_Downloader.ModisNBAR;

import java.io.IOException;

import EastWeb_Downloader.ModisDownloadUtils.ModisListDatesFiles;
import PluginMetaData.DownloadMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import Utilies.DataDate;


public class ModisNBARListDatesFiles extends ModisListDatesFiles
{
    public ModisNBARListDatesFiles(DataDate startDate, DownloadMetaData data, ProjectInfoFile project) throws IOException
    {
        super(startDate, data, project);
    }

}