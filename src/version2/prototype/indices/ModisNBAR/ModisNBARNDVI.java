package version2.prototype.indices.ModisNBAR;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

/*
 *  1: Band 1: Red
 *  2: Band 2: NIR
 *  3: Band 3: Blue
 *  4: Band 4: Green
 *  5: Band 5: SWIR 1
 *  6: Band 6: SWIR 2
 *  7: Band 7: SWIR 3
 */
public class ModisNBARNDVI extends IndicesFramework {
    private final int RED;
    private final int NIR;

    public ModisNBARNDVI()
    {
        int tempRED = -1;
        int tempNIR = -1;

        for(int i=0; i < mInputFiles.length; i++)
        {
            if(mInputFiles[i].getName().toLowerCase().contains(new String("band2")))
            {
                tempNIR = i;
            }
            else if(mInputFiles[i].getName().toLowerCase().contains(new String("band1")))
            {
                tempRED = i;
            }

            if(tempNIR > -1 && tempRED > -1) {
                break;
            }
        }

        RED = tempRED;
        NIR = tempNIR;
    }

    /**
     * Valid input value range: 0 to 32766
     * Valid output value range: -1 to 1
     */
    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[NIR] > 32766 || values[NIR] < 0 || values[RED] > 32766 || values[RED] < 0 || values[NIR] == GdalUtils.NO_VALUE || values[RED] == GdalUtils.NO_VALUE) {
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
