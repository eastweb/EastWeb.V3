package version2.prototype.indices.NldasForcingIndices;

import org.gdal.gdal.Dataset;

import version2.prototype.DataDate;
import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GeneralListener;

public class NldasForcingHeatingDegreeDays extends IndicesFramework {
    DataDate mStart;
    double threshold;

    public NldasForcingHeatingDegreeDays(GeneralListener l, DataDate startDate, double thresholdCelsius) {
        super(l);

        mStart = startDate;
        threshold = thresholdCelsius;
    }

    @Override
    protected void process(Dataset[] inputs, Dataset output) throws Exception
    {
        int xSize = inputs[0].GetRasterXSize();
        int ySize = inputs[0].GetRasterYSize();
        double[][] inputsArray = new double[inputs.length][xSize];
        double[] outputArray = new double[xSize];

        for (int y = 0; y < ySize; y++) {
            for (int i = 0; i < inputs.length; i++) {
                // Raster band 1 == Air Temperature (K)
                inputs[i].GetRasterBand(1).ReadRaster(0, y, xSize, 1, inputsArray[i]);
            }

            for (int x = 0; x < xSize; x++) {
                double[] values = new double[inputs.length];

                for (int i = 0; i < inputs.length; i++) {
                    values[i] = inputsArray[i][x];
                }

                outputArray[x] = calculatePixelValue(values);
            }

            output.GetRasterBand(1).WriteRaster(0, y, xSize, 1, outputArray);
        }
    }

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        double gdd = -9999;

        for (double value : values)
        {
            // Fill value == 9999
            if(value != 9999 && value > threshold)
            {
                if(gdd == -9999) {
                    gdd = 0;
                }
                gdd = value - threshold;
            }
        }

        if(gdd == -9999){
            return -3.4028234663852886E38;
        }

        return gdd;
    }

    @Override
    protected String className() {
        return getClass().getName();
    }
}
