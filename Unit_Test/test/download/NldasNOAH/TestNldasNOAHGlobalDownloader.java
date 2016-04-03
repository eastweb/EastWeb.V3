package test.download.NldasNOAH;

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

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.PluginMetaData.FTP;
import version2.prototype.PluginMetaData.HTTP;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.RegistrationException;
import version2.prototype.download.NldasNOAH.NldasNOAHGlobalDownloader;
import version2.prototype.download.NldasNOAH.NldasNOAHListDatesFiles;

public class TestNldasNOAHGlobalDownloader {

    private static DownloadMetaData data;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String mode = "FTP";// the protocol type: ftp or http
        FTP myFtp = new FTP("hydro1.sci.gsfc.nasa.gov",
                "/data/s4pa/NLDAS/NLDAS_NOAH0125_H.002", "anonymous", "anonymous");
        HTTP myHttp = null;
        String className = null;
        String timeZone = null;
        int filesPerDay = 24;
        String datePatternStr = "\\d{4}";
        String fileNamePatternStr = "NLDAS_NOAH0125_H\\.A(\\d{4})(\\d{2})(\\d{2})\\.(\\d{4})\\.002\\.grb";
        LocalDate ld = LocalDate.parse("Wed Jul 15 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));
        data = new DownloadMetaData(null, null, null, null, null, mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        data = null;
    }

    @Test
    public void testRun() throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, SQLException, RegistrationException {
        ListDatesFiles ldf= new NldasNOAHListDatesFiles(new DataDate(data.originDate), data, null);
        LocalDate startDate = LocalDate.now().minusDays(14);

        NldasNOAHGlobalDownloader ttd = new NldasNOAHGlobalDownloader(1, Config.getAnInstance("test/config.xml"), "NLDASNOAH", data, ldf, startDate);

        ttd.run();

    }

}
