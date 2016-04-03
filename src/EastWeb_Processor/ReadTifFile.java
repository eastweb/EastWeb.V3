package EastWeb_Processor;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import Utilies.GdalUtils;

public class ReadTifFile
{
    public static void main(String args[])
    {
        GdalUtils.register();

        synchronized (GdalUtils.lockObject)
        {
            //String tifFile = "D:\\testProjects\\TW\\Projects\\Project_TW\\TRMM3B42RT\\Processor\\Output\\2014\\077\\3B42RT_daily.2014.03.18.tif";

            // String tifFile = "D:\\testProjects\\TW_trmmRT\\Projects\\Project_TW\\TRMM3B42RT\\Processor\\Output\\2014\\077\\3B42RT_daily.2014.03.18.tif";
            // String tifFile = "D:\\testProjects\\TW_trmmRT\\Projects\\Project_TW\\TRMM3B42RT\\Indices\\Output\\2014\\077\\TRMM3B42RTIndex.tif";
            //String tifFile = "C:\\Users\\yi.liu\\Desktop\\tw_618\\indices\\trmmrt\\2014\\077\\TW_DIS_F_P_Dis_REGION\\trmmrt.tif";

            //String tifFile = "D:\\testProjects\\TW3\\Projects\\Project_TW3\\ModisLST\\Processor\\Temp\\Mask\\2014\\081\\day.tif";

            String tifFile = "D:\\testProjects\\TW3\\Projects\\Project_TW3\\ModisLST\\Processor\\Output\\2014\\081\\day.tif";

            Dataset baseDS= gdal.Open(tifFile);

            Band baseBand = baseDS.GetRasterBand(1);

            int xSize = baseBand.GetXSize();
            int ySize = baseBand.GetYSize();

            double[] baseArr = new double[xSize * ySize];
            baseBand.ReadRaster(0, 0, xSize, ySize, baseArr);

            int count = 0;
            int count_nodata = 0;

            for (int i = 0; i < 50000; i++)
            {
                if ((baseArr[i] !=0 ) && (baseArr[i] != -9999) )
                {//System.out.println(baseArr[i]);
                    count++;
                }

                if (baseArr[i] < 0 )
                {System.out.println(baseArr[i]);
                count_nodata++;
                }
            }
            System.out.println("count : " + count);
            System.out.println("count no data : " + count_nodata);


            baseDS.delete();

        }
    }
}
