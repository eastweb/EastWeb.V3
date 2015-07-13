package version2.prototype.summary.temporal;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

import version2.prototype.DataDate;

/**
 * Storage object for temporal summary raster files. Used as shared storage between temporal summary calculations.
 *
 * @author michael.devos
 *
 */
public class TemporalSummaryRasterFileStore {
    public final TemporalSummaryCompositionStrategy compStrategy;
    private static ArrayList<TemporalSummaryComposition> compositions = new ArrayList<TemporalSummaryComposition>(0);

    /**
     * Creates a TemporalSummaryRasterFileStore utilizing the given composition strategy.
     *
     * @param compStrategy  - temporal summary composition strategy to use when combining raster files
     */
    public TemporalSummaryRasterFileStore(TemporalSummaryCompositionStrategy compStrategy)
    {
        this.compStrategy = compStrategy;
    }

    /**
     * Adds a file to the storage.
     *
     * @param f  - raster file to add
     * @param d  - Datadate associated to the raster file
     * @param daysPerInputData - returns the composition made full by the new file if there is such a composition, otherwise null.
     * @return if file store can create a complete composite with the newly added file a new TemporalSummaryComposition is returned of the newly completed composite
     * while removing used files from the storage.
     * @throws Exception
     */
    public TemporalSummaryComposition addFile(File f, DataDate d, int daysPerInputData) throws Exception
    {
        TemporalSummaryComposition modifiedComp = null;
        LocalDate lDate = compStrategy.getStartDate(d.getCalendar());
        int i;
        boolean matched = false;
        for(i=0; i < compositions.size(); i++) {
            if(compositions.get(i).startDate.compareTo(lDate) == 0)
            {
                matched = true;
                i = compositions.size();
            }
        }

        if(matched)
        {
            compositions.get(i).addFilePair(new FileDatePair(f, d));
            if(compositions.get(i).compositeFull()) {
                modifiedComp = compositions.remove(i);
            }
        }
        else
        {
            TemporalSummaryComposition tempComp = new TemporalSummaryComposition(compStrategy, new FileDatePair(f, d));
            if(compStrategy.getDaysInThisComposite(d.getCalendar()) == 1) {
                modifiedComp = tempComp;
            } else {
                compositions.add(tempComp);
            }
        }

        return modifiedComp;
    }
}
