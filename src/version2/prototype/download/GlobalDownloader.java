package version2.prototype.download;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DownloadFileMetaData;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;


/**
 * @author michael.devos
 *
 */
public abstract class GlobalDownloader extends Observable implements Runnable{
    protected TaskState state;
    protected final int ID;
    protected final Config configInstance;
    protected final String globalSchema;
    protected final String pluginName;
    protected final DownloadMetaData metaData;
    protected final ListDatesFiles listDatesFiles;
    protected LocalDate currentStartDate;

    /**
     * Sets this GlobalDownloader super to have an initial state (TaskState) of STOPPED and registers the GlobalDownloader in the database.
     *
     * @param myID  - unique identifier ID of this GlobalDownloader
     * @param pluginName
     * @param metaData
     * @param listDatesFiles
     * @throws SQLException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws ClassNotFoundException
     */
    protected GlobalDownloader(int myID, Config configInstance, String pluginName, DownloadMetaData metaData, ListDatesFiles listDatesFiles, LocalDate startDate) throws ClassNotFoundException, ParserConfigurationException, SAXException,
    IOException, SQLException
    {
        state = TaskState.STOPPED;
        ID = myID;
        this.configInstance = configInstance;
        globalSchema = configInstance.getGlobalSchema();
        this.pluginName = pluginName;
        this.metaData = metaData;
        this.listDatesFiles = listDatesFiles;
        currentStartDate = startDate;
        RegisterGlobalDownloader();
    }


    /**
     * Sets this GlobalDownloader instance running state to STOPPED.
     */
    public final void Stop()
    {
        state = TaskState.STOPPED;
    }

    /**
     * Sets this GlobalDownloader instance running state to RUNNING.
     */
    public final void Start()
    {
        state = TaskState.RUNNING;
    }

    /**
     * Gets this GlobalDownloader instance's running state.
     *
     * @return TaskState indicating current state of this GlobalDownloader's running status
     */
    public final TaskState GetRunningState()
    {
        return state;
    }

    /**
     * Checks for any new data that's ready to be loaded by a LocalDownlader, flags them, and then sends notifications of those updates, if any, to observing LocalDownloaders.
     * This will all be done on the calling thread including the work that is done by the LocalDownloaders to perform these notified for updates.
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public final void PerformUpdates() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException,
    IOException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final int gdlID = Schemas.getGlobalDownloaderID(globalSchema, pluginName, stmt);
        int filesPerDay = metaData.filesPerDay;
        ArrayList<Integer> datesCompleted = new ArrayList<Integer>();
        Map<Integer, Integer> countOfDates = new TreeMap<Integer, Integer>();
        ResultSet rs;
        StringBuilder update;

        // Check if all files downloaded for any additional days
        rs = stmt.executeQuery("SELECT \"DateGroupID\", COUNT(\"DateGroupID\") AS \"DateGroupIDCount\" FROM \"" + globalSchema + "\".\"Download\" " +
                "WHERE \"GlobalDownloaderID\" = " + gdlID + " AND \"Complete\" = FALSE " +
                "GROUP BY \"DateGroupID\";");
        if(rs != null)
        {
            while(rs.next())
            {
                countOfDates.put(rs.getInt("DateGroupID"), rs.getInt("DateGroupIDCount"));
            }
        }
        rs.close();
        rs = stmt.executeQuery("SELECT B.\"DateGroupID\", COUNT(B.\"DateGroupID\") AS \"DateGroupIDCount\" FROM \"" + globalSchema + "\".\"ExtraDownload\" A " +
                "INNER JOIN \"" + globalSchema + "\".\"Download\" B ON A.\"DownloadID\" = B.\"DownloadID\" " +
                "WHERE B.\"GlobalDownloaderID\" = " + gdlID + " AND B.\"Complete\" = FALSE " +
                "GROUP BY \"DateGroupID\";");
        if(rs != null)
        {
            while(rs.next())
            {
                countOfDates.put(rs.getInt("DateGroupID"), countOfDates.get(rs.getInt("DateGroupID")) + rs.getInt("DateGroupIDCount"));
            }
        }
        rs.close();

        Iterator<Integer> iterator = countOfDates.keySet().iterator();
        int idx;
        while(iterator.hasNext())
        {
            idx = iterator.next();
            if(countOfDates.get(idx) == filesPerDay)
            {
                datesCompleted.add(idx);
            }
        }

        if(datesCompleted.size() > 0)
        {
            update = new StringBuilder("UPDATE \"" + globalSchema + "\".\"Download\" SET \"Complete\" = TRUE WHERE \"DateGroupID\" = " + datesCompleted.get(0));
            for(int i=1; i < datesCompleted.size(); i++)
            {
                update.append(" OR \"DateGroupID\" = " + datesCompleted.get(i));
            }
            update.append(";");
            stmt.executeUpdate(update.toString());
        }

        if(datesCompleted.size() > 0)
        {
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Gets the date this GlobalDownloader will/has started downloading from.
     *
     * @return start date for downloading
     */
    public final LocalDate GetStartDate() { return currentStartDate; }

    /**
     * Changes the start date for this GlobalDownloader and causes it to start downloading from the given date. Does not cause the GlobalDownloader to redownload anything already
     * downloaded but if the date is earlier than the current start date than it will start downloading from that date onward with the next set of downloads until caught up, or if it's
     * later than the current start date then it is simply ignored and the original start date is kept.
     *
     * @param newStartDate  - new date to state downloading from
     */
    public final void SetStartDate(LocalDate newStartDate)
    {
        if(currentStartDate.isAfter(newStartDate)) {
            currentStartDate = newStartDate;
        }
    }

    /**
     * Gets this GlobalDownloader's assigned unique ID. Only expected to be unique when compared to other currently existing GlobalDownloaders.
     *
     * @return  integer representing unique identifier of this GlobalDownloader
     */
    public final int GetID() { return ID; }

    /**
     * Gets the plugin name of the associated plugin metadata this GlobalDownloader uses.
     *
     * @return String of plugin name gotten from plugin metadata
     */
    public final String GetPluginName() { return pluginName; }

    /**
     * Gets all the current Download table entries for this GlobalDownloader. Represents all the dates and files downloaded for this plugin global downloader shareable across all projects.
     *
     * @return list of all Download table entries for the plugin name associated with this GlobalDownloader instance
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public final ArrayList<DataFileMetaData> GetAllDownloadedFiles() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        return GetAllDownloadedFiles(null);
    }

    /**
     * Gets all the current Download table entries for this GlobalDownloader. Represents all the dates and files downloaded for this plugin global downloader shareable across all projects.
     *
     * @param startDate  - the earliest date from which to start getting files
     * @return list of all Download table entries for the plugin name associated with this GlobalDownloader instance
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public final ArrayList<DataFileMetaData> GetAllDownloadedFiles(LocalDate startDate) throws SQLException, ClassNotFoundException, ParserConfigurationException, SAXException, IOException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final int gdlID = Schemas.getGlobalDownloaderID(globalSchema, pluginName, stmt);
        Map<Integer, DataFileMetaData> downloadsList = new HashMap<Integer, DataFileMetaData>();
        ArrayList<Integer> downloadIDs = new ArrayList<Integer>(0);
        ResultSet rs;

        StringBuilder query = new StringBuilder(String.format(
                "SELECT A.\"DownloadID\", A.\"DateGroupID\", A.\"DataFilePath\", B.\"Year\", B.\"DayOfYear\" FROM \"%1$s\".\"Download\" A, \"%1$s\".\"DateGroup\" B " +
                        "WHERE A.\"GlobalDownloaderID\"=" + gdlID + " AND B.\"DateGroupID\"=A.\"DateGroupID\"",
                        globalSchema
                ));
        if(startDate != null)
        {
            int year = startDate.getYear();
            int dayOfYear = startDate.getDayOfYear();
            query.append(" AND ((B.\"Year\" = " + year + " AND B.\"DayOfYear\" >= " + dayOfYear + ") OR (B.\"Year\" > " + year + "))");
        }
        query.append(";");
        rs = stmt.executeQuery(query.toString());
        if(rs != null)
        {
            while(rs.next())
            {
                downloadIDs.add(rs.getInt("DownloadID"));
                downloadsList.put(rs.getInt("DownloadID"), new DataFileMetaData("Data", rs.getString("DataFilePath"), rs.getInt("Year"), rs.getInt("DayOfYear")));
            }
        }
        rs.close();

        query = new StringBuilder(String.format(
                "SELECT A.\"DownloadID\", A.\"DataName\", A.\"FilePath\", B.\"DateGroupID\", B.\"Year\", B.\"DayOfYear\" " +
                        "FROM \"%1$s\".\"ExtraDownload\" A INNER JOIN \"%1$s\".\"Download\" D ON A.\"DownloadID\"=D.\"DownloadID\" " +
                        "INNER JOIN \"%1$s\".\"DateGroup\" B ON D.\"DateGroupID\"=B.\"DateGroupID\"",
                        globalSchema));

        if(downloadIDs.size() > 0)
        {
            query.append(" WHERE A.\"DownloadID\"=" + downloadIDs.get(0));
            for(int i=1; i < downloadIDs.size(); i++)
            {
                query.append(" OR A.\"DownloadID\"=" + downloadIDs.get(i));
            }
        }
        query.append(";");

        rs = stmt.executeQuery(query.toString());
        if(rs != null)
        {
            DownloadFileMetaData temp;
            ArrayList<DataFileMetaData> extraDownloads;
            while(rs.next())
            {
                temp = downloadsList.get(rs.getInt("DownloadID")).ReadMetaDataForProcessor();
                extraDownloads = new ArrayList<DataFileMetaData>();
                for(DownloadFileMetaData dData : temp.extraDownloads)
                {
                    extraDownloads.add(new DataFileMetaData(dData));
                }
                extraDownloads.add(new DataFileMetaData(rs.getString("DataName"), rs.getString("FilePath"), rs.getInt("Year"), rs.getInt("DayOfYear")));
                downloadsList.put(rs.getInt("DownloadID"), new DataFileMetaData("Data", temp.dataFilePath, temp.year, temp.day, extraDownloads));
            }
        }
        rs.close();
        stmt.close();
        conn.close();

        return new ArrayList<DataFileMetaData>(downloadsList.values());
    }

    /**
     * Adds the given file to the database records and updates statuses for observing LocalDownloaders.
     *
     * @param year  - year the file's data is associated with
     * @param dayOfYear  - day of the year the file's data is associated with
     * @param filePath  - full path to the file
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    protected final void AddDownloadFile(int year, int dayOfYear, String filePath) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final LocalDate lDate = LocalDate.ofYearDay(year, dayOfYear);
        // If inserting a "Data" labeled file then insert its record into the 'Download' table
        if(metaData.name.toLowerCase().equals("data"))
        {
            int gdlID = Schemas.getGlobalDownloaderID(globalSchema, pluginName, stmt);
            int dateGroupID = Schemas.getDateGroupID(globalSchema, lDate, stmt);

            // Insert new download
            String query = String.format(
                    "INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\") VALUES\n" +
                            "(" + gdlID + ", " + dateGroupID + ", '" + filePath + "');",
                            globalSchema
                    );
            stmt.executeUpdate(query);
        }
        // Else, insert the record into the 'ExtraDownload' table
        else
        {
            int gdlID = Schemas.getGlobalDownloaderID(globalSchema, pluginName, stmt);
            int dateGroupID = Schemas.getDateGroupID(globalSchema, lDate, stmt);
            int downloadID = Schemas.getDownloadID(globalSchema, gdlID, dateGroupID, stmt);

            String query = String.format(
                    "INSERT INTO \"%1$s\".\"ExtraDownload\" (\"DownloadID\", \"DataName\", \"FilePath\") VALUES\n" +
                            "(" + downloadID + ", '" + metaData.name + "', '" + filePath + "');",
                            globalSchema
                    );
            stmt.executeUpdate(query);
        }

        stmt.close();
        conn.close();
    }

    private boolean RegisterGlobalDownloader() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, SQLException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final int pluginID = Schemas.getPluginID(globalSchema, pluginName, stmt);

        ResultSet rs;
        rs = stmt.executeQuery("SELECT \"GlobalDownloaderID\" FROM \"" + globalSchema + "\".\"GlobalDownloader\" WHERE \"PluginID\" = " + pluginID + ";");
        if(rs != null && rs.next()) {
            return true;
        }

        return stmt.execute("INSERT INTO \"" + globalSchema + "\".\"GlobalDownloader\" (\"PluginID\") VALUES (" + pluginID + ");");
    }
}
