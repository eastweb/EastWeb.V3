package version2.prototype.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;


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
            throw new ClassNotFoundException("Failed to find the PostgreSQL JDBC driver", e);
        }

        try {
            return DriverManager.getConnection(
                    Config.getInstance().getDatabaseHost(),
                    Config.getInstance().getDatabaseUsername(),
                    Config.getInstance().getDatabasePassword()
                    );
        } catch (ConfigReadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
