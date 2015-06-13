package version2.prototype.summary.temporal;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * An implementation of TemporalSummaryCompositionStrategy representing a strategy for creating monthly composites based on the Gregorian calendar.
 *
 * @author michael.devos
 *
 */
public class GregorianMonthlyStrategy implements TemporalSummaryCompositionStrategy {

    /* (non-Javadoc)
     * @see version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy#getStartDate(java.util.GregorianCalendar)
     */
    @Override
    public GregorianCalendar getStartDate(GregorianCalendar sDate)
            throws Exception {
        return new GregorianCalendar(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH);
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy#getDaysInOneComposite(java.util.GregorianCalendar)
     */
    @Override
    public int getDaysInOneComposite(GregorianCalendar dateInComposite) {
        return dateInComposite.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

}
