package version2.prototype.indices.ModisNBAR;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

/**
 * EVI = G * (NIR - RED)/(NIR + C1*RED - C2*BLUE + L) where L=1, C1=6, C2=7.5,
 * and G=2.5
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

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[NIR] == 32767 || values[RED] == 32767 || values[BLUE] == 32767) {
            //            return -3.40282346639e+038;
            return GdalUtils.NoValue;
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