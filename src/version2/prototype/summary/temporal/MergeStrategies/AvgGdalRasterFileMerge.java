package version2.prototype.summary.temporal.MergeStrategies;

import java.io.File;
import java.sql.Statement;
import java.time.LocalDate;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.summary.temporal.MergeStrategy;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseConnection;
import version2.prototype.util.DatabaseConnector;
import version2.prototype.util.FileSystem;
import version2.prototype.util.GdalUtils;
import version2.prototype.util.Schemas;
import version2.prototype.Process;

/**
 * Concrete MergeStrategy. Represents a merging based on averages of values in raster files.
 *
 * @author michael.devos
 *
 */
public class AvgGdalRasterFileMerge implements MergeStrategy {

    @Override
    public DataFileMetaData Merge(Config configInstance, Process process, ProjectInfoFile projectInfo, String pluginName, String indexNm, LocalDate firstDate, File[] rasterFiles) throws Exception {
        GdalUtils.register();

        DataFileMetaData mergedFile = null;
        String newFilePath = FileSystem.GetProcessWorkerTempDirectoryPath(projectInfo.GetWorkingDir(), projectInfo.GetProjectName(), pluginName, ProcessName.SUMMARY) +
                String.format("%04d%03d.tif",
                        firstDate.getYear(),
                        firstDate.getDayOfYear()
                        );

        synchronized (GdalUtils.lockObject) {
            // Create output copy based on rasterFiles[0]
            Dataset rasterDs = gdal.Open(rasterFiles[0].getPath());
            GdalUtils.errorCheck();
            Dataset avgRasterDs = gdal.GetDriverByName("GTiff").CreateCopy(newFilePath, rasterDs);
            rasterDs.delete();

            // Populate avgArray with first file's data
            int xSize = avgRasterDs.GetRasterXSize();
            int ySize = avgRasterDs.GetRasterYSize();
            double[] avgArray = new double[ySize * xSize];
            if(avgRasterDs.GetRasterBand(1).ReadRaster(0, 0, xSize, ySize, avgArray) != 0) {
                ErrorLog.add(process, "Can't read the Raster band : " + rasterFiles[0].getPath(), new Exception("Can't read the Raster band : " + rasterFiles[0].getPath()));
            }

            // Handle no data (invalid data) cases
            int index;
            for(int y=0; y < ySize; y++)
            {
                for(int x=0; x < xSize; x++)
                {
                    index = y * xSize + x;
                    if(avgArray[index] == -3.4028234663852886E38) {
                        avgArray[index] = 0;
                    }
                }
            }

            // Sum up values from the rest of the files
            double[] tempArray = new double[ySize * xSize];
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
                        if(tempArray[index] != -3.4028234663852886E38) {
                            avgArray[index] += tempArray[index];
                        }
                    }
                }
                rasterDs.delete();
            }

            // Average values
            for(int y=0; y < ySize; y++) {
                for(int x=0; x < xSize; x++) {
                    index = y * xSize + x;
                    avgArray[index] = avgArray[index] / rasterFiles.length;
                }
            }

            // Write averaged array to raster file
            synchronized (GdalUtils.lockObject) {
                avgRasterDs.GetRasterBand(1).WriteRaster(0, 0, xSize, ySize, avgArray);
            }

            tempArray = null;
            avgArray = null;
            avgRasterDs.delete();

            DatabaseConnection con = DatabaseConnector.getConnection(configInstance);
            Statement stmt = con.createStatement();
            int dateGroupID = Schemas.getDateGroupID(configInstance.getGlobalSchema(), firstDate, stmt);
            stmt.close();
            con.close();
            mergedFile = new DataFileMetaData(newFilePath, dateGroupID, firstDate.getYear(), firstDate.getDayOfYear(), indexNm);
        }
        return mergedFile;
    }
}

