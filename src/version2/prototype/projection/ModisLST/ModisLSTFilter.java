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
        return 0;
    }

    @Override
    protected double filterByQCFlag(String qcLevel) {
        // TODO Auto-generated method stub
        //the value of qcBands is a 8-didit value
        // int[] value=qcBands;
        // QCLevel qc=ConvertQCLevel(getQCLevel());
        String qclevel=getQCLevel();
        if(qcLevel.equals(qclevel)==true) {
            return 0;
        } else {
            return -9999;
        }
    }


    public QCLevel ConvertQCLevel(String qclevel)
    {
        if(qclevel=="HIGHEST") {
            return QCLevel.HIGHEST;
        } else if(qclevel=="MODERATE") {
            return QCLevel.MODERATE;
        } else if(qclevel=="LOW") {
            return QCLevel.LOW;
        } else if(qclevel=="NONE"){
            return QCLevel.NONE;
        } else {
            return null;
        }
    }

    public String getQCLevel()
    {
        int []i=new int[8];
        i=qcBands;
        boolean h=false;
        boolean m=false;
        boolean l=false;
        String qclevel=null;
        if(i[6]==0 && i[7]==0)
        {
            h=true;
            qclevel="HIGHEST";
        }
        if((i[6]==0 && i[7]==1 && i[0]==0)||h==true)
        {
            m=true;
            qclevel="MODERATE";
        }
        if((i[6]==0 && i[7]==1 && i[0]==1 && i[1]==0)||m==true)
        {
            l=true;
            qclevel="LOW";
        }
        if((i[6]==0 && i[7]==1 && i[0]==1 && i[1]==1)||l==true)
        {
            qclevel="NONE";// no screening
        }
        return qclevel;
    }
}
