package version2.prototype.summary.temporal.CalendarStrategies;

import java.util.Calendar;
import java.util.GregorianCalendar;

import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;

public class GregorianMonthlyStrategy implements TemporalSummaryCompositionStrategy {

    @Override
    public GregorianCalendar getStartDate(GregorianCalendar sDate)
            throws Exception {
        return new GregorianCalendar(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH);
    }

    @Override
    public int getDaysInOneComposite(GregorianCalendar dateInComposite) {
        return dateInComposite.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

}
