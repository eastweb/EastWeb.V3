package version2.prototype.util;

/**
 * Represents the metadata stored for a cached data file within the database. To read the data within it must be gotten from one of the given Read* methods that return specific *FileMetaData objects.
 *
 * @author michael.devos
 *
 */
public class DataFileMetaData {
    private final int rowID;
    private final String dataFilePath;
    private final String qcFilePath;
    private final String dateDirectoryPath;
    private final int dataGroupID;
    private final int year;
    private final int day;
    private final String indexNm;

    /**
     * Creates a DataFileMetaData object initialized with the given metadata and defaults the environmental index to null.
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
        indexNm = null;
    }

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
     * @param indexNm  - the environmental index associated to the data file
     */
    public DataFileMetaData(int rowID, String dataFilePath, String qcFilePath, String dateDirectoryPath, int dataGroupID, int year, int day, String indexNm)
    {
        this.rowID = rowID;
        this.dataFilePath = dataFilePath;
        this.qcFilePath = qcFilePath;
        this.dateDirectoryPath = dateDirectoryPath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        this.indexNm = indexNm;
    }

    /**
     * Creates a DataFileMetaData object initialized with the given metadata and defaults the QC file path to null.
     *
     * @param rowID  - the row ID for this table row
     * @param dataFilePath  - full path to the data file
     * @param dateDirectoryPath  - path to the data file's date directory (e.g. ".../2015/001/")
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param indexNm  - the environmental index associated to the data file
     */
    public DataFileMetaData(int rowID, String dataFilePath, String dateDirectoryPath, int dataGroupID, int year, int day, String indexNm)
    {
        this.rowID = rowID;
        this.dataFilePath = dataFilePath;
        qcFilePath = null;
        this.dateDirectoryPath = dateDirectoryPath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        this.indexNm = indexNm;
    }

    /**
     * Creates a DataFileMetaData object, defaults the QC file path and environmental index to null and others to that given.
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
        indexNm = null;
    }

    public DownloadFileMetaData ReadMetaDataForProcessor()
    {
        return new DownloadFileMetaData(rowID, dataFilePath, qcFilePath, dateDirectoryPath, dataGroupID, year, day);
    }

    public ProcessorFileMetaData ReadMetaDataForIndices()
    {
        return new ProcessorFileMetaData(rowID, dataFilePath, qcFilePath, dateDirectoryPath, dataGroupID, year, day);
    }

    public IndicesFileMetaData ReadMetaDataForSummary()
    {
        return new IndicesFileMetaData(rowID, dataFilePath, qcFilePath, dateDirectoryPath, dataGroupID, year, day, indexNm);
    }
}
