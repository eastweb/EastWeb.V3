package version2.prototype.processor.NldasForcing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.processor.Composite;
import version2.prototype.processor.ProcessData;
import version2.prototype.util.GdalUtils;

public class NldasForcingComposite extends Composite
{
    private int[] mBands;

    public NldasForcingComposite(ProcessData data) {
        super(data);
        mBands = data.getDataBands();
    }

    @Override
    public void composeFiles()
    {
        GdalUtils.register();
        synchronized (GdalUtils.lockObject) {

            if(!(new File(outputFolder).exists())){
                try { FileUtils.forceMkdir(new File(outputFolder)); }
                catch (IOException e) { e.printStackTrace(); }
            }

            for(int band : mBands)
            {
                List<Dataset> inputDSs = new ArrayList<Dataset>();
                File temp = null;
                try {
                    temp = File.createTempFile(String.format("band%02d_", band),
                            ".tif",
                            new File(outputFolder));
                }
                catch (IOException e) { e.printStackTrace(); }

                for (File input : inputFiles) {
                    inputDSs.add(gdal.Open(input.getPath()));
                }

                int rasterX = inputDSs.get(0).GetRasterXSize();
                int rasterY = inputDSs.get(0).GetRasterYSize();

                Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                        temp.getAbsolutePath(),
                        rasterX, rasterY,
                        1,
                        gdalconst.GDT_Float32
                        );

                outputDS.SetGeoTransform(inputDSs.get(0).GetGeoTransform());
                outputDS.SetProjection(inputDSs.get(0).GetProjection());

                double[] inputArray = new double[rasterX * rasterY];
                double[] outputArray = new double[rasterX * rasterY];

                for (Dataset inputDS : inputDSs) {
                    inputDS.GetRasterBand(band).ReadRaster(0, 0, inputDS.GetRasterXSize(), inputDS.GetRasterYSize(), inputArray);
                    for (int i=0; i<inputArray.length; i++) {

                        if(band != 10){
                            // Add each piece of data proportional to the total.
                            outputArray[i] += (inputArray[i] / inputDSs.size());
                        }
                        else {
                            // band 10 == precipitation hourly total, so we don't want the average but rather the total
                            outputArray[i] += inputArray[i];
                        }
                    }
                }

                outputDS.GetRasterBand(1).WriteRaster(0, 0, rasterX, rasterY, outputArray);

                for (Dataset inputDS : inputDSs) {
                    inputDS.delete();
                }
                outputDS.delete();
            }
        }
    }
}
