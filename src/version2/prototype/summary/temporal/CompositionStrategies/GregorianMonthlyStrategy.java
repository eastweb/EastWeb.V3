package version2.prototype.summary.temporal.CompositionStrategies;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;

/**
 * @author michael.devos
 *
 */
public class GregorianMonthlyStrategy implements TemporalSummaryCompositionStrategy {

    @Override
    public LocalDate getStartDate(LocalDate sDate) throws Exception {
        //        return new GregorianCalendar(sDate.getYear(), Calendar.MONTH, Calendar.DAY_OF_MONTH);
        return LocalDate.of(sDate.getYear(), sDate.getMonthValue(), 1);
    }

    @Override
    public int getDaysInThisComposite(LocalDate dateInComposite) {
        //        return dateInComposite.getActualMaximum(Calendar.DAY_OF_MONTH);
        return dateInComposite.lengthOfMonth();
    }

    @Override
    public long getCompositeIndex(LocalDate startDate, LocalDate dateInComposite) {
        return 0;
        // TODO Auto-generated method stub
    }

    @Override
    public long getNumberOfCompleteCompositesInRange(LocalDate startDate, LocalDate endDate) {
        DayOfWeek startDay = startDate.getDayOfWeek();
        LocalDate adjStartDay = startDate;

        if(startDay != DayOfWeek.SUNDAY)
        {
            int value = startDay.getValue();     // 1 - Monday, 7 - Sunday

            adjStartDay = startDate.plusDays(7 - value);
        }

        return ChronoUnit.MONTHS.between(adjStartDay, endDate);
    }

}
