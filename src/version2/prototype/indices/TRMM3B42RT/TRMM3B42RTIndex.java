package version2.prototype.indices.TRMM3B42RT;

import version2.prototype.indices.IndicesFramework;

public class TRMM3B42RTIndex extends IndicesFramework
{
    private final int INPUT = 0;

    public TRMM3B42RTIndex() {}

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[INPUT] == 32767) {
            return -3.4028234663852886E38;
        } else {
            return values[INPUT];
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }

}