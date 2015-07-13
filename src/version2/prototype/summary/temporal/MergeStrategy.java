package version2.prototype.summary.temporal;

import java.io.File;
import java.time.LocalDate;

import version2.prototype.util.DataFileMetaData;

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
     * @param workingDir  - working directory of project
     * @param projectName  - current project's name
     * @param pluginName  - current plugin's name
     * @param firstDate  - date of first data file in composite
     * @param rasterFiles  - list of files to create composite from
     * @return metadata about single merged file created
     * @throws Exception
     */
    DataFileMetaData Merge(String workingDir, String projectName, String pluginName, LocalDate firstDate, File[] rasterFiles) throws Exception;
}
