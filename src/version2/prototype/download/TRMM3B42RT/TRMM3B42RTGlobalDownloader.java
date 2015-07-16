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

    public TRMM3B42RTGlobalDownloader(int myID, String pluginName,  DownloadMetaData metaData, ListDatesFiles listDatesFiles) {
        super(myID, pluginName, metaData, listDatesFiles);
    }

    @Override
    //get the files to in the TRMM3B42RTListDatesFiles, remove from the list the files downloaded already by checking the cached files in the database
    //Download the files in the list by calling the download() method in TRMM3B42RTDownloader

    public void run()
    {
        // Step 1: get all downloads from ListDatesFiles
        Map<DataDate, ArrayList<String>> datesFiles = listDatesFiles.getListDatesFiles();

        // Step 2: Pull all cached downloads
        ArrayList<DataFileMetaData> cachedD = new ArrayList<DataFileMetaData>();
        //try {

        //WRITEBACK
        //cachedD = GetAllDownloadedFiles();
        DataFileMetaData d1 = new DataFileMetaData(1, "D:\\project\\download\\TRMM3B42RT\\0001\\182\\3B42RT_daily.2015.07.01.bin", null, null, 0, 2015, 182);
        DataFileMetaData d2 = new DataFileMetaData(1, "D:\\project\\download\\TRMM3B42RT\\0001\\183\\3B42RT_daily.2015.07.02.bin", null, null, 0, 2015, 183);
        cachedD.add(d1);
        cachedD.add(d2);


        // Step 3: Remove already downloaded files from ListDatesFiles
        for (DataFileMetaData d: cachedD)
        {
            DownloadFileMetaData downloaded =  d.ReadMetaDataForProcessor();
            // get the year and dayOfyear from each downloaded file
            System.out.println(downloaded.day);
            DataDate thisDate = new DataDate( downloaded.day, downloaded.year);

            // get the files associated with the date in the ListDatesFiles
            ArrayList <String> files = datesFiles.get(thisDate);
            for ( String f :files)
            {
                String strPath = downloaded.dataFilePath;
                strPath = strPath.substring(strPath.lastIndexOf(File.separator)+1, strPath.length());

                // if the file is found in the downloade list, set it to null
                if (f.equalsIgnoreCase(strPath))
                {
                    f = null;
                }
            }

            datesFiles.put(thisDate, files);
        }

        //}catch (ClassNotFoundException | SQLException
        //                | ParserConfigurationException | SAXException | IOException e2) {
        //            // TODO Auto-generated catch block
        //            e2.printStackTrace();
        //        }

        // Step 4: Create downloader and run downloader for all that's left
        for(Map.Entry<DataDate, ArrayList<String>> entry : datesFiles.entrySet())
        {
            String outFolder;

            //outFolder = FileSystem.GetGlobalDownloadDirectory(Config.getInstance(), pluginName);
            outFolder = "D:\\project\\download\\TRMM3B42RT";

            DataDate dd = entry.getKey();

            for (String f : entry.getValue())
            {

                if (f != null)
                {
                    TRMM3B42RTDownloader downloader = new TRMM3B42RTDownloader(dd, outFolder, metaData, f);

                    try{
                        downloader.download();
                    } catch (DownloadFailedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                //                    try {
                //                        AddDownloadFile("data", dd.getYear(), dd.getDayOfYear(), downloader.getOutputFilePath());
                //                    } catch (ClassNotFoundException e) {
                //                        // TODO Auto-generated catch block
                //                        e.printStackTrace();
                //                    } catch (SQLException e) {
                //                        // TODO Auto-generated catch block
                //                        e.printStackTrace();
                //                    }
            }

        }

    }

}
