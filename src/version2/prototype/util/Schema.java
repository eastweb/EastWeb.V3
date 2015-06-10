package version2.prototype.util;

import java.sql.Connection;
import java.sql.SQLException;

import version2.prototype.ConfigReadException;

public class Schema {
    public static void RecreateSchema(String projectName, String pluginName) throws ConfigReadException, SQLException, ClassNotFoundException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final String mSchemaName = getSchemaName(projectName, pluginName);

        // Drop an existing schema with the same name
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        // Create the schema for this project
        query = String.format(
                "CREATE SCHEMA \"%s\"",
                mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        // Create the ZoneFields table
        query = String.format(
                "CREATE TABLE \"%1$s\".\"ZoneField\"\n" +
                        "(\n" +
                        "  \"ZoneFieldID\" serial PRIMARY KEY,\n" +
                        "  \"ShapeFile\" varchar(255) NOT NULL,\n" +
                        "  \"Field\" varchar(255) NOT NULL\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        // Create the Zones table
        query = String.format(
                "CREATE TABLE \"%1$s\".\"Zone\"\n" +
                        "(\n" +
                        "  \"ZoneID\" serial PRIMARY KEY,\n" +
                        "  \"ZoneFieldID\" integer REFERENCES \"%1$s\".\"ZoneField\" (\"ZoneFieldID\") NOT NULL,\n" +
                        "  \"FieldID\" integer NOT NULL\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        // Create an index for the Zones table's foreign key
        query = String.format(
                "CREATE INDEX \"FKI_Zone\" ON \"%1$s\".\"Zone\"(\"ZoneFieldID\")",
                mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        // Create the ZonalStats table
        query = String.format(
                "CREATE TABLE \"%1$s\".\"ZonalStat\"\n" +
                        "(\n" +
                        "  \"IndexID\" integer NOT NULL,\n" +
                        "  \"Year\" integer NOT NULL,\n" +
                        "  \"Day\" integer NOT NULL,\n" +
                        "  \"ZoneID\" integer REFERENCES \"%1$s\".\"Zone\" (\"ZoneID\") NOT NULL,\n" +
                        "  \"Count\" double precision NOT NULL,\n" +
                        "  \"Sum\" double precision NOT NULL,\n" +
                        "  \"Mean\" double precision NOT NULL,\n" +
                        "  \"StdDev\" double precision NOT NULL,\n" +
                        "  CONSTRAINT \"PK_ZonalStat\"\n" +
                        "      PRIMARY KEY (\"IndexID\", \"Year\", \"Day\", \"ZoneID\")\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        // Create an index for the ZonalStats table's foreign key
        query = String.format(
                "CREATE INDEX \"FKI_ZonalStat\" ON \"%1$s\".\"ZonalStat\"(\"ZoneID\")",
                mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        // Create Environmental Index table
        query = String.format(
                "CREATE TABLE \"%1$s\".\"Index\"\n" +
                        "(\n" +
                        "  \"IndexID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) NOT NULL\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        // Create DateGroup table
        query = String.format(
                "CREATE TABLE \"%1$s\".\"DateGroup\"\n" +
                        "(\n" +
                        "  \"DateGroupID\" serial PRIMARY KEY,\n" +
                        "  \"Year\" integer NOT NULL,\n" +
                        "  \"Day\" integer NOT NULL\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        // Create cache tables for each framework
        query = String.format(
                "CREATE TABLE \"%1$s\".\"DownloadCache\"\n" +
                        "(\n" +
                        "  \"DownloadCacheID\" serial PRIMARY KEY,\n" +
                        "  \"FullPath\" varchar(255) UNIQUE NOT NULL,\n" +
                        "  \"DateDirectory\" varchar(255) NOT NULL,\n" +
                        "  \"DataGroupID\" integer REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") NOT NULL,\n" +
                        "  \"Retrieved\" boolean DEFAULT FALSE\n" +
                        "  \"Processed\" boolean DEFAULT FALSE\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        query = String.format(
                "CREATE TABLE \"%1$s\".\"ProcessCache\"\n" +
                        "(\n" +
                        "  \"ProcessCacheID\" serial PRIMARY KEY,\n" +
                        "  \"FullPath\" varchar(255) UNIQUE NOT NULL,\n" +
                        "  \"DateDirectory\" varchar(255) NOT NULL,\n" +
                        "  \"DataGroupID\" integer REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") NOT NULL,\n" +
                        "  \"Retrieved\" boolean DEFAULT FALSE\n" +
                        "  \"Processed\" boolean DEFAULT FALSE\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        query = String.format(
                "CREATE TABLE \"%1$s\".\"IndicesCache\"\n" +
                        "(\n" +
                        "  \"IndicesCacheID\" serial PRIMARY KEY,\n" +
                        "  \"FullPath\" varchar(255) UNIQUE NOT NULL,\n" +
                        "  \"DateDirectory\" varchar(255) NOT NULL,\n" +
                        "  \"DataGroupID\" integer REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") NOT NULL,\n" +
                        "  \"Retrieved\" boolean DEFAULT FALSE\n" +
                        "  \"Processed\" boolean DEFAULT FALSE\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();

        query = String.format(
                "CREATE TABLE \"%1$s\".\"SummaryCache\"\n" +
                        "(\n" +
                        "  \"SummaryCacheID\" serial PRIMARY KEY,\n" +
                        "  \"FullPath\" varchar(255) UNIQUE NOT NULL,\n" +
                        "  \"DateDirectory\" varchar(255) NOT NULL,\n" +
                        "  \"DataGroupID\" integer REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") NOT NULL,\n" +
                        "  \"Retrieved\" boolean DEFAULT FALSE\n" +
                        "  \"Processed\" boolean DEFAULT FALSE\n" +
                        ")",
                        mSchemaName
                );
        conn.prepareStatement(query).executeUpdate();
    }

    /**
     * Gets the name of the specified project's database schema.
     * The returned name does not need to be quoted to use in SQL.
     */
    public static String getSchemaName(String project, String pluginName) {
        final String name = project + "_" + pluginName;
        final StringBuilder builder = new StringBuilder();
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
