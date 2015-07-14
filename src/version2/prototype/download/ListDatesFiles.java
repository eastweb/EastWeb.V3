package version2.prototype.download;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;

public abstract class ListDatesFiles
{
    public final Pattern fileNamePattern;
    public final Pattern datePattern;
    protected List<DataDate> lDates;
    protected DataDate sDate;
    protected DownloadMetaData mData;
    protected Map<DataDate, ArrayList<String>>  mapDatesFiles;

    public ListDatesFiles(DataDate startDate, DownloadMetaData data) throws IOException
    {
        sDate = startDate;
        mData = data;
        lDates = null;
        mapDatesFiles =  null;
        fileNamePattern = Pattern.compile(mData.fileNamePattern);
        datePattern = Pattern.compile(mData.datePattern);

        if ((mData.mode).equalsIgnoreCase("FTP"))
        {
            mapDatesFiles = ListDatesFilesFTP();
        };

        if ((mData.mode).equalsIgnoreCase("HTTP"))
        {
            mapDatesFiles = ListDatesFilesHTTP();
        };
    }

    // gets a map of each day and its associated files
    public Map<DataDate, ArrayList<String>> getListDatesFiles()
    {
        return mapDatesFiles;
    }

    /* Overridden by each plugin using FTP protocol
     * produce the map of each day and its associated files
     */
    abstract protected Map<DataDate, ArrayList<String>> ListDatesFilesFTP();

    /* Overridden by each plugin using HTTP protocol
     * produce the map of each day and its associated files
     */
    abstract protected Map<DataDate, ArrayList<String>> ListDatesFilesHTTP();

}
