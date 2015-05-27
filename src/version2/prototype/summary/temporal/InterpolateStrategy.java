package version2.prototype.summary.temporal;

import java.io.File;
import java.util.ArrayList;

public interface InterpolateStrategy {

    ArrayList<File> Interpolate(File rasterFile, int days);
}
