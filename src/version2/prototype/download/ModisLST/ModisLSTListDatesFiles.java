package version2.prototype.download.ModisLST;

import java.io.IOException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.ModisDownloadUtils.ModisListDatesFiles;

public class ModisLSTListDatesFiles extends ModisListDatesFiles
{
    public ModisLSTListDatesFiles(DataDate startDate, DownloadMetaData data)
            throws IOException
    {
        super(startDate, data);
    }

}