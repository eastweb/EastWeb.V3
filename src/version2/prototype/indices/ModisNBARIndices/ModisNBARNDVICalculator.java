package version2.prototype.indices.ModisNBARIndices;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GeneralListener;

public class ModisNBARNDVICalculator extends IndicesFramework {
    private static final int RED = 0;
    private static final int NIR = 1;

    public ModisNBARNDVICalculator(){}

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[NIR] == 32767 || values[RED] == 32767) {
            return -3.40282346639e+038;
        } else {
            return (values[NIR] - values[RED]) / (values[RED] + values[NIR]);
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }

}
