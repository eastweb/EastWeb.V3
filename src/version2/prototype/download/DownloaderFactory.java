/**
 *
 */
package version2.prototype.download;

/**
 * @author michael.devos
 *
 */
public interface DownloaderFactory {
    /**
     * Creates a LocalDownloader instance determined by subclasses.
     *
     * @param globalDLID  - the GlobalDownloader unique ID number
     * @return a LocalDownloader instance
     */
    public LocalDownloader CreateLocalDownloader(int globalDLID);

    /**
     * Creates a GlobaldDownloader instance determined by subclasses.
     *
     * @param myID  - an unique number to use as the ID for this GlobalDownloader instance (only has to be unique in comparison to currently surviving GlobalDownloader instances).
     * @return a GlobalDownloader instance
     */
    public GlobalDownloader CreateGlobalDownloader(int myID);
}
