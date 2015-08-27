package version2.prototype.download;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;

public abstract class ListDatesFiles
{
    protected List<DataDate> lDates;
    protected DataDate sDate;
    protected DownloadMetaData mData;
    protected Map<DataDate, ArrayList<String>>  mapDatesFiles;
    protected ProjectInfoFile mProject;

    public ListDatesFiles(DataDate startDate, DownloadMetaData data, ProjectInfoFile project) throws IOException
    {
        sDate = startDate;
        mData = data;
        lDates = null;
        mapDatesFiles =  null;
        mProject =  project;

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
    public Map<DataDate, ArrayList<String>> CloneListDatesFiles()
    {
        Map<DataDate, ArrayList<String>> filesMap = new HashMap<DataDate, ArrayList<String>>();
        ArrayList<String> files;
        for(DataDate dd : mapDatesFiles.keySet())
        {
            files = new ArrayList<String>();
            for(String file :  mapDatesFiles.get(dd))
            {
                files.add(new String(file));
            }
            filesMap.put(dd, files);
        }
        return filesMap;
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
