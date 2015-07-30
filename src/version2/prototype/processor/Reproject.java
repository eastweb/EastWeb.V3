package version2.prototype.processor;

import java.io.File;

import org.apache.commons.io.FileUtils;

import version2.prototype.Projection;
import version2.prototype.util.GdalUtils;

/* Author: Y. L.
 *
 * Reproject the file from original projection to the specified projection
 */

public class Reproject {
    //locations for the input files. for this step, will only have one folder
    private String [] inputFolders;
    //location for the output file
    private String outputFolder;
    private File inputFolder;
    // the files in the input folder
    private File [] inputFiles;
    private String shapefile;
    private Projection projection;

    public Reproject(ProcessData data) {
        inputFolders = data.getInputFolders();
        outputFolder = data.getOutputFolder();
        shapefile = data.getShapefile();
        projection = data.getProjection();

        //check if there is at least one input file in the given folder
        inputFolder = new File(inputFolders[0]);
        File[] listOfFiles = inputFolder.listFiles();
        assert (listOfFiles.length >1);
        //set the input files
        inputFiles = listOfFiles;
    }

    // run method for the scheduler
    public void run() throws Exception{

        //create outputDirectory
        File outputDir = new File(outputFolder);
        if (!outputDir.exists())
        {   FileUtils.forceMkdir(outputDir); }

        reprojectFiles();

        // remove the input folder
        FileUtils.deleteDirectory(inputFolder);

    }

    // reproject all the input Files and save them to the outputFolder
    private void reprojectFiles()  {
        for (File f : inputFiles) {
            String fileName = f.getName();
            File outputFile = new File (outputFolder, fileName);
            // reproject
            GdalUtils.project(f, shapefile, projection, outputFile);
        }

    }
}
