package version2.prototype.indices;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.util.GdalUtils;

/**
 * Indicies Framework;
 * User will have to use this framework for each indices
 * @author sufi
 *
 */
public abstract class IndicesFramework implements IndexCalculator {
    protected static final float OUTPUT_NODATA = Float.intBitsToFloat(0xff7fffff);

    protected File[] mInputFiles;
    protected File mOutputFile;

    // constructor which takes the listener to talk back to the UI
    // listener will come from the scheduler
    public IndicesFramework()
    { }

    // set input file property
    public void setInputFiles(File[] inputFiles) {
        assert (inputFiles.length > 0);
        mInputFiles = new File[inputFiles.length];
        for(int i=0; i < inputFiles.length; i++) {
            mInputFiles[i] = new File(inputFiles[i].getPath());
        }
    }

    // set output file property
    public void setOutputFile(File outputFile) {
        mOutputFile = outputFile;
    }

    // creates the output file
    protected Dataset createOutput(Dataset[] inputs) throws IOException {
        //        System.out.println("inputs 0 is  "+inputs[0]);
        //        System.out.println(inputs[0].GetRasterXSize());
        //        System.out.println(inputs[0].GetRasterYSize());


        FileUtils.forceMkdir(mOutputFile.getParentFile());
        Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                mOutputFile.getPath(),
                inputs[0].GetRasterXSize(),
                inputs[0].GetRasterYSize(),
                1,
                gdalconst.GDT_Float32);

        //        System.out.println("output is  "+outputDS);
        outputDS.SetGeoTransform(inputs[0].GetGeoTransform());
        outputDS.SetProjection(inputs[0].GetProjection());
        outputDS.SetMetadata(inputs[0].GetMetadata_Dict());

        return outputDS;
    }

    @Override
    public void calculate() throws Exception {
        GdalUtils.register();

        synchronized (GdalUtils.lockObject) {
            // Setup the output and inputs
            Dataset[] inputs = new Dataset[mInputFiles.length];

            for (int i = 0; i < mInputFiles.length; i++) {
                Dataset temp = gdal.Open(mInputFiles[i].getPath());
                inputs[i] = temp;
            }

            /*   System.out.println("ind:  calculate(): " + mInputFiles[0].getPath());
            Band b1 = inputs[0].GetRasterBand(1);
            int xSize = b1.GetXSize();
            int ySize = b1.getYSize();
            double [] myArr = new double[xSize * ySize];
            b1.ReadRaster(0,0,xSize, ySize, myArr );

            for (int i = 0; i < xSize*ySize; i++)
            {
                if ((myArr[i] > 0.0) && (myArr[i] < 32767.0))
                {System.out.println(myArr[i]);}
            }
             */

            Dataset outputDS = createOutput(inputs);
            process(inputs, outputDS);// Process the output and inputs

            // Calculate statistics
            Band band = outputDS.GetRasterBand(1);
            //            band.SetNoDataValue(OUTPUT_NODATA);
            band.SetNoDataValue(GdalUtils.NO_DATA);
            band.ComputeStatistics(false);

            // Close and flush output and inputs
            for (Dataset input : inputs) {
                input.delete();
            }

            outputDS.delete();
        }
        //event.fire(String.format("%s is Complete", className()), 100, className());
    }

    protected void process(Dataset[] inputs, Dataset output) throws Exception {
        int xSize = inputs[0].GetRasterXSize();
        int ySize = inputs[0].GetRasterYSize();
        double[][] inputsArray = new double[inputs.length][xSize * ySize];
        double[] outputArray = new double[xSize * ySize];

        // Read in all of the data for each input
        for (int i = 0; i < inputs.length; i++) {
            inputs[i].GetRasterBand(1).ReadRaster(0, 0, xSize, ySize, inputsArray[i]);
        }

        // For all of the data in each array, calculate the pixel value
        for (int x = 0; x < (xSize*ySize); x++) {
            double[] values = new double[inputs.length];

            for (int i = 0; i < inputs.length; i++) {
                values[i] = inputsArray[i][x];
            }

            outputArray[x] = calculatePixelValue(values);
        }

        // Write the whole array to the output
        if(output.GetRasterBand(1).WriteRaster(0, 0, xSize, ySize, outputArray) == 3) {
            throw new Exception("Error when writing raster during IndicesFramework process.");
        }
    }

    protected abstract double calculatePixelValue(double[] values) throws Exception;

    protected abstract String className();
}