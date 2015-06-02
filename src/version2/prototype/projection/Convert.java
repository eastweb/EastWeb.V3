package version2.prototype.projection;

import java.io.File;

/* Modified by YL on May 31st
 * changed Covert from Interface to abstract class
 * added constructor and run method
 */
/*
 * Convert needs to be extended only when the downloaded data format cannot be read by GDAL directly
 */

public abstract class Convert {
    //locations for the input files. for this step, will only have one folder
    private String [] inputFolders;
    //location for the output file
    private String outputFolder;
    // the file in the input folder
    private File inputFile;

    public Convert(ProcessData data) {
        inputFolders = data.getInputFolders();
        outputFolder = data.getOutputFolder();

        //check if there is one input file in the given folder
        File inputFolder = new File(inputFolders[0]);
        File[] listOfFiles = inputFolder.listFiles();
        assert (listOfFiles.length == 1);
        //set the input files
        inputFile = listOfFiles[0];
    }

    // run method for the scheduler
    public void run(){
        convert();
    }

    /*Override this:
     * postcondition:
     *    Convert one downloaded file in raw format into a result file with the format that GDAL can process (TIFF)
     *    the file is written into folder specified in variable outputFolder
     *
     * Steps for the implementation:
     *   (1) Read the file (inputFile)
     *   (2) Convert it into the Tiff format
     *   (3) Write the result from (2) to outputFolder
     *   (4) Remove the inputFolder
     */
    abstract void convert();

}
