package version2.prototype.download.NldasForcing;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.xml.sax.SAXException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.DownloadUtils;
import version2.prototype.download.DownloaderFramework;
import version2.prototype.download.FTPClientPool;

public class NldasForcingDownloader extends DownloaderFramework
{
    private DownloadMetaData mData;
    private DataDate mDate;
    private String mOutputFolder;
    private String mMode;
    private String mHostName;
    private String mUsername;
    private String mPassword;
    private String mRootDir;
    private String mFileToDownload;
    private String outFilePath;

    public NldasForcingDownloader(DataDate date, String outFolder, DownloadMetaData metaData, String fileToDownload)
    {
        mDate = date;
        mOutputFolder = outFolder;
        mData = metaData;
        mMode = metaData.mode;
        mFileToDownload = fileToDownload;
        outFilePath = null;

        mHostName = metaData.myFtp.hostName;
        mUsername = metaData.myFtp.userName;
        mPassword = metaData.myFtp.password;
        mRootDir = metaData.myFtp.rootDir;
    }

    @Override
    public void download() throws IOException, DownloadFailedException, Exception, SAXException, Exception
    {
        String outDesStr = String.format("%s\\%04d\\%03d", mOutputFolder, mDate.getYear(), mDate.getDayOfYear());
        File outputDestination = new File(outDesStr);

        if(mMode.equalsIgnoreCase("FTP"))
        {
            FTPClient ftpClient = FTPClientPool.getFtpClient(mHostName, mUsername, mPassword);
            try
            {
                if(!ftpClient.changeWorkingDirectory(mRootDir + String.format("/%04d/%03d/", mDate.getYear(), mDate.getDayOfYear()))) {
                    throw new IOException("Couldn't navigate to " + mData.myFtp.rootDir + String.format("/%04d/%03d/", mDate.getYear(), mDate.getDayOfYear()));
                }

                if(!outputDestination.exists()) {
                    FileUtils.forceMkdir(outputDestination);
                }

                outFilePath = String.format("%s"+File.separator+"%s", outDesStr, mFileToDownload);

                DownloadUtils.download(ftpClient, mFileToDownload, new File(outFilePath));
            }
            catch (IOException e) { throw e; }
            finally
            {
                if(ftpClient.isConnected())
                {
                    // Close the FTP session.
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
                FTPClientPool.returnFtpClient(mHostName, ftpClient);
            }

            // Alternative code in case I shouldn't be using the FTPCLientPool class
            /*File outputDestination = new File(String.format("%s\\%04d\\%03d", mOutputFolder, mDate.getYear(), mDate.getDayOfYear()));
            FTPClient ftpClient = new FTPClient();
            try
            {
                ftpClient.connect(mHostName);
                if(!ftpClient.login(mUsername, mPassword)){
                    throw new IOException("Wasn't able to login to remote host with provided credentials.");
                }

                ftpClient.enterLocalPassiveMode();

                if(!ftpClient.changeWorkingDirectory(mData.myFtp.rootDir + String.format("/%04d/%03d/", mDate.getYear(), mDate.getDayOfYear()))){
                    throw new IOException("Couldn't navigate to " + mData.myFtp.rootDir + String.format("/%04d/%03d/", mDate.getYear(), mDate.getDayOfYear()));
                }

                if(!new File(mOutputFolder).exists()) {
                    FileUtils.forceMkdir(new File(mOutputFolder));
                }

                outFilePath = String.format("%s"+File.separator+"%s", outDesStr, mFileToDownload);

                DownloadUtils.download(ftpClient, mFileToDownload, new File(outFilePath));
            }
            catch (Exception e) { throw e; }
            finally
            {
                if(ftpClient.isConnected())
                {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            }*/
        }
    }

    @Override
    public String getOutputFilePath() {
        return outFilePath;
    }


}

