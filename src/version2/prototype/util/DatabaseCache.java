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
import version2.prototype.Scheduler.ProcessName;

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

    private final String globalSchema;
    private final String projectName;
    private final String pluginName;
    private final String getFromTableName;
    private final String cacheToTableName;
    private final ProcessName processCachingFor;
    private final ArrayList<String> extraDownloadFiles;
    private Boolean filesAvailable;

    /**
     * Creates a DatabaseCache object set to cache files to and get file listings from the table identified by the given information.
     *
     * @param globalSchema
     * @param projectName  - project schema to look under
     * @param pluginName  - plugin schema to look under
     * @param processCachingFor  - name of process to cache output for
     * @param extraDownloadFiles  - the data names of the extra download files associated with the related plugin metadata
     * @throws ParseException
     */
    public DatabaseCache(String globalSchema, String projectName, String pluginName, ProcessName processCachingFor, ArrayList<String> extraDownloadFiles) throws ParseException
    {
        this.globalSchema = globalSchema;
        this.projectName = projectName;
        this.pluginName = pluginName;
        this.processCachingFor = processCachingFor;
        if(extraDownloadFiles != null) {
            this.extraDownloadFiles = extraDownloadFiles;
        } else {
            this.extraDownloadFiles = new ArrayList<String>();
        }
        filesAvailable = false;

        // Setup so that a single DatabaseCache object is intended to be used for output by one process and then used by another for input
        switch(this.processCachingFor)
        {
        case DOWNLOAD: cacheToTableName = "DownloadCache"; getFromTableName = null; break;
        case PROCESSOR: cacheToTableName = "ProcessorCache"; getFromTableName = "ProcessorCache"; break;
        case INDICES: cacheToTableName = "IndicesCache"; getFromTableName = "IndicesCache"; break;
        case SUMMARY: cacheToTableName = null; getFromTableName = null; break;
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
        String schemaName = Schemas.getSchemaName(projectName, pluginName);
        TreeMap<Integer, TreeSet<Record>> files = new TreeMap<Integer, TreeSet<Record>>();
        ArrayList<Integer> rows = new ArrayList<Integer>();
        int dateGroupID, tempDayOfYear, tempYear;
        String tempDataName;
        ResultSet rs = null;
        TreeSet<Record> temp;


        try {
            conn = PostgreSQLConnection.getConnection();
            stmt = conn.createStatement();
            conn.createStatement().execute("BEGIN");

            if(processCachingFor == ProcessName.DOWNLOAD)
            {
                synchronized(filesAvailable)
                {
                    // Collect completed but not retrieved records from DownloadCache
                    String downloadCacheQuery = "SELECT D.*, G.\"Year\", G.\"DayOfYear\", G.\"DateGroupID\" FROM \"" + schemaName + "\".\"DownloadCache\" D " +
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
                                        schemaName,
                                        "DownloadCache",
                                        row
                                ));
                    }
                    rows = new ArrayList<Integer>();

                    // If necessary, collect completed but not retrieved records from DownloadCacheExtra
                    String downloadCacheExtraQuery = "";
                    if(extraDownloadFiles != null && extraDownloadFiles.size() > 0)
                    {
                        downloadCacheExtraQuery = "SELECT D.*, G.\"Year\", G.\"DayOfYear\", G.\"DateGroupID\" FROM \"" + schemaName + "\".\"DownloadCacheExtra\" D " +
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
                                            schemaName,
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
                                    schemaName,
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
                                        schemaName,
                                        getFromTableName,
                                        row
                                ));
                    }
                    conn.createStatement().execute("COMMIT");
                    filesAvailable = false;
                }
            }
        } catch(SQLException | ClassNotFoundException | IOException | SAXException | ParserConfigurationException e) {
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
     * @return number of records effected
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public int LoadUnprocessedGlobalDownloadsToLocalDownloader(String globalEASTWebSchema, String projectName, String pluginName, String dataName, LocalDate startDate, ArrayList<String> extraDownloadFiles,
            ArrayList<String> modisTileNames) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        int changes = 0;
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final int gdlID = Schemas.getGlobalDownloaderID(globalEASTWebSchema, pluginName, dataName, stmt);
        final String mSchemaName = Schemas.getSchemaName(projectName, pluginName);
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
                    PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM \"" + mSchemaName + "\".\"DownloadCache\" WHERE \"DateGroupID\" = ?;");
                    int id;
                    Iterator<Integer> it = dates.iterator();
                    while(it.hasNext())
                    {
                        id = it.next();
                        pstmt.setInt(1, id);
                        rs = pstmt.executeQuery();
                        if(rs == null) {
                            it.remove();
                        } else {
                            rs.close();
                        }
                    }

                    // Step 2b
                    if(extraDownloadFiles != null && extraDownloadFiles.size() > 0)
                    {
                        pstmt = conn.prepareStatement("SELECT * FROM \"" + mSchemaName + "\".\"DownloadCacheExtra\" WHERE \"DateGroupID\" = ? AND \"DataName\" = ?;");
                        for(String name : extraDownloadFiles)
                        {
                            it = dates.iterator();
                            while(it.hasNext())
                            {
                                id = it.next();
                                pstmt.setInt(1, id);
                                pstmt.setString(2, name);
                                rs = pstmt.executeQuery();
                                if(rs == null) {
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
        stmt.close();
        conn.close();

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
        String schemaName = Schemas.getSchemaName(projectName, pluginName);
        Connection conn = PostgreSQLConnection.getConnection();
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
                        schemaName,
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

    /**
     * Forces the state of this DatabaseCache to that of "changed" and notifies any and all observers to act and check for available updates.
     */
    public void NotifyObserversToCheckForPastUpdates()
    {
        setChanged();
        notifyObservers();
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
