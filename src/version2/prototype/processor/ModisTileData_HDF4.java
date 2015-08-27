package version2.prototype.processor;

import java.io.File;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import version2.prototype.util.HDF4Reader;

public class ModisTileData_HDF4
{
    public String fname;
    public int horizon;
    public int vertical;
    public int xSize;
    public int ySize;

    /* @param file: the hdf4 file
     */
    public ModisTileData_HDF4(String file)
    {
        fname = file;
        // extract the horizon number and the vertical number from the file name
        // hddvdd
        String basename = FilenameUtils.getBaseName(file);
        horizon = Integer.parseInt(basename.substring(1,3));
        System.out.println("horizon = " + horizon);
        vertical = Integer.parseInt(basename.substring(4));
        System.out.println("vertical = " + vertical);
    }

}
