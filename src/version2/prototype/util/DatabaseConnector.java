/**
 *
 */
package version2.prototype.util;

import java.sql.DriverManager;
import java.sql.SQLException;

import version2.prototype.Config;
import version2.prototype.ErrorLog;

/**
 * @author michael.devos
 *
 */
public class DatabaseConnector {
    private static Integer connectionCount = 0;
    private static Config configInstance = null;

    protected DatabaseConnector()
    {

    }

    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     * @return valid Connection object if successfully connected, otherwise null
     */
    public static DatabaseConnection getConnection()
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

        if(configInstance == null) {
            return null;
        } else {
            return getConnection(configInstance, false, null);
        }
    }

    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     * @param configInstance  - Config reference. Setting this to null causes it to lookup the default config instance.
     * @return valid Connection object if successfully connected, otherwise null
     */
    public static DatabaseConnection getConnection(Config myConfigInstance)
    {
        if(myConfigInstance == null)
        {
            myConfigInstance = Config.getInstance();

            // Driver Connection Check
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                ErrorLog.add(Config.getInstance(), "Failed to find the PostgreSQL JDBC driver.", e);
            }
        }

        if(myConfigInstance == null) {
            return null;
        } else {
            return getConnection(myConfigInstance, false, null);
        }
    }

    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     * @param configInstance  - Config reference. Setting this to null causes it to lookup the default config instance.
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

            // Driver Connection Check
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                ErrorLog.add(Config.getInstance(), "Failed to find the PostgreSQL JDBC driver.", e);
            }
        }

        if(myConfigInstance == null) {
            return null;
        } else {
            return getConnection(myConfigInstance, failFast, null);
        }
    }

    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     * @param config
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

        if(myConfigInstance == null)
        {
            myConfigInstance = Config.getInstance();

            // Driver Connection Check
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                ErrorLog.add(Config.getInstance(), "Failed to find the PostgreSQL JDBC driver.", e);
            }
        }

        do{
            currentTest++;
            synchronized(connectionCount)
            {
                if(connectionCount < myConfigInstance.getMaxNumOfConnectionsPerInstance())
                {
                    try {
                        con = new DatabaseConnection(new DatabaseConnector(), DriverManager.getConnection(
                                myConfigInstance.getDatabaseHost() + ":" + myConfigInstance.getPort() + "/" + myConfigInstance.getDatabaseName(),
                                myConfigInstance.getDatabaseUsername(),
                                myConfigInstance.getDatabasePassword()
                                ));
                    } catch (SQLException e) {
                        ErrorLog.add(myConfigInstance, "Problem creating DatbaseConnection object.", e);
                    }
                }
            }
            if(con == null)
            {
                if(!failFast && (maxAttemptCount == null || currentTest <= maxAttemptCount))
                {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        ErrorLog.add(myConfigInstance, "Problem sleeping thread until next connection available.", e);
                    }
                }
            } else {
                connectionCount++;
            }
        }while(con == null && !failFast && (maxAttemptCount == null || currentTest <= maxAttemptCount));

        return con;
    }

    /**
     * Register closing of a DatabaseConnection object.
     */
    public void closingConnection()
    {
        synchronized(connectionCount)
        {
            connectionCount--;
            if(connectionCount < 0) {
                ErrorLog.add(configInstance, "Connection count became negative.", new Exception("Connection count became negative."));
            }
        }
    }
}
