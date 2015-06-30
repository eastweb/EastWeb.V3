package version2.prototype.summary;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import version2.prototype.summary.summaries.Count;
import version2.prototype.summary.summaries.Max;
import version2.prototype.summary.summaries.Mean;
import version2.prototype.summary.summaries.Min;
import version2.prototype.summary.summaries.SqrSum;
import version2.prototype.summary.summaries.StdDev;
import version2.prototype.summary.summaries.Sum;

/**
 * Represents a collection of registered summaries as SummarySingletons facilitating interaction between summaries to share computations.
 *
 * @author michael.devos
 *
 */
public class SummariesCollection {
    /**
     * Creates a SummariesCollection object.
     *
     * @param summaryNames  - case-sensitive names of the classes of SummarySingleton objects to create
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public SummariesCollection(ArrayList<String> summaryNames) throws ClassNotFoundException, NoSuchMethodException, SecurityException,
    InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        summaries = new ArrayList<SummarySingleton>();
        registry = new ArrayList<SummaryNameInstancePair>();
        SummarySingleton temp = null;
        String canonicalPath = this.getClass().getCanonicalName().substring(0, this.getClass().getCanonicalName().lastIndexOf(".") + 1);
        boolean alreadyRegistered = false;

        if(summaryNames.size() > 0){
            for(String name : summaryNames){
                if(!name.equals("")){
                    Class<?> summary =  Class.forName(canonicalPath + name);
                    Constructor<?> summaryCons = summary.getConstructor(SummariesCollection.class);

                    if(registry.size() == 0){
                        temp = (SummarySingleton)summaryCons.newInstance(this);
                        summaries.add(temp);
                        registry.add(new SummaryNameInstancePair(temp.getCanonicalName(), temp));
                    }
                    else{
                        temp = null;
                        alreadyRegistered = false;
                        for(SummaryNameInstancePair pair : registry){
                            if(pair.getCanonicalName().equals(canonicalPath + name)) {
                                alreadyRegistered = true;
                                temp = pair.getInstance();
                            }
                        }
                        if(alreadyRegistered){
                            summaries.add(temp);
                        }
                        else {
                            temp = (SummarySingleton)summaryCons.newInstance(this);
                            summaries.add(temp);
                            registry.add(new SummaryNameInstancePair(temp.getCanonicalName(), temp));
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieve a reference to the SummarySingleton object (if there is one) with the given canonical name in the stored collection.
     *
     * @param canonicalName  - SummarySingleton object's registered canonical name as gotten with SummarySingleton.getCanonicalName()
     * @return reference to the registered SummarySingleton
     */
    public SummarySingleton lookup(String canonicalName){
        boolean found = false;
        int i = 0;
        SummarySingleton instance = null;
        SummaryNameInstancePair pair = null;

        while(!found && (i < registry.size())){
            pair = registry.get(i);
            if(pair.getCanonicalName().equalsIgnoreCase(canonicalName)){
                instance = pair.getInstance();
                found = true;
            }
            i = i + 1;
        }

        return instance;
    }

    /**
     * Inserts a new value into the collection by inserting the value into all distinct leaflet SummarySingleton objects registered to the collection.
     *
     * @param index  - index of given value
     * @param value  - double value to insert
     */
    public void put(int index, double value){
        ArrayList<SummarySingleton> leavesFromSummaries = new ArrayList<SummarySingleton>();
        ArrayList<SummarySingleton> temp = null;
        for(SummarySingleton summary : summaries){
            temp = summary.getDistinctLeaflets();
            if(temp != null){
                for(SummarySingleton s : temp){
                    if(!leavesFromSummaries.contains(s)) {
                        leavesFromSummaries.add(s);
                    }
                }
            }
        }

        for(SummarySingleton summary : leavesFromSummaries){
            summary.put(index, value);
        }
    }

    /**
     * Registers a SummaryNameInstancePair into the collection.
     *
     * @param pair  - SummaryNameInstancePair to register
     * @return reference to SummarySingleton that is registered with the given name; if none then this will return the same reference as in the given
     * SummaryNameInstancePair
     */
    public SummarySingleton register(SummaryNameInstancePair pair){
        SummarySingleton temp = lookup(pair.getCanonicalName());
        if(temp == null){
            registry.add(pair);
            temp = pair.getInstance();
        }
        return temp;
    }

    /**
     * Gets the results of all registered summaries.
     *
     * @return list of all registered summary results
     */
    public ArrayList<SummaryNameResultPair> getResults(){
        ArrayList<SummaryNameResultPair> results = new ArrayList<SummaryNameResultPair>();
        for(SummarySingleton summary : summaries){
            results.add(new SummaryNameResultPair(summary.getClass().getSimpleName(), summary.getResult()));
        }
        return results;
    }

    /**
     * Provides a list of the names of the framework supplied SummarySingleton implementations.
     *
     * @return String list of the provided SummarySingleton implementation's canonical names
     */
    public static ArrayList<String> providedSummarySingletons()
    {
        ArrayList<String> list = new ArrayList<String>(7);
        list.add(new Count(null).getCanonicalName());
        list.add(new Max(null).getCanonicalName());
        list.add(new Mean(null).getCanonicalName());
        list.add(new Min(null).getCanonicalName());
        list.add(new SqrSum(null).getCanonicalName());
        list.add(new StdDev(null).getCanonicalName());
        list.add(new Sum(null).getCanonicalName());
        return list;
    }

    private ArrayList<SummaryNameInstancePair> registry;
    private ArrayList<SummarySingleton> summaries;
}
