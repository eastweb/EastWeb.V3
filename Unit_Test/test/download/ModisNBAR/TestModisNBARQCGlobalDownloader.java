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

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.PluginMetaData.FTP;
import version2.prototype.PluginMetaData.HTTP;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.download.RegistrationException;
import version2.prototype.download.ModisNBAR.ModisNBARQCGlobalDownloader;
import version2.prototype.download.ModisNBAR.ModisNBARQCListDatesFiles;

public class TestModisNBARQCGlobalDownloader {
    private static Config configInstance = Config.getAnInstance("src/test/config.xml");

    private static DownloadMetaData data;
    private static ProjectInfoFile p;
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String mode = "HTTP";// the protocol type: ftp or http
        FTP myFtp = null;
        HTTP myHttp = new HTTP("http://e4ftl01.cr.usgs.gov/MOTA/MCD43B2.005/");;
        String className = null;
        String timeZone = null;
        int filesPerDay = -1;
        String datePatternStr = "\\d{4}";

        String fileNamePatternStr = "MCD43B2.A(\\d{7}).h(\\d{2})v(\\d{2}).005.(\\d{13}).hdf";

        // Just for test
        LocalDate ld = LocalDate.parse("Sun Mar 01 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));

        data = new DownloadMetaData(null, null, null, null, null, mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);
        p = new ProjectInfoFile(configInstance, "C:\\Users\\yi.liu\\git\\EastWeb.V2\\src\\version2\\prototype\\ProjectInfoMetaData\\Project_TW_TRMMrt.xml");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testRun() throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, SQLException, RegistrationException {
        ListDatesFiles ldf= new ModisNBARQCListDatesFiles(new DataDate(data.originDate), data, p);
        LocalDate startDate = LocalDate.now().minusDays(14);

        ModisNBARQCGlobalDownloader ttd = new ModisNBARQCGlobalDownloader(1, Config.getAnInstance("test/config.xml"), "ModisNBAR",  data,  ldf, startDate);

        ttd.run();
    }

}
