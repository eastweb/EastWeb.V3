package version2.prototype.projection;

import java.io.File;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import version2.prototype.util.GdalUtils;

// Modified and commented by Y.L. on June 2nd 2015

// screen the files and filter out the "bad" data by given value or qcFlag in the data product
public abstract class Filter {

    /* locations for the input files.
     * inputFolders[0] stores the files to be filtered
     * inputFolders[1] stores the QC file(s) if there is any
     */
    String [] inputFolders;

    //location for the output file
    private String outputFolder;
    // the files in the input folder
    private File [] inputFiles;
    // the qcFiles in the inputFolders[1];
    private File [] qcFiles = null;
    // qc level
    private String qcLevel;
    private int [] qcBands;

    public Filter(ProcessData data) {

        /* locations for the input files.
         * inputFolders[0] stores the files to be filtered
         * inputFolders[1] stores the QC file(s) if there is any
         */
        inputFolders = data.getInputFolders();

        //check if there is ate least one input file in the given folder
        File inputFolder1 = new File(inputFolders[0]);
        File[] listOfFiles = inputFolder1.listFiles();
        assert (listOfFiles.length >= 1);
        //set the input files
        inputFiles = listOfFiles;

        //check if there is ate least one input file in the given folder
        File inputFolder2 = new File(inputFolders[1]);
        File[] listOfFiles2= inputFolder2.listFiles();

        if (listOfFiles2.length >= 1) {
            qcFiles = listOfFiles2;
        }

        // set qcLevel
        qcLevel = data.getQCLevel();

        // set QC bands
        qcBands = data.getQCBands();

        outputFolder = data.getOutputFolder();
    }

    // run method for the scheduler
    public void run(){
        if (qcBands != null) {
            try {
                filterByValue();
            } catch (Exception e) {
                // TODO :write into log
                e.printStackTrace();
            }
        } else {
            filterByQCFlag(qcLevel);
        }

        // TODO: delete the input folders
    }

    public void filterByValue() throws Exception {
        GdalUtils.register();

        synchronized (GdalUtils.lockObject) {
            for (File mInput : inputFiles){

                Dataset inputDS = gdal.Open(mInput.getPath());
                assert(inputDS.GetRasterCount() == 1);

                Dataset outputDS = createOutput(inputDS);
                double[] array = new double[outputDS.GetRasterXSize()];

                for (int y=0; y<outputDS.GetRasterYSize(); y++) {
                    outputDS.GetRasterBand(1).ReadRaster(0, y, outputDS.GetRasterXSize(), 1, array);

                    for (int x=0; x<outputDS.GetRasterXSize(); x++) {
                        array[x] = filterValue(array[x]);
                    }

                    synchronized (GdalUtils.lockObject) {
                        outputDS.GetRasterBand(1).WriteRaster(0, y, outputDS.GetRasterXSize(), 1, array);
                    }
                }

                inputDS.delete();
                outputDS.delete();
            }
        }
    }

    protected Dataset createOutput(Dataset inputDS) {
        return gdal.GetDriverByName("GTiff").CreateCopy(outputFolder, inputDS);
    }

    /*Override this:
     * @Param : value:  the given value
     *
     * postcondition:
     *   return the result value after applying filtering strategy on the given value
     */
    protected abstract double filterValue(double value);

    /*Override this:
     * use the qcFiles to filter the inputFiles based on the given qcLevel by the end user
     * the Set of the QC levels are defined in the plugin metadata.
     */
    protected abstract double filterByQCFlag(String qcLevel);
}
