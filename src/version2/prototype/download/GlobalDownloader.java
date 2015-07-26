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
     * Sets this GlobalDownloader super to have an initial state (TaskState) of STOPPED.
     *
     * @param myID  - unique identifier ID of this GlobalDownloader (this is addressed by EASTWebManager and has nothing to do with the internal database IDs)
     * @param pluginName
     * @param metaData
     * @param listDatesFiles
     */
    protected GlobalDownloader(int myID, String pluginName, DownloadMetaData metaData, ListDatesFiles listDatesFiles)
    {
        state = TaskState.STOPPED;
        ID = myID;
        this.pluginName = pluginName;
        this.metaData = metaData;
        keys = new BitSet(1000);
        udpateStates = new TreeMap<Integer, Boolean>();
        this.listDatesFiles = listDatesFiles;
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
    public final ArrayList<DataFileMetaData> GetAllDownloadedFiles() throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException
    {
        return Schemas.getAllDownloadedFiles(Config.getInstance().getGlobalSchema(), pluginName);
    }

    protected final void AddDownloadFile(int year, int dayOfYear, String filePath) throws ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        if(metaData.name.toLowerCase().equals("data"))
        {
            Schemas.insertIntoDownloadTable(Config.getInstance().getGlobalSchema(), GetPluginName(), LocalDate.ofYearDay(year, dayOfYear), filePath);
        }
        else
        {
            Schemas.insertIntoExtraDownloadTable(Config.getInstance().getGlobalSchema(), GetPluginName(), LocalDate.ofYearDay(year, dayOfYear), metaData.name, filePath);
        }

        // Update States
        Iterator<Integer> it = udpateStates.keySet().iterator();
        while(it.hasNext())
        {
            keys.set(it.next());
        }
    }
}
