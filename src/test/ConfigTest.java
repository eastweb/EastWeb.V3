/**
 *
 */
package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;

/**
 * @author Mike
 *
 */
public class ConfigTest {

    /**
     * Test method for {@link version2.prototype.Config#getInstance()}.
     * @throws ConfigReadException
     */
    @Test
    public final void testGetInstance() throws ConfigReadException {
        Config config = Config.getInstance();

        assertTrue("Config DataBase hostName is " + config.getDatabaseHost(), config.getDatabaseHost().equals("jdbc:postgresql://" + "localhost:5432"));
        assertTrue("Config DataBase userName is " + config.getDatabaseUsername(), config.getDatabaseUsername().equals("postgres"));
        assertTrue("Config DataBase passWord is " + config.getDatabasePassword(), config.getDatabasePassword().equals("eastweb"));

        ArrayList<String> summariesExpected = new ArrayList<String>();
        summariesExpected.add("Count");
        summariesExpected.add("Max");
        summariesExpected.add("Mean");
        summariesExpected.add("Min");
        summariesExpected.add("SqrSum");
        summariesExpected.add("StdDev");
        summariesExpected.add("Sum");
        assertTrue("Config Output SummaryCalculation list is " + config.SummaryCalculations().toString(), config.SummaryCalculations().equals(summariesExpected));
    }
}
