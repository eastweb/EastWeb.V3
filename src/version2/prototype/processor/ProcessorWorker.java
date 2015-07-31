package version2.prototype.processor;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import version2.prototype.DataDate;
import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.ProcessWorkerReturn;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.ProcessorMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.DownloadFileMetaData;
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
    public ProcessorWorker(Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles, DatabaseCache outputCache)
    {
        super("ProcessorWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);
        // TODO Auto-generated constructor stub

    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ProcessWorkerReturn call() throws Exception
    {
        String pluginName = pluginMetaData.Title;
        String outputFolder  =
                FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(),
                        projectInfoFile.GetProjectName(), pluginName, ProcessName.PROCESSOR) ;

        ProcessorMetaData pMetaData = pluginMetaData.Processor;
        Map<Integer, String> processStep = pMetaData.processStep;

        // get each plugin's PrepareProcessTask
        Class <?> classPrepareTask =
                Class.forName("version2.prototype.processor." + pluginName + "." + pluginName +"PrepareProcessTask" );

        Constructor<?> cnstPrepareTask =
                classPrepareTask.getConstructor(ProjectInfoFile.class, ProjectInfoPlugin.class,
                        PluginMetaData.class, DataDate.class);

        // Use a map to group CachedFiles based on the dates
        HashMap<DataDate, ArrayList<DownloadFileMetaData>> map =
                new HashMap<DataDate, ArrayList<DownloadFileMetaData>>();

        // extract cachedFiles for download folder
        for (DataFileMetaData dmd : cachedFiles)
        {
            // read each cachedFile
            DownloadFileMetaData downloaded = dmd.ReadMetaDataForProcessor();

            // get the date of the downloaded file
            DataDate thisDate = new DataDate(downloaded.day, downloaded.year);

            // add the downloaded file into the downloaded ArrayList
            ArrayList<DownloadFileMetaData> files = map.get(thisDate);
            if (files == null)
            {
                files = new ArrayList<DownloadFileMetaData>();
            }
            // add the downloaded file into the arraylist associated with this date
            files.add(downloaded);
            // modify the map
            map.put(thisDate, files);
        }

        // for each date, process the steps associated with the plugin
        for (Map.Entry<DataDate, ArrayList<DownloadFileMetaData>> entry : map.entrySet())
        {
            DataDate thisDay = entry.getKey();

            //create necessary folders for the date
            PrepareProcessTask prepareTask =
                    (PrepareProcessTask) cnstPrepareTask.newInstance(projectInfoFile, pluginInfo, pluginMetaData, thisDay);

            String laststepOutputFolder = null;

            // process the files for that date
            for (Entry<Integer, String> step : processStep.entrySet())
            {
                Integer key = step.getKey();

                Class<?> classProcess = Class.forName("version2.prototype.processor." + pluginName + "." + step.getValue());

                Constructor<?> cnstProcess = classProcess.getConstructor(ProcessData.class);

                Object process =  cnstProcess.newInstance(new ProcessData(
                        prepareTask.getInputFolders(key),
                        prepareTask.getOutputFolder(key),
                        prepareTask.getDataDate(),
                        prepareTask.getQC(),
                        prepareTask.getShapeFile(),
                        prepareTask.getMaskFile(),
                        prepareTask.getDataBands(),
                        prepareTask.getQCBands(),
                        prepareTask.getProjection(),
                        prepareTask.getMaskResolution(),
                        prepareTask.getDataResolution(),
                        prepareTask.getClipOrNot(),
                        prepareTask.getFreezingDate(),
                        prepareTask.getHeatingDate(),
                        prepareTask.getFreezingDegree(),
                        prepareTask.getHeatingDegree()
                        )
                        );

                //copy the downloaded files to the input folders
                String [] inputFolders = prepareTask.getInputFolders(key);

                File dataInputFolder = new File(inputFolders[0]);
                File qcInputFolder = null;
                if (inputFolders.length > 1)    // it has a qc folder
                {
                    qcInputFolder =  new File(inputFolders[1]);
                }

                for (DownloadFileMetaData dFile : entry.getValue())
                {
                    FileUtils.copyFileToDirectory(new File(dFile.dataFilePath), dataInputFolder);
                    if (qcInputFolder != null)
                    {
                        // assume QC is the first element in the extraDownloads
                        FileUtils.copyFileToDirectory(
                                new File(dFile.extraDownloads.get(0).dataFilePath),
                                qcInputFolder);
                    }
                }

                laststepOutputFolder = prepareTask.getOutputFolder(key);

                Method methodProcess = process.getClass().getMethod("run");
                methodProcess.invoke(process);
            }

            // check if the laststepOutputFolder is the  final outputFolder for the processor
            String outputPath = String.format("%s"+ File.separator + "%04d" + File.separator+"%03d",
                    outputFolder, thisDay.getYear(), thisDay.getDayOfYear());

            // if not match, copy the files from the last step to the final outputfolder for  processor
            if (!outputPath.equals(laststepOutputFolder))
            {
                File outputDir = new File(outputPath);
                if(!(outputDir.exists()))
                {
                    FileUtils.forceMkdir(outputDir);
                }

                // copy the output files to the system output directory
                if (laststepOutputFolder != null)
                {
                    for (File f: new File(laststepOutputFolder).listFiles()) {
                        FileUtils.copyFileToDirectory(f, outputDir);
                    }
                }
            }

            // remove the entire temp folder
            // find "temp" in the laststepOutputFolder
            if (laststepOutputFolder != null)
            {
                String tempFolder = laststepOutputFolder.substring(0, laststepOutputFolder.lastIndexOf("Temp"))+"Temp";
                FileUtils.deleteDirectory(new File(tempFolder));
            }


            // compile the output files in the outputPath to an arraylist of DataFileMetaData and save to the database
            ArrayList<DataFileMetaData> toCache = new ArrayList<DataFileMetaData>();

            // get the list of files in the output folder
            File dir = new File(outputPath);
            File[] dirList = dir.listFiles();

            // add each file into DataFileMetaData
            for (File oFile : dirList) {
                toCache.add(new DataFileMetaData("data", oFile.getAbsolutePath(), thisDay.getYear(), thisDay.getDayOfYear()));
            }

            // cache to the database
            outputCache.CacheFiles(toCache);
        }

        return null;
    }

}
