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
                int outputs = 1;

                if(band == 1) {
                    outputs = 3;
                }

                for(int output = 0; output < outputs; output++)
                {
                    List<Dataset> inputDSs = new ArrayList<Dataset>();
                    String prefix = GetFilePrefix(band, output);

                    File temp = null;
                    try {
                        temp = File.createTempFile(prefix,
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

                    outputDS.GetRasterBand(1).WriteRaster(0, 0, rasterX, rasterY, GetOutputArray(band, output, inputDSs, rasterX, rasterY));

                    for (Dataset inputDS : inputDSs) {
                        inputDS.delete();
                    }
                    outputDS.delete();
                }
            }
        }
    }

    private static String GetFilePrefix(int band, int output)
    {
        String prefix = String.format("band%02d_", band);

        if(band == 1)
        {
            if(output == 0) {
                prefix = String.format("band%02d_Min_", band);
            }
            else if(output == 1) {
                prefix = String.format("band%02d_Mean_", band);
            }
            else if(output == 2) {
                prefix = String.format("band%02d_Max_", band);
            }
        }
        return prefix;
    }

    private static double[] GetOutputArray(int band, int output, List<Dataset> inputDSs, int rasterX, int rasterY)
    {
        double[] inputArray = new double[rasterX * rasterY];
        double[] outputArray = new double[rasterX * rasterY];

        if(band == 1 && output != 1)
        {
            if(output == 0) {
                outputArray = FindMinValues(inputDSs, rasterX, rasterY);
            }
            else if(output == 2) {
                outputArray = FindMaxValues(inputDSs, rasterX, rasterY);
            }
        }
        else if(band == 2 || (band == 1 && output == 1))
        {
            for (Dataset inputDS : inputDSs)
            {
                inputDS.GetRasterBand(band).ReadRaster(0, 0, inputDS.GetRasterXSize(), inputDS.GetRasterYSize(), inputArray);
                for (int i=0; i<inputArray.length; i++)
                {
                    outputArray[i] += (inputArray[i] / inputDSs.size());
                }
            }
        }
        else if(band == 10)
        {
            for (Dataset inputDS : inputDSs)
            {
                inputDS.GetRasterBand(band).ReadRaster(0, 0, inputDS.GetRasterXSize(), inputDS.GetRasterYSize(), inputArray);
                for (int i=0; i < inputArray.length; i++)
                {
                    // band 10 == precipitation hourly total, so we don't want the average but rather the total
                    outputArray[i] += inputArray[i];
                }
            }
        }

        return outputArray;
    }

    private static double[] FindMinValues(List<Dataset> inputDSs, int rasterX, int rasterY)
    {
        double[][] inputArrays = new double[inputDSs.size()][rasterX * rasterY];
        double[] outputArray = new double[rasterX * rasterY];

        for(int index = 0; index < inputDSs.size(); index++) {
            inputDSs.get(index).GetRasterBand(1).ReadRaster(0, 0, rasterX, rasterY, inputArrays[index]);
        }

        for(int pos = 0; pos < outputArray.length; pos++)
        {
            double minVal = 9999;
            for (int i = 0; i < inputDSs.size(); i++) {
                if(inputArrays[i][pos] < minVal) {
                    minVal = inputArrays[i][pos];
                }
            }
            outputArray[pos] = minVal;
        }

        return outputArray;
    }

    private static double[] FindMaxValues(List<Dataset> inputDSs, int rasterX, int rasterY)
    {
        double[][] inputArrays = new double[inputDSs.size()][rasterX * rasterY];
        double[] outputArray = new double[rasterX * rasterY];

        for(int index = 0; index < inputDSs.size(); index++) {
            inputDSs.get(index).GetRasterBand(1).ReadRaster(0, 0, rasterX, rasterY, inputArrays[index]);
        }

        for(int pos = 0; pos < outputArray.length; pos++)
        {
            double maxVal = -9999;
            for (int i = 0; i < inputDSs.size(); i++) {
                if(inputArrays[i][pos] > maxVal) {
                    maxVal = inputArrays[i][pos];
                }
            }
            outputArray[pos] = maxVal;
        }

        return outputArray;
    }
}

















































