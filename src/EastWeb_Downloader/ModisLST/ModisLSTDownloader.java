package EastWeb_Downloader.ModisLST;

import EastWeb_Downloader.ModisDownloadUtils.ModisDownloader;
import PluginMetaData.DownloadMetaData;
import Utilies.DataDate;

public class ModisLSTDownloader extends ModisDownloader
{

    public ModisLSTDownloader(DataDate date, String outFolder, DownloadMetaData data, String fileToDownload)
    {
        super(date, outFolder, data, fileToDownload);
    }
}
