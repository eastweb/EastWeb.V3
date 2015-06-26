package version2.prototype.indices.ModisLSTIndices;

import version2.prototype.indices.IndicesFramework;

public class GdalModisLST_MEANCalculator extends IndicesFramework{
    final int DAY_LST = 0;
    final int NIGHT_LST = 1;
    final double mMin;
    final double mMax;

    public GdalModisLST_MEANCalculator(){
    }

    @Override
    protected double calculatePixelValue(double[] values) {
        if (values[DAY_LST] == 32767 || values[NIGHT_LST] == 32767
                || values[DAY_LST] < mMin || values[DAY_LST] > mMax
                || values[NIGHT_LST] < mMin || values[NIGHT_LST] > mMax) {
            return -3.4028234663852886E38;
        } else {
            return (values[DAY_LST] + values[NIGHT_LST]) / 2;
        }
    }
}
