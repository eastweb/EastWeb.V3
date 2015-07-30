package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.PluginMetaData.PluginMetaDataCollectionTester;
import test.ProjectInfoMetaData.ProjectInfoFileTester;
import test.download.GenericLocalStorageGlobalDownloaderTest;
import test.summary.zonal.SummariesCollectionTester;
import test.util.SchemasTest;

@SuppressWarnings("javadoc")
@RunWith(Suite.class)
@SuiteClasses({ PluginMetaDataCollectionTester.class, ProjectInfoFileTester.class, ConfigTest.class, SummariesCollectionTester.class, SchemasTest.class,
    GenericLocalStorageGlobalDownloaderTest.class})
public class AllTests {

}
