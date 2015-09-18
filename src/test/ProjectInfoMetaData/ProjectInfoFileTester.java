package test.ProjectInfoMetaData;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;

@SuppressWarnings("javadoc")
public class ProjectInfoFileTester {

    //    @Test
    //    public final void testManualChoosing() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
    //    InvocationTargetException, ParserConfigurationException, SAXException, IOException
    //    {
    //        ProjectInfoFile projectInfo = new ProjectInfoFile(System.getProperty("user.dir") + "\\src\\version2\\prototype\\ProjectInfoMetaData\\Project_Amhara.xml");
    //        assertTrue("Summary " + projectInfo.GetSummaries().get(0).GetID() + " is " + projectInfo.GetSummaries().get(0).toString(), projectInfo.GetSummaries().get(0).toString()
    //                .equals("AreaNameField: R_NAME; Shape File Path: D:\\testProjects\\Amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp; AreaCodeField: R_NAME; Temporal Summary: GregorianWeeklyStrategy"));
    //        System.out.println("Summary " + projectInfo.GetSummaries().get(0).GetID() + " is " + "\"" + projectInfo.GetSummaries().get(0).toString() + "\"");
    //        assertTrue("Summary " + projectInfo.GetSummaries().get(1).GetID() + " is " + projectInfo.GetSummaries().get(1).toString(), projectInfo.GetSummaries().get(1).toString()
    //                .equals("AreaNameField: NAME10; Shape File Path: C:\\Users\\sufi\\Desktop\\shapefile\\shapefile.shp; AreaCodeField: COUNTYNS10; Temporal Summary: GregorianWeeklyStrategy"));
    //    }

    @Test
    public final void testProjectInfoFile() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
    InvocationTargetException, ParserConfigurationException, SAXException, IOException {
        ProjectInfoFile projectInfo = new ProjectInfoFile(System.getProperty("user.dir") + "\\src\\test\\ProjectInfoMetaData\\Test_Project.xml");

        assertTrue("Plugins loaded: " + projectInfo.GetPlugins().size(), projectInfo.GetPlugins().size() == 1);
        assertTrue("Plugin 1 name is " + projectInfo.GetPlugins().get(0).GetName(), projectInfo.GetPlugins().get(0).GetName().equals("Test Plugin"));
        assertTrue("Plugin 1 QC Level is " + projectInfo.GetPlugins().get(0).GetQC(), projectInfo.GetPlugins().get(0).GetQC().equals("Level 1"));
        assertTrue("Plugin 1 indices are " + projectInfo.GetPlugins().get(0).GetIndices().get(0), projectInfo.GetPlugins().get(0).GetIndices().get(0).equals("GdalModisLST_DAYCalculator"));
        assertTrue("Plugin 1 indices are " + projectInfo.GetPlugins().get(0).GetIndices().get(1), projectInfo.GetPlugins().get(0).GetIndices().get(1).equals("GdalModisLST_MEANCalculator"));
        assertTrue("Plugin 1 indices are " + projectInfo.GetPlugins().get(0).GetIndices().get(2), projectInfo.GetPlugins().get(0).GetIndices().get(2).equals("GdalModisLST_NIGHTCalculator"));
        assertTrue("Plugin 1 Modis Tile 1 is " + projectInfo.GetPlugins().get(0).GetModisTiles().get(0), projectInfo.GetPlugins().get(0).GetModisTiles().get(0).equals("v12H42"));
        assertTrue("Plugin 1 Modis Tile 2 is " + projectInfo.GetPlugins().get(0).GetModisTiles().get(1), projectInfo.GetPlugins().get(0).GetModisTiles().get(1).equals("V11H11"));
        assertTrue("StartDate is " + projectInfo.GetStartDate().toString(), projectInfo.GetStartDate().toString().equals("2015-06-02"));
        assertTrue("ProjectName is " + projectInfo.GetProjectName(), projectInfo.GetProjectName().equals("Test_Project"));
        assertTrue("WorkingDir is " + projectInfo.GetWorkingDir(), projectInfo.GetWorkingDir().equals("C:\\EASTWeb_Test\\"));
        assertEquals("Masking File is incorrect.", System.getProperty("user.dir") + "\\Documentation\\TestCases\\ethiopia_amhara\\settings\\watermask\\Ethiopia_watermask.tif", projectInfo.GetMaskingFile());
        assertTrue("Masking Resolution is " + projectInfo.GetMaskingResolution(), projectInfo.GetMaskingResolution() == 1000);
        assertEquals("MasterShapeFile is incorrect.", System.getProperty("user.dir") + "\\Documentation\\TestCases\\ethiopia_amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp", projectInfo.GetMasterShapeFile());
        assertTrue("TimeZone is " + projectInfo.GetTimeZone(), projectInfo.GetTimeZone().equals("Africa/Bangui"));
        ZoneId zid = ZoneId.of(projectInfo.GetTimeZone());
        assertTrue("TimeZone is " + zid.getDisplayName(TextStyle.FULL, Locale.ENGLISH), zid.getDisplayName(TextStyle.FULL, Locale.ENGLISH).equals("Western African Time"));
        //        for(String zone : ZoneId.getAvailableZoneIds())
        //        {
        //            System.out.println(zone);
        //        }
        //        zid = ZoneId.of("GMT+1");
        //        System.out.println(zid.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH));

        assertTrue("TotalModisTiles is " + projectInfo.GetTotModisTiles(), projectInfo.GetTotModisTiles() == 120);
        assertTrue("CoordinateSystem is " + projectInfo.GetProjection().getProjectionType().name(), projectInfo.GetProjection().getProjectionType().name().equals("ALBERS_EQUAL_AREA"));
        assertTrue("ReSampling is " + projectInfo.GetProjection().getResamplingType().name(), projectInfo.GetProjection().getResamplingType().name().equals("NEAREST_NEIGHBOR"));
        assertTrue("Datum is " + projectInfo.GetProjection().getDatum().name(), projectInfo.GetProjection().getDatum().name().equals("NAD83"));
        assertTrue("PixelSize is " + projectInfo.GetProjection().getPixelSize(), projectInfo.GetProjection().getPixelSize() == 12);
        assertTrue("StandardParallel1 is " + projectInfo.GetProjection().getStandardParallel1(), projectInfo.GetProjection().getStandardParallel1() == 112.1);
        assertTrue("StandardParallel2 is " + projectInfo.GetProjection().getStandardParallel2(), projectInfo.GetProjection().getStandardParallel2() == 122.2);
        assertTrue("ScalingFactor is " + projectInfo.GetProjection().getScalingFactor(), projectInfo.GetProjection().getScalingFactor() == 0);
        assertTrue("CentalMeridian is " + projectInfo.GetProjection().getCentralMeridian(), projectInfo.GetProjection().getCentralMeridian() == 121);
        assertTrue("FalseEasting is " + projectInfo.GetProjection().getFalseEasting(), projectInfo.GetProjection().getFalseEasting() == 151);
        assertTrue("FalseNorthing is " + projectInfo.GetProjection().getFalseNorthing(), projectInfo.GetProjection().getFalseNorthing() == 33);
        assertTrue("LatitudeOfOrigin is " + projectInfo.GetProjection().getLatitudeOfOrigin(), projectInfo.GetProjection().getLatitudeOfOrigin() == 111);
        assertTrue("FreezingDate is " + projectInfo.GetFreezingDate().toString(), projectInfo.GetFreezingDate().toString().equals("2015-06-02"));
        //        assertTrue("FreezingDate is " + projectInfo.GetFreezingDate().toString(), projectInfo.GetFreezingDate().toString().equals("--06-02"));
        assertTrue("CoolingDegree is " + projectInfo.GetCoolingDegree(), projectInfo.GetCoolingDegree() == 0);
        assertTrue("HeatingDate is " + projectInfo.GetHeatingDate().toString(), projectInfo.GetHeatingDate().toString().equals("2015-06-02"));
        //        assertTrue("HeatingDate is " + projectInfo.GetHeatingDate().toString(), projectInfo.GetHeatingDate().toString().equals("--06-02"));
        assertTrue("HeatingDegree is " + projectInfo.GetHeatingDegree(), projectInfo.GetHeatingDegree() == 100.1);
        assertTrue("Summary " + projectInfo.GetSummaries().get(0).GetID() + " is " + projectInfo.GetSummaries().get(0).toString(), projectInfo.GetSummaries().get(0).toString()
                .equals("AreaNameField: NAME10; Shape File Path: " + System.getProperty("user.dir") + "\\Documentation\\TestCases\\ethiopia_amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp; AreaCodeField: COUNTYNS10; Temporal Summary: GregorianWeeklyStrategy"));
        assertTrue("Summary " + projectInfo.GetSummaries().get(1).GetID() + " is " + projectInfo.GetSummaries().get(1).toString(), projectInfo.GetSummaries().get(1).toString()
                .equals("AreaNameField: NAME10; Shape File Path: " + System.getProperty("user.dir") + "\\Documentation\\TestCases\\ethiopia_amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp; AreaCodeField: COUNTYNS10; Temporal Summary: GregorianWeeklyStrategy"));
        assertTrue("Summary " + projectInfo.GetSummaries().get(2).GetID() + " is " + projectInfo.GetSummaries().get(2).toString(), projectInfo.GetSummaries().get(2).toString()
                .equals("AreaNameField: NAME10; Shape File Path: " + System.getProperty("user.dir") + "\\Documentation\\TestCases\\ethiopia_amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp; AreaCodeField: COUNTYNS10"));
        assertTrue("Summary " + projectInfo.GetSummaries().get(3).GetID() + " is " + projectInfo.GetSummaries().get(3).toString(), projectInfo.GetSummaries().get(3).toString()
                .equals("AreaNameField: NAME10; Shape File Path: " + System.getProperty("user.dir") + "\\Documentation\\TestCases\\ethiopia_amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp; AreaCodeField: COUNTYNS10"));
    }
}
