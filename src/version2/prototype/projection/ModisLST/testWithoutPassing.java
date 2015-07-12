package version2.prototype.projection.ModisLST;

import java.io.File;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import version2.prototype.util.GdalUtils;
public class testWithoutPassing {
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        //String fileName_tif = "/Users/fish/Desktop/LST_Night_1km.tif";
        File filename=new File("/Users/fish/Desktop/LST_Night_1km.tif");
        String output="/Users/fish/Desktop/x.tif";
        gdal.AllRegister();
        Dataset hDataset = gdal.Open(filename.getPath(), gdalconstConstants.GA_ReadOnly);
        if (hDataset == null)
        {
            System.err.println("GDALOpen failed - " + gdal.GetLastErrorNo());
            System.err.println(gdal.GetLastErrorMsg());
            System.exit(1);
        }
        Dataset Dataset=gdal.GetDriverByName("GTiff").CreateCopy(output, hDataset);
        Driver hDriver = hDataset.GetDriver();
        Driver Driver = Dataset.GetDriver();
        System.out.println("DriverOld: " + hDriver.getShortName() + "/" + hDriver.getLongName());
        System.out.println("DriverCopy: " + Driver.getShortName() + "/" + hDriver.getLongName());
        int iXSize = Dataset.getRasterXSize();
        int iYSize = Dataset.getRasterYSize();
        int band1=Dataset.getRasterCount();
        System.out.println("Size is " + iXSize + ", " + iYSize+"  band number is "+band1);
        Band band = Dataset.GetRasterBand(1);

        int buf[] = new int[iXSize*iYSize];
        int p=band.ReadRaster(0, 0, iXSize, iYSize, buf); //read whole data
        System.out.println("read raster successful!!! P is "+p);
        if (p!= 0) {
            try {
                throw new Exception("Cant read the Raster band : " + filename.getPath());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        int num=0;
        for(int y=iYSize-1; y<iYSize; y++)
        {
            // band.ReadRaster(0, y, iXSize, 1, buf); //read data for one line
            // the pixel values stored in array[index]
            for(int x=0; x<iXSize; x++) {
                int index = y * iXSize + x;
                //filterValue(buf[index]);
                if(buf[index]>255) {
                    buf[index]=0;
                }
                System.out.print(buf[index] + ", ");
                num++;
            }
            System.out.println("\n");
        }

        synchronized (GdalUtils.lockObject) {
            Dataset.GetRasterBand(1).WriteRaster(0, 0, iXSize, iYSize,buf);

        }

        System.out.println("good job!!! num is "+num);
        hDataset.delete();
        Dataset.delete();
        gdal.GDALDestroyDriverManager();
    }
}