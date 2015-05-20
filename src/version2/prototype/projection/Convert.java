package version2.prototype.projection;

import java.io.File;

import version2.prototype.ConfigReadException;
import version2.prototype.ProjectInfo;
import version2.prototype.DirectoryLayout;

public abstract class Convert {

    /* Modified by YL on May 19th
     * changed Covert from Interface to abstract class
     * added constructor and run method
     */

    /*
     * The input files contains ALL the archives from the download process.
     */
    private File [] inputFiles;
    private File outputFolder;
    private ProjectInfo pInfo;

    public Convert(ProcessData data) throws ConfigReadException{
        pInfo = data.projectInfo;
        outputFolder = DirectoryLayout.getRSWorkingDirectory(pInfo);
    }

    // run method for the scheduler
    public void run(){
        convertFiles();
    }

    // check if there is at least one input file
    protected void setInputFiles(File[] inputFiles) {
        assert (inputFiles.length > 0);
        this.inputFiles = inputFiles;
    }

    protected void saveOutputFile() {

    }

    /*Override this:
     * postcondition:
     *    Convert each downloaded file in raw format into a result file with the format that GDAL can process (TIFF)
     *    reprojection also happen in this step to reproject the data into the same projection of the shapefile that the usr inputs.
     *
     * Steps for the implementation:
     *   (1) get the files in the download folder (get the download folder from DirectoryLayer.java)
     *       and call setInputFiles to make the inputFiles reference to the downloaded files
     *   (2) fetch each file in inputFiles and reproject and convert it to tiff format. Save the converted
     *       file into the "working folder" (outputFolder).
     *       call method
     *            GdalUtils.project(inFile, projectInfo, outFile);
     *       in version2.prototype.util.GdalUtils;
     *   (3) repeat step (2) until all the files in the download folder for the plugin are processed
     *   (4) If the user requests to keep intermediate files (information can be found in ProjectInfo), save the output files to the "Reprojected" folder that is declared in DirectoryLayer
     */
    abstract void convertFiles();

}
