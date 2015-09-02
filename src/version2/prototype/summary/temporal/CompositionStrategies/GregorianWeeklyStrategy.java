package version2.prototype.summary.temporal.CompositionStrategies;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;

public class GregorianWeeklyStrategy implements TemporalSummaryCompositionStrategy {

    @Override
    public LocalDate getStartDate(LocalDate sDate) throws Exception {
        //        int firstDay = GregorianCalendar.SUNDAY;
        //        int currentDay = sDate.get(GregorianCalendar.DAY_OF_WEEK);
        //        if(currentDay !=  firstDay){
        //            GregorianCalendar newDate = (GregorianCalendar) sDate.clone();
        //
        //            do{
        //                newDate.add(GregorianCalendar.DAY_OF_MONTH, 1);
        //                currentDay = newDate.get(GregorianCalendar.DAY_OF_WEEK);
        //            }while(currentDay != firstDay);
        //
        //            return newDate;
        //        }
        //        return sDate;
        LocalDate outDate = sDate;
        DayOfWeek currentDay = sDate.getDayOfWeek();
        DayOfWeek firstDay = DayOfWeek.SUNDAY;
        if(currentDay != firstDay)
        {
            outDate = sDate.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        }
        return outDate;
    }

    @Override
    public int getDaysInThisComposite(LocalDate dateInComposite) {
        return 7;
    }

    @Override
    public long getCompositeIndex(LocalDate startDate, LocalDate dateInComposite) {
        return 0;
        // TODO Auto-generated method stub
    }

    @Override
    public long getNumberOfCompleteCompositesInRange(LocalDate startDate, LocalDate endDate, int daysPerInputData) {
        DayOfWeek startDay = startDate.getDayOfWeek();

        if(startDay != DayOfWeek.SUNDAY)
        {
            int value = startDay.getValue();     // 1 - Monday, 7 - Sunday

            startDate.plusDays(7 - value);
        }

        return ChronoUnit.WEEKS.between(startDate, endDate);
    }

}
