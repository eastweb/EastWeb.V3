package version2.prototype.projection.ModisLST;
import java.io.File;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import version2.prototype.util.GdalUtils;



public class testHDFfinal {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        // String filename= "/Users/fish/Desktop/MOD11A2.A2000073.h10v08.005.2007176154547.hdf";// x.filter is the filterd tif file
        File filename=new File("/Users/fish/Desktop/MOD11A2.A2000073.h10v08.005.2007176154547.hdf");
        String dayOutput="/Users/fish/Desktop/dayH.tif";
        String nightOutput="/Users/fish/Desktop/nightH.tif";

        String qcLevel="High";
        gdal.AllRegister();
        //get subDataset from the HDF file

        String Hfilepath=filename.getPath();


        Dataset[] hDataset=new Dataset[4];//store 2 data band and 2 qc band

        //"HDF4_EOS:EOS_GRID:/Users/fish/Desktop/1.hdf:MODIS_Grid_8Day_1km_LST:QC_Night"
        String head="HDF4_EOS:EOS_GRID:";
        String day=":MODIS_Grid_8Day_1km_LST:LST_Day_1km";
        String night=":MODIS_Grid_8Day_1km_LST:LST_Night_1km";
        String qcday=":MODIS_Grid_8Day_1km_LST:QC_Day";
        String qcnight=":MODIS_Grid_8Day_1km_LST:QC_Night";
        String dayPath=head+Hfilepath+day;
        String qcdayPath=head+Hfilepath+qcday;
        String nightPath=head+Hfilepath+night;
        String qcnightPath=head+Hfilepath+qcnight;
        System.out.println(dayPath);
        System.out.println(nightPath);
        System.out.println(qcdayPath);
        System.out.println(qcnightPath);

        hDataset[0]=gdal.Open(dayPath,gdalconstConstants.GA_ReadOnly);
        hDataset[1]=gdal.Open(qcdayPath,gdalconstConstants.GA_ReadOnly);
        hDataset[2]=gdal.Open(nightPath,gdalconstConstants.GA_ReadOnly);
        hDataset[3]=gdal.Open(qcnightPath,gdalconstConstants.GA_ReadOnly);

        //create copy of day databand tif file
        Dataset dayDataset=gdal.GetDriverByName("GTiff").CreateCopy(dayOutput, hDataset[0]);
        Dataset nightDataset=gdal.GetDriverByName("GTiff").CreateCopy(nightOutput, hDataset[2]);

        //in this hdf file subdataset, those four bands have same xsize and ysize
        int XSize=hDataset[1].getRasterXSize();
        int YSize=hDataset[1].getRasterYSize();

        Band dayband=hDataset[0].GetRasterBand(1);
        Band qcdayband=hDataset[1].GetRasterBand(1);
        Band nightband=hDataset[2].GetRasterBand(1);
        Band qcnightband=hDataset[3].GetRasterBand(1);

        //the data type is integer, so use int [] array to store the filtered value
        int dayArray[] = new int[XSize*YSize];
        int qcdayArray[]=new int[XSize*YSize];
        int nightArray[]=new int[XSize*YSize];
        int qcnightArray[]=new int[XSize*YSize];

        //read whole data

        //read day data band to dayArray[]
        int dayread=dayband.ReadRaster(0, 0, XSize, YSize, dayArray);
        //read qcday band to qcdayArray[]
        int qcdayread=qcdayband.ReadRaster(0, 0, XSize, YSize, qcdayArray);
        //read night data band to nightArray[]
        int nightread=nightband.ReadRaster(0, 0, XSize, YSize, nightArray);
        //read qcnight band to qcnightArray[]
        int qcnightread=qcnightband.ReadRaster(0, 0, XSize, YSize, qcnightArray);
        System.out.println("read raster successful!!! reuturn value is "+dayread+qcdayread+nightread+qcnightread);

        if (dayread!=0||qcdayread!=0||nightread!=0||qcnightread!=0) {
            try {
                throw new Exception("Cant read the Raster band : " + filename.getPath());
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
                //filterValue by qcday flag
                qcdayArray[index]=filterQCValue(qcdayArray[index],qcLevel);
                qcnightArray[index]=filterQCValue(qcnightArray[index],qcLevel);

                if(qcdayArray[index]==GdalUtils.NoValue)
                {
                    //use the upper if condition to find the bad pixel index, and this index of bad pixel in data band is bad pixel as well.
                    dayArray[index]=GdalUtils.NoValue;
                }

                //filterValue by qcnight flag
                if(qcnightArray[index]==GdalUtils.NoValue)
                {
                    //use the upper if condition to find the bad pixel index, and this index of bad pixel in data band is bad pixel as well.
                    nightArray[index]=GdalUtils.NoValue;
                }

            }
            System.out.println("\n");
        }

        synchronized (GdalUtils.lockObject) {
            dayDataset.GetRasterBand(1).WriteRaster(0, 0, XSize, YSize,dayArray);
            nightDataset.GetRasterBand(1).WriteRaster(0, 0, XSize, YSize,nightArray);
        }

        hDataset[0].delete();
        hDataset[1].delete();
        hDataset[2].delete();
        hDataset[3].delete();
        dayDataset.delete();
        nightDataset.delete();
        gdal.GDALDestroyDriverManager();
        System.out.println("good job!!!");
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
