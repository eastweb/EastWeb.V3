package version2.prototype.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import version2.prototype.Config;


/**
 * Handles establishing a connection to the PostgreSQL database.
 *
 * @author michael.devos
 *
 */
public class PostgreSQLConnection {
    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     *
     * @return valid Connection object if successfully connected, otherwise null
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        // Driver Connection Check
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Failed to find the PostgreSQL JDBC driver", e);
        }

        return DriverManager.getConnection(
                Config.getInstance().getDatabaseHost(),
                Config.getInstance().getDatabaseUsername(),
                Config.getInstance().getDatabasePassword()
                );
    }
}
