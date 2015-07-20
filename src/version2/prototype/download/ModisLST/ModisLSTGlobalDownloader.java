package version2.prototype.download.ModisLST;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;

public class ModisLSTGlobalDownloader extends GlobalDownloader
{

    public ModisLSTGlobalDownloader(int myID, String pluginName,
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
        //cachedD = GetAllDownloadedFiles();

        //REMOVE
        //for testing.  remove the following afterGetAllDownloadedFiles() is implemented
        DataFileMetaData d1 = new DataFileMetaData(1, "D:\\project\\download\\ModisLST\\2015\\161\\h19v03.hdf", null, null, 0, 2015, 161);
        DataFileMetaData d2 = new DataFileMetaData(1, "D:\\project\\download\\TRMM3B42RT\\2015\\169\\h19v03.hdf", null, null, 0, 2015, 169);
        cachedD.add(d1);
        cachedD.add(d2);


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
            outFolder = "D:\\project\\download\\ModisLST";

            DataDate dd = entry.getKey();

            for (String f : entry.getValue())
            {

                if (f != null)
                {
                    ModisLSTDownloader downloader = new ModisLSTDownloader(dd, outFolder, metaData, f);

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
