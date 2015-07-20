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

    public NldasForcingDownloader(DataDate date, String outFolder, DownloadMetaData metaData)
    {
        mDate = date;
        mOutputFolder = outFolder;
        mData = metaData;
        mMode = metaData.mode;

        mHostName = metaData.myFtp.hostName;
        mUsername = metaData.myFtp.userName;
        mPassword = metaData.myFtp.password;
        mRootDir = metaData.myFtp.rootDir;
    }

    @Override
    public void download() throws IOException, DownloadFailedException, Exception, SAXException
    {
        File outputDestination = new File(String.format("%s\\%04d\\%03d", mOutputFolder, mDate.getYear(), mDate.getDayOfYear()));

        if(mMode.equalsIgnoreCase("FTP"))
        {
            FTPClient ftpClient = FTPClientPool.getFtpClient(mHostName, mUsername, mPassword);
            try
            {
                if(!ftpClient.changeWorkingDirectory(mRootDir + String.format("/%04d/%03d/", mDate.getYear(), mDate.getDayOfYear()))) {
                    throw new IOException("Couldn't navigate to " + mData.myFtp.rootDir + String.format("/%04d/%03d/", mDate.getYear(), mDate.getDayOfYear()));
                }

                String desiredFile = String.format("NLDAS_FORA0125_H.A%04d%02d%02d.%02d00.002.grb", mDate.getYear(), mDate.getMonth(), mDate.getDay(), mDate.getHour());

                if(!outputDestination.exists()) {
                    FileUtils.forceMkdir(outputDestination);
                }

                DownloadUtils.download(ftpClient, desiredFile, new File(outputDestination.getAbsolutePath() + "\\" + desiredFile));
            }
            catch (IOException e) { throw e; }
            finally { FTPClientPool.returnFtpClient(mHostName, ftpClient); }

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

                String desiredFile = String.format("NLDAS_FORA0125_H.A%04d%02d%02d.%02d00.002.grb", mDate.getYear(), mDate.getMonth(), mDate.getDay(), mDate.getHour());

                if(!new File(mOutputFolder).exists()) {
                    FileUtils.forceMkdir(new File(mOutputFolder));
                }

                DownloadUtils.download(ftpClient, desiredFile, new File(outputDestination.getAbsolutePath() + "\\" + desiredFile));
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
}

