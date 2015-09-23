/**
 *
 */
package version2.prototype.util;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sql.ConnectionPoolDataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import version2.prototype.Config;
import version2.prototype.ErrorLog;

/**
 * @author michael.devos
 *
 */
public class DatabaseConnector {
    private static DatabaseConnector connector = null;
    private static ComboPooledDataSource comboPDS = null;
    private static ConnectionPoolDataSource cpds = null;
    private static Integer connectionCount = 0;
    private static Config configInstance = null;
    private static Boolean configLock = true;   // Value doesn't matter.
    private static Map<Integer, DatabaseConnection> connections = new HashMap<Integer, DatabaseConnection>();
    private static Boolean DatabaseConnectorLock = true;  // Value doesn't matter.
    private static BitSet connectionIDs = null;
    private static Boolean connectionIDsLock = true;    // Value doesn't matter.
    private static ConcurrentLinkedQueue<Long> connectionRequests = new ConcurrentLinkedQueue<Long>();
    private static long msTimeout = 120000;     // 120 seconds

    private static Integer postgresqlConnectionCount = 0;

    protected DatabaseConnector()
    {
        // avoid public instantiation
    }

    /**
     * Safely closes the connection pool and open resources if it has been instantiated.
     */
    public static void Close()
    {
        if(comboPDS != null) {
            comboPDS.close();
        }
    }

    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     * @return valid Connection object if successfully connected, otherwise null
     */
    public static DatabaseConnection getConnection()
    {
        if(configInstance == null)
        {
            synchronized(configLock)
            {
                if(configInstance == null)
                {
                    configInstance = Config.getInstance();

                    // Driver Connection Check
                    try {
                        Class.forName("org.postgresql.Driver");
                    } catch (ClassNotFoundException e) {
                        ErrorLog.add(Config.getInstance(), "Failed to find the PostgreSQL JDBC driver.", e);
                    }
                }
            }
        }

        return getConnection(configInstance, false, null);
    }

    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     * @param myConfigInstance  - Config reference. Setting this to null causes it to lookup the default config instance.
     * @return valid Connection object if successfully connected, otherwise null
     */
    public static DatabaseConnection getConnection(Config myConfigInstance)
    {
        if(myConfigInstance == null)
        {
            myConfigInstance = Config.getInstance();
        }

        return getConnection(myConfigInstance, false, null);
    }

    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     * @param myConfigInstance  - Config reference. Setting this to null causes it to lookup the default config instance.
     * @param failFast  - TRUE if this method is to return after failing on the first attempt with a null, FALSE if it is to loop continuously or attempt connecting the number of times specified if specified.
     * Each connection establish retry is done after putting the thread to sleep for 2 seconds.
     *
     * @return valid Connection object if successfully connected, otherwise null
     */
    public static DatabaseConnection getConnection(Config myConfigInstance, boolean failFast)
    {
        if(myConfigInstance == null)
        {
            myConfigInstance = Config.getInstance();
        }

        return getConnection(myConfigInstance, failFast, null);
    }

    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     * @param myConfigInstance
     * @param failFast  - TRUE if this method is to return after failing on the first attempt with a null, FALSE if it is to loop continuously or attempt connecting the number of times specified if specified.
     * Each connection establish retry is done after putting the thread to sleep for 2 seconds.
     * @param maxAttemptCount  - The maximum number of times to attempt connecting. Leave this blank or null if infinite is desired.
     *
     * @return valid Connection object if successfully connected, otherwise null
     */
    public static DatabaseConnection getConnection(Config myConfigInstance, boolean failFast, Integer maxAttemptCount)
    {
        DatabaseConnection con = null;
        int currentTest = 0;

        if(myConfigInstance == null) {
            myConfigInstance = Config.getInstance();

            if(myConfigInstance == null) {
                return null;
            }
        }

        if(configInstance == null)
        {
            // Driver Connection Check
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                ErrorLog.add(Config.getInstance(), "Failed to find the PostgreSQL JDBC driver.", e);
            }
        }

        if(connectionIDs == null) {
            synchronized(connectionIDsLock) {
                if(connectionIDs == null) {
                    connectionIDs = new BitSet(myConfigInstance.getMaxNumOfConnectionsPerInstance());
                }
            }
        }

        //        if(cpds == null)
        //        {
        //            connector = new DatabaseConnector();
        //
        //            // Setup connection pool
        //            comboPDS = new ComboPooledDataSource("test");
        //            try {
        //                comboPDS.setDriverClass("org.postgresql.Driver");
        //            } catch (PropertyVetoException e) {
        //                ErrorLog.add(Config.getInstance(), "Failed to find the PostgreSQL JDBC driver.", e);
        //            }
        //
        //            comboPDS.setJdbcUrl(myConfigInstance.getDatabaseHost() + ":" + myConfigInstance.getPort() + "/" + myConfigInstance.getDatabaseName());
        //            comboPDS.setUser(myConfigInstance.getDatabaseUsername());
        //            comboPDS.setPassword(myConfigInstance.getDatabasePassword());
        //            cpds = comboPDS.getConnectionPoolDataSource();
        //
        //            // Set properties
        //            //            cpds.setMinPoolSize(4);
        //            //            cpds.setAcquireIncrement(5);
        //            //            cpds.setMaxPoolSize(myConfigInstance.getMaxNumOfConnectionsPerInstance());
        //        }

        //        synchronized(connectionRequests) {
        //            Long threadID = connectionRequests.poll();
        //            if(threadID != null) {
        //                threadID.notify();
        //            }
        //        }

        Integer connectionID = null;
        do{
            currentTest++;
            synchronized(DatabaseConnectorLock)
            {
                if((connectionCount + 1) < myConfigInstance.getMaxNumOfConnectionsPerInstance())
                {
                    try {
                        // Test PostgreSQL connection count
                        Connection testCon = DriverManager.getConnection(myConfigInstance.getDatabaseHost() + ":" + myConfigInstance.getPort() + "/" + myConfigInstance.getDatabaseName(),
                                myConfigInstance.getDatabaseUsername(), myConfigInstance.getDatabasePassword());
                        ResultSet rs = testCon.createStatement().executeQuery("SELECT sum(numbackends) as row_count FROM pg_stat_database;");
                        if(rs != null && rs.next()) {
                            postgresqlConnectionCount = rs.getInt("row_count");
                        }
                        rs.close();
                        testCon.close();

                        if(postgresqlConnectionCount < myConfigInstance.getMaxNumOfConnectionsPerInstance())
                        {
                            System.out.println("\nBefore making connection: " +
                                    "\n  num_connections: " + connectionCount);
                            connectionID = getLowestAvailableConnectionID();
                            if(connectionID != -1)
                            {
                                //                                con = new DatabaseConnection(configInstance,
                                //                                        new DatabaseConnector(),
                                //                                        DriverManager.getConnection(myConfigInstance.getDatabaseHost() + ":" + myConfigInstance.getPort() + "/" + myConfigInstance.getDatabaseName(),
                                //                                                myConfigInstance.getDatabaseUsername(), myConfigInstance.getDatabasePassword()),
                                //                                                connectionID);
                                System.out.println("\nAfter making connection: " +
                                        "\n  postgresqlConnectionCount: " + postgresqlConnectionCount +
                                        "\n  num_connections: " + connectionCount);
                            } else {
                                System.out.println("Connection ID was -1");
                            }
                        }
                    } catch (SQLException e) {
                        ErrorLog.add(myConfigInstance, "Problem creating DatbaseConnection object.", e);
                    }
                }

                if(postgresqlConnectionCount >= (myConfigInstance.getMaxNumOfConnectionsPerInstance() / 2)) {
                    System.gc();
                }

                if(con != null && connectionID != null) {
                    ++connectionCount;
                    connections.put(connectionID, con);
                } else if(connectionID != null){
                    releaseConnectionID(connectionID);
                }
            }
            if(con == null || connectionID == null)
            {
                if(!failFast && (maxAttemptCount == null || currentTest <= maxAttemptCount))
                {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        ErrorLog.add(myConfigInstance, "Problem sleeping thread until next connection available.", e);
                    }
                    //                    synchronized(connectionRequests) {
                    //                        Long threadID = Thread.currentThread().getId();
                    //                        connectionRequests.add(threadID);
                    //                        try {
                    //                            threadID.wait(msTimeout);
                    //                        } catch (InterruptedException e) {
                    //                            ErrorLog.add(myConfigInstance, "Problem setting thread to wait for connection.", e);
                    //                        }
                    //                    }
                }
            }
        }while(con == null && !failFast && (maxAttemptCount == null || currentTest <= maxAttemptCount));

        System.out.println("Current connectionCount: " + connectionCount);

        //        try {
        //            System.out.println("\nBefore making connection: " + descriptor +
        //                    "\n  num_connections: " + comboPDS.getNumConnectionsDefaultUser() +
        //                    "\n  num_busy_connections: " + comboPDS.getNumBusyConnectionsDefaultUser() +
        //                    "\n  num_idle_connections: " + comboPDS.getNumIdleConnectionsDefaultUser());
        //            Connection tempCon = cpds.getPooledConnection().getConnection();
        //            ResultSet rs = tempCon.createStatement().executeQuery("SELECT sum(numbackends) as row_count FROM pg_stat_database;");
        //            if(rs != null && rs.next()) {
        //                postgresqlConnectionCount = rs.getInt("row_count");
        //            }
        //            rs.close();
        //            Integer ID = getLowestAvailableConnectionID();
        //            con = new DatabaseConnection(descriptor, myConfigInstance, connector, tempCon, ID);
        //            ++connectionCount;
        //            connections.put(ID, con);
        //            System.out.println("\nAfter making connection: "  + descriptor +
        //                    "\n  postgresqlConnectionCount: " + postgresqlConnectionCount +
        //                    "\n  num_connections: " + comboPDS.getNumConnectionsDefaultUser() +
        //                    "\n  num_busy_connections: " + comboPDS.getNumBusyConnectionsDefaultUser() +
        //                    "\n  num_idle_connections: " + comboPDS.getNumIdleConnectionsDefaultUser());
        //        } catch (SQLException e) {
        //            ErrorLog.add(myConfigInstance, "Problem creating Connection object.", e);
        //        }
        return con;
    }

    /**
     * Closes all currently opened connections and resets connection count.
     * @throws SQLException
     * @throws Exception
     */
    public static void closeAllConnections() throws SQLException, Exception{
        int connectionSize, count;

        synchronized(DatabaseConnectorLock)
        {
            ArrayList<Integer> activeIDs = new ArrayList<Integer>();
            for(Integer id : connections.keySet())
            {
                activeIDs.add(id);
            }
            for(Integer id : activeIDs)
            {
                if(!connections.get(id).isClosed()) {
                    connections.get(id).close();
                }
            }

            connectionSize = connections.size();
            count = connectionCount;
        }

        if(connectionSize != 0)
        {
            throw new Exception("Failed to update connections listing when closing all connections. Size of listing = " + connections.size() + ".");
        }

        if(count != 0)
        {
            throw new Exception("Failed to correctly update connection count when closing all connections. Connection count = " + connectionCount + ".");
        }
    }

    /**
     * Gets the current number of active database connections.
     * @return  int - number of active database connections.
     */
    //    public static int getConnectionCount() {
    //        int count;
    //
    //        synchronized(DatabaseConnectorLock) {
    //            count = connectionCount;
    //        }
    //        return count;
    //    }

    /**
     * Ends the connection and updates the state of the DatabaseConnector.
     * @param ID  - DatabaseConnection ID
     */
    public void endConnection(Integer ID)
    {
        int count;

        synchronized(DatabaseConnectorLock)
        {
            if(connections.remove(ID) != null) {
                --connectionCount;
                releaseConnectionID(ID);
            }
            count = connectionCount;
        }

        if(count < 0) {
            ErrorLog.add(configInstance, "Connection count became negative.", new Exception("Connection count became negative."));
        }
    }

    protected static int getLowestAvailableConnectionID()
    {
        int id = -1;

        synchronized(DatabaseConnectorLock)
        {
            id = connectionIDs.nextClearBit(0);

            if(IsIDValid(id, connectionIDs)) {
                connectionIDs.set(id);
            }
            else {
                id = -1;
            }
        }

        return id;
    }

    protected static void releaseConnectionID(Integer id)
    {
        System.out.println("connectionIDs: " + connectionIDs);
        System.out.println("Releasing connection: " + id);
        if(IsIDValid(id, connectionIDs)) {
            connectionIDs.clear(id);
        }
        System.out.println("connectionIDs: " + connectionIDs);
    }

    protected static boolean IsIDValid(Integer id, BitSet set)
    {
        if((id != null) && (id >= 0) && (id < set.size())) {
            return true;
        } else {
            return false;
        }
    }
}
