package version2.prototype.util;

import java.util.ArrayList;

/**
 *
 * @author michael.devos
 *
 */
public class IndicesFileMetaData {
    public final int rowID;
    public final String dataName;
    public final String dataFilePath;
    public final String dateDirectoryPath;
    public final int dataGroupID;
    public final int year;
    public final int day;
    public final String indexNm;
    public final ArrayList<DataFileMetaData> extraDownloads;

    /**
     * Creates a IndicesFileMetaData object initialized to the given values.
     *
     * @param rowID  - the row ID for this table row
     * @param dataFilePath  - full path to the data file
     * @param qcFilePath  - full path to the QC file associated to the data file
     * @param dateDirectoryPath  - path to the data file's date directory (e.g. ".../2015/001/")
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param indexNm  - the environmental index associated to the data file
     */
    public IndicesFileMetaData(int rowID, String dataName, String dataFilePath, String dateDirectoryPath, int dataGroupID, int year, int day, String indexNm, ArrayList<DataFileMetaData> extraDownloads) {
        this.rowID = rowID;
        this.dataName = dataName;
        this.dataFilePath = dataFilePath;
        this.dateDirectoryPath = dateDirectoryPath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        this.indexNm = indexNm;
        this.extraDownloads = extraDownloads;
    }
}
