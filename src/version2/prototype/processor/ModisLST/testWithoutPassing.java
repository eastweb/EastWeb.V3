package version2.prototype.processor.ModisLST;

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
        File filename=new File("/Users/fish/Desktop/dayold.tif");
        String output="/Users/fish/Desktop/y1.tif";
        String output2="/Users/fish/Desktop/y2.tif";
        gdal.AllRegister();
        Dataset hDataset = gdal.Open(filename.getPath(), gdalconstConstants.GA_ReadOnly);
        if (hDataset == null)
        {
            System.err.println("GDALOpen failed - " + gdal.GetLastErrorNo());
            System.err.println(gdal.GetLastErrorMsg());
            System.exit(1);
        }
        Dataset Dataset=gdal.GetDriverByName("GTiff").CreateCopy(output, hDataset);
        Dataset Dataset2=gdal.GetDriverByName("GTiff").CreateCopy(output, hDataset);

        int iXSize = hDataset.getRasterXSize();
        int iYSize = hDataset.getRasterYSize();
        int band1=hDataset.getRasterCount();
        System.out.println("Size is " + iXSize + ", " + iYSize+"  band number is "+band1);
        Band band = hDataset.GetRasterBand(1);

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
        for(int y=iYSize; y<iYSize; y++)
        {
            // band.ReadRaster(0, y, iXSize, 1, buf); //read data for one line
            // the pixel values stored in array[index]
            for(int x=0; x<iXSize; x++) {
                int index = y * iXSize + x;
                //filterValue(buf[index]);
                if(buf[index]<0) {
                    buf[index]=0;
                    System.out.println("bad pixel index is "+index);
                }

                System.out.print(buf[index] + ", ");
                num++;
            }
            System.out.println("\n");
        }

        synchronized (GdalUtils.lockObject) {
            Dataset.GetRasterBand(1).WriteRaster(0, 0, iXSize, iYSize,buf);
            Dataset2.GetRasterBand(1).WriteRaster(0, 0, iXSize, iYSize,buf);

        }

        System.out.println("good job!!! num is "+num);
        hDataset.delete();
        Dataset.delete();
        Dataset2.delete();
        gdal.GDALDestroyDriverManager();
    }
}