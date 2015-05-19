package version2.prototype.projection;

import java.io.File;

import version2.prototype.ProjectInfo;


public abstract class Convert {

    /* Modified by YL on May 19th
     * changed Covert from Interface to abstract class
     * added constructor and run method
     */

    /* the input files fetched from the "working folder" for processing.
     * The input files contains ALL the archives from the download process.
     */
    private File[] inputArray;
    private ProjectInfo pInfo;

    public Convert(ProcessData data) {
        for (File f : data.inputArray){
            assert(f.exists());
        }
        pInfo = data.projectInfo;
    }

    // run method for scheduler
    public void run(){
        convertFiles();
    }

    /*Override this:
     * postcondition:
     *        Convert each the input files (in inputArray) into a result file in the
     *        requested projection as specified in the ProjectInfo
     *
     * Steps for the implementation:
     *   (1) if the intermediate files are requested to be stored, save the the input files
     *       into a designated location.
     *   (2) fetch each file and convert it to the requested projection. Save the converted
     *       file into the "working folder".
     *       the requested projection can be retrieved from pInfo.
     *   (3) repeat step (2) until all the files in the "working folder" are processed
     *   (4) remove the original input files. Now the result files will be "inputArray"
     *       for the next processing stage
     */
    abstract void convertFiles();
}
