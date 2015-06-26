package version2.prototype.indices.ModisLSTIndices;

import version2.prototype.DataDate;
import version2.prototype.indices.IndicesFramework;

public class GdalModisLST_DAYCalculator extends IndicesFramework{

    final static int INPUT = 0;
    final double mMin;
    final double mMax;

    public GdalModisLST_DAYCalculator(){
    }

    @Override
    protected double calculatePixelValue(double[] values) {
        if (values[INPUT] < mMin || values[INPUT] > mMax) {
            return -3.4028234663852886E38;
        } else {
            return values[INPUT];
        }
    }
}