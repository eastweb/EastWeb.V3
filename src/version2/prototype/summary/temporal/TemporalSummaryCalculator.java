package version2.prototype.summary.temporal;

import java.io.File;
import java.util.ArrayList;

import version2.prototype.DataDate;
import version2.prototype.util.DataFileMetaData;

public class TemporalSummaryCalculator {
    private String workingDir;
    private String projectName;
    private String pluginName;
    private File inRasterFile;
    private DataDate inDataDate;
    private int daysPerInputData;
    private int daysPerOutputData;
    private TemporalSummaryRasterFileStore fileStore;
    private InterpolateStrategy intStrategy;
    private MergeStrategy mergeStrategy;

    public TemporalSummaryCalculator(String workingDir, String projectName, String pluginName, File inRasterFile, DataDate inDataDate,
            int daysPerInputData, int daysPerOutputData, TemporalSummaryRasterFileStore fileStore, InterpolateStrategy intStrategy,
            MergeStrategy mergeStrategy) {
        this.workingDir = workingDir;
        this.projectName = projectName;
        this.pluginName = pluginName;
        this.inRasterFile = inRasterFile;
        this.inDataDate = inDataDate;
        this.daysPerInputData = daysPerInputData;
        this.daysPerOutputData = daysPerOutputData;
        this.fileStore = fileStore;
        this.intStrategy = intStrategy;
        this.mergeStrategy = mergeStrategy;
    }

    public DataFileMetaData calculate() throws Exception {
        DataFileMetaData output = null;
        ArrayList<File> inputFileSet = new ArrayList<File>();

        if(daysPerInputData > daysPerOutputData) {
            inputFileSet = intStrategy.Interpolate(inRasterFile, daysPerInputData);
        } else {
            inputFileSet.add(inRasterFile);
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
