package version2.prototype.summary.zonal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract summary definition. Implementing classes are to define a calculation of a summary for double values. Classes won't be allowed to be instantiated multiple
 * times but will share their results with all requiring classes registered within a SummariesCollection object.
 *
 * @author michael.devos
 *
 */
public abstract class SummarySingleton {
    /**
     * Create a SummarySingleton. Registers this instance to the given SummariesCollection object along with all its dependencies. Implementing classes have to take
     * care of defining the dependencies and the calculation this instance will do with acquired doubles.
     *
     * @param col
     */
    public SummarySingleton(SummariesCollection col){
        map = new HashMap<Integer, Double>();
        this.col = col;
        canonicalName = this.getClass().getCanonicalName();
        registerDependencies();
    }

    /**
     * Implementing classes will override this method and register required dependencies by creating instances of required SummarySingletons and registering them.
     * The SummariesCollection object will handle returning the correct reference to the stored SummarySingleton to use. If a SummarySingleton has no dependencies
     * then it need not override this method.
     */
    protected void registerDependencies(){    }

    /**
     * Implementing classes override this method to handle accepting inserted values. Internally the values should be stored in a Map<Integer, Double> type.
     *
     * @param index  - index within the map the value is to be added to. All indices should be initialized to zero.
     * @param value  - value to add to the specified index
     */
    public abstract void put(int index, double value);

    /**
     * Get the resulting calculation.
     *
     * @return returns a map object containing results to this objects calculation to each index
     */
    public abstract Map<Integer, Double> getResult();

    /**
     * A full name to this calculation. Not to be abbreviated. SummarySingletons are looked up in SummariesCollections using this value.
     *
     * @return a name to identify this calculation by
     */
    public String getCanonicalName(){
        return canonicalName;
    }

    /**
     * Gets the list of SummarySingleton objects that are this object's dependencies. Must return the list of unique dependencies amongst all of its dependencies and
     * itself. Only SummarySingleton objects that have no dependencies are to be included in this list (hence distinct leaflets, or leaves of the tree structure).
     * These can be found from dependencies by finding SummarySingleton objects which return a list of only themselves for this method.
     *
     * @return
     */
    public abstract ArrayList<SummarySingleton> getDistinctLeaflets();

    protected String canonicalName;
    protected Map<Integer, Double> map;
    protected SummariesCollection col;
}
