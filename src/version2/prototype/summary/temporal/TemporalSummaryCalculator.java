package version2.prototype.summary.temporal;

import java.io.File;
import java.util.ArrayList;

import version2.prototype.DataDate;
import version2.prototype.util.DataFileMetaData;

/**
 * Calculates the temporal summary for given raster files using a shared stored list of files to create composites from.
 *
 * @author michael.devos
 *
 */
public class TemporalSummaryCalculator {
    private String workingDir;
    private String projectName;
    private String pluginName;
    private File inRasterFile;
    private DataDate inDataDate;
    private int daysPerInputData;
    private TemporalSummaryRasterFileStore fileStore;
    private InterpolateStrategy intStrategy;
    private MergeStrategy mergeStrategy;

    /**
     * Creates a TemporalSummaryCalculator. Uses a shared TemporalSummaryRasterFileStore and breaks apart and combines files depending on the values for
     * daysPerInputData and daysPerOutputData.
     *
     * @param projectName  - name of current project
     * @param workingDir  - path to current working directory
     * @param pluginName  - name of current plugin
     * @param inRasterFile  - File object of input raster file
     * @param inDataDate  - DataDate object associated with inRasterFile
     * @param daysPerInputData  - number of days the inRasterFile contains data for
     * @param daysPerOutputData  - number of days the output raster file should contain in order to be written
     * @param intStrategy  - interpolation strategy (method for splitting apart data files into multiple days if they are of more than 1)
     * @param mergeStrategy  - merge strategy for combining multiple files into a single one representing more days than any single file
     * @param fileStore  - common storage object to hold files waiting to be merged together into a single composite
     */
    public TemporalSummaryCalculator(String workingDir, String projectName, String pluginName, File inRasterFile, DataDate inDataDate,
            int daysPerInputData, TemporalSummaryRasterFileStore fileStore, InterpolateStrategy intStrategy, MergeStrategy mergeStrategy) {
        this.workingDir = workingDir;
        this.projectName = projectName;
        this.pluginName = pluginName;
        this.inRasterFile = inRasterFile;
        this.inDataDate = inDataDate;
        this.daysPerInputData = daysPerInputData;
        this.fileStore = fileStore;
        this.intStrategy = intStrategy;
        this.mergeStrategy = mergeStrategy;
    }

    /**
     * Run temporal summary calculation.
     *
     * @return metadata of created raster file if composite created, already existing raster file if no temporal calculation needed, or null if additional files
     * required for composite to be created
     * @throws Exception
     */
    public DataFileMetaData calculate() throws Exception {
        DataFileMetaData output = null;
        ArrayList<File> inputFileSet = new ArrayList<File>();

        // Check if interpolation is needed
        if(fileStore.compStrategy.getDaysInThisComposite(inDataDate.getCalendar()) % daysPerInputData == 0) {
            inputFileSet.add(inRasterFile);
        } else {
            // If the given interpolation strategy is be able to complete the required composites..
            if(fileStore.compStrategy.getDaysInThisComposite(inDataDate.getCalendar()) % intStrategy.GetResultingNumOfFiles() == 0) {
                inputFileSet = intStrategy.Interpolate(inRasterFile, daysPerInputData);
            }
            // Else, if it isn't then the default daily interpolation strategy.
            else {

            }
        }

        TemporalSummaryComposition tempComp;
        for(File inRaster : inputFileSet)
        {
            tempComp = fileStore.addFile(inRaster, inDataDate, daysPerInputData);

            if(tempComp != null)
            {
                ArrayList<File> files = new ArrayList<File>(tempComp.files.size());
                for(FileDatePair fdPair : tempComp.files) {
                    files.add(fdPair.file);
                }
                output = mergeStrategy.Merge(workingDir, projectName, pluginName, tempComp.startDate, (File[])files.toArray());
            }
        }
        return output;
    }
}
