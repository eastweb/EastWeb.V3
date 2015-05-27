package version2.prototype.summary.temporal;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class WHOWeeklyStrategy implements TemporalSummaryCompositionStrategy {

    @Override
    public GregorianCalendar getStartDate(GregorianCalendar sDate) throws Exception {
        int firstDay = GregorianCalendar.MONDAY;
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

    @Override
    public int getDaysInOneComposite(GregorianCalendar dateInComposite) {
        return 7;
    }

}
