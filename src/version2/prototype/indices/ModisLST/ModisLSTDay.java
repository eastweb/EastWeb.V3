package version2.prototype.indices.ModisLST;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

public class ModisLSTDay extends IndicesFramework{

    final static int INPUT = 0;

    public ModisLSTDay(){}

    // valid range for data value are from 7500 to 65535
    @Override
    protected double calculatePixelValue(double[] values) {
        if (values[INPUT] < 7500 || values[INPUT] > 65535 || values[INPUT] == GdalUtils.NoValue) {
            //        if(values[INPUT] == GdalUtils.NoValue) {
            //            return -3.4028234663852886E38;
            return GdalUtils.NoValue;
        } else {
            return (values[INPUT] * 0.02) - 273.16;
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }
}