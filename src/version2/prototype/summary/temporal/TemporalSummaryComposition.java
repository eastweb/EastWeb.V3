package version2.prototype.summary.temporal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;


/**
 * Represents a temporal summary composition either partial or complete. Allows for file to be added to it until full.
 *
 * @author michael.devos
 *
 */
public class TemporalSummaryComposition {
    public final GregorianCalendar startDate;
    public final ArrayList<FileDatePair> files;
    private final TemporalSummaryCompositionStrategy strategy;

    /**
     * Creates a TemporalSummaryComposition object that will use the given strategy to create composites and initialized with a single FileDatePair.
     *
     * @param strategy  - a strategy pattern to use to combine raster files
     * @param FDPair  - first FileDatePair to insert into the composite
     * @throws Exception
     */
    public TemporalSummaryComposition(TemporalSummaryCompositionStrategy strategy, FileDatePair FDPair) throws Exception
    {
        this.strategy = strategy;
        files = new ArrayList<FileDatePair>(1);
        files.add(FDPair);
        startDate = strategy.getStartDate(FDPair.date.getCalendar());
    }

    /**
     * Adds a file to this class's composite file listing.
     *
     * @param FDPair  - FileDatePair to add to file listing
     * @return true if successfully added the FileDatePair, otherwise false
     */
    public boolean addFilePair(FileDatePair FDPair)
    {
        boolean success = true;
        if((strategy.getDaysInOneComposite(startDate) > files.size()) && (FDPair.date.getCalendar().compareTo(startDate) >= 0))
        {
            files.add(FDPair);
            Collections.sort(files, new AscComp());
        } else {
            success = false;
        }
        return success;
    }

    /**
     * Checks if the composite is full.
     *
     * @return returns true if the composite is full, otherwise returns false
     * @throws Exception
     */
    public boolean compositeFull() throws Exception
    {
        if(strategy.getDaysInOneComposite(startDate) == files.size()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Used to compare FileDatePairs within composite in order to sort them.
     *
     * @author michael.devos
     *
     */
    public class AscComp implements Comparator<FileDatePair>
    {
        @Override
        public int compare(FileDatePair arg0, FileDatePair arg1) {
            return arg0.date.compareTo(arg1.date);
        }
    }
}
