package EastWeb_Indices.NldasNOAH;

import java.io.File;
import java.util.List;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import EastWeb_Indices.IndicesFramework;
import Utilies.GdalUtils;

/*
 * Degrees Celsius = Kelvin - 273.15
 * originalValue - 273.15 = Degrees Celsius
 */

public class NldasNOAHMeanDailySoilTemp40_100 extends IndicesFramework{

    private final static int INPUT = 0;

    public NldasNOAHMeanDailySoilTemp40_100(List<File> inputFiles, File outputFile, Integer noDataValue)
    {
        super(inputFiles, outputFile, noDataValue);
    }

    @Override
    public void calculate() throws Exception {
        GdalUtils.register();

        synchronized (GdalUtils.lockObject) {

            Dataset[] inputs = new Dataset[1];

            for(File inputFile : mInputFiles)
            {
                if(inputFile.getName().contains("Band21"))
                {
                    inputs[0] = gdal.Open(inputFile.getAbsolutePath());
                }
            }

            Dataset outputDS = createOutput(inputs);
            process(inputs, outputDS);

            for (int i = 1; i <= outputDS.GetRasterCount(); i++) {
                Band band = outputDS.GetRasterBand(i);

                band.SetNoDataValue(noDataValue);
                band.ComputeStatistics(false);
            }

            for (Dataset input : inputs) {
                input.delete();
            }

            outputDS.delete();
        }
    }

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        // TODO Auto-generated method stub

        if(values[INPUT] == noDataValue)
        {
            //            return -3.4028234663852886E38;
            return noDataValue;
        }
        else
        {
            //return calculated value
            return values[INPUT] - 273.15;
        }
    }

    @Override
    protected String className() {
        // TODO Auto-generated method stub

        return getClass().getName();
    }
}
