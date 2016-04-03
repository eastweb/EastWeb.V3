package EastWeb_Indices.TRMM3B42;

import java.io.File;
import java.util.List;

import EastWeb_Indices.IndicesFramework;

public class TRMM3B42Index extends IndicesFramework
{
    private final int INPUT = 0;

    public TRMM3B42Index(List<File> inputFiles, File outputFile, Integer noDataValue)
    {
        super(inputFiles, outputFile, noDataValue);
    }

    @Override
    public void calculate() throws Exception
    { super.calculate(); }

    @Override
    protected double calculatePixelValue(double[] values) throws Exception {
        if (values[INPUT] == 32767 || values[INPUT] == noDataValue) {
            //            return -3.4028234663852886E38;
            return noDataValue;
        } else {
            return values[INPUT];
        }
    }

    @Override
    protected String className() {
        return getClass().getName();
    }
}
