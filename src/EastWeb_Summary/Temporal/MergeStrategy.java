package EastWeb_Summary.Temporal;

import java.io.File;
import java.time.LocalDate;

import EastWeb_Config.Config;
import EastWeb_Database.DatabaseConnection;
import ProjectInfoMetaData.ProjectInfoFile;
import Utilies.DataFileMetaData;

import EastWeb_ProcessWorker.Process;

/**
 * Merging strategy template to be used in temporal summary for creating classes to define how to create composite data files.
 *
 * @author michael.devos
 *
 */
public interface MergeStrategy {
    /**
     * Merge method. Returns metadata about created merged raster file.
     *
     * @param configInstance
     * @param con
     * @param process
     * @param projectInfo
     * @param pluginName  - current plugin's name
     * @param indexNm  - current index name
     * @param firstDate  - date of first data file in composite
     * @param rasterFiles  - list of files to create composite from
     * @param outputFilePath  - string path for the merged file
     * @return metadata about single merged file created
     * @throws Exception
     */
    DataFileMetaData Merge(Config configInstance, DatabaseConnection con, Process process, ProjectInfoFile projectInfo, String pluginName, String indexNm, LocalDate firstDate, File[] rasterFiles,
            String outputFilePath) throws Exception;
}
