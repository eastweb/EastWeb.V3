package version2.prototype.download.NldasNOAH;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.ErrorLog;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.ListDatesFiles;

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

            mapDatesFiles =  new HashMap<DataDate, ArrayList<String>>();

            for (FTPFile yearFile : ftpC.listFiles())
            {
                if (!yearFile.isDirectory()
                        || !yearDirPattern.matcher(yearFile.getName())
                        .matches())
                {
                    continue;
                }

                int year = Integer.parseInt(yearFile.getName());
                if (year < sDate.getYear())
                {
                    continue;
                }

                // List days in this year
                String yearDirectory =
                        String.format("%s/%s", mRoot, yearFile.getName());

                if (!ftpC.changeWorkingDirectory(yearDirectory))
                {
                    throw new IOException(
                            "Couldn't navigate to directory: " + yearDirectory);
                }

                for(FTPFile dayFile : ftpC.listFiles())
                {
                    if (!dayFile.isDirectory()
                            || !dayDirPattern.matcher(dayFile.getName())
                            .matches())
                    {
                        continue;
                    }

                    // List 24 hours in this day
                    String dayDirectory =
                            String.format("%s/%s", yearDirectory, dayFile.getName());

                    if (!ftpC.changeWorkingDirectory(dayDirectory))
                    {
                        throw new IOException(
                                "Couldn't navigate to directory: " + dayDirectory);
                    }

                    ArrayList<String> fileNames = new ArrayList<String>();

                    for (FTPFile file : ftpC.listFiles())
                    {
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
                                mapDatesFiles.put(dataDate, fileNames);
                            }
                        }
                    }
                }

            }

            ftpC.disconnect();
            ftpC = null;
            System.out.println("connection is closed " + ftpC);
            return mapDatesFiles;
        }
        catch (Exception e)
        {
            ErrorLog.add(Config.getInstance(), "NldasNOAH", "NldasNOAHListDatesFiles.ListDatesFilesFTP problem while creating list using FTP.", e);
            return null;
        }

    }

}
