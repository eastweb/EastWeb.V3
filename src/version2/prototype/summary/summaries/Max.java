package version2.prototype.summary.summaries;

import java.util.ArrayList;
import java.util.Map;

import version2.prototype.summary.SummariesCollection;
import version2.prototype.summary.SummarySingleton;

/**
 * Represents a maximum summary based on indexed double values.
 *
 * @author michael.devos
 *
 */
public class Max extends SummarySingleton {

    /**
     * Creates a Max object representing a maximum summary.
     *
     * @param col  - the SummariesCollection to register itself to
     */
    public Max(SummariesCollection col) {
        super(col);
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummarySingleton#put(int, double)
     */
    @Override
    public void put(int index, double value) {
        if(map.get(index) == null) {
            map.put(index, value);
        } else if(map.get(index) < value) {
            map.put(index, value);
        }
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummarySingleton#getResult()
     */
    @Override
    public Map<Integer, Double> getResult() {
        return map;
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummarySingleton#getDistinctLeaflets()
     */
    @Override
    public ArrayList<SummarySingleton> getDistinctLeaflets() {
        ArrayList<SummarySingleton> temp = new ArrayList<SummarySingleton>();
        temp.add(this);
        return temp;
    }
}
