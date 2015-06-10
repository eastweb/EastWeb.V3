package version2.prototype.summary.temporal;

import java.io.File;
import java.util.GregorianCalendar;

import version2.prototype.util.CachedDataFile;

public interface MergeStrategy {
    CachedDataFile Merge(String projectName, String pluginName, GregorianCalendar firstDate, File... rasterFiles) throws Exception;
}
