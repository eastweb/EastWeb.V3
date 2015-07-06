package version2.prototype.projection.ModisLST;

import java.io.File;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import version2.prototype.projection.Filter;
import version2.prototype.projection.ProcessData;
import version2.prototype.util.GdalUtils;

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
    protected void filterByQCFlag(String qcLevel) {
        // TODO Auto-generated method stub
        // filter pixel by pixel

        GdalUtils.register();
        synchronized (GdalUtils.lockObject) {
            for (File mInput : inputFiles){

                Dataset inputDS = gdal.Open(mInput.getPath());
                assert(inputDS.GetRasterCount() == 1);

                Dataset outputDS = createOutput(inputDS);

                int xSize = outputDS.GetRasterXSize();
                int ySize = outputDS.GetRasterYSize();

                //FIXME: assume the dataset is double.  If not, need to define different array type and buf-type.
                // maybe in an abstract class?
                double[] array = new double[xSize * ySize];

                // use GDT_Float32 (6) for the buffer
                // read the whole raster out into the array
                int readReturn = outputDS.GetRasterBand(1).ReadRaster(0, 0, xSize, ySize, 6, array);
                if (readReturn != 0) {
                    try {
                        throw new Exception("Cant read the Raster band : " + mInput.getPath());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                // get each unit out and filter it
                for (int y=0; y<outputDS.GetRasterYSize(); y++) {
                    for (int x=0; x<outputDS.GetRasterXSize(); x++) {
                        int index = y * xSize + x;
                        array[index] = filterQCValue(array[index],qcLevel);
                    }
                }

                synchronized (GdalUtils.lockObject) {
                    outputDS.GetRasterBand(1).WriteRaster(0, 0, xSize, ySize, 6, array);

                }

                inputDS.delete();
                outputDS.delete();
            }
        }
    }


    private double filterQCValue(double array, String qcLevel) {
        // TODO Auto-generated method stub
        String qclevel=getQCLevel(array);
        if(qclevel.equalsIgnoreCase(qcLevel))
        {
            return array;
        } else {
            return
                    -9999;
        }
    }

    public String getQCLevel(double value)
    {
        //convert the integer qcband value into binary
        Double v=new Double(value);
        int iValue=v.intValue();
        String qcbandValue=Integer.toBinaryString(iValue);
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
