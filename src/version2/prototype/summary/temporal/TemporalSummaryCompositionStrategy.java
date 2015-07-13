package version2.prototype.summary.temporal;

import java.time.LocalDate;

/**
 * Interface designed for defining temporal summary composition strategies (methods by which to combine the raster files to create temporal composites).
 *
 * @author michael.devos
 *
 */
public interface TemporalSummaryCompositionStrategy {
    /**
     * Gets the date of the first day in the period the given date belongs to.
     * I.E. The first day of a week a date falls in.
     *
     * @param sDate  - start date for this composite
     * @return Calendar  - The first day of the containing time period.
     * @throws Exception
     */
    LocalDate getStartDate(LocalDate sDate) throws Exception;

    /**
     * Gets the number of days in a single composition of data for this calendar.
     * E.G. A CalendarStrategy using weeks will return 7 here.
     *
     * @param dateInComposite  - date to which to check composition size for
     * @return number of days in the checked composite
     */
    int getDaysInThisComposite(LocalDate dateInComposite);

    /**
     * Gets the 0-based ordered index number of the containing composite for the given dateInComposite to be used as an array index.
     *
     * @param startDate  - the locale (zone) and time zone independent start date
     * @param dateInComposite  - the locale (zone) and time zone independent date to find the index for
     * @return the 0-based index of the containing composite
     */
    int getCompositeIndex(LocalDate startDate, LocalDate dateInComposite);

    /**
     * Gets the number of complete composites (equivalent to number of total expected summary results if end date is current date + 1 day) within the given range of dates.
     *
     * @param startDate  - the locale (zone) and time zone independent inclusive start date
     * @param endDate  - the locale (zone) and time zone independent exlusive end date
     * @param daysPerInputData  - number of days a single raster data file represents
     * @return the number of complete composites that can be created within the given date range and input size
     */
    int getNumberOfCompleteCompositesInRange(LocalDate startDate, LocalDate endDate, int daysPerInputData);
}
