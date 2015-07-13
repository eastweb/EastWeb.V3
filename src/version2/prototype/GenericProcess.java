package version2.prototype;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GeneralUIEventObject;

/**
 * A generic concrete Process extending class. Can be used in place of a custom built framework Process class to handle managing worker threads for the
 * framework.
 *
 * @author michael.devos
 *
 * @param <WorkerType>  - the ProcessWorker extending class the spawned worker threads should be created as
 */
public class GenericProcess<WorkerType extends ProcessWorker> extends Process {
    private Class<?> processWorkerClass;

    /**
     * Creates a GenericFrameworkProcess object with the defined initial TaskState, owned by the given Scheduler, labeled by the given processName,
     * and acquiring its input from the specified process (inputProcessName).
     *
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param scheduler  - reference to the controlling Scheduler object
     * @param state  - TaskState to initialize this object to
     * @param processName  - name of this threaded process
     * @param inputProcessName  - name of process to use the output of for its input
     * @param executor  - executor service to use to spawn worker threads
     * @throws ClassNotFoundException
     */
    public GenericProcess(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            ProcessName processName, DatabaseCache inputCache, DatabaseCache outputCache) throws ClassNotFoundException
    {
        super(projectInfoFile, pluginInfo, pluginMetaData, scheduler, processName, outputCache);

        if(inputCache != null) {
            inputCache.addObserver(this);
        }

        switch(processName)
        {
        case DOWNLOAD: processWorkerClass = Class.forName("version2.prototype.download.DownloadWorker"); break;
        case INDICES: processWorkerClass = Class.forName("version2.prototype.indices.IndicesWorker"); break;
        case PROCESSOR: processWorkerClass = Class.forName("version2.prototype.processor.ProcessorWorker"); break;
        case SUMMARY: processWorkerClass = Class.forName("version2.prototype.summary.SummaryWorker"); break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(ArrayList<DataFileMetaData> cachedFiles) {
        Constructor<?> cstr;
        try {
            cstr = processWorkerClass.getConstructor(Process.class, ProjectInfoFile.class, ProjectInfoPlugin.class, PluginMetaData.class, ArrayList.class);
            EASTWebManager.StartNewProcessWorker((WorkerType) cstr.newInstance(this, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache));
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // TODO: Need to define when "finished" state has been reached as this doesn't work with asynchronous.
        String name = null;
        switch(processName)
        {
        case DOWNLOAD: name = "Download"; break;
        case INDICES: name = "Indices"; break;
        case PROCESSOR: name = "Processor"; break;
        case SUMMARY: name = "Summary"; break;
        }
        scheduler.NotifyUI(new GeneralUIEventObject(this, name + " Finished", 100, pluginInfo.GetName()));
    }
}
