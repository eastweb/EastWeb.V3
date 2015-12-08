package version2.prototype.processor.TRMM3B42;

import version2.prototype.processor.ProcessData;
import version2.prototype.processor.Reproject;

public class TRMM3B42Reproject extends Reproject{

    public TRMM3B42Reproject(ProcessData data, Boolean deleteInputDirectory) {
        super(data, deleteInputDirectory);
        NoProj =  false;
    }



}
