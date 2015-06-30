package version2.prototype.summary.summaries;

import java.util.ArrayList;
import java.util.Map;

import version2.prototype.summary.SummariesCollection;
import version2.prototype.summary.SummaryNameInstancePair;
import version2.prototype.summary.SummarySingleton;

/**
 * Represents a mean summary based on indexed double values.
 *
 * @author michael.devos
 *
 */
public class Mean extends SummarySingleton {
    private SummarySingleton sum;
    private SummarySingleton count;

    /**
     * Creates a Mean object representing a mean summary.
     *
     * @param col  - the SummariesCollection to register itself to
     */
    public Mean(SummariesCollection col) {
        super(col);
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummarySingleton#put(int, double)
     */
    @Override
    public void put(int index, double value) {
        sum.put(index, value);
        count.put(index, value);
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummarySingleton#getResult()
     */
    @Override
    public Map<Integer, Double> getResult() {
        if(map.size() == 0 || map.size() < count.getResult().size()){
            Map<Integer, Double> sumRs = sum.getResult();
            Map<Integer, Double> countRs = count.getResult();

            for(int i=0; i < countRs.size(); i++){
                map.put(i, sumRs.get(i)/countRs.get(i));
            }
        }
        return map;
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummarySingleton#getDistinctLeaflets()
     */
    @Override
    public ArrayList<SummarySingleton> getDistinctLeaflets() {
        ArrayList<SummarySingleton> temp = new ArrayList<SummarySingleton>(2);
        temp.add(count);
        temp.add(sum);
        return temp;
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummarySingleton#registerDependencies()
     */
    @Override
    protected void registerDependencies() {
        sum = new Sum(col);
        sum = col.register(new SummaryNameInstancePair(sum.getCanonicalName(), sum));

        count = new Count(col);
        count = col.register(new SummaryNameInstancePair(count.getCanonicalName(), count));
    }
}
