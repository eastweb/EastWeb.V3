package test.summary.zonal;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import version2.prototype.summary.zonal.SummariesCollection;
import version2.prototype.summary.zonal.SummaryNameResultPair;

@SuppressWarnings("javadoc")
public class SummariesCollectionTester {

    @Test
    public final void testSummariesCollection() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        SummariesCollection col1 = new SummariesCollection(new ArrayList<String>(Arrays.asList("Count", "Sum", "Mean", "StdDev")));
        SummariesCollection col2 = new SummariesCollection(new ArrayList<String>(Arrays.asList("Count", "Sum", "Mean", "StdDev")));
        Map<Integer, Double> countResults1 = new HashMap<Integer, Double>();
        Map<Integer, Double> sumResults1 = new HashMap<Integer, Double>();
        Map<Integer, Double> meanResults1 = new HashMap<Integer, Double>();
        Map<Integer, Double> stdDevResults1 = new HashMap<Integer, Double>();
        Map<Integer, Double> countResults2 = new HashMap<Integer, Double>();
        Map<Integer, Double> sumResults2 = new HashMap<Integer, Double>();
        Map<Integer, Double> meanResults2 = new HashMap<Integer, Double>();
        Map<Integer, Double> stdDevResults2 = new HashMap<Integer, Double>();

        // Setup results maps
        double m1 = 1;
        double m2 = 2;
        double m3 = 3;
        double m4 = 4;
        double m5 = 5;
        double m6 = 6;
        double ansStdDev1_1 = Math.sqrt((Math.pow(1 - m1, 2) + Math.pow(1 - m1, 2) + Math.pow(1 - m1, 2)) / 3);
        double ansStdDev1_2 = Math.sqrt((Math.pow(2 - m2, 2) + Math.pow(2 - m2, 2) + Math.pow(2 - m2, 2)) / 3);
        double ansStdDev1_3 = Math.sqrt((Math.pow(3 - m3, 2) + Math.pow(3 - m3, 2) + Math.pow(3 - m3, 2)) / 3);
        double ansStdDev1_4 = Math.sqrt((Math.pow(4 - m4, 2) + Math.pow(4 - m4, 2) + Math.pow(4 - m4, 2)) / 3);
        double ansStdDev1_5 = Math.sqrt((Math.pow(5 - m5, 2) + Math.pow(5 - m5, 2) + Math.pow(5 - m5, 2)) / 3);
        double ansStdDev1_6 = Math.sqrt((Math.pow(6 - m6, 2) + Math.pow(6 - m6, 2) + Math.pow(6 - m6, 2)) / 3);
        stdDevResults1.put(10, ansStdDev1_1);
        stdDevResults1.put(11, ansStdDev1_2);
        stdDevResults1.put(12, ansStdDev1_3);
        stdDevResults1.put(13, ansStdDev1_4);
        stdDevResults1.put(14, ansStdDev1_5);
        stdDevResults1.put(15, ansStdDev1_6);

        m1 = 2.5;
        m2 = 2.0;
        double ansStdDev2_1 = Math.sqrt((Math.pow(1 - m1, 2) + Math.pow(2 - m1, 2) + Math.pow(3 - m1, 2) + Math.pow(4 - m1, 2)) / 4);
        double ansStdDev2_2 = Math.sqrt((Math.pow(1 - m2, 2) + Math.pow(2 - m2, 2) + Math.pow(3 - m2, 2)) / 3);
        //        double ansStdDev2_1 = new BigDecimal(Math.sqrt((Math.pow(1 - m1, 2) + Math.pow(2 - m1, 2) + Math.pow(3 - m1, 2) + Math.pow(4 - m1, 2)) / 4)).setScale(14, BigDecimal.ROUND_DOWN).doubleValue();
        //        double ansStdDev2_2 = new BigDecimal(Math.sqrt((Math.pow(1 - m2, 2) + Math.pow(2 - m2, 2) + Math.pow(3 - m2, 2)) / 3)).setScale(14, BigDecimal.ROUND_DOWN).doubleValue();

        for(int i=0; i < 6; i++)
        {
            // Collection 1 results
            countResults1.put(10 + i, 3.0);
            sumResults1.put(10 + i, 3.0 * (i + 1));
            meanResults1.put(10 + i, new Double(i+1));

            // Collection 2 results
            if(i == 0)
            {
                countResults2.put(10 + i, 4.0);
                sumResults2.put(10 + i, 10.0);
                meanResults2.put(10 + i, new Double(2.5));
                stdDevResults2.put(10 + i, ansStdDev2_1);
            }
            else
            {
                countResults2.put(10 + i, 3.0);
                sumResults2.put(10 + i, 6.0);
                meanResults2.put(10 + i, new Double(2));
                stdDevResults2.put(10 + i, ansStdDev2_2);
            }
        }

        // Initialize collections
        testData1(col1);
        testData2(col2);

        // Verification
        ArrayList<SummaryNameResultPair> results;
        results = col1.getResults();
        for(SummaryNameResultPair pair : results)
        {
            switch(pair.getSimpleName())
            {
            case "Count":
                assertEquals("Count1 results not as expected.", countResults1, pair.getResult());
                break;
            case "Sum":
                assertEquals("Sum1 results not as expected.", sumResults1, pair.getResult());
                break;
            case "Mean":
                assertEquals("Mean1 results not as expected.", meanResults1, pair.getResult());
                break;
            case "StdDev":
                assertEquals("StdDev1 results not as expected.", stdDevResults1, pair.getResult());
                break;
            }
        }
        results = col2.getResults();
        for(SummaryNameResultPair pair : results)
        {
            switch(pair.getSimpleName())
            {
            case "Count":
                assertEquals("Count2 results not as expected.", countResults2, pair.getResult());
                break;
            case "Sum":
                assertEquals("Sum2 results not as expected.", sumResults2, pair.getResult());
                break;
            case "Mean":
                assertEquals("Mean2 results not as expected.", meanResults2, pair.getResult());
                break;
            case "StdDev":
                assertEquals("StdDev2 results not as expected.", stdDevResults2, pair.getResult());
                break;
            }
        }

        col1 = null;
        col2 = null;
    }

    private void testData1(SummariesCollection col) {
        col.add(10 + 0, 1.0);
        col.add(10 + 1, 2.0);
        col.add(10 + 2, 3.0);
        col.add(10 + 3, 4.0);
        col.add(10 + 4, 5.0);
        col.add(10 + 5, 6.0);

        col.add(10 + 0, 1.0);
        col.add(10 + 1, 2.0);
        col.add(10 + 2, 3.0);
        col.add(10 + 3, 4.0);
        col.add(10 + 4, 5.0);
        col.add(10 + 5, 6.0);

        col.add(10 + 0, 1.0);
        col.add(10 + 1, 2.0);
        col.add(10 + 2, 3.0);
        col.add(10 + 3, 4.0);
        col.add(10 + 4, 5.0);
        col.add(10 + 5, 6.0);
    }

    private static void testData2(SummariesCollection col) {
        col.add(10 + 0, 1.0);
        col.add(10 + 1, 1.0);
        col.add(10 + 2, 1.0);
        col.add(10 + 3, 1.0);
        col.add(10 + 4, 1.0);
        col.add(10 + 5, 1.0);

        col.add(10 + 0, 2.0);
        col.add(10 + 1, 2.0);
        col.add(10 + 2, 2.0);
        col.add(10 + 3, 2.0);
        col.add(10 + 4, 2.0);
        col.add(10 + 5, 2.0);

        col.add(10 + 0, 3.0);
        col.add(10 + 1, 3.0);
        col.add(10 + 2, 3.0);
        col.add(10 + 3, 3.0);
        col.add(10 + 4, 3.0);
        col.add(10 + 5, 3.0);

        col.add(10 + 0, 4.0);
    }
}
