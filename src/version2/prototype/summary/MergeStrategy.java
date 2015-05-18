package version2.prototype.summary;

import java.io.File;
import java.util.GregorianCalendar;

public interface MergeStrategy {

    File Merge(GregorianCalendar firstDate, File shapeFile, File... rasterFiles) throws Exception;
}
