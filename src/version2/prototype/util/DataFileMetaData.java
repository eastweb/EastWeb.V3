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
    private final int year;
    private final int day;
    private final String indexNm;
    private final ArrayList<DataFileMetaData> extraDownloads;

    /**
     * Creates a DataFileMetaData object initialized with the given metadata and defaults the environmental index to null.
     *
     * @param dataName  - name of the data this file represents (data name = plugin name)
     * @param filePath  - full path to the data file
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param extraDownloads  - the files
     */
    public DataFileMetaData(String dataName, String filePath, int year, int day, ArrayList<DataFileMetaData> extraDownloads)
    {
        this.dataName = dataName;
        this.filePath = filePath;
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
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param indexNm  - the environmental index associated to the data file
     * @param extraDownloads  - collection of the extra downloads specified by the plugin metadata
     */
    public DataFileMetaData(String dataName, String filePath, int year, int day, String indexNm, ArrayList<DataFileMetaData> extraDownloads)
    {
        this.dataName = dataName;
        this.filePath = filePath;
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
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param indexNm  - the environmental index associated to the data file
     */
    public DataFileMetaData(String dataName, String filePath, int year, int day, String indexNm)
    {
        this.dataName = dataName;
        this.filePath = filePath;
        this.year = year;
        this.day = day;
        this.indexNm = indexNm;
        extraDownloads = new ArrayList<DataFileMetaData>(0);
    }

    /**
     * Creates a DataFileMetaData object. Defaults the environmental index to null, extraDownloads list to an empty list, and others to that given.
     *
     * @param dataName  - name of the data this file represents (name is that of what's found for the 'Name' attribute of the 'Download' node in the plugin metadata this file is for)
     * @param filePath  - full path to the data file
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     */
    public DataFileMetaData(String dataName, String filePath, int year, int day)
    {
        this.dataName = dataName;
        this.filePath = filePath;
        this.year = year;
        this.day = day;
        indexNm = null;
        extraDownloads = new ArrayList<DataFileMetaData>(0);
    }

    /**
     * Copy constructor
     *
     * @param dData  - DownloadFileMetaData object to create a copy DataFileMetaData object of
     */
    public DataFileMetaData(DownloadFileMetaData dData)
    {
        dataName = dData.dataName;
        filePath = dData.dataFilePath;
        year = dData.year;
        day = dData.day;
        indexNm = null;

        ArrayList<DataFileMetaData> extras = new ArrayList<DataFileMetaData>();
        if (dData.extraDownloads != null)
        {
            for(DownloadFileMetaData extra : dData.extraDownloads)
            {
                extras.add(new DataFileMetaData(extra.dataName, dData.dataFilePath, dData.year, dData.day));
            }
            extraDownloads = extras;
        } else {
            extraDownloads = null;
        }

    }

    /**
     * Copy constructor
     *
     * @param pData  - ProcessorFileMetaData object to create a copy DataFileMetaData object of
     */
    public DataFileMetaData(ProcessorFileMetaData pData)
    {
        dataName = null;
        filePath = pData.dataFilePath;
        year = pData.year;
        day = pData.day;
        indexNm = null;
        extraDownloads = null;
    }

    /**
     * Copy constructor
     *
     * @param iData  - IndicesFileMetaData object to create a copy DataFileMetaData object of
     */
    public DataFileMetaData(IndicesFileMetaData iData)
    {
        dataName = null;
        filePath = iData.dataFilePath;
        year = iData.year;
        day = iData.day;
        indexNm = iData.indexNm;
        extraDownloads = null;
    }

    /**
     * Retrieves only the data relevant to Processor from this DataFileMetaData as a DownloadFileMetaData object.
     *
     * @return a tailored DownloadFileMetaData object
     */
    public DownloadFileMetaData ReadMetaDataForProcessor()
    {
        if (extraDownloads != null)
        {
            ArrayList<DownloadFileMetaData> extras = new ArrayList<DownloadFileMetaData>(extraDownloads.size());
            for(DataFileMetaData extraData : extraDownloads)
            {
                extras.add(extraData.ReadMetaDataForProcessor());
            }
            return new DownloadFileMetaData(dataName, filePath, year, day, extras);
        } else {
            return new DownloadFileMetaData(dataName, filePath, year, day, null);
        }
    }

    /**
     * Retrieves only the data relevant to Indices from this DataFileMetaData as a ProcessorFileMetaData object.
     *
     * @return a tailored ProcessorFileMetaData object
     */
    public ProcessorFileMetaData ReadMetaDataForIndices()
    {
        return new ProcessorFileMetaData(filePath, year, day);
    }

    /**
     * Retrieves only the data relevant to Summary from this DataFileMetaData as a IndicesFileMetaData object.
     *
     * @return a tailored IndicesFileMetaData object
     */
    public IndicesFileMetaData ReadMetaDataForSummary()
    {
        return new IndicesFileMetaData(filePath, year, day, indexNm);
    }
}
