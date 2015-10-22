package version2.prototype.indices.ModisLST;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

public class ModisLSTMean extends IndicesFramework{
    final int DAY_LST = 0;
    final int NIGHT_LST = 1;

    // valid range for data value are from 7500 to 65535
    public ModisLSTMean(){}

    @Override
    protected double calculatePixelValue(double[] values) {
        //        if (values[DAY_LST] == GdalUtils.NoValue || values[NIGHT_LST] == GdalUtils.NoValue) {
        if (values[DAY_LST] == GdalUtils.NO_VALUE || values[NIGHT_LST] == GdalUtils.NO_VALUE
                || values[DAY_LST] < 7500 || values[DAY_LST] > 65535
                || values[NIGHT_LST] < 7500 || values[NIGHT_LST] > 65535) {
            //            return -3.4028234663852886E38;
            return GdalUtils.NO_DATA;
        } else {
            double day_lst = (values[DAY_LST] * 0.02) - 273.16;
            double night_lst = (values[NIGHT_LST] * 0.02) - 273.16;
            return (day_lst + night_lst) / 2;
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }
}
