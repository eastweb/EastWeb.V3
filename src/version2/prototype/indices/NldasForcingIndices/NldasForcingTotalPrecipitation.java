package version2.prototype.indices.NldasForcingIndices;

import org.gdal.gdal.Dataset;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GeneralListener;

public class NldasForcingTotalPrecipitation extends IndicesFramework {

    public NldasForcingTotalPrecipitation() {}

    @Override
    protected void process(Dataset[] inputs, Dataset output) throws Exception
    {
        int xSize = inputs[0].GetRasterXSize();
        int ySize = inputs[0].GetRasterYSize();
        double[][] inputsArray = new double[inputs.length][xSize];
        double[] outputArray = new double[xSize];

        for (int y = 0; y < ySize; y++) {
            for (int i = 0; i < inputs.length; i++) {
                // Raster band 10 == Total Daily Precipitation (kg/m^2)
                inputs[i].GetRasterBand(10).ReadRaster(0, y, xSize, 1, inputsArray[i]);
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
        double total = -9999;

        for(double value : values)
        {
            // Fill value == 9999
            if (value != 9999) {
                if(total == -9999) {
                    total = 0;
                }
                // No conversion necessary because the
                // density of water is approximately 1000 kg/m^3,
                // so the total mass of a 1-mm layer of water covering an area of 1 m^2 is 1 kg.
                total += value;
            }
        }

        if(total == -9999) {
            return -3.4028234663852886E38;
        }

        return total;
    }

    @Override
    protected String className() {
        // TODO Auto-generated method stub
        return null;
    }

}
