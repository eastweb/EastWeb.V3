package version2.prototype.Scheduler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.DirectoryLayout;
import version2.prototype.ProjectInfo;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.ProcessMetaData;
import version2.prototype.projection.PrepareProcessTask;
import version2.prototype.projection.ProcessData;
import version2.prototype.summary.AvgGdalRasterFileMerge;
import version2.prototype.summary.SummaryData;
import version2.prototype.summary.TemporalSummaryCalculator;
import version2.prototype.summary.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.ZonalSummaryCalculator;

public class Scheduler {

    private static Scheduler instance;
    public ProjectInfo projectInfo;
    public Config config;
    public PluginMetaDataCollection pluginMetaDataCollection;
    private File outTable;
    private ArrayList<String> summarySingletonNames;

    private Scheduler(SchedulerData data)
    {
        projectInfo = data.projectInfo;
        config = data.config;
        pluginMetaDataCollection = data.pluginMetaDataCollection;
        outTable = data.OutTableFile;
        summarySingletonNames = data.SummarySingletonNames;
    }

    public static Scheduler getInstance(SchedulerData data)
    {
        if(instance == null)
            instance = new Scheduler(data);

        return instance;
    }

    public void run() throws Exception
    {
        for(String item: projectInfo.getPlugin())
        {
            RunDownloader(item);
            RunProcess(item);
            RunIndicies(item);
            RunSummary(item);
        }
    }

    public void RunDownloader(String pluginName) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        // uses reflection
        Class<?> clazzDownloader = Class.forName("version2.prototype.download."
                + PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginName).Title
                + PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginName).Download.className);
        Constructor<?> ctorDownloader = clazzDownloader.getConstructor(DataDate.class, DownloadMetaData.class);
        Object downloader =  ctorDownloader.newInstance(new Object[] {projectInfo.getStartDate(), PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginName).Download});
        Method methodDownloader = downloader.getClass().getMethod("run");
        methodDownloader.invoke(downloader);
    }

    public void RunProcess(String pluginName) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        ProcessMetaData temp = PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginName).Projection;
        PrepareProcessTask prepareProcessTask = new PrepareProcessTask(projectInfo, "NBAR", projectInfo.getStartDate());

        for (int i = 1; i <= temp.processStep.size(); i++)
            if(temp.processStep.get(i) != null && !temp.processStep.get(i).isEmpty())
            {
                Class<?> clazzProcess = Class.forName("version2.prototype.projection."
                        + PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginName).Title
                        + temp.processStep.get(i));
                Constructor<?> ctorProcess = clazzProcess.getConstructor(ProcessData.class);
                Object process =  ctorProcess.newInstance(new Object[] {new ProcessData(
                        prepareProcessTask.getInputFiles(),
                        prepareProcessTask.getBands(),
                        prepareProcessTask.getInputFile(),
                        prepareProcessTask.getOutputFile(),
                        projectInfo)});
                Method methodProcess = process.getClass().getMethod("run");
                methodProcess.invoke(process);
            }
    }

    public void RunIndicies(String pluginName) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        // get data for data, file, and the last thing that i dont really know about
        // ask jiameng what the hell the file is suppose to do
        for(String indexCalculatorItem: PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginName).IndicesMetaData)
        {
            Class<?> clazzIndicies = Class.forName("version2.prototype.indices."
                    + PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginName).Title
                    + indexCalculatorItem);
            Constructor<?> ctorIndicies = clazzIndicies.getConstructor(String.class, DataDate.class, String.class, String.class);
            Object indexCalculator =  ctorIndicies.newInstance(
                    new Object[] {
                            projectInfo.getName(),
                            projectInfo.getStartDate(),
                            new File(indexCalculatorItem).getName().split("\\.")[0],
                            indexCalculatorItem}
                    );
            Method methodIndicies = indexCalculator.getClass().getMethod("calculate");
            methodIndicies.invoke(indexCalculator);
        }
    }

    public void RunSummary(String pluginName) throws Exception
    {
        if(PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginName).Summary.IsTemporalSummary)
            for(ZonalSummary zone: projectInfo.getZonalSummaries())
            {
                Class<?> strategyClass = Class.forName(PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginName).Summary
                        .CompositionStrategyClassName);
                Constructor<?> ctorStrategy = strategyClass.getConstructor();
                Object temporalSummaryCompositionStrategy = ctorStrategy.newInstance();

                TemporalSummaryCalculator temporalSummaryCal = new TemporalSummaryCalculator(new SummaryData(
                    DirectoryLayout.getIndexMetadata(projectInfo, pluginName, projectInfo.getStartDate(), zone.getShapeFile()),
                    new File(DirectoryLayout.getSettingsDirectory(projectInfo), zone.getShapeFile()),
                    null,
                    null,
                    null,
                    projectInfo.getStartDate(),
                    0,
                    0,
                    projectInfo.getStartDate(),
                    (TemporalSummaryCompositionStrategy) temporalSummaryCompositionStrategy,       // User selected
                    null,   // InterpolateStrategy (Framework user defined)
                    new AvgGdalRasterFileMerge()));       // (Framework user defined)
                temporalSummaryCal.run();
            }

        for(ZonalSummary zone: projectInfo.getZonalSummaries())
        {
            ZonalSummaryCalculator zonalSummaryCal = new ZonalSummaryCalculator(new SummaryData(
                DirectoryLayout.getIndexMetadata(projectInfo, pluginName, projectInfo.getStartDate(), zone.getShapeFile()),
                new File(DirectoryLayout.getSettingsDirectory(projectInfo), zone.getShapeFile()),
                outTable,
                zone.getField(),
                summarySingletonNames,
                projectInfo.getStartDate(),
                0,
                0,
                projectInfo.getStartDate(),
                null,
                null,
                null));
            zonalSummaryCal.run();
        }
    }
}

