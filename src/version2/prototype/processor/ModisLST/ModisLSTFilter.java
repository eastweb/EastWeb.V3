package version2.prototype.processor.ModisLST;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import version2.prototype.processor.Filter;
import version2.prototype.processor.ProcessData;
import version2.prototype.util.GdalUtils;

//Rewritten by Yi Liu

public class ModisLSTFilter extends Filter{

    public ModisLSTFilter(ProcessData data) {
        super(data);
    }

    @Override
    protected double filterValue(double value)
    {
        return 0;
    }

    @Override
    protected void filterByQCFlag(String qcLevel) {
        // filter pixel by pixel

        GdalUtils.register();
        synchronized (GdalUtils.lockObject)
        {
            // order: day(band1), night(band5), day_qc(band2), night_qc(band6)
            Band [] bands = new Band[4];

            Dataset inputDS = null;
            for (File mInput : inputFiles)   // after Mozaic, they should be in separate bands
            {
                String fName = mInput.getName();
                inputDS = gdal.Open(fName);

                switch (FilenameUtils.getBaseName(fName))
                {
                case "band1" :
                    bands[0] = inputDS.GetRasterBand(1);
                    break;
                case "band5":
                    bands[1] = inputDS.GetRasterBand(1);
                    break;
                case "band2":
                    bands[2] = inputDS.GetRasterBand(1);
                    break;
                case "band6":
                    bands[3] = inputDS.GetRasterBand(1);
                    break;
                }
            }

            int xSize = bands[0].getXSize();
            int ySize = bands[0].getYSize();
            int totalSize = xSize * ySize;

            ArrayList<int []> arrays = new ArrayList<int []>();

            for (int s = 0; s < 4; s++)
            {   // store order:  day, night, day_qc, night_qc
                arrays.add(new int[totalSize]);
                bands[s].ReadRaster(0, 0, xSize, ySize, arrays.get(s));
            }

            // name the output file as the same as the input's plus "day"
            Dataset outputDS =
                    gdal.GetDriverByName("GTiff").Create(File.separator + "day.tif",
                            xSize, ySize, 1);

            // filter day band, and write to the day.tif file
            outputDS.GetRasterBand(1).WriteRaster(0, 0, xSize, ySize,
                    filterWithFlags(arrays.get(0), arrays.get(2), qcLevel));

            // name the output file as the same as the input's plus "night"
            outputDS =
                    gdal.GetDriverByName("GTiff").Create(File.separator + "night.tif",
                            xSize, ySize, 1);

            // filter night band, and write to the night.tif file
            outputDS.GetRasterBand(1).WriteRaster(0, 0, xSize, ySize,
                    filterWithFlags(arrays.get(1), arrays.get(3), qcLevel));

            outputDS.delete();
            inputDS.delete();
        }
    }

    /*
     * bit no:  7  6 5  4  3  2   1  0 ( I assume it uses Most Significant bit order)
    X means we will not consider the value on the bit
    (1) Highest: Accept only high quality data
    XX XX XX 00   (LST produced, good quality, not necessary to examine detailed QA)
    (2) Moderate: Accept only moderate and high quality data
    everything in (1) plus the following:
    00 XX XX 01   (Average LST error <= 1 K )
    01 XX XX 01   (Average LST error <= 2 K)
    (3) Low: Accept only low, moderate, and high quality data
    everything in (2) plus the following:
    10 XX XX 01   (Average LST error <= 3K)
    (4) No QC screening: accept all data
    everything in (3) plus the following:
    11 XX XX 01 (Average LST error > 3K)
     */

    private int [] filterWithFlags(int [] dBand, int [] qBand, String qcLevel)
    {
        // "HIGHEST","MODERATE","LOW","NOSCREENING"

        int size = dBand.length;
        int bitMask = 0;
        String bits1 = null;
        String bits2 = null;

        for (int i = 0; i < size; i++)
        {
            //read the every pixel of the qBand and
            //compare it with the qcLevel to set the pixel of dBand
            switch(qcLevel)
            {
            case "NOSCREENING": // last two bits are 00 or 01
                bits1 = Integer.toBinaryString(qBand[i]).substring(6, 7);
                if (!((bits1.equals("00") || bits1.equals("01"))))
                {
                    dBand[i] = GdalUtils.NoValue;
                }
            case "LOW":
                // last two bits are 01, first two except "11", use mask 0xC1F
                // last two are "00" (covered by the above case)
                bitMask = 0XC1F;
                int r = bitMask ^ qBand[i];
                bits1 = Integer.toBinaryString(r).substring(0, 1);
                bits2 = Integer.toBinaryString(r).substring(6, 7);
                if (bits1.equals("00") && (bits2.equals("00")))
                {
                    dBand[i] = GdalUtils.NoValue;
                }
            case "MODERATE":
                // last two bits are 01, first two are 00  or 01
                // last two are "00" (covered by the above case)
                bits1 = Integer.toBinaryString(qBand[i]).substring(0, 1);
                bits2 = Integer.toBinaryString(qBand[i]).substring(6, 7);
                if  (bits2.equals("01"))
                {
                    if (!((bits1.equals("01")) || (bits1.equals("00"))))
                    {
                        dBand[i] =  GdalUtils.NoValue;
                    }
                }
            case "HIGHEST":
                // last two should be "00"
                bits2 = Integer.toBinaryString(qBand[i]).substring(6, 7);
                if  (!bits2.equals("00"))
                {
                    dBand[i] = GdalUtils.NoValue;
                }
                break;
            }
        }

        return dBand;

    }
}
