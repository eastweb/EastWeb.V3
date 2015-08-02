package version2.prototype.processor.ModisLST;

import version2.prototype.processor.Mozaic;
import version2.prototype.processor.ProcessData;

public class ModisLSTMozaic extends Mozaic{

    public ModisLSTMozaic(ProcessData data) throws InterruptedException {
        super(data);
    }

    @Override
    protected int[] getBands() {
        return new int[] {1,2, 5, 6};
    }
}
