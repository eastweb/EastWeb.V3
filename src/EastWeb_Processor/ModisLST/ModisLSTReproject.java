package EastWeb_Processor.ModisLST;

import EastWeb_Processor.ProcessData;
import EastWeb_Processor.Reproject;

public class ModisLSTReproject extends Reproject{

    public ModisLSTReproject(ProcessData data, Boolean deleteInputDirectory) {
        super(data, deleteInputDirectory);
        NoProj =  false;
    }

}
