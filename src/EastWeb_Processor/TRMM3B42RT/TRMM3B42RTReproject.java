package EastWeb_Processor.TRMM3B42RT;

import EastWeb_Processor.ProcessData;
import EastWeb_Processor.Reproject;

// For reflection
public class TRMM3B42RTReproject extends Reproject{

    public TRMM3B42RTReproject(ProcessData data, Boolean deleteInputDirectory) {
        super(data, deleteInputDirectory);
        NoProj =  false;
    }


}
