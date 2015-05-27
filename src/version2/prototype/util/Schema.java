package version2.prototype.util;

import java.sql.Connection;
import java.sql.SQLException;

import version2.prototype.ProjectInfo;
import version2.prototype.ConfigReadException;

public class Schema {
    public static void RecreateSchema(ProjectInfo project) throws ConfigReadException, SQLException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final String mSchemaName = getSchemaName(project);

        // Drop an existing schema with the same name
        String _query = String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                mSchemaName
                );
        conn.prepareStatement(_query).executeUpdate();

        // Create the schema for this project
        _query = String.format(
                "CREATE SCHEMA \"%s\"",
                mSchemaName
                );
        conn.prepareStatement(_query).executeUpdate();

        // Create the ZoneFields table
        _query = String.format(
                "CREATE TABLE \"%1$s\".\"ZoneFields\"\n" +
                        "(\n" +
                        "  \"zoneFieldID\" serial NOT NULL,\n" +
                        "  \"shapefile\" text NOT NULL,\n" +
                        "  \"field\" text NOT NULL,\n" +
                        "  CONSTRAINT \"pk_ZoneFields\"\n" +
                        "      PRIMARY KEY (\"zoneFieldID\")\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(_query).executeUpdate();

        // Create the Zones table
        _query = String.format(
                "CREATE TABLE \"%1$s\".\"Zones\"\n" +
                        "(\n" +
                        "  \"zoneID\" serial NOT NULL,\n" +
                        "  \"zoneFieldID\" integer NOT NULL,\n" +
                        "  \"fieldID\" integer NOT NULL,\n" +
                        "  CONSTRAINT \"pk_Zones\"\n" +
                        "      PRIMARY KEY (\"zoneID\"),\n" +
                        "  CONSTRAINT \"fk_Zones\"\n" +
                        "      FOREIGN KEY (\"zoneFieldID\")\n" +
                        "      REFERENCES \"%1$s\".\"ZoneFields\" (\"zoneFieldID\")\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(_query).executeUpdate();

        // Create an index for the Zones table's foreign key
        _query = String.format(
                "CREATE INDEX \"fki_Zones\" ON \"%1$s\".\"Zones\"(\"zoneFieldID\")",
                mSchemaName
                );
        conn.prepareStatement(_query).executeUpdate();

        // Create the ZonalStats table
        _query = String.format(
                "CREATE TABLE \"%1$s\".\"ZonalStats\"\n" +
                        "(\n" +
                        "  \"index\" integer NOT NULL,\n" +
                        "  \"year\" integer NOT NULL,\n" +
                        "  \"day\" integer NOT NULL,\n" +
                        "  \"zoneID\" integer NOT NULL,\n" +
                        "  \"Count\" double precision NOT NULL,\n" +
                        "  \"Sum\" double precision NOT NULL,\n" +
                        "  \"Mean\" double precision NOT NULL,\n" +
                        "  \"StdDev\" double precision NOT NULL,\n" +
                        "  CONSTRAINT \"pk_ZonalStats\"\n" +
                        "      PRIMARY KEY (\"index\", \"year\", \"day\", \"zoneID\"),\n" +
                        "  CONSTRAINT \"fk_ZonalStats\"\n" +
                        "      FOREIGN KEY (\"zoneID\")\n" +
                        "      REFERENCES \"%1$s\".\"Zones\" (\"zoneID\")\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(_query).executeUpdate();

        // Create an index for the ZonalStats table's foreign key
        _query = String.format(
                "CREATE INDEX \"fki_ZonalStats\" ON \"%1$s\".\"ZonalStats\"(\"zoneID\")",
                mSchemaName
                );
        conn.prepareStatement(_query).executeUpdate();

        // Create Environmental Indices table
        _query = String.format(
                "CREATE  TABLE \"%1$s\".\"Indicies\"\n" +
                        "(\n" +
                        "  \"index\" serial NOT NULL,\n" +
                        "  \"name\" text NOT NULL,\n" +
                        "  CONSTRAINT \"pk_Indicies\"\n" +
                        "      PRIMARY KEY (\"index\")\n" +
                        ")",
                        mSchemaName
                );
    }

    /**
     * Gets the name of the specified project's database schema.
     * The returned name does not need to be quoted to use in SQL.
     */
    public static String getSchemaName(ProjectInfo project) {
        final String name = project.getName();
        final StringBuilder builder = new StringBuilder("project_");
        for (int index = 0; index < name.length(); ) {
            final int codePointIn = name.codePointAt(index);
            index += Character.charCount(codePointIn);

            final int codePointOut;
            if (codePointIn >= 'A' && codePointIn <= 'Z') {
                // Convert to upper-case letters to lower-case
                codePointOut = codePointIn - 'A' + 'a';
            } else if ((codePointIn >= 'a' && codePointIn <= 'z') ||
                    (codePointIn >= '0' && codePointIn <= '9')) {
                // Preserve lower-case letters and digits
                codePointOut = codePointIn;
            } else {
                // Make anything else an underscore
                codePointOut = '_';
            }
            builder.appendCodePoint(codePointOut);
        }
        return builder.toString();
    }

    /**
     * Gets the name of the specified project's database schema.
     * The returned name does not need to be quoted to use in SQL.
     */
    public static String getSchemaName(String project) {
        final String name = project;
        final StringBuilder builder = new StringBuilder("project_");
        for (int index = 0; index < name.length(); ) {
            final int codePointIn = name.codePointAt(index);
            index += Character.charCount(codePointIn);

            final int codePointOut;
            if (codePointIn >= 'A' && codePointIn <= 'Z') {
                // Convert to upper-case letters to lower-case
                codePointOut = codePointIn - 'A' + 'a';
            } else if ((codePointIn >= 'a' && codePointIn <= 'z') ||
                    (codePointIn >= '0' && codePointIn <= '9')) {
                // Preserve lower-case letters and digits
                codePointOut = codePointIn;
            } else {
                // Make anything else an underscore
                codePointOut = '_';
            }
            builder.appendCodePoint(codePointOut);
        }
        return builder.toString();
    }
}
