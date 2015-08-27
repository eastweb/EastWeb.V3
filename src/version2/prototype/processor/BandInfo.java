package version2.prototype.processor;

import java.nio.ByteOrder;

public class BandInfo
{
    public int totalBands;
    public int thisBand;
    public ByteOrder order;
    public int intSize;

    public BandInfo(int totalBands, int thisBand, ByteOrder order, int intSize)
    {
        this.totalBands = totalBands ;
        this.thisBand = thisBand ;
        this.order = order;
        this.intSize = intSize;
    }
}
