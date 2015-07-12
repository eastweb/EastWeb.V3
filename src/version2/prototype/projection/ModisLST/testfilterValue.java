package version2.prototype.projection.ModisLST;

import version2.prototype.util.GdalUtils;


public class testfilterValue {

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        String s1=setQCLevel(10);

        System.out.println(s1);
        String s2=setQCLevel(165);

        System.out.println(s2);
        int m=filterQCValue(165,"low");
        System.out.println(m);
        m=filterQCValue(165,"moderate");
        System.out.println(m);
        m=filterQCValue(165,"highest");
        System.out.println(m);
        m=filterQCValue(165,"none");
        System.out.println(m);
        m=filterQCValue(165,"xxxx");
        System.out.println(m);




    }
    private static int filterQCValue(int array, String QCLevel) {
        String []level={"HIGHEST","MODERATE","LOW","NONE"};
        int indexQCLevel=-1;
        int indexqclevel=-1;
        for(int i=0;i<4;i++)
        {
            if(QCLevel.equalsIgnoreCase(level[i]))
            {
                indexQCLevel=i;
                break;
            }
        }
        String qclevel=setQCLevel(array);
        for(int j=0;j<4;j++)
        {
            if(qclevel.equalsIgnoreCase(level[j]))
            {
                indexqclevel=j;
                break;
            }
        }

        if(indexqclevel<=indexQCLevel&&indexqclevel>=0)//means match, no need to filter
        {
            return array;
        }
        else
        {
            return GdalUtils.NoValue;
        }
    }

    public static String setQCLevel(int value)
    {
        //convert the integer qcband value into binary
        String qcbandValue=Integer.toBinaryString(value);
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
        String qclevel="null";
        if(convertValue[6]=='0' && convertValue[7]=='0')
        {
            h=true;
            qclevel="HIGHEST";
            return qclevel;
        }
        if((convertValue[6]=='0' && convertValue[7]=='1' &&convertValue[0]=='0')||h==true)
        {
            m=true;
            qclevel="MODERATE";
            return qclevel;
        }
        if((convertValue[6]=='0' && convertValue[7]=='1' && convertValue[0]=='1' && convertValue[1]=='0')||m==true)
        {
            l=true;
            qclevel="LOW";
            return qclevel;
        }
        if((convertValue[6]=='0' && convertValue[7]=='1' && convertValue[0]=='1' && convertValue[1]=='1')||l==true)
        {
            qclevel="NONE";// no screening
            return qclevel;
        }
        return qclevel;
    }

}
