package EastWeb_Summary;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_ErrorHandling.ErrorLog;
import EastWeb_Scheduler.ProcessName;
import EastWeb_Scheduler.Scheduler;
import EastWeb_Summary.Temporal.TemporalSummaryCompositionStrategy;
import EastWeb_Summary.Temporal.TemporalSummaryRasterFileStore;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;
import ProjectInfoMetaData.ProjectInfoSummary;
import Utilies.DataFileMetaData;
import EastWeb_ProcessWorker.Process;


/**
 * The custom Summary framework, Process extending class. Manages SummaryWorker objects.
 *
 * @author michael.devos
 *
 */
public class Summary extends Process {
    private Map<Integer, TemporalSummaryRasterFileStore> fileStores;

    /**
     * Creates a Summary object with the defined initial TaskState, owned by the given Scheduler, and acquiring its input from the specified
     * process, inputProcessName.
     * @param configInstance  - Config reference to use and pass on
     * @param projectMetaData  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param scheduler  - reference to the controlling Scheduler object
     * @param inputCache
     * @param outputCache
     */
    public Summary(Config configInstance, ProjectInfoFile projectMetaData, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache inputCache, DatabaseCache outputCache)
    {
        super(configInstance, ProcessName.SUMMARY, projectMetaData, pluginInfo, pluginMetaData, scheduler, outputCache);
        inputCache.addObserver(this);

        fileStores = new HashMap<Integer, TemporalSummaryRasterFileStore>();
        String temporalSummaryCompositionStrategyClassName;
        TemporalSummaryRasterFileStore fileStore;
        Class<?> strategyClass;
        Constructor<?> ctorStrategy = null;
        for(ProjectInfoSummary summary : projectMetaData.GetSummaries())
        {
            fileStore = null;
            temporalSummaryCompositionStrategyClassName = summary.GetTemporalSummaryCompositionStrategyClassName();
            if(temporalSummaryCompositionStrategyClassName != null && !temporalSummaryCompositionStrategyClassName.isEmpty())
            {
                try {
                    strategyClass = Class.forName("version2.prototype.summary.temporal.CompositionStrategies." + temporalSummaryCompositionStrategyClassName);
                    ctorStrategy = strategyClass.getConstructor();
                    fileStore = new TemporalSummaryRasterFileStore((TemporalSummaryCompositionStrategy)ctorStrategy.newInstance());
                } catch(ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    ErrorLog.add(processName, scheduler, "Problem instantiating temporal filestore.", e);
                }
            }
            fileStores.put(summary.GetID(), fileStore);
        }
    }

    @Override
    public void process(ArrayList<DataFileMetaData> cachedFiles) {
        scheduler.StartNewProcessWorker(new SummaryWorker(configInstance, this, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache, fileStores));
    }

}