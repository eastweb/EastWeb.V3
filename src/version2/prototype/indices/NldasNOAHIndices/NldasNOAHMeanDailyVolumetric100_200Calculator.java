package version2.prototype.indices.NldasNOAHIndices;

//import java.io.File;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

/*
 * Volumetric100_200: originalValue * (1/1) = ANS
 *  ANS * (0.001 / 1) = proportion
 */

public class NldasNOAHMeanDailyVolumetric100_200Calculator extends IndicesFramework{

    private final static int INPUT = 0;

    public NldasNOAHMeanDailyVolumetric100_200Calculator() { }

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        // TODO Auto-generated method stub

        if(values[INPUT] == GdalUtils.NoValue)
        {
            return -3.4028234663852886E38;
        }
        else
        {
            //return calculated value
            return values[INPUT] * (0.001 / 1);
        }
    }

    @Override
    protected String className() {
        // TODO Auto-generated method stub

        return getClass().getName();
    }

}
