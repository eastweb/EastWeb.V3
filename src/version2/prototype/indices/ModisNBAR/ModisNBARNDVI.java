package version2.prototype.indices.ModisNBAR;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

public class ModisNBARNDVI extends IndicesFramework {
    private static final int RED = 0;
    private static final int NIR = 1;

    public ModisNBARNDVI(){}

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[NIR] == 32767 || values[RED] == 32767 || values[NIR] == GdalUtils.NO_VALUE || values[RED] == GdalUtils.NO_VALUE) {
            //            return -3.40282346639e+038;
            return GdalUtils.NO_DATA;
        } else {
            return (values[NIR] - values[RED]) / (values[RED] + values[NIR]);
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }

}
