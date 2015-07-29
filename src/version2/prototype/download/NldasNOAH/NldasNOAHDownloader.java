package version2.prototype.download.NldasNOAH;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.xml.sax.SAXException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.FTP;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.DownloadUtils;
import version2.prototype.download.DownloaderFramework;
import version2.prototype.download.FTPClientPool;

public class NldasNOAHDownloader extends DownloaderFramework{

    private DataDate mDate;
    private String mOutputFolder;
    private String mMode;
    private String mHost;
    private String mRoot;
    private String mFileToDownload;
    private DownloadMetaData mData;
    private String outFilePath;

    public NldasNOAHDownloader(DataDate date, String outFolder, DownloadMetaData data, String fileToDownload)
    {
        mDate = date;
        mOutputFolder = outFolder;
        mData = data;
        mFileToDownload = fileToDownload;
        setFTPValues();
    }

    // get the values from DownloadMetaData
    private void setFTPValues()
    {
        mMode = mData.mode;
        FTP f = mData.myFtp;
        mHost = f.hostName;
        mRoot = f.rootDir;
    }

    @Override
    public void download() throws IOException, DownloadFailedException, SAXException, Exception
    {
        if (mMode.equalsIgnoreCase("FTP"))
        {
            final FTPClient ftpC =
                    (FTPClient) ConnectionContext.getConnection(mData);
            try {
                final String yearDirectory =
                        String.format("%s/%d", mRoot, mDate.getYear());
                if (!ftpC.changeWorkingDirectory(yearDirectory))
                {
                    throw new IOException("Couldn't navigate to directory: " + yearDirectory);
                }

                final String dayDirectory = String.format("%s/%03d", yearDirectory, mDate.getDayOfYear());

                if (!ftpC.changeWorkingDirectory(dayDirectory))
                {
                    throw new IOException("Couldn't navigate to directory: " + dayDirectory);
                }

                String dir = String.format("%s"+File.separator+"%04d" + File.separator+"%03d",
                        mOutputFolder, mDate.getYear(), mDate.getDayOfYear());

                if(!(new File(dir).exists()))
                {
                    FileUtils.forceMkdir(new File(dir));
                }

                outFilePath = String.format("%s"+File.separator+"%s",dir, mFileToDownload);

                File outputFile = new File(outFilePath);

                DownloadUtils.download(ftpC, mFileToDownload, outputFile);
                //ftpC.disconnect();

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

    @Override
    public String getOutputFilePath()
    {
        return outFilePath;
    }
}
