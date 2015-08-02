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
    private ArrayList<Class<?>> processWorkerClasses;

    /**
     * Creates a GenericFrameworkProcess object with the defined initial TaskState, owned by the given Scheduler, labeled by the given processName,
     * and acquiring its input from the specified process (inputProcessName).
     *
     * @param manager  - EASTWebManager reference
     * @param processName  - name of this threaded process
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param scheduler  - reference to the controlling Scheduler object
     * @param inputCache  - DatabaseCache to listen on for available file to process
     * @param outputCache  - DatabaseCache to use when caching output files from the process
     * @param classNames  - worker class names to spawn for each new input file. Expects the class to exist within one of the following packages:
     * 1. version2.prototype.download
     * 2. version2.prototype.indices
     * 3. version2.prototype.processor
     * 4. version2.prototype.summary
     * @throws ClassNotFoundException
     */
    public GenericProcess(EASTWebI manager, ProcessName processName, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, Scheduler scheduler,
            DatabaseCache inputCache, DatabaseCache outputCache, String... classNames) throws ClassNotFoundException
    {
        super(manager, processName, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);

        if(inputCache != null) {
            inputCache.addObserver(this);
        }

        String packageOfWorker = "";
        switch(processName)
        {
        case DOWNLOAD: packageOfWorker = "version2.prototype.download."; break;
        case INDICES: packageOfWorker = "version2.prototype.indices."; break;
        case PROCESSOR: packageOfWorker = "version2.prototype.processor."; break;
        case SUMMARY: packageOfWorker = "version2.prototype.summary."; break;
        }

        processWorkerClasses = new ArrayList<Class<?>>(1);
        for(String className : classNames)
        {
            processWorkerClasses.add(Class.forName(packageOfWorker + className));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(ArrayList<DataFileMetaData> cachedFiles) {
        Constructor<?> cstr;
        try {
            for(Class<?> cl : processWorkerClasses)
            {
                cstr = cl.getConstructor(Process.class, ProjectInfoFile.class, ProjectInfoPlugin.class, PluginMetaData.class, ArrayList.class);
                manager.StartNewProcessWorker((WorkerType) cstr.newInstance(this, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache));
            }
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
        //        scheduler.NotifyUI(new GeneralUIEventObject(this, name + " Finished", 100, pluginInfo.GetName()));
    }
}
