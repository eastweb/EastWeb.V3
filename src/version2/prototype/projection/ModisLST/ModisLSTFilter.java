package version2.prototype.projection.ModisLST;

import java.io.File;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

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

                // String output="/Users/fish/Desktop/x.tif";
                gdal.AllRegister();
                Dataset hDataset = gdal.Open(mInput.getPath(), gdalconstConstants.GA_ReadOnly);
                if (hDataset == null)
                {
                    System.err.println("GDALOpen failed - " + gdal.GetLastErrorNo());
                    System.err.println(gdal.GetLastErrorMsg());
                    System.exit(1);
                }
                Dataset Dataset=gdal.GetDriverByName("GTiff").CreateCopy(outputFolder, hDataset);
                Driver hDriver = hDataset.GetDriver();
                Driver Driver = Dataset.GetDriver();
                System.out.println("DriverOld: " + hDriver.getShortName() + "/" + hDriver.getLongName());
                System.out.println("DriverCopy: " + Driver.getShortName() + "/" + hDriver.getLongName());
                int XSize = Dataset.getRasterXSize();
                int YSize = Dataset.getRasterYSize();
                int band1=Dataset.getRasterCount();
                System.out.println("Size is " + XSize + ", " + YSize+"  band number is "+band1);
                Band band = Dataset.GetRasterBand(1);

                //the data type is integer, so use int [] array to store the filtered value
                int array[] = new int[XSize*YSize];

                //read whole data
                int readReturn=band.ReadRaster(0, 0, XSize, YSize, array);
                System.out.println("read raster successful!!! reuturn value is "+readReturn);

                if (readReturn!= 0) {
                    try {
                        throw new Exception("Cant read the Raster band : " + mInput.getPath());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                for(int y=0; y<YSize; y++)
                {
                    // band.ReadRaster(0, y, iXSize, 1, buf); //read data for one line
                    // the pixel values stored in array[index]
                    for(int x=0; x<XSize; x++) {
                        int index = y * XSize + x;
                        //filterValue by qc flag
                        array[index]=filterQCValue(array[index],qcLevel);
                        // System.out.print(array[index] + ", ");
                    }
                    System.out.println("\n");
                }

                synchronized (GdalUtils.lockObject) {
                    Dataset.GetRasterBand(1).WriteRaster(0, 0, XSize, YSize,array);

                }
                hDataset.delete();
                Dataset.delete();
                gdal.GDALDestroyDriverManager();
            }
        }
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
