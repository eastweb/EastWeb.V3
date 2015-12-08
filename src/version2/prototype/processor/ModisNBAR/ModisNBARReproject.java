package version2.prototype.processor.ModisNBAR;

import version2.prototype.processor.ProcessData;
import version2.prototype.processor.Reproject;

public class ModisNBARReproject extends Reproject {

    public ModisNBARReproject(ProcessData data, Boolean deleteInputDirectory) {
        super(data, deleteInputDirectory);
        NoProj =  false;
    }


}
