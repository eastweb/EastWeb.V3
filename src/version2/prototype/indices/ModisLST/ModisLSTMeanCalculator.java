package version2.prototype.indices.ModisLST;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

public class ModisLSTMeanCalculator extends IndicesFramework{
    final int DAY_LST = 0;
    final int NIGHT_LST = 1;

    // valid range for data value are from 7500 to 65535
    public ModisLSTMeanCalculator(){}

    @Override
    protected double calculatePixelValue(double[] values) {
        if (values[DAY_LST] == GdalUtils.NoValue || values[NIGHT_LST] == GdalUtils.NoValue
                || values[DAY_LST] < 7500 || values[DAY_LST] > 65535
                || values[NIGHT_LST] < 7500 || values[NIGHT_LST] > 65535) {
            return -3.4028234663852886E38;
        } else {
            return (values[DAY_LST] + values[NIGHT_LST]) / 2;
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }
}
