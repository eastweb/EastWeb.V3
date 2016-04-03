package EastWeb_Processor.ModisLST;

import EastWeb_Processor.Mozaic;
import EastWeb_Processor.ProcessData;

public class ModisLSTMozaic extends Mozaic{

    public ModisLSTMozaic(ProcessData data, Boolean deleteInputDirectory) throws InterruptedException {
        super(data, deleteInputDirectory);
    }

    @Override
    protected int[] getBands() {
        return new int[] {1,2, 5, 6};
    }
}
