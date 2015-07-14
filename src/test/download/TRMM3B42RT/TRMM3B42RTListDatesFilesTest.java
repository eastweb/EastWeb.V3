/**
 *
 */
package test.download.TRMM3B42RT;

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
import version2.prototype.download.TRMM3B42RT.TRMM3B42RTListDatesFiles;

/**
 * @author michael.devos
 *
 */
public class TRMM3B42RTListDatesFilesTest {

    //    private static DataDate date;
    private static DownloadMetaData data;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // date = new DataDate(LocalDate.now().minusDays(1));
        //        date = new DataDate(22, 3, 2015);

        String mode = "FTP";// the protocol type: ftp or http
        FTP myFtp = PluginMetaDataCollection.CreateFTP("disc2.nascom.nasa.gov",
                "/data/TRMM/Gridded/Derived_Products/3B42RT/Daily/", "anonymous", "anonymous");
        HTTP myHttp = null;
        String className = null;
        String timeZone = null;
        int filesPerDay = -1;
        String datePatternStr = "\\d{4}";
        String fileNamePatternStr = "3B42RT_daily\\.(\\d{4})\\.(\\d{2})\\.(\\d{2})\\.bin";
        LocalDate ld = LocalDate.parse("Wed Mar 01 00:00:01 CDT 2000", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));
        System.out.println(ld.toString());
        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        //        date = null;
        data = null;
    }

    /**
     * Test method for {@link version2.prototype.download.TRMM3B42RT.TRMM3B42RTListDatesFiles#ListDatesFilesFTP()}.
     * @throws IOException
     */
    @Test
    public final void testListDatesFilesFTP() throws IOException {
        TRMM3B42RTListDatesFiles testy = new TRMM3B42RTListDatesFiles(new DataDate(data.originDate), data);

        Map<DataDate, ArrayList<String>> datesFiles = testy.getListDatesFiles();

        System.out.println(datesFiles.size());

        for (Map.Entry<DataDate, ArrayList<String>> entry : datesFiles.entrySet())
        {
            System.out.println(entry.getKey() + " : /" + entry.getValue().get(0));
        }

        fail("Results not validated yet"); // TODO
        assertTrue("files is '" + datesFiles.toString() + "'", datesFiles.toString().equals(""));
    }

}
