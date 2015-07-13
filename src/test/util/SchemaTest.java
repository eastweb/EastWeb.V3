package test.util;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.Test;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schema;

public class SchemaTest {
    private String testGlobalSchema = "Test_EASTWeb";

    @Test
    public final void testGetSchemaName() {
        String schemaName = Schema.getSchemaName("Test_Project", "Test_Plugin");
        assertTrue("Schema name is " + schemaName, schemaName.equals("test_project_test_plugin"));

        schemaName = Schema.getSchemaName("1Test_Project", "Test_Plugin");
        assertTrue("Schema name is " + schemaName, schemaName.equals("_test_project_test_plugin"));

        schemaName = Schema.getSchemaName(" ", " ");
        assertTrue("Schema name is " + schemaName, schemaName.equals("___"));
    }

    @Test
    public final void testCreateProjectPluginSchema() throws ConfigReadException, ClassNotFoundException, SQLException {
        Connection con = PostgreSQLConnection.getConnection();
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        ArrayList<String> summaryNames = new ArrayList<String>();
        summaryNames.add("Count");
        summaryNames.add("Sum");
        summaryNames.add("Mean");
        summaryNames.add("StdDev");
        String testSchemaName = Schema.getSchemaName("Test_Project", "Test_Plugin");

        // Remove test schemas if they exist
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testGlobalSchema
                ));
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testSchemaName
                ));

        // Run method under test - defined for MODIS plugin
        Schema.CreateProjectPluginSchema(testGlobalSchema, "Test_Project", "Test_Plugin", LocalDate.now(), 8, 3, summaryNames);

        // Check the created test schemas
        String query = "select n.nspname as \"Name\", count(*) over() as \"RowCount\" " +
                "from pg_catalog.pg_namespace n " +
                "where (n.nspname = '" + testGlobalSchema + "' OR n.nspname = '" + testSchemaName + "') " +
                "AND pg_catalog.pg_get_userbyid(n.nspowner) = '" + Config.getInstance().getDatabaseUsername() + "' " +
                "order by \"RowCount\" desc;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertTrue("Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 2);
        } else {
            fail("ResultSet is null from querying schemas");
        }

        // Check the existence of the test global schema tables
        query = "SELECT c.relname as \"Name\", count(*) over() as \"RowCount\" " +
                "FROM pg_catalog.pg_class c LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                "WHERE c.relkind = 'r' AND n.nspname = '" + testGlobalSchema + "' " +
                "AND pg_catalog.pg_get_userbyid(c.relowner) = '" + Config.getInstance().getDatabaseUsername() + "' " +
                "ORDER BY \"RowCount\" desc;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertTrue("Test Global Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 11);

            // Check global schema table names
            do{
                switch(rs.getString("Name"))
                {
                case "DateGroup": break;
                case "Index": break;
                case "Region": break;
                case "Zone": break;
                case "ZoneVar": break;
                case "ExpectedResults": break;
                case "Project": break;
                case "Plugin": break;
                case "GDExpectedResults": break;
                case "GlobalDownloader": break;
                case "Download": break;
                default: fail("Unknown table in test global schema: " + rs.getString("Name"));
                }
            } while(rs.next());
        } else {
            fail("ResultSet is null from querying test global schema tables");
        }

        // Check the existence of the test schema tables
        query = "SELECT c.relname as \"Name\", count(*) over() as \"RowCount\" " +
                "FROM pg_catalog.pg_class c LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                "WHERE c.relkind = 'r' AND n.nspname = '" + testSchemaName + "' " +
                "AND pg_catalog.pg_get_userbyid(c.relowner) = '" + Config.getInstance().getDatabaseUsername() + "' " +
                "ORDER BY \"RowCount\" desc;";
        rs = stmt.executeQuery(query);
        if(rs != null)
        {
            rs.next();
            assertTrue("Test Schema Check row count is " + rs.getInt("RowCount"), rs.getInt("RowCount") == 6);

            // Check schema table names
            do{
                switch(rs.getString("Name"))
                {
                case "ZoneMapping": break;
                case "ZoneField": break;
                case "ZonalStat": break;
                case "DownloadCache": break;
                case "ProcessorCache": break;
                case "IndicesCache": break;
                default: fail("Unknown table in test schema: " + rs.getString("Name"));
                }
            } while(rs.next());
        } else {
            fail("ResultSet is null from querying test schema tables");
        }

        // Cleanup
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testGlobalSchema
                ));
        stmt.execute(String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                testSchemaName
                ));
        stmt.close();
        rs.close();
    }
}
