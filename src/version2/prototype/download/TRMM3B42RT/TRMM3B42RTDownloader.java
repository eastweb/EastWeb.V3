package version2.prototype.download.TRMM3B42RT;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import org.xml.sax.SAXException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.FTP;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.DownloadUtils;
import version2.prototype.download.DownloaderFramework;
import version2.prototype.download.FTPClientPool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;

/*
 * @Author: Yi Liu
 */

public class TRMM3B42RTDownloader extends DownloaderFramework
{
    private DataDate mDate;
    private String mOutputFolder;
    private String mMode;
    private String mHost;
    private String mRoot;
    private DownloadMetaData mData;

    public TRMM3B42RTDownloader(DataDate date, String outFolder, DownloadMetaData data)
    {
        mDate = date;
        mOutputFolder = outFolder;
        mData = data;
        setFTPValues(mData);
    }

    // get the values from DownloadMetaData
    private void setFTPValues(DownloadMetaData data)
    {
        mMode = data.mode;
        FTP f = data.myFtp;
        mHost = f.hostName;
        mRoot = f.rootDir;
    }

    @Override
    public void download() throws IOException, DownloadFailedException,
    Exception, SAXException
    {
        if (mMode.equalsIgnoreCase("FTP"))
        {
            final FTPClient ftpC =
                    (FTPClient) ConnectionContext.getConnection(mData);
            try {
                final String yearDirectory =
                        String.format("%s%d", mRoot, mDate.getYear());
                System.out.println(yearDirectory);
                if (!ftpC.changeWorkingDirectory(yearDirectory))
                {
                    throw new IOException("Couldn't navigate to directory: "
                            + yearDirectory);
                }

                int year = mDate.getYear();
                int month = mDate.getMonth();
                int day = mDate.getDay();

                String fileToDownload =
                        String.format("3B42RT_daily.%04d.%02d.%02d.bin",
                                year, month, day);

                // Save it to the sub-folder with Day_of_year
                LocalDate ld = LocalDate.of(year, month, day);
                int day_of_year = ld.getDayOfYear();

                String dir = String.format("%s\\%04d\\%03d",mOutputFolder,year,day_of_year);

                FileUtils.forceMkdir(new File(dir));

                File outputFile = new File(String.format("%s\\%s",dir,fileToDownload));

                DownloadUtils.download(ftpC, fileToDownload, outputFile);
                ftpC.disconnect();

            } catch (IOException e)
            {
                throw e;
            }
            finally
            {
                FTPClientPool.returnFtpClient(mHost, ftpC);
            }
        } ;
    }
}
