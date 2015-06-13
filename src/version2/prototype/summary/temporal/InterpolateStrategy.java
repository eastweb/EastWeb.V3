package version2.prototype.summary.temporal;

import java.io.File;
import java.util.ArrayList;

/**
 * Interface designed for defining interpolation strategies for temporal summary calculations.
 *
 * @author michael.devos
 *
 */
public interface InterpolateStrategy {

    /**
     * Concrete classes will implement Interpolate(File, in) to define how to split single data files which contain multiple days worth of data into multiple files.
     *
     * @param rasterFile  - raster file to split
     * @param days  - days the raster file should be split into
     * @return  - list of files creates from the given rasterFile
     */
    ArrayList<File> Interpolate(File rasterFile, int days);
}
