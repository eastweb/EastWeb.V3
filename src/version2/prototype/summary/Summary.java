package version2.prototype.summary;


import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;


import version2.prototype.Process;
import version2.prototype.ThreadState;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.summary.summaries.SummariesCollection;
import version2.prototype.summary.temporal.AvgGdalRasterFileMerge;
import version2.prototype.summary.temporal.TemporalSummaryCalculator;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.summary.zonal.ZonalSummaryCalculator;
import version2.prototype.util.CachedDataFile;
import version2.prototype.util.DatabaseFileCache;
import version2.prototype.util.GeneralUIEventObject;

public class Summary<V> extends Process<V> {
    private TemporalSummaryRasterFileStore fileStore;

    public Summary(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, ThreadState state, ProcessName processName, String inputTableName)
    {
        super(projectInfoFile, pluginInfo, pluginMetaData, scheduler, state, processName, inputTableName);
    }

    @Override
    public V call() throws Exception {
        SummaryData data;
        Class<?> strategyClass = Class.forName(pluginMetaData.Summary.CompositionStrategyClassName);
        Constructor<?> ctorStrategy = strategyClass.getConstructor();
        TemporalSummaryCompositionStrategy tempSummaryCompStrategy = (TemporalSummaryCompositionStrategy)ctorStrategy.newInstance();
        fileStore = new TemporalSummaryRasterFileStore(tempSummaryCompStrategy);

        ArrayList<CachedDataFile> cachedFiles = new ArrayList<CachedDataFile>();
        cachedFiles = DatabaseFileCache.GetAvailableFiles(projectInfoFile.GetProjectName(), pluginInfo.GetName(), mInputTableName);

        if(pluginMetaData.Summary.IsTemporalSummary)
        {
            for(CachedDataFile cachedFile : cachedFiles)
            {
                for(ZonalSummary zone: projectInfoFile.GetZonalSummaries())
                {
                    data = new SummaryData(
                            projectInfoFile.GetProjectName(),   // projectName
                            pluginInfo.GetName(),   // pluginName
                            new File(cachedFile.fullPath),     // inRasterFile
                            new File(zone.GetShapeFile()),  // inShapeFile
                            null,   // outTableFile     // TODO: Define output file
                            null,   // zoneField
                            null,   // summarySingletonNames
                            null,   // inDataDate
                            0,      // daysPerInputData
                            0,      // daysPerOutputData
                            fileStore,   // TemporalSummaryRasterFileStore
                            null,   // InterpolateStrategy
                            new AvgGdalRasterFileMerge() // (Framework user defined)
                            );
                    TemporalSummaryCalculator temporalSummaryCal = new TemporalSummaryCalculator(data, "TemporalSummaryWorker", this);
                    temporalSummaryCal.call();
                }
            }
        }

        for(CachedDataFile cachedFile : cachedFiles)
        {
            for(ZonalSummary zone: projectInfoFile.GetZonalSummaries())
            {
                data = new SummaryData(
                        projectInfoFile.GetProjectName(),   // projectName
                        pluginInfo.GetName(),   // pluginName
                        new File(cachedFile.fullPath),   // inRasterFile
                        new File(zone.GetShapeFile()),  // inShapeFile
                        null,   // outTableFile
                        zone.GetField(),    // zoneField
                        new SummariesCollection(new ArrayList<String>(Arrays.asList("Count", "Sum", "Mean", "StdDev"))), // summariesCollection
                        null,  // inDataDate
                        0,  // daysPerInputData
                        0,  // daysPerOutputData
                        null,   // TemporalSummaryRasterFileStore
                        null,   // InterpolateStrategy
                        null); // MergeStrategy
                ZonalSummaryCalculator zonalSummaryCal = new ZonalSummaryCalculator(data, "ZonalSummaryWorker", this);
                zonalSummaryCal.call();
            }
        }

        // TODO: Need to define when "finished" state has been reached as this doesn't work with asynchronous.
        scheduler.NotifyUI(new GeneralUIEventObject(this, "Summary Finished", 100));
        return null;
    }

}