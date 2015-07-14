package version2.prototype.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import version2.prototype.ConfigReadException;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;

/**
 * Represents a schema in the PostgreSQL database. Allows for recreating/creating of required schemas for EASTWeb.
 *
 * @author michael.devos
 *
 */
public class Schemas {
    /**
     * Recreates or creates a full schema identified by the given project name and plugin name.
     * Creates all database cache tables required by frameworks and download classes.
     *
     * @param globalEASTWebSchema  - the global schema name (can be gotten from Config.getInstance().getGlobalSchema()
     * @param projectName  - name of project to create schema for
     * @param pluginName  - name of plugin to create schema for
     * @param startDate
     * @param daysPerInputFile
     * @param numOfIndices
     * @param summaries
     * @param summaryNames
     * @param extraDownloadFiles
     * @throws ConfigReadException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void CreateProjectPluginSchema(String globalEASTWebSchema, String projectName, String pluginName, LocalDate startDate, int daysPerInputFile, int numOfIndices,
            ArrayList<ProjectInfoSummary> summaries, ArrayList<String> summaryNames, ArrayList<String> extraDownloadFiles) throws ConfigReadException, SQLException, ClassNotFoundException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        ResultSet rs;
        final String mSchemaName = getSchemaName(projectName, pluginName);

        // Drop an existing schema with the same name
        String query = String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                mSchemaName
                );
        stmt.executeUpdate(query);

        // Create the schema for this project
        query = String.format(
                "CREATE SCHEMA \"%s\"",
                mSchemaName
                );
        stmt.executeUpdate(query);

        // Create the global schema if it doesn't exist
        query = String.format(
                "CREATE SCHEMA IF NOT EXISTS \"%s\"",
                globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create DateGroup table
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"DateGroup\"\n" +
                        "(\n" +
                        "  \"DateGroupID\" serial PRIMARY KEY,\n" +
                        "  \"Year\" integer NOT NULL,\n" +
                        "  \"Day\" integer NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create Project table
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"Project\"\n" +
                        "(\n" +
                        "  \"ProjectID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL,\n" +
                        "  \"StartDate_DateGroupID\" integer REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") NOT NULL,\n" +   // Represents the project's start date
                        "  \"IndicesCount\" integer NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create Plugin table
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"Plugin\"\n" +
                        "(\n" +
                        "  \"PluginID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL,\n" +
                        "  \"DaysPerInputFile\" integer NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create ExpectedResults table to keep track of how many results are expected for each plugin for each project
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"ExpectedResults\"\n" +
                        "(\n" +
                        "  \"ExpectedResultsID\" serial PRIMARY KEY,\n" +
                        "  \"ProjectID\" integer REFERENCES \"%1$s\".\"Project\" (\"ProjectID\") NOT NULL,\n" +
                        "  \"PluginID\" integer REFERENCES \"%1$s\".\"Plugin\" (\"PluginID\") NOT NULL,\n" +
                        "  \"ExpectedTotalResults\" integer NOT NULL,\n" +
                        "  \"TemporalSummaryCompositionStrategyClass\" varchar(255) NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create GlobalDownloader table
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"GlobalDownloader\"\n" +
                        "(\n" +
                        "  \"GlobalDownloaderID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL,\n" +
                        "  \"UniqueInstanceNum\" integer UNIQUE NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create GDExpectedResults table
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"GDExpectedResults\"\n" +
                        "(\n" +
                        "  \"GDExpectedResultsID\" serial PRIMARY KEY,\n" +
                        "  \"GlobalDownloaderID\" integer REFERENCES \"%1$s\".\"GlobalDownloader\" (\"GlobalDownloaderID\") NOT NULL,\n" +
                        "  \"ExpectedResultsID\" integer REFERENCES \"%1$s\".\"ExpectedResults\" (\"ExpectedResultsID\") NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create Download table
        StringBuilder query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE IF NOT EXISTS \"%1$s\".\"Download\"\n" +
                                "(\n" +
                                "  \"DownloadID\" serial PRIMARY KEY,\n" +
                                "  \"GlobalDownloaderID\" integer REFERENCES \"%1$s\".\"GlobalDownloader\" (\"GlobalDownloaderID\") NOT NULL,\n" +
                                "  \"DataFileFullPath\" varchar(255) UNIQUE NOT NULL,\n",
                                globalEASTWebSchema
                        ));
        for(String fileName : extraDownloadFiles)
        {
            query_.append("  \"" + fileName + "FullPath\" varchar(255) UNIQUE NOT NULL,\n");
        }
        query_.append(
                String.format("  \"DateGroupID\" integer REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());

        // Create Environmental Index table
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"Index\"\n" +
                        "(\n" +
                        "  \"IndexID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create Region table
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"Region\"\n" +
                        "(\n" +
                        "  \"RegionID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create the Zone table
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"Zone\"\n" +
                        "(\n" +
                        "  \"ZoneID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL,\n" +
                        "  \"RegionID\" integer REFERENCES \"%1$s\".\"Region\" (\"RegionID\") NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create the Zone_Var table
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"ZoneVar\"\n" +
                        "(\n" +
                        "  \"ZoneVarID\" serial PRIMARY KEY,\n" +
                        "  \"ZoneID\" integer REFERENCES \"%1$s\".\"Zone\" (\"ZoneID\") NOT NULL,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

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
        stmt.executeUpdate(query);

        // Create the Zone_Var table
        query = String.format(
                "CREATE TABLE \"%1$s\".\"ZoneMapping\"\n" +
                        "(\n" +
                        "  \"ZoneMappingID\" serial PRIMARY KEY,\n" +
                        "  \"ZoneID\" integer REFERENCES \"%2$s\".\"Zone\" (\"ZoneID\") NOT NULL,\n" +
                        "  \"ZoneFieldID\" integer REFERENCES \"%1$s\".\"ZoneField\" (\"ZoneFieldID\") NOT NULL\n" +
                        ")",
                        mSchemaName,
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);

        // Create the ZonalStats table
        //        query = String.format(
        //                "CREATE TABLE \"%1$s\".\"ZonalStat\"\n" +
        //                        "(\n" +
        //                        "  \"IndexID\" integer NOT NULL,\n" +
        //                        "  \"Year\" integer NOT NULL,\n" +
        //                        "  \"Day\" integer NOT NULL,\n" +
        //                        "  \"ZoneID\" integer REFERENCES \"%1$s\".\"Zone\" (\"ZoneID\") NOT NULL,\n" +
        //                        "  \"Count\" double precision NOT NULL,\n" +
        //                        "  \"Sum\" double precision NOT NULL,\n" +
        //                        "  \"Mean\" double precision NOT NULL,\n" +
        //                        "  \"StdDev\" double precision NOT NULL,\n" +
        //                        "  CONSTRAINT \"PK_ZonalStat\"\n" +
        //                        "      PRIMARY KEY (\"IndexID\", \"Year\", \"Day\", \"ZoneID\")\n" +
        //                        ")",
        //                        mSchemaName
        //                );
        query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE \"%1$s\".\"ZonalStat\"\n" +
                                "(\n" +
                                "  \"ZonalStatID\" serial PRIMARY KEY,\n" +
                                "  \"DateGroupID\" integer REFERENCES \"%2$s\".\"DateGroup\" (\"DateGroupID\") NOT NULL,\n" +
                                "  \"IndexID\" integer REFERENCES \"%2$s\".\"Index\" (\"IndexID\") NOT NULL,\n" +
                                "  \"ZoneMappingID\" integer REFERENCES \"%1$s\".\"ZoneMapping\" (\"ZoneMappingID\") NOT NULL,\n" +
                                "  \"ExpectedResultsID\" integer REFERENCES \"%2$s\".\"ExpectedResults\" (\"ExpectedResultsID\") NOT NULL,\n" +
                                "  \"TemporalSummaryCompositionStrategyClass\" varchar(255) NOT NULL\n",
                                mSchemaName,
                                globalEASTWebSchema
                        ));
        for(String summary : summaryNames)
        {
            query_.append(",\n  \"" + summary + "\" double precision NOT NULL");
        }
        query_.append(
                "\n)");
        query = query_.toString();
        stmt.executeUpdate(query);

        // Create cache tables for each framework
        query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE \"%1$s\".\"DownloadCache\"\n" +
                                "(\n" +
                                "  \"DownloadCacheID\" serial PRIMARY KEY,\n" +
                                "  \"DataFilePath\" varchar(255) UNIQUE NOT NULL,\n",
                                mSchemaName
                        ));
        for(String fileName : extraDownloadFiles)
        {
            query_.append("  \"" + fileName + "FilePath\" varchar(255) UNIQUE NOT NULL,\n");
        }
        query_.append(
                String.format(
                        "  \"DateDirectory\" varchar(255) NOT NULL,\n" +
                                "  \"DataGroupID\" integer REFERENCES \"%2$s\".\"DateGroup\" (\"DateGroupID\") NOT NULL,\n" +
                                "  \"Retrieved\" boolean DEFAULT FALSE,\n" +
                                "  \"Processed\" boolean DEFAULT FALSE\n" +
                                ")",
                                globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());

        query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE \"%1$s\".\"ProcessorCache\"\n" +
                                "(\n" +
                                "  \"ProcessorCacheID\" serial PRIMARY KEY,\n" +
                                "  \"DataFilePath\" varchar(255) UNIQUE NOT NULL,\n",
                                mSchemaName
                        ));
        for(String fileName : extraDownloadFiles)
        {
            query_.append("  \"" + fileName + "FilePath\" varchar(255) UNIQUE NOT NULL,\n");
        }
        query_.append(
                String.format(
                        "  \"DateDirectory\" varchar(255) NOT NULL,\n" +
                                "  \"DataGroupID\" integer REFERENCES \"%2$s\".\"DateGroup\" (\"DateGroupID\") NOT NULL,\n" +
                                "  \"Retrieved\" boolean DEFAULT FALSE,\n" +
                                "  \"Processed\" boolean DEFAULT FALSE\n" +
                                ")",
                                globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());

        query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE \"%1$s\".\"IndicesCache\"\n" +
                                "(\n" +
                                "  \"IndicesCacheID\" serial PRIMARY KEY,\n" +
                                "  \"DataFilePath\" varchar(255) UNIQUE NOT NULL,\n",
                                mSchemaName
                        ));
        for(String fileName : extraDownloadFiles)
        {
            query_.append("  \"" + fileName + "FilePath\" varchar(255) UNIQUE NOT NULL,\n");
        }
        query_.append(
                String.format(
                        "  \"DateDirectory\" varchar(255) NOT NULL,\n" +
                                "  \"DataGroupID\" integer REFERENCES \"%2$s\".\"DateGroup\" (\"DateGroupID\") NOT NULL,\n" +
                                "  \"Retrieved\" boolean DEFAULT FALSE,\n" +
                                "  \"Processed\" boolean DEFAULT FALSE\n" +
                                ")",
                                globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());

        // Get DateGroupID
        int dateGroupID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"DateGroupID\" FROM \"%1$s\".\"DateGroup\" " +
                "WHERE \"Year\"=" + startDate.getYear() + "\"Day\"=" + startDate.getDayOfYear() + ";",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            dateGroupID = rs.getInt("DateGroupID");
        }
        else{
            stmt.executeUpdate(String.format(
                    "INSERT INTO \"%1$s\".\"DateGroup\" (\"Year\", \"Day\") VALUES (" + startDate.getYear() + "," + startDate.getDayOfYear() + ");",
                    globalEASTWebSchema
                    ));
            rs = stmt.executeQuery(String.format("SELECT \"DateGroupID\" FROM \"%1$s\".\"DateGroup\" " +
                    "WHERE \"Year\"=" + startDate.getYear() + " AND \"Day\"=" + startDate.getDayOfYear() + ";",
                    globalEASTWebSchema
                    ));
            if(rs != null && rs.next())
            {
                dateGroupID = rs.getInt("DateGroupID");
            }
        }
        rs.close();

        // Add entry to Project table
        int projectID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"ProjectID\" FROM \"%1$s\".\"Project\" " +
                "WHERE \"Name\"=" + projectName + " AND \"StartDate_DateGroupID\"=" + dateGroupID + " AND \"IndicesCount\"=" + numOfIndices + ";",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            projectID = rs.getInt("ProjectID");
        }
        else{
            stmt.executeUpdate(String.format(
                    "INSERT INTO \"%1$s\".\"Project\" (\"Name\", \"StartDate_DateGroupID\", \"IndicesCount\")\n" +
                            "VALUES ('" + projectName + "', " + dateGroupID + ", " + numOfIndices + ");",
                            globalEASTWebSchema
                    ));
            rs = stmt.executeQuery(String.format("SELECT \"ProjectID\" FROM \"%1$s\".\"Project\" " +
                    "WHERE \"Name\"=" + projectName + " AND \"StartDate_DateGroupID\"=" + dateGroupID + " AND \"IndicesCount\"=" + numOfIndices + ";",
                    globalEASTWebSchema
                    ));
            if(rs != null && rs.next())
            {
                projectID = rs.getInt("ProjectID");
            }
        }
        rs.close();

        // Add entry to Plugin table if not already existing
        int pluginID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"PluginID\" FROM \"%1$s\".\"Plugin\" " +
                "WHERE \"Name\"=" + pluginName + "\"DaysPerInputFile\"=" + daysPerInputFile + ";",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            pluginID = rs.getInt("PluginID");
        }
        else{
            stmt.executeUpdate(String.format(
                    "INSERT INTO \"%1$s\".\"Plugin\" (\"Name\", \"DaysPerInputFile\") VALUES (" + pluginName + "," + daysPerInputFile + ");",
                    globalEASTWebSchema
                    ));
            rs = stmt.executeQuery(String.format("SELECT \"PluginID\" FROM \"%1$s\".\"Plugin\" " +
                    "WHERE \"Name\"=" + pluginName + "\"DaysPerInputFile\"=" + daysPerInputFile + ";",
                    globalEASTWebSchema
                    ));
            if(rs != null && rs.next())
            {
                pluginID = rs.getInt("PluginID");
            }
        }
        rs.close();

        // Add entry to EASTWeb global ExpectedResults table
        //        LocalDate startLDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        //        int numOfDaysInProject = Period.between(startDate, LocalDate.now().plusDays(1)).getDays();
        int expectedTotalResults;
        for(ProjectInfoSummary summary : summaries)
        {
            expectedTotalResults = summary.GetTemporalFileStore().compStrategy.getNumberOfCompleteCompositesInRange(startDate, LocalDate.now().plusDays(1), daysPerInputFile) * numOfIndices;
            query = String.format(
                    "INSERT INTO \"%1$s\".\"ExpectedResults\" (\"ProjectID\", \"PluginID\", \"ExpectedTotalResults\", \"TemporalSummaryCompositionStrategyClass\") " +
                            "VALUES (" + projectID + ", " + pluginID + ", " + expectedTotalResults + ", " + summary.GetTemporalSummaryCompositionStrategyClassName() + ")",
                            globalEASTWebSchema
                    );
        }
    }

    /**
     * Gets the name of the specified project's database schema. The returned name does not need to be quoted to use in SQL.
     *
     * @param projectName  - project name the schema is for
     * @param pluginName  - plugin name the schema is for
     * @return name of schema within database formatted as seen in database
     */
    public static String getSchemaName(String projectName, String pluginName) {
        String name = projectName + "_" + pluginName;
        StringBuilder builder = new StringBuilder();
        int codePointOut, codePointIn;

        for (int index = 0; index < name.length(); index += Character.charCount(codePointIn)) {
            codePointIn = name.codePointAt(index);

            if (codePointIn >= 'A' && codePointIn <= 'Z') {
                // Convert upper-case letters to lower-case
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
        return FileSystem.StandardizeName(builder.toString());
    }
}
