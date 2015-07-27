package version2.prototype.download;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
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
    protected final String pluginName;
    protected final DownloadMetaData metaData;
    protected final ListDatesFiles listDatesFiles;

    private static BitSet keys;
    private Map<Integer, Boolean> udpateStates;

    /**
     * Sets this GlobalDownloader super to have an initial state (TaskState) of STOPPED and registers the GlobalDownloader in the database.
     *
     * @param myID  - unique identifier ID of this GlobalDownloader (this is addressed by EASTWebManager and has nothing to do with the internal database IDs)
     * @param pluginName
     * @param metaData
     * @param listDatesFiles
     * @throws SQLException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws ClassNotFoundException
     */
    protected GlobalDownloader(int myID, String pluginName, DownloadMetaData metaData, ListDatesFiles listDatesFiles) throws ClassNotFoundException, ParserConfigurationException, SAXException,
    IOException, SQLException
    {
        state = TaskState.STOPPED;
        ID = myID;
        this.pluginName = pluginName;
        this.metaData = metaData;
        keys = new BitSet(1000);
        udpateStates = new TreeMap<Integer, Boolean>();
        this.listDatesFiles = listDatesFiles;
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



    public final LocalDate GetOriginDate() { return metaData.originDate; }

    public final int GetID() { return ID; }

    public final String GetPluginName() { return pluginName; }

    public final int GetAnUpdateKey()
    {
        synchronized(keys)
        {
            int registerKey = keys.nextClearBit(0);
            keys.set(registerKey);
            udpateStates.put(registerKey, true);
            return registerKey;
        }
    }

    public final void ReleaseUpdateKey(int key)
    {
        synchronized(keys)
        {
            if((key >= 0) && (key < keys.size()))
            {
                keys.clear(key);
                udpateStates.remove(key);
            }
        }
    }

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
    protected final ArrayList<DataFileMetaData> GetAllDownloadedFiles() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final String globalEASTWebSchema = Config.getInstance().getGlobalSchema();
        final int gdlID = Schemas.getGlobalDownloaderID(globalEASTWebSchema, pluginName, stmt);
        Map<Integer, DataFileMetaData> downloadsList = new HashMap<Integer, DataFileMetaData>();
        ArrayList<Integer> downloadIDs = new ArrayList<Integer>(0);
        ResultSet rs;

        rs = stmt.executeQuery(String.format(
                "SELECT A.\"DownloadID\", A.\"DateGroupID\", A.\"DataFilePath\", B.\"Year\", B.\"DayOfYear\" FROM \"%1$s\".\"Download\" A, \"%1$s\".\"DateGroup\" B " +
                        "WHERE A.\"GlobalDownloaderID\"=" + gdlID + " AND B.\"DateGroupID\"=A.\"DateGroupID\";",
                        globalEASTWebSchema
                ));
        if(rs != null)
        {
            while(rs.next())
            {
                downloadIDs.add(rs.getInt("DownloadID"));
                downloadsList.put(rs.getInt("DownloadID"), new DataFileMetaData("Data", rs.getString("DataFilePath"), rs.getInt("Year"), rs.getInt("DayOfYear")));
            }
        }
        rs.close();

        StringBuilder query = new StringBuilder(String.format(
                "SELECT A.\"DownloadID\", A.\"DataName\", A.\"FilePath\", B.\"DateGroupID\", B.\"Year\", B.\"DayOfYear\" " +
                        "FROM \"%1$s\".\"ExtraDownload\" A INNER JOIN \"%1$s\".\"Download\" D ON A.\"DownloadID\"=D.\"DownloadID\" " +
                        "INNER JOIN \"%1$s\".\"DateGroup\" B ON D.\"DateGroupID\"=B.\"DateGroupID\" WHERE ",
                        globalEASTWebSchema));

        if(downloadIDs.size() > 0)
        {
            query.append("A.\"DownloadID\"=" + downloadIDs.get(0));
            for(int i=1; i < downloadIDs.size(); i++)
            {
                query.append(" OR A.\"DownloadID\"=" + downloadIDs.get(i));
            }
        }

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
        final String globalEASTWebSchema = Config.getInstance().getGlobalSchema();
        final LocalDate lDate = LocalDate.ofYearDay(year, dayOfYear);

        // If inserting a "Data" labeled file then insert its record into the 'Download' table
        if(metaData.name.toLowerCase().equals("data"))
        {
            int gdlID = Schemas.getGlobalDownloaderID(globalEASTWebSchema, pluginName, stmt);
            int filesPerDay = Schemas.getFilesPerDayValue(globalEASTWebSchema, pluginName, stmt);
            int dateGroupID = Schemas.getDateGroupID(globalEASTWebSchema, lDate, stmt);

            // Insert new download
            String query = String.format(
                    "INSERT INTO \"%1$s\".\"Download\" (\"GlobalDownloaderID\", \"DateGroupID\", \"DataFilePath\") VALUES\n" +
                            "(" + gdlID + ", " + dateGroupID + ", '" + filePath + "');",
                            globalEASTWebSchema
                    );
            stmt.executeUpdate(query);

            // Check if all files downloaded for any additional days
            Map<Integer, Integer> countOfDates = new TreeMap<Integer, Integer>();
            ResultSet rs;
            rs = stmt.executeQuery("SELECT \"DateGroupID\", COUNT(\"DateGroupID\") AS \"DateGroupIDCount\" FROM \"" + globalEASTWebSchema + "\".\"Download\" " +
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
            rs = stmt.executeQuery("SELECT B.\"DateGroupID\", COUNT(B.\"DateGroupID\") AS \"DateGroupIDCount\" FROM \"" + globalEASTWebSchema + "\".\"ExtraDownload\" A " +
                    "INNER JOIN \"" + globalEASTWebSchema + "\".\"Download\" B ON A.\"DownloadID\" = B.\"DownloadID\" " +
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
            ArrayList<Integer> datesCompleted = new ArrayList<Integer>();
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
                StringBuilder update = new StringBuilder("UPDATE \"" + globalEASTWebSchema + "\" SET \"Completed\" = TRUE WHERE \"DateGroupID\" = " + datesCompleted.get(0));
                for(int i=1; i < datesCompleted.size(); i++)
                {
                    update.append(" OR \"DateGroupID\" = " + datesCompleted.get(i));
                }
                update.append(";");
                stmt.executeUpdate(update.toString());
            }
        }
        // Else, insert the record into the 'ExtraDownload' table
        else
        {
            int gdlID = Schemas.getGlobalDownloaderID(globalEASTWebSchema, pluginName, stmt);
            int dateGroupID = Schemas.getDateGroupID(globalEASTWebSchema, lDate, stmt);
            int downloadID = Schemas.getDownloadID(globalEASTWebSchema, gdlID, dateGroupID, stmt);

            String query = String.format(
                    "INSERT INTO \"%1$s\".\"ExtraDownload\" (\"DownloadID\", \"DataName\", \"FilePath\") VALUES\n" +
                            "(" + downloadID + ", '" + metaData.name + "', '" + filePath + "');",
                            globalEASTWebSchema
                    );
            stmt.executeUpdate(query);
        }

        // Update States
        Iterator<Integer> it = udpateStates.keySet().iterator();
        while(it.hasNext())
        {
            keys.set(it.next());
        }

        stmt.close();
        conn.close();
    }

    private boolean RegisterGlobalDownloader() throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, SQLException
    {
        final Connection conn = PostgreSQLConnection.getConnection();
        final Statement stmt = conn.createStatement();
        final String globalEASTWebSchema = Config.getInstance().getGlobalSchema();
        final int pluginID = Schemas.getPluginID(globalEASTWebSchema, pluginName, stmt);

        ResultSet rs;
        rs = stmt.executeQuery("SELECT \"GlobalDownloaderID\" FROM \"" + globalEASTWebSchema + "\".\"GlobalDownloader\" WHERE \"PluginID\" = " + pluginID + ";");
        if(rs != null && rs.next()) {
            return true;
        }

        return stmt.execute("INSERT INTO \"" + globalEASTWebSchema + "\".\"GlobalDownloader\" (\"PluginID\") VALUES (" + pluginID + ");");
    }
}
