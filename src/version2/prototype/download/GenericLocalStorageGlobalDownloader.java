/**
 *
 */
package version2.prototype.download;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.FileSystem;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class GenericLocalStorageGlobalDownloader extends GlobalDownloader {
    private Constructor<?> downloadCtr;

    /**
     * @param myID
     * @param pluginName
     * @param metaData
     * @param listDatesFiles
     * @param downloaderClassName
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public GenericLocalStorageGlobalDownloader(int myID, String pluginName, DownloadMetaData metaData, ListDatesFiles listDatesFiles, String downloaderClassName)
            throws ClassNotFoundException, NoSuchMethodException, SecurityException
    {
        super(myID, pluginName, metaData, listDatesFiles);
        Class<?> downloaderClass = Class.forName(downloaderClassName);
        downloadCtr = downloaderClass.getConstructor(DataDate.class, String.class, DownloadMetaData.class, String.class);
    }

    @Override
    /*
     * Step 1: Get all downloads from ListDatesFiles
     * Step 2: Pull all cached downloads
     * Step 3: Remove already downloaded files from ListDatesFiles
     * Step 4: Create downloader and run downloader for all that's left
     */
    public void run() {
        try {
            // Step 1: Get all downloads from ListDatesFiles
            Map<DataDate, ArrayList<String>> datesFiles = listDatesFiles.getListDatesFiles();

            // Step 2: Pull all cached downloads
            ArrayList<DataFileMetaData> cachedD = GetAllDownloadedFiles();

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
            }

            // Step 4: Create downloader and run downloader for all that's left
            for(Map.Entry<DataDate, ArrayList<String>> entry : datesFiles.entrySet())
            {
                String outFolder = FileSystem.GetGlobalDownloadDirectory(Config.getInstance(), pluginName);
                DataDate dd = entry.getKey();

                for (String f : entry.getValue())
                {
                    if (f != null)
                    {
                        DownloaderFramework downloader = (DownloaderFramework) downloadCtr.newInstance(dd, outFolder, metaData, f);
                        try {
                            downloader.download();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
