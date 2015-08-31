/**
 *
 */
package test.util;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import version2.prototype.Config;
import version2.prototype.util.DatabaseConnection;
import version2.prototype.util.DatabaseConnector;

/**
 * @author michael.devos
 *
 */
public class DatabaseConnectorTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.util.DatabaseConnector#getConnection()}.
     * @throws SQLException
     */
    @Test
    public final void testGetConnection() throws SQLException {
        DatabaseConnection con = DatabaseConnector.getConnection();
        Statement stmt = con.createStatement();
        stmt.execute("SELECT 1;");
        con.close();
    }

    /**
     * Test method for {@link version2.prototype.util.DatabaseConnector#getConnection(Config)}.
     * @throws SQLException
     */
    @Test
    public final void testGetConnectionConfig() throws SQLException {
        Config configInstance = Config.getAnInstance("src/test/config.xml");
        DatabaseConnection con = DatabaseConnector.getConnection(configInstance);
        Statement stmt = con.createStatement();
        stmt.execute("SELECT 1;");
        con.close();
    }

    /**
     * Test method for {@link version2.prototype.util.DatabaseConnector#getConnection(Config, boolean)} and {@link version2.prototype.util.DatabaseConnector#getConnection(Config, boolean, Integer)}.
     * @throws SQLException
     */
    @Test
    public final void testGetConnectionConfigBooleanInteger() throws SQLException {
        Config configInstance = Config.getAnInstance("src/test/config.xml");
        // Test Case 1
        DatabaseConnection con = DatabaseConnector.getConnection(configInstance, false);
        Statement stmt = con.createStatement();
        stmt.execute("SELECT 1;");

        ArrayList<DatabaseConnection> connections = new ArrayList<DatabaseConnection>();
        connections.add(con);
        for(int i=1; i < configInstance.getMaxNumOfConnectionsPerInstance(); i++)
        {
            // Test Case 3
            con = DatabaseConnector.getConnection(configInstance, false, 2);
            if(con == null) {
                fail("Connection["+ i +"] failed.");
            } else {
                connections.add(con);
            }
        }

        // Test Case 2
        con = DatabaseConnector.getConnection(configInstance, true);
        assertNull("Connection was made beyond limiter.", con);

        connections.remove(0).close();

        // Test Case 4
        con = DatabaseConnector.getConnection(configInstance, true, 2);
        assertNotNull("Connection failed to be made after reduction of connection count from max limit.", con);
        con.close();

        for(DatabaseConnection conn : connections)
        {
            conn.close();
        }
    }

}
