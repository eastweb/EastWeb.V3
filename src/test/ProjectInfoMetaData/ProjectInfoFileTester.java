package test.ProjectInfoMetaData;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;

public class ProjectInfoFileTester {

    @Test
    public final void testProjectInfoFile() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
    InvocationTargetException, ParserConfigurationException, SAXException, IOException {
        ProjectInfoFile projectInfo = new ProjectInfoFile(System.getProperty("user.dir") + "\\src\\test\\ProjectInfoMetaData\\Test_Project.xml");

        assertTrue("StartDate is " + projectInfo.GetStartDate().toString(), projectInfo.GetStartDate().toString().equals("Tue Jun 02 23:17:37 CDT 2015"));
        assertTrue("ProjectName is " + projectInfo.GetProjectName(), projectInfo.GetProjectName().equals("sufi_Project"));
        assertTrue("WorkingDir is " + projectInfo.GetWorkingDir(), projectInfo.GetWorkingDir().equals("C:\\Users\\sufi"));
        assertTrue("Masking File is " + projectInfo.GetMaskingFile(), projectInfo.GetMaskingFile().equals("C:\\Users\\Public\\Desktop\\3D Vision Photo Viewer.lnk"));
        assertTrue("Masking Resolution is " + projectInfo.GetMaskingResolution(), projectInfo.GetMaskingResolution() == 1000);
        assertTrue("MasterShapeFile is " + projectInfo.GetMasterShapeFile(), projectInfo.GetMasterShapeFile().equals("C:\\Users\\sufi\\Desktop\\shapefile\\shapefile.shp"));
        assertTrue("TimeZone is " + projectInfo.GetTimeZone(), projectInfo.GetTimeZone().equals("(GMT+1:00) Africa/Bangui"));
        assertTrue("TotalModisTiles is " + projectInfo.GetTotModisTiles(), projectInfo.GetTotModisTiles() == 120);
        assertTrue("Modis Tile 1 is " + projectInfo.GetModisTiles().get(0), projectInfo.GetModisTiles().get(0).equals("v12H42"));
        assertTrue("Modis Tile 2 is " + projectInfo.GetModisTiles().get(1), projectInfo.GetModisTiles().get(1).equals("V11H11"));
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
        assertTrue("Freezing is " + projectInfo.GetFreezingDate().toString(), projectInfo.GetFreezingDate().toString().equals("Tue Jun 02 23:17:37 CDT 2015"));
        assertTrue("Heating is " + projectInfo.GetHeatingDate().toString(), projectInfo.GetHeatingDate().toString().equals("Tue Jun 02 23:17:37 CDT 2015"));
        assertTrue("Summary 1 is " + projectInfo.GetSummaries().get(0).toString(), projectInfo.GetSummaries().get(0).toString()
                .equals("Shape File Path: C:\\Users\\sufi\\Desktop\\shapefile\\shapefile.shp; Field: COUNTYNS10; Temporal Summary: GregorianWeeklyStrategy"));
        assertTrue("Summary 2 is " + projectInfo.GetSummaries().get(1).toString(), projectInfo.GetSummaries().get(1).toString()
                .equals("Shape File Path: C:\\Users\\sufi\\Desktop\\shapefile\\shapefile.shp; Field: COUNTYNS10; Temporal Summary: GregorianWeeklyStrategy"));
        assertTrue("Summary 3 is " + projectInfo.GetSummaries().get(2).toString(), projectInfo.GetSummaries().get(2).toString()
                .equals("Shape File Path: C:\\Users\\sufi\\Desktop\\shapefile\\shapefile.shp; Field: COUNTYNS10"));
        assertTrue("Summary 4 is " + projectInfo.GetSummaries().get(3).toString(), projectInfo.GetSummaries().get(3).toString()
                .equals("Shape File Path: C:\\Users\\sufi\\Desktop\\shapefile\\shapefile.shp; Field: COUNTYNS10"));
    }

}
