/**
 *
 */
package version2.prototype.download.TRMM3B42RT;

import java.util.ArrayList;
import java.util.Map;

import version2.prototype.DataDate;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.ListDatesFiles;

/**
 * @author michael.devos
 *
 */
public class TRMM3B42RTGlobalDownloader extends GlobalDownloader {

    protected TRMM3B42RTGlobalDownloader(int myID, String pluginName, TaskState initialState, DownloadMetaData metaData, ListDatesFiles listDatesFiles) {
        super(myID, pluginName, initialState, metaData, listDatesFiles);
    }

    @Override
    public void run() {
        Map<DataDate, ArrayList<String>> datesFiles = listDatesFiles.getListDatesFiles();

        for(Map.Entry<DataDate, ArrayList<String>> entry : datesFiles.entrySet())
        {
            TRMM3B42RTDownloader downloader = new TRMM3B42RTDownloader(date, outFolder, data);
        }

    }

}
