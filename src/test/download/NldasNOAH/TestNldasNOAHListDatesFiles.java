package test.download.NldasNOAH;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import version2.prototype.download.NldasNOAH.NldasNOAHListDatesFiles;


public class TestNldasNOAHListDatesFiles {

    private static DownloadMetaData data;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        String mode = "FTP";// the protocol type: ftp or http
        FTP myFtp = PluginMetaDataCollection.CreateFTP("hydro1.sci.gsfc.nasa.gov",
                "/data/s4pa/NLDAS/NLDAS_NOAH0125_H.002", "anonymous", "anonymous");
        HTTP myHttp = null;
        String className = null;
        String timeZone = null;
        int filesPerDay = 24;
        String datePatternStr = "\\d{4}";
        String fileNamePatternStr = "NLDAS_NOAH0125_H\\.A(\\d{4})(\\d{2})(\\d{2})\\.(\\d{4})\\.002\\.grb";
        LocalDate ld = LocalDate.parse("Wed Jul 15 00:00:01 CDT 2015", DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu"));
        //System.out.println("OriginDate: " + ld.toString() + "\n");
        data = PluginMetaDataCollection.CreateDownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, ld);
    }


    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        data = null;
    }

    /**
     * Test method for {@link version2.prototype.download.NldasNOAH.NldasNOAHListDatesFiles#ListDatesFilesFTP()}.
     * @throws IOException
     */

    @Test
    public void testListDatesFilesFTP() throws IOException {
        NldasNOAHListDatesFiles testy = new NldasNOAHListDatesFiles(new DataDate(data.originDate), data, null);

        Map<DataDate, ArrayList<String>> tempDatesFiles = testy.CloneListDatesFiles();
        Map<Integer, Map.Entry<DataDate, ArrayList<String>>> datesFilesSorted = new TreeMap<Integer, Map.Entry<DataDate, ArrayList<String>>>();

        System.out.println(tempDatesFiles.size() + "\n");

        for (Map.Entry<DataDate, ArrayList<String>> entry : tempDatesFiles.entrySet())
        {
            datesFilesSorted.put((((entry.getKey().getYear() - data.originDate.getYear()) + 1) * 365) + entry.getKey().getDayOfYear(), entry);
        }

        for (Map.Entry<Integer, Map.Entry<DataDate, ArrayList<String>>> entry : datesFilesSorted.entrySet())
        {
            for(int i = 0; i < entry.getValue().getValue().size(); i++)
            {
                System.out.println(entry.getValue().getKey() + " : /" + entry.getValue().getValue().get(i) + "\n");
            }

        }

        System.out.println("Completed");

    }

}

