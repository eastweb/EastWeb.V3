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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;

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
    public final void testGetInstanceFile() throws ParserConfigurationException, SAXException, IOException {
        // Test instance creation
        File testPluginXmlFile = new File(System.getProperty("user.dir") + "\\src\\test\\PluginMetaData\\Test_Plugin.xml");
        PluginMetaDataCollection instance = PluginMetaDataCollection.getInstance(testPluginXmlFile);
        assertNotNull(instance);
        assertTrue("Plugin List size is " + instance.pluginMetaDataMap.size(), instance.pluginMetaDataMap.size() == 1);
        assertTrue("First plugin title is " + instance.pluginList.get(0), instance.pluginMetaDataMap.get(instance.pluginList.get(0)).Title.equals("Test Plugin"));
        PluginMetaData pluginMetaData = instance.pluginMetaDataMap.get(instance.pluginList.get(0));
        assertNotNull(pluginMetaData);
        assertTrue("Plugin DaysPerInputData is " + pluginMetaData.DaysPerInputData, pluginMetaData.DaysPerInputData == 1);

        // Test getting DownloadMetaData
        DownloadMetaData downloadData = pluginMetaData.Download;
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
        Pattern datePattern = null;
        try {
            //            String dateString = "";
            //            int expectedYear = 0;
            //            int expectedMonth = 0;
            //            int expectedDay = 0;

            datePattern = Pattern.compile(downloadData.datePattern);
            //            Matcher matcher = datePattern.matcher(dateString);
            //            if(matcher.find())
            //            {
            //                assertTrue("Year is " + matcher.group(1), Integer.parseInt(matcher.group(1)) == expectedYear);
            //                assertTrue("Month is " + matcher.group(3), Integer.parseInt(matcher.group(3)) == expectedMonth);
            //                assertTrue("Day is " + matcher.group(4), Integer.parseInt(matcher.group(4)) == expectedDay);
            //            } else {
            //                fail("DatePattern didn't match date test string.");
            //            }
        }
        catch(PatternSyntaxException e) {
            fail("DatePattern compiling throws PatternSyntaxException.");
        }
        Pattern fileNamePattern = null;
        try {
            //            String dateString = "";
            //            int expectedYear = 0;
            //            int expectedMonth = 0;
            //            int expectedDay = 0;

            fileNamePattern = Pattern.compile(downloadData.fileNamePattern);
            //            Matcher matcher = datePattern.matcher(dateString);
            //            if(matcher.find())
            //            {
            //                assertTrue("Year is " + matcher.group(1), Integer.parseInt(matcher.group(1)) == expectedYear);
            //                assertTrue("Month is " + matcher.group(3), Integer.parseInt(matcher.group(3)) == expectedMonth);
            //                assertTrue("Day is " + matcher.group(4), Integer.parseInt(matcher.group(4)) == expectedDay);
            //            } else {
            //                fail("DatePattern didn't match date test string.");
            //            }
        }
        catch(PatternSyntaxException e) {
            fail("FileNamePattern compiling throws PatternSyntaxException.");
        }

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
    }
}
