package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.PluginMetaData.PluginMetaDataCollectionTester;
import test.ProjectInfoMetaData.ProjectInfoFileTester;
import test.summary.temporal.TemporalSummaryCompositionStrategyTester;
import test.summary.zonal.SummariesCollectionTester;
import test.util.SchemaTest;

@RunWith(Suite.class)
@SuiteClasses({ PluginMetaDataCollectionTester.class, ProjectInfoFileTester.class, ConfigTest.class, SummariesCollectionTester.class, TemporalSummaryCompositionStrategyTester.class,
    SchemaTest.class })
public class AllTests {

}
