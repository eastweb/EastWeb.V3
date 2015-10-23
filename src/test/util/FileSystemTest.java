/**
 *
 */
package test.util;

import static org.junit.Assert.*;
import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.util.FileSystem;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.Scheduler.ProcessName;

/**
 * @author michael.devos
 *
 */
public class FileSystemTest {
    private static Config testConfigInstance;
    private static ProjectInfoFile testProjectInfo;
    private static ArrayList<String> testNames;
    private static String testPluginName;
    private static String testProjectName;
    private static ArrayList<ProcessName> testProcessNames;

    /**
     *
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        testConfigInstance = Config.getAnInstance("C:\\EASTWeb", "C:\\EASTWeb\\Downloads", null, null, null, null, null, null, null, null, null);
        testProjectInfo = new ProjectInfoFile(null, null, null, "C:\\Users\\sufi", null, null, null, null, null, null, null, null, null, null, null, null);
        testNames = new ArrayList<String>(4);
        testNames.add("A_Product");
        testNames.add("1A_Product");
        testNames.add("1A_Product1");
        testNames.add("@1");
        testPluginName = "Test Plugin";
        testProjectName = "Test Project";
        testProcessNames = new ArrayList<ProcessName>(4);
        testProcessNames.add(ProcessName.DOWNLOAD);
        testProcessNames.add(ProcessName.INDICES);
        testProcessNames.add(ProcessName.PROCESSOR);
        testProcessNames.add(ProcessName.SUMMARY);
    }

    /**
     *
     */
    @AfterClass
    public static void tearDownAfterClass() {
        testProjectInfo = null;
        testNames = null;
        testPluginName = null;
        testProcessNames = null;
    }

    /**
     * Test method for {@link version2.prototype.util.FileSystem#GetRootDirectoryPath(version2.prototype.ProjectInfoMetaData.ProjectInfoFile)}.
     */
    @Test
    public final void testGetRootDirectoryPath() {
        FileSystem.GetRootDirectoryPath(testProjectInfo);
    }

    /**
     * Test method for {@link version2.prototype.util.FileSystem#StandardizeName(java.lang.String)}.
     */
    @Test
    public final void testStandardizeName() {
        ArrayList<String> expectedNames = new ArrayList<String>(4);
        expectedNames.add("A_Product");
        expectedNames.add("_A_Product");
        expectedNames.add("_A_Product1");
        expectedNames.add("_1");

        for(int i=0; i < testNames.size(); i++) {
            assertTrue("Standardized Name is '" + FileSystem.StandardizeName(testNames.get(i)) + "'", FileSystem.StandardizeName(testNames.get(i)).equals(expectedNames.get(i)));
        }
    }

    /**
     * Test method for {@link version2.prototype.util.FileSystem#GetProjectDirectoryPath(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void testGetProjectDirectoryPath() {
        /*
         * Expected Result Form:
         *  workingDir + "/Projects/" + FileSystem.StandardizeName(testProjectName) + "/"
         */
        assertEquals("ProjectDirectoryPath is incorrect.", testProjectInfo.GetWorkingDir() + "Projects\\" + FileSystem.StandardizeName(testProjectName) + "\\",
                FileSystem.GetProjectDirectoryPath(testProjectInfo.GetWorkingDir(), testProjectName));
    }

    /**
     * Test method for {@link version2.prototype.util.FileSystem#GetProcessDirectoryPath(java.lang.String, java.lang.String, java.lang.String, version2.prototype.Scheduler.ProcessName)}.
     */
    @Test
    public final void testGetProcessDirectoryPath() {
        /*
         * Expected Result Form:
         *  workingDir + "/Projects/" + FileSystem.StandardizeName(testProjectName) + "/" + FileSystem.StandardizeName(testPluginName) + "/" + processName + "/"
         */
        ArrayList<String> expectedResults = new ArrayList<String>(4);
        for(ProcessName name : testProcessNames) {
            expectedResults.add(testProjectInfo.GetWorkingDir() + "Projects\\" + FileSystem.StandardizeName(testProjectName) + "\\" + FileSystem.StandardizeName(testPluginName) + "\\" + name + "\\");
        }

        // Test
        for(int i=0; i < testProcessNames.size(); i++) {
            assertEquals("ProcessDirectoryPath is incorrect.", expectedResults.get(i), FileSystem.GetProcessDirectoryPath(testProjectInfo.GetWorkingDir(), testProjectName, testPluginName, testProcessNames.get(i)));
        }
    }

    /**
     * Test method for {@link version2.prototype.util.FileSystem#GetProcessOutputDirectoryPath(java.lang.String, java.lang.String, java.lang.String, version2.prototype.Scheduler.ProcessName)}.
     */
    @Test
    public final void testGetProcessOutputDirectoryPath() {
        /*
         * Expected Result Form:
         *  workingDir + "/Projects/" + FileSystem.StandardizeName(testProjectName) + "/" + FileSystem.StandardizeName(testPluginName) + "/" + processName + "/" + "Output/"
         */
        ArrayList<String> expectedResults = new ArrayList<String>(4);
        for(ProcessName name : testProcessNames) {
            expectedResults.add(testProjectInfo.GetWorkingDir() + "Projects\\" + FileSystem.StandardizeName(testProjectName) + "\\" + FileSystem.StandardizeName(testPluginName) + "\\" + name + "\\Output\\");
        }

        // Test
        for(int i=0; i < testProcessNames.size(); i++) {
            assertEquals("ProcessOutputDirectoryPath is incorrect", expectedResults.get(i), FileSystem.GetProcessOutputDirectoryPath(testProjectInfo.GetWorkingDir(), testProjectName, testPluginName, testProcessNames.get(i)));
        }
    }

    /**
     * Test method for {@link version2.prototype.util.FileSystem#GetProcessWorkerTempDirectoryPath(java.lang.String, java.lang.String, java.lang.String, version2.prototype.Scheduler.ProcessName)}.
     */
    @Test
    public final void testGetProcessWorkerTempDirectoryPath() {
        /*
         * Expected Result Form:
         *  workingDir + "/Projects/" + FileSystem.StandardizeName(testProjectName) + "/" + FileSystem.StandardizeName(testPluginName) + "/" + processName + "/" + "Temp/"
         */
        ArrayList<String> expectedResults = new ArrayList<String>(4);
        for(ProcessName name : testProcessNames) {
            expectedResults.add(testProjectInfo.GetWorkingDir() + "Projects\\" + FileSystem.StandardizeName(testProjectName) + "\\" + FileSystem.StandardizeName(testPluginName) + "\\" + name + "\\Temp\\");
        }

        // Test
        for(int i=0; i < testProcessNames.size(); i++) {
            assertEquals("ProcessWorkerTempDirectoryPath is incorrect.", expectedResults.get(i),
                    FileSystem.GetProcessWorkerTempDirectoryPath(testProjectInfo.GetWorkingDir(), testProjectName, testPluginName, testProcessNames.get(i)));
        }
    }

    /**
     * Test method for {@link version2.prototype.util.FileSystem#GetGlobalDownloadDirectory(java.lang.String)}.
     * @throws ConfigReadException
     */
    @Test
    public final void testGetGlobalDownloadDirectory() throws ConfigReadException {
        /*
         * Expected Result Form:
         *  testConfigInstance.getDownloadDir() + "/" + FileSystem.StandardizeName(testPluginName) + "/"
         */
        assertEquals("GlobalDownloadDirectory is incorrect.", testConfigInstance.getDownloadDir() + FileSystem.StandardizeName(testPluginName) + "\\Data\\",
                FileSystem.GetGlobalDownloadDirectory(testConfigInstance, testPluginName, "Data"));
    }

}
