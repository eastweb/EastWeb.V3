package version2.prototype.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.ErrorLog;
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
     * @param postgreSQLConnection  - a connection object (can be obtained from PostgreSQLConnection.getConnection())
     * @param globalEASTWebSchema  - the global schema name (can be gotten from Config.getInstance().getGlobalSchema()
     * @param projectName  - name of project to create schema for
     * @param pluginName  - name of plugin to create schema for
     * @param summaryNames  - used to define ZonalStat table
     * @param extraDownloadFiles  - used to define cache tables and global Download table
     * @param startDate  - relevant to project entry creation and calculating the number of expected results to be found in ZonalStat
     * @param daysPerInputFile  - relevant to plugin entry creation and calculating the number of expected results to be found in ZonalStat
     * @param filesPerDay  - relevant to plugin entry creation and calculating when downloads are ready to be loaded by the LocalDownloader
     * @param numOfIndices  - relevant to project entry creation and calculating the number of expected results to be found in ZonalStat
     * @param summaries  - relevant to calculating the number of expected results to be found in ZonalStat
     * @param createTablesWithForeignKeyReferences  - TRUE if tables should be created so that their foreign keys are referencing their corresponding primary keys, FALSE if tables shouldn't be created
     * to enforce foreign key rules
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws ConfigReadException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void CreateProjectPluginSchema(Connection postgreSQLConnection, String globalEASTWebSchema, String projectName, String pluginName, ArrayList<String> summaryNames,
            ArrayList<String> extraDownloadFiles, LocalDate startDate, Integer daysPerInputFile, Integer filesPerDay, Integer numOfIndices,
            ArrayList<ProjectInfoSummary> summaries, boolean createTablesWithForeignKeyReferences) throws ParserConfigurationException, SAXException, IOException
    {
        final Connection conn;
        final Statement stmt;
        final String mSchemaName;
        try{
            conn = postgreSQLConnection;
            stmt = conn.createStatement();
            mSchemaName = getSchemaName(projectName, pluginName);

            // Drop an existing schema with the same name
            //        dropSchemaIfExists(stmt, mSchemaName);

            // Create the schema for this project
            createSchemaIfNotExist(stmt, mSchemaName);

            // Create the global schema for EASTWeb if it doesn't already exist
            createSchemaIfNotExist(stmt, globalEASTWebSchema);

            // Create DateGroup table
            createDateGroupTable(globalEASTWebSchema, stmt);

            // Create Project table
            createProjectTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

            // Create Plugin table
            createPluginTableIfNotExists(globalEASTWebSchema, stmt);

            // Create TemporalSummaryCompositionStrategy table
            createTemporalSummaryCompositionStrategyTableIfNotExists(globalEASTWebSchema, stmt);

            // Create ExpectedResults table to keep track of how many results are expected for each plugin for each project
            createExpectedResultsTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

            // Create GlobalDownloader table
            createGlobalDownloaderTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

            // Create GDExpectedResults table
            createGlobalDownloaderExpectedResultsTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

            // Create Download table
            createDownloadTableIfNotExists(globalEASTWebSchema, extraDownloadFiles, stmt, createTablesWithForeignKeyReferences);

            // Create DownloadExtra table
            createDownloadExtraTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

            // Create Environmental Index table
            createIndexTableIfNotExists(globalEASTWebSchema, stmt);

            // Create the ZoneEW table
            createZoneTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

            // Create the ZoneVar table
            createZoneVarTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

            // Create the ZoneFields table
            createZoneFieldTableIfNotExists(stmt, mSchemaName);

            // Create the ZoneMapping table
            createZoneMappingTableIfNotExists(globalEASTWebSchema, stmt, mSchemaName, createTablesWithForeignKeyReferences);

            // Create the ZonalStats table
            createZonalStatTableIfNotExists(globalEASTWebSchema, summaryNames, stmt, mSchemaName, createTablesWithForeignKeyReferences);

            // Create cache tables for each framework
            createDownloadCacheTableIfNotExists(globalEASTWebSchema, stmt, mSchemaName, createTablesWithForeignKeyReferences);
            createDownloadCacheExtraTableIfNotExists(globalEASTWebSchema, stmt, mSchemaName, createTablesWithForeignKeyReferences);
            createProcessorCacheTableIfNotExists(globalEASTWebSchema, extraDownloadFiles, stmt, mSchemaName, createTablesWithForeignKeyReferences);
            createIndicesCacheTableIfNotExists(globalEASTWebSchema, extraDownloadFiles, stmt, mSchemaName, createTablesWithForeignKeyReferences);

            // Get DateGroupID
            int dateGroupID = getDateGroupID(globalEASTWebSchema, startDate, stmt);

            // Add entry to Project table
            int projectID = addProject(globalEASTWebSchema, projectName, numOfIndices, stmt, dateGroupID);

            // Add entry to Plugin table if not already existing
            int pluginID = addPlugin(globalEASTWebSchema, pluginName, daysPerInputFile, filesPerDay, stmt);

            // Add entry to EASTWeb global ExpectedResults table
            addExpectedResults(globalEASTWebSchema, startDate, daysPerInputFile, numOfIndices, summaries, projectID, pluginID, conn);
        }
        catch(SQLException e)
        {
            ErrorLog.add(Config.getInstance(), "SQL Exception in Schemas.", e);
        }
    }

    /**
     * Updates the ExpectedResults table calculations starting from the given start date up to, but not including, tomorrow.
     *
     * @param globalEASTWebSchema  - the schema for the globally accessible EASTWeb schema
     * @param projectName  - name of project to update ExpectedResults for
     * @param pluginName  - name of plugin to update ExpectedResults for
     * @param startDate  - date to start calculations from
     * @param daysPerInputFile  - the number of days expected per input file specified in the plugin metadata
     * @param numOfIndices  - the number of indices specified in the project metadata
     * @param summaries  - the summaries specified in the project metadata
     * @param conn
     * @return the number of updates done for each summary specified in the project metadata indexed by the value for their 'ID' attribute
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static TreeMap<Integer, Integer> updateExpectedResults(String globalEASTWebSchema, String projectName, String pluginName, LocalDate startDate, Integer daysPerInputFile,
            Integer numOfIndices, ArrayList<ProjectInfoSummary> summaries, Connection conn) throws SQLException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException {
        TreeMap<Integer, Integer> results = new TreeMap<Integer, Integer>();
        if(globalEASTWebSchema == null || startDate == null || daysPerInputFile == null || numOfIndices == null || summaries == null || projectName == null || pluginName == null || conn == null) {
            return results;
        }

        final Statement stmt = conn.createStatement();
        //        final String mSchemaName = getSchemaName(projectName, pluginName);

        int projectID = getProjectID(globalEASTWebSchema, projectName, stmt);
        int pluginID = getPluginID(globalEASTWebSchema, pluginName, stmt);

        PreparedStatement preparedStmt = conn.prepareStatement(String.format(
                "UPDATE \"%1$s\".\"ExpectedResults\" SET \"ExpectedTotalResults\" = ? WHERE \"ExpectedResultsID\" = ?;",
                globalEASTWebSchema
                ));

        int temporalSummaryCompositionStrategyID;
        int expectedResultsID;
        long expectedTotalResults;
        for(ProjectInfoSummary summary : summaries)
        {
            temporalSummaryCompositionStrategyID = getTemporalSummaryCompositionStrategyID(globalEASTWebSchema, summary.GetTemporalSummaryCompositionStrategyClassName(), stmt);
            expectedResultsID = getExpectedResultsID(globalEASTWebSchema, projectID, pluginID, temporalSummaryCompositionStrategyID, stmt);
            expectedTotalResults = summary.GetTemporalFileStore().compStrategy.getNumberOfCompleteCompositesInRange(startDate, LocalDate.now().plusDays(1), daysPerInputFile) * numOfIndices;
            preparedStmt.setLong(1, expectedTotalResults);
            preparedStmt.setInt(2, expectedResultsID);
            preparedStmt.addBatch();
            results.put(summary.GetID(), 0);
        }
        int[] updates = preparedStmt.executeBatch();
        for(int i=0; i < updates.length; i++)
        {
            results.replace(summaries.get(i).GetID(), updates[i]);
        }
        return results;
    }

    public static boolean registerGlobalDownloader(final String globalEASTWebSchema, final String pluginName, final String dataName, final Statement stmt) throws SQLException
    {
        final int pluginID = Schemas.getPluginID(globalEASTWebSchema, pluginName, stmt);

        ResultSet rs;
        rs = stmt.executeQuery("SELECT \"GlobalDownloaderID\" FROM \"" + globalEASTWebSchema + "\".\"GlobalDownloader\" WHERE \"PluginID\" = " + pluginID + " AND \"DataName\" = '" + dataName + "';");
        if(rs != null && rs.next()) {
            return false;
        }

        return stmt.execute("INSERT INTO \"" + globalEASTWebSchema + "\".\"GlobalDownloader\" (\"PluginID\", \"DataName\") VALUES (" + pluginID + ", '" + dataName + "');");
    }

    public static int getDateGroupID(final String globalEASTWebSchema, final LocalDate lDate, final Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null || lDate == null) {
            return -1;
        }

        ResultSet rs;
        int dateGroupID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"DateGroupID\" FROM \"%1$s\".\"DateGroup\" " +
                "WHERE \"Year\"=" + lDate.getYear() + " AND \"DayOfYear\"=" + lDate.getDayOfYear() + ";",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            dateGroupID = rs.getInt("DateGroupID");
        }
        else{
            stmt.executeUpdate(String.format(
                    "INSERT INTO \"%1$s\".\"DateGroup\" (\"Year\", \"DayOfYear\") VALUES (" + lDate.getYear() + "," + lDate.getDayOfYear() + ");",
                    globalEASTWebSchema
                    ));
            rs = stmt.executeQuery(String.format("SELECT \"DateGroupID\" FROM \"%1$s\".\"DateGroup\" " +
                    "WHERE \"Year\"=" + lDate.getYear() + " AND \"DayOfYear\"=" + lDate.getDayOfYear() + ";",
                    globalEASTWebSchema
                    ));
            if(rs != null && rs.next())
            {
                dateGroupID = rs.getInt("DateGroupID");
            }
        }
        rs.close();
        return dateGroupID;
    }

    public static Integer getIndexID(final String globalEASTWebSchema, final String indexNm, final Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null || indexNm == null) {
            return null;
        }

        ResultSet rs;
        int indexID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"IndexID\" FROM \"%1$s\".\"Index\" " +
                "WHERE \"Name\"='" + indexNm + "';",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            indexID = rs.getInt("IndexID");
        }
        else{
            stmt.executeUpdate(String.format(
                    "INSERT INTO \"%1$s\".\"Index\" (\"Name\") VALUES ('" + indexNm + "');",
                    globalEASTWebSchema
                    ));
            rs = stmt.executeQuery(String.format("SELECT \"IndexID\" FROM \"%1$s\".\"Index\" " +
                    "WHERE \"Name\"='" + indexNm + "';",
                    globalEASTWebSchema
                    ));
            if(rs != null && rs.next())
            {
                indexID = rs.getInt("IndexID");
            }
        }
        rs.close();
        return indexID;
    }

    public static int getGlobalDownloaderID(final String globalEASTWebSchema, final String pluginName, final String dataName, final Statement stmt) throws SQLException,
    ClassNotFoundException, ParserConfigurationException, SAXException, IOException
    {
        int ID = -1;

        int pluginID = getPluginID(globalEASTWebSchema, pluginName, stmt);

        ResultSet rs;
        String selectQuery = String.format(
                "SELECT \"GlobalDownloaderID\" FROM \"%1$s\".\"GlobalDownloader\" WHERE \n" +
                        "\"PluginID\"=" + pluginID + " AND \"DataName\" = '" + dataName + "' ORDER BY \"GlobalDownloaderID\" DESC;",
                        globalEASTWebSchema
                );
        rs = stmt.executeQuery(selectQuery);
        if(rs != null && rs.next())
        {
            ID = rs.getInt("GlobalDownloaderID");
            rs.close();
        }
        else
        {
            registerGlobalDownloader(globalEASTWebSchema, pluginName, dataName, stmt);
            rs = stmt.executeQuery(selectQuery);
            if(rs != null && rs.next())
            {
                ID = rs.getInt("GlobalDownloaderID");
                rs.close();
            }
        }
        return ID;
    }

    public static int getPluginID(String globalEASTWebSchema, String pluginName, final Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null || pluginName == null) {
            return -1;
        }

        ResultSet rs;
        int pluginID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"PluginID\" FROM \"%1$s\".\"Plugin\" " +
                "WHERE \"Name\"='" + pluginName + "';",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            pluginID = rs.getInt("PluginID");
        }
        rs.close();
        return pluginID;
    }

    public static int getDownloadID(String globalEASTWebSchema, Integer globalDownloaderID, Integer dateGroupID, final Statement stmt) throws ClassNotFoundException, SQLException,
    ParserConfigurationException, SAXException, IOException {
        if(globalEASTWebSchema == null || globalDownloaderID == null || dateGroupID == null) {
            return -1;
        }

        int ID = -1;

        ResultSet rs;
        rs = stmt.executeQuery(String.format(
                "SELECT \"DownloadID\" FROM \"%1$s\".\"Download\" WHERE \n" +
                        "\"GlobalDownloaderID\" = " + globalDownloaderID + " AND \"DateGroupID\" = " + dateGroupID + ";",
                        globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            ID = rs.getInt("DownloadID");
        }
        rs.close();
        return ID;
    }

    public static int getProjectID(String globalEASTWebSchema, String projectName, final Statement stmt) throws SQLException
    {
        if(globalEASTWebSchema == null) {
            return -1;
        }

        ResultSet rs;
        int projectID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"ProjectID\" FROM \"%1$s\".\"Project\" " +
                "WHERE \"Name\"='" + projectName + "';",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            projectID = rs.getInt("ProjectID");
        }

        rs.close();
        return projectID;
    }

    public static int getTemporalSummaryCompositionStrategyID(String globalEASTWebSchema, String temporalSummaryCompositionStrategyClassName, Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null || temporalSummaryCompositionStrategyClassName == null) {
            return -1;
        }

        ResultSet rs;
        int temporalSummaryCompositionStrategyID = -1;
        String selectQuery = String.format("SELECT \"TemporalSummaryCompositionStrategyID\" FROM \"%1$s\".\"TemporalSummaryCompositionStrategy\" " +
                "WHERE \"Name\"='" + temporalSummaryCompositionStrategyClassName + "';",
                globalEASTWebSchema
                );
        rs = stmt.executeQuery(selectQuery);
        if(rs != null && rs.next())
        {
            temporalSummaryCompositionStrategyID = rs.getInt("TemporalSummaryCompositionStrategyID");
        }
        else{
            stmt.executeUpdate(String.format(
                    "INSERT INTO \"%1$s\".\"TemporalSummaryCompositionStrategy\" (\"Name\") VALUES " +
                            "('" + temporalSummaryCompositionStrategyClassName + "');",
                            globalEASTWebSchema
                    ));
            rs = stmt.executeQuery(selectQuery);
            if(rs != null && rs.next())
            {
                temporalSummaryCompositionStrategyID = rs.getInt("TemporalSummaryCompositionStrategyID");
            }
        }
        rs.close();
        return temporalSummaryCompositionStrategyID;
    }

    public static int getExpectedResultsID(String globalEASTWebSchema, Integer projectID, Integer pluginID, Integer temporalSummaryCompositionStrategyID, Statement stmt) throws SQLException
    {
        if(globalEASTWebSchema == null || projectID == null || pluginID == null || temporalSummaryCompositionStrategyID == null) {
            return -1;
        }

        ResultSet rs;
        int expectedResultsID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"ExpectedResultsID\" FROM \"%1$s\".\"ExpectedResults\" " +
                "WHERE \"ProjectID\"=" + projectID + " AND \"PluginID\"=" + pluginID + " AND \"TemporalSummaryCompositionStrategyID\"=" + temporalSummaryCompositionStrategyID + ";",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            expectedResultsID = rs.getInt("ExpectedResultsID");
        }
        rs.close();
        return expectedResultsID;
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

    private static int[] addExpectedResults(String globalEASTWebSchema, LocalDate startDate, Integer daysPerInputFile, Integer numOfIndices, ArrayList<ProjectInfoSummary> summaries, Integer projectID,
            Integer pluginID, final Connection conn) throws SQLException{
        if(globalEASTWebSchema == null || startDate == null || daysPerInputFile == null || numOfIndices == null || summaries == null || projectID == null || pluginID == null) {
            return new int[0];
        }
        Statement stmt = conn.createStatement();
        PreparedStatement preparedStmt = conn.prepareStatement(String.format(
                "INSERT INTO \"%1$s\".\"ExpectedResults\" (\"ProjectID\", \"PluginID\", \"ExpectedTotalResults\", \"TemporalSummaryCompositionStrategyID\") VALUES (" +
                        projectID + ", " +
                        pluginID + ", " +
                        "?, " +   // 1. ExpectedTotalResults
                        "? " +  // 2. TemporalSummaryCompositionStrategyID
                        ")",
                        globalEASTWebSchema
                ));

        long expectedTotalResults;
        for(ProjectInfoSummary summary : summaries)
        {
            expectedTotalResults = summary.GetTemporalFileStore().compStrategy.getNumberOfCompleteCompositesInRange(startDate, LocalDate.now().plusDays(1), daysPerInputFile) * numOfIndices;
            preparedStmt.setLong(1, expectedTotalResults);
            preparedStmt.setInt(2, getTemporalSummaryCompositionStrategyID(globalEASTWebSchema, summary.GetTemporalSummaryCompositionStrategyClassName(), stmt));
            preparedStmt.addBatch();
        }
        return preparedStmt.executeBatch();
    }

    private static int addPlugin(final String globalEASTWebSchema, final String pluginName, final Integer daysPerInputFile, final Integer filesPerDay,
            final Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null || pluginName == null || daysPerInputFile == null) {
            return -1;
        }

        ResultSet rs;
        int pluginID = -1;
        String selectQuery = String.format("SELECT \"PluginID\" FROM \"%1$s\".\"Plugin\" " +
                "WHERE \"Name\"='" + pluginName + "';",
                globalEASTWebSchema
                );
        rs = stmt.executeQuery(selectQuery);
        if(rs != null && rs.next())
        {
            pluginID = rs.getInt("PluginID");
        }
        else{
            stmt.executeUpdate(String.format(
                    "INSERT INTO \"%1$s\".\"Plugin\" (\"Name\", \"DaysPerInputFile\", \"FilesPerDay\") VALUES " +
                            "('" + pluginName + "', " + daysPerInputFile + ", " + filesPerDay + ");",
                            globalEASTWebSchema
                    ));
            rs = stmt.executeQuery(selectQuery);
            if(rs != null && rs.next())
            {
                pluginID = rs.getInt("PluginID");
            }
        }
        rs.close();
        return pluginID;
    }

    private static int addProject(String globalEASTWebSchema, String projectName, Integer numOfIndices, final Statement stmt, Integer dateGroupID) throws SQLException {
        if(globalEASTWebSchema == null || numOfIndices == null || dateGroupID == null || dateGroupID == -1) {
            return -1;
        }

        ResultSet rs;
        int projectID = -1;
        String selectQuery = String.format("SELECT \"ProjectID\" FROM \"%1$s\".\"Project\" " +
                "WHERE \"Name\"='" + projectName + "' AND \"StartDate_DateGroupID\"=" + dateGroupID + " AND \"IndicesCount\"=" + numOfIndices + ";",
                globalEASTWebSchema
                );
        rs = stmt.executeQuery(selectQuery);
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
            rs = stmt.executeQuery(selectQuery);
            if(rs != null && rs.next())
            {
                projectID = rs.getInt("ProjectID");
            }
        }
        rs.close();
        return projectID;
    }

    private static void createIndicesCacheTableIfNotExists(String globalEASTWebSchema, ArrayList<String> extraDownloadFiles, final Statement stmt, final String mSchemaName,
            boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null || mSchemaName == null || extraDownloadFiles == null) {
            return;
        }

        StringBuilder query_;
        query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE IF NOT EXISTS \"%1$s\".\"IndicesCache\"\n" +
                                "(\n" +
                                "  \"IndicesCacheID\" serial PRIMARY KEY,\n" +
                                "  \"DataFilePath\" varchar(255) UNIQUE NOT NULL,\n" +
                                "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                                "  \"IndexID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"Index\" (\"IndexID\") " : "") + "DEFAULT NULL,\n" +
                                "  \"Retrieved\" boolean DEFAULT FALSE,\n" +
                                "  \"Processed\" boolean DEFAULT FALSE\n" +
                                ")",
                                mSchemaName,
                                globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());
    }

    private static void createProcessorCacheTableIfNotExists(String globalEASTWebSchema, ArrayList<String> extraDownloadFiles, final Statement stmt, final String mSchemaName,
            boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null || mSchemaName == null || extraDownloadFiles == null) {
            return;
        }

        StringBuilder query_;
        query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE IF NOT EXISTS \"%1$s\".\"ProcessorCache\"\n" +
                                "(\n" +
                                "  \"ProcessorCacheID\" serial PRIMARY KEY,\n" +
                                "  \"DataFilePath\" varchar(255) UNIQUE NOT NULL,\n" +
                                "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                                "  \"Retrieved\" boolean DEFAULT FALSE,\n" +
                                "  \"Processed\" boolean DEFAULT FALSE\n" +
                                ")",
                                mSchemaName,
                                globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());
    }

    private static void createDownloadCacheExtraTableIfNotExists(String globalEASTWebSchema, final Statement stmt, final String mSchemaName, boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null || mSchemaName == null) {
            return;
        }

        StringBuilder query_;
        query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE IF NOT EXISTS \"%1$s\".\"DownloadCacheExtra\"\n" +
                                "(\n" +
                                "  \"DownloadCacheExtraID\" serial PRIMARY KEY,\n" +
                                "  \"DataName\" varchar(255) NOT NULL,\n" +
                                "  \"FilePath\" varchar(255) UNIQUE NOT NULL,\n" +
                                "  \"DownloadExtraID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"DownloadExtra\" (\"DownloadExtraID\") " : "") + "NOT NULL,\n" +
                                "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                                "  \"DownloadCacheID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"DownloadCache\" (\"DownloadCacheID\") " : "") + "DEFAULT NULL,\n" +
                                "  \"Retrieved\" boolean DEFAULT FALSE,\n" +
                                "  \"Processed\" boolean DEFAULT FALSE,\n" +
                                "  \"Complete\" boolean DEFAULT FALSE\n" +
                                ")",
                                mSchemaName,
                                globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());
    }

    private static void createDownloadCacheTableIfNotExists(String globalEASTWebSchema, final Statement stmt, final String mSchemaName, boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null || mSchemaName == null) {
            return;
        }

        StringBuilder query_;
        query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE IF NOT EXISTS \"%1$s\".\"DownloadCache\"\n" +
                                "(\n" +
                                "  \"DownloadCacheID\" serial PRIMARY KEY,\n" +
                                "  \"DataFilePath\" varchar(255) UNIQUE NOT NULL,\n" +
                                "  \"DownloadID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"Download\" (\"DownloadID\") " : "") + "NOT NULL,\n" +
                                "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                                "  \"Retrieved\" boolean DEFAULT FALSE,\n" +
                                "  \"Processed\" boolean DEFAULT FALSE,\n" +
                                "  \"Complete\" boolean DEFAULT FALSE\n" +
                                ")",
                                mSchemaName,
                                globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());
    }

    private static void createZonalStatTableIfNotExists(String globalEASTWebSchema, ArrayList<String> summaryNames, final Statement stmt, final String mSchemaName,
            boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null || mSchemaName == null || summaryNames == null) {
            return;
        }

        StringBuilder query_;
        query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE IF NOT EXISTS \"%1$s\".\"ZonalStat\"\n" +
                                "(\n" +
                                "  \"ZonalStatID\" serial PRIMARY KEY,\n" +
                                "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                                "  \"IndexID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"Index\" (\"IndexID\") " : "") + "NOT NULL,\n" +
                                "  \"ZoneMappingID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"ZoneMapping\" (\"ZoneMappingID\") " : "") + "NOT NULL,\n" +
                                "  \"ExpectedResultsID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"ExpectedResults\" (\"ExpectedResultsID\") " : "") + " NOT NULL,\n" +
                                "  \"TemporalSummaryCompositionStrategyClass\" varchar(255) NOT NULL,\n" +
                                "  \"FilePath\" varchar(255) NOT NULL",
                                mSchemaName,
                                globalEASTWebSchema
                        ));
        for(String summary : summaryNames)
        {
            query_.append(",\n  \"" + summary + "\" double precision NOT NULL");
        }
        query_.append("\n)");
        stmt.executeUpdate(query_.toString());
    }

    private static void createZoneMappingTableIfNotExists(String globalEASTWebSchema, final Statement stmt, final String mSchemaName, boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null || mSchemaName == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"ZoneMapping\"\n" +
                        "(\n" +
                        "  \"ZoneMappingID\" serial PRIMARY KEY,\n" +
                        "  \"ZoneEWID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%2$s\".\"ZoneEW\" (\"ZoneEWID\") " : "") + "NOT NULL,\n" +
                        "  \"ZoneFieldID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"ZoneField\" (\"ZoneFieldID\") " : "") + "NOT NULL\n" +
                        ")",
                        mSchemaName,
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createZoneFieldTableIfNotExists(final Statement stmt, final String mSchemaName) throws SQLException {
        if(mSchemaName == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"ZoneField\"\n" +
                        "(\n" +
                        "  \"ZoneFieldID\" serial PRIMARY KEY,\n" +
                        "  \"ShapeFile\" varchar(255) NOT NULL,\n" +
                        "  \"Field\" varchar(255) NOT NULL\n" +
                        ")",
                        mSchemaName
                );
        stmt.executeUpdate(query);
    }

    private static void createZoneVarTableIfNotExists(String globalEASTWebSchema, final Statement stmt, boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"ZoneVar\"\n" +
                        "(\n" +
                        "  \"ZoneVarID\" serial PRIMARY KEY,\n" +
                        "  \"ZoneEWID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"ZoneEW\" (\"ZoneEWID\") " : "") + "NOT NULL,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createZoneTableIfNotExists(String globalEASTWebSchema, final Statement stmt, boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"ZoneEW\"\n" +
                        "(\n" +
                        "  \"ZoneEWID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createIndexTableIfNotExists(String globalEASTWebSchema, final Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"Index\"\n" +
                        "(\n" +
                        "  \"IndexID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createDownloadExtraTableIfNotExists(String globalEASTWebSchema, final Statement stmt, boolean createTablesWithForeignKeyReferences) throws SQLException
    {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"DownloadExtra\"\n" +
                        "(\n" +
                        "  \"DownloadExtraID\" serial PRIMARY KEY,\n" +
                        "  \"DownloadID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"Download\" (\"DownloadID\") " : "") + "DEFAULT NULL,\n" +
                        "  \"GlobalDownloaderID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"GlobalDownloader\" (\"GlobalDownloaderID\") " : "") + "NOT NULL,\n" +
                        "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                        "  \"DataName\" varchar(255) NOT NULL,\n" +
                        "  \"FilePath\" varchar(255) UNIQUE NOT NULL,\n" +
                        "  \"Complete\" boolean DEFAULT FALSE\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createDownloadTableIfNotExists(String globalEASTWebSchema, ArrayList<String> extraDownloadFiles, final Statement stmt, boolean createTablesWithForeignKeyReferences)
            throws SQLException {
        if(globalEASTWebSchema == null || extraDownloadFiles == null) {
            return;
        }

        StringBuilder query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE IF NOT EXISTS \"%1$s\".\"Download\"\n" +
                                "(\n" +
                                "  \"DownloadID\" serial PRIMARY KEY,\n" +
                                "  \"GlobalDownloaderID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"GlobalDownloader\" (\"GlobalDownloaderID\") " : "") + "NOT NULL,\n" +
                                "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                                "  \"DataFilePath\" varchar(255) UNIQUE NOT NULL,\n" +
                                "  \"Complete\" boolean DEFAULT FALSE\n" +
                                ")",
                                globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());
    }

    private static void createGlobalDownloaderExpectedResultsTableIfNotExists(String globalEASTWebSchema, final Statement stmt, boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"GDExpectedResults\"\n" +
                        "(\n" +
                        "  \"GDExpectedResultsID\" serial PRIMARY KEY,\n" +
                        "  \"GlobalDownloaderID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"GlobalDownloader\" (\"GlobalDownloaderID\") " : "") + "NOT NULL,\n" +
                        "  \"ExpectedResultsID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"ExpectedResults\" (\"ExpectedResultsID\") " : "") + "NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createGlobalDownloaderTableIfNotExists(String globalEASTWebSchema, final Statement stmt, boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"GlobalDownloader\"\n" +
                        "(\n" +
                        "  \"GlobalDownloaderID\" serial PRIMARY KEY,\n" +
                        "  \"PluginID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"Plugin\" (\"PluginID\") " : "") + "NOT NULL\n," +
                        "  \"DataName\" varchar(255) NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createExpectedResultsTableIfNotExists(String globalEASTWebSchema, final Statement stmt, boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"ExpectedResults\"\n" +
                        "(\n" +
                        "  \"ExpectedResultsID\" serial PRIMARY KEY,\n" +
                        "  \"ProjectID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"Project\" (\"ProjectID\") " : "") + "NOT NULL,\n" +
                        "  \"PluginID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"Plugin\" (\"PluginID\") " : "") + "NOT NULL,\n" +
                        "  \"ExpectedTotalResults\" bigint NOT NULL,\n" +
                        "  \"TemporalSummaryCompositionStrategyID\" integer "
                        + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"TemporalSummaryCompositionStrategy\" (\"TemporalSummaryCompositionStrategyID\") " : "") + "NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createTemporalSummaryCompositionStrategyTableIfNotExists(String globalEASTWebSchema, final Statement stmt) throws SQLException
    {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"TemporalSummaryCompositionStrategy\"\n" +
                        "(\n" +
                        "  \"TemporalSummaryCompositionStrategyID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createPluginTableIfNotExists(String globalEASTWebSchema, final Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"Plugin\"\n" +
                        "(\n" +
                        "  \"PluginID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL,\n" +
                        "  \"DaysPerInputFile\" integer NOT NULL,\n" +
                        "  \"FilesPerDay\" integer NOT NULL" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createProjectTableIfNotExists(String globalEASTWebSchema, final Statement stmt, boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"Project\"\n" +
                        "(\n" +
                        "  \"ProjectID\" serial PRIMARY KEY,\n" +
                        "  \"Name\" varchar(255) UNIQUE NOT NULL,\n" +
                        // Represents the project's start date
                        "  \"StartDate_DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                        "  \"IndicesCount\" integer NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createDateGroupTable(String globalEASTWebSchema, final Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"DateGroup\"\n" +
                        "(\n" +
                        "  \"DateGroupID\" serial PRIMARY KEY,\n" +
                        "  \"Year\" integer NOT NULL,\n" +
                        "  \"DayOfYear\" integer NOT NULL\n" +
                        ")",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static void createSchemaIfNotExist(final Statement stmt, final String mSchemaName) throws SQLException {
        if(mSchemaName == null) {
            return;
        }

        String query;
        query = String.format(
                "CREATE SCHEMA IF NOT EXISTS \"%s\"",
                mSchemaName
                );
        stmt.executeUpdate(query);
    }

    private static void dropSchemaIfExists(final Statement stmt, final String mSchemaName) throws SQLException {
        if(mSchemaName == null) {
            return;
        }

        String query;
        query = String.format(
                "DROP SCHEMA IF EXISTS \"%s\" CASCADE",
                mSchemaName
                );
        stmt.executeUpdate(query);
    }
}
