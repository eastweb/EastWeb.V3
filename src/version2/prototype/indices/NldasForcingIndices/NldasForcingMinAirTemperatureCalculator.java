package version2.prototype.indices.NldasForcingIndices;

import org.gdal.gdal.Dataset;

import version2.prototype.indices.IndicesFramework;

public class NldasForcingMinAirTemperatureCalculator extends IndicesFramework {

    public NldasForcingMinAirTemperatureCalculator() {
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
        double min = 9999;

        for(double value : values)
        {
            // fill value == 9999
            if (value != 9999) {
                // Tc = Tk - 273.15
                value = value - 273.15;

                if(value < min) {
                    min = value;
                }
            }
        }

        if(min == 9999){
            return -3.4028234663852886E38;
        }

        return min;
    }

    @Override
    protected String className() {
        return getClass().getName();
    }

}
