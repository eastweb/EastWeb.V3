/**
 *
 */
package EastWeb_Downloader.TRMM3B42RT;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import EastWeb_Config.Config;
import EastWeb_Config.ConfigReadException;
import EastWeb_Database.DatabaseConnection;
import EastWeb_Database.DatabaseConnector;
import EastWeb_Downloader.DownloadFailedException;
import EastWeb_Downloader.GlobalDownloader;
import EastWeb_Downloader.ListDatesFiles;
import EastWeb_Downloader.RegistrationException;
import EastWeb_ErrorHandling.ErrorLog;
import PluginMetaData.DownloadMetaData;
import Utilies.DataDate;
import Utilies.DataFileMetaData;
import Utilies.DownloadFileMetaData;
import Utilies.FileSystem;

/**
 * @author michael.devos
 * @author Yi Liu
 *
 */
public class TRMM3B42RTGlobalDownloader extends GlobalDownloader {

    public TRMM3B42RTGlobalDownloader(int myID, Config configInstance, String pluginName, DownloadMetaData metaData, ListDatesFiles listDatesFiles, LocalDate startDate) throws ClassNotFoundException,
    ParserConfigurationException, SAXException, IOException, SQLException, RegistrationException {
        super(myID, configInstance, pluginName, metaData, listDatesFiles, startDate);
    }

    @Override
    //get the files to in the TRMM3B42RTListDatesFiles, remove from the list the files downloaded already by checking the cached files in the database
    //Download the files in the list by calling the download() method in TRMM3B42RTDownloader
    public void run()
    {
        DatabaseConnection con = DatabaseConnector.getConnection(configInstance);
        if(con == null) {
            return;
        }
        Statement stmt;
        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "TRMM3B42RTGlobalDownloader.run problem while attempting to download file.", e);
            return;
        }

        // Step 1: get all downloads from ListDatesFiles
        Map<DataDate, ArrayList<String>> datesFiles = listDatesFiles.CloneListDatesFiles();

        // Step 2: Pull all cached downloads
        ArrayList<DataFileMetaData> cachedD = new ArrayList<DataFileMetaData>();

        try {
            cachedD = GetAllDownloadedFiles();

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

        }catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException | RegistrationException e) {
            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "TRMM3B42RTGlobalDownloader.run problem while attempting to download file.", e);
        }

        // Step 4: Create downloader and run downloader for all that's left
        for(Map.Entry<DataDate, ArrayList<String>> entry : datesFiles.entrySet())
        {
            String outFolder;

            try {
                outFolder = FileSystem.GetGlobalDownloadDirectory(configInstance, pluginName, metaData.name);

                DataDate dd = entry.getKey();

                for (String f : entry.getValue())
                {

                    if (f != null)
                    {
                        TRMM3B42RTDownloader downloader = new TRMM3B42RTDownloader(dd, outFolder, metaData, f);

                        try{
                            downloader.download();
                        } catch (IOException | DownloadFailedException | SAXException e) {
                            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "TRMM3B42RTGlobalDownloader.run problem while attempting to download file.", e);
                        } catch (Exception e) {
                            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "TRMM3B42RTGlobalDownloader.run problem while attempting to download file.", e);
                        }


                        try {
                            AddDownloadFile(stmt, dd.getYear(), dd.getDayOfYear(), downloader.getOutputFilePath());
                        } catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException | RegistrationException e) {
                            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "TRMM3B42RTGlobalDownloader.run problem while attempting to add download file.", e);
                        }
                    }
                }
            } catch (ConfigReadException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
        }
        try {
            stmt.close();
        } catch (SQLException e) { /* do nothing */ }
        con.close();
    }

}
