package test.download.ModisNBAR;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
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
import version2.prototype.download.ModisNBAR.ModisNBARGlobalDownloader;
import version2.prototype.download.ModisNBAR.ModisNBARListDatesFiles;
import version2.prototype.download.ModisNBAR.ModisNBARQCGlobalDownloader;
import version2.prototype.download.ModisNBAR.ModisNBARQCListDatesFiles;

public class TestModisNBARQCGlobalDownloader {

    private static DownloadMetaData data;
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String mode = "HTTP";// the protocol type: ftp or http
        FTP myFtp = null;
        HTTP myHttp = PluginMetaDataCollection.CreateHTTP("http://e4ftl01.cr.usgs.gov/MOTA/MCD43B2.005/");;
        String className = null;
        String timeZone = null;
        int filesPerDay = -1;
        String datePatternStr = "\\d{4}";

        String fileNamePatternStr = "MCD43B2.A(\\d{7}).h(\\d{2})v(\\d{2}).005.(\\d{13}).hdf";

        // Just for test
        LocalDate ld = LocalDate.parse("Sun Mar 01 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));

        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testRun() throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, SQLException {
        ListDatesFiles ldf= new ModisNBARQCListDatesFiles(new DataDate(data.originDate), data);

        ModisNBARQCGlobalDownloader ttd = new ModisNBARQCGlobalDownloader(1,"ModisNBAR",  data,  ldf);

        ttd.run();
    }

}
