package EastWeb_Downloader.ModisNBAR;

import EastWeb_Downloader.ModisDownloadUtils.ModisDownloader;
import PluginMetaData.DownloadMetaData;
import Utilies.DataDate;

public class ModisNBARDownloader extends ModisDownloader
{
    public ModisNBARDownloader(DataDate date, String outFolder, DownloadMetaData data, String fileToDownload)
    {
        super(date, outFolder, data, fileToDownload);
    }
}
