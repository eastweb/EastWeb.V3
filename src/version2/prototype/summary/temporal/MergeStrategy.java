package version2.prototype.summary.temporal;

import java.io.File;
import java.util.GregorianCalendar;

public interface MergeStrategy {

    File Merge(GregorianCalendar firstDate, File... rasterFiles) throws Exception;
}
