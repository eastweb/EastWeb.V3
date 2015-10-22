package version2.prototype.indices.TRMM3B42RT;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GdalUtils;

public class TRMM3B42RTIndex extends IndicesFramework
{
    private final int INPUT = 0;

    public TRMM3B42RTIndex() {}

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[INPUT] == GdalUtils.NoValue) {
            //   System.out.println("novalue : " + values[INPUT]);
            //            return -3.4028234663852886E38;
            return GdalUtils.NoValue;
        } else {
            return values[INPUT];
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }

}
