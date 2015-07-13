package version2.prototype.summary.temporal.MergeStrategies;

import java.io.File;
import java.time.LocalDate;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.Scheduler.ProcessName;
import version2.prototype.summary.temporal.MergeStrategy;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.FileSystem;
import version2.prototype.util.GdalUtils;

/**
 * Concrete MergeStrategy. Represents a merging based on averages of values in raster files.
 *
 * @author michael.devos
 *
 */
public class AvgGdalRasterFileMerge implements MergeStrategy {

    /* (non-Javadoc)
     * @see version2.prototype.summary.temporal.MergeStrategy#Merge(java.lang.String, java.lang.String, java.lang.String, java.util.GregorianCalendar, java.io.File[])
     */
    @Override
    public DataFileMetaData Merge(String workingDir, String projectName, String pluginName, LocalDate firstDate, File[] rasterFiles)
            throws Exception {
        GdalUtils.register();
        DataFileMetaData mergedFile = null;
        String newFilePath = FileSystem.GetProcessWorkerTempDirectoryPath(workingDir, projectName, pluginName, ProcessName.SUMMARY) +
                String.format("%04d%03d.tif",
                        firstDate.getYear(),
                        firstDate.getDayOfYear()
                        );

        synchronized (GdalUtils.lockObject) {
            // Create output copy based on rasterFiles[0]
            Dataset inputRasterDs = gdal.Open(rasterFiles[0].getPath(), gdalconst.GA_ReadOnly);
            GdalUtils.errorCheck();
            Dataset avgRasterDs = gdal.GetDriverByName("GTiff").CreateCopy(newFilePath, inputRasterDs);
            //            Dataset outputRasterDs = originRasterDs.GetDriver().CreateCopy(newFilePath, inputRasterDs);
            double[][] avgArray = new double[avgRasterDs.GetRasterYSize()][avgRasterDs.GetRasterXSize()];

            // Populate avgArray with first file's data
            for(int y=0; y < avgRasterDs.GetRasterYSize(); y++) {
                avgRasterDs.GetRasterBand(1).ReadRaster(0, y, avgRasterDs.GetRasterXSize(), 1, avgArray[y]);
            }

            // Sum up values from the rest of the files
            double[] tempArray;
            for(int i=1; i < rasterFiles.length; i++)
            {
                inputRasterDs = gdal.Open(rasterFiles[i].getPath(), gdalconst.GA_ReadOnly);
                GdalUtils.errorCheck();
                tempArray = new double[inputRasterDs.GetRasterXSize()];
                for(int y=0; y < avgRasterDs.GetRasterYSize(); y++) {
                    inputRasterDs.GetRasterBand(1).ReadRaster(0, y, inputRasterDs.GetRasterXSize(), 1, tempArray);
                    for(int x=0; x < tempArray.length; x++) {
                        avgArray[y][x] += tempArray[x];
                    }
                }
                inputRasterDs.delete();
                tempArray = null;
            }

            // Average values
            for(int y=0; y < avgArray.length; y++) {
                for(int x=0; x < avgArray[y].length; x++) {
                    avgArray[y][x] = avgArray[y][x] / rasterFiles.length;
                }

                // Write averaged array to raster file
                avgRasterDs.GetRasterBand(1).WriteRaster(0, y, avgRasterDs.GetRasterXSize(), 1, avgArray[y]);
            }
            avgArray = null;
            avgRasterDs.delete();

            mergedFile = DatabaseCache.Parse(newFilePath);
        }
        return mergedFile;
    }
}

