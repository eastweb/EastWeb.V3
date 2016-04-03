package EastWeb_ProcessWorker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_ErrorHandling.ErrorLog;
import EastWeb_Scheduler.ProcessName;
import EastWeb_Scheduler.Scheduler;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;
import Utilies.DataFileMetaData;

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
     * @param configInstance  - Config reference to use and pass on
     * @param processName  - name of this threaded process
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param scheduler  - reference to the controlling Scheduler object
     * @param inputCache  - DatabaseCache to listen on for available file to process
     * @param outputCache  - DatabaseCache to use when caching output files from the process
     * @param classPathNames  - fully qualified worker class names to spawn for each new input file.
     * @throws ClassNotFoundException
     */
    public GenericProcess(Config configInstance, ProcessName processName, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, Scheduler scheduler, DatabaseCache inputCache, DatabaseCache outputCache, String... classPathNames)
                    throws ClassNotFoundException
    {
        super(configInstance, processName, projectInfoFile, pluginInfo, pluginMetaData, scheduler, outputCache);

        if(inputCache != null) {
            inputCache.addObserver(this);
        }

        processWorkerClasses = new ArrayList<Class<?>>(1);
        Class<?> tempClass;
        for(String classPathName : classPathNames)
        {
            tempClass = Class.forName(classPathName);
            processWorkerClasses.add(tempClass);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(ArrayList<DataFileMetaData> cachedFiles) {
        Constructor<?> cstr;
        Class<?> cl = null;
        try {
            for(int i=0; i < processWorkerClasses.size(); i++)
            {
                cl = processWorkerClasses.get(i);
                cstr = cl.getConstructor(Config.class, Process.class, ProjectInfoFile.class,
                        ProjectInfoPlugin.class, PluginMetaData.class, ArrayList.class, DatabaseCache.class);
                scheduler.StartNewProcessWorker((WorkerType) cstr.newInstance(configInstance, this, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache));
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            ErrorLog.add(processName, scheduler, "GenericProcess.process error while starting new ProcessWorker '" + cl + "'.", e);
        }
    }
}