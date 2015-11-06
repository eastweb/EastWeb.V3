package version2.prototype.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.ConfigReadException;
import version2.prototype.ErrorLog;
import version2.prototype.Process;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.download.ListDatesFiles;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.zonal.SummaryResult;

/**
 * Database cache system interface. Frameworks use this to get and submit files from and to the database cache. Internally tracks and logs acquired and available
 * files for each of the four frameworks and the global downloaders.
 *
 * @author michael.devos
 *
 */
public class DatabaseCache extends Observable{
    static final Pattern filePathPattern = Pattern.compile("(\\w+)\\\\(\\w+)\\\\(\\w+)\\\\(\\d{4})\\\\(\\d{3})\\\\");   // To save time
    static final Pattern dateStringPattern = Pattern.compile("(\\d{4})\\\\(\\d{3})\\\\");   // To save time
    static final Pattern modisPattern = Pattern.compile(".*modis.*");

    public final String globalSchema;
    public final String mSchemaName;
    public final String projectName;
    public final ProjectInfoPlugin pluginInfo;
    public final PluginMetaData pluginMetaData;
    public final String pluginName;
    public final String workingDir;
    public final String getFromTableName;
    public final String cacheToTableName;
    public final String setProcessedForTableName;
    public final ProcessName processCachingFor;
    public final ArrayList<String> extraDownloadFiles;

    private final Scheduler scheduler;
    private Boolean filesAvailable;

    /**
     * Creates a DatabaseCache object set to cache files to and get file listings from the table identified by the given information.
     *
     * @param scheduler
     * @param globalSchema
     * @param projectName  - project schema to look under
     * @param pluginInfo
     * @param pluginMetaData  - plugin metadata to use
     * @param workingDir  - the working directory gotten from ProjectInfoFile
     * @param processCachingFor  - name of process to cache output for
     * @throws ParseException
     */
    public DatabaseCache(Scheduler scheduler, String globalSchema, String projectName, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData, String workingDir, ProcessName processCachingFor)
            throws ParseException
    {
        this.scheduler = scheduler;
        this.globalSchema = globalSchema;
        pluginName = pluginInfo.GetName();
        mSchemaName = Schemas.getSchemaName(projectName, pluginName);
        this.projectName = projectName;
        this.pluginInfo = pluginInfo;
        this.pluginMetaData = pluginMetaData;
        this.workingDir = workingDir;
        this.processCachingFor = processCachingFor;
        if(pluginMetaData.ExtraDownloadFiles != null) {
            extraDownloadFiles = pluginMetaData.ExtraDownloadFiles;
        } else {
            extraDownloadFiles = new ArrayList<String>();
        }
        filesAvailable = false;

        // Setup so that a single DatabaseCache object is intended to be used for output by one process and then used by another for input
        switch(this.processCachingFor)
        {
        case DOWNLOAD: cacheToTableName = "DownloadCache"; getFromTableName = null; setProcessedForTableName = null; break;
        case PROCESSOR: cacheToTableName = "ProcessorCache"; getFromTableName = "ProcessorCache"; setProcessedForTableName = "DownloadCache"; break;
        case INDICES: cacheToTableName = "IndicesCache"; getFromTableName = "IndicesCache"; setProcessedForTableName = "ProcessorCache"; break;
        case SUMMARY: cacheToTableName = "ZonalStat"; getFromTableName = null; setProcessedForTableName = "IndicesCache"; break;
        default: throw new ParseException("ProcessName 'processCachingFor' doesn't contain an expected framework identifier.", 0);
        }
    }

    /**
     * Retrieves a set of files from the desired table that have yet to be retrieved by a ProcessWorker. This set will be at minimum the resulting files from computations done on all data files for
     * a single day or however large the download composite size is in days.
     *
     * @return a list of available files to process
     * @throws SQLException | ClassNotFoundException | IOException | SAXException | ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws ConfigReadException
     * @throws ClassNotFoundException
     */
    public ArrayList<DataFileMetaData> GetUnprocessedCacheFiles() throws SQLException, ClassNotFoundException, IOException, SAXException, ParserConfigurationException
    {
        Connection con = null;
        Statement stmt = null;
        ArrayList<Integer> rows = new ArrayList<Integer>();
        int dateGroupID, tempDayOfYear, tempYear;
        String tempDataName;
        ResultSet rs = null;
        TreeSet<Record> temp = new TreeSet<Record>();


        try {
            con = DatabaseConnector.getConnection();
            stmt = con.createStatement();

            if(processCachingFor == ProcessName.DOWNLOAD)
            {
                synchronized(filesAvailable)
                {
                    stmt.execute("BEGIN");

                    // Collect completed but not retrieved records from DownloadCache
                    String downloadCacheQuery = "SELECT D.*, G.\"Year\", G.\"DayOfYear\", G.\"DateGroupID\" FROM \"" + mSchemaName + "\".\"DownloadCache\" D " +
                            "INNER JOIN \"" + globalSchema + "\".\"DateGroup\" G ON D.\"DateGroupID\" = G.\"DateGroupID\"" +
                            "WHERE \"Complete\" = TRUE AND \"Retrieved\" = FALSE AND \"Processed\" = FALSE FOR UPDATE;";
                    rs = stmt.executeQuery(downloadCacheQuery);
                    if(rs != null)
                    {
                        while(rs.next()) {
                            tempDayOfYear = rs.getInt("DayOfYear");
                            tempYear = rs.getInt("Year");
                            dateGroupID = rs.getInt("DateGroupID");
                            if(!temp.add(new Record(dateGroupID, "Data", new DataFileMetaData("Data", rs.getString("DataFilePath"), dateGroupID, tempYear, tempDayOfYear)))) {
                                ErrorLog.add(processCachingFor, scheduler, "Problem adding cached file to unprocessed cached file return list.", new Exception("Element could not be added."));
                            }
                            rows.add(rs.getInt("DownloadCacheID"));
                        }
                        rs.close();
                    }

                    for(int row : rows)
                    {
                        stmt.execute(String.format(
                                "UPDATE \"%1$s\".\"%2$s\"\n" +
                                        "SET \"Retrieved\" = TRUE\n" +
                                        "WHERE \"%2$sID\" = %3$d",
                                        mSchemaName,
                                        "DownloadCache",
                                        row
                                ));
                    }
                    rows = new ArrayList<Integer>();

                    // If necessary, collect completed but not retrieved records from DownloadCacheExtra
                    String downloadCacheExtraQuery = "";
                    if(extraDownloadFiles != null && extraDownloadFiles.size() > 0)
                    {
                        downloadCacheExtraQuery = "SELECT D.*, G.\"Year\", G.\"DayOfYear\", G.\"DateGroupID\" FROM \"" + mSchemaName + "\".\"DownloadCacheExtra\" D " +
                                "INNER JOIN \"" + globalSchema + "\".\"DateGroup\" G ON D.\"DateGroupID\" = G.\"DateGroupID\"" +
                                "WHERE \"Complete\" = TRUE AND \"Retrieved\" = FALSE AND \"Processed\" = FALSE FOR UPDATE;";
                        rs = stmt.executeQuery(downloadCacheExtraQuery);
                        if(rs != null)
                        {
                            while(rs.next()) {
                                tempDayOfYear = rs.getInt("DayOfYear");
                                tempYear = rs.getInt("Year");
                                dateGroupID = rs.getInt("DateGroupID");
                                tempDataName = rs.getString("DataName");
                                if(!temp.add(new Record(dateGroupID, tempDataName, new DataFileMetaData(tempDataName, rs.getString("FilePath"), dateGroupID, tempYear, tempDayOfYear)))) {
                                    ErrorLog.add(processCachingFor, scheduler, "Problem adding cached file to unprocessed cached file return list.", new Exception("Element could not be added."));
                                }
                                rows.add(rs.getInt("DownloadCacheExtraID"));
                            }
                            rs.close();
                        }

                        for(int row : rows)
                        {
                            stmt.execute(String.format(
                                    "UPDATE \"%1$s\".\"%2$s\"\n" +
                                            "SET \"Retrieved\" = TRUE\n" +
                                            "WHERE \"%2$sID\" = %3$d",
                                            mSchemaName,
                                            "DownloadCacheExtra",
                                            row
                                    ));
                        }
                    }
                    stmt.execute("COMMIT");
                    filesAvailable = false;
                }
            }
            else
            {
                synchronized(filesAvailable)
                {
                    stmt.execute("BEGIN");

                    String indexSelectString;
                    String indexJoinString;
                    if(processCachingFor == ProcessName.INDICES) {
                        indexSelectString = ", I.\"Name\" AS \"IndexName\"";
                        indexJoinString = " INNER JOIN \"%3$s\".\"Index\" I ON (A.\"IndexID\" = I.\"IndexID\")";
                    } else {
                        indexSelectString = "";
                        indexJoinString = "";
                    }

                    String query = String.format(
                            "SELECT A.\"%1$sID\", A.\"DataFilePath\", A.\"DateGroupID\", D.\"Year\", D.\"DayOfYear\"" + indexSelectString + " \n" +
                                    "FROM \"%2$s\".\"%1$s\" A INNER JOIN \"%3$s\".\"DateGroup\" D ON (A.\"DateGroupID\" = D.\"DateGroupID\")" + indexJoinString + " \n" +
                                    "WHERE \"Retrieved\" = FALSE AND \"Processed\" = FALSE FOR UPDATE;",
                                    getFromTableName,
                                    mSchemaName,
                                    globalSchema
                            );
                    rs = stmt.executeQuery(query);

                    while(rs.next()) {
                        tempDayOfYear = rs.getInt("DayOfYear");
                        tempYear = rs.getInt("Year");
                        dateGroupID = rs.getInt("DateGroupID");

                        if(processCachingFor == ProcessName.INDICES) {
                            if(!temp.add(new Record(dateGroupID, "Data", new DataFileMetaData(rs.getString("DataFilePath"), dateGroupID, tempYear, tempDayOfYear, rs.getString("IndexName"))))) {
                                ErrorLog.add(processCachingFor, scheduler, "Problem adding cached file to unprocessed cached file return list.", new Exception("Element could not be added."));
                            }
                        } else {
                            if(!temp.add(new Record(dateGroupID, "Data", new DataFileMetaData("Data", rs.getString("DataFilePath"), dateGroupID, tempYear, tempDayOfYear)))) {
                                ErrorLog.add(processCachingFor, scheduler, "Problem adding cached file to unprocessed cached file return list.", new Exception("Element could not be added."));
                            }
                        }
                        rows.add(rs.getInt(getFromTableName + "ID"));
                    }
                    rs.close();

                    for(int row : rows)
                    {
                        stmt.execute(String.format(
                                "UPDATE \"%1$s\".\"%2$s\"\n" +
                                        "SET \"Retrieved\" = TRUE\n" +
                                        "WHERE \"%2$sID\" = %3$d",
                                        mSchemaName,
                                        getFromTableName,
                                        row
                                ));
                    }

                    if(processCachingFor == ProcessName.PROCESSOR)
                    {
                        rows = new ArrayList<Integer>();
                        query = String.format(
                                "SELECT A.\"DownloadCacheExtraID\", A.\"DataName\", A.\"FilePath\", A.\"DateGroupID\", D.\"Year\", D.\"DayOfYear\" \n" +
                                        "FROM \"%1$s\".\"DownloadCacheExtra\" A INNER JOIN \"%2$s\".\"DateGroup\" D ON (A.\"DateGroupID\" = D.\"DateGroupID\") \n" +
                                        "WHERE \"Retrieved\" = FALSE AND \"Processed\" = FALSE FOR UPDATE;",
                                        mSchemaName,
                                        globalSchema
                                );
                        rs = stmt.executeQuery(query);

                        while(rs.next()) {
                            tempDayOfYear = rs.getInt("DayOfYear");
                            tempYear = rs.getInt("Year");
                            dateGroupID = rs.getInt("DateGroupID");
                            tempDataName = rs.getString("DataName");

                            if(!temp.add(new Record(dateGroupID, tempDataName, new DataFileMetaData(tempDataName, rs.getString("DataFilePath"), dateGroupID, tempYear, tempDayOfYear)))) {
                                ErrorLog.add(processCachingFor, scheduler, "Problem adding cached file to unprocessed cached file return list.", new Exception("Element could not be added."));
                            }
                            rows.add(rs.getInt("DownloadCacheExtraID"));
                        }
                        rs.close();

                        for(int row : rows)
                        {
                            stmt.execute(String.format(
                                    "UPDATE \"%1$s\".\"%2$s\"\n" +
                                            "SET \"Retrieved\" = TRUE\n" +
                                            "WHERE \"%2$sID\" = %3$d",
                                            mSchemaName,
                                            "DownloadCacheExtra",
                                            row
                                    ));
                        }
                    }

                    stmt.execute("COMMIT");
                    filesAvailable = false;
                }
            }
            if(stmt != null) {
                stmt.close();
                stmt = null;
            }
            if(rs != null) {
                rs.close();
                rs = null;
            }
            if(con != null) {
                con.close();
                con = null;
            }
        } catch(SQLException e) {
            con.createStatement().execute("ROLLBACK");
            if(stmt != null) {
                stmt.close();
            }
            if(rs != null) {
                rs.close();
            }
            if(con != null) {
                con.close();
            }
            throw e;
        }

        ArrayList<DataFileMetaData> output = new ArrayList<DataFileMetaData>();
        for(Record rec : temp)
        {
            output.add(rec.data);
        }
        return output;
    }

    /**
     * Used by LocalDownloaders to load newly completed sets of downloads from GlobalDownloader objects and update this cache for observers.
     *
     * @param globalEASTWebSchema  - the schema for the globally accessible EASTWeb schema
     * @param projectName  - name of the project to load downloads to
     * @param pluginName  - name of the plugin to load downloads to
     * @param dataName
     * @param startDate  - the start date to load downloads beginning from this time or later
     * @param extraDownloadFiles  - names of extra download files listed within the plugin metadata
     * @param modisTileNames  - modis tile names as listed in project metadata
     * @param listDatesFiles
     * @return number of records effected
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public int LoadUnprocessedGlobalDownloadsToLocalDownloader(String globalEASTWebSchema, String projectName, String pluginName, String dataName, LocalDate startDate, ArrayList<String> extraDownloadFiles,
            ArrayList<String> modisTileNames, ListDatesFiles listDatesFiles) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        int changes = 0;
        final Connection conn = DatabaseConnector.getConnection();
        final Statement stmt = conn.createStatement();
        final int gdlID = Schemas.getGlobalDownloaderID(globalEASTWebSchema, pluginName, dataName, stmt);
        StringBuilder insertQuery;
        StringBuilder selectQuery;

        System.out.println("Checking for unprocessed downloads for plugin '" + pluginName + "' in project '" + projectName + "'.");

        // Only use modisTileNames if the plugin uses Modis data
        //        if(!modisPattern.matcher(pluginName.toLowerCase()).matches())
        //        {
        //            modisTileNames = null;
        //        }

        // Set up for Download to DownloadCache insert
        if(dataName.toLowerCase().equals("data"))
        {
            insertQuery = new StringBuilder(String.format(
                    "INSERT INTO \"%1$s\".\"DownloadCache\" (\"DownloadID\", \"DataFilePath\", \"DateGroupID\") " +
                            "SELECT D.\"DownloadID\", D.\"DataFilePath\", D.\"DateGroupID\" " +
                            "FROM \"%2$s\".\"Download\" D " +
                            "INNER JOIN \"%2$s\".\"DateGroup\" G ON D.\"DateGroupID\" = G.\"DateGroupID\" " +
                            "LEFT JOIN \"%1$s\".\"DownloadCache\" C ON D.\"DownloadID\" = C.\"DownloadID\" " +
                            "WHERE D.\"GlobalDownloaderID\" = " + gdlID +
                            " AND ((G.\"Year\" = " + startDate.getYear() + " AND G.\"DayOfYear\" >= " + startDate.getDayOfYear() + ") OR (G.\"Year\" > " + startDate.getYear() + "))" +
                            " AND D.\"Complete\" = TRUE" +
                            " AND C.\"DownloadID\" IS NULL",
                            mSchemaName,
                            globalEASTWebSchema));
            if((modisTileNames != null) && (modisTileNames.size() > 0))
            {
                insertQuery.append(" AND (D.\"DataFilePath\" ilike '%" + modisTileNames.get(0) + "%'");
                for(int i=1; i < modisTileNames.size(); i++)
                {
                    insertQuery.append(" OR D.\"DataFilePath\" ilike '%" + modisTileNames.get(i) + "%'");
                }
                insertQuery.append(")");
            }
        }
        else{
            // Set up for DownloadExtra to DownloadCacheExtra insert
            insertQuery = new StringBuilder(String.format(
                    "INSERT INTO \"%1$s\".\"DownloadCacheExtra\" (\"DownloadExtraID\", \"DataName\", \"FilePath\", \"DateGroupID\") " +
                            "SELECT D.\"DownloadExtraID\", D.\"DataName\", D.\"FilePath\", D.\"DateGroupID\" " +
                            "FROM \"%2$s\".\"DownloadExtra\" D ",
                            mSchemaName,
                            globalEASTWebSchema
                    ));
            insertQuery.append(String.format("INNER JOIN \"%1$s\".\"DateGroup\" G ON D.\"DateGroupID\" = G.\"DateGroupID\" " +
                    "LEFT JOIN \"%2$s\".\"DownloadCacheExtra\" C ON D.\"DownloadExtraID\" = C.\"DownloadExtraID\" " +
                    "WHERE D.\"GlobalDownloaderID\" = " + gdlID +
                    " AND ((G.\"Year\" = " + startDate.getYear() + " AND G.\"DayOfYear\" >= " + startDate.getDayOfYear() + ") OR (G.\"Year\" > " + startDate.getYear() + "))" +
                    " AND D.\"Complete\" = TRUE" +
                    " AND C.\"DownloadExtraID\" IS NULL",
                    globalEASTWebSchema,
                    mSchemaName));
            if((modisTileNames != null) && (modisTileNames.size() > 0))
            {
                insertQuery.append(" AND (D.\"FilePath\" ilike '%" + modisTileNames.get(0) + "%'");
                for(int i=1; i < modisTileNames.size(); i++)
                {
                    insertQuery.append(" OR D.\"FilePath\" ilike '%" + modisTileNames.get(i) + "%'");
                }
                insertQuery.append(")");
            }

        }

        synchronized(filesAvailable)
        {
            // Execute Download to DownloadCache/DownloadCacheExtra insert and get number of rows effected
            changes = stmt.executeUpdate(insertQuery.toString());

            // Notify observers if changes were made and at least one day of cached data is awaiting processing.
            /* Steps:
             * 1) Get all DateGroupIDs from DateGroup table that are equal to or later than the startDate and add them to a ArrayList<Integer> object
             * 2) Use that list and, for each date, first check if any records exist for it in DownloadCache table and if they're are none then remove the date from the list. Then, IF and ONLY IF there are
             * extra download files to handle, for each date remaining in the list search the DownloadCacheExtra table for any existing records for it and if none exist then remove the date.
             * 3) Finally, for all dates that remain, set their 'Completed' fields in DownloadCache, and if necessary, in DownloadCacheExtra tables.
             */
            ArrayList<Integer> dates = new ArrayList<Integer>();
            if(changes > 0)
            {
                selectQuery = new StringBuilder("SELECT * FROM \"" + globalEASTWebSchema + "\".\"DateGroup\";");
                ResultSet rs = stmt.executeQuery(selectQuery.toString());
                if(rs != null)
                {
                    // Step 1
                    while(rs.next())
                    {
                        dates.add(rs.getInt("DateGroupID"));
                    }
                    rs.close();

                    if(dates.size() > 0)
                    {
                        // Step 2a
                        PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM \"" + mSchemaName + "\".\"DownloadCache\" WHERE \"Complete\" = FALSE AND \"DateGroupID\" = ?;");
                        int id;
                        Iterator<Integer> it = dates.iterator();
                        while(it.hasNext())
                        {
                            id = it.next();
                            pstmt.setInt(1, id);
                            rs = pstmt.executeQuery();
                            if(rs == null || !rs.next()) {
                                it.remove();
                            } else {
                                rs.close();
                            }
                        }

                        // Step 2b
                        if(extraDownloadFiles != null && extraDownloadFiles.size() > 0)
                        {
                            pstmt = conn.prepareStatement("SELECT * FROM \"" + mSchemaName + "\".\"DownloadCacheExtra\" WHERE \"Complete\" = FALSE AND \"DateGroupID\" = ? AND \"DataName\" ilike ?;");
                            for(String name : extraDownloadFiles)
                            {
                                it = dates.iterator();
                                while(it.hasNext())
                                {
                                    id = it.next();
                                    pstmt.setInt(1, id);
                                    pstmt.setString(2, name);
                                    rs = pstmt.executeQuery();
                                    if(rs == null || !rs.next()) {
                                        it.remove();
                                    } else {
                                        rs.close();
                                    }
                                }
                            }
                        }

                        // Step 3
                        if(dates.size() > 0)
                        {
                            pstmt = conn.prepareStatement("UPDATE \"" + mSchemaName + "\".\"DownloadCache\" SET \"Complete\" = TRUE WHERE \"DateGroupID\" = ?");
                            for(Integer dateID : dates)
                            {
                                pstmt.setInt(1, dateID);
                                pstmt.execute();
                            }

                            if(extraDownloadFiles != null)
                            {
                                pstmt = conn.prepareStatement("UPDATE \"" + mSchemaName + "\".\"DownloadCacheExtra\" SET \"Complete\" = TRUE WHERE \"DateGroupID\" = ?");
                                for(Integer dateID : dates)
                                {
                                    pstmt.setInt(1, dateID);
                                    pstmt.execute();
                                }
                            }
                        }

                        pstmt.close();
                    }
                }
            }

            // Update progress bar
            scheduler.UpdateDownloadProgressByData(dataName, pluginName, listDatesFiles, modisTileNames, stmt);

            stmt.close();
            conn.close();

            // Signal to observers that changes occurred
            System.out.println("Finished checking for unprocessed downloads Files for " + dates.size() + " day" + (dates.size() > 1 ? "s" : "") + " loaded for plugin '" + pluginName
                    + "' in project '" + projectName + "'. Notifying project Processor of the additional work.");
            if(dates.size() > 0)
            {
                filesAvailable = true;
                setChanged();
                notifyObservers();
            }
        }

        return changes;
    }

    /**
     * Add file(s) to the cache table this DatabaseCache object is mapped to. Notifies observers that files are available for further processing.
     * All files will be submitted as a single transaction and associated to the given year and day (or the number of days in the download composite size if larger than a day).
     *
     * @param filesForASingleComposite  - A list of all files to be inserted and handled as a single download/data composite. The extraDownloads field will not be used.
     * Each file needs its own DataFileMetaData instance. DataFileMetaData instances with data name of "Data" will be inserted into the 'Download' table and others will be
     * added to the 'ExtraDownload' table.
     * @throws SQLException
     * @throws ParseException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void CacheFiles(ArrayList<DataFileMetaData> filesForASingleComposite) throws SQLException, ParseException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException
    {
        if(filesForASingleComposite.size() == 0) {
            return;
        }
        Connection conn = DatabaseConnector.getConnection();
        Statement stmt = conn.createStatement();

        String processCachingForName;
        String processNotifyingName;
        switch(processCachingFor)
        {
        case PROCESSOR: processCachingForName = "Processor"; processNotifyingName = "Indices"; break;
        case INDICES: processCachingForName = "Indices"; processNotifyingName = "Summary"; break;
        case SUMMARY: processCachingForName = "Summary"; processNotifyingName = null; break;
        default: processCachingForName = null; processNotifyingName = null;
        }

        if(processCachingForName != null) {
            System.out.println("Caching files for " + processCachingForName + " in project '" + projectName + "' for plugin '" + pluginName + "' (Year: " + filesForASingleComposite.get(0).ReadMetaDataForSummary().year +
                    ", Day: " + filesForASingleComposite.get(0).ReadMetaDataForSummary().day + ").");
        }

        IndicesFileMetaData temp = filesForASingleComposite.get(0).ReadMetaDataForSummary();
        Integer dateGroupID = Schemas.getDateGroupID(globalSchema, LocalDate.ofYearDay(temp.year, temp.day), stmt);
        Integer indexID = Schemas.getIndexID(globalSchema, temp.indexNm, stmt);
        String indexIDSelectString;
        String indexIDValueString;
        if(indexID != null && indexID != -1 && processCachingFor == ProcessName.INDICES)
        {
            indexIDSelectString = ", \"IndexID\"";
            indexIDValueString = ", " + indexID;
        }
        else
        {
            indexIDSelectString = "";
            indexIDValueString = "";
        }
        StringBuilder query = new StringBuilder(String.format(
                "INSERT INTO \"%1$s\".\"%2$s\" \n" +
                        "(\"DataFilePath\", \"DateGroupID\"" + indexIDSelectString + ") VALUES \n" +
                        "('" + temp.dataFilePath + "', " + dateGroupID + indexIDValueString + ")",
                        mSchemaName,
                        cacheToTableName
                ));
        for(int i=1; i < filesForASingleComposite.size(); i++)
        {
            temp = filesForASingleComposite.get(i).ReadMetaDataForSummary();
            dateGroupID = Schemas.getDateGroupID(globalSchema, LocalDate.ofYearDay(temp.year, temp.day), stmt);
            if(!indexIDSelectString.equals(""))
            {
                indexID = Schemas.getIndexID(globalSchema, temp.indexNm, stmt);
                indexIDValueString = ", " + indexID;
            }
            query.append(",\n('" + temp.dataFilePath + "', " + dateGroupID + indexIDValueString + ")");
        }
        query.append(";");
        stmt.execute(query.toString());

        // Update progress bar
        if(processCachingFor == ProcessName.PROCESSOR) {
            scheduler.UpdateProcessorProgress(pluginName, stmt);
        }
        else if(processCachingFor == ProcessName.INDICES) {
            scheduler.UpdateIndicesProgress(pluginName, stmt);
        }

        Schemas.setProcessed(mSchemaName, setProcessedForTableName, dateGroupID, stmt);
        if(processCachingFor == ProcessName.PROCESSOR) {
            Schemas.setProcessed(mSchemaName, "DownloadCacheExtra", dateGroupID, stmt);
        }
        //        scheduler.NotifyUI(new GeneralUIEventObject(this, null));

        stmt.close();
        conn.close();

        synchronized(filesAvailable) {
            filesAvailable = true;
        }
        if(processNotifyingName != null) {
            System.out.println("Notifying " + processNotifyingName + " calculator in project '" + projectName + "' of the additional work.");
        }
        setChanged();
        notifyObservers();
    }

    /**
     * Uploads summary results to the database as the "cache" update for summary as there is no actual cache to be used by it but results are stored in the database for UI retrieval. Summary calculators are
     * expected to produce result files which the mTableFile refers to.
     * @param newResults
     * @param summaryIDNum
     * @param indexNm
     * @param compStrategy
     * @param year
     * @param day
     * @param process
     * @param daysPerInputData
     * @throws IllegalArgumentException
     * @throws UnsupportedOperationException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws SQLException
     */
    public void UploadResultsToDb(ArrayList<SummaryResult> newResults, int summaryIDNum, String indexNm, TemporalSummaryCompositionStrategy compStrategy, int year, int day, Process process, int daysPerInputData)
            throws IllegalArgumentException, UnsupportedOperationException, IOException, ClassNotFoundException, ParserConfigurationException, SAXException, SQLException {
        final Connection conn = DatabaseConnector.getConnection();
        Statement stmt = conn.createStatement();
        PreparedStatement pStmt = null;
        //        final boolean previousAutoCommit = conn.getAutoCommit();

        if(newResults.size() == 0) {
            return;
        }

        System.out.println("Uploading summary results in project '" + projectName + "' for plugin '" + pluginName + "' of index '" + indexNm + "' (Year: " + year + ", Day: " + day + ").");

        try {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            StringBuilder insertUpdate = new StringBuilder("INSERT INTO \"%s\".\"ZonalStat\" ("
                    + "\"ProjectSummaryID\", "
                    + "\"AreaName\", "
                    + "\"AreaCode\", "
                    + "\"DateGroupID\", "
                    + "\"IndexID\", "
                    + "\"FilePath\""
                    );
            for(String summarySimpleName : newResults.get(0).summaryResults.keySet())
            {
                insertUpdate.append(", \"" + summarySimpleName + "\"");
            }
            insertUpdate.append(") VALUES (" +
                    "?" +   // 1. ProjectSummaryID
                    ",?" +  // 2. AreaName
                    ",?" +  // 3. AreaCode
                    ",?" +  // 4. DateGroupID
                    ",?" +  // 5. IndexID
                    ",?");  // 6. FilePath
            for(@SuppressWarnings("unused") String summarySimpleName : newResults.get(0).summaryResults.keySet())
            {
                insertUpdate.append(",?");
            }
            insertUpdate.append(")");

            pStmt = conn.prepareStatement(String.format(insertUpdate.toString(), mSchemaName));

            int i;
            for(SummaryResult newResult : newResults)
            {
                pStmt.setInt(1, newResult.projectSummaryID);
                pStmt.setString(2, newResult.areaName);
                pStmt.setInt(3, newResult.areaCode);
                pStmt.setInt(4, newResult.dateGroupID);
                pStmt.setInt(5, newResult.indexID);
                pStmt.setString(6, newResult.filePath);

                i = 0;
                for(Double value : newResult.summaryResults.values())
                {
                    pStmt.setDouble(7 + i++, value);
                }
                pStmt.addBatch();
            }

            //            conn.setAutoCommit(false);
            int[] changesMade = pStmt.executeBatch();
            boolean updateSuccessful = true;
            for(i=0; i < changesMade.length; i++) {
                if(changesMade[i] <= 0) {
                    updateSuccessful = false;
                    break;
                }
            }
            if(!updateSuccessful) {
                ErrorLog.add(processCachingFor, scheduler, "Summary db insert failed for date: Year=" + year + ", DayOfYear=" + day + ". Warnings=\"" + pStmt.getWarnings().toString(),
                        new Exception("Summary db insert failed for date: Year=" + year + ", DayOfYear=" + day + "."));
            }
            //            conn.commit();

            // Update progress bar
            scheduler.UpdateSummaryProgress(summaryIDNum, compStrategy, daysPerInputData, pluginInfo, stmt);
            Schemas.setProcessed(mSchemaName, setProcessedForTableName, newResults.get(0).dateGroupID, stmt);
            //            scheduler.NotifyUI(new GeneralUIEventObject(this, null));
        }
        catch (SQLException e) {
            ErrorLog.add(process, "Problem in ZonalSummaryCalculator.uploadResultsToDb executing zonal summaries results.", e);
            //            conn.rollback();
        }
        finally {
            //            conn.setAutoCommit(previousAutoCommit);
            stmt.close();
            if(pStmt != null) {
                pStmt.close();
            }
            conn.close();
        }
    }

    /**
     * Forces the state of this DatabaseCache to that of "changed" and notifies any and all observers to act and check for available updates.
     */
    public void NotifyObserversToCheckForPastUpdates()
    {
        setChanged();
        notifyObservers();
    }

    /**
     * Used to set up custom sort order:
     *  1. DateGroupID ascending order
     *  2. Data Name = "Data"
     *  3. Data Name ascending order
     * @author Michael DeVos
     */
    protected class Record implements Comparable<Record>
    {
        public final int dateGroupID;
        public final String dataName;
        public final DataFileMetaData data;

        public Record(int dateGroupID, String dataName, DataFileMetaData data)
        {
            this.dateGroupID = dateGroupID;
            this.dataName = dataName;
            this.data = data;
        }

        @Override
        public int compareTo(Record o) {
            if(dateGroupID < o.dateGroupID) {
                return -1;
            } else if(dateGroupID > o.dateGroupID) {
                return 1;
            }

            if(dataName.equals(o.dataName)) {
                return data.ReadMetaDataForProcessor().dataFilePath.compareTo(o.data.ReadMetaDataForProcessor().dataFilePath);
            } else if(dataName.equalsIgnoreCase("Data")) {
                return -1;
            } else {
                return dataName.compareTo(o.dataName);
            }
        }
    }

}
