package version2.prototype.indices.ModisNBAR;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;


/**
 * uses the same logic for ndwi5 and ndwi6
 * NDWI5 = (NIR-SWIR2)/(NIR+SWIR2)
 * NDWI6 = (NIR-SWIR)/(NIR+SWIR)
 * @author Isaiah Snell-Feikema
 */

public class ModisNBARNDWI6 extends IndicesFramework {
    private static final int NIR = 0;
    private static final int SWIR = 1;

    public ModisNBARNDWI6(){}

    /**
     * Valid input value range: 0 to 32766
     * Valid output value range: -1 to 1
     */
    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[NIR] > 32766 || values[NIR] < 0 || values[SWIR] > 32766 || values[SWIR] < 0 || values[NIR] == GdalUtils.NO_VALUE || values[SWIR] == GdalUtils.NO_VALUE) {
            //            return -3.40282346639e+038;
            return GdalUtils.NO_DATA;
        } else {
            return (values[NIR] - values[SWIR]) / (values[SWIR] + values[NIR]);
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }

}

