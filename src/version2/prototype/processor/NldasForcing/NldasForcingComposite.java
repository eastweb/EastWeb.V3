package version2.prototype.processor.NldasForcing;

import java.io.File;
import java.io.IOException;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.ErrorLog;
import version2.prototype.processor.Composite;
import version2.prototype.processor.ProcessData;
import version2.prototype.util.GdalUtils;

public class NldasForcingComposite extends Composite
{
    private static MonthDay hDate;
    private static double hDegree;

    private static MonthDay fDate;
    private static double fDegree;

    private int[] mBands;

    public NldasForcingComposite(ProcessData data, Boolean deleteInputDirectory) {
        super(data, deleteInputDirectory);

        mBands = data.getDataBands();

        hDate = data.getHeatingDate();
        hDegree = data.getHeatingDegree();

        fDate = data.getFreezingDate();
        fDegree = data.getFreezingDegree();
    }

    @Override
    public void composeFiles()
    {
        GdalUtils.register();
        synchronized (GdalUtils.lockObject) {

            if(!(new File(outputFolder).exists())){
                new File(outputFolder).mkdirs();
                //                try { FileUtils.forceMkdir(new File(outputFolder)); }
                //                catch (IOException e) { ErrorLog.add(Config.getInstance(), "NldasForcingComposite.composeFiles error", e); }
            }

            List<Dataset> inputDSs = new ArrayList<Dataset>();
            for (File input : inputFiles) {
                inputDSs.add(gdal.Open(input.getPath()));
            }

            for(int band : mBands)
            {
                int rasterX = inputDSs.get(0).GetRasterXSize();
                int rasterY = inputDSs.get(0).GetRasterYSize();

                int outputs = 1;

                if(band == 1) {
                    // Used to differentiate Min--Mean/DegreeDays--Max Air Temp
                    outputs = 3;
                }

                for(int output = 0; output < outputs; output++)
                {
                    ArrayList<String> prefixList = GetFilePrefix(band, output);

                    for(String prefix : prefixList)
                    {
                        File temp = new File(outputFolder + "\\" + prefix + ".tif");
                        try {
                            temp.createNewFile();
                        } catch (IOException e) {
                            ErrorLog.add(Config.getInstance(), "NldasForcingComposite.composeFiles error while creating new file.", e);
                        }

                        Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                                temp.getAbsolutePath(),
                                rasterX, rasterY,
                                1,
                                gdalconstConstants.GDT_Float32
                                );

                        outputDS.SetGeoTransform(inputDSs.get(0).GetGeoTransform());
                        outputDS.SetProjection(inputDSs.get(0).GetProjection());
                        outputDS.GetRasterBand(1).SetNoDataValue(GdalUtils.NO_VALUE);

                        outputDS.GetRasterBand(1).WriteRaster(0, 0, rasterX, rasterY, GetOutputArray(band, output, inputDSs, rasterX, rasterY, prefix));

                        outputDS.delete();
                    }
                }
            }
            for (Dataset inputDS : inputDSs) {
                inputDS.delete();
            }
        }
    }

    private ArrayList<String> GetFilePrefix(int band, int output)
    {
        ArrayList<String> prefixList = new ArrayList<String>();
        String prefix = "";

        if(band == 1)
        {
            if(output == 0) {
                prefix = "AirTemp_Min";
            }
            else if(output == 1) {
                prefixList.add("HeatingDegreeDays");
                prefixList.add("FreezingDegreeDays");
                prefix = "AirTemp_Mean";
            }
            else if(output == 2) {
                prefix = "AirTemp_Max";
            }
        }
        else if (band == 2) {
            prefix = "Humidity_Mean";
        }
        else if (band == 10) {
            prefix = "Precip_Total";
        }
        prefixList.add(prefix);

        return prefixList;
    }

    private double[] GetOutputArray(int band, int output, List<Dataset> inputDSs, int rasterX, int rasterY, String prefix)
    {
        double[] inputArray = new double[rasterX * rasterY];
        double[] outputArray = new double[rasterX * rasterY];

        if ((band == 1 && output == 1) || band == 2)
        {
            // Always take the average.
            for (Dataset inputDS : inputDSs)
            {
                inputDS.GetRasterBand(band).ReadRaster(0, 0, rasterX, rasterY, inputArray);

                for (int i = 0; i < inputArray.length; i++) {
                    // Get the proportional average for each input.
                    outputArray[i] += (inputArray[i] / inputDSs.size());
                }
            }

            if(band == 1)
            {
                // Convert the values from Kelvin to Celsius
                //for(int i = 0; i < outputArray.length; i++) {
                // Tc = Tk - 273.15
                //    outputArray[i] = outputArray[i] - 273.15;
                //}

                if(prefix.equalsIgnoreCase("HeatingDegreeDays")) {
                    outputArray = GetCumulativeHeatingDegreeDays(outputArray, prefix);
                }
                else if (prefix.equalsIgnoreCase("FreezingDegreeDays")) {
                    outputArray = GetCumulativeFreezingDegreeDays(outputArray, prefix);
                }
            }
        }
        else if(band == 1)
        {
            if(output == 0) {
                outputArray = FindMinValues(inputDSs, rasterX, rasterY);
            }
            else if(output == 2) {
                outputArray = FindMaxValues(inputDSs, rasterX, rasterY);
            }
        }
        else if(band == 10)
        {
            for (Dataset inputDS : inputDSs) {
                inputDS.GetRasterBand(band).ReadRaster(0, 0, rasterX, rasterY, inputArray);
                for (int i=0; i < inputArray.length; i++) {
                    // band 10 == precipitation hourly total, so we don't want the average but rather the total
                    // No conversion necessary because the
                    // density of water is approximately 1000 kg/m^3,
                    // so the total mass of a 1-mm layer of water covering an area of 1 m^2 is 1 kg.
                    outputArray[i] += inputArray[i];
                }
            }
        }

        return outputArray;
    }

    private double[] FindMinValues(List<Dataset> inputDSs, int rasterX, int rasterY)
    {
        double[][] inputArrays = new double[inputDSs.size()][rasterX * rasterY];
        double[] outputArray = new double[rasterX * rasterY];

        for(int index = 0; index < inputDSs.size(); index++) {
            inputDSs.get(index).GetRasterBand(1).ReadRaster(0, 0, rasterX, rasterY, inputArrays[index]);
        }

        for(int pos = 0; pos < outputArray.length; pos++)
        {
            double minVal = inputArrays[0][pos];
            for (int i = 0; i < inputDSs.size(); i++) {
                // Fill value is 9999.0
                if(inputArrays[i][pos] != 9999.0 && inputArrays[i][pos] < minVal) {
                    minVal = inputArrays[i][pos];
                }
            }
            outputArray[pos] = minVal;
        }

        //for(int i = 0; i < outputArray.length; i++) {
        // Tc = Tk - 273.15
        //outputArray[i] = outputArray[i] - 273.15;
        //}

        return outputArray;
    }

    private double[] FindMaxValues(List<Dataset> inputDSs, int rasterX, int rasterY)
    {
        double[][] inputArrays = new double[inputDSs.size()][rasterX * rasterY];
        double[] outputArray = new double[rasterX * rasterY];

        for(int index = 0; index < inputDSs.size(); index++) {
            inputDSs.get(index).GetRasterBand(1).ReadRaster(0, 0, rasterX, rasterY, inputArrays[index]);
        }

        for(int pos = 0; pos < outputArray.length; pos++)
        {
            double maxVal = inputArrays[0][pos];
            for (int i = 0; i < inputDSs.size(); i++) {
                // Fill value is 9999.
                if(inputArrays[i][pos] != 9999.0 && inputArrays[i][pos] > maxVal) {
                    maxVal = inputArrays[i][pos];
                }
            }
            outputArray[pos] = maxVal;
        }

        //for(int i = 0; i < outputArray.length; i++) {
        //    // Tc = Tk - 273.15
        //outputArray[i] = outputArray[i] - 273.15;
        //}

        return outputArray;
    }

    private double[] GetCumulativeHeatingDegreeDays(double[] meanValues, String prefix)
    {
        double[] cumulative = GetPreviousValues(prefix, hDate);

        for(int i = 0; i < meanValues.length; i++)
        {
            double previousVal = 0.0;
            if(cumulative != null) {
                previousVal = cumulative[i];
            }

            // Fill value is 9999.0
            if(meanValues[i] != 9999.0 && meanValues[i] > hDegree) {
                meanValues[i] = previousVal + (meanValues[i] - hDegree);
            } else {
                meanValues[i] = previousVal;
            }
        }

        return meanValues;
    }

    private double[] GetCumulativeFreezingDegreeDays(double[] meanValues, String prefix)
    {
        double[] cumulative = GetPreviousValues(prefix, fDate);

        for(int i = 0; i < meanValues.length; i++)
        {
            double previousVal = 0.0;
            if(cumulative != null) {
                previousVal = cumulative[i];
            }

            // Fill value is 9999.0
            if(meanValues[i] != 9999.0 && meanValues[i] < fDegree) {
                meanValues[i] = previousVal + (fDegree - meanValues[i]);
            } else {
                meanValues[i] = previousVal;
            }
        }

        return meanValues;
    }

    private double[] GetPreviousValues(String prefix, MonthDay start)
    {
        double[] cumulative = null;
        File yesterdayFile = null;

        try
        {
            int year = Integer.parseInt(inputFolder.getParentFile().getName());
            DataDate startDate = new DataDate(start.getDayOfMonth(), start.getMonthValue(), year);

            // if we're currently processing the start date,
            // we don't want to continue with the previous run of heating degree days
            if(!(inputFolder.getName().equalsIgnoreCase(String.format("%03d", startDate.getDayOfYear()))))
            {
                //Get the day before's values
                File yesterdayFolder = null;
                int currentDayOfYear = Integer.parseInt(inputFolder.getName());

                if(currentDayOfYear > 1) {
                    yesterdayFolder = new File(new File(outputFolder).getParentFile().getAbsolutePath() + File.separator
                            + String.format("%03d", (currentDayOfYear-1)));
                }
                else {
                    // Previous day would be last year and day of year = 365
                    yesterdayFolder = new File(new File(outputFolder).getParentFile().getParentFile().getAbsolutePath() + File.separator
                            + String.format("%04d", (year-1)) + File.separator + "365");
                }

                if(yesterdayFolder != null && yesterdayFolder.exists())
                {
                    for(File file : yesterdayFolder.listFiles())
                    {
                        if(file.getName().contains(prefix)) {
                            yesterdayFile = file;
                            break;
                        }
                    }
                }
            }
        }
        catch(NumberFormatException e)
        {
            ErrorLog.add(Config.getInstance(), "NldasForcingComposite.GetPreviousValues error.", e);
        }

        if(yesterdayFile != null) {
            Dataset ds = gdal.Open(yesterdayFile.getPath());
            int rasterX = ds.GetRasterXSize();
            int rasterY = ds.GetRasterYSize();

            cumulative = new double[rasterX * rasterY];
            ds.GetRasterBand(1).ReadRaster(0, 0, rasterX, rasterY, cumulative);
        }

        return cumulative;
    }
}
