package version2.prototype.summary.temporal;

import java.io.File;
import java.util.GregorianCalendar;

public interface MergeStrategy {
    File Merge(String projectName, String pluginName, GregorianCalendar firstDate, File... rasterFiles) throws Exception;
}
