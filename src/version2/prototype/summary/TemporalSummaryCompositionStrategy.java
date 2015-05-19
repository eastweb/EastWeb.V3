package version2.prototype.summary;

import java.util.GregorianCalendar;

public interface TemporalSummaryCompositionStrategy {
    /**
     * Returns the date of the first day in the period the given date belongs to.
     * I.E. The first day of a week a date falls in.
     *
     * @param sDate
     * @return Calendar - The first day of the containing time period.
     * @throws Exception
     */
    GregorianCalendar getStartDate(GregorianCalendar sDate) throws Exception;

    /**
     * Returns the number of days in a single composition of data for this calendar.
     * E.G. A CalendarStrategy using weeks will return 7 here.
     * @return int - Number of Days
     */
    int getDaysInOneComposite(GregorianCalendar dateInComposite);
}
