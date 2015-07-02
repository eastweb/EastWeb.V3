package version2.prototype.summary.zonal.summaries;

import java.util.ArrayList;
import java.util.Map;

import version2.prototype.summary.zonal.SummariesCollection;
import version2.prototype.summary.zonal.SummaryNameInstancePair;
import version2.prototype.summary.zonal.SummaryCalculation;

/**
 * Represents a standard deviation summary based on indexed double values.
 *
 * @author michael.devos
 *
 */
public class StdDev extends SummaryCalculation {
    private SummaryCalculation sqrSum;
    private SummaryCalculation count;
    private SummaryCalculation mean;

    /**
     * Creates a StdDev object representing a standard deviation summary.
     *
     * @param col  - the SummariesCollection to register itself to
     */
    public StdDev(SummariesCollection col) {
        super(col);
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummaryCalculation#put(int, double)
     */
    @Override
    public void put(int index, double value) {
        sqrSum.put(index, value);
        count.put(index, value);
        mean.put(index, value);
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummaryCalculation#getResult()
     */
    @Override
    public Map<Integer, Double> getResult() {
        if(map.size() == 0 || map.size() < count.getResult().size()){
            Map<Integer, Double> sqrSumRs = sqrSum.getResult();
            Map<Integer, Double> countRs = count.getResult();
            Map<Integer, Double> meanRs = mean.getResult();

            for(int i=0; i < countRs.size(); i++){
                map.put(i, Math.sqrt((sqrSumRs.get(i)/countRs.get(i)) - (meanRs.get(i) * meanRs.get(i))));
            }
        }
        return map;
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummaryCalculation#getDistinctLeaflets()
     */
    @Override
    public ArrayList<SummaryCalculation> getDistinctLeaflets() {
        ArrayList<SummaryCalculation> temp = new ArrayList<SummaryCalculation>(3);
        temp.add(count);
        temp.add(sqrSum);
        ArrayList<SummaryCalculation> fromMean = mean.getDistinctLeaflets();
        if(fromMean.get(0).getClass().getSimpleName().equalsIgnoreCase("sum")) {
            temp.add(fromMean.get(0));
        } else {
            temp.add(fromMean.get(1));
        }
        return temp;
    }

    /* (non-Javadoc)
     * @see version2.prototype.summary.summaries.SummaryCalculation#registerDependencies()
     */
    @Override
    protected void registerDependencies() {
        sqrSum = new SqrSum(col);
        sqrSum = col.register(new SummaryNameInstancePair(sqrSum.getCanonicalName(), sqrSum));

        count = new Count(col);
        count = col.register(new SummaryNameInstancePair(count.getCanonicalName(), count));

        mean = new Mean(col);
        mean = col.register(new SummaryNameInstancePair(mean.getCanonicalName(), mean));
    }

}
