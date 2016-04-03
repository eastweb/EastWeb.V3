package EastWeb_Downloader.ModisDownloadUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import EastWeb_Config.Config;
import EastWeb_Downloader.DownloadFailedException;
import EastWeb_Downloader.DownloadUtils;
import EastWeb_Downloader.DownloaderFramework;
import EastWeb_ErrorHandling.ErrorLog;
import PluginMetaData.DownloadMetaData;
import PluginMetaData.HTTP;
import Utilies.DataDate;


/*
 * @Author: Chris Plucker, FangYu
 * @Author: Yi Liu
 */

public class ModisDownloader extends DownloaderFramework
{
    protected DataDate mDate;
    protected String mOutFolder;
    protected String mHostURL;
    protected String mMode;
    protected DownloadMetaData mData;
    protected String mFileToD;
    protected String outFilePath;

    /*
     * @param date:  The date for the files to be downloaded
     * @param outFolder: the folder to hold the file to be downloaded
     * @param data: DownloadMetaData that holds the information of URLs, communication prototol and such
     * @param fileToDownload: the file name of the file to be downloaded
     */
    public ModisDownloader(DataDate date, String outFolder, DownloadMetaData data, String fileToDownload)
    {
        mDate = date;
        mOutFolder = outFolder;
        mData = data;
        mFileToD = fileToDownload;
        outFilePath = null;
        setHttpValues();
    }

    //set the http values from DownloadMetaData
    private void setHttpValues()
    {
        mMode = mData.mode;
        HTTP h = mData.myHttp;
        mHostURL = h.url;
    }

    @Override
    public void download() throws IOException, DownloadFailedException, SAXException, Exception
    {
        /*set the directory to store the file to be downloaded
         *all the tiles on the same day will be placed in a folder
         *workingDir\download\ProductName\year\dayOfYear
         */
        String dir = String.format(
                "%s"+"%04d" + File.separator+"%03d" ,
                mOutFolder, mDate.getYear(), mDate.getDayOfYear());

        if(!(new File(dir).exists()))
        {
            FileUtils.forceMkdir(new File(dir));
        }

        // extract the modis tiles from the file name in mFileToD
        // and name each downloaded file as its tile name
        // for example, h03v24.hdf
        final Pattern tilePattern = Pattern.compile("h\\d{2}v\\d{2}");
        Matcher matcher = tilePattern.matcher(mFileToD);

        String outFileName = null;
        if (matcher.find())
        {
            outFileName = matcher.group(0);
        } else {
            throw new Exception("the download file does not contain tile number!");
        }

        outFilePath = String.format("%s"+File.separator+"%s",dir, outFileName+".hdf");
        File outputFile  = new File(outFilePath);

        if (mMode.equalsIgnoreCase("HTTP"))
        {
            try
            {
                // form the complete url for the file to be downloaded
                String fileURL = mHostURL +
                        String.format("%04d.%02d.%02d/%s",
                                mDate.getYear(), mDate.getMonth(), mDate.getDay(),mFileToD);

                DownloadUtils.downloadToFile(new URL(fileURL), outputFile);
            }
            catch (IOException e)
            {
                ErrorLog.add(Config.getInstance(), "Modis", mData.name, "ModisDownloader.download problem while attempting to download to file.", e);
                return;
            }
        }

    }

    @Override
    public String getOutputFilePath()
    {
        return outFilePath;
    }

}
