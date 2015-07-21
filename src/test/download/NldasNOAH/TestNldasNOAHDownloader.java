package test.download.NldasNOAH;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.FTP;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.HTTP;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.ModisLST.ModisLSTDownloader;
import version2.prototype.download.NldasNOAH.NldasNOAHDownloader;

public class TestNldasNOAHDownloader {

    private static DownloadMetaData data;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        String mode = "FTP";// the protocol type: ftp or http
        FTP myFtp = PluginMetaDataCollection.CreateFTP("hydro1.sci.gsfc.nasa.gov",
                "/data/s4pa/NLDAS/NLDAS_NOAH0125_H.002", "anonymous", "anonymous");
        HTTP myHttp = null;
        String className = null;
        String timeZone = null;
        int filesPerDay = 24;
        String datePatternStr = "\\d{4}";
        String fileNamePatternStr = "NLDAS_NOAH0125_H\\.A(\\d{4})(\\d{2})(\\d{2})\\.(\\d{4})\\.002\\.grb";
        LocalDate ld = LocalDate.parse("Thu Jul 09 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));
        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        data = null;
    }

    @Test
    public void testDownload() {
        NldasNOAHDownloader nnt = new NldasNOAHDownloader(
                new DataDate(10, 06, 2015), "C:\\project\\download\\NLDASNOAH",  data, "NLDAS_NOAH0125_H.A20150610.0500.002.grb");

        try {
            nnt.download();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DownloadFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

}
