package version2.prototype.summary.temporal;

import java.io.File;
import java.util.ArrayList;

import version2.prototype.summary.SummaryCalculator;
import version2.prototype.summary.SummaryData;

public class TemporalSummaryCalculator implements SummaryCalculator {

    private SummaryData data;
    private TemporalSummaryRasterFileStore fileStore;

    /**
     * @param inRaster
     * @param inShape
     * @param inDate
     * @param outPath
     * @param hrsPerInputData
     * @param hrsPerOutputData
     * @param sumStrategy
     * @param merStrategy
     * @param intStrategy
     */
    public TemporalSummaryCalculator(SummaryData data)
    {
        this.data = data;
        fileStore = new TemporalSummaryRasterFileStore(SummaryData.compositions, data.compStrategy);
    }

    @Override
    public void run() throws Exception {
        ArrayList<File> inputFileSet = new ArrayList<File>();

        if(data.daysPerInputData > data.daysPerOutputData) {
            inputFileSet = data.intStrategy.Interpolate(data.inRaster, data.daysPerInputData);
        } else {
            inputFileSet.add(data.inRaster);
        }

        TemporalSummaryComposition tempComp;
        for(File inRaster : inputFileSet)
        {
            tempComp = fileStore.addFile(inRaster, data.inDate, data.daysPerInputData);

            if(tempComp != null)
            {
                ArrayList<File> files = new ArrayList<File>(tempComp.files.size());
                for(FileDatePair fdPair : tempComp.files) {
                    files.add(fdPair.file);
                }
                data.mergeStrategy.Merge(tempComp.startDate, (File[])files.toArray());
            }
        }
    }

}
