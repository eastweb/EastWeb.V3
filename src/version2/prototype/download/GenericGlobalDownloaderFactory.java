/**
 *
 */
package version2.prototype.download;

import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;

/**
 * @author michael.devos
 *
 */
public class GenericGlobalDownloaderFactory implements GlobalDownloaderFactory {
    private int myID;
    private final String pluginName;
    private final DownloadMetaData metaData;
    private final ListDatesFiles listDatesFiles;
    private final String downloaderClassName;

    public GenericGlobalDownloaderFactory(String pluginName, DownloadMetaData metaData, ListDatesFiles listDatesFiles, String downloaderClassName)
    {
        this.pluginName = pluginName;
        this.metaData = metaData;
        this.listDatesFiles = listDatesFiles;
        this.downloaderClassName = downloaderClassName;
    }

    /* (non-Javadoc)
     * @see version2.prototype.download.GlobalDownloaderFactory#createGlobalDownloader()
     */
    @Override
    public GlobalDownloader createGlobalDownloader(int myID) {
        GlobalDownloader downloader = null;
        try {
            downloader = new GenericGlobalDownloader(myID, pluginName, metaData, listDatesFiles, downloaderClassName);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return downloader;
    }

}
