package version2.prototype.indices;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import version2.prototype.DataDate;
import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.ProcessWorkerReturn;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.IndexMetaData;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.processor.ProcessData;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.FileSystem;
import version2.prototype.util.ProcessorFileMetaData;


/**
 * An implementation of a ProcessWorker to handle the work for the Indices framework and to be used by a Process extending class.
 *
 * @author michael.devos
 * @author Yi Liu
 *
 */
public class IndicesWorker extends ProcessWorker{

    /**
     * An implementation of ProcessWorker that handles the indexing of a list of raster files after being handled by the Processor framework. Output used by the
     * Summary framework. Meant to be ran on its own thread.
     *
     * @param process  - the parent Process object to this threaded worker.
     * @param projectInfoFile  - information about the project gotten from the project's info xml.
     * @param pluginInfo  - information about the plugin being used for the acquired data files.
     * @param pluginMetaData  - information relevant to this ProcessWorker about the plugin being used gotten from the plugin's info xml.
     * @param cachedFiles  - the list of files to process in this ProcessWorker.
     */
    public IndicesWorker(Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles, DatabaseCache outputCache)
    {
        super("IndicesWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ProcessWorkerReturn call() throws Exception {
        String pluginName = pluginMetaData.Title;
        String outputFolder  =
                FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(),
                        projectInfoFile.GetProjectName(), pluginName, ProcessName.INDICES) ;

        // Use a map to group CachedFiles based on the dates
        HashMap<DataDate, ArrayList<ProcessorFileMetaData>> map =
                new HashMap<DataDate, ArrayList<ProcessorFileMetaData>>();

        // extract cachedFiles for indices inputs
        for (DataFileMetaData dmd : cachedFiles)
        {
            // read each cachedFile
            ProcessorFileMetaData input = dmd.ReadMetaDataForIndices();

            // get the date of the downloaded file
            DataDate thisDate = new DataDate(input.day, input.year);

            // add the cachedInput file into the file ArrayList
            ArrayList<ProcessorFileMetaData> files = map.get(thisDate);
            if (files == null)
            {
                files = new ArrayList<ProcessorFileMetaData>();
            }
            // add the cachedInput file into the arraylist associated with this date
            files.add(input);
            // modify the map
            map.put(thisDate, files);
        }

        IndexMetaData iMetaData = pluginMetaData.Indices;
        ArrayList<String> indicesNames  = iMetaData.indicesNames;

        for (Map.Entry<DataDate, ArrayList<ProcessorFileMetaData>> entry : map.entrySet())
        {
            DataDate thisDay = entry.getKey();

            File [] inputFiles =  new File [entry.getValue().size()];

            int i = 0;
            // feed the inputs
            for (ProcessorFileMetaData dFile : entry.getValue())
            {
                inputFiles[i++]= new File(dFile.dataFilePath);
            }

            // output file path
            String outputPath = String.format("%s"+ File.separator + "%04d" + File.separator+"%03d",
                    outputFolder, thisDay.getYear(), thisDay.getDayOfYear());

            if(!(new File(outputPath).exists()))
            {
                FileUtils.forceMkdir(new File(outputPath));
            }

            for(String indices: indicesNames)
            {
                Class<?> clazzIndicies;
                try
                {

                    clazzIndicies = Class.forName(String.format("version2.prototype.indices.%s.%s", pluginName, indices));
                    Constructor<?> ctorIndicies = clazzIndicies.getConstructor();

                    Object indexCalculator =  ctorIndicies.newInstance();

                    //set input files
                    Method method = indexCalculator.getClass().getMethod("setInputFiles", new Class[]{File[].class});
                    method.invoke(indexCalculator, new Object[]{inputFiles});

                    // set output file
                    String outFile = outputPath + indices + ".tif";
                    Method methodOut = indexCalculator.getClass().getMethod("setOutputFile", File.class);
                    methodOut.invoke(indexCalculator, new File(outFile));

                    Method methodCal = indexCalculator.getClass().getMethod("calculate");
                    methodCal.invoke(indexCalculator);



                }catch(Exception e)
                {
                    throw new EmptyStackException(); // class not found
                }
            }

        }

        return null;
    }
}
