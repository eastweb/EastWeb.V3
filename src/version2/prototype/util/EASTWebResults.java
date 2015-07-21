/**
 *
 */
package version2.prototype.util;

import java.io.File;
import java.io.IOException;
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
    public enum Sign{
        LESS_THAN,
        GREATER_THAN,
        EQUAL_TO,
        NOT_EQUAL_TO,
        LESS_THAN_OR_EQUAL_TO,
        GREATER_THAN_OR_EQUAL_TO;

        @Override
        public String toString() {
            String name = null;

            switch(name())
            {
            case "LESS_THAN":
                name = "<";
                break;
            case "GREATER_THAN":
                name = ">";
                break;
            case "EQUAL_TO":
                name = "=";
                break;
            case "NOT_EQUAL_TO":
                name = "<>";
                break;
            case "LESS_THAN_OR_EQUAL_TO":
                name = "<=";
                break;
            case "GREATER_THAN_OR_EQUAL_TO":
                name = ">=";
                break;
            }
            return name;
        }
    };

    /**
     *
     * @param globalSchema
     * @param projectName
     * @param pluginName
     * @param selectCount
     * @param selectSum
     * @param selectMean
     * @param selectStdDev
     * @param zoneSign
     * @param zoneVal
     * @param yearSign
     * @param yearVal
     * @param daySign
     * @param dayVal
     * @param includedIndices
     * @param zoneNameField
     * @param shapefile
     * @return
     */
    public static EASTWebQuery GetEASTWebQuery(String globalSchema, String projectName, String pluginName, boolean selectCount, boolean selectSum, boolean selectMean,
            boolean selectStdDev, String zoneSign, int zoneVal, String yearSign, int yearVal, String daySign, int dayVal, ArrayList<String> includedIndices,
            String zoneNameField, String shapefile)
    {
        final String mSchemaName = Schemas.getSchemaName(projectName, pluginName);
        globalSchema = FileSystem.StandardizeName(globalSchema);

        StringBuilder query = new StringBuilder("SELECT A.\"FilePath\"");

        if(selectCount) {
            query.append(", A.\"Count\"");
        }
        if(selectSum) {
            query.append(", A.\"Sum\"");
        }
        if(selectMean) {
            query.append(", A.\"Mean\"");
        }
        if(selectStdDev) {
            query.append(", A.\"StdDev\"");
        }

        query.append(String.format(
                " FROM \"%1$s\".\"ZonalStat\" A, \"%2$s\".\"DateGroup\" D, \"%2$s\".\"DateGroup\", \"%2$s\".\"Index\" I, \"%1$s\".\"ZoneMapping\" M , \"%2$s\".\"ZoneVar\" Z, \"%1$s\".\"ZoneField\" F " +
                        "WHERE (A.\"DateGroupID\" = D.\"DateGroupID\" AND D.\"Year\"" + yearSign + yearVal + " AND D.\"Day\"" + daySign + dayVal + ") " +
                        mSchemaName,
                        globalSchema)
                );

        if(includedIndices.size() > 0)
        {
            query.append("(A.\"IndexID\"=I.\"IndexID\" AND (I.\"Name\"=" + includedIndices.get(0));
            for(int i=1; i < includedIndices.size(); i++)
            {
                query.append(" OR I.\"Name\"=" + includedIndices.get(i));
            }
            query.append(")) ");
        }

        query.append("(Z.\"Name\"=" + zoneNameField + " AND F.\"ShapeFile\"='" + shapefile + "' AND F.\"Field\"='" + zoneNameField + "') ");
        query.append("(A.\"ZoneMappingID\"=M.\"ZoneMappingID\" AND M.\"ZoneEWID\"=Z.\"ZoneEWID\" AND M.\"ZoneFieldID\"=F.\"ZoneFieldID\");");

        return new EASTWebQuery(query.toString());
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

        // Build query
        ArrayList<String> summaries = Config.getInstance().getSummaryCalculations();
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
        return new EASTWebQuery(query.toString());
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
                for(String summary : Config.getInstance().getSummaryCalculations())
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
                            rs.getInt("ExpectedTotalResults"), rs.getString("TemporalSummaryCompositionStrategyClass"), Config.getInstance().getSummaryCalculations(), summaryCalculations));
                }
            }
        }

        return results;
    }

    public static ArrayList<File> GetResultCSVFiles(EASTWebQuery query) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        Statement stmt = PostgreSQLConnection.getConnection().createStatement();
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
        }

        return resultFiles;
    }
}
