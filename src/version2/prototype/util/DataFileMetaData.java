package version2.prototype.util;

/**
 * Represents the metadata stored for a cached data file within the database.
 *
 * @author michael.devos
 *
 */
public class DataFileMetaData {
    public final int rowID;
    public final String dataFilePath;
    public final String qcFilePath;
    public final String dateDirectoryPath;
    public final int dataGroupID;
    public final int year;
    public final int day;

    /**
     * Creates a DataFileMetaData object initialized with the given metadata.
     *
     * @param rowID  - the row ID for this table row
     * @param dataFilePath  - full path to the data file
     * @param qcFilePath  - full path to the QC file associated to the data file
     * @param dateDirectoryPath  - path to the data file's date directory (e.g. ".../2015/001/")
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     */
    public DataFileMetaData(int rowID, String dataFilePath, String qcFilePath, String dateDirectoryPath, int dataGroupID, int year, int day)
    {
        this.rowID = rowID;
        this.dataFilePath = dataFilePath;
        this.qcFilePath = qcFilePath;
        this.dateDirectoryPath = dateDirectoryPath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
    }

    /**
     * Creates a DataFileMetaData object, defaults the QC file path to null and others to that given.
     *
     * @param rowID  - the row ID for this table row
     * @param dataFilePath  - full path to the data file
     * @param dateDirectoryPath  - path to the data file's date directory (e.g. ".../2015/001/")
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     */
    public DataFileMetaData(int rowID, String dataFilePath, String dateDirectoryPath, int dataGroupID, int year, int day)
    {
        this.rowID = rowID;
        this.dataFilePath = dataFilePath;
        qcFilePath = null;
        this.dateDirectoryPath = dateDirectoryPath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
    }
}
