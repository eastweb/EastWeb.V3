package version2.prototype.summary;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import version2.prototype.DataDate;
import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.ThreadState;
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

/**
 * An implementation of a ProcessWorker to handle the work for the Summary framework and to be used by a Process extending class.
 *
 * @author michael.devos
 *
 */
public class SummaryWorker extends ProcessWorker<Void> {
    private TemporalSummaryRasterFileStore fileStore;

    /**
     * An implementation of ProcessWorker that handles the summarization of a list of raster files after being handled by the Indices and Processor
     * frameworks. Meant to be ran on its own thread.
     *
     * @param process  - the parent Process object to this threaded worker.
     * @param projectInfoFile  - information about the project gotten from the project's info xml.
     * @param pluginInfo  - information about the plugin being used for the acquired data files.
     * @param pluginMetaData  - information relevant to this ProcessWorker about the plugin being used gotten from the plugin's info xml.
     * @param fileStore  - shared memory between SummaryWorkers used to collect data files and create compositions of them when needed for temporal
     * summarization.
     * @param cachedFiles  - the list of files to process in this ProcessWorker.
     */
    public SummaryWorker(Process<?> process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            TemporalSummaryRasterFileStore fileStore, ArrayList<DataFileMetaData> cachedFiles)
    {
        super("SummaryWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles);
        this.fileStore = fileStore;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Void call() throws Exception {
        if(pluginMetaData.Summary.IsTemporalSummary)
        {
            ArrayList<DataFileMetaData> tempFiles = new ArrayList<DataFileMetaData>(0);

            for(DataFileMetaData cachedFile : cachedFiles)
            {
                if(mState == ThreadState.RUNNING)
                {
                    TemporalSummaryCalculator temporalSummaryCal = new TemporalSummaryCalculator(
                            projectInfoFile.GetProjectName(),
                            projectInfoFile.GetWorkingDir(),
                            pluginInfo.GetName(),
                            new File(cachedFile.dataFilePath),
                            new DataDate(cachedFile.day, cachedFile.year),
                            pluginMetaData.Summary.DaysPerInputData,
                            pluginMetaData.Summary.DaysPerOutputData,
                            null,
                            new AvgGdalRasterFileMerge(),
                            fileStore);
                    tempFiles.add(temporalSummaryCal.calculate());
                }
            }
            cachedFiles = tempFiles;
        }

        File outputFile;
        for(DataFileMetaData cachedFile : cachedFiles)
        {
            if(mState == ThreadState.RUNNING)
            {
                for(ZonalSummary zone: projectInfoFile.GetZonalSummaries())
                {
                    outputFile = new File(FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName(),
                            pluginInfo.GetName(), ProcessName.SUMMARY) + String.format("%04d%03d.tif", cachedFile.year, cachedFile.day));
                    ZonalSummaryCalculator zonalSummaryCal = new ZonalSummaryCalculator(
                            projectInfoFile.GetProjectName(),
                            new File(cachedFile.dataFilePath),
                            new File(zone.GetShapeFile()),
                            outputFile,
                            zone.GetField(),
                            new SummariesCollection(new ArrayList<String>(Arrays.asList("Count", "Sum", "Mean", "StdDev"))));
                    zonalSummaryCal.calculate();
                }
            }
        }
        return null;
    }
}
