package version2.prototype.summary.temporal;

import java.util.GregorianCalendar;

/**
 * An implementation of TemporalSummaryCompositionStrategy representing a strategy for creating weekly composites based on the Gregorian calendar.
 *
 * @author michael.devos
 *
 */
public class GregorianWeeklyStrategy implements TemporalSummaryCompositionStrategy {

    /* (non-Javadoc)
     * @see version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy#getStartDate(java.util.GregorianCalendar)
     */
    @Override
    public GregorianCalendar getStartDate(GregorianCalendar sDate) throws Exception {
        int firstDay = GregorianCalendar.SUNDAY;
        int currentDay = sDate.get(GregorianCalendar.DAY_OF_WEEK);
        if(currentDay !=  firstDay){
            GregorianCalendar newDate = (GregorianCalendar) sDate.clone();

            do{
                newDate.add(GregorianCalendar.DAY_OF_MONTH, 1);
                currentDay = newDate.get(GregorianCalendar.DAY_OF_WEEK);
            }while(currentDay != firstDay);

            return newDate;
        }
        return sDate;
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy#getDaysInOneComposite(java.util.GregorianCalendar)
     */
    @Override
    public int getDaysInOneComposite(GregorianCalendar dateInComposite) {
        return 7;
    }

}
