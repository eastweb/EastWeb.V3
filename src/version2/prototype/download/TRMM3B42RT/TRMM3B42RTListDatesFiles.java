package version2.prototype.download.TRMM3B42RT;

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
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.ListDatesFiles;

/*
 * @Author: Yi Liu
 */

public class TRMM3B42RTListDatesFiles extends ListDatesFiles
{

    public TRMM3B42RTListDatesFiles(DataDate date, DownloadMetaData data, ProjectInfoFile project) throws IOException
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
        final Pattern yearDirPattern = mData.datePattern;

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

            // List years
            for (FTPFile yearFile : ftpC.listFiles())
            {
                // Skip non-directory, non-year entries
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

                for (FTPFile file : ftpC.listFiles())
                {
                    if (file.isFile() &&
                            mData.fileNamePattern.matcher(file.getName()).matches())
                    {
                        /* pattern of TRMM 3B42RT
                         * {productname}.%y4.%m2.%d2.bin
                         */

                        ArrayList<String> fileNames = new ArrayList<String>();
                        fileNames.add(file.getName());

                        String[] strings = file.getName().split("[.]");
                        final int month = Integer.parseInt(strings[2]);
                        final int day = Integer.parseInt(strings[3]);
                        DataDate dataDate = new DataDate(day, month, year);
                        if (dataDate.compareTo(sDate) >= 0)
                        {
                            mapDatesFiles.put(dataDate, fileNames);
                        }
                    }
                }

            }

            ftpC.disconnect();
            ftpC = null;
            return mapDatesFiles;
        }
        catch (Exception e)
        {
            ErrorLog.add(Config.getInstance(), "TRMM3B42RT", mData.name, "TRMM3B42RTListDatesFiles.ListDatesFilesFTP problem while creating list using FTP.", e);
            return null;
        }

    }

}


