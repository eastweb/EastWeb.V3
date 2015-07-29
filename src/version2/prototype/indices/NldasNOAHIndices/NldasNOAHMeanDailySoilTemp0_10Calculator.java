package version2.prototype.indices.NldasNOAHIndices;

//import java.io.File;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

/*
 * Degrees Celsius = Kelvin - 273.15
 * originalValue - 273.15 = Degrees Celsius
 */

public class NldasNOAHMeanDailySoilTemp0_10Calculator extends IndicesFramework{

    private final static int INPUT = 0;

    public NldasNOAHMeanDailySoilTemp0_10Calculator() { }

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
            return values[INPUT] - 273.15;
        }
    }

    @Override
    protected String className() {
        // TODO Auto-generated method stub

        return getClass().getName();
    }
}
