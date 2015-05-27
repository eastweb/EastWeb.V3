/**
 * 
 */
package version2.prototype.summary.summaries;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Mike DeVos
 *
 */
public class SummariesCollectionTest {
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    /**
     * Test method for {@link version2.prototype.summary.summaries.SummariesCollection#SummariesCollection(java.lang.String[])}.
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    @Test
    public void testSummariesCollection() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        SummariesCollection col = new SummariesCollection(new ArrayList<String>());

    }

    /**
     * Test method for {@link version2.prototype.summary.summaries.SummariesCollection#lookup(java.lang.String)}.
     */
    @Test
    public void testLookup() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link version2.prototype.summary.summaries.SummariesCollection#put(int, double)}.
     */
    @Test
    public void testPut() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link version2.prototype.summary.summaries.SummariesCollection#register(version2.prototype.summary.SummaryNameInstancePair)}.
     */
    @Test
    public void testRegister() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link version2.prototype.summary.summaries.SummariesCollection#getResults()}.
     */
    @Test
    public void testGetResults() {
        fail("Not yet implemented");
    }

}
