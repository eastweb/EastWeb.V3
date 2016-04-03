package EastWeb_Downloader.NldasNOAH;

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

public class NldasNOAHGlobalDownloader extends GlobalDownloader{

    public NldasNOAHGlobalDownloader(int myID, Config configInstance, String pluginName, DownloadMetaData metaData, ListDatesFiles listDatesFiles, LocalDate startDate) throws ClassNotFoundException, ParserConfigurationException, SAXException,
    IOException, SQLException, RegistrationException {
        super(myID, configInstance, pluginName, metaData, listDatesFiles, startDate);
    }

    @Override
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
            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "NldasNOAHGlobalDownloader.run problem while running NldasNOAHDownloader.", e);
            return;
        }

        // Step 1: get all downloads from ListDatesFiles
        Map<DataDate, ArrayList<String>> datesFiles = listDatesFiles.CloneListDatesFiles();

        // Step 2: Pull all cached downloads
        //ArrayList<DataFileMetaData> cachedD = new ArrayList<DataFileMetaData>();
        ArrayList<DataFileMetaData> cachedD = null;

        try {
            cachedD = GetAllDownloadedFiles();

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

        } catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException | RegistrationException e) {
            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "NldasNOAHGlobalDownloader.run problem while running NldasNOAHDownloader.", e);
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
                    if(f != null)
                    {
                        NldasNOAHDownloader downloader = new NldasNOAHDownloader(dd, outFolder, metaData, f);

                        try {
                            downloader.download();
                        } catch (DownloadFailedException e) {
                            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "NldasNOAHGlobalDownloader.run problem while running NldasNOAHDownloader.", e);
                        } catch (Exception e) {
                            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "NldasNOAHGlobalDownloader.run problem while running NldasNOAHDownloader.", e);
                        }
                    }

                }
            } catch (IOException e) {
                ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "NldasNOAHGlobalDownloader.run problem while attempting to handle downloading.", e);
            }
        }
        try {
            stmt.close();
        } catch (SQLException e) { /* do nothing */ }
        con.close();
    }

}
