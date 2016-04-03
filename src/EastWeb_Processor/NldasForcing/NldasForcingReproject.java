package EastWeb_Processor.NldasForcing;

import EastWeb_Processor.ProcessData;
import EastWeb_Processor.Reproject;

public class NldasForcingReproject extends Reproject {

    public NldasForcingReproject(ProcessData data, Boolean deleteInputDirectory) {
        super(data, deleteInputDirectory);
        NoProj =  false;
    }


}
