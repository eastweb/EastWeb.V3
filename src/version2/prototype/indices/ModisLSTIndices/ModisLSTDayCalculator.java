package version2.prototype.indices.ModisLSTIndices;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GeneralListener;

public class ModisLSTDayCalculator extends IndicesFramework{

    final static int INPUT = 0;
    double mMin;
    double mMax;

    public ModisLSTDayCalculator(){}


    @Override
    protected double calculatePixelValue(double[] values) {
        if (values[INPUT] < mMin || values[INPUT] > mMax) {
            return -3.4028234663852886E38;
        } else {
            return values[INPUT];
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }
}