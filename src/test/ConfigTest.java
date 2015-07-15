/**
 *
 */
package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;

/**
 * @author Mike
 *
 */
public class ConfigTest {

    /**
     * Test method for {@link version2.prototype.Config#getInstance()}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws ConfigReadException
     */
    @Test
    public final void testGetInstance() throws ParserConfigurationException, SAXException, IOException {
        Config config = Config.getAnInstance("config.xml");     // Works no different than Config.getInstance()

        assertTrue("Config downloadDir is " + config.getDownloadDir(), config.getDownloadDir().equals("C:/EASTWeb/Downloads"));

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
        assertTrue("Config Output SummaryCalculation list is " + config.getSummaryCalculations().toString(), config.getSummaryCalculations().equals(summariesExpected));
    }
}
