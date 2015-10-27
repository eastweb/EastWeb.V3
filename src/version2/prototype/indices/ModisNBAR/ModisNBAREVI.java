package version2.prototype.indices.ModisNBAR;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

/**
 * EVI = G * (NIR - RED)/(NIR + C1*RED - C2*BLUE + L) where L=1, C1=6, C2=7.5, and G=2.5
 *
 *@author Isaiah Snell-Feikema
 */

public class ModisNBAREVI extends IndicesFramework
{

    public ModisNBAREVI(){}

    private static final double L = 1;
    private static final double C1 = 6;
    private static final double C2 = 7.5;
    private static final double G = 2.5;

    private static final int RED = 0;
    private static final int NIR = 1;
    private static final int BLUE = 2;

    /**
     * Valid input value range: 0 to 32766
     * Possible output value range: -163800.0 to 163770.0
     * Valid output value range:
     */
    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[NIR] > 32766 || values[NIR] < 0 || values[RED] > 32766 || values[RED] < 0 || values[BLUE] > 32766 || values[BLUE] < 0 ||
                values[NIR] == GdalUtils.NO_VALUE || values[RED] == GdalUtils.NO_VALUE || values[BLUE] == GdalUtils.NO_VALUE) {
            //            return -3.40282346639e+038;
            return GdalUtils.NO_DATA;
        } else {
            return G * (values[NIR] - values[RED])
                    / (values[NIR] + C1 * values[RED] - C2 * values[BLUE] + L);
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }

}