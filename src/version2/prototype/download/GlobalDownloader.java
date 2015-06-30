package version2.prototype.download;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Observable;

import version2.prototype.ConfigReadException;
import version2.prototype.TaskState;
import version2.prototype.util.PostgreSQLConnection;


/**
 * @author michael.devos
 *
 */
public abstract class GlobalDownloader extends Observable implements Runnable{
    protected GlobalDownloader instance;
    protected TaskState state;
    protected final int ID;
    protected final String pluginName;

    protected GlobalDownloader(TaskState initialState, String pluginName, int myID)
    {
        state = initialState;
        ID = myID;
        this.pluginName = pluginName;
    }

    public abstract GlobalDownloader GetInstance(int myID);

    public abstract void Stop();

    public abstract void Start();

    public int GetID() { return ID; }

    public String GetPluginName() { return pluginName; }

    /**
     * Add the given file and associated information to the appropriate global downloads table.
     *
     * @param projectName  - project schema to look under
     * @param dataName  - name of the downloaded file's data type
     * @param year  - year of the downloaded file
     * @param day  - day of the downloaded file
     * @param filePath  - path to the downloaded file
     * @throws SQLException
     * @throws ParseException
     * @throws ConfigReadException
     * @throws ClassNotFoundException
     */
    protected void AddDownloadFile(String projectName, String dataName, int year, int day, String filePath) throws SQLException,
    ParseException, ConfigReadException, ClassNotFoundException
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
        } finally {
            rs.close();
        }
    }

    @Override
    public void notifyObservers(Object arg0) {
        super.notifyObservers(arg0);
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
    }
}
