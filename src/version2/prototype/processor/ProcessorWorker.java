package version2.prototype.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import version2.prototype.DataDate;
import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.ProcessWorkerReturn;
import version2.prototype.Projection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.ProcessorMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.summary.zonal.ZonalSummaryCalculator;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.FileSystem;


/**
 * An implementation of a ProcessWorker to handle the work for the Processor framework and to be used by a Process extending class.
 *
 * @author michael.devos
 * @author Yi Liu
 *
 */
public class ProcessorWorker extends ProcessWorker {

    /**
     * An implementation of ProcessWorker that handles the major processing of a list of raster files after being downloaded and handled by the Download framework.
     * Output is used by the Indices framework. Meant to be ran on its own thread.
     *
     * @param process  - the parent Process object to this threaded worker.
     * @param projectInfoFile  - information about the project gotten from the project's info xml.
     * @param pluginInfo  - information about the plugin being used for the acquired data files.
     * @param pluginMetaData  - information relevant to this ProcessWorker about the plugin being used gotten from the plugin's info xml.
     * @param cachedFiles  - the list of files to process in this ProcessWorker.
     */
    protected ProcessorWorker(Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles)
    {
        super("ProcessorWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ProcessWorkerReturn call() throws Exception
    {
        String pluginName = pluginMetaData.Title;
        String outputFiles  =
                FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(),
                        projectInfoFile.GetProjectName(), pluginName, ProcessName.PROCESSOR) ;

        ProcessorMetaData pMetaData = pluginMetaData.Processor;
        Map<Integer, String> processStep = pMetaData.processStep;

        // get each plugin's PrepareProcessTask
        Class <?> classPrepareTask =
                Class.forName("version2.prototype.processor." + pluginName + "PrepareProcessTask" );

        Constructor<?> cnstPrepareTask =
                classPrepareTask.getConstructor(ProjectInfoFile.class, ProjectInfoPlugin.class, DataDate.class);

        PrepareProcessTask prepareTask = (PrepareProcessTask) cnstPrepareTask.newInstance(projectInfoFile, pluginInfo, projectInfoFile.GetStartDate());

        for (Entry<Integer, String> step : processStep.entrySet())
        {
            Integer key = step.getKey();

            Class<?> classProcess = Class.forName("version2.prototype.projection."
                    + pluginName + step.getValue());

            Constructor<?> cnstProcess = classProcess.getConstructor(ProcessData.class);

            Object process =  cnstProcess.newInstance(new Object[] {new ProcessData(
                    prepareTask.getInputFolders(key),
                    prepareTask.getOutputFolder(key),
                    prepareTask.getQC(),
                    prepareTask.getShapeFile(),
                    prepareTask.getMaskFile(),
                    prepareTask.getDataBands(),
                    prepareTask.getQCBands(),
                    prepareTask.getProjection())});
            Method methodProcess = process.getClass().getMethod("run");
            methodProcess.invoke(process);
        }

        return new ProcessWorkerReturn(outputFiles);
    }

}
