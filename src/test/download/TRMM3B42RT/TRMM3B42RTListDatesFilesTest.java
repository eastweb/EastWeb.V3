/**
 *
 */
package test.download.TRMM3B42RT;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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
    private static File outputFile;
    private static FileWriter writer;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // date = new DataDate(LocalDate.now().minusDays(1));
        //        date = new DataDate(22, 3, 2015);
        outputFile = new File("TRMM3B42RTListDatesFilesTest_output.txt");
        outputFile.delete();
        outputFile.createNewFile();
        writer = new FileWriter(outputFile);

        String mode = "FTP";// the protocol type: ftp or http
        FTP myFtp = PluginMetaDataCollection.CreateFTP("disc2.nascom.nasa.gov",
                "/data/TRMM/Gridded/Derived_Products/3B42RT/Daily/", "anonymous", "anonymous");
        HTTP myHttp = null;
        String className = null;
        String listDatesFilesClassName = null;
        String downloaderClassName = null;
        String timeZone = null;
        int filesPerDay = -1;
        String datePatternStr = "\\d{4}";
        String fileNamePatternStr = "3B42RT_daily\\.(\\d{4})\\.(\\d{2})\\.(\\d{2})\\.bin";
        LocalDate ld = LocalDate.parse("Wed Mar 01 00:00:01 CDT 2000", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));
        writer.write("OriginDate: " + ld.toString() + "\n");
        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, listDatesFilesClassName, downloaderClassName, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);
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

        Map<DataDate, ArrayList<String>> tempDatesFiles = testy.getListDatesFiles();
        Map<Integer, Map.Entry<DataDate, ArrayList<String>>> datesFilesSorted = new TreeMap<Integer, Map.Entry<DataDate, ArrayList<String>>>();

        writer.write(tempDatesFiles.size() + "\n");

        for (Map.Entry<DataDate, ArrayList<String>> entry : tempDatesFiles.entrySet())
        {
            datesFilesSorted.put((((entry.getKey().getYear() - data.originDate.getYear()) + 1) * 365) + entry.getKey().getDayOfYear(), entry);
        }

        for (Map.Entry<Integer, Map.Entry<DataDate, ArrayList<String>>> entry : datesFilesSorted.entrySet())
        {
            writer.write(entry.getValue().getKey() + " : /" + entry.getValue().getValue().get(0) + "\n");
        }
        writer.flush();

        System.out.println("Check output in file " + outputFile.getCanonicalPath());

        //        fail("Results not validated yet"); // TODO
        //        assertTrue("files is '" + datesFiles.toString() + "'", datesFiles.toString().equals(""));
    }

}
