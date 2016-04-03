/**
 *
 */
package test.summary.temporal;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.CompositionStrategies.CDCWeeklyStrategy;
import version2.prototype.summary.temporal.CompositionStrategies.GregorianMonthlyStrategy;
import version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy;
import version2.prototype.summary.temporal.CompositionStrategies.WHOWeeklyStrategy;

/**
 * @author michael.devos
 *
 */
public class TemporalSummaryCompositionStrategyTester {
    private TemporalSummaryCompositionStrategy cdcWeeklyStrategy;
    private TemporalSummaryCompositionStrategy gregorianMonthlyStrategy;
    private TemporalSummaryCompositionStrategy gregorianWeeklyStretegy;
    private TemporalSummaryCompositionStrategy whoWeeklyStrategy;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUp() throws Exception {
        cdcWeeklyStrategy = new CDCWeeklyStrategy();
        gregorianMonthlyStrategy = new GregorianMonthlyStrategy();
        gregorianWeeklyStretegy = new GregorianWeeklyStrategy();
        whoWeeklyStrategy = new WHOWeeklyStrategy();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDown() throws Exception {
        cdcWeeklyStrategy = null;
        gregorianMonthlyStrategy = null;
        gregorianWeeklyStretegy = null;
        whoWeeklyStrategy = null;
    }

    /**
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.CDCWeeklyStrategy#getStartDate(java.time.LocalDate)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.GregorianMonthlyStrategy#getStartDate(java.time.LocalDate)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.WHOWeeklyStrategy#getStartDate(java.time.LocalDate)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy#getStartDate(java.time.LocalDate)}.
     */
    @Test
    public final void testGetStartDate() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.CDCWeeklyStrategy#getDaysInThisComposite(java.time.LocalDate)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.GregorianMonthlyStrategy#getDaysInThisComposite(java.time.LocalDate)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.WHOWeeklyStrategy#getDaysInThisComposite(java.time.LocalDate)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy#getDaysInThisComposite(java.time.LocalDate)}.
     */
    @Test
    public final void testGetDaysInThisComposite() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.CDCWeeklyStrategy#getCompositeIndex(java.time.LocalDate, java.time.LocalDate)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.GregorianMonthlyStrategy#getCompositeIndex(java.time.LocalDate, java.time.LocalDate)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.WHOWeeklyStrategy#getCompositeIndex(java.time.LocalDate, java.time.LocalDate)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy#getCompositeIndex(java.time.LocalDate, java.time.LocalDate)}.
     */
    @Test
    public final void testGetCompositeIndex() {
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.CDCWeeklyStrategy#getNumberOfCompleteCompositesInRange(java.time.LocalDate, java.time.LocalDate, int)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.GregorianMonthlyStrategy#getNumberOfCompleteCompositesInRange(java.time.LocalDate, java.time.LocalDate, int)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.WHOWeeklyStrategy#getNumberOfCompleteCompositesInRange(java.time.LocalDate, java.time.LocalDate, int)}.
     * Test method for {@link version2.prototype.summary.temporal.CompositionStrategies.GregorianWeeklyStrategy#getNumberOfCompleteCompositesInRange(java.time.LocalDate, java.time.LocalDate, int)}.
     */
    @Test
    public final void testGetNumberOfCompleteCompositesInRange() {
        fail("Not yet implemented"); // TODO
    }

}
