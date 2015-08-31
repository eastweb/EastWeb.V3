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
import java.util.regex.Matcher;
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
        case DOWNLOAD: cacheToTableName = "DownloadCache"; getFromTableName = null; break;
        case PROCESSOR: cacheToTableName = "ProcessorCache"; getFromTableName = "ProcessorCache"; break;
        case INDICES: cacheToTableName = "IndicesCache"; getFromTableName = "IndicesCache"; break;
        case SUMMARY: cacheToTableName = "ZonalStat"; getFromTableName = null; break;
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
        Connection conn = null;
        Statement stmt = null;
        TreeMap<Integer, TreeSet<Record>> files = new TreeMap<Integer, TreeSet<Record>>();
        ArrayList<Integer> rows = new ArrayList<Integer>();
        int dateGroupID, tempDayOfYear, tempYear;
        String tempDataName;
        ResultSet rs = null;
        TreeSet<Record> temp;


        try {
            conn = DatabaseConnector.getConnection();
            stmt = conn.createStatement();
            conn.createStatement().execute("BEGIN");

            if(processCachingFor == ProcessName.DOWNLOAD)
            {
                synchronized(filesAvailable)
                {
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

                            if(files.isEmpty() || files.get(dateGroupID) == null) {
                                temp = new TreeSet<Record>();
                                temp.add(new Record(dateGroupID, "Data", new DataFileMetaData("Data", rs.getString("DataFilePath"), tempYear, tempDayOfYear)));
                                files.put(dateGroupID, temp);
                            }
                            else
                            {
                                files.get(dateGroupID).add(new Record(dateGroupID, "Data", new DataFileMetaData("Data", rs.getString("DataFilePath"), tempYear, tempDayOfYear)));
                            }
                            rows.add(rs.getInt("DownloadCacheID"));
                        }
                        rs.close();
                    }

                    for(int row : rows)
                    {
                        conn.createStatement().execute(String.format(
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

                                if(files.isEmpty() || files.get(dateGroupID) == null) {
                                    temp = new TreeSet<Record>();
                                    temp.add(new Record(dateGroupID, tempDataName, new DataFileMetaData(tempDataName, rs.getString("FilePath"), tempYear, tempDayOfYear)));
                                    files.put(dateGroupID, temp);
                                }
                                else
                                {
                                    files.get(dateGroupID).add(new Record(dateGroupID, tempDataName, new DataFileMetaData(tempDataName, rs.getString("FilePath"), tempYear, tempDayOfYear)));
                                }
                                rows.add(rs.getInt("DownloadCacheExtraID"));
                            }
                            rs.close();
                        }

                        for(int row : rows)
                        {
                            conn.createStatement().execute(String.format(
                                    "UPDATE \"%1$s\".\"%2$s\"\n" +
                                            "SET \"Retrieved\" = TRUE\n" +
                                            "WHERE \"%2$sID\" = %3$d",
                                            mSchemaName,
                                            "DownloadCacheExtra",
                                            row
                                    ));
                        }
                    }
                    conn.createStatement().execute("COMMIT");
                    filesAvailable = false;
                }
            }
            else
            {
                synchronized(filesAvailable)
                {
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
                            "SELECT A.\"%1$sID\", A.\"DataFilePath\", A.\"DateGroupID\", D.\"Year\", D.\"DayOfYear\"" + indexSelectString + "\n" +
                                    "FROM \"%2$s\".\"%1$s\" A INNER JOIN \"%3$s\".\"DateGroup\" D ON (A.\"DateGroupID\" = D.\"DateGroupID\")" + indexJoinString + "\n" +
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

                        if(files.isEmpty() || files.get(dateGroupID) == null) {
                            temp = new TreeSet<Record>();
                            files.put(dateGroupID, temp);
                        }
                        else {
                            temp = files.get(dateGroupID);
                        }

                        if(processCachingFor == ProcessName.INDICES) {
                            temp.add(new Record(dateGroupID, "Data", new DataFileMetaData(rs.getString("DataFilePath"), tempYear, tempDayOfYear, rs.getString("IndexName"))));
                        } else {
                            temp.add(new Record(dateGroupID, "Data", new DataFileMetaData("Data", rs.getString("DataFilePath"), tempYear, tempDayOfYear)));
                        }
                        rows.add(rs.getInt(getFromTableName + "ID"));
                    }
                    rs.close();

                    for(int row : rows)
                    {
                        conn.createStatement().execute(String.format(
                                "UPDATE \"%1$s\".\"%2$s\"\n" +
                                        "SET \"Retrieved\" = TRUE\n" +
                                        "WHERE \"%2$sID\" = %3$d",
                                        mSchemaName,
                                        getFromTableName,
                                        row
                                ));
                    }
                    conn.createStatement().execute("COMMIT");
                    filesAvailable = false;
                }
            }
        } catch(SQLException e) {
            conn.createStatement().execute("ROLLBACK");
            if(stmt != null) {
                stmt.close();
            }
            if(rs != null) {
                rs.close();
            }
            if(conn != null) {
                conn.close();
            }
            throw e;
        }

        ArrayList<DataFileMetaData> output = new ArrayList<DataFileMetaData>();
        Collection<TreeSet<Record>> recsCol = files.values();
        for(TreeSet<Record> recs : recsCol)
        {
            for(Record rec : recs)
            {
                output.add(rec.data);
            }
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
        StringBuilder query;

        // Only use modisTileNames if the plugin uses Modis data
        if(!modisPattern.matcher(pluginName.toLowerCase()).matches())
        {
            modisTileNames = null;
        }

        // Set up for Download to DownloadCache insert
        if(dataName.toLowerCase().equals("data"))
        {
            query = new StringBuilder(String.format(
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
            if(modisTileNames != null)
            {
                for(String tile : modisTileNames)
                {
                    query.append(" AND D.\"DataFilePath\" LIKE '%" + tile + "%'");
                }
            }

            // Execute Download to DownloadCache insert and get number of rows effected
            changes = stmt.executeUpdate(query.toString());
        }
        else{
            // Set up for DownloadExtra to DownloadCacheExtra insert
            query = new StringBuilder(String.format(
                    "INSERT INTO \"%1$s\".\"DownloadCacheExtra\" (\"DownloadExtraID\", \"DataName\", \"FilePath\", \"DateGroupID\") " +
                            "SELECT D.\"DownloadExtraID\", D.\"DataName\", D.\"FilePath\", D.\"DateGroupID\" " +
                            "FROM \"%2$s\".\"DownloadExtra\" D ",
                            mSchemaName,
                            globalEASTWebSchema
                    ));
            query.append(String.format("INNER JOIN \"%1$s\".\"DateGroup\" G ON D.\"DateGroupID\" = G.\"DateGroupID\" " +
                    "LEFT JOIN \"%2$s\".\"DownloadCacheExtra\" C ON D.\"DownloadExtraID\" = C.\"DownloadExtraID\" " +
                    "WHERE D.\"GlobalDownloaderID\" = " + gdlID +
                    " AND ((G.\"Year\" = " + startDate.getYear() + " AND G.\"DayOfYear\" >= " + startDate.getDayOfYear() + ") OR (G.\"Year\" > " + startDate.getYear() + "))" +
                    " AND D.\"Complete\" = TRUE" +
                    " AND C.\"DownloadExtraID\" IS NULL",
                    globalEASTWebSchema,
                    mSchemaName));
            if(modisTileNames != null)
            {
                for(String tile : modisTileNames)
                {
                    query.append(" AND D.\"FilePath\" LIKE '%" + tile + "%'");
                }
            }

            // Execute Download to DownloadCache insert and get number of rows effected
            changes += stmt.executeUpdate(query.toString());
        }

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
            query = new StringBuilder("SELECT * FROM \"" + globalEASTWebSchema + "\".\"DateGroup\";");
            ResultSet rs = stmt.executeQuery(query.toString());
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
                        pstmt = conn.prepareStatement("SELECT * FROM \"" + mSchemaName + "\".\"DownloadCacheExtra\" WHERE \"Complete\" = FALSE AND \"DateGroupID\" = ? AND \"DataName\" = ?;");
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
        String progressQuery;
        double progress = 0;
        int currentCount = 0;
        int expectedCount = 0;
        if(dataName.toLowerCase().equals("data"))
        {
            progressQuery = "SELECT Count(\"DownloadCacheID\") AS \"DownloadCacheIDCount\" FROM \"" + mSchemaName + "\".\"DownloadCache\";";
            ResultSet rs = stmt.executeQuery(progressQuery);
            if(rs != null && rs.next())
            {
                currentCount = rs.getInt("DownloadCacheIDCount");
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
                rs.close();
            }
        }
        else
        {
            progressQuery = "SELECT Count(\"DownloadCacheExtraID\") AS \"DownloadCacheExtraIDCount\" FROM \"" + mSchemaName + "\".\"DownloadCacheExtra\";";
            ResultSet rs = stmt.executeQuery(progressQuery);
            if(rs != null && rs.next())
            {
                currentCount = rs.getInt("DownloadCacheExtraIDCount");
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
                rs.close();
            }
        }

        if(expectedCount > 0 && currentCount > 0)
        {
            progress = (new Double(currentCount) / new Double(expectedCount)) * 100;
        }
        scheduler.NotifyUI(new GeneralUIEventObject(this, null, progress, pluginName, dataName, expectedCount));

        stmt.close();
        conn.close();

        // Signal to observers that changes occurred
        if(dates.size() > 0)
        {
            synchronized(this) {
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
        String progressQuery = "SELECT Count(\"" + cacheToTableName + "ID\") AS \"" + cacheToTableName + "IDCount\" FROM \"" + mSchemaName + "\".\"" + cacheToTableName + "\";";
        double progress = 0;
        int currentCount = 0;
        int expectedCount = 0;
        ResultSet rs = stmt.executeQuery(progressQuery);
        if(rs != null && rs.next())
        {
            currentCount = rs.getInt(cacheToTableName + "IDCount");
            rs.close();
        }
        if(processCachingFor == ProcessName.PROCESSOR)
        {
            expectedCount = pluginMetaData.Processor.numOfOutput * scheduler.GetSchedulerStatus().downloadExpectedDataFiles.get(pluginName);
        }
        else if(processCachingFor == ProcessName.INDICES)
        {
            expectedCount = pluginMetaData.Indices.indicesNames.size() * scheduler.GetSchedulerStatus().processorExpectedNumOfOutputs.get(pluginName);
        }
        if(expectedCount > 0 && currentCount > 0)
        {
            progress = (new Double(currentCount) / new Double(expectedCount)) * 100;
        }
        scheduler.NotifyUI(new GeneralUIEventObject(this, null, progress, pluginName, expectedCount));

        stmt.close();
        conn.close();

        synchronized(this)
        {
            filesAvailable = true;
        }
        setChanged();
        notifyObservers();
    }

    /**
     * Uploads summary results to the database as the "cache" update for summary as there is no actual cache to be used by it but results are stored in the database for UI retrieval. Summary calculators are
     * expected to produce result files which the mTableFile refers to.
     * @param newResults
     * @param summaryID
     * @param compStrategy
     * @param year
     * @param day
     * @param process
     * @param count
     * @param fileNum
     * @throws IllegalArgumentException
     * @throws UnsupportedOperationException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws SQLException
     */
    public void UploadResultsToDb(ArrayList<SummaryResult> newResults, int summaryID, TemporalSummaryCompositionStrategy compStrategy, int year, int day, Process process, int count, int fileNum)
            throws IllegalArgumentException, UnsupportedOperationException, IOException, ClassNotFoundException, ParserConfigurationException, SAXException, SQLException {
        final Connection conn = DatabaseConnector.getConnection();
        Statement stmt = conn.createStatement();
        PreparedStatement pStmt = null;
        //        final boolean previousAutoCommit = conn.getAutoCommit();

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
            pStmt.executeBatch();
            //            conn.commit();

            // Update progress bar
            String progressQuery = "SELECT Count(A.\"ZonalStatID\") AS \"ZonalStatIDCount\", B.\"SummaryIDNum\" FROM \"" + mSchemaName + "\".\"ZonalStat\" A INNER JOIN \""
                    + globalSchema + "\".\"ProjectSummary\" B ON A.\"ProjectSummaryID\" = B.\"ProjectSummaryID\" WHERE B.\"SummaryIDNum\"=" + summaryID + " GROUP BY B.\"SummaryIDNum\";";
            double progress = 0;
            int currentCount = 0;
            int expectedCount = 0;
            ResultSet rs = stmt.executeQuery(progressQuery);
            if(rs != null && rs.next())
            {
                currentCount = rs.getInt("ZonalStatIDCount");
                rs.close();

                if(compStrategy != null) {
                    rs = stmt.executeQuery("SELECT Count(A.\"DateGroupID\") AS \"DateGroupIDCount\", Max(D.\"Year\") AS \"MaxYear\", Max(D.\"DayOfYear\") AS \"MaxDay\", Min(D.\"Year\") AS \"MinYear\", " +
                            "Min(D.\"DayOfYear\") AS \"MinDay\" FROM \"" + mSchemaName + "\".\"IndicesCache\" A INNER JOIN \"" + globalSchema + "\".\"DateGroup\" D ON A.\"DateGroupID\"=D.\"DateGroupID\";");
                    if(rs != null && rs.next()) {
                        expectedCount = (int) ((rs.getInt("DateGroupIDCount") / compStrategy.getNumberOfCompleteCompositesInRange(LocalDate.ofYearDay(rs.getInt("MinYear"), rs.getInt("MinDay")),
                                LocalDate.ofYearDay(rs.getInt("MaxYear"), rs.getInt("MaxDay")), 1)) * pluginInfo.GetIndices().size());
                        rs.close();
                    }
                }
                else {
                    expectedCount = scheduler.GetSchedulerStatus().indicesExpectedNumOfOutputs.get(pluginName);
                }
            }

            if(expectedCount > 0 && currentCount > 0)
            {
                progress = (new Double(currentCount) / new Double(expectedCount)) * 100;
            }
            scheduler.NotifyUI(new GeneralUIEventObject(this, null, progress, pluginName, summaryID, expectedCount));
        }
        catch (SQLException e) {
            ErrorLog.add(workingDir, projectName, process, "Problem in ZonalSummaryCalculator.uploadResultsToDb executing zonal summaries results.", e);
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
     * Can be used to get a DataFileMetaData object created from parsing the given file path. Assumes data name of the file is "Data".
     *
     * @param fullPath  - path to the data file
     * @return returns the DataFileMetaData equivalent to the information that could be parsed from the path string passed in
     * @throws SQLException
     * @throws ParseException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static DataFileMetaData Parse(String fullPath) throws SQLException, ParseException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException
    {
        int year, day;

        // Parse out date directory
        Matcher matcher = filePathPattern.matcher(fullPath);
        if(matcher.find()) {
            year = Integer.parseInt(matcher.group(4));
            day = Integer.parseInt(matcher.group(5));
        } else {
            throw new ParseException("Filepath doesn't contain expected formatted project, plugin, year, and day. Expecting path to be of form \"" + filePathPattern.pattern() + "\"."
                    + " Path \"" + fullPath + "\" doesn't match.", 0);
        }

        return new DataFileMetaData("Data", fullPath, year, day);
    }

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
            if(dataName.equals(o.dataName)) {
                return 0;
            } else if(dataName.equalsIgnoreCase("Data")) {
                return -1;
            } else {
                return dataName.compareTo(o.dataName);
            }
        }
    }

}
