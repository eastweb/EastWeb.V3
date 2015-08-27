package version2.prototype.processor.ModisLST;

import java.nio.ByteOrder;

import version2.prototype.processor.BandInfo;
import version2.prototype.processor.Mozaic_HDF4;
import version2.prototype.processor.ProcessData;

public class ModisLSTMozaic_HDF4 extends Mozaic_HDF4
{
    public ModisLSTMozaic_HDF4(ProcessData data) throws Exception {
        super(data);
    }

    //    public ModisLSTMozaic_HDF4() throws Exception
    //    {   super();    }

    @Override
    protected BandInfo[] bandInfo() {
        // TODO Auto-generated method stub
        return new BandInfo[]{new BandInfo(12, 1, ByteOrder.LITTLE_ENDIAN, 16),
                new BandInfo(12, 2, ByteOrder.LITTLE_ENDIAN, 8),
                new BandInfo(12, 5, ByteOrder.LITTLE_ENDIAN, 16),
                new BandInfo(12, 6, ByteOrder.LITTLE_ENDIAN, 8)};
    }

    @Override
    protected int getXSize() {
        // TODO Auto-generated method stub
        return 1200;
    }

    @Override
    protected int getYSize() {
        // TODO Auto-generated method stub
        return 1200;
    }
}
