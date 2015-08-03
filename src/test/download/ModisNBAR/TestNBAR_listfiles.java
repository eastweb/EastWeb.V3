package test.download.ModisNBAR;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.FTP;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.HTTP;
import version2.prototype.download.ModisLST.ModisLSTListDatesFiles;
import version2.prototype.download.ModisNBAR.ModisNBARListDatesFiles;

public class TestNBAR_listfiles {

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

        LocalDate ld = LocalDate.parse("Sun Mar 01 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));

        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        data = null;
    }

    @Test
    public void testListDatesFilesHTTP() throws IOException {
        ModisNBARListDatesFiles mld = new ModisNBARListDatesFiles(new DataDate(data.originDate), data);

        Map<DataDate, ArrayList<String>> tempDatesFiles = mld.getListDatesFiles();

        for (Map.Entry<DataDate, ArrayList<String>> entry : tempDatesFiles.entrySet())
        {
            System.out.println(entry.getKey() + " : /" + entry.getValue().get(0));
        }

    }

}