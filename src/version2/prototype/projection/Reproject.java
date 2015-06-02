package version2.prototype.projection;

import java.io.File;

import version2.prototype.ConfigReadException;
import version2.prototype.Projection;
import version2.prototype.util.GdalUtils;

/* Author: Y. L.
 *
 * Reproject the file from original projection to the specified projection
 */

public class Reprojection {
    //locations for the input files. for this step, will only have one folder
    private String [] inputFolders;
    //location for the output file
    private String outputFolder;
    // the file in the input folder
    private File [] inputFiles;
    private String shapefile;
    private Projection projection;

    public Reprojection(ProcessData data) {
        inputFolders = data.getInputFolders();
        outputFolder = data.getOutputFolder();
        shapefile = data.getShapefile();
        projection = data.getProjection();

        //check if there is at least one input file in the given folder
        File inputFolder = new File(inputFolders[0]);
        File[] listOfFiles = inputFolder.listFiles();
        assert (listOfFiles.length >1);
        //set the input files
        inputFiles = listOfFiles;
    }

    // run method for the scheduler
    public void run(){
        reprojection();
    }

    /* (1) reproject all the input Files and save them to the outputFolder
     * (2) remove the inputFolder
     */
    private void reprojection()  {
        for (File f : inputFiles) {
            String fileName = f.getName();
            File outputFile = new File (outputFolder + fileName);
            // reproject
            GdalUtils.project(f, shapefile, projection, outputFile);

            // delete the input file after processing
            f.delete();
        }

        // remove the inputFolder
        (new File(outputFolder)).delete();

    }
}
