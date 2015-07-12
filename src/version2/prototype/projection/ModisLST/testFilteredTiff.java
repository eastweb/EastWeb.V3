package version2.prototype.projection.ModisLST;


import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
public class testFilteredTiff{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String fileName_tif = "/Users/fish/Desktop/x.tif";// x.filter is the filterd tif file
        gdal.AllRegister();
        Dataset hDataset = gdal.Open(fileName_tif, gdalconstConstants.GA_ReadOnly);
        if (hDataset == null)
        {
            System.err.println("GDALOpen failed - " + gdal.GetLastErrorNo());
            System.err.println(gdal.GetLastErrorMsg());
            System.exit(1);
        }
        Driver hDriver = hDataset.GetDriver();
        System.out.println("Driver: " + hDriver.getShortName() + "/" + hDriver.getLongName());
        int iXSize = hDataset.getRasterXSize();
        int iYSize = hDataset.getRasterYSize();
        System.out.println("Size is " + iXSize + ", " + iYSize);
        Band band = hDataset.GetRasterBand(1);

        int buf[] = new int[iXSize*iYSize];
        int num=0;
        band.ReadRaster(0, 0, iXSize, iYSize, buf);
        for(int i=0; i<iYSize; i++)
        {
            for(int j=0; j<iXSize; j++) {
                System.out.print(buf[i*iXSize+j] + ", ");
                num++;
            }
            System.out.println("\n");
        }
        hDataset.delete();
        System.out.println("num is "+num);
        gdal.GDALDestroyDriverManager();
    }


}
