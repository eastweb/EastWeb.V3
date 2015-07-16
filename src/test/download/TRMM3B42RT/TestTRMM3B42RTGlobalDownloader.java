package test.download.TRMM3B42RT;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.FTP;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.HTTP;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.TRMM3B42RT.TRMM3B42RTGlobalDownloader;
import version2.prototype.download.TRMM3B42RT.TRMM3B42RTListDatesFiles;

public class TestTRMM3B42RTGlobalDownloader {

    private static DownloadMetaData data;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String mode = "FTP";// the protocol type: ftp or http
        FTP myFtp = PluginMetaDataCollection.CreateFTP("disc2.nascom.nasa.gov",
                "/data/TRMM/Gridded/Derived_Products/3B42RT/Daily/", "anonymous", "anonymous");
        HTTP myHttp = null;
        String className = null;
        String timeZone = null;
        int filesPerDay = -1;
        String datePatternStr = "\\d{4}";
        String fileNamePatternStr = "3B42RT_daily\\.(\\d{4})\\.(\\d{2})\\.(\\d{2})\\.bin";
        LocalDate ld = LocalDate.parse("Wed Jul 01 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));

        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        data = null;
    }

    @Test
    public void testRun() throws IOException {
        ListDatesFiles ldf= new TRMM3B42RTListDatesFiles(new DataDate(data.originDate), data);

        TRMM3B42RTGlobalDownloader ttd = new TRMM3B42RTGlobalDownloader(1,"TRMM3B42RT",  data,  ldf);

        ttd.run();

    }

}
