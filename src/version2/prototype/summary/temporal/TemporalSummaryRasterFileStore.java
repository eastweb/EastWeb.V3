package version2.prototype.summary.temporal;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;

import version2.prototype.DataDate;

public class TemporalSummaryRasterFileStore {
    public final ArrayList<TemporalSummaryComposition> compositions;
    public final TemporalSummaryCompositionStrategy compStrategy;

    public TemporalSummaryRasterFileStore(ArrayList<TemporalSummaryComposition> compositions, TemporalSummaryCompositionStrategy compStrategy)
    {
        this.compositions = compositions;
        this.compStrategy = compStrategy;
    }

    /**
     *
     * @param f
     * @param d
     * @param daysPerInputData
     * @return TemporalSummaryComposition - returns the composition made full by the new file if there is such a composition, otherwise null.
     * @throws Exception
     */
    public TemporalSummaryComposition addFile(File f, DataDate d, int daysPerInputData) throws Exception
    {
        TemporalSummaryComposition modifiedComp = null;
        GregorianCalendar gDate = compStrategy.getStartDate(d.getCalendar());
        int i;
        boolean matched = false;
        for(i=0; i < compositions.size(); i++)
            if(compositions.get(i).startDate.compareTo(gDate) == 0)
            {
                matched = true;
                i = compositions.size();
            }

        if(matched)
        {
            compositions.get(i).addFilePair(new FileDatePair(f, d));
            if(compositions.get(i).compositeFull())
                modifiedComp = compositions.remove(i);
        }
        else
        {
            TemporalSummaryComposition tempComp = new TemporalSummaryComposition(compStrategy, new FileDatePair(f, d));
            if(compStrategy.getDaysInOneComposite(d.getCalendar()) == 1)
                modifiedComp = tempComp;
            else
                compositions.add(tempComp);
        }

        return modifiedComp;
    }
}
