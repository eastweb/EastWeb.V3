package EastWeb_Summary.Zonal.Summaries;

import java.util.ArrayList;
import java.util.Map;

import EastWeb_Summary.Zonal.SummariesCollection;

/**
 * Represents a summation summary based on indexed double values.
 *
 * @author michael.devos
 *
 */
public class Sum extends SummaryCalculation {

    /**
     * Creates a Sum object representing a summation summary.
     *
     * @param col  - the SummariesCollection to register itself to
     */
    public Sum(SummariesCollection col) {
        super(col);
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummaryCalculation#put(int, double)
     */
    @Override
    public void put(int index, double value) {
        if(resultMap.get(index) == null) {
            resultMap.put(index, value);
        } else {
            resultMap.put(index, resultMap.get(index) + value);
        }
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummaryCalculation#getResult()
     */
    @Override
    public Map<Integer, Double> getResult() {
        return resultMap;
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummaryCalculation#getDistinctLeaflets()
     */
    @Override
    public ArrayList<SummaryCalculation> getDistinctLeaflets() {
        ArrayList<SummaryCalculation> temp = new ArrayList<SummaryCalculation>();
        temp.add(this);
        return temp;
    }
}
