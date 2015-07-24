package version2.prototype.download.ModisNBAR;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.ModisNBAR.ModisNBARDownloader;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;

//@Author: Yi Liu
public class ModisNBARGlobalDownloader extends GlobalDownloader
{
    public ModisNBARGlobalDownloader(int myID, String pluginName,
            DownloadMetaData metaData, ListDatesFiles listDatesFiles) {
        super(myID, pluginName, metaData, listDatesFiles);
        // TODO Auto-generated constructor stub
    }

    @Override
    /*
     * Step 1: get all downloads from ListDatesFiles
     * Step 2: Pull all cached downloads
     * Step 3: Remove already downloaded files from ListDatesFiles
     * Step 4: Create downloader and run downloader for all that's left
     */
    public void run()
    {
        // Step 1: get all downloads from ListDatesFiles
        Map<DataDate, ArrayList<String>> datesFiles = listDatesFiles.getListDatesFiles();

        // Step 2: Pull all cached downloads
        ArrayList<DataFileMetaData> cachedD = new ArrayList<DataFileMetaData>();
        //try {

        //WRITEBACK
        try {
            cachedD = GetAllDownloadedFiles();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
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

        // Step 3: Remove already downloaded files from ListDatesFiles
        for (DataFileMetaData d: cachedD)
        {
            DownloadFileMetaData downloaded =  d.ReadMetaDataForProcessor();

            // get the year and dayOfyear from each downloaded file
            DataDate thisDate = new DataDate( downloaded.day, downloaded.year);

            // get the files associated with the date in the ListDatesFiles
            ArrayList <String> files = datesFiles.get(thisDate);

            Iterator<String> fIter = files.iterator();

            while (fIter.hasNext())
            {
                String strPath = downloaded.dataFilePath;
                strPath = strPath.substring(strPath.lastIndexOf(File.separator)+1, strPath.lastIndexOf("."));

                // remove the file if it is found in the downloaded list
                if ((fIter.next().toLowerCase()).contains((strPath.toLowerCase())))
                {
                    fIter.remove();
                }
            }

            datesFiles.put(thisDate, files);
        }

        //        for (Map.Entry<DataDate, ArrayList<String>> entry : datesFiles.entrySet())
        //        {
        //            System.out.println("dateFile: " + entry.getKey() + " : /" + entry.getValue().size() + "\n");
        //        }

        //WRITEBACK after GetAllDownloadedFiles() is implemented
        //}catch (ClassNotFoundException | SQLException
        //                | ParserConfigurationException | SAXException | IOException e2) {
        //            // TODO Auto-generated catch block
        //            e2.printStackTrace();
        //        }

        // Step 4
        for(Map.Entry<DataDate, ArrayList<String>> entry : datesFiles.entrySet())
        {
            String outFolder;

            //WRITEBACK after config is integrated
            //outFolder = FileSystem.GetGlobalDownloadDirectory(Config.getInstance(), pluginName);

            //REMOVE the following statement, for testing only
            outFolder = "D:\\project\\download\\ModisNBAR";

            DataDate dd = entry.getKey();

            for (String f : entry.getValue())
            {

                if (f != null)
                {
                    ModisNBARDownloader downloader = new ModisNBARDownloader(dd, outFolder, metaData, f);

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

                // WRITEBACK after AddDownloadFile is implemented
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
