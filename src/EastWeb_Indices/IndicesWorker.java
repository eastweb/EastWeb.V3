package EastWeb_Indices;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseCache;
import EastWeb_Database.DatabaseConnection;
import EastWeb_Database.DatabaseConnector;
import EastWeb_ErrorHandling.ErrorLog;
import EastWeb_ProcessWorker.ProcessWorker;
import EastWeb_ProcessWorker.ProcessWorkerReturn;
import EastWeb_Scheduler.ProcessName;
import PluginMetaData.IndicesMetaData;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;
import Utilies.DataDate;
import Utilies.DataFileMetaData;
import Utilies.FileSystem;
import Utilies.ProcessorFileMetaData;
import Utilies.Schemas;
import EastWeb_ProcessWorker.Process;

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
     * @param configInstance  - Config reference to use
     * @param process  - the parent Process object to this threaded worker.
     * @param projectInfoFile  - information about the project gotten from the project's info xml.
     * @param pluginInfo  - information about the plugin being used for the acquired data files.
     * @param pluginMetaData  - information relevant to this ProcessWorker about the plugin being used gotten from the plugin's info xml.
     * @param cachedFiles  - the list of files to process in this ProcessWorker.
     * @param outputCache
     */
    public IndicesWorker(Config configInstance, Process process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles, DatabaseCache outputCache)
    {
        super(configInstance, "IndicesWorker", process, projectInfoFile, pluginInfo, pluginMetaData, cachedFiles, outputCache);
    }

    @Override
    public ProcessWorkerReturn process() {
        DatabaseConnection con = DatabaseConnector.getConnection(configInstance);
        if(con == null) {
            return null;
        }
        String pluginName = pluginMetaData.Title;
        String outputFolder  =
                FileSystem.GetProcessOutputDirectoryPath(projectInfoFile.GetWorkingDir(),
                        projectInfoFile.GetProjectName(), pluginName, ProcessName.INDICES);
        Statement stmt = null;


        try {
            stmt = con.createStatement();
        } catch (SQLException e) {
            ErrorLog.add(process, "Problem creating Statement from db connection.", e);
        }

        if(stmt == null) {
            if(con != null) {
                con.close();
            }
            return null;
        }

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

        IndicesMetaData iMetaData = pluginMetaData.Indices;
        //        ArrayList<String> indicesNames  = iMetaData.indicesNames;
        ArrayList<String> indicesNames  = pluginInfo.GetIndices();
        boolean exists;
        for(String index : indicesNames)
        {
            exists = false;
            for(String knownIdx : iMetaData.indicesNames)
            {
                if(index.equals(knownIdx)) {
                    exists = true;
                }
            }
            if(!exists) {
                ErrorLog.add(process, "Problem encountered while caching data for IndicesWorker.", new Exception("Specified index, " + index + " is not part of plugin indices list."));
            }
        }

        ArrayList<DataFileMetaData> output;

        for (Map.Entry<DataDate, ArrayList<ProcessorFileMetaData>> entry : map.entrySet())
        {
            if(Thread.currentThread().isInterrupted())
            {
                try{
                    if(stmt != null) {
                        stmt.close();
                    }
                } catch(SQLException e) {
                    ErrorLog.add(process, "Problem closing connection.", e);
                }
                if(con != null) {
                    con.close();
                }
                return null;
            }

            output = new ArrayList<DataFileMetaData>();
            DataDate thisDay = entry.getKey();

            File [] inputFiles =  new File [entry.getValue().size()];

            int i = 0;
            // feed the inputs
            for (ProcessorFileMetaData dFile : entry.getValue())
            {
                inputFiles[i++]= new File(dFile.dataFilePath);
            }

            // output file path
            String outputPath = String.format("%s" + "%04d" + File.separator+"%03d",
                    outputFolder, thisDay.getYear(), thisDay.getDayOfYear());

            if(!(new File(outputPath).exists()))
            {
                try {
                    FileUtils.forceMkdir(new File(outputPath));
                } catch (IOException e) {
                    ErrorLog.add(process, "Problem creating output directory.", e);
                }
            }

            for(String indices: indicesNames)
            {
                Class<?> clazzIndicies;
                try
                {
                    clazzIndicies = Class.forName(String.format("version2.prototype.indices.%s.%s", pluginName, indices));
                    Constructor<?> ctorIndicies = clazzIndicies.getConstructor(List.class, File.class, Integer.class);

                    String outFile = outputPath + File.separator + indices + ".tif";
                    Object indexCalculator = ctorIndicies.newInstance(Arrays.asList(inputFiles.clone()), new File(outFile), pluginMetaData.NoDataValue);

                    Method method = indexCalculator.getClass().getMethod("calculate");
                    method.invoke(indexCalculator);

                    output.add(new DataFileMetaData(outFile, Schemas.getDateGroupID(configInstance.getGlobalSchema(), thisDay.getLocalDate(), stmt), thisDay.getYear(), thisDay.getDayOfYear(), indices));
                } catch(Exception e) {
                    ErrorLog.add(process, "Problem setting up IndexCalculator object for day " + thisDay.toString() + ". Number of input files " + inputFiles.length + ". Output file " + outputPath + File.separator + indices + ".tif.", e);
                }
            }

            try{
                outputCache.CacheFiles(stmt, output);
            } catch(SQLException | ParseException | ClassNotFoundException | ParserConfigurationException | SAXException | IOException e) {
                ErrorLog.add(process, "Problem encountered while caching data for IndicesWorker.", e);
            } catch (Exception e) {
                ErrorLog.add(process, "Problem encountered while caching data for IndicesWorker.", e);
            }
        }

        try{
            if(stmt != null) {
                stmt.close();
            }
        } catch(SQLException e) {
            ErrorLog.add(process, "Problem closing connection.", e);
        }

        if(con != null) {
            con.close();
        }
        return null;
    }
}