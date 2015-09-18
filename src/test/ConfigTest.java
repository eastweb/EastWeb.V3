/**
 *
 */
package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.util.DatabaseConnector;

/**
 * @author Mike
 *
 */
public class ConfigTest {

    /** Test database connection by using the given string in first parameter index. To run and see output in command prompt export as executable jar
     * and run with "java -jar .\Exported_Jar_Name.jar config.xml".
     * @param args
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        Connection con = DatabaseConnector.getConnection(Config.getAnInstance(args[0]));
        ResultSet rs = con.createStatement().executeQuery("select 1");
        if(rs != null) {
            System.out.println("Sucessfully connected to database");
        } else {
            System.out.println("Failed to connect to database");
        }
    }

    /**
     * Test method for {@link version2.prototype.Config#getInstance()}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConfigReadException
     */
    @Test
    public final void testGetInstance() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, SQLException {
        Config config = Config.getAnInstance("src\\test\\config.xml");     // Works no different than Config.getInstance()

        assertEquals("Config ErrorLogDir is incorrect.", "C:\\EASTWeb_Test\\", config.getErrorLogDir());
        assertEquals("Config DownloadDir is incorrect.", "C:\\EASTWeb_Test\\Downloads\\", config.getDownloadDir());

        assertEquals("Config Database HostName is incorrect.", "jdbc:postgresql://" + "localhost", config.getDatabaseHost());
        assertEquals("Config Database Port incorrect.", 5432, config.getPort().intValue());
        assertEquals("Config Database DatabaseName incorrect.", "postgres", config.getDatabaseName());
        assertEquals("Config Database UserName is incorrect.", "postgres", config.getDatabaseUsername());
        assertEquals("Config Database PassWord is incorrect.", "eastweb", config.getDatabasePassword());
        Connection con = DatabaseConnector.getConnection();
        assertNotNull("Database connection failed.", con.createStatement().executeQuery("select 1"));

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
