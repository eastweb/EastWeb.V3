package test.download.TRMM3B42;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.FTP;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.HTTP;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.TRMM3B42.TRMM3B42Downloader;
import version2.prototype.download.TRMM3B42RT.TRMM3B42RTDownloader;

public class TestTRMM3B42Downloader {

    private static DownloadMetaData data;
    public TestTRMM3B42Downloader() {
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {


        String mode = "FTP";// the protocol type: ftp or http
        FTP myFtp = PluginMetaDataCollection.CreateFTP("disc2.nascom.nasa.gov",
                "/data/TRMM/Gridded/Derived_Products/3B42_V7/Daily/", "anonymous", "anonymous");
        HTTP myHttp = null;
        String className = null;
        String timeZone = null;
        int filesPerDay = -1;
        String datePatternStr = "\\d{4}";
        String fileNamePatternStr = "3B42_daily\\.(\\d{4})\\.(\\d{2})\\.(\\d{2})\\.7\\.bin";
        LocalDate ld = LocalDate.parse("Sat May 30 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));

        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testDownload() {
        TRMM3B42Downloader ttd = new TRMM3B42Downloader(new DataDate(118, 2015), "D:\\project\\download\\TRMM2", data, "3B42_daily.2015.04.29.7.bin");
        try {
            ttd.download();
        } catch (DownloadFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
