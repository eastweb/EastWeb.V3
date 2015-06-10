package version2.prototype.summary.temporal;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.FileSystem;
import version2.prototype.util.GdalUtils;

public class AvgGdalRasterFileMerge implements MergeStrategy {

    @Override
    public DataFileMetaData Merge(String workingDir, String projectName, String pluginName, GregorianCalendar firstDate, File[] rasterFiles)
            throws Exception {
        GdalUtils.register();
        DataFileMetaData mergedFile = null;

        synchronized (GdalUtils.lockObject) {
            double[] outTransform = null;
            ArrayList<double[]> transforms = new ArrayList<double[]>(1);

            // Open inputs
            for(File raster : rasterFiles)
            {
                // Prepare raster file data
                Dataset rasterDs = gdal.Open(raster.getPath(), gdalconst.GA_ReadOnly);
                GdalUtils.errorCheck();

                // Create the zone raster
                transforms.add(rasterDs.GetGeoTransform());
            }

            // Get averages
            Dataset rasterCopy = gdal.Open(rasterFiles[1].getPath(), gdalconst.GA_ReadOnly);
            outTransform = new double[transforms.get(1).length];
            int sum;
            for(int i=0; i < transforms.get(1).length; i++)
            {
                sum = 0;
                for(int t=0; t < transforms.size(); t++)
                {
                    sum += transforms.get(t)[i];
                }
                outTransform[i] = sum / transforms.size();
            }

            String newFilePath = FileSystem.GetProcessWorkerTempDirectoryPath(workingDir, projectName, pluginName, ProcessName.SUMMARY) +
                    String.format("%04d%03d.tif",
                            firstDate.get(Calendar.YEAR),
                            firstDate.get(Calendar.DAY_OF_YEAR)
                            );
            rasterCopy.SetGeoTransform(outTransform);
            rasterCopy.GetDriver().CreateCopy(newFilePath, rasterCopy, 1);
            mergedFile = DatabaseCache.Parse(newFilePath);
        }
        return mergedFile;
    }
}

