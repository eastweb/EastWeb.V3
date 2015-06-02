package version2.prototype.projection;

import java.io.File;

/* Modified by Y. L.  on June 2nd
 * changed Composite from Interface to abstract class
 * added constructor and run method
 */

// compose hourly data into a daily data
public abstract class Composite {

    //locations for the input files. for this step, will only use inputFolders[0]
    private String [] inputFolders;
    //location for the output file
    private String outputFolder;
    // the files in the input folder for composition
    private File [] inputFiles;

    public Composite(ProcessData data) {
        inputFolders = data.getInputFolders();
        outputFolder = data.getOutputFolder();

        //check if there is at least one input file in the given folder
        File inputFolder = new File(inputFolders[0]);
        File[] listOfFiles = inputFolder.listFiles();
        assert (listOfFiles.length > 1);
        //set the input files
        inputFiles = listOfFiles;
    }

    // run method for scheduler
    public void run(){
        composeFiles();
    }

    /*Override this:
     * postcondition:
     *        compose the files in the array inputFiles into a file that contains
     *        a composition of a number of input files.
     *        For example, as for a hourly NLDAS data, a result file is composed
     *        from 24 input files.
     *
     * Steps for the implementation:
     *   (1) check if there are enough number of files in the inputFiles array.(e.g. 24 for hourly NLDAS)
     *   (2) compose the files into a result file and save the result file to outputFolder
     *   (3) remove the inputFolder
     */
    public abstract void composeFiles();

}