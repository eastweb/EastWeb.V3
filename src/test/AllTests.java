package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.PluginMetaData.PluginMetaDataCollectionTester;
import test.ProjectInfoMetaData.ProjectInfoFileTester;

@RunWith(Suite.class)
@SuiteClasses({ PluginMetaDataCollectionTester.class, ProjectInfoFileTester.class })
public class AllTests {

}
