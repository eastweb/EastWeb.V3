package version2.prototype.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Observable;
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
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ConfigReadException
     * @throws ClassNotFoundException
     */
    public ArrayList<DataFileMetaData> GetUnprocessedCacheFiles() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        String schemaName = Schemas.getSchemaName(projectName, pluginName);
        ArrayList<DataFileMetaData> files = new ArrayList<DataFileMetaData>();
        Connection conn = PostgreSQLConnection.getConnection();
        conn.createStatement().execute("BEGIN");

        StringBuilder query = new StringBuilder(String.format(
                "SELECT A.\"%1$sID\", A.\"DataFilePath\", ",
                getFromTableName
                ));
        for(int i=0; i < extraDownloadFiles.size(); i++)
        {
            query.append("A.\"" + extraDownloadFiles.get(i) + "FilePath\", ");
        }
        query.append(String.format("I.\"Name\", A.\"DateGroupID\", D.\"Year\", D.\"DayOfYear\" \n" +
                "FROM \"%1$s\".\"%2$s\" A INNER JOIN \"%3$s\".\"DateGroup\" D ON (A.\"DateGroupID\" = D.\"DateGroupID\") INNER JOIN \"%3$s\".\"Index\" I ON (A.\"IndexID\" = I.\"IndexID\")\n" +
                "WHERE \"Retrieved\" = FALSE AND \"Processed\" = FALSE FOR UPDATE;",
                schemaName,
                getFromTableName,
                globalSchema
                ));
        final Statement stmt = conn.createStatement();

        synchronized(filesAvailable)
        {
            final ResultSet rs = stmt.executeQuery(query.toString());
            ArrayList<Integer> rows = new ArrayList<Integer>();
            try {
                int tempDayOfYear, tempYear;
                ArrayList<DataFileMetaData> tempExtraDownloads = new ArrayList<DataFileMetaData>();
                while(rs.next()) {
                    tempDayOfYear = rs.getInt("DayOfYear");
                    tempYear = rs.getInt("Year");

                    for(String dataName : extraDownloadFiles)
                    {
                        tempExtraDownloads.add(new DataFileMetaData(dataName, rs.getString(dataName + "FilePath"), tempYear, tempDayOfYear));
                    }

                    files.add(new DataFileMetaData("Data", rs.getString("DataFilePath"), tempYear, tempDayOfYear, tempExtraDownloads));
                    rows.add(rs.getInt(getFromTableName + "ID"));
                }

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
            } catch(Exception e) {
                conn.createStatement().execute("ROLLBACK");
            } finally {
                rs.close();
            }
        }

        return files;
    }

    /**
     * Used by LocalDownloaders to load newly completed sets of downloads from GlobalDownloader objects and update this cache for observers.
     *
     * @param globalEASTWebSchema  - the schema for the globally accessible EASTWeb schema
     * @param projectName  - name of the project to load downloads to
     * @param pluginName  - name of the plugin to load downloads to
     * @param startDate  - the start date to load downloads beginning from this time or later
     * @param extraDownloadFiles  - names of extra download files listed within the plugin metadata
     * @return number of records effected
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public int LoadUnprocessedGlobalDownloadsToLocalDownloader(String globalEASTWebSchema, String projectName, String pluginName, LocalDate startDate, ArrayList<String> extraDownloadFiles)
            throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        int changes = -1;
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final int gdlID = Schemas.getGlobalDownloaderID(globalEASTWebSchema, pluginName, stmt);
        final String mSchemaName = Schemas.getSchemaName(projectName, pluginName);

        // Set up query
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
                + "WHERE D.\"GlobalDownloaderID\" = " + gdlID + " AND D.\"Complete\" = TRUE AND L.\"DownloadID\" IS NULL",
                mSchemaName));
        for(int i=0; i < extraDownloadFiles.size(); i++)
        {
            query.append(" AND " + Character.toChars('E' + i)[0] + ".\"DataName\"='" + extraDownloadFiles.get(i) + "';");
        }

        // Execute and get number of rows effected
        changes = stmt.executeUpdate(query.toString());
        stmt.close();
        conn.close();

        // Notify observers
        synchronized(this)
        {
            filesAvailable = true;
        }
        setChanged();
        notifyObservers();

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
        StringBuilder query = new StringBuilder(String.format(
                "INSERT INTO \"%1$s\".\"%2$s\" \n" +
                        "(\"DataFilePath\", \"DateGroupID\", \"IndexID\") VALUES \n" +
                        "('" + temp.dataFilePath + "', " + dateGroupID + ", " + indexID + ")",
                        schemaName,
                        cacheToTableName
                ));
        for(int i=1; i < filesForASingleComposite.size(); i++)
        {
            temp = filesForASingleComposite.get(i).ReadMetaDataForSummary();
            dateGroupID = Schemas.getDateGroupID(globalSchema, LocalDate.ofYearDay(temp.year, temp.day), stmt);
            indexID = Schemas.getIndexID(globalSchema, temp.indexNm, stmt);
            query.append(",\n('" + temp.dataFilePath + "', " + dateGroupID + ", " + indexID + ")");
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
}
