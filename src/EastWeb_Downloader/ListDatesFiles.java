package EastWeb_Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import PluginMetaData.DownloadMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import Utilies.DataDate;


/**
 * Abstract map object container containing the list of available downloads for a specific plugin file type.
 * This map utilizes lazy initialization to avoid slowing down project creation due to the list creation time.
 *
 */
public abstract class ListDatesFiles
{
    protected List<DataDate> lDates;
    protected DataDate sDate;
    protected DownloadMetaData mData;
    protected Map<DataDate, ArrayList<String>>  mapDatesFiles;
    protected Boolean mapDatesFilesSet;
    protected ProjectInfoFile mProject;

    /**
     * Creates ListDatesFiles object but delays list creation until CloneListDatesFiles() is first called. The contained list will be of all files on data file server described by the DownloadMetaData and
     * with a date equal to or more recent than the given DataDate.
     *
     * @param startDate
     * @param data
     * @param project
     * @throws IOException
     */
    public ListDatesFiles(DataDate startDate, DownloadMetaData data, ProjectInfoFile project) throws IOException
    {
        sDate = startDate;
        mData = data;
        lDates = null;
        mapDatesFiles =  null;
        mProject =  project;
        mapDatesFiles = null;
        mapDatesFilesSet = new Boolean(false);
    }

    /**
     * Gets a map of each day and its associated files. First call to this starts the list creation and subsequent calls after this returns will reuse the created list from the first call.
     * @return map of server file paths to associated file creation dates
     */
    public Map<DataDate, ArrayList<String>> CloneListDatesFiles()
    {
        Map<DataDate, ArrayList<String>> filesMap = new HashMap<DataDate, ArrayList<String>>();
        ArrayList<String> files;

        if(!mapDatesFilesSet)
        {
            synchronized(mapDatesFilesSet)
            {
                if(!mapDatesFilesSet)
                {
                    System.out.println("Creating ListDatesFiles map for '" + mData.Title + "':'" + mData.name + "'.");
                    if ((mData.mode).equalsIgnoreCase("FTP"))
                    {
                        mapDatesFiles = ListDatesFilesFTP();
                    };

                    if ((mData.mode).equalsIgnoreCase("HTTP"))
                    {
                        mapDatesFiles = ListDatesFilesHTTP();
                    };
                    System.out.println("Finished creating ListDatesFiles map for '" + mData.Title + "':'" + mData.name + "'.");
                }
                mapDatesFilesSet = new Boolean(true);
            }
        }

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
