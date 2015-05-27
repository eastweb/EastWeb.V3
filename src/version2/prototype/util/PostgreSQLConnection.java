package version2.prototype.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;


public class PostgreSQLConnection {
    public static Connection getConnection() throws ConfigReadException, SQLException {
        // Driver Connection Check
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ConfigReadException("Failed to find the PostgreSQL JDBC driver");
        }

        return DriverManager.getConnection(
                Config.getInstance().getDatabaseHost(),
                Config.getInstance().getDatabaseUsername(),
                Config.getInstance().getDatabasePassword()
                );
    }
}
