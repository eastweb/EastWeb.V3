package version2.prototype.projection.ModisLST;

import version2.prototype.projection.Filter;
import version2.prototype.projection.ProcessData;

public class ModisLSTFilter extends Filter{

    public ModisLSTFilter(ProcessData data) {
        super(data);
    }

    @Override
    protected double filterValue(double value) {
        //in data bands the data type is 16bits unsigned integer
        if (value < 7500 || value > 65535) {
            return 65536;
        } else {
            return value;
        }
    }

    @Override
    protected double filterByQCFlag(String qcLevel) {
        // TODO Auto-generated method stub
        return 0;
    }




}
