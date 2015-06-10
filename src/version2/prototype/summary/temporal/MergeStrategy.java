package version2.prototype.summary.temporal;

import java.io.File;
import java.util.GregorianCalendar;

import version2.prototype.util.DataFileMetaData;

public interface MergeStrategy {
    DataFileMetaData Merge(String workingDir, String projectName, String pluginName, GregorianCalendar firstDate, File[] rasterFiles) throws Exception;
}
