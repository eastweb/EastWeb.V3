package version2.prototype.summary;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.ZonalSummary;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.summary.summaries.SummariesCollection;
import version2.prototype.summary.temporal.AvgGdalRasterFileMerge;
import version2.prototype.summary.temporal.TemporalSummaryCalculator;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.summary.zonal.ZonalSummaryCalculator;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.FileSystem;

public class SummaryWorker extends ProcessWorker<DataFileMetaData> {
    private ProjectInfoFile projectInfoFile;
    private ProjectInfoPlugin pluginInfo;
    private PluginMetaData pluginMetaData;
    private TemporalSummaryRasterFileStore fileStore;
    private ArrayList<DataFileMetaData> cachedFiles;

    protected SummaryWorker(Process<?> process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, TemporalSummaryRasterFileStore fileStore, ArrayList<DataFileMetaData> cachedFiles)
    {
        super("SummaryWorker", process);
        this.projectInfoFile = projectInfoFile;
        this.pluginInfo = pluginInfo;
        this.pluginMetaData = pluginMetaData;
        this.fileStore = fileStore;
        this.cachedFiles = cachedFiles;
    }

    @Override
    public DataFileMetaData call() throws Exception {
        SummaryData data;

        if(pluginMetaData.Summary.IsTemporalSummary)
        {
            ArrayList<DataFileMetaData> tempFiles = new ArrayList<DataFileMetaData>(0);

            for(DataFileMetaData cachedFile : cachedFiles)
            {
                for(ZonalSummary zone: projectInfoFile.GetZonalSummaries())
                {
                    data = new SummaryData(
                            projectInfoFile.GetWorkingDir(),
                            projectInfoFile.GetProjectName(),   // projectName
                            pluginInfo.GetName(),   // pluginName
                            new File(cachedFile.fullPath),     // inRasterFile
                            new File(zone.GetShapeFile()),  // inShapeFile
                            null,   // outTableFile
                            null,   // zoneField
                            null,   // summarySingletonNames
                            null,   // inDataDate
                            0,      // daysPerInputData
                            0,      // daysPerOutputData
                            fileStore,   // TemporalSummaryRasterFileStore
                            null,   // InterpolateStrategy
                            new AvgGdalRasterFileMerge() // (Framework user defined)
                            );
                    TemporalSummaryCalculator temporalSummaryCal = new TemporalSummaryCalculator(data);
                    tempFiles.add(temporalSummaryCal.calculate());
                }
            }
            cachedFiles = tempFiles;
        }

        File outputFile;
        for(DataFileMetaData cachedFile : cachedFiles)
        {
            for(ZonalSummary zone: projectInfoFile.GetZonalSummaries())
            {
                outputFile = new File(FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName(),
                        pluginInfo.GetName(), ProcessName.SUMMARY) + String.format("%04d%03d.tif", cachedFile.year, cachedFile.day));
                data = new SummaryData(
                        projectInfoFile.GetWorkingDir(),
                        projectInfoFile.GetProjectName(),   // projectName
                        pluginInfo.GetName(),   // pluginName
                        new File(cachedFile.fullPath),   // inRasterFile
                        new File(zone.GetShapeFile()),  // inShapeFile
                        outputFile,   // outTableFile
                        zone.GetField(),    // zoneField
                        new SummariesCollection(new ArrayList<String>(Arrays.asList("Count", "Sum", "Mean", "StdDev"))), // summariesCollection
                        null,  // inDataDate
                        0,  // daysPerInputData
                        0,  // daysPerOutputData
                        null,   // TemporalSummaryRasterFileStore
                        null,   // InterpolateStrategy
                        null); // MergeStrategy
                ZonalSummaryCalculator zonalSummaryCal = new ZonalSummaryCalculator(data);
                zonalSummaryCal.calculate();
            }
        }
        return null;
    }

}
