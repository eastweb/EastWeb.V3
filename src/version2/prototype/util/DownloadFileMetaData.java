package version2.prototype.util;

import java.util.ArrayList;

/**
 *
 * @author michael.devos
 *
 */
public class DownloadFileMetaData {
    public final String dataName;
    public final String dataFilePath;
    public final int year;
    public final int day;
    public final ArrayList<DownloadFileMetaData> extraDownloads;

    /**
     * Creates a DownloadFileMetaData object initialized to the given values.
     *
     * @param dataName  - name of the data file this object represents
     * @param dataFilePath  - full path to the data file
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param extraDownloads  - extra download files gotten as define by the plugin metadata
     */
    public DownloadFileMetaData(String dataName, String dataFilePath, int year, int day, ArrayList<DownloadFileMetaData> extraDownloads) {
        this.dataName = dataName;
        this.dataFilePath = dataFilePath;
        this.year = year;
        this.day = day;
        this.extraDownloads = extraDownloads;
    }
}
