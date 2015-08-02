/**
 *
 */
package test.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.util.EASTWebResults;

/**
 * @author michael.devos
 *
 */
public class EASTWebResultsTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.util.EASTWebResults#GetEASTWebQuery(java.lang.String, java.lang.String, java.lang.String, boolean, boolean, boolean, boolean, java.lang.String, int,
     * java.lang.String, int, java.lang.String, int, java.util.ArrayList, version2.prototype.ZonalSummary)}.
     */
    @Test
    public final void testGetEASTWebQueryStringStringStringBooleanBooleanBooleanBooleanStringIntStringIntStringIntArrayListOfStringZonalSummary() {
        EASTWebResults.GetEASTWebQuery(globalSchema, projectName, pluginName, selectCount, selectSum, selectMean, selectStdDev, zoneSign, zoneVal,
                yearSign, yearVal, daySign, dayVal, includedIndices, zonalSummary);
    }

    /**
     * Test method for {@link version2.prototype.util.EASTWebResults#GetEASTWebQuery(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])}.
     */
    @Test
    public final void testGetEASTWebQueryStringStringStringStringArray() {
        EASTWebResults.GetEASTWebQuery(globalSchema, projectName, pluginName, indices);
    }

    /**
     * Test method for {@link version2.prototype.util.EASTWebResults#GetEASTWebQuery(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testGetEASTWebQueryStringStringString() {
        EASTWebResults.GetEASTWebQuery(globalSchema, projectName, pluginName);
    }

    /**
     * Test method for {@link version2.prototype.util.EASTWebResults#GetEASTWebResults(version2.prototype.util.EASTWebQuery)}.
     */
    @Test
    public final void testGetEASTWebResults() {
        EASTWebResults.GetEASTWebResults(query);
    }

    /**
     * Test method for {@link version2.prototype.util.EASTWebResults#GetResultCSVFiles(version2.prototype.util.EASTWebQuery)}.
     */
    @Test
    public final void testGetResultCSVFiles() {
        EASTWebResults.GetResultCSVFiles(query);
    }

}
