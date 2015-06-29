package version2.prototype.download.ModisNBAR;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.xml.sax.SAXException;

import version2.prototype.ConfigReadException;
import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.ModisTile;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.DownloadUtils;
import version2.prototype.download.DownloaderFramework;
import version2.prototype.util.ParallelUtils.Parallel;

public final class ModisNBARDownloader extends DownloaderFramework {
    private final DataDate mDate;
    private final ModisTile mTile;
    private final String mOutDataFolder;
    private final String mOutQCFolder;
    private static DownloadMetaData metaData;

    private String mHostURL;
    private String mMode;

    private String mHostName;
    private String mUsername;
    private String mPassword;
    private String mFtpRootDir;

    public ModisNBARDownloader(DataDate date, ModisTile tile, String outFolder, String qcOutFolder, DownloadMetaData data) {
        mDate = date;
        mTile = tile;
        mOutDataFolder = outFolder;
        mOutQCFolder = qcOutFolder;
        metaData = data;
        mMode = metaData.mode;

        if(metaData.mode == "http") {
            mHostURL = metaData.myHttp.url;
        } else if(metaData.mode == "ftp"){
            mHostName = metaData.myFtp.hostName;
            mUsername = metaData.myFtp.userName;
            mPassword = metaData.myFtp.password;
            mFtpRootDir = metaData.myFtp.rootDir;
        }
    }

    public ModisNBARDownloader(DataDate date, ModisTile tile, String outFolder, String qcOutFolder, String mode, String hostURL) {
        mDate = date;
        mTile = tile;
        mOutDataFolder = outFolder;
        mOutQCFolder = qcOutFolder;
        mMode = mode;
        mHostURL = hostURL;
    }

    public ModisNBARDownloader(DataDate date, ModisTile tile, String outFolder, String qcOutFolder, String mode, String hostName, String userName, String password, String rootDir) {
        mDate = date;
        mTile = tile;
        mOutDataFolder = outFolder;
        mOutQCFolder = qcOutFolder;
        mMode = mode;
        mHostName = hostName;
        mUsername = userName;
        mPassword = password;
        mFtpRootDir = rootDir;
    }

    /**
     * Builds and returns a list containing all of the available data dates no
     * earlier than the specified start date.
     *
     * @param startDate
     *            Specifies the inclusive lower bound for the returned data date
     *            list
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    protected static final List<DataDate> listDates(final DataDate startDate)
            throws ConfigReadException, IOException, ParserConfigurationException, SAXException {
        final List<DataDate> desiredDates = new ArrayList<DataDate>();
        final Lock lock = new ReentrantLock();

        try
        {
            ByteArrayOutputStream folderOutStream = new ByteArrayOutputStream();
            DownloadUtils.downloadToStream(new URL(metaData.myHttp.url), folderOutStream);

            Iterable<String> availableDates = Arrays.asList(folderOutStream.toString().split("[\\r\\n]+"));

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

                            if(year >= startDate.getYear() && month >= startDate.getMonth() && day >= startDate.getDay())
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
            e.printStackTrace();
            return null;
        }

        return desiredDates;
    }

    @Override
    public final void download() throws IOException, ConfigReadException,
    DownloadFailedException, SAXException, Exception
    {
        if(!(new File(mOutDataFolder).exists())) {
            FileUtils.forceMkdir(new File(mOutDataFolder));
        }
        if(!(new File(mOutQCFolder).exists())) {
            FileUtils.forceMkdir(new File(mOutQCFolder));
        }

        if (mMode == "http")
        {
            try
            {
                String urlString = getFileURL("MCD43B4.005");
                String qcUrlString = getFileURL("MCD43B2.005");
                URL fileURL = new URL(urlString);
                URL qcFileURL = new URL(qcUrlString);

                if(fileURL != null)
                {

                    DownloadUtils.downloadToFile(fileURL,
                            new File(mOutDataFolder + "\\" + urlString.substring(urlString.lastIndexOf("MCD43B4"))));
                }

                if(qcFileURL != null)
                {
                    DownloadUtils.downloadToFile(qcFileURL,
                            new File(mOutQCFolder + "\\" + qcUrlString.substring(qcUrlString.lastIndexOf("MCD43B2"))));
                }
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
                return;
            }
            catch (IOException eIO)
            {
                eIO.printStackTrace();
                return;
            }
        }
        else if(mMode == "ftp")
        {
            // Untested code, only in the case that the method should change (current access method is http) 6/7/2015
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(mHostName);
            ftpClient.login(mUsername, mPassword);

            ftpClient.changeWorkingDirectory(mFtpRootDir + String.format("MCD43B4.005/%04d.%02d.%02d/", mDate.getYear(), mDate.getMonth(), mDate.getDay()));
            FTPFile[] files = ftpClient.listFiles();
            for(FTPFile file : files)
            {
                String details = file.getName();
                if(details.contains(".hdf") && !details.contains(".xml") &&
                        details.contains(String.format("h%02dv%02d", mTile.getHTile(), mTile.getVTile())))
                {
                    DownloadUtils.download(ftpClient, details,
                            new File(mOutDataFolder + "\\" + details));
                }
            }

            ftpClient.changeWorkingDirectory(mFtpRootDir + String.format("MCD43B2.005/%04d.%02d.%02d/", mDate.getYear(), mDate.getMonth(), mDate.getDay()));
            FTPFile[] qcFiles = ftpClient.listFiles();
            for(FTPFile file : qcFiles)
            {
                String details = file.getName();
                if(details.contains(".hdf") && !details.contains(".xml") &&
                        details.contains(String.format("h%02dv%02d", mTile.getHTile(), mTile.getVTile())))
                {
                    DownloadUtils.download(ftpClient, details,
                            new File(mOutQCFolder + "\\" + details));
                }
            }
        }
    }

    public final List<String> getAllUrlsFromStartDate(DataDate startDate, final ModisTile desiredTile, final String hostURL) throws MalformedURLException, IOException
    {
        final ArrayList<String> fullURLs = new ArrayList<String>();
        final Lock lock = new ReentrantLock();

        try
        {
            List<DataDate> desiredDates = listDates(startDate);
            if(desiredDates != null && !desiredDates.isEmpty())
            {
                // Parallelizing was approximately 40% faster than sequential (Getting the stream is the primary bottleneck)
                Parallel.ForEach(desiredDates,
                        new Parallel.Operation<DataDate>() {
                    @Override
                    public void perform(DataDate date)
                    {
                        try
                        {
                            ByteArrayOutputStream filesOutStream = new ByteArrayOutputStream();
                            DownloadUtils.downloadToStream(new URL(hostURL + String.format("%04d.%02d.%02d/", date.getYear(), date.getMonth(), date.getDay())), filesOutStream);

                            Iterable<String> folderContents = Arrays.asList(filesOutStream.toString().split("[//r//n]+"));

                            for (String line : folderContents)
                            {
                                //Shorten the list as much as possible, restricting to only files that have ".hdf" extension
                                if(line.contains(".hdf") && !line.contains(".xml") &&
                                        line.contains(String.format("h%02dv%02d", desiredTile.getHTile(), desiredTile.getVTile())))
                                {
                                    lock.lock();
                                    fullURLs.add(hostURL + String.format("%04d.%02d.%.02d/", date.getYear(), date.getMonth(), date.getDay()) + line.substring(line.indexOf("MCD43B4"), line.indexOf(".hdf") + 4));
                                    lock.unlock();
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    };
                });
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return fullURLs;
    }

    private String getFileURL(String parentFolder) throws MalformedURLException, IOException
    {
        String fileURL = null;

        String containingURL = mHostURL + String.format("%s/%04d.%02d.%02d/", parentFolder, mDate.getYear(), mDate.getMonth(), mDate.getDay());
        ByteArrayOutputStream directoryOutstream = new ByteArrayOutputStream();
        DownloadUtils.downloadToStream(new URL(containingURL), directoryOutstream);

        Iterable<String> folderContents = Arrays.asList(directoryOutstream.toString().split("[//r//n]+"));
        for (String line : folderContents)
        {
            //Shorten the list as much as possible, restricting to only files that have ".hdf" extension
            if(line.contains(".hdf") && !line.contains(".xml") &&
                    line.contains(String.format("h%02dv%02d", mTile.getHTile(), mTile.getVTile())))
            {
                fileURL = (containingURL + line.substring(line.indexOf(parentFolder.substring(0, parentFolder.indexOf('.'))), line.indexOf(".hdf") + 4));
                break;
            }
        }
        return fileURL;
    }
}


