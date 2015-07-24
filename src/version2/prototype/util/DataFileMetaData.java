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
    private final String filePath;
    private final int dataGroupID;
    private final int year;
    private final int day;
    private final String indexNm;
    private final ArrayList<DataFileMetaData> extraDownloads;

    /**
     * Creates a DataFileMetaData object initialized with the given metadata and defaults the environmental index to null.
     *
     * @param dataName  - name of the data this file represents (data name = plugin name)
     * @param filePath  - full path to the data file
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param extraDownloads  - the files
     */
    public DataFileMetaData(String dataName, String filePath, int dataGroupID, int year, int day, ArrayList<DataFileMetaData> extraDownloads)
    {
        this.dataName = dataName;
        this.filePath = filePath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        indexNm = null;
        this.extraDownloads = extraDownloads;
    }

    /**
     * Creates a DataFileMetaData object initialized with the given metadata.
     *
     * @param dataName  - name of the data this file represents (data name = plugin name)
     * @param filePath  - full path to the data file
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param indexNm  - the environmental index associated to the data file
     * @param extraDownloads  - collection of the extra downloads specified by the plugin metadata
     */
    public DataFileMetaData(String dataName, String filePath, int dataGroupID, int year, int day, String indexNm, ArrayList<DataFileMetaData> extraDownloads)
    {
        this.dataName = dataName;
        this.filePath = filePath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        this.indexNm = indexNm;
        this.extraDownloads = extraDownloads;
    }

    /**
     * Creates a DataFileMetaData object initialized with the given metadata and defaults the QC file path to null.
     *
     * @param dataName  - name of the data this file represents (data name = plugin name)
     * @param filePath  - full path to the data file
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param indexNm  - the environmental index associated to the data file
     */
    public DataFileMetaData(String dataName, String filePath, int dataGroupID, int year, int day, String indexNm)
    {
        this.dataName = dataName;
        this.filePath = filePath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        this.indexNm = indexNm;
        extraDownloads = new ArrayList<DataFileMetaData>(0);
    }

    /**
     * Creates a DataFileMetaData object, defaults the QC file path and environmental index to null and others to that given.
     *
     * @param dataName  - name of the data this file represents (data name = plugin name)
     * @param filePath  - full path to the data file
     * @param dataGroupID  - unique ID associated with the combination of the year and day
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     */
    public DataFileMetaData(String dataName, String filePath, int dataGroupID, int year, int day)
    {
        this.dataName = dataName;
        this.filePath = filePath;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
        indexNm = null;
        extraDownloads = new ArrayList<DataFileMetaData>(0);
    }

    /**
     * Retrieves a the data from this DataFileMetaData as a DownloadFileMetaData object.
     *
     * @return a DownloadFileMetaData object
     */
    public DownloadFileMetaData ReadMetaDataForProcessor()
    {
        return new DownloadFileMetaData(dataName, filePath, dataGroupID, year, day, extraDownloads);
    }

    /**
     * @return
     */
    public ProcessorFileMetaData ReadMetaDataForIndices()
    {
        return new ProcessorFileMetaData(dataName, filePath, dataGroupID, year, day, extraDownloads);
    }

    /**
     * @return
     */
    public IndicesFileMetaData ReadMetaDataForSummary()
    {
        return new IndicesFileMetaData(dataName, filePath, dataGroupID, year, day, indexNm, extraDownloads);
    }
}
