package version2.prototype.summary;

import java.io.File;
import java.util.ArrayList;

import version2.prototype.Config;
import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.ProcessWorkerReturn;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.summary.temporal.TemporalSummaryCalculator;
import version2.prototype.summary.temporal.MergeStrategies.AvgGdalRasterFileMerge;
import version2.prototype.summary.zonal.SummariesCollection;
import version2.prototype.summary.zonal.ZonalSummaryCalculator;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.FileSystem;
import version2.prototype.util.IndicesFileMetaData;

/**
 * @author michael.devos
 *
 */
public class SummaryWorker extends ProcessWorker {

    public SummaryWorker(Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles)
    {
        super("SummaryWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, null);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ProcessWorkerReturn call() throws Exception {
        ArrayList<DataFileMetaData> outputFiles = new ArrayList<DataFileMetaData>(1);
        ArrayList<DataFileMetaData> tempFiles = new ArrayList<DataFileMetaData>(0);
        IndicesFileMetaData cachedFileData;

        for(ProjectInfoSummary summary: projectInfoFile.GetSummaries())
        {
            // Check if doing temporal summarization
            if(summary.GetTemporalFileStore() != null)
            {
                for(DataFileMetaData cachedFile : cachedFiles)
                {
                    cachedFileData = cachedFile.ReadMetaDataForSummary();
                    TemporalSummaryCalculator temporalSummaryCal = new TemporalSummaryCalculator(
                            projectInfoFile.GetWorkingDir(),
                            projectInfoFile.GetProjectName(),   // projectName
                            pluginInfo.GetName(),   // pluginName
                            new File(cachedFileData.dataFilePath),     // inRasterFile
                            null,   // inDataDate
                            pluginMetaData.DaysPerInputData,      // daysPerInputData
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
            cachedFileData = cachedFile.ReadMetaDataForSummary();
            for(ProjectInfoSummary summary: projectInfoFile.GetSummaries())
            {
                outputFile = new File(FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName(),
                        pluginInfo.GetName(), ProcessName.SUMMARY) + String.format("%s/%04d/%03d.csv", cachedFileData.indexNm, cachedFileData.year, cachedFileData.day));
                ZonalSummaryCalculator zonalSummaryCal = new ZonalSummaryCalculator(
                        projectInfoFile.GetWorkingDir(),
                        projectInfoFile.GetProjectName(),   // projectName
                        pluginInfo.GetName(),   // pluginName
                        cachedFileData.indexNm,
                        cachedFileData.year,
                        cachedFileData.day,
                        new File(cachedFileData.dataFilePath),   // inRasterFile
                        new File(summary.GetZonalSummary().GetShapeFile()),  // inShapeFile
                        outputFile,   // outTableFile
                        summary.GetZonalSummary().GetAreaNameField(),    // zoneField
                        new SummariesCollection(Config.getInstance().getSummaryCalculations())); // summariesCollection
                zonalSummaryCal.calculate();
                outputFiles.add(new DataFileMetaData("Data", outputFile.getCanonicalPath(), cachedFileData.year, cachedFileData.day, cachedFileData.indexNm));
            }
        }
        return new ProcessWorkerReturn(outputFiles);
    }

}
