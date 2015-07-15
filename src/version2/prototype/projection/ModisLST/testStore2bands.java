package version2.prototype.projection.ModisLST;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

public class testStore2bands {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        //Create a new dataset with this driver

        String filename="/Users/fish/Desktop/day.tif";
        String filename2="/Users/fish/Desktop/night.tif";
        gdal.AllRegister();
        Dataset oldDS1=gdal.Open(filename,gdalconstConstants.GA_ReadOnly);
        Dataset oldDS2=gdal.Open(filename2,gdalconstConstants.GA_ReadOnly);
        String outputFolder="/Users/fish/Desktop/love.tif";

        System.out.println("hello!");
        int xsize=oldDS1.getRasterXSize();
        int ysize=oldDS1.getRasterYSize();
        int []array=new int[xsize*ysize];
        int []Array=new int[xsize*ysize];
        Dataset newDS=gdal.GetDriverByName("GTiff").Create(outputFolder, xsize, ysize, 2);

        System.out.println("band count is:"+newDS.getRasterCount());
        Band band=oldDS1.GetRasterBand(1);
        Band band2=oldDS2.GetRasterBand(1);
        int returnNum=band.ReadRaster(0, 0, xsize, ysize, array);
        int returnA=band2.ReadRaster(0, 0, xsize, ysize, Array);
        System.out.println(returnNum+" "+returnA);
        newDS.GetRasterBand(1).WriteRaster(0, 0, xsize, ysize, array);
        newDS.GetRasterBand(2).WriteRaster(0, 0, xsize, ysize, Array);


        int num=0;
        System.out.println("haha");
        for(int i=ysize-1; i<ysize; i++)
        {
            for(int j=0; j<xsize; j++) {
                //System.out.print(array[i*xsize+j] + ", ");
                System.out.print(Array[i*xsize+j] + ", ");
                //  System.out.println("/n");
                num++;
            }
            System.out.println("\n");
        }
        oldDS1.delete();
        oldDS2.delete();
        newDS.delete();
        System.out.println("num is "+num);
        gdal.GDALDestroyDriverManager();

    }

}
