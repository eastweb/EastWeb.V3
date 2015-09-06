/**
 *
 */
package version2.prototype.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import version2.prototype.Config;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;

/**
 * @author michael.devos
 *
 */
public class ProgressUpdater {
    private final Config configInstance;
    private final ProjectInfoFile projectMetaData;
    private final PluginMetaDataCollection pluginMetaDataCollection;

    private TreeMap<String, TreeMap<String, Integer>> downloadExpectedFiles;
    private TreeMap<String, Integer> processorExpectedNumOfOutputs;
    private TreeMap<String, Integer> indicesExpectedNumOfOutputs;
    private TreeMap<String, TreeMap<Integer, Integer>> summaryExpectedNumOfOutputs;

    /**
     * Creates a ProgressUpdater object bound to the given Scheduler/project. This object will then handle calculating the progress values for each Process.
     *
     * @param configInstance  - the Config instance to use
     * @param projectMetaData  - project metadata
     * @param pluginMetaDataCollection  - reference to PluginMetaDataCollection instance being used by the referenced Scheduler
     */
    public ProgressUpdater(Config configInstance, ProjectInfoFile projectMetaData, PluginMetaDataCollection pluginMetaDataCollection)
    {
        this.configInstance = configInstance;
        this.projectMetaData = projectMetaData;
        this.pluginMetaDataCollection = pluginMetaDataCollection;

        downloadExpectedFiles = new TreeMap<String, TreeMap<String, Integer>>();
        TreeMap<String, Integer> downloadExpectedFilesTemp;
        processorExpectedNumOfOutputs = new TreeMap<String, Integer>();
        indicesExpectedNumOfOutputs = new TreeMap<String, Integer>();
        summaryExpectedNumOfOutputs = new TreeMap<String, TreeMap<Integer, Integer>>();
        TreeMap<Integer, Integer> summaryExpectedNumOfPluginOutputs;
        String pluginName;
        for(ProjectInfoPlugin plugin : projectMetaData.GetPlugins())
        {
            pluginName = plugin.GetName();

            // Setup Download progresses
            downloadExpectedFilesTemp = new TreeMap<String, Integer>();
            downloadExpectedFilesTemp.put("data", null);
            for(String dataName : pluginMetaDataCollection.pluginMetaDataMap.get(pluginName).ExtraDownloadFiles)
            {
                downloadExpectedFilesTemp.put(dataName.toLowerCase(), null);
            }
            downloadExpectedFiles.put(pluginName, downloadExpectedFilesTemp);

            // Setup Processor progresses
            processorExpectedNumOfOutputs.put(pluginName, null);

            // Setup Indices progresses
            indicesExpectedNumOfOutputs.put(pluginName, null);

            // Setup Summary progresses
            summaryExpectedNumOfPluginOutputs = new TreeMap<Integer, Integer>();
            for(ProjectInfoSummary summary : projectMetaData.GetSummaries())
            {
                summaryExpectedNumOfPluginOutputs.put(summary.GetID(), null);
            }
            summaryExpectedNumOfOutputs.put(pluginName, summaryExpectedNumOfPluginOutputs);
        }
    }

    /**
     * Calculates the current progress percentage for the Download process.
     *
     * @param dataName  - the name of the file type being downloaded (e.g. "Data" or "Qc")
     * @param pluginName  - the plugin title gotten from the plugin metadata to calculate progress in relation to
     * @param stmt  - Statement object to reuse
     * @return double  - progress percentage of the download process in relation to the local downloader downloading the files named by the given dataName and for the specified pluginName
     * @throws SQLException
     */
    public double GetCurrentDownloadProgress(String dataName, String pluginName, Statement stmt) throws SQLException
    {
        double progress = 0;
        String mSchemaName = Schemas.getSchemaName(projectMetaData.GetProjectName(), pluginName);
        int currentCount = calculateDownloadCurrentCount(mSchemaName, dataName, stmt);
        int expectedCount = getStoredDownloadExpectedTotalOutput(projectMetaData.GetProjectName(), pluginName, dataName, stmt);

        if(expectedCount > 0 && currentCount > 0)
        {
            progress = (new Double(currentCount) / new Double(expectedCount)) * 100;
        }

        return progress;
    }

    /**
     * Calculates the current progress percentage for the Processor process.
     *
     * @param pluginName  - the plugin title gotten from the plugin metadata to calculate progress in relation to
     * @param stmt  - Statement object to reuse
     * @return double  - progress percentage of the processor process for the specified pluginName
     * @throws SQLException
     */
    public double GetCurrentProcessorProgress(String pluginName, Statement stmt) throws SQLException
    {
        double progress = 0;
        PluginMetaData pluginMetaData = pluginMetaDataCollection.pluginMetaDataMap.get(pluginName);
        String mSchemaName = Schemas.getSchemaName(projectMetaData.GetProjectName(), pluginName);
        int currentCount = calculateProcessorCurrentCount(mSchemaName, stmt);
        int expectedCount = calculateProcessorExpectedCount(pluginMetaData, pluginName, stmt);

        if(expectedCount > 0 && currentCount > 0)
        {
            progress = (new Double(currentCount) / new Double(expectedCount)) * 100;
        }

        return progress;
    }

    /**
     * Calculates the current progress percentage for the Indices process.
     *
     * @param pluginName  - the plugin title gotten from the plugin metadata to calculate progress in relation to
     * @param stmt  - Statement object to reuse
     * @return double  - progress percentage of the processor process for the specified pluginName
     * @throws SQLException
     */
    public double GetCurrentIndicesProgress(String pluginName, Statement stmt) throws SQLException
    {
        double progress = 0;
        PluginMetaData pluginMetaData = pluginMetaDataCollection.pluginMetaDataMap.get(pluginName);
        String mSchemaName = Schemas.getSchemaName(projectMetaData.GetProjectName(), pluginName);
        int currentCount = calculateIndicesCurrentCount(mSchemaName, stmt);
        int expectedCount = calculateIndicesExpectedCount(pluginMetaData, pluginName, stmt);

        if(expectedCount > 0 && currentCount > 0)
        {
            progress = (new Double(currentCount) / new Double(expectedCount)) * 100;
        }
        return progress;
    }

    /**
     * Calculates the current progress percentage for the Summary process.
     *
     * @param summaryIDNum  - ID attribute value to calculate progress for gotten from project metadata
     * @param pluginInfo  - reference to a ProjectInfoPlugin object to use
     * @param stmt  - Statement object to reuse
     * @return double  - progress percentage of the summary process for the specified pluginInfo and project defined summary identified by summaryID
     * @throws SQLException
     */
    public double GetCurrentSummaryProgress(int summaryIDNum, ProjectInfoPlugin pluginInfo, Statement stmt) throws SQLException
    {
        double progress = 0;
        int projectSummaryID = Schemas.getProjectSummaryID(configInstance.getGlobalSchema(), projectMetaData.GetProjectName(), summaryIDNum, stmt);
        String mSchemaName = Schemas.getSchemaName(projectMetaData.GetProjectName(), pluginInfo.GetName());
        int currentCount = calculateSummaryCurrentCount(projectSummaryID, mSchemaName, stmt);
        int expectedCount = getStoredSummaryExpectedTotalOutput(projectMetaData.GetProjectName(), pluginInfo.GetName(), summaryIDNum, stmt);

        if(expectedCount > 0 && currentCount > 0)
        {
            progress = (new Double(currentCount) / new Double(expectedCount)) * 100;
        }

        return progress;
    }

    /**
     * Calculates and updates, if necessary, the currently database stored DownloadExpectedCount values.
     * @param dataName  - the name of the file type being downloaded (e.g. "Data" or "Qc")
     * @param pluginName  - the plugin title gotten from the plugin metadata to calculate progress in relation to
     * @param listDatesFiles  - reference to the ListDatesFiles object to use
     * @param modisTileNames  - list of modis tiles included
     * @param stmt  - Statement object to reuse
     * @throws SQLException
     */
    public void UpdateDBDownloadExpectedCount(String pluginName, String dataName, ListDatesFiles listDatesFiles, ArrayList<String> modisTileNames, Statement stmt) throws SQLException
    {
        int storedExpectedCount = getStoredDownloadExpectedTotalOutput(projectMetaData.GetProjectName(), pluginName, dataName, stmt);
        int calculatedExpectedCount = calculateDownloadExpectedCount(listDatesFiles, modisTileNames);
        if(storedExpectedCount != calculatedExpectedCount)
        {
            String updateQuery = "UPDATE \"" + configInstance.getGlobalSchema() + "\".\"DownloadExpectedTotalOutput\" SET \"ExpectedNumOfOutputs\" = " + calculatedExpectedCount + " WHERE " +
                    "\"ProjectID\" = " + Schemas.getProjectID(configInstance.getGlobalSchema(), projectMetaData.GetProjectName(), stmt) + " AND " +
                    "\"PluginID\" = " + Schemas.getPluginID(configInstance.getGlobalSchema(), pluginName, stmt) + " AND " +
                    "\"DataName\" = '" + dataName + "';";
            stmt.execute(updateQuery);
            downloadExpectedFiles.get(pluginName).put(dataName, calculatedExpectedCount);
        }
    }

    /**
     * Calculates and updates, if necessary, the currently database stored ProcessorExpectedCount value.
     * @param pluginName  - the plugin title gotten from the plugin metadata to calculate progress in relation to
     * @param stmt  - Statement object to reuse
     * @throws SQLException
     */
    public void UpdateDBProcessorExpectedCount(String pluginName, Statement stmt) throws SQLException
    {
        int storedExpectedCount = getStoredProcessorExpectedTotalOutput(projectMetaData.GetProjectName(), pluginName, stmt);
        int calculatedExpectedCount = calculateProcessorExpectedCount(pluginMetaDataCollection.pluginMetaDataMap.get(pluginName), pluginName, stmt);
        if(storedExpectedCount != calculatedExpectedCount)
        {
            String updateQuery = "UPDATE \"" + configInstance.getGlobalSchema() + "\".\"ProcessorExpectedTotalOutput\" SET \"ExpectedNumOfOutputs\" = " + calculatedExpectedCount + " WHERE " +
                    "\"ProjectID\" = " + Schemas.getProjectID(configInstance.getGlobalSchema(), projectMetaData.GetProjectName(), stmt) + " AND " +
                    "\"PluginID\" = " + Schemas.getPluginID(configInstance.getGlobalSchema(), pluginName, stmt) + ";";
            stmt.execute(updateQuery);
            processorExpectedNumOfOutputs.put(pluginName, calculatedExpectedCount);
        }
    }

    /**
     * Calculates and updates, if necessary, the currently database stored IndicesExpectedCount value.
     * @param pluginName  - the plugin title gotten from the plugin metadata to calculate progress in relation to
     * @param stmt  - Statement object to reuse
     * @throws SQLException
     */
    public void UpdateDBIndicesExpectedCount(String pluginName, Statement stmt) throws SQLException
    {
        int storedExpectedCount = getStoredIndicesExpectedTotalOutput(projectMetaData.GetProjectName(), pluginName, stmt);
        int calculatedExpectedCount = calculateIndicesExpectedCount(pluginMetaDataCollection.pluginMetaDataMap.get(pluginName), pluginName, stmt);
        if(storedExpectedCount != calculatedExpectedCount)
        {
            String updateQuery = "UPDATE \"" + configInstance.getGlobalSchema() + "\".\"IndicesExpectedTotalOutput\" SET \"ExpectedNumOfOutputs\" = " + calculatedExpectedCount + " WHERE " +
                    "\"ProjectID\" = " + Schemas.getProjectID(configInstance.getGlobalSchema(), projectMetaData.GetProjectName(), stmt) + " AND " +
                    "\"PluginID\" = " + Schemas.getPluginID(configInstance.getGlobalSchema(), pluginName, stmt) + ";";
            stmt.execute(updateQuery);
            indicesExpectedNumOfOutputs.put(pluginName, calculatedExpectedCount);
        }
    }

    /**
     * Calculates and updates, if necessary, the currently database stored SummaryExpectedCount value.
     * @param summaryIDNum  - ID attribute value to calculate progress for gotten from project metadata
     * @param compStrategy  - TemporalSummaryCompositionStrategy object to use in calculating total expecting results in temporal summary cases
     * @param daysPerInputData  - number of days each input file represents
     * @param pluginInfo  - reference to a ProjectInfoPlugin object to use
     * @param stmt  - Statement object to reuse
     * @throws SQLException
     */
    public void UpdateDBSummaryExpectedCount(int summaryIDNum, TemporalSummaryCompositionStrategy compStrategy, int daysPerInputData, ProjectInfoPlugin pluginInfo, Statement stmt) throws SQLException
    {
        int storedExpectedCount = getStoredSummaryExpectedTotalOutput(projectMetaData.GetProjectName(), pluginInfo.GetName(), summaryIDNum, stmt);
        int calculatedExpectedCount = calculateSummaryExpectedCount(Schemas.getSchemaName(projectMetaData.GetProjectName(), pluginInfo.GetName()), compStrategy, daysPerInputData, pluginInfo, stmt);
        if(storedExpectedCount != calculatedExpectedCount)
        {
            String updateQuery = "UPDATE \"" + configInstance.getGlobalSchema() + "\".\"SummaryExpectedTotalOutput\" SET \"ExpectedNumOfOutputs\" = " + calculatedExpectedCount + " WHERE " +
                    "\"ProjectSummaryID\" = " + Schemas.getProjectSummaryID(configInstance.getGlobalSchema(), projectMetaData.GetProjectName(), summaryIDNum, stmt) + " AND " +
                    "\"PluginID\" = " + Schemas.getPluginID(configInstance.getGlobalSchema(), pluginInfo.GetName(), stmt) + ";";
            stmt.execute(updateQuery);
            summaryExpectedNumOfOutputs.get(pluginInfo.GetName()).put(summaryIDNum, calculatedExpectedCount);
        }
    }

    protected int calculateDownloadCurrentCount(String mSchemaName, String dataName, Statement stmt) throws SQLException
    {
        int currentCount = 0;
        String progressQuery;
        String columnName;

        if(dataName.toLowerCase().equals("data"))
        {
            progressQuery = "SELECT Count(\"DownloadCacheID\") AS \"DownloadCacheIDCount\" FROM \"" + mSchemaName + "\".\"DownloadCache\";";
            columnName = "DownloadCacheIDCount";
        }
        else
        {
            progressQuery = "SELECT Count(\"DownloadCacheExtraID\") AS \"DownloadCacheExtraIDCount\" FROM \"" + mSchemaName + "\".\"DownloadCacheExtra\";";
            columnName = "DownloadCacheExtraIDCount";
        }

        ResultSet rs = stmt.executeQuery(progressQuery);
        if(rs != null && rs.next())
        {
            currentCount = rs.getInt(columnName);
            rs.close();
        }

        return currentCount;
    }

    protected int calculateDownloadExpectedCount(ListDatesFiles listDatesFiles, ArrayList<String> modisTileNames)
    {
        int expectedCount = 0;

        for(ArrayList<String> files : listDatesFiles.CloneListDatesFiles().values())
        {
            if(modisTileNames != null && modisTileNames.size() > 0)
            {
                Iterator<String> tileIt;
                for(String file : files)
                {
                    tileIt = modisTileNames.iterator();
                    while(tileIt.hasNext())
                    {
                        if(file.contains(tileIt.next())) {
                            expectedCount += 1;
                            break;
                        }
                    }
                }
            } else {
                expectedCount += files.size();
            }
        }

        return expectedCount;
    }

    protected int calculateProcessorCurrentCount(String mSchemaName, Statement stmt) throws SQLException
    {
        String progressQuery = "SELECT Count(\"ProcessorCacheID\") AS \"ProcessorCacheIDCount\" FROM \"" + mSchemaName + "\".\"ProcessorCache\";";
        int currentCount = 0;
        ResultSet rs = stmt.executeQuery(progressQuery);
        if(rs != null && rs.next())
        {
            currentCount = rs.getInt("ProcessorCacheIDCount");
            rs.close();
        }
        return currentCount;
    }

    protected int calculateProcessorExpectedCount(PluginMetaData pluginMetaData, String pluginName, Statement stmt) throws SQLException
    {
        if(downloadExpectedFiles.get(pluginName).get("data") == null)
        {
            downloadExpectedFiles.get(pluginName).put("data", getStoredDownloadExpectedTotalOutput(projectMetaData.GetProjectName(), pluginName, "data", stmt));
        }
        return pluginMetaData.Processor.numOfOutput * downloadExpectedFiles.get(pluginName).get("data");
    }

    protected int calculateIndicesCurrentCount(String mSchemaName, Statement stmt) throws SQLException
    {
        String progressQuery = "SELECT Count(\"IndicesCacheID\") AS \"IndicesCacheIDCount\" FROM \"" + mSchemaName + "\".\"IndicesCache\";";
        int currentCount = 0;
        ResultSet rs = stmt.executeQuery(progressQuery);
        if(rs != null && rs.next())
        {
            currentCount = rs.getInt("IndicesCacheIDCount");
            rs.close();
        }
        return currentCount;
    }

    protected int calculateIndicesExpectedCount(PluginMetaData pluginMetaData, String pluginName, Statement stmt) throws SQLException
    {
        if(processorExpectedNumOfOutputs.get(pluginName) == null)
        {
            processorExpectedNumOfOutputs.put(pluginName, getStoredProcessorExpectedTotalOutput(projectMetaData.GetProjectName(), pluginName, stmt));
        }
        return pluginMetaData.Indices.indicesNames.size() * processorExpectedNumOfOutputs.get(pluginName);
    }

    protected int calculateSummaryCurrentCount(int projectSummaryID, String mSchemaName, Statement stmt) throws SQLException
    {
        int currentCount = 0;

        String progressQuery = "SELECT Count(DISTINCT \"DateGroupID\") AS \"DateGroupIDCount\" FROM \"" + mSchemaName + "\".\"ZonalStat\" "
                + "WHERE \"ProjectSummaryID\"=" + projectSummaryID + " GROUP BY \"ProjectSummaryID\";";
        ResultSet rs = stmt.executeQuery(progressQuery);
        if(rs != null && rs.next())
        {
            currentCount = rs.getInt("DateGroupIDCount");
            rs.close();
        }

        return currentCount;
    }

    protected int calculateSummaryExpectedCount(String mSchemaName, TemporalSummaryCompositionStrategy compStrategy, int daysPerInputData, ProjectInfoPlugin pluginInfo, Statement stmt) throws SQLException
    {
        int expectedCount = 0;

        if(compStrategy != null)
        {
            ResultSet rs = stmt.executeQuery("SELECT Count(DISTINCT A.\"DateGroupID\") AS \"DateGroupIDCount\", Max(D.\"Year\") AS \"MaxYear\", Max(D.\"DayOfYear\") AS \"MaxDay\", Min(D.\"Year\") AS \"MinYear\", " +
                    "Min(D.\"DayOfYear\") AS \"MinDay\" FROM \"" + mSchemaName + "\".\"IndicesCache\" A INNER JOIN \"" + configInstance.getGlobalSchema() + "\".\"DateGroup\" D ON A.\"DateGroupID\"=D.\"DateGroupID\";");
            if(rs != null && rs.next()) {
                LocalDate startDate = LocalDate.ofYearDay(rs.getInt("MinYear"), rs.getInt("MinDay"));
                LocalDate endDate = LocalDate.ofYearDay(rs.getInt("MaxYear"), rs.getInt("MaxDay"));
                long completeCompositesInRange = compStrategy.getNumberOfCompleteCompositesInRange(startDate, endDate);
                expectedCount = (int) (completeCompositesInRange * pluginInfo.GetIndices().size());
                rs.close();
            }
        }
        else {
            if(indicesExpectedNumOfOutputs.get(pluginInfo.GetName()) == null)
            {
                indicesExpectedNumOfOutputs.put(pluginInfo.GetName(), getStoredIndicesExpectedTotalOutput(projectMetaData.GetProjectName(), pluginInfo.GetName(), stmt));
            }
            expectedCount = indicesExpectedNumOfOutputs.get(pluginInfo.GetName());
        }

        return expectedCount;
    }

    protected int getStoredDownloadExpectedTotalOutput(String projectName, String pluginName, String dataName, Statement stmt) throws SQLException
    {
        int expectedCount = 0;
        int projectID = Schemas.getProjectID(configInstance.getGlobalSchema(), projectName, stmt);
        int pluginID = Schemas.getPluginID(configInstance.getGlobalSchema(), pluginName, stmt);

        String selectQuery = "SELECT \"ExpectedNumOfOutputs\" FROM \"" + configInstance.getGlobalSchema() + "\".\"DownloadExpectedTotalOutput\" WHERE " +
                "\"ProjectID\" = " + projectID + " AND " +
                "\"PluginID\" = " + pluginID + " AND " +
                "\"DataName\" = '" + dataName + "';";
        String insertQuery = "INSERT INTO \"" + configInstance.getGlobalSchema() + "\".\"DownloadExpectedTotalOutput\" (\"ExpectedNumOfOutputs\", \"ProjectID\", \"PluginID\", \"DataName\") VALUES " +
                "(0, " + projectID + ", " + pluginID + ", '" + dataName + "');";
        expectedCount = getOrInsertIfMissingValue(selectQuery, "ExpectedNumOfOutputs", insertQuery, stmt);

        return expectedCount;
    }

    protected int getStoredProcessorExpectedTotalOutput(String projectName, String pluginName, Statement stmt) throws SQLException
    {
        int expectedCount = 0;
        int projectID = Schemas.getProjectID(configInstance.getGlobalSchema(), projectName, stmt);
        int pluginID = Schemas.getPluginID(configInstance.getGlobalSchema(), pluginName, stmt);

        String selectQuery = "SELECT \"ExpectedNumOfOutputs\" FROM \"" + configInstance.getGlobalSchema() + "\".\"ProcessorExpectedTotalOutput\" WHERE " +
                "\"ProjectID\" = " + projectID + " AND " +
                "\"PluginID\" = " + pluginID + ";";
        String insertQuery = "INSERT INTO \"" + configInstance.getGlobalSchema() + "\".\"ProcessorExpectedTotalOutput\" (\"ExpectedNumOfOutputs\", \"ProjectID\", \"PluginID\") VALUES " +
                "(0, " + projectID + ", " + pluginID + ");";
        expectedCount = getOrInsertIfMissingValue(selectQuery, "ExpectedNumOfOutputs", insertQuery, stmt);

        return expectedCount;
    }

    protected int getStoredIndicesExpectedTotalOutput(String projectName, String pluginName, Statement stmt) throws SQLException
    {
        int expectedCount = 0;
        int projectID = Schemas.getProjectID(configInstance.getGlobalSchema(), projectName, stmt);
        int pluginID = Schemas.getPluginID(configInstance.getGlobalSchema(), pluginName, stmt);

        String selectQuery = "SELECT \"ExpectedNumOfOutputs\" FROM \"" + configInstance.getGlobalSchema() + "\".\"IndicesExpectedTotalOutput\" WHERE " +
                "\"ProjectID\" = " + projectID + " AND " +
                "\"PluginID\" = " + pluginID + ";";
        String insertQuery = "INSERT INTO \"" + configInstance.getGlobalSchema() + "\".\"IndicesExpectedTotalOutput\" (\"ExpectedNumOfOutputs\", \"ProjectID\", \"PluginID\") VALUES " +
                "(0, " + projectID + ", " + pluginID + ");";
        expectedCount = getOrInsertIfMissingValue(selectQuery, "ExpectedNumOfOutputs", insertQuery, stmt);

        return expectedCount;
    }

    protected int getStoredSummaryExpectedTotalOutput(String projectName, String pluginName, int summaryIDNum, Statement stmt) throws SQLException
    {
        int expectedCount = 0;
        int projectSummaryID = Schemas.getProjectSummaryID(configInstance.getGlobalSchema(), projectName, summaryIDNum, stmt);
        int pluginID = Schemas.getPluginID(configInstance.getGlobalSchema(), pluginName, stmt);

        String selectQuery = "SELECT \"ExpectedNumOfOutputs\" FROM \"" + configInstance.getGlobalSchema() + "\".\"SummaryExpectedTotalOutput\" WHERE " +
                "\"ProjectSummaryID\" = " + projectSummaryID + " AND " +
                "\"PluginID\" = " + Schemas.getPluginID(configInstance.getGlobalSchema(), pluginName, stmt) + ";";
        String insertQuery = "INSERT INTO \"" + configInstance.getGlobalSchema() + "\".\"SummaryExpectedTotalOutput\" (\"ExpectedNumOfOutputs\", \"ProjectSummaryID\", \"PluginID\") VALUES " +
                "(0, " + projectSummaryID + ", " + pluginID + ");";
        expectedCount = getOrInsertIfMissingValue(selectQuery, "ExpectedNumOfOutputs", insertQuery, stmt);

        return expectedCount;
    }

    protected int getOrInsertIfMissingValue(final String selectQuery, final String valueField, final String insertQuery, final Statement stmt) throws SQLException
    {
        int value = 0;

        ResultSet rs = stmt.executeQuery(selectQuery);
        if(rs != null && rs.next()) {
            value = rs.getInt(valueField);
            rs.close();
        }
        else {
            stmt.execute(insertQuery);
            rs = stmt.executeQuery(selectQuery);
            if(rs != null && rs.next()) {
                value = rs.getInt(valueField);
                rs.close();
            }
        }

        return value;
    }
}
