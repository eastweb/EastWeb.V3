package version2.prototype.processor.NldasNOAH;

import version2.prototype.processor.ProcessData;
import version2.prototype.processor.Reproject;

public class NldasNOAHReproject extends Reproject{

    public NldasNOAHReproject(ProcessData data) {
        super(data);
        NoProj =  false;
    }



}
