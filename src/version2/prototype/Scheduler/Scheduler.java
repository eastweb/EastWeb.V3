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
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.projection.PrepareProcessTask;
import version2.prototype.projection.ProcessData;
import version2.prototype.summary.SummaryData;
import version2.prototype.summary.temporal.AvgGdalRasterFileMerge;
import version2.prototype.summary.temporal.TemporalSummaryCalculator;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.zonal.ZonalSummaryCalculator;
import version2.prototype.util.GeneralListener;
import version2.prototype.util.GeneralUIEventObject;

public class Scheduler {

    private static Scheduler instance;
    private SchedulerData data;
    public ProjectInfo projectInfo;
    public Config config;
    public PluginMetaDataCollection pluginMetaDataCollection;
    private File outTable;
    private ArrayList<String> summarySingletonNames;

    private Scheduler(SchedulerData data)
    {
        this.data = data;
        projectInfo = data.projectInfo;
        config = data.config;
        pluginMetaDataCollection = data.pluginMetaDataCollection;
        outTable = data.OutTableFile;
        summarySingletonNames = data.SummarySingletonNames;
    }

    public static Scheduler getInstance(SchedulerData data)
    {
        if(instance == null) {
            instance = new Scheduler(data);
        }

        return instance;
    }

    public void run() throws Exception
    {
        for(ProjectInfoPlugin item: data.projectInfoFile.plugins)
        {
            RunDownloader(item);
            RunProcess(item);
            RunIndicies(item);
            RunSummary(item);
        }
    }

    public void RunDownloader(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        // uses reflection
        Class<?> clazzDownloader = Class.forName("version2.prototype.download."
                + PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(plugin.GetName()).Title
                + PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(plugin.GetName()).Download.className);
        Constructor<?> ctorDownloader = clazzDownloader.getConstructor(DataDate.class, DownloadMetaData.class, GeneralListener.class);
        Object downloader =  ctorDownloader.newInstance(new Object[] {
                data.projectInfoFile.startDate,
                PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(plugin.GetName()).Download,
                new downloaderListener()});
        Method methodDownloader = downloader.getClass().getMethod("run");
        methodDownloader.invoke(downloader);
    }

    public void RunProcess(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        ProcessMetaData temp = PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(plugin.GetName()).Projection;
        PrepareProcessTask prepareProcessTask = new PrepareProcessTask(projectInfo, "NBAR", projectInfo.getStartDate(), new processListener());

        for (int i = 1; i <= temp.processStep.size(); i++) {
            if(temp.processStep.get(i) != null && !temp.processStep.get(i).isEmpty())
            {
                Class<?> clazzProcess = Class.forName("version2.prototype.projection."
                        + PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(plugin.GetName()).Title
                        + temp.processStep.get(i));
                Constructor<?> ctorProcess = clazzProcess.getConstructor(ProcessData.class);
                Object process =  ctorProcess.newInstance(new Object[] {new ProcessData(
                        prepareProcessTask.getInputFiles(),
                        prepareProcessTask.getBands(),
                        prepareProcessTask.getInputFile(),
                        prepareProcessTask.getOutputFile(),
                        data.projectInfoFile,
                        prepareProcessTask.listener)});
                Method methodProcess = process.getClass().getMethod("run");
                methodProcess.invoke(process);
            }
        }
    }

    public void RunIndicies(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        for(String indicie: plugin.GetIndicies())
        {
            Class<?> clazzIndicies = Class.forName(String.format("version2.prototype.indices.%S.%S", plugin.GetName(), indicie));
            Constructor<?> ctorIndicies = clazzIndicies.getConstructor(String.class, DataDate.class, String.class, String.class, GeneralListener.class);
            Object indexCalculator =  ctorIndicies.newInstance(
                    new Object[] {
                            plugin.GetName(),
                            data.projectInfoFile.startDate,
                            new File(indicie).getName().split("\\.")[0],
                            indicie,
                            new indiciesListener()});
            Method methodIndicies = indexCalculator.getClass().getMethod("calculate");
            methodIndicies.invoke(indexCalculator);
        }
    }

    public void RunSummary(ProjectInfoPlugin plugin) throws Exception
    {
    	if(PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(plugin.GetName()).Summary.IsTemporalSummary)
        {
            for(ZonalSummary zone: projectInfo.getZonalSummaries())
            {
                Class<?> strategyClass = Class.forName(PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(plugin.GetName()).Summary
                        .CompositionStrategyClassName);
                Constructor<?> ctorStrategy = strategyClass.getConstructor();
                Object temporalSummaryCompositionStrategy = ctorStrategy.newInstance();

                TemporalSummaryCalculator temporalSummaryCal = new TemporalSummaryCalculator(new SummaryData(
                        projectInfo.getName(),
                        DirectoryLayout.getIndexMetadata(projectInfo, plugin.GetName(), projectInfo.getStartDate(), zone.getShapeFile()),
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
                        new AvgGdalRasterFileMerge(),
                        new summaryListener()));       // (Framework user defined)
                temporalSummaryCal.run();
            }
        }

        for(ZonalSummary zone: projectInfo.getZonalSummaries())
        {
            ZonalSummaryCalculator zonalSummaryCal = new ZonalSummaryCalculator(new SummaryData(
                    projectInfo.getName(),
                    DirectoryLayout.getIndexMetadata(projectInfo, plugin.GetName(), projectInfo.getStartDate(), zone.getShapeFile()),
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
                    null,
                    new summaryListener()));
            zonalSummaryCal.run();
        }
    }

    class downloaderListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            // TODO Auto-generated method stub

        }
    }

    class processListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            // TODO Auto-generated method stub

        }
    }

    class indiciesListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            // TODO Auto-generated method stub

        }
    }

    class summaryListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            // TODO Auto-generated method stub

        }
    }

}

