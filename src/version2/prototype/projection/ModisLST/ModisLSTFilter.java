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
        // filter pixel by pixel

        GdalUtils.register();

        synchronized (GdalUtils.lockObject) {
            for (File mInput : inputFiles){

                Dataset inputDS = gdal.Open(mInput.getPath());
                assert(inputDS.GetRasterCount() == 2);

                Dataset outputDS = createOutput(inputDS);
                double[] array = new double[outputDS.GetRasterXSize()];

                for (int y=0; y<outputDS.GetRasterYSize(); y++) {
                    outputDS.GetRasterBand(2).ReadRaster(0, y, outputDS.GetRasterXSize(), 1, array);

                    for (int x=0; x<outputDS.GetRasterXSize(); x++) {
                        String qclevel=getQCLevel(array[x]);
                        if(qcLevel.equals(qclevel)==true)
                        {
                            return array[x];
                        }
                        else
                        {
                            return -9999;
                        }
                    }

                    synchronized (GdalUtils.lockObject) {
                        outputDS.GetRasterBand(2).WriteRaster(0, y, outputDS.GetRasterXSize(), 1, array);
                    }
                }
                inputDS.delete();
                outputDS.delete();
            }
        }
    }


    public String getQCLevel(int value)
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
        String qclevel=null;
        if(convertValue[6]=='0' && convertValue[7]=='0')
        {
            h=true;
            qclevel="HIGHEST";
        }
        if((convertValue[6]=='0' && convertValue[7]=='1' &&convertValue[0]=='0')||h==true)
        {
            m=true;
            qclevel="MODERATE";
        }
        if((convertValue[6]=='0' && convertValue[7]=='1' && convertValue[0]=='1' && convertValue[1]=='0')||m==true)
        {
            l=true;
            qclevel="LOW";
        }
        if((convertValue[6]=='0' && convertValue[7]=='1' && convertValue[0]=='1' && convertValue[1]=='1')||l==true)
        {
            qclevel="NONE";// no screening
        }
        return qclevel;
    }
}
