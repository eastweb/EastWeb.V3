package version2.prototype.download.ModisNBAR;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.download.ModisDownloadUtils.ModisDownloader;

public class ModisNBARDownloader extends ModisDownloader
{
    public ModisNBARDownloader(DataDate date, String outFolder,
            DownloadMetaData data, String fileToDownload)
    {
        super(date, outFolder, data, fileToDownload);
    }
}
