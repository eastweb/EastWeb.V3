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
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.ProcessMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.projection.PrepareProcessTask;
import version2.prototype.projection.ProcessData;
import version2.prototype.summary.temporal.AvgGdalRasterFileMerge;
import version2.prototype.summary.SummaryData;
import version2.prototype.summary.temporal.TemporalSummaryCalculator;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.zonal.ZonalSummaryCalculator;
import version2.prototype.util.GeneralListener;
import version2.prototype.util.GeneralUIEventObject;

public class Scheduler implements Runnable {

    public int DownloadProgress;
    public int ProcessProgress;
    public int IndiciesProgress;
    public int SummaryProgress;
    public ArrayList<String> Log;

    public SchedulerData data;
    public ProjectInfoFile projectInfoFile;
    public PluginMetaDataCollection pluginMetaDataCollection;
    private File outTable;
    private ArrayList<String> summarySingletonNames;

    public Scheduler(SchedulerData data)
    {
        DownloadProgress = 0;
        ProcessProgress = 0;
        IndiciesProgress = 0;
        SummaryProgress = 0;
        Log = new ArrayList<String>();

        this.data = data;
        projectInfoFile = data.projectInfoFile;
        pluginMetaDataCollection = data.pluginMetaDataCollection;
        summarySingletonNames = data.SummarySingletonNames;
    }

    @Override
    public void run()
    {
        for(ProjectInfoPlugin item: data.projectInfoFile.plugins)
        {
            try {
                RunDownloader(item);
                RunProcess(item);
                RunIndicies(item);
                RunSummary(item);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

        DownloadProgress = 100;
        Log.add("Download Finish");
    }

    public void RunProcess(ProjectInfoPlugin plugin) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserConfigurationException, SAXException, IOException
    {
        ProcessMetaData temp = PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(plugin.GetName()).Projection;
        // TODO: revise the "date"
        PrepareProcessTask prepareProcessTask;
        // TODO: initiate it with each plugin's implementation
        //prepareProcessTask= new PrepareProcessTask(projectInfoFile, plugin.GetName(), projectInfoFile.startDate, new processListener());

        /* will move to the Projection framework
        for (int i = 1; i <= temp.processStep.size(); i++) {
            if(temp.processStep.get(i) != null && !temp.processStep.get(i).isEmpty())
            {
                Class<?> clazzProcess = Class.forName("version2.prototype.projection."
                        + PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(plugin.GetName()).Title
                        + temp.processStep.get(i));
                Constructor<?> ctorProcess = clazzProcess.getConstructor(ProcessData.class);
                Object process =  ctorProcess.newInstance(new Object[] {new ProcessData(
                        prepareProcessTask.getInputFolders(i),
                        prepareProcessTask.getOutputFolder(i),
                        prepareProcessTask.getQC(),
                        prepareProcessTask.getShapeFile(),
                        prepareProcessTask.getMaskFile(),
                        prepareProcessTask.getDataBands(),
                        prepareProcessTask.getQCBands(),
                        prepareProcessTask.getProjection(),
                        prepareProcessTask.getListener())});
                Method methodProcess = process.getClass().getMethod("run");
                methodProcess.invoke(process);
            }
        }
         */
        ProcessProgress = 100;
        Log.add("Process Finish");
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
        IndiciesProgress = 100;
        Log.add("Indicies Finish");
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
        SummaryProgress = 100;
        Log.add("Summary Finish");

    }

    class downloaderListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            DownloadProgress = e.getProgress();
            Log.add(e.getStatus());
        }
    }

    class processListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            ProcessProgress = e.getProgress();
            Log.add(e.getStatus());
        }
    }

    class indiciesListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            IndiciesProgress = e.getProgress();
            Log.add(e.getStatus());
        }
    }

    class summaryListener implements GeneralListener{
        @Override
        public void NotifyUI(GeneralUIEventObject e) {
            SummaryProgress = e.getProgress();
            Log.add(e.getStatus());
        }
    }

}

