package version2.prototype.summary;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.ProcessWorkerReturn;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.summary.temporal.TemporalSummaryCalculator;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
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
    private final Config configInstance;
    private final Map<Integer, TemporalSummaryRasterFileStore> fileStores;

    /**
     * @param configInstance
     * @param process
     * @param projectInfoFile
     * @param pluginInfo
     * @param pluginMetaData
     * @param cachedFiles
     * @param outputCache
     * @param fileStores
     */
    public SummaryWorker(Config configInstance, Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles,
            DatabaseCache outputCache, Map<Integer, TemporalSummaryRasterFileStore> fileStores)
    {
        super(configInstance, "SummaryWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);
        this.configInstance = configInstance;
        this.fileStores = fileStores;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ProcessWorkerReturn call() throws Exception {
        Map<Integer, ArrayList<DataFileMetaData>> summaryInputMap = new HashMap<Integer, ArrayList<DataFileMetaData>>();
        ArrayList<DataFileMetaData> outputFiles = new ArrayList<DataFileMetaData>(1);
        ArrayList<DataFileMetaData> tempFiles;
        DataFileMetaData tempFile;
        IndicesFileMetaData cachedFileData = null;

        // Setup initial input mappings
        for(ProjectInfoSummary summary : projectInfoFile.GetSummaries())
        {
            summaryInputMap.put(summary.GetID(), cachedFiles);
        }

        for(ProjectInfoSummary summary: projectInfoFile.GetSummaries())
        {
            try{
                // Check if doing temporal summarization
                if(fileStores.get(summary.GetID()) != null && fileStores.get(summary.GetID()).compStrategy.maxNumberOfDaysInComposite() != pluginMetaData.DaysPerInputData)
                {
                    tempFiles = new ArrayList<DataFileMetaData>();
                    for(DataFileMetaData cachedFile : summaryInputMap.get(summary.GetID()))
                    {
                        cachedFileData = cachedFile.ReadMetaDataForSummary();
                        TemporalSummaryCalculator temporalSummaryCal = new TemporalSummaryCalculator(
                                configInstance,                         // configInstance
                                process,                                // process
                                projectInfoFile,                        // projectInfoFile
                                pluginInfo.GetName(),                   // pluginName
                                cachedFileData,                         // inputFile
                                pluginMetaData.DaysPerInputData,        // daysPerInputData
                                fileStores.get(summary.GetID()),        // TemporalSummaryRasterFileStore
                                null,                                   // InterpolateStrategy
                                new AvgGdalRasterFileMerge()            // (Framework user defined)
                                );
                        tempFile = temporalSummaryCal.calculate();
                        if(tempFile != null) {
                            tempFiles.add(tempFile);
                        }
                    }
                    summaryInputMap.put(summary.GetID(), tempFiles);
                }
            }catch(Exception e) {
                if(cachedFileData != null) {
                    ErrorLog.add(process, "Problem during temporal summary calculation for summary " + summary.toString() + ", date {day of year=" + cachedFileData.day + ", year=" + cachedFileData.year + "}.", e);
                } else {
                    ErrorLog.add(process, "Problem during temporal summary calculation for summary " + summary.toString() + ".", e);
                }
            }
        }

        File outputFile;
        for(ProjectInfoSummary summary: projectInfoFile.GetSummaries())
        {
            for(DataFileMetaData cachedFile : summaryInputMap.get(summary.GetID()))
            {
                cachedFileData = cachedFile.ReadMetaDataForSummary();
                try{
                    outputFile = new File(FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName(),
                            pluginInfo.GetName(), ProcessName.SUMMARY) + String.format("%s/%s/%04d/%03d.csv", cachedFileData.indexNm, "Summary " + summary.GetID(), cachedFileData.year, cachedFileData.day));
                    ZonalSummaryCalculator zonalSummaryCal = new ZonalSummaryCalculator(
                            process,
                            configInstance.getGlobalSchema(),
                            projectInfoFile.GetWorkingDir(),
                            projectInfoFile.GetProjectName(),       // projectName
                            pluginInfo.GetName(),                   // pluginName
                            pluginMetaData.DaysPerInputData,        // daysPerInputData
                            cachedFileData,                         // inputFile
                            outputFile,                             // outTableFile
                            new SummariesCollection(Config.getInstance().getSummaryCalculations()),
                            summary,                                // summariesCollection
                            fileStores.get(summary.GetID()),        // fileStore
                            outputCache);
                    zonalSummaryCal.calculate();
                    outputFiles.add(new DataFileMetaData(outputFile.getCanonicalPath(), cachedFileData.dateGroupID, cachedFileData.year, cachedFileData.day, cachedFileData.indexNm));
                }catch(Exception e) {
                    if(cachedFileData != null) {
                        ErrorLog.add(process, "Problem during zonal summary calculation for summary " + summary.toString() + ", date {day of year=" + cachedFileData.day + ", year=" + cachedFileData.year + "}.", e);
                    } else {
                        ErrorLog.add(process, "Problem during zonal summary calculation for summary " + summary.toString() + ".", e);
                    }
                }
            }
        }
        return new ProcessWorkerReturn(outputFiles);
    }

}
