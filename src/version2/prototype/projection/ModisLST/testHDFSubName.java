package version2.prototype.projection.ModisLST;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import version2.prototype.util.GdalUtils;



public class testHDFSubName {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        File filename= new File("/Users/fish/Desktop/MOD11A2.A2000073.h10v08.005.2007176154547.hdf");// x.filter is the filterd tif file
        gdal.AllRegister();
        //read subDataset from the HDF file
        Dataset hDataset=gdal.Open(filename.getPath(),gdalconstConstants.GA_ReadOnly);
        Hashtable sdsdict = null;
        String qaBandName = new String();
        if(hDataset != null)
        {
            sdsdict = hDataset.GetMetadata_Dict("SUBDATASETS");
            Enumeration<String> keys = sdsdict.keys();

            // Get the correct SDS
            while (keys.hasMoreElements()) {
                Object aKey = keys.nextElement();
                Object aValue = sdsdict.get(aKey);

                if(aKey.toString().contains("NAME"))
                {
                    String []bandName1= aKey.toString().split("_");
                    // We are only interested in band 4
                    if(Integer.parseInt(bandName1[1]) == 6) // 1 for day, 2 for qcday,5 for night,6 for qcnight
                    {
                        qaBandName = aValue.toString();
                        System.out.println("\n");
                    }
                }
            }
        }

        System.out.println(qaBandName);
        //                        HDF4_EOS:EOS_GRID:"/Users/fish/Desktop/tile.hdf":MODIS_Grid_8Day_1km_LST:QC_Day
        //Dataset ds=gdal.Open("HDF4_EOS:EOS_GRID:/Users/fish/Desktop/1.hdf:MODIS_Grid_8Day_1km_LST:QC_Night");

        Dataset []ds=new Dataset[4];
        String op1="/Users/fish/Desktop/dayold.tif";

        String head="HDF4_EOS:EOS_GRID:";
        String day=":MODIS_Grid_8Day_1km_LST:LST_Day_1km";
        String night="MODIS_Grid_8Day_1km_LST:LST_Night_1km";
        String qcday=":MODIS_Grid_8Day_1km_LST:QC_Day";
        String qcnight=":MODIS_Grid_8Day_1km_LST:QC_Night";
        String Hfilepath=filename.getPath();
        String dayPath=head+Hfilepath+day;
        String qcdayPath=head+Hfilepath+qcday;
        String nightPath=head+Hfilepath+night;
        String qcnightPath=head+Hfilepath+qcnight;

        ds[0]=gdal.Open(dayPath);
        ds[1]=gdal.Open(qcdayPath);
        ds[2]=gdal.Open(nightPath);
        ds[3]=gdal.Open(qcnightPath);

        int XSize = ds[0].getRasterXSize();
        int YSize = ds[0].getRasterYSize();
        int band1=ds[0].getRasterCount();
        System.out.println("Size is " + XSize + ", " + YSize+"  band number is "+band1);
        Band band = ds[0].GetRasterBand(1);


        Dataset dayDataset=gdal.GetDriverByName("GTiff").CreateCopy(op1, ds[0]);
        int array[] = new int[XSize*YSize];

        //read whole data
        int readReturn=band.ReadRaster(0, 0, XSize, YSize, array);
        System.out.println("read raster successful!!! reuturn value is "+readReturn);

        if (readReturn!= 0) {
            try {
                throw new Exception("Cant read the Raster band ");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for(int y=YSize-1; y<YSize; y++)
        {
            // band.ReadRaster(0, y, iXSize, 1, buf); //read data for one line
            // the pixel values stored in array[index]
            for(int x=0; x<XSize; x++) {
                int index = y * XSize + x;
                //filterValue by qc flag
                // array[index]=filterQCValue(array[index],qcLevel);
                System.out.print(array[index] + ", ");
            }
            System.out.println("\n");
        }

        synchronized (GdalUtils.lockObject) {
            dayDataset.GetRasterBand(1).WriteRaster(0, 0, XSize, YSize,array);
        }

        ds[0].delete();
        dayDataset.delete();

    }
}
