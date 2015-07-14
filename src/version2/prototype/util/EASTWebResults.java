/**
 *
 */
package version2.prototype.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;

/**
 * @author michael.devos
 *
 */
public class EASTWebResults {
    /**
     * Get the EASTWebQuery object that represents the SQL query to handle getting EASTWeb zonal summary results associated with the given project and plugin names and for the given list of indices.
     *
     * @param globalSchema  - the global schema name (can be gotten from Config.getInstance().getGlobalSchema()
     * @param projectName  - the project name to be queried
     * @param pluginName  - the plugin name to be queried
     * @param indices  - the environmental indices to get summary information for
     * @return The EASTWebQuery object that contains the query that can retrieve results from zonal summary associated with the given parameters.
     * @throws ConfigReadException
     */
    public static EASTWebQuery GetEASTWebQuery(String globalSchema, String projectName, String pluginName, String... indices) throws ConfigReadException
    {
        // Check parameters given
        if(projectName == null || pluginName == null) {
            return null;
        }

        // Build query
        ArrayList<String> summaries = Config.getInstance().SummaryCalculations();
        String schemaName = Schemas.getSchemaName(projectName, pluginName);
        StringBuilder query = new StringBuilder("SELECT F.\"Field\", F.\"ShapeFile\", Z.\"ZoneName\", C.\"Year\", C.\"Day\", I.\"IndexName\", T.\"ExpectedTotalResults\", " +
                "A.\"TemporalSummaryCompositionStrategyClass\", A." + summaries.get(0));
        for(int i=1; i < summaries.size(); i++)
        {
            query.append(", A." + summaries.get(i));
        }
        query.append(" FROM \"" + schemaName + "\".\"ZonalStat\" A, \"" + globalSchema + "\".\"ExpectedResults\" T, (SELECT \"Name\" FROM \"" + globalSchema + "\".\"Index\"");
        if(indices != null && indices.length > 0)
        {
            query.append(" WHERE (\"Name\" = '" + indices[0] + "'");
            for(int i=1; i < indices.length; i++)
            {
                query.append(" OR \"Name\" = '" + indices[i] + "'");
            }
            query.append(")");
        }
        query.append(") I, \"" + globalSchema + "\".\"DateGroup\" C, \"" + schemaName + "\".\"ZoneMapping\" M, \"" + schemaName + "\".\"ZoneField\" F, \"" + globalSchema + "\".\"Zone\" Z " +
                "WHERE A.\"IndexID\" = I.\"IndexID\" AND A.\"DateGroupID\" = C.\"DateGroupID\" AND A.\"ZoneMapping\" = M.\"ZoneMappingID\" AND M.\"ZoneID\" = Z.\"ZoneID\" AND " +
                "M.\"ZoneFieldID\" = F.\"ZoneFieldID\";");

        // Create custom query holder object (keeps users from being able to use this class to create custom queries and directly passing them to the database).
        return new EASTWebQuery(schemaName, query.toString());
    }

    /**
     * Get the EASTWebQuery object that represents the SQL query to handle getting EASTWeb zonal summary results associated with the given project and plugin names.
     *
     * @param globalSchema  - the global schema name (can be gotten from Config.getInstance().getGlobalSchema()
     * @param projectName  - the project name to be queried
     * @param pluginName  - the plugin name to be queried
     * @return The EASTWebQuery object that contains the query that can retrieve results from zonal summary associated with the given parameters.
     * @throws ConfigReadException
     */
    public static EASTWebQuery GetEASTWebQuery(String globalSchema, String projectName, String pluginName) throws ConfigReadException
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
     * @throws ConfigReadException
     */
    public static ArrayList<EASTWebResult> GetEASTWebResults(EASTWebQuery query) throws SQLException, ClassNotFoundException, ConfigReadException
    {
        Statement stmt = PostgreSQLConnection.getConnection().createStatement();
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
                valid = true;
                summaryCalculations = new ArrayList<Double>(0);
                for(String summary : Config.getInstance().SummaryCalculations())
                {
                    foundColumn = false;
                    for(int i=0; i < rs.getMetaData().getColumnCount(); i++)
                    {
                        if(rs.getMetaData().getColumnName(i).equals(summary))
                        {
                            summaryCalculations.add(rs.getDouble(i));
                            foundColumn = true;
                            break;
                        }
                    }
                    if(!foundColumn) {
                        valid = false;
                    }
                }
                if(valid) {
                    results.add(new EASTWebResult(rs.getString("IndexName"), rs.getInt("Year"), rs.getInt("Day"), rs.getString("Field"), rs.getString("ZoneName"), rs.getString("ShapeFile"),
                            rs.getInt("ExpectedTotalResults"), rs.getString("TemporalSummaryCompositionStrategyClass"), Config.getInstance().SummaryCalculations(), summaryCalculations));
                }
            }
        }

        return results;
    }
}
