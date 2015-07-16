/**
 *
 */
package version2.prototype.download.TRMM3B42RT;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.DataDate;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.FileSystem;

/**
 * @author michael.devos
 * @author Yi Liu
 *
 */
public class TRMM3B42RTGlobalDownloader extends GlobalDownloader {

    protected TRMM3B42RTGlobalDownloader(int myID, String pluginName,  DownloadMetaData metaData, ListDatesFiles listDatesFiles) {
        super(myID, pluginName, metaData, listDatesFiles);
    }

    @Override
    public void run()
    {
        // Step 1: get all downloads from ListDatesFiles
        Map<DataDate, ArrayList<String>> datesFiles = listDatesFiles.getListDatesFiles();

        // Step 2: Pull all cached downloads
        ArrayList<DataFileMetaData> cachedD;
        try {
            cachedD = GetAllDownloadedFiles();


            // Step 3: Remove already downloaded files from ListDatesFiles
            for (DataFileMetaData d: cachedD)
            {
                DownloadFileMetaData downloaded =  d.ReadMetaDataForProcessor();
                // get the year and dayOfyear from each downloaded file
                DataDate thisDate = new DataDate(downloaded.year, downloaded.day);

                // get the files associated with the date in the ListDatesFiles
                ArrayList <String> files = datesFiles.get(thisDate);
                for ( String f :files)
                {
                    String strPath = downloaded.dataFilePath;
                    strPath = strPath.substring(strPath.lastIndexOf(File.separator)+1, strPath.length());

                    // if the file is found in the downloade list, remove it
                    if (f.equalsIgnoreCase(strPath))
                    {
                        files.remove(f);
                    }
                }

                datesFiles.put(thisDate, files);
            }
        } catch (ClassNotFoundException | SQLException
                | ParserConfigurationException | SAXException | IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        // Step 4: Create downloader and run downloader for all that's left
        for(Map.Entry<DataDate, ArrayList<String>> entry : datesFiles.entrySet())
        {
            String outFolder;

            try {
                outFolder = FileSystem.GetGlobalDownloadDirectory(Config.getInstance(), pluginName);

                DataDate dd = entry.getKey();

                for (String f : entry.getValue())
                {
                    TRMM3B42RTDownloader downloader = new TRMM3B42RTDownloader(dd, outFolder, metaData);

                    try {
                        downloader.download();
                    } catch (DownloadFailedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    String filePath = outFolder + File.separator + f;
                    try {
                        AddDownloadFile("data", dd.getYear(), dd.getDayOfYear(), filePath);
                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            } catch (ConfigReadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

}
