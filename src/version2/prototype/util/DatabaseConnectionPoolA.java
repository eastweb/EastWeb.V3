/**
 *
 */
package version2.prototype.util;

import java.util.BitSet;

import version2.prototype.Config;

/**
 * @author michael.devos
 *
 */
public abstract class DatabaseConnectionPoolA {
    protected final Config configInstance;
    protected Boolean DatabaseConnectorLock;  // Value doesn't matter.
    protected BitSet connectionIDs;

    protected DatabaseConnectionPoolA(Config configInstance)
    {
        if(configInstance == null)
        {
            configInstance = Config.getInstance();
        }
        this.configInstance = configInstance;
        DatabaseConnectorLock = new Boolean(true);
        connectionIDs = new BitSet(configInstance.getMaxNumOfConnectionsPerInstance());
    }

    /**
     * Safely closes the connection pool and open resources if it has been instantiated.
     */
    public abstract void close();

    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     * @param failFast  - TRUE if this method is to return after failing on the first attempt with a null, FALSE if it is to loop continuously or attempt connecting the number of times specified if specified.
     * Each connection establish retry is done after putting the thread to sleep for 2 seconds.
     * @param maxAttemptCount  - The maximum number of times to attempt connecting. Leave this blank or null if infinite is desired.
     *
     * @return valid Connection object if successfully connected, otherwise null
     */
    public abstract DatabaseConnection getConnection(boolean failFast, Integer maxAttemptCount);

    /**
     * Gets the current number of active database connections.
     * @return  int - number of active database connections.
     */
    public abstract int getConnectionCount();

    /**
     * Ends the connection and updates the state of the DatabaseConnector.
     * @param ID  - DatabaseConnection ID
     */
    public abstract void endConnection(Integer ID);

    protected int getLowestAvailableConnectionID()
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

    protected void releaseConnectionID(Integer id)
    {
        if(IsIDValid(id, connectionIDs)) {
            connectionIDs.clear(id);
        }
    }

    protected boolean IsIDValid(Integer id, BitSet set)
    {
        if((id != null) && (id >= 0) && (id < set.size())) {
            return true;
        } else {
            return false;
        }
    }
}
