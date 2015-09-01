package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.PluginMetaData.PluginMetaDataCollectionTester;
import test.ProjectInfoMetaData.ProjectInfoFileTester;
import test.Scheduler.SchedulerStatusContainerTest;
import test.Scheduler.SchedulerTest;
import test.download.GenericLocalRetrievalLocalDownloaderTest;
import test.download.GenericLocalStorageGlobalDownloaderTest;
import test.summary.zonal.SummariesCollectionTester;
import test.util.DatabaseCacheTest;
import test.util.DatabaseConnectorTest;
import test.util.EASTWebResultsTest;
import test.util.FileSystemTest;
import test.util.SchemasTest;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({ PluginMetaDataCollectionTester.class, ProjectInfoFileTester.class, ConfigTest.class, SummariesCollectionTester.class, SchemasTest.class,
    GenericLocalStorageGlobalDownloaderTest.class, FileSystemTest.class, DatabaseCacheTest.class, EASTWebResultsTest.class, SchedulerStatusContainerTest.class, SchedulerTest.class,
    GenericLocalRetrievalLocalDownloaderTest.class, DatabaseConnectorTest.class })
public class AllTests {

}
