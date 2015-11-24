/**
 *
 */
package version2.prototype.summary.temporal.MergeStrategies;

import java.io.File;
import java.sql.Statement;
import java.time.LocalDate;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.Process;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.summary.temporal.MergeStrategy;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseConnection;
import version2.prototype.util.FileSystem;
import version2.prototype.util.GdalUtils;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class SummationGdalRasterFileMerge implements MergeStrategy {

    @Override
    public DataFileMetaData Merge(Config configInstance, DatabaseConnection con, Process process, ProjectInfoFile projectInfo, String pluginName, String indexNm, LocalDate firstDate, File[] rasterFiles)
            throws Exception {

        DataFileMetaData mergedFile = null;
        GdalUtils.register();

        String newFilePath = FileSystem.GetProcessWorkerTempDirectoryPath(projectInfo.GetWorkingDir(), projectInfo.GetProjectName(), pluginName, ProcessName.SUMMARY) +
                String.format("%04d%03d.tif",
                        firstDate.getYear(),
                        firstDate.getDayOfYear()
                        );
        new File(newFilePath).delete();

        synchronized (GdalUtils.lockObject) {
            // Create output copy based on rasterFiles[0]
            Dataset rasterDs = gdal.Open(rasterFiles[0].getPath());
            GdalUtils.errorCheck();
            Dataset sumRasterDs = gdal.GetDriverByName("GTiff").CreateCopy(newFilePath, rasterDs);
            rasterDs.delete();

            // Populate avgArray with first file's data
            int xSize = sumRasterDs.GetRasterXSize();
            int ySize = sumRasterDs.GetRasterYSize();
            double[] avgArray = new double[ySize * xSize];
            if(sumRasterDs.GetRasterBand(1).ReadRaster(0, 0, xSize, ySize, avgArray) != 0) {
                ErrorLog.add(process, "Can't read the Raster band : " + rasterFiles[0].getPath(), new Exception("Can't read the Raster band : " + rasterFiles[0].getPath()));
            }

            // Initialize arrays
            int index;
            double[] tempArray = new double[ySize * xSize];
            int[] pixelsPerPos = new int[tempArray.length];
            for(int y=0; y < ySize; y++)
            {
                for(int x=0; x < xSize; x++)
                {
                    index = y * xSize + x;
                    avgArray[index] = 0;
                    pixelsPerPos[index] = 0;
                }
            }

            // Sum up values from the rest of the files
            for(int i=1; i < rasterFiles.length; i++)
            {
                rasterDs = gdal.Open(rasterFiles[i].getPath());
                if(rasterDs.GetRasterBand(1).ReadRaster(0, 0, xSize, ySize, tempArray) != 0) {
                    ErrorLog.add(process, "Can't read the Raster band : " + rasterFiles[i].getPath(), new Exception("Can't read the Raster band : " + rasterFiles[i].getPath()));
                }

                for(int y=0; y < ySize; y++)
                {
                    for(int x=0; x < xSize; x++)
                    {
                        index = y * xSize + x;
                        if(tempArray[index] != GdalUtils.NO_DATA) {
                            avgArray[index] += tempArray[index];
                            pixelsPerPos[index] += 1;
                        }
                    }
                }
                rasterDs.delete();
            }

            // Write averaged array to raster file
            //            synchronized (GdalUtils.lockObject) {
            sumRasterDs.GetRasterBand(1).WriteRaster(0, 0, xSize, ySize, avgArray);
            sumRasterDs.GetRasterBand(1).SetNoDataValue(GdalUtils.NO_DATA);
            //            }

            tempArray = null;
            avgArray = null;
            sumRasterDs.delete();

            Statement stmt = con.createStatement();
            try{
                int dateGroupID = Schemas.getDateGroupID(configInstance.getGlobalSchema(), firstDate, stmt);
                mergedFile = new DataFileMetaData(newFilePath, dateGroupID, firstDate.getYear(), firstDate.getDayOfYear(), indexNm);
            } finally {
                stmt.close();
            }
        }
        return mergedFile;
    }

}
