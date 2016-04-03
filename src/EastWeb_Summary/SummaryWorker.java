package EastWeb_Summary;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_Database.DatabaseConnection;
import EastWeb_Database.DatabaseConnector;
import EastWeb_ErrorHandling.ErrorLog;
import EastWeb_ProcessWorker.ProcessWorker;
import EastWeb_ProcessWorker.ProcessWorkerReturn;
import EastWeb_Scheduler.ProcessName;
import EastWeb_Summary.Temporal.MergeStrategy;
import EastWeb_Summary.Temporal.TemporalSummaryCalculator;
import EastWeb_Summary.Temporal.TemporalSummaryRasterFileStore;
import EastWeb_Summary.Zonal.SummariesCollection;
import EastWeb_Summary.Zonal.ZonalSummaryCalculator;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;
import ProjectInfoMetaData.ProjectInfoSummary;
import Utilies.DataFileMetaData;
import Utilies.FileSystem;
import Utilies.IndicesFileMetaData;
import EastWeb_ProcessWorker.Process;

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

    @Override
    public ProcessWorkerReturn process() {
        DatabaseConnection con = DatabaseConnector.getConnection(configInstance);
        if(con == null) {
            return null;
        }
        Map<Integer, ArrayList<DataFileMetaData>> summaryInputMap = new HashMap<Integer, ArrayList<DataFileMetaData>>();
        Map<Integer, ArrayList<DataFileMetaData>> tempFilesMap = new HashMap<Integer, ArrayList<DataFileMetaData>>();
        ArrayList<DataFileMetaData> outputFiles = new ArrayList<DataFileMetaData>(1);
        ArrayList<DataFileMetaData> tempFiles;
        DataFileMetaData tempFile;
        IndicesFileMetaData cachedFileData = null;

        Class<?> strategyClass;
        Constructor<?> ctorStrategy;
        MergeStrategy mergeStrategy = null;
        try {
            strategyClass = Class.forName("version2.prototype.summary.temporal.MergeStrategies." + pluginMetaData.Summary.mergeStrategyClass);
            ctorStrategy = strategyClass.getConstructor();
            mergeStrategy = (MergeStrategy)ctorStrategy.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            ErrorLog.add(process,  "Problem creating MergeStrategy.", e);
        }

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
                        if(Thread.currentThread().isInterrupted()) {
                            return null;
                        }

                        cachedFileData = cachedFile.ReadMetaDataForSummary();
                        TemporalSummaryCalculator temporalSummaryCal = new TemporalSummaryCalculator(
                                configInstance,                         // configInstance
                                con,                                    // DatabaseConnection con
                                process,                                // process
                                projectInfoFile,                        // projectInfoFile
                                pluginInfo.GetName(),                   // pluginName
                                cachedFileData,                         // inputFile
                                pluginMetaData.DaysPerInputData,        // daysPerInputData
                                fileStores.get(summary.GetID()),        // TemporalSummaryRasterFileStore
                                null,                                   // InterpolateStrategy
                                mergeStrategy,
                                summary
                                );
                        tempFile = temporalSummaryCal.calculate();
                        if(tempFile != null) {
                            tempFiles.add(tempFile);
                        }
                    }
                    summaryInputMap.put(summary.GetID(), tempFiles);
                    tempFilesMap.put(summary.GetID(), tempFiles);
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
                if(Thread.currentThread().isInterrupted()) {
                    con.close();
                    return null;
                }

                cachedFileData = cachedFile.ReadMetaDataForSummary();
                try{
                    outputFile = new File(FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName(),
                            pluginInfo.GetName(), ProcessName.SUMMARY) + String.format("%s/%s/%04d/%03d.csv", cachedFileData.indexNm, "Summary " + summary.GetID(), cachedFileData.year, cachedFileData.day));
                    ZonalSummaryCalculator zonalSummaryCal = new ZonalSummaryCalculator(
                            con,
                            process,
                            configInstance.getGlobalSchema(),
                            projectInfoFile.GetWorkingDir(),
                            projectInfoFile.GetProjectName(),       // projectName
                            pluginInfo.GetName(),                   // pluginName
                            pluginMetaData.DaysPerInputData,        // daysPerInputData
                            cachedFileData,                         // inputFile
                            outputFile,                             // outTableFile
                            new SummariesCollection(Config.getInstance().getSummaryCalculations()),     // summariesCollection
                            summary,
                            pluginMetaData.NoDataValue,
                            fileStores.get(summary.GetID()),        // fileStore
                            outputCache);
                    zonalSummaryCal.calculate();

                    if(process.GetClearIntermediateFilesFlag())
                    {
                        String newFilePath = FileSystem.GetProcessWorkerTempDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName(), pluginInfo.GetName(), ProcessName.SUMMARY) +
                                String.format("%04d%03d.tif",
                                        cachedFileData.year,
                                        cachedFileData.day
                                        );
                        File intermediateFile = new File(newFilePath);
                        if(intermediateFile.exists()) {
                            intermediateFile.delete();
                        }
                    }

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

        if(process.GetClearIntermediateFilesFlag() && tempFilesMap.size() > 0)
        {
            Iterator<Integer> tempIt = tempFilesMap.keySet().iterator();
            Integer key;
            ArrayList<DataFileMetaData> intermediateFiles;
            File dFile;
            while(tempIt.hasNext())
            {
                key = tempIt.next();
                intermediateFiles = tempFilesMap.get(key);
                for(DataFileMetaData dfmd : intermediateFiles)
                {
                    dFile = new File(dfmd.ReadMetaDataForSummary().dataFilePath);
                    if(dFile.exists()) {
                        dFile.delete();
                    }
                }
            }
        }

        con.close();
        return new ProcessWorkerReturn(outputFiles);
    }

}
