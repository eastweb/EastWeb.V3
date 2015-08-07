package version2.prototype.processor;

import java.time.LocalDate;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import version2.prototype.util.GdalUtils;

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

            String tifFile = "D:\\project\\Noah_b19_utm14.tif";
            //String tifFile = "D:\\testProjects\\Projects\\Project_EA\\NldasNOAH\\Processor\\Temp\\clip\\2015\\155\\Band20.tif";

            Dataset baseDS= gdal.Open(tifFile);

            Band baseBand = baseDS.GetRasterBand(1);

            int xSize = baseBand.GetXSize();
            int ySize = baseBand.GetYSize();

            double[] baseArr = new double[xSize * ySize];
            baseBand.ReadRaster(0, 0, xSize, ySize, baseArr);

            int count = 0;
            for (int i = 0; i < xSize*ySize; i++)
            {
                if (baseArr[i] > 0.0)
                {System.out.println(baseArr[i]);
                count++;
                }
            }
            System.out.println("count : " + count);


            baseDS.delete();

        }
    }
}
