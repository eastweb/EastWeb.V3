package version2.prototype.summary.temporal;

import java.util.GregorianCalendar;

/**
 * Interface designed for defining temporal summary composition strategies (methods by which to combine the raster files to create temporal composites).
 *
 * @author michael.devos
 *
 */
public interface TemporalSummaryCompositionStrategy {
    /**
     * Returns the date of the first day in the period the given date belongs to.
     * I.E. The first day of a week a date falls in.
     *
     * @param sDate  - start date for this composite
     * @return Calendar  - The first day of the containing time period.
     * @throws Exception
     */
    GregorianCalendar getStartDate(GregorianCalendar sDate) throws Exception;

    /**
     * Returns the number of days in a single composition of data for this calendar.
     * E.G. A CalendarStrategy using weeks will return 7 here.
     *
     * @param dateInComposite  - date to which to check composition size for
     * @return number of days in the checked composite
     */
    int getDaysInOneComposite(GregorianCalendar dateInComposite);
}
