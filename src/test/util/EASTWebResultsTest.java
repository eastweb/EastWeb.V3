/**
 *
 */
package test.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.util.EASTWebQuery;
import version2.prototype.util.EASTWebResults;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class EASTWebResultsTest {
    private static Connection con;
    private static String globalSchema = "Test_EW";
    private static String projectName = "Test_Project";
    private static String pluginName = "Test_Plugin";
    private static ArrayList<String> extraDownloadFiles;
    private static ArrayList<String> summaryNames;
    private static boolean selectCount = true;
    private static boolean selectMax = true;
    private static boolean selectMin = true;
    private static boolean selectSum = true;
    private static boolean selectMean = true;
    private static boolean selectSqrSum = true;
    private static boolean selectStdDev = true;
    private static String zoneSign = "<";
    private static int zoneVal = 300;
    private static String yearSign = ">";
    private static int yearVal = 2000;
    private static String daySign = "=";
    private static int dayVal = 180;
    private static ArrayList<String> includedIndices = new ArrayList<String>();
    private static Integer[] includedSummaries = new Integer[]{1};
    private static String[] indices = new String[5];
    private static ProjectInfoFile projectMetaData;
    //    private static String shapeFile = "Settings\\shapeFile";
    //    private static String areaValueField = "areaValueField";
    //    private static String areaNameField = "areaNameField";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        con = DatabaseConnector.getConnection();

        extraDownloadFiles = new ArrayList<String>();
        extraDownloadFiles.add("QC");

        includedIndices.add("ModisNBARNDVICalculator");
        includedIndices.add("ModisNBAREVICalculator");
        includedIndices.add("ModisNBARNDWI5Calculator");
        includedIndices.add("ModisNBARNDWI6Calculator");
        includedIndices.add("ModisNBARSAVICalculator");

        indices[0] = "ModisNBARNDVICalculator";
        indices[1] = "ModisNBAREVICalculator";
        indices[2] = "ModisNBARNDWI5Calculator";
        indices[3] = "ModisNBARNDWI6Calculator";
        indices[4] = "ModisNBARSAVICalculator";

        summaryNames = new ArrayList<String>();
        summaryNames.add("Count");
        summaryNames.add("Sum");
        summaryNames.add("Mean");
        summaryNames.add("Max");
        summaryNames.add("Min");
        summaryNames.add("StdDev");
        summaryNames.add("SqrSum");

        projectMetaData = new ProjectInfoFile(null, LocalDate.now(), projectName, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Statement stmt = con.createStatement();
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                globalSchema
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(projectName, pluginName)
                );
        stmt.execute(query);
        stmt.close();
        con.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Statement stmt = con.createStatement();
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                globalSchema
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(projectName, pluginName)
                );
        stmt.execute(query);
        stmt.close();

        Schemas.CreateProjectPluginSchema(con, globalSchema, projectMetaData, pluginName, summaryNames, null, null, null, true);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.util.EASTWebResults#GetEASTWebQuery(String, String, String, boolean, boolean, boolean, boolean, boolean, boolean, boolean, String, Integer, String, Integer,
     * String, Integer, ArrayList, ArrayList)}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testGetEASTWebQueryStringStringStringBooleanBooleanBooleanBooleanStringIntStringIntStringIntArrayListOfStringZonalSummary() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        //        ZonalSummary zonalSummary = new ZonalSummary(shapeFile, areaValueField, areaNameField);
        EASTWebQuery query = EASTWebResults.GetEASTWebQuery(globalSchema, projectName, pluginName, selectCount, selectMax, selectMin, selectSum, selectMean, selectSqrSum,
                selectStdDev, zoneSign, zoneVal,
                yearSign, yearVal, daySign, dayVal, includedIndices, includedSummaries);
        System.out.println("testGetEASTWebQueryStringStringStringBooleanBooleanBooleanBooleanStringIntStringIntStringIntArrayListOfStringZonalSummary:");
        System.out.println(query);
        System.out.println();
        EASTWebResults.GetEASTWebResults(query);
    }

    /**
     * Test method for {@link version2.prototype.util.EASTWebResults#GetEASTWebQuery(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testGetEASTWebQueryStringStringStringStringArray() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, SQLException {
        EASTWebQuery query = EASTWebResults.GetEASTWebQuery(globalSchema, projectName, pluginName, indices);
        System.out.println("testGetEASTWebQueryStringStringStringStringArray:");
        System.out.println(query);
        System.out.println();
        EASTWebResults.GetEASTWebResults(query);
    }

    /**
     * Test method for {@link version2.prototype.util.EASTWebResults#GetEASTWebQuery(java.lang.String, java.lang.String, java.lang.String)}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    public final void testGetEASTWebQueryStringStringString() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, SQLException {
        EASTWebQuery query = EASTWebResults.GetEASTWebQuery(globalSchema, projectName, pluginName);
        System.out.println("testGetEASTWebQueryStringStringString:");
        System.out.println(query);
        System.out.println();
        EASTWebResults.GetEASTWebResults(query);
    }

}
