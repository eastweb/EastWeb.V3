package version2.prototype.download.TRMM3B42RT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.DownloadUtils;
import version2.prototype.download.FTPClientPool;
import version2.prototype.util.ParallelUtils.Parallel;

public class ListDatesToDownload
{
    private List<DataDate> lDates;
    private DataDate sDate;
    private DownloadMetaData mData;

    public ListDatesToDownload(DataDate startDate, DownloadMetaData data) throws IOException
    {
        sDate = startDate;
        mData = data;
        lDates = null;

        if ((mData.mode).equalsIgnoreCase("FTP"))
        {
            lDates = ListDatesFTP();
        };

        if ((mData.mode).equalsIgnoreCase("HTTP"))
        {
            lDates = ListDatesHTTP();
        };
    }

    public List<DataDate> getListDates()
    {
        return lDates;
    }


    private List<DataDate> ListDatesHTTP()
    {
        final List<DataDate> desiredDates = new ArrayList<DataDate>();
        final Lock lock = new ReentrantLock();

        try
        {
            ByteArrayOutputStream folderOutStream = new ByteArrayOutputStream();
            DownloadUtils.downloadToStream(new URL(mData.myHttp.url), folderOutStream);

            Iterable<String> availableDates =
                    Arrays.asList(folderOutStream.toString().split("[\\r\\n]+"));

            // Parallelizing was approximately 5 times faster.
            Parallel.ForEach(availableDates,
                    new Parallel.Operation<String>() {
                @Override
                public void perform(String param)
                {
                    String datePattern = "((19|20)\\d\\d).(0?[1-9]|1[012]).(0?[1-9]|[12][0-9]|3[01])/";
                    Pattern pattern = Pattern.compile(datePattern);
                    Matcher matcher = pattern.matcher(param);

                    if(matcher.find())
                    {
                        try
                        {
                            int year = Integer.parseInt(matcher.group(1));
                            int month = Integer.parseInt(matcher.group(3));
                            int day = Integer.parseInt(matcher.group(4));

                            if(year >= sDate.getYear() && month >= sDate.getMonth() && day >= sDate.getDay())
                            {
                                lock.lock();
                                desiredDates.add(new DataDate(day, month, year));
                                lock.unlock();
                            }
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            return;
                        }
                    }
                };
            });
        }
        catch (Exception e)
        {
            System.out.println("Can't connect to download website, please check your URL.");
            e.printStackTrace();
            return null;
        }

        return desiredDates;
    }

    private List<DataDate> ListDatesFTP()
    {
        final Pattern yearDirPattern = Pattern.compile("\\d{4}");

        FTPClient ftpC = null;

        try
        {
            ftpC = (FTPClient) ConnectionContext.getConnection(mData);
        }
        catch (ConnectException e)
        {
            System.out.println("Can't connect to TRMM download website, please check your URL.");
            return null;
        }

        String mRoot = mData.myFtp.rootDir;
        try
        {
            if (!ftpC.changeWorkingDirectory(mRoot))
            {
                throw new IOException("Couldn't navigate to directory: " + mRoot);
            }

            // List years
            final List<DataDate> list = new ArrayList<DataDate>();
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
                    Pattern tPattern =
                            Pattern.compile("3B42RT_daily\\.(\\d{4})\\.(\\d{2})\\.(\\d{2})\\.bin");

                    if (file.isFile() &&
                            tPattern.matcher(file.getName()).matches())
                    {
                        /* pattern of TRMM 3B42RT
                         * {productname}.%y4.%m2.%d2.7.bin
                         */

                        String[] strings = file.getName().split("[.]");
                        final int month = Integer.parseInt(strings[2]);
                        final int day = Integer.parseInt(strings[3]);
                        DataDate dataDate = new DataDate(day, month, year);
                        if (dataDate.compareTo(sDate) >= 0)
                        {
                            list.add(dataDate);
                        }
                    }
                }
            }

            return list;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }


}


