/**
 *
 */
package version2.prototype.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;

/**
 * @author michael.devos
 *
 */
public class EASTWebResults {

    /**
     * Get the EASTWebQuery object that represents a query for retrieving zonal summary results.
     *
     * @param globalSchema
     * @param projectName
     * @param pluginName
     * @param selectCount
     * @param selectMax
     * @param selectMin
     * @param selectSum
     * @param selectMean
     * @param selectSqrSum
     * @param selectStdDev
     * @param zoneSign
     * @param zoneVal
     * @param yearSign
     * @param yearVal
     * @param daySign
     * @param dayVal
     * @param includedIndices
     * @param summaryIDs
     * @return  Returns a EASTWebQuery object that represents a query that can be executed to acquire results from EASTWeb
     */
    public static EASTWebQuery GetEASTWebQuery(String globalSchema, String projectName, String pluginName, boolean selectCount, boolean selectMax, boolean selectMin,
            boolean selectSum, boolean selectMean, boolean selectSqrSum, boolean selectStdDev, String zoneSign, Integer zoneVal, String yearSign, Integer yearVal, String daySign, Integer dayVal,
            String[] includedIndices, Integer[] summaryIDs)
    {
        String zoneCondition = "";
        if(zoneSign == null || zoneVal == null) {
            zoneCondition = "";
        }
        else {
            zoneCondition = " AND A.\"AreaCode\"" + zoneSign + zoneVal;
        }

        return getEASTWebQuery(globalSchema, projectName, pluginName, selectCount, selectMax, selectMin, selectSum, selectMean, selectSqrSum, selectStdDev, zoneCondition,
                yearSign, yearVal, daySign, dayVal, includedIndices, summaryIDs);
    }
    /**
     * Get the EASTWebQuery object that represents a query for retrieving zonal summary results.
     *
     * @param globalSchema
     * @param projectName
     * @param pluginName
     * @param selectCount
     * @param selectMax
     * @param selectMin
     * @param selectSum
     * @param selectMean
     * @param selectSqrSum
     * @param selectStdDev
     * @param zones
     * @param yearSign
     * @param yearVal
     * @param daySign
     * @param dayVal
     * @param includedIndices
     * @param summaryIDs
     * @return  Returns a EASTWebQuery object that represents a query that can be executed to acquire results from EASTWeb
     */
    public static EASTWebQuery GetEASTWebQuery(String globalSchema, String projectName, String pluginName, boolean selectCount, boolean selectMax, boolean selectMin,
            boolean selectSum, boolean selectMean, boolean selectSqrSum, boolean selectStdDev, String[] zones, String yearSign, Integer yearVal, String daySign, Integer dayVal,
            String[] includedIndices, Integer[] summaryIDs)
    {
        StringBuilder zoneCondition = new StringBuilder();
        if(zones.length > 0)
        {
            zoneCondition.append(" AND (A.\"AreaName\" like '" + Schemas.escapeUnderScoresAndPercents(zones[0]) + "'");
            for(int i=1; i < zones.length; i++) {
                zoneCondition.append(" OR A.\"AreaName\" like '" + Schemas.escapeUnderScoresAndPercents(zones[i]) + "'");
            }
            zoneCondition.append(")");
        }

        return getEASTWebQuery(globalSchema, projectName, pluginName, selectCount, selectMax, selectMin, selectSum, selectMean, selectSqrSum, selectStdDev, zoneCondition.toString(),
                yearSign, yearVal, daySign, dayVal, includedIndices, summaryIDs);
    }

    /**
     * Get the EASTWebQuery object that represents the SQL query to handle getting EASTWeb zonal summary results associated with the given project and plugin names and for the given list of indices.
     *
     * @param globalSchema  - the global schema name (can be gotten from Config.getInstance().getGlobalSchema()
     * @param projectName  - the project name to be queried
     * @param pluginName  - the plugin name to be queried
     * @param indices  - the environmental indices to get summary information for
     * @return The EASTWebQuery object that contains the query that can retrieve results from zonal summary associated with the given parameters.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static EASTWebQuery GetEASTWebQuery(String globalSchema, String projectName, String pluginName, String... indices) throws ParserConfigurationException, SAXException, IOException
    {
        // Check parameters given
        if(projectName == null || pluginName == null) {
            return null;
        }
        // TemporalSummaryCompositionStrategyID
        // Build query
        ArrayList<String> summaries = Config.getInstance().getSummaryCalculations();
        String schemaName = Schemas.getSchemaName(projectName, pluginName);
        StringBuilder query = new StringBuilder("SELECT A.\"AreaName\", A.\"AreaCode\", P.\"AreaNameField\", P.\"AreaCodeField\", P.\"ShapeFile\", C.\"Year\", C.\"DayOfYear\", I.\"Name\" as \"IndexName\", " +
                "T.\"Name\" as \"TemporalSummaryCompositionStrategyClass\", A.\"" + summaries.get(0) + "\"");
        for(int i=1; i < summaries.size(); i++)
        {
            query.append(", A.\"" + summaries.get(i) + "\"");
        }
        query.append(", A.\"FilePath\"");
        query.append(" FROM \"" + schemaName + "\".\"ZonalStat\" A, (SELECT \"Name\", \"IndexID\" FROM \"" + globalSchema + "\".\"Index\"");
        if(indices != null && indices.length > 0)
        {
            query.append(" WHERE (\"Name\" = '" + indices[0] + "'");
            for(int i=1; i < indices.length; i++)
            {
                query.append(" OR \"Name\" = '" + indices[i] + "'");
            }
            query.append(")");
        }
        query.append(") I, \"" + globalSchema + "\".\"DateGroup\" C, \"" + globalSchema + "\".\"ProjectSummary\" P, \"" + globalSchema + "\".\"TemporalSummaryCompositionStrategy\" T " +
                "WHERE A.\"IndexID\" = I.\"IndexID\" AND A.\"DateGroupID\" = C.\"DateGroupID\" AND A.\"ProjectSummaryID\" = P.\"ProjectSummaryID\" " +
                "AND P.\"TemporalSummaryCompositionStrategyID\" = T.\"TemporalSummaryCompositionStrategyID\";");

        // Create custom query holder object (keeps users from being able to use this class to create custom queries and directly passing them to the database).
        return new EASTWebQuery(projectName, pluginName, query.toString());
    }

    /**
     * Get the EASTWebQuery object that represents the SQL query to handle getting EASTWeb zonal summary results associated with the given project and plugin names and for the given list of indices.
     *
     * @param globalSchema  - the global schema name (can be gotten from Config.getInstance().getGlobalSchema()
     * @param projectName  - the project name to be queried
     * @param pluginName  - the plugin name to be queried
     * @param zones  - the zones to get summary information for
     * @param indices  - the environmental indices to get summary information for
     * @return The EASTWebQuery object that contains the query that can retrieve results from zonal summary associated with the given parameters.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static EASTWebQuery GetEASTWebQuery(String globalSchema, String projectName, String pluginName, ArrayList<String> zones, String... indices)
            throws ParserConfigurationException, SAXException, IOException
    {
        // Check parameters given
        if(projectName == null || pluginName == null) {
            return null;
        }
        // TemporalSummaryCompositionStrategyID
        // Build query
        ArrayList<String> summaries = Config.getInstance().getSummaryCalculations();
        String schemaName = Schemas.getSchemaName(projectName, pluginName);
        StringBuilder query = new StringBuilder("SELECT A.\"AreaName\", A.\"AreaCode\", P.\"AreaNameField\", P.\"AreaCodeField\", P.\"ShapeFile\", C.\"Year\", C.\"DayOfYear\", I.\"Name\" as \"IndexName\", " +
                "T.\"Name\" as \"TemporalSummaryCompositionStrategyClass\", A.\"" + summaries.get(0) + "\"");
        for(int i=1; i < summaries.size(); i++)
        {
            query.append(", A.\"" + summaries.get(i) + "\"");
        }
        query.append(", A.\"FilePath\"");
        query.append(" FROM \"" + schemaName + "\".\"ZonalStat\" A, (SELECT \"Name\", \"IndexID\" FROM \"" + globalSchema + "\".\"Index\"");
        if(indices != null && indices.length > 0)
        {
            query.append(" WHERE (\"Name\" = '" + indices[0] + "'");
            for(int i=1; i < indices.length; i++)
            {
                query.append(" OR \"Name\" = '" + indices[i] + "'");
            }
            query.append(")");
        }

        StringBuilder zoneCondition = new StringBuilder();
        if(zones.size() > 0)
        {
            zoneCondition.append(" AND (A.\"AreaName\" like '" + Schemas.escapeUnderScoresAndPercents(zones.get(0)) + "'");
            for(int i=1; i < zones.size(); i++) {
                zoneCondition.append(" OR A.\"AreaName\" like '" + Schemas.escapeUnderScoresAndPercents(zones.get(i)) + "'");
            }
            zoneCondition.append(")");
        }

        query.append(") I, \"" + globalSchema + "\".\"DateGroup\" C, \"" + globalSchema + "\".\"ProjectSummary\" P, \"" + globalSchema + "\".\"TemporalSummaryCompositionStrategy\" T " +
                "WHERE A.\"IndexID\" = I.\"IndexID\" AND A.\"DateGroupID\" = C.\"DateGroupID\" AND A.\"ProjectSummaryID\" = P.\"ProjectSummaryID\" " +
                "AND P.\"TemporalSummaryCompositionStrategyID\" = T.\"TemporalSummaryCompositionStrategyID\"");
        query.append(zoneCondition.toString());
        query.append(";");

        // Create custom query holder object (keeps users from being able to use this class to create custom queries and directly passing them to the database).
        return new EASTWebQuery(projectName, pluginName, query.toString());
    }

    /**
     * Get the EASTWebQuery object that represents the SQL query to handle getting EASTWeb zonal summary results associated with the given project and plugin names.
     *
     * @param globalSchema  - the global schema name (can be gotten from Config.getInstance().getGlobalSchema()
     * @param projectName  - the project name to be queried
     * @param pluginName  - the plugin name to be queried
     * @return The EASTWebQuery object that contains the query that can retrieve results from zonal summary associated with the given parameters.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static EASTWebQuery GetEASTWebQuery(String globalSchema, String projectName, String pluginName) throws ParserConfigurationException, SAXException, IOException
    {
        // Create custom query holder object (keeps users from being able to use this class to create custom queries and directly passing them to the database).
        return GetEASTWebQuery(globalSchema, projectName, pluginName, new String[0]);
    }

    /**
     * Gets the list of a zonal summaries queried by the given EASTWebQuery object. Each EASTWebResult represents a row in the results table.
     *
     * @param query  - sql query wrapper object
     * @return the list of resulting summary row data composition objects
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static ArrayList<EASTWebResult> GetEASTWebResults(EASTWebQuery query) throws SQLException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException
    {
        Connection con = DatabaseConnector.getConnection();
        if(con == null) {
            return new ArrayList<EASTWebResult>();
        }
        Statement stmt = con.createStatement();
        ResultSet rs;
        ArrayList<Double> summaryCalculations;
        ArrayList<EASTWebResult> results = new ArrayList<EASTWebResult>(0);
        boolean valid;
        boolean foundColumn;

        // Run EASTWebQuery
        rs = stmt.executeQuery(query.GetSQL());
        if(rs != null)
        {
            while(rs.next())
            {
                summaryCalculations = new ArrayList<Double>(0);
                for(String summary : Config.getInstance().getSummaryCalculations())
                {
                    foundColumn = false;
                    for(int i=1; i <= rs.getMetaData().getColumnCount(); i++)
                    {
                        if(rs.getMetaData().getColumnName(i).equalsIgnoreCase(summary))
                        {
                            summaryCalculations.add(rs.getDouble(i));
                            foundColumn = true;
                            break;
                        }
                    }
                    if(!foundColumn) {
                        summaryCalculations.add(null);
                    }
                }

                results.add(new EASTWebResult(query.projectName, query.pluginName, rs.getString("IndexName"), rs.getInt("Year"), rs.getInt("DayOfYear"),
                        rs.getString("AreaNameField"), rs.getString("AreaName"), rs.getString("AreaCodeField"), rs.getInt("AreaCode"), rs.getInt("SummaryIDNum"),
                        rs.getString("ShapeFile"), rs.getString("TemporalSummaryCompositionStrategyClass"), Config.getInstance().getSummaryCalculations(), summaryCalculations,
                        rs.getString("FilePath")));
            }
            rs.close();
        }
        stmt.close();
        con.close();

        return results;
    }

    /**
     * Writes the given results array to a new csv file with the specified destination.
     *
     * @param filePath
     * @param results
     * @throws IOException
     */
    public static boolean WriteEASTWebResultsToCSV(String filePath, ArrayList<EASTWebResult> results) throws IOException
    {
        if(filePath == null) {
            return false;
        }

        if(filePath.endsWith("\\")) {
            filePath = filePath + System.currentTimeMillis() + ".csv";
        }

        if(!filePath.endsWith(".csv")) {
            filePath = filePath + ".csv";
        }

        File oFile = new File(filePath);
        if(oFile.getParent() != null) {
            new File(oFile.getParent()).mkdirs();
        }
        else {
            return false;
        }

        FileWriter writer = new FileWriter(oFile);

        writer.write("Project Name, Plugin Name, Index Name, Year, Day Of Year, Area Name, Area Code, Summary ID, Value Count");
        if(results.size() > 0)
        {
            for(String name : results.get(0).summaryNames)
            {
                writer.write(", " + name);
            }
        }
        writer.write("\n");

        for(EASTWebResult result : results)
        {
            writer.write(result.projectName + ", " + result.pluginName + ", " + result.indexName + ", " + result.year + ", " + result.day + ", " + result.areaName + ", " + result.areaCode + ", " +
                    result.summaryID);
            for(Double value : result.summaryCalculations)
            {
                if(value != null){
                    writer.write(", " + value);
                }
            }
            writer.write("\n");
        }

        writer.close();
        return true;
    }

    /**
     * Gets the list of a zonal summaries queried by the given EASTWebQuery object and returns a list of the files to each of the csv files written.
     *
     * @param query  - sql query wrapper object
     * @return the list of csv result files
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static ArrayList<File> GetResultCSVFiles(EASTWebQuery query) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        Connection con = DatabaseConnector.getConnection();
        if(con == null) {
            return new ArrayList<File>();
        }
        Statement stmt = con.createStatement();
        ResultSet rs;
        ArrayList<File> resultFiles = new ArrayList<File>(1);

        // Run EASTWebQuery
        rs = stmt.executeQuery(query.GetSQL());
        if(rs != null)
        {
            while(rs.next())
            {
                resultFiles.add(new File(rs.getString("FilePath")));
            }
            rs.close();
        }
        stmt.close();
        con.close();

        return resultFiles;
    }

    /**
     * Retrieves a list of zone names found from the results table for the given project and plugin.
     *
     * @param projectName
     * @param pluginName
     * @return  ArrayList of the distinct zone names found in the results table
     * @throws SQLException
     */
    public static String[] GetZonesListFromProject(String projectName, String pluginName) throws SQLException
    {
        ArrayList<String> zones = new ArrayList<String>();
        Connection con = DatabaseConnector.getConnection();
        if(con == null) {
            return null;
        }
        Statement stmt = con.createStatement();
        ResultSet rs;
        String schemaName = Schemas.getSchemaName(projectName, pluginName);

        rs = stmt.executeQuery("select \"ZonalStatID\" from \"" + schemaName + "\".\"ZonalStat\";");
        if(rs == null || !rs.next()) {
            return null;
        }
        rs.close();

        rs = stmt.executeQuery("select distinct \"AreaName\" from \"" + schemaName + "\".\"ZonalStat\";");
        if(rs != null)
        {
            while(rs.next()) {
                zones.add(rs.getString("AreaName"));
            }
            rs.close();
        }
        stmt.close();
        con.close();

        return zones.toArray(new String[zones.size()]);
    }

    private static EASTWebQuery getEASTWebQuery(String globalSchema, String projectName, String pluginName, boolean selectCount, boolean selectMax, boolean selectMin,
            boolean selectSum, boolean selectMean, boolean selectSqrSum, boolean selectStdDev, String zoneCondition, String yearSign, Integer yearVal, String daySign, Integer dayVal,
            String[] includedIndices, Integer[] summaryIDs)
    {
        final String mSchemaName = Schemas.getSchemaName(projectName, pluginName);
        globalSchema = FileSystem.StandardizeName(globalSchema);
        String yearCondition = "";
        String dayCondition = "";

        if(yearSign == null || yearVal == null) {
            yearCondition = "";
        }
        else {
            yearCondition = " AND D.\"Year\"" + yearSign + yearVal;
        }

        if(daySign == null || dayVal == null) {
            dayCondition = "";
        }
        else {
            dayCondition = " AND D.\"DayOfYear\"" + daySign + dayVal;
        }

        StringBuilder query = new StringBuilder("SELECT A.\"AreaName\", A.\"AreaCode\", P.\"AreaNameField\", P.\"AreaCodeField\", P.\"SummaryIDNum\", P.\"ShapeFile\", D.\"Year\", D.\"DayOfYear\", I.\"Name\" as \"IndexName\", " +
                "T.\"Name\" as \"TemporalSummaryCompositionStrategyClass\", A.\"FilePath\"");

        if(selectCount) {
            query.append(", A.\"Count\"");
        }
        if(selectMax) {
            query.append(", A.\"Max\"");
        }
        if(selectMin) {
            query.append(", A.\"Min\"");
        }
        if(selectSum) {
            query.append(", A.\"Sum\"");
        }
        if(selectMean) {
            query.append(", A.\"Mean\"");
        }
        if(selectSqrSum) {
            query.append(", A.\"SqrSum\"");
        }
        if(selectStdDev) {
            query.append(", A.\"StdDev\"");
        }

        query.append(String.format(
                " \nFROM \"%1$s\".\"ZonalStat\" A, \"%2$s\".\"DateGroup\" D, \"%2$s\".\"Index\" I, \"%2$s\".\"ProjectSummary\" P, \"%2$s\".\"TemporalSummaryCompositionStrategy\" T " +
                        "WHERE (A.\"DateGroupID\" = D.\"DateGroupID\"" + yearCondition + dayCondition + ")\n",
                        mSchemaName,
                        globalSchema)
                );

        if(includedIndices != null && includedIndices.length > 0)
        {
            query.append("AND (A.\"IndexID\"=I.\"IndexID\" AND (I.\"Name\"='" + includedIndices[0] + "'");
            for(int i=1; i < includedIndices.length; i++) {
                query.append(" OR I.\"Name\"='" + includedIndices[i] + "'");
            }
            query.append(")) ");
        }

        StringBuilder summariesCondition = new StringBuilder();
        if(summaryIDs.length > 0)
        {
            summariesCondition.append(" AND (P.\"SummaryIDNum\"=" + summaryIDs[0]);
            for(int i=1; i < summaryIDs.length; i++) {
                summariesCondition.append(" OR P.\"SummaryIDNum\"=" + summaryIDs[i] + "");
            }
            summariesCondition.append(")");
        }

        query.append("AND A.\"ProjectSummaryID\" = P.\"ProjectSummaryID\" AND P.\"TemporalSummaryCompositionStrategyID\" = T.\"TemporalSummaryCompositionStrategyID\"" +
                zoneCondition + summariesCondition.toString() + ";");

        return new EASTWebQuery(projectName, pluginName, query.toString());
    }

    private EASTWebResults()
    {
        // no need to instantiate
    }
}
