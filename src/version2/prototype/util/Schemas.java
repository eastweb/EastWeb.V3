package version2.prototype.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.ConfigReadException;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
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
     * @param numOfIndices  - relevant to project entry creation and calculating the number of expected results to be found in ZonalStat
     * @param summaries  - relevant to calculating the number of expected results to be found in ZonalStat
     * @param createTablesWithForeignKeyReferences  - TRUE if tables should be created so that their foreign keys are referencing their corresponding primary keys, FALSE if tables shouldn't be created
     * to enforce foreign key rules
     * @throws ConfigReadException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void CreateProjectPluginSchema(Connection postgreSQLConnection, String globalEASTWebSchema, String projectName, String pluginName, ArrayList<String> summaryNames,
            ArrayList<String> extraDownloadFiles, LocalDate startDate, Integer daysPerInputFile, Integer numOfIndices, ArrayList<ProjectInfoSummary> summaries,
            boolean createTablesWithForeignKeyReferences) throws ConfigReadException, SQLException, ClassNotFoundException
    {
        final Connection conn = postgreSQLConnection;
        final Statement stmt = conn.createStatement();
        final String mSchemaName = getSchemaName(projectName, pluginName);

        // Drop an existing schema with the same name
        dropSchemaIfExists(stmt, mSchemaName);

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

        // Create ExpectedResults table to keep track of how many results are expected for each plugin for each project
        createExpectedResultsTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

        // Create GlobalDownloader table
        createGlobalDownloaderTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

        // Create GDExpectedResults table
        createGlobalDownloaderExpectedResultsTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

        // Create Download table
        createDownloadTableIfNotExists(globalEASTWebSchema, extraDownloadFiles, stmt, createTablesWithForeignKeyReferences);

        // Create ExtraDownload table
        createExtraDownloadTableIfNotExists(globalEASTWebSchema, stmt, createTablesWithForeignKeyReferences);

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
        createDownloadCacheTableIfNotExists(globalEASTWebSchema, extraDownloadFiles, stmt, mSchemaName, createTablesWithForeignKeyReferences);
        createProcessorCacheTableIfNotExists(globalEASTWebSchema, extraDownloadFiles, stmt, mSchemaName, createTablesWithForeignKeyReferences);
        createIndicesCacheTableIfNotExists(globalEASTWebSchema, extraDownloadFiles, stmt, mSchemaName, createTablesWithForeignKeyReferences);

        // Get DateGroupID
        int dateGroupID = getDateGroupID(globalEASTWebSchema, startDate, stmt);

        // Add entry to Project table
        int projectID = addProject(globalEASTWebSchema, projectName, numOfIndices, stmt, dateGroupID);

        // Add entry to Plugin table if not already existing
        int pluginID = addPlugin(globalEASTWebSchema, pluginName, daysPerInputFile, stmt);

        // Add entry to EASTWeb global ExpectedResults table
        addExpectedResults(globalEASTWebSchema, startDate, daysPerInputFile, numOfIndices, summaries, projectID, pluginID, conn, stmt);
    }

    public static boolean registerGlobalDownloader(String globalEASTWebSchema, String pluginName, int daysPerInputFile, int globalDownloaderInstanceID) throws ClassNotFoundException, SQLException,
    ParserConfigurationException, SAXException, IOException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final int pluginID = getPluginID(globalEASTWebSchema, pluginName, daysPerInputFile, stmt);

        String query = "INSERT INTO \"" + globalEASTWebSchema + "\".\"GlobalDownloader\" (\"PluginID\", \"UniqueInstanceNum\") VALUES (" + pluginID + ", " + globalDownloaderInstanceID + ");";
        return stmt.execute(query);
    }

    public static int loadUnprocessedDownloadsToLocalDownloader(String globalEASTWebSchema, String projectName, String pluginName, int globalDownloaderInstanceID, LocalDate startDate,
            ArrayList<String> extraDownloadFiles, int daysPerInputFile) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final int gdlID = getGlobalDownloaderID(globalEASTWebSchema, pluginName, daysPerInputFile, globalDownloaderInstanceID);
        final String mSchemaName = getSchemaName(projectName, pluginName);

        StringBuilder query = new StringBuilder(String.format(
                "INSERT INTO \"%1$s\".\"DownloadCache\" (\"DownloadID\", \"DataFilePath\", ",
                mSchemaName
                ));
        for(String dataName : extraDownloadFiles)
        {
            query.append("\"" + dataName + "FilePath\", ");
        }
        query.append("\"DateGroupID\") "
                + "SELECT D.\"DownloadID\", D.\"DataFilePath\", ");
        for(int i=0; i < extraDownloadFiles.size(); i++)
        {
            query.append(Character.toChars('E' + i)[0] + ".\"FilePath\", ");
        }
        query.append(String.format("D.\"DateGroupID\" "
                + "FROM \"%1$s\".\"Download\" D ",
                globalEASTWebSchema
                ));
        for(int i=0; i < extraDownloadFiles.size(); i++)
        {
            query.append(String.format(
                    "INNER JOIN \"%1$s\".\"ExtraDownload\" " + Character.toChars('E' + i)[0] + " ON D.\"DownloadID\"=" + Character.toChars('E' + i)[0] + ".\"DownloadID\" ",
                    globalEASTWebSchema));
        }
        query.append(String.format("LEFT JOIN \"%1$s\".\"DownloadCache\" L ON D.\"DownloadID\"=L.\"DownloadID\" "
                + "WHERE L.\"DownloadID\" IS NULL",
                mSchemaName));
        for(int i=0; i < extraDownloadFiles.size(); i++)
        {
            query.append(" AND " + Character.toChars('E' + i)[0] + ".\"DataName\"='" + extraDownloadFiles.get(i) + "';");
        }
        return stmt.executeUpdate(query.toString());
    }

    public static ArrayList<DataFileMetaData> getAllDownloadedFiles(String globalEASTWebSchema, String pluginName, int globalDownloaderInstanceID, int daysPerInputFile) throws ClassNotFoundException,
    SQLException, ParserConfigurationException, SAXException, IOException {
        Map<Integer, DataFileMetaData> downloadsList = new HashMap<Integer, DataFileMetaData>();
        ArrayList<Integer> downloadIDs = new ArrayList<Integer>(0);

        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final int gdlID = getGlobalDownloaderID(globalEASTWebSchema, pluginName, daysPerInputFile, globalDownloaderInstanceID);

        ResultSet rs;
        rs = stmt.executeQuery(String.format(
                "SELECT A.\"DownloadID\", A.\"DateGroupID\", A.\"DataFilePath\", B.\"Year\", B.\"DayOfYear\" FROM \"%1$s\".\"Download\" A, \"%1$s\".\"DateGroup\" B " +
                        "WHERE A.\"GlobalDownloaderID\"=" + gdlID + " AND B.\"DateGroupID\"=A.\"DateGroupID\";",
                        globalEASTWebSchema
                ));
        if(rs != null)
        {
            while(rs.next())
            {
                downloadIDs.add(rs.getInt("DownloadID"));
                downloadsList.put(rs.getInt("DownloadID"), new DataFileMetaData("Data", rs.getString("DataFilePath"), rs.getInt("DateGroupID"), rs.getInt("Year"), rs.getInt("DayOfYear")));
            }
        }
        rs.close();

        StringBuilder query = new StringBuilder(String.format(
                "SELECT A.\"DownloadID\", A.\"DataName\", A.\"FilePath\", B.\"DateGroupID\", B.\"Year\", B.\"DayOfYear\" " +
                        "FROM \"%1$s\".\"ExtraDownload\" A INNER JOIN \"%1$s\".\"Download\" D ON A.\"DownloadID\"=D.\"DownloadID\" " +
                        "INNER JOIN \"%1$s\".\"DateGroup\" B ON D.\"DateGroupID\"=B.\"DateGroupID\" WHERE ",
                        globalEASTWebSchema));

        if(downloadIDs.size() > 0)
        {
            query.append("A.\"DownloadID\"=" + downloadIDs.get(0));
            for(int i=1; i < downloadIDs.size(); i++)
            {
                query.append(" OR A.\"DownloadID\"=" + downloadIDs.get(i));
            }
        }

        rs = stmt.executeQuery(query.toString());
        if(rs != null)
        {
            DownloadFileMetaData temp;
            while(rs.next())
            {
                temp = downloadsList.get(rs.getInt("DownloadID")).ReadMetaDataForProcessor();
                temp.extraDownloads.add(new DataFileMetaData(rs.getString("DataName"), rs.getString("FilePath"), rs.getInt("DateGroupID"), rs.getInt("Year"), rs.getInt("DayOfYear")));
                downloadsList.put(rs.getInt("DownloadID"), new DataFileMetaData("Data", temp.dataFilePath, temp.dataGroupID, temp.year, temp.day, temp.extraDownloads));
            }
        }
        rs.close();

        return new ArrayList<DataFileMetaData>(downloadsList.values());
    }

    public static TreeMap<Integer, Integer> udpateExpectedResults(String globalEASTWebSchema, String projectName, String pluginName, LocalDate startDate, Integer daysPerInputFile, Integer numOfIndices,
            ArrayList<ProjectInfoSummary> summaries) throws SQLException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException {
        TreeMap<Integer, Integer> results = new TreeMap<Integer, Integer>();
        if(globalEASTWebSchema == null || startDate == null || daysPerInputFile == null || numOfIndices == null || summaries == null || projectName == null || pluginName == null) {
            return results;
        }

        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        //        final String mSchemaName = getSchemaName(projectName, pluginName);

        int dateGroupID = getDateGroupID(globalEASTWebSchema, startDate, stmt);
        int projectID = getProjectID(globalEASTWebSchema, projectName, numOfIndices, stmt, dateGroupID);
        int pluginID = getPluginID(globalEASTWebSchema, pluginName, daysPerInputFile, stmt);

        PreparedStatement preparedStmt = conn.prepareStatement(String.format(
                "UPDATE \"%1$s\".\"ExpectedResults\" SET \"ExpectedTotalResults\" = ? WHERE \"ExpectedResultsID\" = ?;",
                globalEASTWebSchema
                ));

        int temporalSummaryCompositionStrategyID;
        int expectedResultsID;
        int expectedTotalResults;
        for(ProjectInfoSummary summary : summaries)
        {
            temporalSummaryCompositionStrategyID = getTemporalSummaryCompositionStrategyID(globalEASTWebSchema, summary.GetTemporalSummaryCompositionStrategyClassName(), stmt);
            expectedResultsID = getExpectedResultsID(globalEASTWebSchema, projectID, pluginID, temporalSummaryCompositionStrategyID, stmt);
            expectedTotalResults = summary.GetTemporalFileStore().compStrategy.getNumberOfCompleteCompositesInRange(startDate, LocalDate.now().plusDays(1), daysPerInputFile) * numOfIndices;
            preparedStmt.setInt(1, expectedTotalResults);
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

    public static void insertIntoExtraDownloadTable(String globalEASTWebSchema, String pluginName, int globalDownloaderInstanceID, LocalDate dataDate,
            String dataName, String dataFilePath, int daysPerInputFile) throws SQLException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException
    {
        if(globalEASTWebSchema == null || pluginName == null || dataDate == null || dataName == null || dataFilePath == null) {
            return;
        }
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        int gdlID = getGlobalDownloaderID(globalEASTWebSchema, pluginName, daysPerInputFile, globalDownloaderInstanceID);
        int dateGroupID = getDateGroupID(globalEASTWebSchema, dataDate, stmt);
        int downloadID = getDownloadID(globalEASTWebSchema, gdlID, dateGroupID);

        String query = String.format(
                "INSERT INTO \"%1$s\".\"ExtraDownload\" (\"DownloadID\", \"DataName\", \"FilePath\") VALUES\n" +
                        "(" + downloadID + ", '" + dataName + "', '" + dataFilePath + "');",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    public static void insertIntoDownloadTable(String globalEASTWebSchema, String pluginName, int globalDownloaderInstanceID, LocalDate dataDate, String dataFilePath,
            int daysPerInputFile) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        int gdlID = getGlobalDownloaderID(globalEASTWebSchema, pluginName, daysPerInputFile, globalDownloaderInstanceID);
        int dateGroupID = getDateGroupID(globalEASTWebSchema, dataDate, stmt);

        String query = String.format(
                "INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\") VALUES\n" +
                        "(" + gdlID + ", " + dateGroupID + ", '" + dataFilePath + "');",
                        globalEASTWebSchema
                );
        stmt.executeUpdate(query);
    }

    private static int getDownloadID(String globalEASTWebSchema, Integer globalDownloaderID, Integer dateGroupID) throws ClassNotFoundException, SQLException,
    ParserConfigurationException, SAXException, IOException {
        if(globalEASTWebSchema == null || globalDownloaderID == null || dateGroupID == null) {
            return -1;
        }

        int ID = -1;
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();

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

    private static int getGlobalDownloaderID(String globalEASTWebSchema, String pluginName, int daysPerInputFile, int instanceID) throws SQLException,
    ClassNotFoundException, ParserConfigurationException, SAXException, IOException
    {
        int ID = -1;
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();

        int pluginID = getPluginID(globalEASTWebSchema, pluginName, daysPerInputFile, stmt);

        ResultSet rs;
        rs = stmt.executeQuery(String.format(
                "SELECT \"GlobalDownloaderID\" FROM \"%1$s\".\"GlobalDownloader\" WHERE \n" +
                        "\"PluginID\"=" + pluginID + " AND \"UniqueInstanceNum\"=" + instanceID + " ORDER BY \"GlobalDownloaderID\" DESC;",
                        globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            ID = rs.getInt("GlobalDownloaderID");
        }
        rs.close();
        return ID;
    }

    private static int getTemporalSummaryCompositionStrategyID(String globalEASTWebSchema, String temporalSummaryCompositionStrategyClassName, Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null || temporalSummaryCompositionStrategyClassName == null) {
            return -1;
        }

        ResultSet rs;
        int temporalSummaryCompositionStrategyID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"TemporalSummaryCompositionStrategyID\" FROM \"%1$s\".\"TemporalSummaryCompositionStrategy\" " +
                "WHERE \"Name\"='" + temporalSummaryCompositionStrategyClassName + "';",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            temporalSummaryCompositionStrategyID = rs.getInt("TemporalSummaryCompositionStrategyID");
        }
        rs.close();
        return temporalSummaryCompositionStrategyID;
    }

    private static int getExpectedResultsID(String globalEASTWebSchema, Integer projectID, Integer pluginID, Integer temporalSummaryCompositionStrategyID, Statement stmt) throws SQLException
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

    private static int getPluginID(String globalEASTWebSchema, String pluginName, Integer daysPerInputFile, final Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null || pluginName == null || daysPerInputFile == null) {
            return -1;
        }

        ResultSet rs;
        int pluginID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"PluginID\" FROM \"%1$s\".\"Plugin\" " +
                "WHERE \"Name\"='" + pluginName + "' AND \"DaysPerInputFile\"=" + daysPerInputFile + ";",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            pluginID = rs.getInt("PluginID");
        }
        rs.close();
        return pluginID;
    }

    private static int getProjectID(String globalEASTWebSchema, String projectName, Integer numOfIndices, final Statement stmt, Integer dateGroupID) throws SQLException
    {
        if(globalEASTWebSchema == null || numOfIndices == null || dateGroupID == null) {
            return -1;
        }

        ResultSet rs;
        int projectID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"ProjectID\" FROM \"%1$s\".\"Project\" " +
                "WHERE \"Name\"='" + projectName + "' AND \"StartDate_DateGroupID\"=" + dateGroupID + " AND \"IndicesCount\"=" + numOfIndices + ";",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            projectID = rs.getInt("ProjectID");
        }

        rs.close();
        return projectID;
    }

    private static int[] addExpectedResults(String globalEASTWebSchema, LocalDate startDate, Integer daysPerInputFile, Integer numOfIndices, ArrayList<ProjectInfoSummary> summaries, Integer projectID,
            Integer pluginID, final Connection conn, final Statement stmt) throws SQLException{
        if(globalEASTWebSchema == null || startDate == null || daysPerInputFile == null || numOfIndices == null || summaries == null || projectID == null || pluginID == null) {
            return new int[0];
        }
        PreparedStatement preparedStmt = conn.prepareStatement(String.format(
                "INSERT INTO \"%1$s\".\"ExpectedResults\" (\"ProjectID\", \"PluginID\", \"ExpectedTotalResults\", \"TemporalSummaryCompositionStrategyClass\") VALUES (" +
                        projectID + ", " +
                        pluginID + ", " +
                        "?, " +   // 1. ExpectedTotalResults
                        "? " +  // 2. TemporalSummaryCompositionStrategyClass
                        ")",
                        globalEASTWebSchema
                ));

        int expectedTotalResults;
        for(ProjectInfoSummary summary : summaries)
        {
            expectedTotalResults = summary.GetTemporalFileStore().compStrategy.getNumberOfCompleteCompositesInRange(startDate, LocalDate.now().plusDays(1), daysPerInputFile) * numOfIndices;
            preparedStmt.setInt(1, expectedTotalResults);
            preparedStmt.setString(2, summary.GetTemporalSummaryCompositionStrategyClassName());
            preparedStmt.addBatch();
        }
        return preparedStmt.executeBatch();
    }

    private static int addPlugin(String globalEASTWebSchema, String pluginName, Integer daysPerInputFile, final Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null || pluginName == null || daysPerInputFile == null) {
            return -1;
        }

        ResultSet rs;
        int pluginID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"PluginID\" FROM \"%1$s\".\"Plugin\" " +
                "WHERE \"Name\"='" + pluginName + "' AND \"DaysPerInputFile\"=" + daysPerInputFile + ";",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            pluginID = rs.getInt("PluginID");
        }
        else{
            stmt.executeUpdate(String.format(
                    "INSERT INTO \"%1$s\".\"Plugin\" (\"Name\", \"DaysPerInputFile\") VALUES ('" + pluginName + "'," + daysPerInputFile + ");",
                    globalEASTWebSchema
                    ));
            rs = stmt.executeQuery(String.format("SELECT \"PluginID\" FROM \"%1$s\".\"Plugin\" " +
                    "WHERE \"Name\"='" + pluginName + "' AND \"DaysPerInputFile\"=" + daysPerInputFile + ";",
                    globalEASTWebSchema
                    ));
            if(rs != null && rs.next())
            {
                pluginID = rs.getInt("PluginID");
            }
        }
        rs.close();
        return pluginID;
    }

    private static int addProject(String globalEASTWebSchema, String projectName, Integer numOfIndices, final Statement stmt, Integer dateGroupID) throws SQLException {
        if(globalEASTWebSchema == null || numOfIndices == null || dateGroupID == null) {
            return -1;
        }

        ResultSet rs;
        int projectID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"ProjectID\" FROM \"%1$s\".\"Project\" " +
                "WHERE \"Name\"='" + projectName + "' AND \"StartDate_DateGroupID\"=" + dateGroupID + " AND \"IndicesCount\"=" + numOfIndices + ";",
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
                    "WHERE \"Name\"='" + projectName + "' AND \"StartDate_DateGroupID\"=" + dateGroupID + " AND \"IndicesCount\"=" + numOfIndices + ";",
                    globalEASTWebSchema
                    ));
            if(rs != null && rs.next())
            {
                projectID = rs.getInt("ProjectID");
            }
        }
        rs.close();
        return projectID;
    }

    private static int getDateGroupID(String globalEASTWebSchema, LocalDate date, final Statement stmt) throws SQLException {
        if(globalEASTWebSchema == null || date == null) {
            return -1;
        }

        ResultSet rs;
        int dateGroupID = -1;
        rs = stmt.executeQuery(String.format("SELECT \"DateGroupID\" FROM \"%1$s\".\"DateGroup\" " +
                "WHERE \"Year\"=" + date.getYear() + " AND \"DayOfYear\"=" + date.getDayOfYear() + ";",
                globalEASTWebSchema
                ));
        if(rs != null && rs.next())
        {
            dateGroupID = rs.getInt("DateGroupID");
        }
        else{
            stmt.executeUpdate(String.format(
                    "INSERT INTO \"%1$s\".\"DateGroup\" (\"Year\", \"DayOfYear\") VALUES (" + date.getYear() + "," + date.getDayOfYear() + ");",
                    globalEASTWebSchema
                    ));
            rs = stmt.executeQuery(String.format("SELECT \"DateGroupID\" FROM \"%1$s\".\"DateGroup\" " +
                    "WHERE \"Year\"=" + date.getYear() + " AND \"DayOfYear\"=" + date.getDayOfYear() + ";",
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

    private static void createIndicesCacheTableIfNotExists(String globalEASTWebSchema, ArrayList<String> extraDownloadFiles, final Statement stmt, final String mSchemaName,
            boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null || mSchemaName == null || extraDownloadFiles == null) {
            return;
        }

        StringBuilder query_;
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
                        "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                                "  \"IndexID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"Index\" (\"IndexID\") " : "") + "NOT NULL,\n" +
                                "  \"Retrieved\" boolean DEFAULT FALSE,\n" +
                                "  \"Processed\" boolean DEFAULT FALSE\n" +
                                ")",
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
                        "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                                "  \"Retrieved\" boolean DEFAULT FALSE,\n" +
                                "  \"Processed\" boolean DEFAULT FALSE\n" +
                                ")",
                                globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());
    }

    private static void createDownloadCacheTableIfNotExists(String globalEASTWebSchema, ArrayList<String> extraDownloadFiles, final Statement stmt, final String mSchemaName,
            boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null || mSchemaName == null || extraDownloadFiles == null) {
            return;
        }

        StringBuilder query_;
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
                        "  \"DownloadID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"Download\" (\"DownloadID\") " : "") + "NOT NULL,\n" +
                                "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                                "  \"Retrieved\" boolean DEFAULT FALSE,\n" +
                                "  \"Processed\" boolean DEFAULT FALSE\n" +
                                ")",
                                globalEASTWebSchema
                        ));
        stmt.executeUpdate(query_.toString());
    }

    private static void createZonalStatTableIfNotExists(String globalEASTWebSchema, ArrayList<String> summaryNames, final Statement stmt, final String mSchemaName,
            boolean createTablesWithForeignKeyReferences) throws SQLException {
        if(globalEASTWebSchema == null || mSchemaName == null || summaryNames == null) {
            return;
        }

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

        StringBuilder query_;
        query_ = new StringBuilder(
                String.format(
                        "CREATE TABLE \"%1$s\".\"ZonalStat\"\n" +
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
                "CREATE TABLE \"%1$s\".\"ZoneMapping\"\n" +
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
                "CREATE TABLE \"%1$s\".\"ZoneField\"\n" +
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

    private static void createExtraDownloadTableIfNotExists(String globalEASTWebSchema, final Statement stmt, boolean createTablesWithForeignKeyReferences) throws SQLException
    {
        if(globalEASTWebSchema == null) {
            return;
        }

        String query = String.format(
                "CREATE TABLE IF NOT EXISTS \"%1$s\".\"ExtraDownload\"\n" +
                        "(\n" +
                        "  \"ExtraDownloadID\" serial PRIMARY KEY,\n" +
                        "  \"DownloadID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"Download\" (\"DownloadID\") " : "") + "NOT NULL,\n" +
                        //                        "  \"GlobalDownloaderID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"GlobalDownloader\" (\"GlobalDownloaderID\") " : "") + "NOT NULL,\n" +
                        //                        "  \"DateGroupID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"DateGroup\" (\"DateGroupID\") " : "") + "NOT NULL,\n" +
                        "  \"DataName\" varchar(255) NOT NULL,\n" +
                        "  \"FilePath\" varchar(255) UNIQUE NOT NULL\n" +
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
                                "  \"DataFilePath\" varchar(255) UNIQUE NOT NULL\n" +
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
                        "  \"PluginID\" integer " + (createTablesWithForeignKeyReferences ? "REFERENCES \"%1$s\".\"Plugin\" (\"PluginID\") " : "") + "NOT NULL,\n" +
                        "  \"UniqueInstanceNum\" integer NOT NULL\n" +
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
                        "  \"ExpectedTotalResults\" integer NOT NULL,\n" +
                        "  \"TemporalSummaryCompositionStrategyClass\" varchar(255) NOT NULL\n" +
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
                        "  \"DaysPerInputFile\" integer NOT NULL\n" +
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
