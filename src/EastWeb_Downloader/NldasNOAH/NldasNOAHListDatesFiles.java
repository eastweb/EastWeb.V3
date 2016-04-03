package EastWeb_Downloader.NldasNOAH;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import EastWeb_Config.Config;
import EastWeb_Downloader.ConnectionContext;
import EastWeb_Downloader.ListDatesFiles;
import EastWeb_ErrorHandling.ErrorLog;
import PluginMetaData.DownloadMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import Utilies.DataDate;


public class NldasNOAHListDatesFiles extends ListDatesFiles{

    public NldasNOAHListDatesFiles(DataDate date, DownloadMetaData data, ProjectInfoFile project) throws IOException
    {
        super(date, data, project);
    }


    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesHTTP()
    {
        return null;
    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesFTP()
    {
        Map<DataDate, ArrayList<String>>  tempMapDatesToFiles = new HashMap<DataDate, ArrayList<String>>();
        System.out.println(sDate);
        final Pattern yearDirPattern = Pattern.compile("\\d{4}");
        final Pattern dayDirPattern = Pattern.compile("\\d{3}");

        FTPClient ftpC = null;

        try
        {
            ftpC = (FTPClient) ConnectionContext.getConnection(mData);
        }
        catch (ConnectException e)
        {
            System.out.println("Can't connect to download website, please check your URL.");
            return null;
        }

        String mRoot = mData.myFtp.rootDir;
        try
        {
            if (!ftpC.changeWorkingDirectory(mRoot))
            {
                throw new IOException("Couldn't navigate to directory: " + mRoot);
            }

            tempMapDatesToFiles =  new HashMap<DataDate, ArrayList<String>>();

            outerLoop: for (FTPFile yearFile : ftpC.listFiles())
            {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }

                if (!yearFile.isDirectory()
                        || !yearDirPattern.matcher(yearFile.getName()).matches()) {
                    continue;
                }

                int year = Integer.parseInt(yearFile.getName());
                if (year < sDate.getYear()) {
                    continue;
                }

                // List days in this year
                String yearDirectory =
                        String.format("%s/%s", mRoot, yearFile.getName());

                if (!ftpC.changeWorkingDirectory(yearDirectory)) {
                    throw new IOException(
                            "Couldn't navigate to directory: " + yearDirectory);
                }

                for(FTPFile dayFile : ftpC.listFiles())
                {
                    if (!dayFile.isDirectory()
                            || !dayDirPattern.matcher(dayFile.getName()).matches()) {
                        continue;
                    }

                    // List 24 hours in this day
                    String dayDirectory =
                            String.format("%s/%s", yearDirectory, dayFile.getName());

                    if (!ftpC.changeWorkingDirectory(dayDirectory)) {
                        throw new IOException(
                                "Couldn't navigate to directory: " + dayDirectory);
                    }

                    ArrayList<String> fileNames = new ArrayList<String>();

                    for (FTPFile file : ftpC.listFiles())
                    {
                        if(Thread.currentThread().isInterrupted()) {
                            break outerLoop;
                        }

                        if (file.isFile() &&
                                mData.fileNamePattern.matcher(file.getName()).matches())
                        {
                            /* pattern of NLDASNOAH
                             * {productname}.A%y4%m2%d2.%h4.002.grb
                             */

                            fileNames.add(file.getName());

                            String[] strings = file.getName().split("[.]");
                            final int month = Integer.parseInt(strings[1].substring(5, 7));
                            final int day = Integer.parseInt(strings[1].substring(7, 9));
                            final int hour = Integer.parseInt(strings[2]);
                            DataDate dataDate = new DataDate(hour, day, month, year);
                            if (dataDate.compareTo(sDate) >= 0)
                            {
                                tempMapDatesToFiles.put(dataDate, fileNames);
                            }
                        }
                    }
                }

            }

            ftpC.disconnect();
            ftpC = null;
            return tempMapDatesToFiles;
        }
        catch (Exception e)
        {
            ErrorLog.add(Config.getInstance(), "NldasNOAH", mData.name, "NldasNOAHListDatesFiles.ListDatesFilesFTP problem while creating list using FTP.", e);
            return null;
        }

    }

}
