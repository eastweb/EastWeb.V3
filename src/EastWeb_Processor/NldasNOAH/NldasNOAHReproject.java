package EastWeb_Processor.NldasNOAH;

import EastWeb_Processor.ProcessData;
import EastWeb_Processor.Reproject;

public class NldasNOAHReproject extends Reproject{

    public NldasNOAHReproject(ProcessData data, Boolean deleteInputDirectory) {
        super(data, deleteInputDirectory);
        NoProj =  false;
    }
}