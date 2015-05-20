package version2.prototype.projection;

import java.io.File;

/* Modified by YL on May 15th
 * changed Composite from Interface to abstract class
 * added constructor and run method
 */

public abstract class Composite {

    /* the input files fetched from the "working folder" for processing.
     * The input files contains ALL the archives from the download process.
     */
    private File[] inputFiles;

    public Composite(ProcessData data) {
        assert(data.inputFiles.length > 0);
        inputFiles = data.inputFiles;
    }

    // run method for scheduler
    public void run(){
        composeFiles();
    }

    /*Override this:
     * postcondition:
     *        compose the input files (inputArray) into a collection of files
     *        so that each file contains a composition of a number of input files.
     *        For example, as for a 3-hour TRMM data, each result file is composed
     *        from 8 input files
     *
     * Steps for the implementation:
     *   (1) if the intermediate files are requested to be stored, save the the input files
     *       into a designated location.
     *   (2) fetch the number of files (e.g. 8 for 3-hour TRMM) and compose them into
     *       a result file and save the result file.
     *       the filename should follow the name convention to contain the time.
     *   (3) repeat step (2) until all the files in the "working folder" are processed
     *   (4) remove the original input files. Now the result files will be "inputArray"
     *       for the next processing stage
     */
    public abstract void composeFiles();

}