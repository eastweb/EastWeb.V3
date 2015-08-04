package version2.prototype.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.ErrorLog;


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
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConfigReadException
     */
    public static Connection getConnection() throws SQLException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException {
        // Driver Connection Check
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            ErrorLog.add(Config.getInstance(), "Failed to find the PostgreSQL JDBC driver", e);
        }

        Config configInstance = Config.getInstance();
        return DriverManager.getConnection(
                configInstance.getDatabaseHost() + ":" + configInstance.getPort() + "/" + configInstance.getDatabaseName(),
                configInstance.getDatabaseUsername(),
                configInstance.getDatabasePassword()
                );
    }

    /**
     * Creates a valid Connection object if a connection could be established with the PostgreSQL database using information from config.xml.
     * @param configInstance
     *
     * @return valid Connection object if successfully connected, otherwise null
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConfigReadException
     */
    public static Connection getConnection(Config configInstance) throws SQLException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException {
        // Driver Connection Check
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            ErrorLog.add(configInstance, "Failed to find the PostgreSQL JDBC driver", e);
        }

        return DriverManager.getConnection(
                configInstance.getDatabaseHost() + ":" + configInstance.getPort() + "/" + configInstance.getDatabaseName(),
                configInstance.getDatabaseUsername(),
                configInstance.getDatabasePassword()
                );
    }
}
