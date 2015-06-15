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
        int []i=qcBands;
        //convert the integer qcband value into binary
        String qcbandValue=Integer.toBinaryString(i[0]);
        int size=qcbandValue.length();
        char[]convertValue=new char[8];
        //convert the general binary order into 8-digit format
        for(int m=0;m<8-size;m++)
        {
            convertValue[m]='0';
        }
        int k=0;
        for(int n=8-size;n<8;n++)
        {
            convertValue[n]=qcbandValue.charAt(k);
            k++;
        }

        boolean h=false;
        boolean m=false;
        boolean l=false;
        String qclevel=null;
        if(qcbandValue.charAt(6)=='0' && qcbandValue.charAt(7)=='0')
        {
            h=true;
            qclevel="HIGHEST";
        }
        if((qcbandValue.charAt(6)=='0' && qcbandValue.charAt(7)=='1' && qcbandValue.charAt(0)=='0')||h==true)
        {
            m=true;
            qclevel="MODERATE";
        }
        if((qcbandValue.charAt(6)=='0' && qcbandValue.charAt(7)=='1' && qcbandValue.charAt(0)=='1' && qcbandValue.charAt(7)=='0')||m==true)
        {
            l=true;
            qclevel="LOW";
        }
        if((qcbandValue.charAt(6)=='0' && qcbandValue.charAt(7)=='1' && qcbandValue.charAt(0)=='1' && qcbandValue.charAt(1)=='1')||l==true)
        {
            qclevel="NONE";// no screening
        }
        return qclevel;
    }
}
