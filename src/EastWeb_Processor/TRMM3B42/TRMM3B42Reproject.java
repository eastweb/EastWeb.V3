package EastWeb_Processor.TRMM3B42;

import EastWeb_Processor.ProcessData;
import EastWeb_Processor.Reproject;

public class TRMM3B42Reproject extends Reproject{

    public TRMM3B42Reproject(ProcessData data, Boolean deleteInputDirectory) {
        super(data, deleteInputDirectory);
        NoProj =  false;
    }
}