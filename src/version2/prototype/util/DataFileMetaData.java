package version2.prototype.util;

import java.util.ArrayList;

/**
 * Represents the metadata stored for a cached data file within the database. To read the data within it must be gotten from one of the given Read* methods that return specific *FileMetaData objects.
 *
 * @author michael.devos
 *
 */
public class DataFileMetaData {
    private final String dataName;
    private final String dataFilePath;
    private final int dataGroupID;
    private final int year;
    private final int day;
    private final String indexNm;
    private final ArrayList<DataFileMetaData> extraDownloads;

    /**
     * Creates a DataFileMetaData object initialized with the given metadata and defaults the environmental index to null.
     *
     * @param dataFilePath  - full path to the data file
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param extraDownloads  - the files
     */
    public DataFileMetaData(String dataName, String dataFilePath, int dataGroupID, int year, int day, ArrayList<DataFileMetaData> extraDownloads)
    {
        this.dataName = dataName;
        this.dataFilePath = dataFilePath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        indexNm = null;
        this.extraDownloads = extraDownloads;
    }

    /**
     * Creates a DataFileMetaData object initialized with the given metadata.
     *
     * @param dataFilePath  - full path to the data file
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param indexNm  - the environmental index associated to the data file
     */
    public DataFileMetaData(String dataName, String dataFilePath, int dataGroupID, int year, int day, String indexNm, ArrayList<DataFileMetaData> extraDownloads)
    {
        this.dataName = dataName;
        this.dataFilePath = dataFilePath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        this.indexNm = indexNm;
        this.extraDownloads = extraDownloads;
    }

    /**
     * Creates a DataFileMetaData object initialized with the given metadata and defaults the QC file path to null.
     *
     * @param dataFilePath  - full path to the data file
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param indexNm  - the environmental index associated to the data file
     */
    public DataFileMetaData(String dataName, String dataFilePath, int dataGroupID, int year, int day, String indexNm)
    {
        this.dataName = dataName;
        this.dataFilePath = dataFilePath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        this.indexNm = indexNm;
        extraDownloads = new ArrayList<DataFileMetaData>(0);
    }

    /**
     * Creates a DataFileMetaData object, defaults the QC file path and environmental index to null and others to that given.
     *
     * @param dataFilePath  - full path to the data file
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     */
    public DataFileMetaData(String dataName, String dataFilePath, int dataGroupID, int year, int day)
    {
        this.dataName = dataName;
        this.dataFilePath = dataFilePath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        indexNm = null;
        extraDownloads = new ArrayList<DataFileMetaData>(0);
    }

    public DownloadFileMetaData ReadMetaDataForProcessor()
    {
        return new DownloadFileMetaData(dataName, dataFilePath, dataGroupID, year, day, extraDownloads);
    }

    public ProcessorFileMetaData ReadMetaDataForIndices()
    {
        return new ProcessorFileMetaData(dataName, dataFilePath, dataGroupID, year, day, extraDownloads);
    }

    public IndicesFileMetaData ReadMetaDataForSummary()
    {
        return new IndicesFileMetaData(dataName, dataFilePath, dataGroupID, year, day, indexNm, extraDownloads);
    }
}
