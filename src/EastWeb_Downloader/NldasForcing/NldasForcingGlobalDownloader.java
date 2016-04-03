package EastWeb_Downloader.NldasForcing;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseConnection;
import EastWeb_Database.DatabaseConnector;
import EastWeb_Downloader.GlobalDownloader;
import EastWeb_Downloader.ListDatesFiles;
import EastWeb_Downloader.RegistrationException;
import EastWeb_ErrorHandling.ErrorLog;
import PluginMetaData.DownloadMetaData;
import Utilies.DataDate;
import Utilies.DataFileMetaData;
import Utilies.DownloadFileMetaData;
import Utilies.FileSystem;

public class NldasForcingGlobalDownloader extends GlobalDownloader {

    public NldasForcingGlobalDownloader(int myID, Config configInstance, String pluginName, DownloadMetaData metaData, ListDatesFiles listDatesFiles, LocalDate startDate) throws ClassNotFoundException, ParserConfigurationException, SAXException,
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
            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "NldasForcingGlobalDownloader.run problem while attempting to handle download.", e);
            return;
        }

        Map<DataDate, ArrayList<String>> mapDatesToFiles = listDatesFiles.CloneListDatesFiles();
        try
        {
            for(DataFileMetaData dataMetaData : GetAllDownloadedFiles())
            {
                DownloadFileMetaData downloaded = dataMetaData.ReadMetaDataForProcessor();
                DataDate downloadDate = new DataDate(downloaded.day, downloaded.year);

                ArrayList<String> files = mapDatesToFiles.get(downloadDate);
                if(files != null)
                {
                    // Iterate through method return rather than the collection being changed.
                    for (String file : mapDatesToFiles.get(downloadDate))
                    {
                        String fileName = downloaded.dataFilePath.substring(downloaded.dataFilePath.lastIndexOf(File.separator)+1,
                                downloaded.dataFilePath.length());
                        if(file.equalsIgnoreCase(fileName)) {
                            files.remove(file);
                        }
                    }
                }
                mapDatesToFiles.put(downloadDate, files);
            }
        }
        catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException | RegistrationException e) {
            ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "NldasForcingGlobalDownloader.run problem while setting up current list of missing download files.", e);
        }

        for(Map.Entry<DataDate, ArrayList<String>> entry : mapDatesToFiles.entrySet())
        {
            String outFolder;

            try {
                outFolder = FileSystem.GetGlobalDownloadDirectory(configInstance, pluginName, metaData.name);

                DataDate dd = entry.getKey();

                for (String f : entry.getValue())
                {
                    NldasForcingDownloader downloader = new NldasForcingDownloader(dd, outFolder, metaData, f);

                    try {
                        downloader.download();
                    }
                    catch (Exception e) {
                        ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "NldasForcingGlobalDownloader.run problem while running NldasForcingDownloader.", e);
                    }

                    try {
                        AddDownloadFile(stmt, dd.getYear(), dd.getDayOfYear(), downloader.getOutputFilePath());
                    }
                    catch (ClassNotFoundException | SQLException | RegistrationException e) {
                        ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "NldasForcingGlobalDownloader.run problem while attempting to add download file.", e);
                    }
                }

            } catch (ParserConfigurationException | SAXException | IOException e) {
                ErrorLog.add(Config.getInstance(), pluginName, metaData.name, "NldasForcingGlobalDownloader.run problem while attempting to handle download.", e);
            }
        }
        try {
            stmt.close();
        } catch (SQLException e) { /* do nothing */ }
        con.close();
    }
}
