package version2.prototype.indices.ModisNBAR;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;


/**
 * uses the same logic for ndwi5 and ndwi6
 * NDWI5 = (NIR-SWIR2)/(NIR+SWIR2)
 * NDWI6 = (NIR-SWIR)/(NIR+SWIR)
 * @author Isaiah Snell-Feikema
 */

/*
 *  1: Band 1: Red
 *  2: Band 2: NIR
 *  3: Band 3: Blue
 *  4: Band 4: Green
 *  5: Band 5: SWIR 1
 *  6: Band 6: SWIR 2
 *  7: Band 7: SWIR 3
 */
public class ModisNBARNDWI6 extends IndicesFramework {
    private final int NIR;
    private final int SWIR;

    public ModisNBARNDWI6()
    {
        int tempNIR = -1;
        int tempSWIR = -1;

        for(int i=0; i < mInputFiles.length; i++)
        {
            if(mInputFiles[i].getName().toLowerCase().contains(new String("band2")))
            {
                tempNIR = i;
            }
            else if(mInputFiles[i].getName().toLowerCase().contains(new String("band6")))
            {
                tempSWIR = i;
            }

            if(tempNIR > -1 && tempSWIR > -1) {
                break;
            }
        }

        NIR = tempNIR;
        SWIR = tempSWIR;
    }

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

