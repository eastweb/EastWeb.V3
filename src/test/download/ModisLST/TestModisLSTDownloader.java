package test.download.ModisLST;

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
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.ModisLST.ModisLSTDownloader;

public class TestModisLSTDownloader {

    private static DownloadMetaData data;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        String mode = "HTTP";// the protocol type: ftp or http
        FTP myFtp = null;
        HTTP myHttp = PluginMetaDataCollection.CreateHTTP("http://e4ftl01.cr.usgs.gov/MOLT/MOD11A2.005/");;
        String className = null;
        String timeZone = null;
        int filesPerDay = -1;
        String datePatternStr = "\\d{4}";

        String fileNamePatternStr = "MOD11A2.A(\\d{7}).h(\\d{2})v(\\d{2}).005.(\\d{13}).hdf";

        LocalDate ld = LocalDate.parse("Wed Jul 01 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));

        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);
        
        ProjectInfoFile projectInfoFile = new ProjectInfoFile("C:\\Users\\yi.liu\\git\\EastWeb.V2\\src\\version2\\prototype\\ProjectInfoMetaData\\Project_TW_TRMMrt.xml");

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        data = null;
    }

    @Test
    public void testDownload() {
        ModisLSTDownloader mdt = new ModisLSTDownloader(
                new DataDate(10, 06, 2015), "D:\\project\\download\\MODISLST",  data, "MOD11A2.A2015161.h19v03.005.2015174065146.hdf");

        try {
            mdt.download();
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
