package version2.prototype.indices;

import version2.prototype.util.GeneralListener;

/**
 * EVI = G * (NIR - RED)/(NIR + C1*RED - C2*BLUE + L) where L=1, C1=6, C2=7.5,
 * and G=2.5
 *
 * @author Isaiah Snell-Feikema
 */

public class GdalEVICalculator extends IndicesFramework {

    public GdalEVICalculator(GeneralListener l) {
        super(l);
        // TODO Auto-generated constructor stub
    }

    private static final double L = 1;
    private static final double C1 = 6;
    private static final double C2 = 7.5;
    private static final double G = 2.5;

    private static final int RED = 0;
    private static final int NIR = 1;
    private static final int BLUE = 2;

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[NIR] == 32767 || values[RED] == 32767
                || values[BLUE] == 32767) {
            return -3.40282346639e+038;
        } else {
            return G * (values[NIR] - values[RED])
                    / (values[NIR] + C1 * values[RED] - C2 * values[BLUE] + L);
        }
    }

}