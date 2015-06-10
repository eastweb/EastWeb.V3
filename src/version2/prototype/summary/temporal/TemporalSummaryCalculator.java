package version2.prototype.summary.temporal;

import java.io.File;
import java.util.ArrayList;

import version2.prototype.summary.SummaryData;
import version2.prototype.util.DataFileMetaData;

public class TemporalSummaryCalculator {
    private SummaryData data;
    private TemporalSummaryRasterFileStore fileStore;

    public TemporalSummaryCalculator(SummaryData data) {
        this.data = data;
        fileStore = data.fileStore;
    }

    public DataFileMetaData calculate() throws Exception {
        DataFileMetaData output = null;
        ArrayList<File> inputFileSet = new ArrayList<File>();

        if(data.daysPerInputData > data.daysPerOutputData) {
            inputFileSet = data.intStrategy.Interpolate(data.inRasterFile, data.daysPerInputData);
        } else {
            inputFileSet.add(data.inRasterFile);
        }

        TemporalSummaryComposition tempComp;
        for(File inRaster : inputFileSet)
        {
            tempComp = fileStore.addFile(inRaster, data.inDataDate, data.daysPerInputData);

            if(tempComp != null)
            {
                ArrayList<File> files = new ArrayList<File>(tempComp.files.size());
                for(FileDatePair fdPair : tempComp.files) {
                    files.add(fdPair.file);
                }
                output = data.mergeStrategy.Merge(data.workingDir, data.projectName, data.pluginName, tempComp.startDate, (File[])files.toArray());
            }
        }
        return output;
    }
}
