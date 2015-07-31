package version2.prototype.processor.NldasForcing;

import java.io.File;
import java.io.IOException;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.DataDate;
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

    public NldasForcingComposite(ProcessData data) {
        super(data);

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
                try { FileUtils.forceMkdir(new File(outputFolder)); }
                catch (IOException e) { e.printStackTrace(); }
            }

            for(int band : mBands)
            {
                int outputs = 1;

                if(band == 1) {
                    outputs = 3;
                }

                for(int output = 0; output < outputs; output++)
                {
                    List<Dataset> inputDSs = new ArrayList<Dataset>();
                    ArrayList<String> prefixList = GetFilePrefix(band, output);

                    for(String prefix : prefixList)
                    {
                        File temp = null;
                        try {
                            temp = File.createTempFile(prefix,
                                    ".tif",
                                    new File(outputFolder));
                        }
                        catch (IOException e) { e.printStackTrace(); }

                        for (File input : inputFiles) {
                            inputDSs.add(gdal.Open(input.getPath()));
                        }

                        int rasterX = inputDSs.get(0).GetRasterXSize();
                        int rasterY = inputDSs.get(0).GetRasterYSize();

                        Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                                temp.getAbsolutePath(),
                                rasterX, rasterY,
                                1,
                                gdalconst.GDT_Float32
                                );

                        outputDS.SetGeoTransform(inputDSs.get(0).GetGeoTransform());
                        outputDS.SetProjection(inputDSs.get(0).GetProjection());

                        outputDS.GetRasterBand(1).WriteRaster(0, 0, rasterX, rasterY, GetOutputArray(band, output, inputDSs, rasterX, rasterY, prefix));

                        for (Dataset inputDS : inputDSs) {
                            inputDS.delete();
                        }
                        outputDS.delete();
                    }
                }
            }
        }
    }

    private ArrayList<String> GetFilePrefix(int band, int output)
    {
        ArrayList<String> prefixList = new ArrayList<String>();
        String prefix = String.format("band%02d_", band);

        if(band == 1)
        {
            if(output == 0) {
                prefix = String.format("band%02d_Min_", band);
            }
            else if(output == 1) {
                prefixList.add("HeatingDegreeDays_");
                prefixList.add("FreezingDegreeDays_");
                prefix = String.format("band%02d_Mean_", band);
            }
            else if(output == 2) {
                prefix = String.format("band%02d_Max_", band);
            }
        }
        prefixList.add(prefix);

        return prefixList;
    }

    private double[] GetOutputArray(int band, int output, List<Dataset> inputDSs, int rasterX, int rasterY, String prefix)
    {
        double[] inputArray = new double[rasterX * rasterY];
        double[] outputArray = new double[rasterX * rasterY];

        if(band == 1 && output != 1)
        {
            if(output == 0) {
                outputArray = FindMinValues(inputDSs, rasterX, rasterY);
            }
            else if(output == 2) {
                outputArray = FindMaxValues(inputDSs, rasterX, rasterY);
            }
        }
        else if(band == 2 || (band == 1 && output == 1))
        {
            for (Dataset inputDS : inputDSs)
            {
                inputDS.GetRasterBand(band).ReadRaster(0, 0, inputDS.GetRasterXSize(), inputDS.GetRasterYSize(), inputArray);

                for (int i=0; i<inputArray.length; i++) {
                    outputArray[i] += (inputArray[i] / inputDSs.size());
                }

                if(band == 1) {
                    for(int i = 0; i < outputArray.length; i++) {
                        // Tc = Tk - 273.15
                        outputArray[i] = outputArray[i] - 273.15;
                    }
                }

                if(prefix.equalsIgnoreCase("HeatingDegreeDays_")) {
                    GetCumulativeHeatingDegreeDays(outputArray, prefix);
                }
                else if (prefix.equalsIgnoreCase("FreezingDegreeDays_")) {
                    GetCumulativeFreezingDegreeDays(outputArray, prefix);
                }
            }
        }
        else if(band == 10)
        {
            for (Dataset inputDS : inputDSs)
            {
                inputDS.GetRasterBand(band).ReadRaster(0, 0, inputDS.GetRasterXSize(), inputDS.GetRasterYSize(), inputArray);
                for (int i=0; i < inputArray.length; i++)
                {
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

    private static double[] FindMinValues(List<Dataset> inputDSs, int rasterX, int rasterY)
    {
        double[][] inputArrays = new double[inputDSs.size()][rasterX * rasterY];
        double[] outputArray = new double[rasterX * rasterY];

        for(int index = 0; index < inputDSs.size(); index++) {
            inputDSs.get(index).GetRasterBand(1).ReadRaster(0, 0, rasterX, rasterY, inputArrays[index]);
        }

        for(int pos = 0; pos < outputArray.length; pos++)
        {
            double minVal = 9999;
            for (int i = 0; i < inputDSs.size(); i++) {
                // Fill value is 9999.
                if(inputArrays[i][pos] != 9999 && inputArrays[i][pos] < minVal) {
                    minVal = inputArrays[i][pos];
                }
            }
            outputArray[pos] = minVal;
        }

        for(int i = 0; i < outputArray.length; i++) {
            // Tc = Tk - 273.15
            outputArray[i] = outputArray[i] - 273.15;
        }

        return outputArray;
    }

    private static double[] FindMaxValues(List<Dataset> inputDSs, int rasterX, int rasterY)
    {
        double[][] inputArrays = new double[inputDSs.size()][rasterX * rasterY];
        double[] outputArray = new double[rasterX * rasterY];

        for(int index = 0; index < inputDSs.size(); index++) {
            inputDSs.get(index).GetRasterBand(1).ReadRaster(0, 0, rasterX, rasterY, inputArrays[index]);
        }

        for(int pos = 0; pos < outputArray.length; pos++)
        {
            double maxVal = -9999;
            for (int i = 0; i < inputDSs.size(); i++) {
                // Fill value is 9999.
                if(inputArrays[i][pos] != 9999 && inputArrays[i][pos] > maxVal) {
                    maxVal = inputArrays[i][pos];
                }
            }
            outputArray[pos] = maxVal;
        }

        for(int i = 0; i < outputArray.length; i++) {
            // Tc = Tk - 273.15
            outputArray[i] = outputArray[i] - 273.15;
        }

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

            // Fill value is 9999.
            if(meanValues[i] > hDegree && meanValues[i] != 9999) {
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

            // Fill value is 9999.
            if(meanValues[i] < fDegree && meanValues[i] != 9999) {
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
            DataDate date = new DataDate(start.getDayOfMonth(), start.getMonthValue(), Integer.parseInt(inputFolder.getParent()));
            if(!(inputFolder.getName().equalsIgnoreCase(String.format("%03d", date.getDayOfYear())) &&
                    inputFolder.getParent().equalsIgnoreCase(String.format("%04d", date.getYear()))))
            {
                //Get the day before's values
                File yesterdayFolder = null;

                int currentDayOfYear = Integer.parseInt(inputFolder.getName());
                if(currentDayOfYear > 1) {
                    yesterdayFolder = new File(inputFolder.getParentFile().getAbsolutePath() + File.pathSeparator
                            + String.format("%03d", (currentDayOfYear-1)));
                }
                else
                {
                    // Previous day would be last year day of year = 365
                    yesterdayFolder = new File(inputFolder.getParentFile().getParentFile().getAbsolutePath() + File.pathSeparator
                            + String.format("%04d", Integer.parseInt(inputFolder.getParent())-1) + File.pathSeparator + "365");
                }

                if(yesterdayFolder != null && yesterdayFolder.exists())
                {
                    for(File file : yesterdayFolder.listFiles())
                    {
                        if(file.getName().contains(prefix)) {
                            yesterdayFile = file;
                        }
                    }
                }
            }
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
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