package version2.prototype.indices;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.util.GdalUtils;
import version2.prototype.util.GeneralListener;
import version2.prototype.util.GeneralUIEvent;

/**
 * Indicies Framework;
 * User will have to use this framework for each indices
 * @author sufi
 *
 */
public abstract class IndicesFramework implements IndexCalculator {
    private static final float OUTPUT_NODATA = Float.intBitsToFloat(0xff7fffff);

    private GeneralUIEvent event;
    private File[] mInputFiles;
    private File mOutputFile;

    // constructor which takes the listener to talk back to the UI
    // listener will come from the scheduler
    public IndicesFramework(GeneralListener l)
    {
        event = new GeneralUIEvent();
        event.addListener(l);
    }

    // set input file property
    protected void setInputFiles(File[] inputFiles) {
        assert (inputFiles.length > 0);
        mInputFiles = inputFiles;
    }

    // set output file property
    protected void setOutputFile(File outputFile) {
        mOutputFile = outputFile;
    }

    // creates the output file
    private Dataset createOutput(Dataset[] inputs) throws IOException {
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
                System.out.println("index calculate input files name: "+mInputFiles[i].getPath());
                inputs[i] = gdal.Open(mInputFiles[i].getPath());
            }

            Dataset outputDS = createOutput(inputs);
            process(inputs, outputDS);// Process the output and inputs

            // Calculate statistics
            for (int i = 1; i <= outputDS.GetRasterCount(); i++) {
                Band band = outputDS.GetRasterBand(i);

                band.SetNoDataValue(OUTPUT_NODATA);
                band.ComputeStatistics(false);
            }

            // Close and flush output and inputs
            for (Dataset input : inputs) {
                input.delete();
            }

            outputDS.delete();
        }
        event.fire(String.format("%s is Complete", className()), 100, className());
    }

    protected void process(Dataset[] inputs, Dataset output) throws Exception {
        int xSize = inputs[0].GetRasterXSize();
        int ySize = inputs[0].GetRasterYSize();
        double[][] inputsArray = new double[inputs.length][xSize];
        double[] outputArray = new double[xSize];

        for (int y = 0; y < ySize; y++) {
            for (int i = 0; i < inputs.length; i++) {
                inputs[i].GetRasterBand(1).ReadRaster(0, y, xSize, 1, inputsArray[i]);
            }

            for (int x = 0; x < xSize; x++) {
                double[] values = new double[inputs.length];

                for (int i = 0; i < inputs.length; i++) {
                    values[i] = inputsArray[i][x];
                }

                outputArray[x] = calculatePixelValue(values);
            }

            output.GetRasterBand(1).WriteRaster(0, y, xSize, 1, outputArray);
        }
    }

    protected abstract double calculatePixelValue(double[] values) throws Exception;

    protected abstract String className();
}
