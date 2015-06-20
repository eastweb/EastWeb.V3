package version2.prototype.summary;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.summary.summaries.SummariesCollection;
import version2.prototype.summary.temporal.AvgGdalRasterFileMerge;
import version2.prototype.summary.temporal.TemporalSummaryCalculator;
import version2.prototype.summary.zonal.ZonalSummaryCalculator;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.FileSystem;

public class SummaryWorker extends ProcessWorker<Void> {
    private ProjectInfoFile projectInfoFile;
    private ProjectInfoPlugin pluginInfo;
    private PluginMetaData pluginMetaData;
    private ArrayList<DataFileMetaData> cachedFiles;

    protected SummaryWorker(Process<?> process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles)
    {
        super("SummaryWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles);
        this.projectInfoFile = projectInfoFile;
        this.pluginInfo = pluginInfo;
        this.pluginMetaData = pluginMetaData;
        this.cachedFiles = cachedFiles;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    // TODO: Need to fix this to run on a specified plugin. Fix after adding database cache information.
    public Void call() throws Exception {
        ArrayList<DataFileMetaData> tempFiles = new ArrayList<DataFileMetaData>(0);

        for(ProjectInfoSummary summary: projectInfoFile.GetSummaries())
        {
            // Check if doing temporal summarization
            if(summary.GetTemporalFileStore() != null)
            {
                for(DataFileMetaData cachedFile : cachedFiles)
                {
                    TemporalSummaryCalculator temporalSummaryCal = new TemporalSummaryCalculator(
                            projectInfoFile.GetWorkingDir(),
                            projectInfoFile.GetProjectName(),   // projectName
                            pluginInfo.GetName(),   // pluginName
                            new File(cachedFile.fullPath),     // inRasterFile
                            null,   // inDataDate
                            0,      // daysPerInputData
                            0,      // daysPerOutputData
                            summary.GetTemporalFileStore(),   // TemporalSummaryRasterFileStore
                            null,   // InterpolateStrategy
                            new AvgGdalRasterFileMerge() // (Framework user defined)
                            );
                    tempFiles.add(temporalSummaryCal.calculate());
                }
            }

        }
        cachedFiles = tempFiles;

        File outputFile;
        for(DataFileMetaData cachedFile : cachedFiles)
        {
            for(ProjectInfoSummary summary: projectInfoFile.GetSummaries())
            {
                outputFile = new File(FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName(),
                        pluginInfo.GetName(), ProcessName.SUMMARY) + String.format("%04d%03d.tif", cachedFile.year, cachedFile.day));
                ZonalSummaryCalculator zonalSummaryCal = new ZonalSummaryCalculator(
                        projectInfoFile.GetWorkingDir(),
                        projectInfoFile.GetProjectName(),   // projectName
                        pluginInfo.GetName(),   // pluginName
                        new File(cachedFile.fullPath),   // inRasterFile
                        new File(summary.GetZonalSummary().GetShapeFile()),  // inShapeFile
                        outputFile,   // outTableFile
                        summary.GetZonalSummary().GetField(),    // zoneField
                        new SummariesCollection(new ArrayList<String>(Arrays.asList("Count", "Sum", "Mean", "StdDev")))); // summariesCollection
                zonalSummaryCal.calculate();
            }
        }
        return null;
    }

}
