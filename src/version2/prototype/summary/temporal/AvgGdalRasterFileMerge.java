package version2.prototype.summary.temporal;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

import version2.prototype.summary.summaries.SummariesCollection;
import version2.prototype.summary.summaries.SummaryNameResultPair;
import version2.prototype.util.FileSystem;
import version2.prototype.util.GdalUtils;

public class AvgGdalRasterFileMerge implements MergeStrategy {

    @Override
    public File Merge(String projectName, String pluginName, GregorianCalendar firstDate, File... rasterFiles) throws Exception {
        GdalUtils.register();
        File mergedFile = null;

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

            String newFilePath = FileSystem.GetProcessWorkerTempDirectoryPath(projectName, pluginName, "Summary", "Temporal") +
                    String.format("%04d%03d.tif",
                            firstDate.get(Calendar.YEAR),
                            firstDate.get(Calendar.DAY_OF_YEAR)
                            );
            rasterCopy.SetGeoTransform(outTransform);
            rasterCopy.GetDriver().CreateCopy(newFilePath, rasterCopy, 1);
            mergedFile = new File(newFilePath);
        }
        return mergedFile;
    }
}

