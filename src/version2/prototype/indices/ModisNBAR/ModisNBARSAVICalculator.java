package version2.prototype.indices.ModisNBAR;

import version2.prototype.indices.IndicesFramework;

public class ModisNBARSAVICalculator extends IndicesFramework {
    private final static double L = 0.5;
    private final static int RED = 0;
    private final static int NIR = 1;

    public ModisNBARSAVICalculator(){}

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[NIR] == 32767 || values[RED] == 32767) {
            return -3.40282346639e+038;
        } else {
            return (values[NIR] - values[RED] * (1 + L))
                    / (values[NIR] + values[RED] + L);
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }

}

