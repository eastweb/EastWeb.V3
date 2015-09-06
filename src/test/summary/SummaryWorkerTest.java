/**
 *
 */
package test.summary;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Observable;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.Config;
import version2.prototype.EASTWebManagerI;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.summary.SummaryWorker;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.EASTWebResult;
import version2.prototype.util.EASTWebResults;
import version2.prototype.util.GeneralUIEventObject;
import version2.prototype.util.IndicesFileMetaData;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.Schemas;
import version2.prototype.Process;

/**
 * @author michael.devos
 *
 */
public class SummaryWorkerTest {
    private static Config configInstance;
    private static String pluginName = "TRMM3B42RT";
    private static ProjectInfoFile projectInfoFile;
    private static LocalDate startDate;
    private static ProjectInfoPlugin pluginInfo;
    private static PluginMetaData pluginMetaData;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        configInstance = Config.getAnInstance("src\\test\\config.xml");
        projectInfoFile = new ProjectInfoFile("src\\test\\summary\\Test_Project.xml");
        startDate = projectInfoFile.GetStartDate();
        pluginInfo = projectInfoFile.GetPlugins().get(0);
        pluginMetaData = PluginMetaDataCollection.getInstance(new File("plugins\\Plugin_TRMM3B42RT.xml")).pluginMetaDataMap.get(projectInfoFile.GetPlugins().get(0).GetName());

        Connection con = DatabaseConnector.getConnection();
        Statement stmt = con.createStatement();
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                configInstance.getGlobalSchema()
                );
        stmt.execute(query);
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%1$s\" CASCADE",
                Schemas.getSchemaName(projectInfoFile.GetProjectName(), pluginName)
                );
        stmt.execute(query);
        stmt.close();
        con.close();

        Schemas.CreateProjectPluginSchema(DatabaseConnector.getConnection(), configInstance.getGlobalSchema(), projectInfoFile, pluginName, configInstance.getSummaryCalculations(), pluginMetaData.DaysPerInputData,
                pluginMetaData.Download.filesPerDay, pluginMetaData.Indices.indicesNames.size(), false);
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
     * Test method for {@link version2.prototype.summary.SummaryWorker#call()}.
     * @throws Exception
     */
    @Test
    public final void testCall() throws Exception {

        // Setup test files
        ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm1.tif", 1, startDate.getYear(), startDate.minusDays(6).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm2.tif", 1, startDate.getYear(), startDate.minusDays(5).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm3.tif", 1, startDate.getYear(), startDate.minusDays(4).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm4.tif", 1, startDate.getYear(), startDate.minusDays(3).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm5.tif", 1, startDate.getYear(), startDate.minusDays(2).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm6.tif", 1, startDate.getYear(), startDate.minusDays(1).getDayOfYear(), "TRMM3B42RTIndex")));
        cachedFiles.add(new DataFileMetaData(new IndicesFileMetaData("src\\test\\summary\\trmm7.tif", 1, startDate.getYear(), startDate.getDayOfYear(), "TRMM3B42RTIndex")));

        SummaryWorker worker = new SummaryWorker(configInstance, new MyProcess(projectInfoFile), projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, null);
        worker.call();

        // Verify results
        ArrayList<EASTWebResult> results = EASTWebResults.GetEASTWebResults(EASTWebResults.GetEASTWebQuery(configInstance.getGlobalSchema(), projectInfoFile.GetProjectName(), pluginInfo.GetName()));
        assertTrue("Results empty.", results.size() > 0);
    }

    private class MyProcess extends Process
    {

        protected MyProcess(ProjectInfoFile projectMetaData) {
            super(null, null, null, projectMetaData, null, null, null, null);
        }

        @Override
        public void process(ArrayList<DataFileMetaData> cachedFiles) {
            // Do nothing
        }

        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            // Do nothing
        }

        @Override
        public void update(Observable o, Object arg) {
            // Do nothing
        }
    }
}
