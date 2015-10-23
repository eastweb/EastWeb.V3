package version2.prototype.download.ModisLST;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.download.ModisDownloadUtils.ModisDownloader;

public class ModisLSTDownloader extends ModisDownloader
{

    public ModisLSTDownloader(DataDate date, String outFolder, DownloadMetaData data, String fileToDownload)
    {
        super(date, outFolder, data, fileToDownload);
    }
}
