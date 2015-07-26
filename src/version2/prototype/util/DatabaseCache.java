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
    private final String inputTableName;
    private final String myTableName;
    private final ProcessName process;
    private final ArrayList<String> extraDownloadFiles;
    private Boolean filesAvailable;

    /**
     * Creates a DatabaseCache object set to cache files to and get file listings from the table identified by the given information.
     *
     * @param projectName  - project schema to look under
     * @param pluginName  - plugin schema to look under
     * @param dataComingFrom  - name of process to check output of for available files to process
     * @param extraDownloadFiles  - the data names of the extra download files associated with the related plugin metadata
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
        case DOWNLOAD: inputTableName = "DownloadCache"; myTableName = "ProcessorCache"; break;
        case PROCESSOR: inputTableName = "ProcessorCache"; myTableName = "IndicesCache"; break;
        case INDICES: inputTableName = "IndicesCache"; myTableName = null; break;
        case SUMMARY: inputTableName = null; myTableName = null; break;
        default: throw new ParseException("ProcessName 'dataComingFrom' doesn't contain an expected framework identifier.", 0);
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
                inputTableName
                ));
        for(int i=0; i < extraDownloadFiles.size(); i++)
        {
            query.append("A.\"" + extraDownloadFiles.get(i) + "FilePath\", ");
        }
        query.append(String.format("I.\"Name\", A.\"DataGroupID\", D.\"Year\", D.\"DayOfYear\" \n" +
                "FROM \"%1$s\".\"%2$s\" A INNER JOIN \"%3$s\".\"DateGroup\" D ON (A.\"DataGroupID\" = D.\"DataGroupID\") INNER JOIN \"%3$s\".\"Index\" D ON (A.\"IndexID\" = I.\"IndexID\")\n" +
                "WHERE \"Retrieved\" = FALSE AND \"Processed\" = FALSE FOR UPDATE;",
                schemaName,
                inputTableName,
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
                        tempExtraDownloads.add(new DataFileMetaData(dataName, rs.getString(dataName + "FilePath"), tempYear, tempDayOfYear));
                    }

                    files.add(new DataFileMetaData("Data", rs.getString("DataFilePath"), tempYear, tempDayOfYear, tempExtraDownloads));
                    rows.add(rs.getInt(inputTableName + "ID"));
                }

                for(int row : rows)
                {
                    conn.createStatement().execute(String.format(
                            "UPDATE \"%1$s\".\"%2$s\"\n" +
                                    "SET \"Retrieved\" = TRUE\n" +
                                    "WHERE \"%2$sID\" = %3$d",
                                    schemaName,
                                    inputTableName,
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

        changes = Schemas.loadUnprocessedDownloadsToLocalDownloader(globalEASTWebSchema, projectName, pluginName, startDate, extraDownloadFiles);

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
        Integer dateGroupID = Schemas.getDateGroupID(Config.getInstance().getGlobalSchema(), LocalDate.ofYearDay(temp.year, temp.day), stmt);
        Integer indexID = Schemas.getIndexID(Config.getInstance().getGlobalSchema(), temp.indexNm, stmt);
        StringBuilder query = new StringBuilder(String.format(
                "INSERT INTO \"%1$s\".\"%2$s\" \n" +
                        "(\"DataFilePath\", \"DataGroupID\", \"IndexID\") VALUES \n" +
                        "('" + temp.dataFilePath + "', " + dateGroupID + ", " + indexID + ")",
                        schemaName,
                        myTableName
                ));
        for(int i=1; i < filesForASingleComposite.size(); i++)
        {
            temp = filesForASingleComposite.get(i).ReadMetaDataForSummary();
            dateGroupID = Schemas.getDateGroupID(Config.getInstance().getGlobalSchema(), LocalDate.ofYearDay(temp.year, temp.day), stmt);
            indexID = Schemas.getIndexID(Config.getInstance().getGlobalSchema(), temp.indexNm, stmt);
            query.append(",\n('" + temp.dataFilePath + "', " + dateGroupID + ", " + indexID + ")");
        }
        query.append(";");
        stmt.execute(query.toString());

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
            throw new ParseException("Filepath doesn't contain expected formatted project, plugin, year, and day.", 0);
        }

        return new DataFileMetaData("Data", fullPath, year, day);
    }

    public void NotifyObserversToCheckForPastUpdates()
    {
        setChanged();
        notifyObservers();
    }
}
