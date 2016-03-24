package version2.prototype.processor.TRMM3B42RT;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.FilenameUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import version2.prototype.ErrorLog;
import version2.prototype.processor.Convert;
import version2.prototype.processor.ProcessData;
import version2.prototype.util.GdalUtils;

public class TRMM3B42RTConvert extends Convert {
    private Integer noDataValue;

    public TRMM3B42RTConvert(ProcessData data, Boolean deleteInputDirectory) {
        super(data, deleteInputDirectory);
        noDataValue = data.getNoDataValue();
    }

    @Override
    protected void convertFiles() throws Exception{
        GdalUtils.register();
        synchronized (GdalUtils.lockObject) {

            int xSize = 1440;
            // size for 3B42RT
            int ySize = 480;

            if (inputFiles == null)
            {   return; }

            for (File f:inputFiles)
            {
                DataInputStream dis = new DataInputStream(new FileInputStream(f));

                String fileName = FilenameUtils.getBaseName(f.getName());

                File mOutput = new File (outputFolder + File.separator + fileName +".tif");
                Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                        mOutput.getPath(),
                        xSize, ySize,
                        1,
                        gdalconstConstants.GDT_Float32
                        );

                double[] array = new double[xSize];
                int row = 0, col = 0;
                try {
                    for (row=0; row<ySize; row++)
                    {
                        for (col=0; col<xSize; col++)
                        {
                            array[col] = dis.readFloat();
                        }
                        outputDS.GetRasterBand(1).WriteRaster(0, row, xSize, 1, array);
                    }
                } catch (EOFException e) {
                    ErrorLog.add("Problem while reading from data input stream to convert TRMM3B42RT file. [row=" + row + ", RowMax=" + ySize + ", col=" + col + ", ColMax=" + xSize + "]", e);
                }

                dis.close();

                outputDS.GetRasterBand(1).SetNoDataValue(noDataValue);
                //TRMM 3B42RT
                outputDS.SetGeoTransform(new double[] {
                        0.125, 0.25, 0,
                        -59.8750000, 0, 0.25
                });

                outputDS.SetProjection("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");

                outputDS.GetRasterBand(1).ComputeStatistics(false);
                outputDS.delete();
            }
        }
    }

}
