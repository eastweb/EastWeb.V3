/**
 *
 */
package version2.prototype.Scheduler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.TaskState;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class SchedulerStatusContainer {
    /**
     * Scheduler instance assigned unique identifier.
     */
    public final int schedulerID;
    /**
     * Project name associated with the Scheduler.
     */
    public final String projectName;
    /**
     * ProjectInfoPlugin list originally parsed from project info metadata file the Scheduler was instantiated to run with.
     */
    public final ArrayList<ProjectInfoPlugin> pluginInfo;
    /**
     * ProjectInfoSummary list originally parsed from project info metadata file the Scheduler was instantiated to run with. Each have an ID attributed to them within the project metadata file.
     */
    public final ArrayList<ProjectInfoSummary> summaries;

    private final Config configInstance;
    private TreeMap<String, Double> downloadProgresses;
    private TreeMap<String, Double> processorProgresses;
    private TreeMap<String, Double> indicesProgresses;
    private TreeMap<String, TreeMap<Integer, Double>> summaryProgresses;
    private List<String> log;
    private TaskState state;
    private boolean projectUpToDate;
    private TreeMap<String, TreeMap<Integer, Boolean>> resultsUpToDate;
    private TreeMap<String, Integer> numOfFilesLoaded;
    private TreeMap<String, TreeMap<Integer, Integer>> numOfResultsPublished;
    private LocalDateTime lastModifiedTime;

    /**
     * Creates a SchedulerStatusContainer
     * @param configInstance
     * @param schedulerID
     * @param projectName
     * @param pluginInfo
     * @param summaries
     * @param log
     * @param state
     * @param downloadProgresses
     * @param processorProgresses
     * @param indicesProgresses
     * @param summaryProgresses
     * @param projectUpToDate
     * @param resultsUpToDate
     * @param numOfFilesLoaded
     * @param numOfResultsPublished
     * @param lastModifiedTime
     */
    public SchedulerStatusContainer(Config configInstance, int schedulerID, String projectName, ArrayList<ProjectInfoPlugin> pluginInfo, ArrayList<ProjectInfoSummary> summaries,
            List<String> log, TaskState state, TreeMap<String, Double> downloadProgresses, TreeMap<String, Double> processorProgresses, TreeMap<String, Double> indicesProgresses,
            TreeMap<String, TreeMap<Integer, Double>> summaryProgresses, boolean projectUpToDate, TreeMap<String, TreeMap<Integer, Boolean>> resultsUpToDate,
            TreeMap<String, Integer> numOfFilesLoaded, TreeMap<String, TreeMap<Integer, Integer>> numOfResultsPublished, LocalDateTime lastModifiedTime)
    {
        this.configInstance = configInstance;
        this.schedulerID = schedulerID;
        this.projectName = projectName;
        this.pluginInfo = pluginInfo;
        this.summaries = summaries;
        this.downloadProgresses = downloadProgresses;
        this.processorProgresses = processorProgresses;
        this.indicesProgresses = indicesProgresses;
        this.summaryProgresses = summaryProgresses;
        this.log = log;
        this.state = state;
        this.projectUpToDate = projectUpToDate;
        this.resultsUpToDate = resultsUpToDate;
        this.numOfFilesLoaded = numOfFilesLoaded;
        this.numOfResultsPublished = numOfResultsPublished;
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * Creates a default SchedulerStatusContainer with everything set to either the current time, 0, false, or the associated value given.
     * @param configInstance
     * @param schedulerID
     * @param projectName
     * @param pluginInfo
     * @param summaries
     * @param state
     */
    public SchedulerStatusContainer(Config configInstance, int schedulerID, String projectName, ArrayList<ProjectInfoPlugin> pluginInfo, ArrayList<ProjectInfoSummary> summaries, TaskState state)
    {
        this.configInstance = configInstance;
        this.schedulerID = schedulerID;
        this.projectName = projectName;
        this.pluginInfo = pluginInfo;
        this.summaries = summaries;
        this.state = state;
        log = Collections.synchronizedList(new ArrayList<String>(1));
        projectUpToDate = false;

        downloadProgresses = new TreeMap<String, Double>();
        processorProgresses = new TreeMap<String, Double>();
        indicesProgresses = new TreeMap<String, Double>();
        for(ProjectInfoPlugin info : pluginInfo)
        {
            downloadProgresses.put(info.GetName(), 0.0);
            processorProgresses.put(info.GetName(), 0.0);
            indicesProgresses.put(info.GetName(), 0.0);
        }

        summaryProgresses = new TreeMap<String, TreeMap<Integer, Double>>();
        resultsUpToDate = new TreeMap<String, TreeMap<Integer, Boolean>>();
        numOfFilesLoaded = new TreeMap<String, Integer>();
        numOfResultsPublished = new TreeMap<String, TreeMap<Integer, Integer>>();
        TreeMap<Integer, Double> summaryProgressesPluginResults;
        TreeMap<Integer, Boolean> resultsUpToDatePluginResults;
        TreeMap<Integer, Integer> numOfResultsPublishedPluginResults;
        for(ProjectInfoPlugin info : pluginInfo)
        {
            summaryProgressesPluginResults = new TreeMap<Integer, Double>();
            resultsUpToDatePluginResults = new TreeMap<Integer, Boolean>();
            numOfResultsPublishedPluginResults = new TreeMap<Integer, Integer>();
            for(ProjectInfoSummary summary : summaries)
            {
                summaryProgressesPluginResults.put(summary.GetID(), 0.0);
                resultsUpToDatePluginResults.put(summary.GetID(), false);
                numOfResultsPublishedPluginResults.put(summary.GetID(), 0);
            }
            summaryProgresses.put(info.GetName(), summaryProgressesPluginResults);
            resultsUpToDate.put(info.GetName(), resultsUpToDatePluginResults);
            numOfResultsPublished.put(info.GetName(), numOfResultsPublishedPluginResults);
            numOfFilesLoaded.put(info.GetName(), 0);
        }

        lastModifiedTime = LocalDateTime.now();
    }

    /**
     * Get last modified time of this container.
     * @return LocalDateTime object representing the last time something in this container was modified
     */
    public LocalDateTime GetLastModifiedTime() { return lastModifiedTime; }

    /**
     * Gets the stored state of the Scheduler.
     * @return current TaskState of Scheduler
     */
    public TaskState GetState() { return state; }

    /**
     * Updates the download progress for the given plugin.
     * @param progress  - percentage of completion to set the download progress to
     * @param pluginName  - name of plugin whos download progress to change
     */
    public synchronized void UpdateDownloadProgress(double progress, String pluginName)
    {
        downloadProgresses.put(pluginName, progress);
        updateLastModifiedTime();
    }

    /**
     * Updates the processor progress for the given plugin.
     * @param progress  - percentage of completion to set the processor progress to
     * @param pluginName  - name of plugin whos processor progress to change
     */
    public synchronized void UpdateProcessorProgress(double progress, String pluginName)
    {
        processorProgresses.put(pluginName, progress);
        updateLastModifiedTime();
    }

    /**
     * Updates the indices progress for the given plugin.
     * @param progress  - percentage of completion to set the indices progress to
     * @param pluginName  - name of plugin whos download progress to change
     */
    public synchronized void UpdateIndicesProgress(double progress, String pluginName)
    {
        indicesProgresses.put(pluginName, progress);
        updateLastModifiedTime();
    }

    /**
     * Updates the summary progress for the summary attributed to the given ID for the named plugin.
     * @param progress  - percentage of completion to set the summary progress to
     * @param pluginName  - name of plugin whos summary progress to change
     * @param summaryID  - summary ID whos progress to change
     */
    public synchronized void UpdateSummaryProgress(double progress, String pluginName, int summaryID)
    {
        summaryProgresses.get(pluginName).put(summaryID, progress);
        updateLastModifiedTime();
    }

    /**
     * Updates the stored state of the Scheduler.
     * @param state  - TaskState to assign to the SchedulerStatueContainer
     */
    public synchronized void UpdateSchedulerTaskState(TaskState state)
    {
        this.state = state;
        updateLastModifiedTime();
    }

    /**
     * Adds a string to the log.
     * @param logText  - the string to add to the log
     */
    public synchronized void AddToLog(String logText)
    {
        log.add(logText);
        updateLastModifiedTime();
    }

    /**
     * Updates the number of files loaded map object.
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void UpdateNumOfFilesLoaded() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        synchronized(numOfFilesLoaded)
        {
            Connection con = PostgreSQLConnection.getConnection(configInstance);
            Statement stmt = con.createStatement();
            String formatStringDownloadsLoaded = "SELECT Count(\"DownloadCacheID\") AS \"DownloadCacheIDCount\" FROM \"%s\".\"DownloadCache\";";
            ResultSet rs = null;
            String projectPluginSchema;

            numOfFilesLoaded = new TreeMap<String, Integer>();
            for(ProjectInfoPlugin info : pluginInfo)
            {
                projectPluginSchema = Schemas.getSchemaName(projectName, info.GetName());
                rs = stmt.executeQuery(String.format(formatStringDownloadsLoaded, projectPluginSchema));
                if(rs != null && rs.next()) {
                    numOfFilesLoaded.put(info.GetName(), rs.getInt("DownloadCacheIDCount"));
                }
                else {
                    numOfFilesLoaded.put(info.GetName(), 0);
                }
            }

            stmt.close();
            if(rs != null) {
                rs.close();
            }
            updateLastModifiedTime();
        }
    }

    /**
     * Updates the number of results published map object.
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void UpdateNumOfResultsPublished() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        synchronized(numOfResultsPublished)
        {
            Connection con = PostgreSQLConnection.getConnection(configInstance);
            Statement stmt = con.createStatement();
            ResultSet rs = null;
            String formatStringPublished = "SELECT A.\"ProjectSummaryID\", Count(A.\"ZonalStatID\") AS \"ZonalStatIDCount\", B.\"SummaryIDNum\" FROM \"%1$s\".\"ZonalStat\" A " +
                    "INNER JOIN \"%2$s\".\"ProjectSummary\" B ON A.\"ProjectSummaryID\" = B.\"ProjectSummaryID\" GROUP BY A.\"ProjectSummaryID\", B.\"SummaryIDNum\";";
            String projectPluginSchema;

            numOfResultsPublished = new TreeMap<String, TreeMap<Integer, Integer>>();
            TreeMap<Integer, Integer> pluginResults;
            for(ProjectInfoPlugin info : pluginInfo)
            {
                pluginResults = new TreeMap<Integer, Integer>();

                projectPluginSchema = Schemas.getSchemaName(projectName, info.GetName());
                rs = stmt.executeQuery(String.format(formatStringPublished,
                        projectPluginSchema,
                        configInstance.getGlobalSchema()));
                if(rs != null)
                {
                    while(rs.next()) {
                        pluginResults.put(rs.getInt("SummaryIDNum"), rs.getInt("ZonalStatIDCount"));
                    }
                    if(pluginResults.keySet().size() != summaries.size())
                    {
                        Set<Integer> keys = pluginResults.keySet();

                        for(ProjectInfoSummary summary : summaries)
                        {
                            if(!keys.contains(summary.GetID())) {
                                pluginResults.put(summary.GetID(), 0);
                            }
                        }
                    }
                }
                else {
                    for(ProjectInfoSummary summary : summaries)
                    {
                        pluginResults.put(summary.GetID(), 0);
                    }
                }
                rs.close();

                numOfResultsPublished.put(info.GetName(), pluginResults);
            }

            stmt.close();
            if(rs != null) {
                rs.close();
            }
            updateLastModifiedTime();
        }
    }

    /**
     * Checks if the whole project is up to date or if any summary for any of the plugins being processed still has work to accomplish with the current information in this container.
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public synchronized void CheckIfProjectIsUpToDate() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        CheckIfProjectIsUpToDate(false, false);
    }

    /**
     * Updates the status of resultsUpToDate by checking if the results of each summary for each plugin are up to date or still processing data.
     * @param updateNumOfResultsPublished  - whether or not to update the number of results published
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void CheckIfResultsUpToDate(boolean updateNumOfResultsPublished) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        synchronized(resultsUpToDate)
        {
            Connection con = PostgreSQLConnection.getConnection(configInstance);
            Statement stmt = con.createStatement();
            PreparedStatement pStmtExpected = con.prepareStatement("SELECT \"ExpectedTotalResults\" FROM \"" + configInstance.getGlobalSchema() + "\".\"ExpectedResults\" WHERE " +
                    "\"ProjectSummaryID\" = ? AND \"PluginID\" = ?;");
            ResultSet rs;
            int expectedNumOfResults;
            int currentNumOfResults;

            if(updateNumOfResultsPublished) {
                UpdateNumOfResultsPublished();
            }

            resultsUpToDate = new TreeMap<String, TreeMap<Integer, Boolean>>();
            TreeMap<Integer, Boolean> pluginResults;
            for(ProjectInfoPlugin info : pluginInfo)
            {
                pluginResults = new TreeMap<Integer, Boolean>();
                for(ProjectInfoSummary summary : summaries)
                {
                    // Get ExpectedTotalResults
                    expectedNumOfResults = -1;
                    currentNumOfResults = -1;
                    pStmtExpected.setInt(1, Schemas.getProjectSummaryID(configInstance.getGlobalSchema(), projectName, summary.GetID(), stmt));
                    pStmtExpected.setInt(2, Schemas.getPluginID(configInstance.getGlobalSchema(), info.GetName(), stmt));
                    rs = pStmtExpected.executeQuery();
                    if(rs != null && rs.next()) {
                        expectedNumOfResults = rs.getInt("ExpectedTotalResults");
                    }
                    rs.close();

                    // Get actual total number of results
                    currentNumOfResults = numOfResultsPublished.get(info.GetName()).get(summary.GetID());

                    // Update plugin results
                    pluginResults.put(summary.GetID(), (((currentNumOfResults != -1) && (expectedNumOfResults != -1) && (currentNumOfResults == expectedNumOfResults)) ? true : false));
                }
                resultsUpToDate.put(info.GetName(), pluginResults);
            }

            stmt.close();
            pStmtExpected.close();
            updateLastModifiedTime();
        }
    }

    /**
     * Checks if the whole project is up to date or if any summary for any of the plugins being processed still has work to accomplish.
     * @param updateResultsUpToDateListing  - whether or not to check if the results are up to date
     * @param updateNumOfResultsPublished  - whether or not to update the number of results published
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public synchronized void CheckIfProjectIsUpToDate(boolean updateResultsUpToDateListing, boolean updateNumOfResultsPublished) throws ClassNotFoundException, SQLException,
    ParserConfigurationException, SAXException, IOException
    {
        boolean isUpToDate = true;
        if(updateResultsUpToDateListing) {
            CheckIfResultsUpToDate(updateNumOfResultsPublished);
        }

        TreeMap<Integer, Boolean> pluginResults;
        Iterator<String> pluginsIt = resultsUpToDate.keySet().iterator();
        Iterator<Integer> summaryIt;
        while(isUpToDate && pluginsIt.hasNext())
        {
            pluginResults = resultsUpToDate.get(pluginsIt.next());
            summaryIt = pluginResults.keySet().iterator();
            while(isUpToDate && summaryIt.hasNext())
            {
                if(!pluginResults.get(summaryIt.next()))
                {
                    isUpToDate = false;
                }
            }
        }
        projectUpToDate = isUpToDate;
        updateLastModifiedTime();
    }

    /**
     * Gets the current state of this SchedulerStatusContainer as a SchedulerStatus object which erases the log as it instantiates the returned SchedulerStatus object.
     * @return SchedulerStatus representation of this object
     */
    public SchedulerStatus GetStatus()
    {
        TreeMap<String, TreeMap<Integer, Double>> summaryProgressesTemp = cloneTreeMapStringIntegerDouble(summaryProgresses);
        TreeMap<String, TreeMap<Integer, Boolean>> resultsUpToDateTemp = cloneTreeMapStringIntegerBoolean(resultsUpToDate);
        TreeMap<String, TreeMap<Integer, Integer>> numOfResultsPublishedTemp = cloneTreeMapStringIntegerInteger(numOfResultsPublished);
        ArrayList<String> newLog = new ArrayList<String>(log);
        log = new ArrayList<String>();

        return new SchedulerStatus(schedulerID, projectName, pluginInfo, summaries, downloadProgresses, processorProgresses, indicesProgresses, summaryProgressesTemp, newLog, state,
                resultsUpToDateTemp, numOfFilesLoaded, numOfResultsPublishedTemp, projectUpToDate, lastModifiedTime, LocalDateTime.now());
    }

    private TreeMap<String, TreeMap<Integer, Boolean>> cloneTreeMapStringIntegerBoolean(TreeMap<String, TreeMap<Integer, Boolean>> input)
    {
        TreeMap<String, TreeMap<Integer, Boolean>> clone = new TreeMap<String, TreeMap<Integer, Boolean>>();
        Iterator<String> pluginsIt = input.keySet().iterator();
        Iterator<Integer> summaryProgressesIt;
        TreeMap<Integer, Boolean> pluginResults;
        String plugin;
        int summaryID;
        while(pluginsIt.hasNext())
        {
            plugin = pluginsIt.next();
            summaryProgressesIt = input.get(plugin).keySet().iterator();
            pluginResults = new TreeMap<Integer, Boolean>();
            while(summaryProgressesIt.hasNext())
            {
                summaryID = summaryProgressesIt.next();
                pluginResults.put(summaryID, input.get(plugin).get(summaryID));
            }
            clone.put(plugin, pluginResults);
        }
        return clone;
    }

    private TreeMap<String, TreeMap<Integer, Integer>> cloneTreeMapStringIntegerInteger(TreeMap<String, TreeMap<Integer, Integer>> input)
    {
        TreeMap<String, TreeMap<Integer, Integer>> clone = new TreeMap<String, TreeMap<Integer, Integer>>();
        Iterator<String> pluginsIt = input.keySet().iterator();
        Iterator<Integer> summaryProgressesIt;
        TreeMap<Integer, Integer> pluginResults;
        String plugin;
        int summaryID;
        while(pluginsIt.hasNext())
        {
            plugin = pluginsIt.next();
            summaryProgressesIt = input.get(plugin).keySet().iterator();
            pluginResults = new TreeMap<Integer, Integer>();
            while(summaryProgressesIt.hasNext())
            {
                summaryID = summaryProgressesIt.next();
                pluginResults.put(summaryID, input.get(plugin).get(summaryID));
            }
            clone.put(plugin, pluginResults);
        }
        return clone;
    }

    private TreeMap<String, TreeMap<Integer, Double>> cloneTreeMapStringIntegerDouble(TreeMap<String, TreeMap<Integer, Double>> input)
    {
        TreeMap<String, TreeMap<Integer, Double>> clone = new TreeMap<String, TreeMap<Integer, Double>>();
        Iterator<String> pluginsIt = input.keySet().iterator();
        Iterator<Integer> summaryProgressesIt;
        TreeMap<Integer, Double> pluginResults;
        String plugin;
        int summaryID;
        while(pluginsIt.hasNext())
        {
            plugin = pluginsIt.next();
            summaryProgressesIt = input.get(plugin).keySet().iterator();
            pluginResults = new TreeMap<Integer, Double>();
            while(summaryProgressesIt.hasNext())
            {
                summaryID = summaryProgressesIt.next();
                pluginResults.put(summaryID, input.get(plugin).get(summaryID));
            }
            clone.put(plugin, pluginResults);
        }
        return clone;
    }

    private synchronized void updateLastModifiedTime() {
        lastModifiedTime = LocalDateTime.now();
    }
}
