package version2.prototype.processor.TRMM3B42;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.FilenameUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.processor.Convert;
import version2.prototype.processor.ProcessData;
import version2.prototype.util.GdalUtils;

public class TRMM3B42Convert extends Convert{

    public TRMM3B42Convert(ProcessData data) {
        super(data);
    }

    @Override
    protected void convertFiles() throws Exception {
        GdalUtils.register();
        synchronized (GdalUtils.lockObject) {

            // size for 3B42
            int xSize = 1440;
            int ySize = 400;

            if (inputFiles == null)
            { System.out.println("file does not exist");}

            for (File f:inputFiles){
                DataInputStream dis = new DataInputStream(new FileInputStream(f));
                String fileName = FilenameUtils.getBaseName(f.getName());

                File mOutput = new File (outputFolder + File.separator + fileName +".tif");

                Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                        mOutput.getPath(),
                        xSize, ySize,
                        1,
                        gdalconst.GDT_Float32
                        );

                double[] array = new double[xSize];
                for (int row=0; row<ySize; row++)
                {
                    for (int col=0; col<xSize; col++)
                    {
                        array[col] = dis.readFloat();
                    }
                    outputDS.GetRasterBand(1).WriteRaster(0, row, xSize, 1, array);
                }

                dis.close();

                outputDS.GetRasterBand(1).SetNoDataValue(GdalUtils.NoValue);
                //TRMM 3B42
                outputDS.SetGeoTransform(new double[] {
                        0.125, 0.25, 0,
                        -49.8750000, 0, 0.25
                });

                outputDS.SetProjection("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");

                outputDS.GetRasterBand(1).ComputeStatistics(false);
                outputDS.delete();
            }

        }

    }

}
