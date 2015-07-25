package version2.prototype.indices.TRMM3B42RTIndices;

import version2.prototype.indices.IndicesFramework;
import version2.prototype.util.GeneralListener;

public class TRMM3B42RTCalculator extends IndicesFramework
{
    private final int INPUT = 0;

    public TRMM3B42RTCalculator(GeneralListener l) {
        super(l);
    }

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
