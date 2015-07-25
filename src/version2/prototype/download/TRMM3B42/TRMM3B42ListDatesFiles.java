package version2.prototype.download.TRMM3B42;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.xml.sax.SAXException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.ListDatesFiles;


public class TRMM3B42ListDatesFiles extends ListDatesFiles
{

    public TRMM3B42ListDatesFiles(DataDate startDate, DownloadMetaData data)
            throws IOException {
        super(startDate, data);
    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesFTP() {
        final Pattern yearDirPattern = mData.datePattern;
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
                String yearDirectory = String.format("%s%s", mRoot, yearFile.getName());

                if (!ftpC.changeWorkingDirectory(yearDirectory))
                {
                    throw new IOException(
                            "Couldn't navigate to directory: " + yearDirectory);
                }

                for (FTPFile dayFolder : ftpC.listFiles())
                {
                    if (!dayFolder.isDirectory()
                            || !dayDirPattern.matcher(dayFolder.getName()).matches())
                    { continue; }

                    int day = Integer.parseInt(dayFolder.getName());
                    if (day < sDate.getDayOfYear())
                    { continue; }

                    // List files in the day
                    String dayDirectory =  String.format("%s/%s", yearDirectory, dayFolder.getName());

                    if (!ftpC.changeWorkingDirectory(dayDirectory))
                    {
                        throw new IOException(
                                "Couldn't navigate to directory: " + dayFolder);
                    }

                    for (FTPFile fileFile : ftpC.listFiles())
                    {
                        /* pattern of TRMM 3B42
                         * {productname}.%y4.%m2.%d2.7.bin
                         */

                        String fileName = fileFile.getName();
                        if (!fileName.contains("xml"))
                        {
                            ArrayList<String> fileNames = new ArrayList<String>();
                            fileNames.add(fileName);

                            String[] strings = fileName.split("[.]");
                            final int month = Integer.parseInt(strings[2]);
                            final int thisday = Integer.parseInt(strings[3]);
                            DataDate dataDate = new DataDate(thisday, month, year);
                            if (dataDate.compareTo(sDate) >= 0)
                            {
                                mapDatesFiles.put(dataDate, fileNames);
                            }
                        }
                    }
                }

            }
            System.out.println(mapDatesFiles.size());

            ftpC.disconnect();
            ftpC = null;

            return mapDatesFiles;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesHTTP() {
        return null;
    }

}
