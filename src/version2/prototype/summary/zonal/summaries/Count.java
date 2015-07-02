package version2.prototype.summary.zonal.summaries;

import java.util.ArrayList;
import java.util.Map;

import version2.prototype.summary.zonal.SummariesCollection;
import version2.prototype.summary.zonal.SummaryCalculation;

/**
 * Represents a counting summary based on the count of indexed double values.
 *
 * @author michael.devos
 *
 */
public class Count extends SummaryCalculation {

    /**
     * Creates a Count object representing a counting summary.
     *
     * @param col  - the SummariesCollection to register itself to
     */
    public Count(SummariesCollection col) {
        super(col);
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummaryCalculation#put(int, double)
     */
    @Override
    public void put(int index, double value) {
        if(map.get(index) == null) {
            map.put(index, 1.0);
        } else {
            map.put(index, map.get(index) + 1);
        }
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummaryCalculation#getResult()
     */
    @Override
    public Map<Integer, Double> getResult() {
        return map;
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
