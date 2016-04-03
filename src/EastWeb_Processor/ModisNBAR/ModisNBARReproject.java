package EastWeb_Processor.ModisNBAR;

import EastWeb_Processor.ProcessData;
import EastWeb_Processor.Reproject;

public class ModisNBARReproject extends Reproject {

    public ModisNBARReproject(ProcessData data, Boolean deleteInputDirectory) {
        super(data, deleteInputDirectory);
        NoProj =  false;
    }


}
