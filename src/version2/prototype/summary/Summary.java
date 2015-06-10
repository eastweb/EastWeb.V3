package version2.prototype.summary;


import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;

import javax.swing.event.ChangeEvent;

import version2.prototype.Process;
import version2.prototype.ThreadState;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.Scheduler.SchedulerState;
import version2.prototype.summary.temporal.AvgGdalRasterFileMerge;
import version2.prototype.summary.temporal.TemporalSummaryCalculator;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.zonal.ZonalSummaryCalculator;
import version2.prototype.util.FileSystem;
import version2.prototype.util.GeneralListener;
import version2.prototype.util.GeneralUIEventObject;

public class Summary<V> extends Process<V> {
    public Summary(ProcessName processName, ThreadState state, Scheduler scheduler, ProjectInfoPlugin pluginInfo, ProjectInfoFile projectInfoFile,
            PluginMetaDataCollection pluginMetaDataCollection, String inputTableName)
    {
        super(processName, state, scheduler, pluginInfo, projectInfoFile, pluginMetaDataCollection, inputTableName);
    }

    @Override
    public V call() throws Exception {
        ArrayList<String> dataFilePaths = new ArrayList<String>();
        for(CachedDataFile cdf : DatabaseFileCache.GetAvailableFiles(projectInfoFile.GetProjectName(), pluginInfo.GetName(), mInputTableName))
        {
            dataFilePaths.add(cdf.fullPath);
        }

        if(PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginInfo.GetName()).Summary.IsTemporalSummary)
        {
            for(String dataFilePath : dataFilePaths)
            {
                for(ZonalSummary zone: projectInfoFile.GetZonalSummaries())
                {
                    Class<?> strategyClass = Class.forName(PluginMetaDataCollection.getInstance().pluginMetaDataMap.get(pluginInfo.GetName()).Summary
                            .CompositionStrategyClassName);
                    Constructor<?> ctorStrategy = strategyClass.getConstructor();
                    Object tempSummaryCompStrategy = ctorStrategy.newInstance();

                    TemporalSummaryCalculator temporalSummaryCal = new TemporalSummaryCalculator(new SummaryData(
                            projectInfoFile.GetProjectName(),   // projectName
                            new File(dataFilePath),     // inRaster
                            new File(zone.GetShapeFile()),  // inShape
                            null,   // outTable     // TODO: Define output file
                            null,   // zone field
                            null,   // SummarySingletonNames
                            null,   // DataDate inDate
                            0,      // daysPerInputData
                            0,      // daysPerOutputData
                            (TemporalSummaryCompositionStrategy) tempSummaryCompStrategy,   // TemporalSummaryCompositionStrategy (User selected)
                            null,   // InterpolateStrategy (Framework user defined)
                            new AvgGdalRasterFileMerge()));       // (Framework user defined)
                    temporalSummaryCal.run();
                }
            }
        }

        for(String dataFilePath : dataFilePaths)
        {
            for(ZonalSummary zone: projectInfoFile.GetZonalSummaries())
            {
                ZonalSummaryCalculator zonalSummaryCal = new ZonalSummaryCalculator(new SummaryData(
                        projectInfoFile.GetProjectName(),   // projectName
                        new File(dataFilePath),   // inRaster
                        new File(zone.GetShapeFile()),  // inShape
                        null,   // outTable
                        zone.GetField(),    // zone field
                        new ArrayList<String>(Arrays.asList("Count", "Sum", "Mean", "StdDev")), // SummarySingletonNames
                        null,  // DataDate inDate
                        0,  // daysPerInputData
                        0,  // daysPerOutputData
                        null,   // TemporalSummaryCompositionStrategy
                        null,   // InterpolateStrategy
                        null)); // MergeStrategy
                zonalSummaryCal.run();
            }
        }

        // TODO: Need to define when "finished" state has been reached as this doesn't work with asynchronous.
        scheduler.NotifyUI(new GeneralUIEventObject(this, "Summary Finished", 100));
        return null;
    }

}