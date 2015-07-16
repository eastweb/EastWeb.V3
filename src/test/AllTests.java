package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.PluginMetaData.PluginMetaDataCollectionTester;
import test.ProjectInfoMetaData.ProjectInfoFileTester;
import test.download.TRMM3B42RT.TRMM3B42RTListDatesFilesTest;
import test.summary.temporal.TemporalSummaryCompositionStrategyTester;
import test.summary.zonal.SummariesCollectionTester;
import test.util.SchemasTest;

@RunWith(Suite.class)
@SuiteClasses({ PluginMetaDataCollectionTester.class, ProjectInfoFileTester.class, ConfigTest.class, SummariesCollectionTester.class, TemporalSummaryCompositionStrategyTester.class,
    SchemasTest.class, TRMM3B42RTListDatesFilesTest.class })
public class AllTests {

}
