package version2.prototype.summary.temporal;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;

import version2.prototype.Process;
import version2.prototype.ProcessWorker;
import version2.prototype.summary.SummaryData;
import version2.prototype.util.CachedDataFile;

public class TemporalSummaryCalculator extends ProcessWorker<CachedDataFile> {
    private SummaryData data;
    private TemporalSummaryRasterFileStore fileStore;

    public TemporalSummaryCalculator(SummaryData data, String processWorkerName, Process<?> process) {
        super(processWorkerName, process);
        this.data = data;
        fileStore = data.fileStore;
    }

    @Override
    public CachedDataFile call() throws Exception {
        CachedDataFile output = null;
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
                output = data.mergeStrategy.Merge(data.projectName, data.pluginName, tempComp.startDate, (File[])files.toArray());
            }
        }
        return output;
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        // TODO Auto-generated method stub

    }

}
