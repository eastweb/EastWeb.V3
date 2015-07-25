package version2.prototype.download.NldasForcing;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.FileSystem;

public class NldasForcingGlobalDownloader extends GlobalDownloader {

    public NldasForcingGlobalDownloader(int myID, String pluginName, DownloadMetaData metaData, ListDatesFiles listDatesFiles) {
        super(myID, pluginName, metaData, listDatesFiles);
    }

    @Override
    public void run()
    {
        Map<DataDate, ArrayList<String>> mapDatesToFiles = listDatesFiles.getListDatesFiles();
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
        catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException | IOException e2) {
            e2.printStackTrace();
        }

        for(Map.Entry<DataDate, ArrayList<String>> entry : mapDatesToFiles.entrySet())
        {
            String outFolder;

            try {
                outFolder = FileSystem.GetGlobalDownloadDirectory(Config.getInstance(), pluginName);

                DataDate dd = entry.getKey();

                for (String f : entry.getValue())
                {
                    NldasForcingDownloader downloader = new NldasForcingDownloader(dd, outFolder, metaData, f);

                    try { downloader.download(); }
                    catch (Exception e1) { e1.printStackTrace(); }

                    try { AddDownloadFile("data", dd.getYear(), dd.getDayOfYear(), downloader.getOutputFilePath()); }
                    catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); }
                }

            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
