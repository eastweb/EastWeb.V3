package test.download.TRMM3B42;

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
import version2.prototype.download.TRMM3B42.TRMM3B42ListDatesFiles;

public class TestTRMM3B42ListDatesFiles {

    private static DownloadMetaData data;

    public TestTRMM3B42ListDatesFiles() {
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
        LocalDate ld = LocalDate.parse("Wed Apr 01 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));

        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        data = null;
    }

    @Test
    public void testListDatesFilesFTP() {
        TRMM3B42ListDatesFiles testy;
        try {
            testy = new TRMM3B42ListDatesFiles(new DataDate(data.originDate), data, null);
            Map<DataDate, ArrayList<String>> tempDatesFiles = testy.CloneListDatesFiles();

            for (Map.Entry<DataDate, ArrayList<String>> entry : tempDatesFiles.entrySet())
            {
                System.out.println(entry.getKey() + ":/ " +  entry.getValue());
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

}
