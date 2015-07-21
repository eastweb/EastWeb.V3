package version2.prototype.download.NldasNOAH;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.FileSystem;

public class NldasNOAHGlobalDownloader extends GlobalDownloader{

    public NldasNOAHGlobalDownloader(int myID, String pluginName,  DownloadMetaData metaData, ListDatesFiles listDatesFiles) {
        super(myID, pluginName, metaData, listDatesFiles);
    }

    @Override
    public void run()
    {
        // Step 1: get all downloads from ListDatesFiles
        Map<DataDate, ArrayList<String>> datesFiles = listDatesFiles.getListDatesFiles();

        // Step 2: Pull all cached downloads
        //ArrayList<DataFileMetaData> cachedD = new ArrayList<DataFileMetaData>();
        ArrayList<DataFileMetaData> cachedD = null;

        try {
            cachedD = GetAllDownloadedFiles();

            //Testing Only
            //DataFileMetaData d1 = new DataFileMetaData(1, "data","C:\\project\\download\\NLDAS_NOAH0125_H.002\\2015\\196\\NLDAS_NOAH0125_H.A20150715.0000.002.grb",  null, 0, 2015, 196);
            //DataFileMetaData d2 = new DataFileMetaData(1, "data", "C:\\project\\download\\NLDAS_NOAH0125_H.002\\2015\\196\\NLDAS_NOAH0125_H.A20150715.0100.002.grb", null, 0, 2015, 196);
            //cachedD.add(d1);
            //cachedD.add(d2);

            // Step 3: Remove already downloaded files from ListDatesFiles
            for (DataFileMetaData d: cachedD)
            {
                DownloadFileMetaData downloaded =  d.ReadMetaDataForProcessor();
                // get the year and dayOfyear from each downloaded file
                DataDate thisDate = new DataDate(downloaded.day, downloaded.year);

                // get the files associated with the date in the ListDatesFiles
                ArrayList <String> files = datesFiles.get(thisDate);

                Iterator<String> fIter = files.iterator();

                while (fIter.hasNext())
                {
                    String strPath = downloaded.dataFilePath;
                    System.out.println(strPath);
                    strPath = strPath.substring(strPath.lastIndexOf(File.separator)+1, strPath.lastIndexOf("."));
                    // remove the file if it is found in the downloaded list
                    if ((fIter.next().toLowerCase()).contains((strPath.toLowerCase())))
                    {
                        fIter.remove();
                    }
                }

                datesFiles.put(thisDate, files);
                //                for ( String f :files)
                //                {
                //                    String strPath = downloaded.dataFilePath;
                //                    strPath = strPath.substring(strPath.lastIndexOf(File.separator)+1, strPath.length());
                //
                //                    // if the file is found in the downloade list, remove it
                //                    if (f.equalsIgnoreCase(strPath))
                //                    {
                //                        files.remove(f);
                //                    }
                //                }

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

            //outFolder = FileSystem.GetGlobalDownloadDirectory(Config.getInstance(), pluginName);
            outFolder = "D:\\project\\download\\NLDASNOAH";

            DataDate dd = entry.getKey();

            for (String f : entry.getValue())
            {
                if(f != null)
                {
                    NldasNOAHDownloader downloader = new NldasNOAHDownloader(dd, outFolder, metaData, f);

                    try {
                        downloader.download();
                    } catch (DownloadFailedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

            }
        }

    }

}
