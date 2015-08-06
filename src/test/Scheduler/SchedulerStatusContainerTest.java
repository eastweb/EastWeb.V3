/**
 *
 */
package test.Scheduler;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.Config;
import version2.prototype.TaskState;
import version2.prototype.ZonalSummary;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.Scheduler.SchedulerStatus;
import version2.prototype.Scheduler.SchedulerStatusContainer;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy;

/**
 * @author michael.devos
 *
 */
public class SchedulerStatusContainerTest {
    private static SchedulerStatusContainer container;
    private static String pluginName1 = "ModisNBAR";
    private static String pluginName2 = "TRMM3B42RT";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Config configInstance = Config.getAnInstance("config.xml");
        int schedulerID = 1;
        String projectName = "test_SchedulerStatusContainerTest";
        ArrayList<ProjectInfoPlugin> pluginInfo = new ArrayList<ProjectInfoPlugin>();
        ArrayList<String> indices = new ArrayList<String>();
        indices.add("ModisNBARNDVI");
        indices.add("ModisNBAREVI");
        indices.add("ModisNBARNDWI5");
        pluginInfo.add(new ProjectInfoPlugin(pluginName1, indices, "Highest"));
        indices = new ArrayList<String>();
        indices.add("TRMM3B42RTIndex");
        pluginInfo.add(new ProjectInfoPlugin(pluginName2, indices, null));
        ArrayList<ProjectInfoSummary> summaries = new ArrayList<ProjectInfoSummary>();
        ZonalSummary zonalSummary = new ZonalSummary("a shape file path", "areaValueField", "areaNameField");
        String temporalSummaryCompositionStrategyClassName = "GregorianWeeklyStrategy";
        TemporalSummaryCompositionStrategy compStrategy = new GregorianWeeklyStrategy();
        TemporalSummaryRasterFileStore fileStore = new TemporalSummaryRasterFileStore(compStrategy);
        int summaryID = 1;
        summaries.add(new ProjectInfoSummary(zonalSummary, fileStore, temporalSummaryCompositionStrategyClassName, summaryID));
        TaskState state = TaskState.STOPPED;

        container = new SchedulerStatusContainer(configInstance, schedulerID, projectName, pluginInfo, summaries, state);
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
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateDownloadProgress(int, java.lang.String)}.
     */
    @Test
    public final void testUpdateDownloadProgress() {
        SchedulerStatus status = container.GetStatus();
        status.GetDownloadProgress();
        container.UpdateDownloadProgress(25, pluginName1);
        container.UpdateDownloadProgress(50, pluginName2);
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateProcessorProgress(int, java.lang.String)}.
     */
    @Test
    public final void testUpdateProcessorProgress() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateIndicesProgress(int, java.lang.String)}.
     */
    @Test
    public final void testUpdateIndicesProgress() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateSummaryProgress(int, java.lang.String)}.
     */
    @Test
    public final void testUpdateSummaryProgress() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateNumOfFilesLoaded()}.
     */
    @Test
    public final void testUpdateNumOfFilesLoaded() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#UpdateNumOfResultsPublished()}.
     */
    @Test
    public final void testUpdateNumOfResultsPublished() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#CheckIfProjectIsUpToDate()}.
     */
    @Test
    public final void testCheckIfProjectIsUpToDate() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#CheckIfProjectIsUpToDate(boolean, boolean)}.
     */
    @Test
    public final void testCheckIfProjectIsUpToDateBooleanBoolean() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.Scheduler.SchedulerStatusContainer#CheckIfResultsUpToDate(boolean)}.
     */
    @Test
    public final void testCheckIfResultsUpToDate() {
        fail("Not yet implemented"); // TODO
    }

}
