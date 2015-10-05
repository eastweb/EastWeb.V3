/**
 *
 */
package version2.prototype.summary.temporal.CompositionStrategies;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;

/**
 * @author Michael DeVos
 *
 */
public class CompositeOf8Days implements TemporalSummaryCompositionStrategy {

    /* (non-Javadoc)
     * @see version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy#getStartDate(java.time.LocalDate)
     */
    @Override
    public LocalDate getStartDate(LocalDate iDate) throws Exception {
        LocalDate sDate = LocalDate.ofYearDay(iDate.getYear(), 1);
        while(ChronoUnit.DAYS.between(sDate, iDate) >= 8) {
            sDate.plusDays(8);
        }
        return sDate;
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy#getDaysInThisComposite(java.time.LocalDate)
     */
    @Override
    public int getDaysInThisComposite(LocalDate dateInComposite) {
        if(dateInComposite.getMonthValue() < 12) {
            return 8;
        } else {
            if(ChronoUnit.DAYS.between(dateInComposite, LocalDate.ofYearDay(dateInComposite.getYear() + 1, 1)) >= 8) {
                return 8;
            } else {
                LocalDate sDate = LocalDate.ofYearDay(dateInComposite.getYear(), 1);
                while(ChronoUnit.DAYS.between(sDate, dateInComposite) >= 8) {
                    sDate.plusDays(8);
                }
                return (int) ChronoUnit.DAYS.between(sDate, LocalDate.ofYearDay(dateInComposite.getYear() + 1, 1));
            }
        }
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy#getCompositeIndex(java.time.LocalDate, java.time.LocalDate)
     */
    @Override
    public long getCompositeIndex(LocalDate startDate, LocalDate dateInComposite) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy#getNumberOfCompleteCompositesInRange(java.time.LocalDate, java.time.LocalDate)
     */
    @Override
    public long getNumberOfCompleteCompositesInRange(LocalDate startDate, LocalDate endDate) {
        LocalDate sDate = LocalDate.ofYearDay(startDate.getYear(), 1);
        while(ChronoUnit.DAYS.between(sDate, startDate) >= 8) {
            sDate.plusDays(8);
        }
        if(ChronoUnit.DAYS.between(sDate, startDate) != 0) {
            sDate.plusDays(8);
        }

        int count = 0;
        while(ChronoUnit.DAYS.between(sDate, endDate) >= 8) {
            sDate.plusDays(8);
            count += 8;
        }
        return count;
    }

}
