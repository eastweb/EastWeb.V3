/**
 *
 */
package version2.prototype.util;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.ConnectionPoolDataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import version2.prototype.Config;
import version2.prototype.ErrorLog;

/**
 * @author michael.devos
 *
 */
public class C3P0ConnectionPool extends DatabaseConnectionPoolA {
    private final ComboPooledDataSource comboPDS;
    private final ConnectionPoolDataSource cpds;
    private final Connection testCon;
    private Integer postgresqlConnectionCount;

    /**
     * @param configInstance
     */
    public C3P0ConnectionPool(Config configInstance) {
        super(configInstance);

        if(configInstance.getMaxNumOfConnectionsPerInstance() <= 1) {
            ErrorLog.add(configInstance, "Maximum number of allowed database connections must exceed 1. Please increase this value in config.xml.",
                    new Exception("Maximum number of allowed database connections must exceed 1."));
        }

        // Setup connection pool
        comboPDS = new ComboPooledDataSource();
        try {
            comboPDS.setDriverClass("org.postgresql.Driver");
        } catch (PropertyVetoException e) {
            ErrorLog.add(Config.getInstance(), "Failed to find the PostgreSQL JDBC driver.", e);
        }

        comboPDS.setJdbcUrl(configInstance.getDatabaseHost() + ":" + configInstance.getPort() + "/" + configInstance.getDatabaseName());
        comboPDS.setUser(configInstance.getDatabaseUsername());
        comboPDS.setPassword(configInstance.getDatabasePassword());
        comboPDS.setMaxPoolSize(configInstance.getMaxNumOfConnectionsPerInstance());
        cpds = comboPDS.getConnectionPoolDataSource();

        Connection temp = null;
        try {
            temp = cpds.getPooledConnection().getConnection();
        } catch (SQLException e) {
            ErrorLog.add(configInstance, "Problem creating initial connection during connection pool setup.", e);
        }
        testCon = temp;

        postgresqlConnectionCount = 0;
    }

    /* (non-Javadoc)
     * @see version2.prototype.util.DatabaseConnectionPoolA#close()
     */
    @Override
    public void close() {
        synchronized(super.DatabaseConnectorLock) {
            if(testCon != null) {
                try {
                    testCon.close();
                } catch (SQLException e) {
                    ErrorLog.add(configInstance, "Problem closing HikariConnectionPool stored connection.", e);
                }
            }
            comboPDS.close();
        }
    }

    /* (non-Javadoc)
     * @see version2.prototype.util.DatabaseConnectionPoolA#getConnection(boolean, java.lang.Integer)
     */
    @Override
    public DatabaseConnection getConnection() {
        DatabaseConnection dbCon = null;
        //        Statement stmt = null;
        //        ResultSet rs = null;
        Integer connectionID = null;

        synchronized(super.DatabaseConnectorLock) {
            //            try {
            //                stmt = testCon.createStatement();
            //                rs = stmt.executeQuery("SELECT sum(numbackends) as row_count FROM pg_stat_database;");
            //                if(rs != null && rs.next()) {
            //                    postgresqlConnectionCount = rs.getInt("row_count");
            //                }
            //                stmt.close();
            //                rs.close();
            //            } catch(SQLException e) {
            //                ErrorLog.add(configInstance, "Problem while checking actve database connection count.", e);
            //            }

            //            if(postgresqlConnectionCount < configInstance.getMaxNumOfConnectionsPerInstance()) {
            connectionID = getLowestAvailableConnectionID();
            Connection con = null;
            try {
                System.out.println("\nBefore getting connection: " +
                        "\n  num_connections: " + comboPDS.getNumConnections() +
                        "\n  num_busy_connections: " + comboPDS.getNumBusyConnections() +
                        "\n  num_idle_connections: " + comboPDS.getNumIdleConnections());

                con = cpds.getPooledConnection().getConnection();

                System.out.println("\nAfter getting connection: " +
                        "\n  num_connections: " + comboPDS.getNumConnections() +
                        "\n  num_busy_connections: " + comboPDS.getNumBusyConnections() +
                        "\n  num_idle_connections: " + comboPDS.getNumIdleConnections());
            } catch (SQLException e) {
                ErrorLog.add(configInstance, "Problem getting new connection from connection pool.", e);
            }
            if(connectionID > -1 && con != null) {
                dbCon = new DatabaseConnection(configInstance, this, con, connectionID);
            } else {
                System.err.println("Problem setting up new connection (connection ID: " + connectionID + ", connection: " + (con == null ? "is null)." : "is not null)."));
                if(connectionID > -1) {
                    releaseConnectionID(connectionID);
                }
            }
            //            }
        }

        return dbCon;
    }

    /* (non-Javadoc)
     * @see version2.prototype.util.DatabaseConnectionPoolA#getConnectionCount()
     */
    @Override
    public int getConnectionCount() {
        Statement stmt = null;
        ResultSet rs = null;
        int count = -1;

        synchronized(super.DatabaseConnectorLock) {
            try {
                //                stmt = testCon.createStatement();
                //                rs = stmt.executeQuery("SELECT sum(numbackends) as row_count FROM pg_stat_database;");
                //                if(rs != null && rs.next()) {
                //                    postgresqlConnectionCount = rs.getInt("row_count");
                //                    count = postgresqlConnectionCount;
                //                }
                //                stmt.close();
                //                rs.close();
                count = comboPDS.getNumConnections();
            } catch(SQLException e) {
                ErrorLog.add(configInstance, "Problem while checking actve database connection count.", e);
            }
        }

        return count;
    }

    /* (non-Javadoc)
     * @see version2.prototype.util.DatabaseConnectionPoolA#endConnection(java.lang.Integer)
     */
    @Override
    public void endConnection(Integer ID) {
        synchronized(super.DatabaseConnectorLock) {
            releaseConnectionID(ID);
        }
    }

}
