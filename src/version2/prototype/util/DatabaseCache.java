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
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
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

    private final String projectName;
    private final String pluginName;
    private final String tableName;
    private final ProcessName process;
    private final ArrayList<String> extraDownloadFiles;
    private Boolean filesAvailable;

    /**
     * Creates a DatabaseCache object set to cache files to and get file listings from the table identified by the given information.
     *
     * @param projectName  - project schema to look under
     * @param pluginName  - plugin schema to look under
     * @param dataComingFrom  - name of process to check output of for available files to process
     * @throws ParseException
     */
    public DatabaseCache(String projectName, String pluginName, ProcessName dataComingFrom, ArrayList<String> extraDownloadFiles) throws ParseException
    {
        this.projectName = projectName;
        this.pluginName = pluginName;
        process = dataComingFrom;
        this.extraDownloadFiles = extraDownloadFiles;
        filesAvailable = false;

        switch(process)
        {
        case DOWNLOAD: tableName = "DownloadCache"; break;
        case INDICES: tableName = "IndicesCache"; break;
        case PROCESSOR: tableName = "ProcessCache"; break;
        case SUMMARY: tableName = "SummaryCache"; break;
        default: throw new ParseException("ProcessName 'dataComingFrom' doesn't contain an expected framework identifier.", 0);
        }
    }

    /**
     * Retrieves a list of files from the desired table that have yet to be retrieved by a ProcessWorker.
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
                tableName
                ));
        for(int i=0; i < extraDownloadFiles.size(); i++)
        {
            query.append("A.\"" + extraDownloadFiles.get(i) + "FilePath\", ");
        }
        query.append(String.format("I.\"Name\", A.\"DataGroupID\", D.\"Year\", D.\"DayOfYear\" \n" +
                "FROM \"%1$s\".\"%2$s\" A INNER JOIN \"%3$s\".\"DateGroup\" D ON (A.\"DataGroupID\" = D.\"DataGroupID\") INNER JOIN \"%3$s\".\"Index\" D ON (A.\"IndexID\" = I.\"IndexID\")\n" +
                "WHERE \"Complete\" = TRUE AND \"Retrieved\" != TRUE FOR UPDATE;",
                schemaName,
                tableName,
                Config.getInstance().getGlobalSchema()
                ));
        final Statement stmt = conn.createStatement();

        synchronized(filesAvailable)
        {
            final ResultSet rs = stmt.executeQuery(query.toString());
            ArrayList<Integer> rows = new ArrayList<Integer>();
            try {
                int tempDateGroupID, tempDayOfYear, tempYear;
                ArrayList<DataFileMetaData> tempExtraDownloads = new ArrayList<DataFileMetaData>();
                while(rs.next()) {
                    tempDateGroupID = rs.getInt("DataGroupID");
                    tempDayOfYear = rs.getInt("DayOfYear");
                    tempYear = rs.getInt("Year");

                    for(String dataName : extraDownloadFiles)
                    {
                        tempExtraDownloads.add(new DataFileMetaData(dataName, rs.getString(dataName + "FilePath"), tempDateGroupID, tempYear, tempDayOfYear));
                    }

                    files.add(new DataFileMetaData("Data", rs.getString("DataFilePath"), tempDateGroupID, tempYear, tempDayOfYear, tempExtraDownloads));
                    rows.add(rs.getInt(tableName + "ID"));
                }

                for(int row : rows)
                {
                    conn.createStatement().execute(String.format(
                            "UPDATE \"%1$s\".\"%2$s\"\n" +
                                    "SET \"Retrieved\" = TRUE\n" +
                                    "WHERE \"%2$sID\" = %3$d",
                                    schemaName,
                                    tableName,
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
     * Used by LocalDownloaders to load new downloads from GlobalDownloader objects and update this cache for observers.
     *
     * @param globalEASTWebSchema
     * @param projectName
     * @param pluginName
     * @param globalDownloaderInstanceID
     * @param startDate
     * @param extraDownloadFiles
     * @param daysPerInputFile
     * @return number of records effected
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public int loadUnprocessedDownloadsToLocalDownloader(String globalEASTWebSchema, String projectName, String pluginName, int globalDownloaderInstanceID, LocalDate startDate,
            ArrayList<String> extraDownloadFiles, int daysPerInputFile) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        int changes = -1;

        changes = Schemas.loadUnprocessedDownloadsToLocalDownloader(globalEASTWebSchema, projectName, pluginName, globalDownloaderInstanceID, startDate, extraDownloadFiles, daysPerInputFile);

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
     * All files will be submitted as a single transaction and associated to the given year and day.
     *
     * @param year  - 4 digit Gregorian year the data file(s) are for
     * @param day  - day of the year the data file(s) are for
     * @param filePaths  - file paths to add to the cache table
     * @throws SQLException
     * @throws ParseException
     * @throws ConfigReadException
     * @throws ClassNotFoundException
     */
    public void CacheFile(ArrayList<DataFileMetaData> cacheFiles) throws SQLException, ParseException, ConfigReadException, ClassNotFoundException
    {
        String dateDirectory;

        // Parse out date directory
        Matcher matcher = filePathPattern.matcher(filePath);
        if(matcher.find()) {
            dateDirectory = filePath.substring(0, matcher.end());
        } else {
            throw new ParseException("Filepath doesn't contain expected formatted project, plugin, year, and day.", 0);
        }

        String schemaName = Schemas.getSchemaName(projectName, pluginName);
        Connection conn = PostgreSQLConnection.getConnection();
        String query = String.format(
                "INSERT INTO \"%1$s\".\"%2$s\" (\n" +
                        "\"FullPath\",\n" +
                        "\"DateDirectory\",\n" +
                        "\"DataGroupID\"\n" +
                        ") VALUES (\n" +
                        "\"%3$s\",\n" +
                        "\"%4$s\",\n" +
                        "?\n" +
                        ")",
                        schemaName,
                        tableName,
                        filePath,
                        dateDirectory
                );
        PreparedStatement psInsertFile = conn.prepareStatement(query);

        // Get data group ID
        query = String.format(
                "SELECT DataGroupdID FROM \"%1$s\".\"%2$s\"\n" +
                        "WHERE \"Year\" = ? AND \n" +
                        "\"DayOfYear\" = ?",
                        schemaName,
                        tableName
                );
        PreparedStatement psDG = conn.prepareStatement(query);
        psDG.setString(1, String.valueOf(year));
        psDG.setString(2, String.valueOf(day));
        ResultSet rs = psDG.executeQuery();
        try {
            if(rs.next()) {
                psInsertFile.setInt(1, rs.getInt(1));
            }
            else
            {
                query = String.format(
                        "INSERT INTO \"%1$s\".\"%2$s\" (\n" +
                                "\"Year\",\n" +
                                "\"DayOfYear\")\n" +
                                "VALUES (" +
                                "%3$d,\n" +
                                "%4$d)",
                                schemaName,
                                tableName,
                                year,
                                day
                        );
                psDG = conn.prepareStatement(query);
                rs = psDG.executeQuery();
                query = String.format(
                        "SELECT currval(\"%1$s\".\"%2$s\")",
                        schemaName,
                        tableName + "_" + tableName + "ID_seq"
                        );
                rs = conn.prepareStatement(query).executeQuery();

                if (rs.next()) {
                    psInsertFile.setInt(1, rs.getInt(1));
                } else {
                    throw new SQLException("Couldn't get ID of inserted DataGroup row.");
                }
            }
            rs = psInsertFile.executeQuery();

            synchronized(this)
            {
                filesAvailable = true;
            }
            setChanged();
            notifyObservers();
        } finally {
            rs.close();
        }
    }

    /**
     * Can be used to get a DataFileMetaData object created from parsing the given file path.
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
        String projectName, pluginName, tableName, dateDirectory;
        int dataGroupID = -1, year, day;

        // Parse out date directory
        Matcher matcher = filePathPattern.matcher(fullPath);
        if(matcher.find()) {
            projectName = matcher.group(1);
            dateDirectory = fullPath.substring(0, matcher.end());

            switch(matcher.group(2))
            {
            case "download": tableName = "DownloadCache"; break;
            case "indices": tableName = "IndicesCache"; break;
            case "process": tableName = "ProcessCache"; break;
            case "summary": tableName = "SummaryCache"; break;
            default: throw new ParseException("Filepath doesn't contain an expected framework identifier.", 0);
            }

            pluginName = matcher.group(3);
            year = Integer.parseInt(matcher.group(4));
            day = Integer.parseInt(matcher.group(5));
        } else {
            throw new ParseException("Filepath doesn't contain expected formatted project, plugin, year, and day.", 0);
        }

        String schemaName = Schemas.getSchemaName(projectName, pluginName);
        Connection conn = PostgreSQLConnection.getConnection();

        // Get data group ID
        String query = String.format(
                "SELECT DataGroupdID FROM \"%1$s\".\"%2$s\"\n" +
                        "WHERE \"Year\" = ? AND \n" +
                        "\"DayOfYear\" = ?",
                        schemaName,
                        tableName
                );
        PreparedStatement psDG = conn.prepareStatement(query);
        psDG.setString(1, String.valueOf(year));
        psDG.setString(2, String.valueOf(day));
        ResultSet rs = psDG.executeQuery();
        try {
            if(rs.next()) {
                dataGroupID = rs.getInt(1);
            }
            else
            {
                query = String.format(
                        "INSERT INTO \"%1$s\".\"%2$s\" (\n" +
                                "\"Year\",\n" +
                                "\"DayOfYear\")\n" +
                                "VALUES (" +
                                "%3$d,\n" +
                                "%4$d)",
                                schemaName,
                                tableName,
                                year,
                                day
                        );
                psDG = conn.prepareStatement(query);
                rs = psDG.executeQuery();
                query = String.format(
                        "SELECT currval(\"%1$s\".\"%2$s\")",
                        schemaName,
                        tableName + "_" + tableName + "ID_seq"
                        );
                rs = conn.prepareStatement(query).executeQuery();

                if (rs.next()) {
                    dataGroupID = rs.getInt(1);
                } else {
                    throw new SQLException("Couldn't get ID of inserted DataGroup row.");
                }
            }
        } finally {
            rs.close();
        }

        return new DataFileMetaData(fullPath, dateDirectory, dataGroupID, year, day);
    }

    public void NotifyObserversToCheckForPastUpdates()
    {
        setChanged();
        notifyObservers();
    }
}
