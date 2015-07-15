package version2.prototype.download;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.ConfigReadException;
import version2.prototype.TaskState;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.PostgreSQLConnection;


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


    protected GlobalDownloader(int myID, String pluginName, TaskState initialState, DownloadMetaData metaData, ListDatesFiles listDatesFiles)
    {
        state = initialState;
        ID = myID;
        this.pluginName = pluginName;
        this.metaData = metaData;
        keys = new BitSet(1000);
        udpateStates = new HashMap<Integer, Boolean>(0);
        this.listDatesFiles = listDatesFiles;
    }

    public final void Stop()
    {
        state = TaskState.STOPPED;
    }

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

    public final ArrayList<DataFileMetaData> GetAllDownloadedFiles()
    {

    }

    public final ArrayList<DataFileMetaData> CheckForUpdate(int key)
    {

    }

    public final DataFileMetaData GetSpecificDownload(LocalDate ld)
    {

    }

    /**
     * Add the given file and associated information to the appropriate global downloads table.
     *
     * @param projectName  - project schema to look under
     * @param dataName  - name of the downloaded file's data type
     * @param year  - year of the downloaded file
     * @param day  - day of the downloaded file
     * @param filePath  - path to the downloaded file
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ParseException
     * @throws ConfigReadException
     * @throws ClassNotFoundException
     */
    protected void AddDownloadFile(String dataName, int year, int day, String filePath) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        String tableName = "Download";
        Connection conn = PostgreSQLConnection.getConnection();
        String query = String.format(
                "INSERT INTO \"%1$s\" (\n" +
                        "\"FullPath\",\n" +
                        "\"DateDirectory\",\n" +
                        "\"DataGroupID\"\n" +
                        ") VALUES (\n" +
                        "\"%2$s\",\n" +
                        "?,\n" +
                        "?\n" +
                        ")",
                        tableName,
                        filePath
                );
        PreparedStatement psInsertFile = conn.prepareStatement(query);

        // Get data group ID
        query = String.format(
                "SELECT DataGroupdID FROM \"%1$s\"\n" +
                        "WHERE \"Year\" = ? AND \n" +
                        "\"Day\" = ?",
                        tableName
                );
        PreparedStatement psDG = conn.prepareStatement(query);
        psDG.setString(1, String.valueOf(year));
        psDG.setString(2, String.valueOf(day));
        ResultSet rs = psDG.executeQuery();
        try {
            if(rs.next()) {
                psInsertFile.setString(2, rs.getString(1));
            }
            else
            {
                query = String.format(
                        "INSERT INTO \"%1$s\" (\n" +
                                "\"Year\",\n" +
                                "\"Day\")\n" +
                                "VALUES (" +
                                "%2$d,\n" +
                                "%3$d)",
                                tableName,
                                year,
                                day
                        );
                psDG = conn.prepareStatement(query);
                rs = psDG.executeQuery();
                query = String.format(
                        "SELECT currval(\"%1$s\")",
                        tableName + "_" + tableName + "ID_seq"
                        );
                rs = conn.prepareStatement(query).executeQuery();

                if (rs.next()) {
                    psInsertFile.setString(2, rs.getString(1));
                } else {
                    throw new SQLException("Couldn't get ID of inserted DataGroup row.");
                }
            }
            rs = psInsertFile.executeQuery();

            // Update states
            Iterator<Integer> it = udpateStates.keySet().iterator();
            while(it.hasNext())
            {
                keys.set(it.next());
            }
        } finally {
            rs.close();
        }
    }

    @Override
    public final void notifyObservers(Object arg0) {
        super.notifyObservers(arg0);
    }

    @Override
    public final void notifyObservers() {
        super.notifyObservers();
    }
}
