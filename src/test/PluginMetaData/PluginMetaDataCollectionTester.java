/**
 *
 */
package test.PluginMetaData;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.ProcessorMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.SummaryMetaData;

/**
 * @author michael.devos
 *
 */
public class PluginMetaDataCollectionTester {
    /**
     * Test method for {@link version2.prototype.PluginMetaData.PluginMetaDataCollection#getInstance(java.io.File)}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @Test
    public final void testGetInstanceFile() throws ParserConfigurationException, SAXException, IOException, Exception {
        // Setup
        File[] testFiles = new File[2];
        testFiles[0] = new File(System.getProperty("user.dir") + "\\src\\test\\PluginMetaData\\Test_Plugin.xml");
        testFiles[1] = new File(System.getProperty("user.dir") + "\\src\\test\\PluginMetaData\\Test_MultipleDownloads.xml");

        // Test instance creation
        //        File testPluginXmlFile = new File(System.getProperty("user.dir") + "\\src\\test\\PluginMetaData\\Test_Plugin.xml");
        PluginMetaDataCollection instance = PluginMetaDataCollection.getInstance(testFiles);
        assertNotNull(instance);
        assertTrue("Plugin list size is " + instance.pluginMetaDataMap.size(), instance.pluginMetaDataMap.size() == 2);
        assertTrue("First plugin file name is " + instance.pluginList.get(0), instance.pluginList.get(0).equals("Test_Plugin"));
        assertTrue("First plugin title is " + instance.pluginList.get(0), instance.pluginMetaDataMap.get(instance.pluginList.get(0)).Title.equals("Test Plugin"));
        PluginMetaData pluginMetaData = instance.pluginMetaDataMap.get(instance.pluginList.get(0));
        assertNotNull(pluginMetaData);
        assertTrue("Plugin DaysPerInputData is " + pluginMetaData.DaysPerInputData, pluginMetaData.DaysPerInputData == 1);
        assertTrue("ExtraDownloadFiles list is " + pluginMetaData.ExtraDownloadFiles.toString(), pluginMetaData.ExtraDownloadFiles.size() == 0);

        // Test getting DownloadMetaData
        DownloadMetaData downloadData = pluginMetaData.Download;
        assertTrue("Name is " + downloadData.name, downloadData.name.equals("Data"));
        assertTrue("Class is " + downloadData.className, downloadData.className.equals("NldasDownloadTask"));
        assertTrue("Mode is " + downloadData.mode, downloadData.mode.equalsIgnoreCase("ftp"));
        assertTrue("FTP hostname is " + downloadData.myFtp.hostName, downloadData.myFtp.hostName.equalsIgnoreCase("hydro1.sci.gsfc.nasa.gov"));
        assertTrue("FTP root directory is " + downloadData.myFtp.rootDir, downloadData.myFtp.rootDir.equals("/data/s4pa/NLDAS/NLDAS_FORA0125_H.002"));
        assertTrue("FTP username is " + downloadData.myFtp.userName, downloadData.myFtp.userName.equals("anonymous"));
        assertTrue("FTP password is " + downloadData.myFtp.password, downloadData.myFtp.password.equals("anonymous"));
        assertTrue("TimeZone is " + downloadData.timeZone, downloadData.timeZone.equals("Africa/Bangui"));
        ZoneId zid = ZoneId.of(downloadData.timeZone);
        assertTrue("TimeZone is " + zid.getDisplayName(TextStyle.FULL, Locale.ENGLISH), zid.getDisplayName(TextStyle.FULL, Locale.ENGLISH).equals("Western African Time"));
        assertTrue("FilesPerDay is " + downloadData.filesPerDay, downloadData.filesPerDay == 1);
        assertTrue("ExtraDownloads list is " + (downloadData.extraDownloads == null ? "NULL" : downloadData.extraDownloads.toString()), downloadData.extraDownloads == null);

        Matcher matcher;
        // TODO: Test downloadData.datePattern.
        //            String dateString = "";
        //            int expectedYear = 0;
        //            int expectedMonth = 0;
        //            int expectedDay = 0;

        matcher = downloadData.datePattern.matcher("");
        //            if(matcher.find())
        //            {
        //                assertTrue("Year is " + matcher.group(1), Integer.parseInt(matcher.group(1)) == expectedYear);
        //                assertTrue("Month is " + matcher.group(3), Integer.parseInt(matcher.group(3)) == expectedMonth);
        //                assertTrue("Day is " + matcher.group(4), Integer.parseInt(matcher.group(4)) == expectedDay);
        //            } else {
        //                fail("DatePattern didn't match date test string.");
        //            }

        // TODO: Test downloadData.fileNamePattern
        //            String dateString = "";
        //            int expectedYear = 0;
        //            int expectedMonth = 0;
        //            int expectedDay = 0;

        matcher = downloadData.fileNamePattern.matcher("");
        //            if(matcher.find())
        //            {
        //                assertTrue("Year is " + matcher.group(1), Integer.parseInt(matcher.group(1)) == expectedYear);
        //                assertTrue("Month is " + matcher.group(3), Integer.parseInt(matcher.group(3)) == expectedMonth);
        //                assertTrue("Day is " + matcher.group(4), Integer.parseInt(matcher.group(4)) == expectedDay);
        //            } else {
        //                fail("DatePattern didn't match date test string.");
        //            }

        // Test getting ProcessorMetaData
        ProcessorMetaData processorData = pluginMetaData.Processor;
        Map<Integer, String> tempStep = new HashMap<Integer, String>();
        tempStep.put(1,  "NldasProjection");
        tempStep.put(2, "NldasMozaic");
        tempStep.put(3, "NldasConvertor");
        tempStep.put(4, "NldasFilter");
        assertTrue("Processor Steps are: " + tempStep.toString(), processorData.processStep.equals(tempStep));

        // Test getting IndicesMetaData
        ArrayList<String> indicesData = pluginMetaData.IndicesMetaData;
        ArrayList<String> compareData = new ArrayList<String>(1);
        compareData.add("GdalNldasCalculator");
        assertTrue("Indices are " + indicesData.toString(), indicesData.equals(compareData));

        // Test getting SummaryMetaData
        SummaryMetaData summaryData = pluginMetaData.Summary;
        //        assertTrue("DaysPerInputData is " + summaryData.daysPerInputData,summaryData.daysPerInputData == 1);
        assertTrue("MergeStrategyClass is " + summaryData.mergeStrategyClass, summaryData.mergeStrategyClass.equals("AvgGdalRasterFileMerge"));
        assertTrue("InterpolateStrategyClass is not empty string but " + summaryData.interpolateStrategyClass, summaryData.interpolateStrategyClass.equals(""));


        // Use file Test_MultipleDownloads.xml to test multiple Download elements and missing TimeZone and FilesPerDay elements from "QC" Download
        assertTrue("Second plugin file name is " + instance.pluginList.get(1), instance.pluginList.get(1).equals("Test_MultipleDownloads"));
        assertTrue("Second plugin title is " + instance.pluginList.get(1), instance.pluginMetaDataMap.get(instance.pluginList.get(1)).Title.equals("Test Multiple Downloads"));
        pluginMetaData = instance.pluginMetaDataMap.get(instance.pluginList.get(1));
        assertTrue("ExtraDownloadFiles list is " + pluginMetaData.ExtraDownloadFiles.toString(), pluginMetaData.ExtraDownloadFiles.size() == 1);

        downloadData = pluginMetaData.Download;
        assertTrue("ExtraDownloads list is null", downloadData.extraDownloads != null);
        assertTrue("ExtraDownloads list is " + downloadData.extraDownloads.toString(), downloadData.extraDownloads.size() == 1);
        assertTrue("Name is " + downloadData.name, downloadData.name.equals("Data"));
        assertTrue("Class is " + downloadData.className, downloadData.className.equals("NldasDownloadTask"));
        assertTrue("Mode is " + downloadData.mode, downloadData.mode.equalsIgnoreCase("ftp"));
        assertTrue("FTP hostname is " + downloadData.myFtp.hostName, downloadData.myFtp.hostName.equalsIgnoreCase("hydro1.sci.gsfc.nasa.gov"));
        assertTrue("FTP root directory is " + downloadData.myFtp.rootDir, downloadData.myFtp.rootDir.equals("/data/s4pa/NLDAS/NLDAS_FORA0125_H.002"));
        assertTrue("FTP username is " + downloadData.myFtp.userName, downloadData.myFtp.userName.equals("anonymous"));
        assertTrue("FTP password is " + downloadData.myFtp.password, downloadData.myFtp.password.equals("anonymous"));
        assertTrue("TimeZone is " + downloadData.timeZone, downloadData.timeZone.equals("Africa/Bangui"));
        zid = ZoneId.of(downloadData.timeZone);
        assertTrue("TimeZone is " + zid.getDisplayName(TextStyle.FULL, Locale.ENGLISH), zid.getDisplayName(TextStyle.FULL, Locale.ENGLISH).equals("Western African Time"));
        assertTrue("FilesPerDay is " + downloadData.filesPerDay, downloadData.filesPerDay == 1);

        downloadData = downloadData.extraDownloads.get(0);
        assertTrue("Name is " + downloadData.name, downloadData.name.equals("QC"));
        assertTrue("Class is " + downloadData.className, downloadData.className.equals("NldasDownloadTask"));
        assertTrue("Mode is " + downloadData.mode, downloadData.mode.equalsIgnoreCase("ftp"));
        assertTrue("FTP hostname is " + downloadData.myFtp.hostName, downloadData.myFtp.hostName.equalsIgnoreCase("other.sci.gsfc.nasa.gov"));
        assertTrue("FTP root directory is " + downloadData.myFtp.rootDir, downloadData.myFtp.rootDir.equals("/qc/s4pa/NLDAS/NLDAS_FORA0125_H.002"));
        assertTrue("FTP username is " + downloadData.myFtp.userName, downloadData.myFtp.userName.equals("anonymous"));
        assertTrue("FTP password is " + downloadData.myFtp.password, downloadData.myFtp.password.equals("anonymous"));
        assertTrue("TimeZone is " + downloadData.timeZone, downloadData.timeZone.equals("Africa/Bangui"));
        zid = ZoneId.of(downloadData.timeZone);
        assertTrue("TimeZone is " + zid.getDisplayName(TextStyle.FULL, Locale.ENGLISH), zid.getDisplayName(TextStyle.FULL, Locale.ENGLISH).equals("Western African Time"));
        assertTrue("FilesPerDay is " + downloadData.filesPerDay, downloadData.filesPerDay == 1);
        assertTrue("ExtraDownloads list is " + (downloadData.extraDownloads == null ? "NULL" : downloadData.extraDownloads.toString()), downloadData.extraDownloads == null);

        // TODO: Test downloadData.datePattern.
        //            String dateString = "";
        //            int expectedYear = 0;
        //            int expectedMonth = 0;
        //            int expectedDay = 0;

        matcher = downloadData.datePattern.matcher("");
        //            if(matcher.find())
        //            {
        //                assertTrue("Year is " + matcher.group(1), Integer.parseInt(matcher.group(1)) == expectedYear);
        //                assertTrue("Month is " + matcher.group(3), Integer.parseInt(matcher.group(3)) == expectedMonth);
        //                assertTrue("Day is " + matcher.group(4), Integer.parseInt(matcher.group(4)) == expectedDay);
        //            } else {
        //                fail("DatePattern didn't match date test string.");
        //            }

        // TODO: Test downloadData.fileNamePattern
        //            String dateString = "";
        //            int expectedYear = 0;
        //            int expectedMonth = 0;
        //            int expectedDay = 0;

        matcher = downloadData.fileNamePattern.matcher("");
        //            if(matcher.find())
        //            {
        //                assertTrue("Year is " + matcher.group(1), Integer.parseInt(matcher.group(1)) == expectedYear);
        //                assertTrue("Month is " + matcher.group(3), Integer.parseInt(matcher.group(3)) == expectedMonth);
        //                assertTrue("Day is " + matcher.group(4), Integer.parseInt(matcher.group(4)) == expectedDay);
        //            } else {
        //                fail("DatePattern didn't match date test string.");
        //            }
    }
}
