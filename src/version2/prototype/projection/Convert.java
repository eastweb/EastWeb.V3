package version2.prototype.projection;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/* Modified by YL on May 31st
 * changed Covert from Interface to abstract class
 * added constructor and run method
 */
/*
 * Convert needs to be extended only when the downloaded data format cannot be read by GDAL directly
 */

public abstract class Convert {
    //locations for the input files. for this step, will only use inputFolders[0]
    private String [] inputFolders;
    private File inputFolder;
    //location for the output file
    private String outputFolder;
    // the file in the input folder
    private File [] inputFiles;

    public Convert(ProcessData data) {
        inputFolders = data.getInputFolders();
        outputFolder = data.getOutputFolder();

        //check if there is more than one input file in the given folder
        inputFolder = new File(inputFolders[0]);
        File[] listOfFiles = inputFolder.listFiles();
        assert (listOfFiles.length >= 1);
        //set the input files
        inputFiles = listOfFiles;
    }

    // run method for the scheduler
    public void run(){
        convertFile();

        // remove the input folder
        try {
            FileUtils.deleteDirectory(inputFolder);
        } catch (IOException e) {
            // TODO : write to log
            e.printStackTrace();
        }
    }

    /*Override this:
     * postcondition:
     *    Convert one downloaded file in raw format into a result file with the format that GDAL can process (TIFF)
     *    the file is written into folder specified in variable outputFolder
     *
     * Steps for the implementation:
     *   (1) Read each file (inputFile)
     *   (2) Convert it into the Tiff format
     *   (3) Write the result from (2) to outputFolder
     *   (4) repeat step (1) - (3)
     */
    abstract void convertFile();

}
