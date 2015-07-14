/**
 *
 */
package test.download.TRMM3B42RT;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.LocalDate;
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
    private static DataDate date;
    private static DownloadMetaData data;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        date = new DataDate(LocalDate.now().minusDays(1));

        String mode = null;// the protocol type: ftp or http
        FTP myFtp = null;
        HTTP myHttp = null;
        String className = null;
        String timeZone = null;
        int filesPerDay = -1;
        String datePatternStr = null;
        String fileNamePatternStr = null;
        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        date = null;
        data = null;
    }

    /**
     * Test method for {@link version2.prototype.download.TRMM3B42RT.TRMM3B42RTListDatesFiles#ListDatesFilesFTP()}.
     * @throws IOException
     */
    @Test
    public final void testListDatesFilesFTP() throws IOException {
        TRMM3B42RTListDatesFiles testy = new TRMM3B42RTListDatesFiles(date, data);

        Map<DataDate, ArrayList<String>> files = testy.getListDatesFiles();

        fail("Results not validated yet"); // TODO
        assertTrue("files is '" + files.toString() + "'", files.toString().equals(""));
    }

}
