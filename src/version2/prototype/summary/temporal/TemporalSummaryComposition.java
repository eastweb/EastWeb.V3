package version2.prototype.summary.temporal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;


public class TemporalSummaryComposition {
    public final GregorianCalendar startDate;
    public final ArrayList<FileDatePair> files;
    private final TemporalSummaryCompositionStrategy strategy;

    public TemporalSummaryComposition(TemporalSummaryCompositionStrategy strategy, FileDatePair FDPair) throws Exception
    {
        this.strategy = strategy;
        files = new ArrayList<FileDatePair>(1);
        files.add(FDPair);
        startDate = strategy.getStartDate(FDPair.date.getCalendar());
    }

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

    public boolean compositeFull() throws Exception
    {
        if(strategy.getDaysInOneComposite(startDate) == files.size()) {
            return true;
        } else {
            return false;
        }
    }

    public class AscComp implements Comparator<FileDatePair>
    {
        @Override
        public int compare(FileDatePair arg0, FileDatePair arg1) {
            return arg0.date.compareTo(arg1.date);
        }
    }
}
