package version2.prototype.download.ModisLST;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.DownloadUtils;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.util.ParallelUtils.Parallel;

/*
 * @Author: Fangyu Zhang
 * @Author: Yi Liu
 */

public class ModisLSTListDatesFiles extends ListDatesFiles
{

    public ModisLSTListDatesFiles(DataDate startDate, DownloadMetaData data)
            throws IOException
    {
        super(startDate, data);
    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesFTP() {
        return null;
    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesHTTP()
    {
        mapDatesFiles =  new HashMap<DataDate, ArrayList<String>>();

        final String mHostURL = mData.myHttp.url;

        //final List<DataDate> desiredDates = new ArrayList<DataDate>();
        final Lock lock = new ReentrantLock();

        try
        {
            ByteArrayOutputStream folderOutStream = new ByteArrayOutputStream();
            DownloadUtils.downloadToStream(new URL(mHostURL), folderOutStream);

            Iterable<String> availableDates = Arrays.asList(folderOutStream.toString().split("[\\r\\n]+"));

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
                                String fileFolderURL = mHostURL +
                                        String.format("%04d.%02d.%02d/", year, month, day);

                                ArrayList<String> fileList = new ArrayList<String>();

                                ByteArrayOutputStream dateFolderOutstream = new ByteArrayOutputStream();
                                DownloadUtils.downloadToStream(new URL(fileFolderURL), dateFolderOutstream);

                                //FIXME:  will be retrieved from the metadata
                                String fileNamePatternStr = "MOD11A2.A(\\d{7}).h(\\d{2})v(\\d{2}).005.(\\d{13}).hdf";
                                Pattern fileNamePattern = Pattern.compile(fileNamePatternStr);

                                // extract all the files that matches the filename pattern in the date directory
                                // add each one into the fileList
                                Iterable<String> folderContents = Arrays.asList(dateFolderOutstream.toString().split("[//r//n]+"));
                                for (String line : folderContents)
                                {
                                    Matcher m = fileNamePattern.matcher(line);
                                    if(line.contains(".hdf") && !line.contains(".xml") &&
                                            (m.find()))
                                    {
                                        lock.lock();
                                        fileList.add(m.group(0));
                                        lock.unlock();
                                    }
                                }

                                // add the date and fileList pair to the map
                                lock.lock();
                                mapDatesFiles.put(new DataDate(day, month, year), fileList);
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
            e.printStackTrace();
            return null;
        }

        return mapDatesFiles;
    }

}
