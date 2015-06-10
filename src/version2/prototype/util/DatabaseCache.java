package version2.prototype.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import version2.prototype.ConfigReadException;

public class DatabaseCache {
    static final Pattern filePathPattern = Pattern.compile("(\\w+)\\\\(\\w+)\\\\(\\w+)\\\\(\\d{4})\\\\(\\d{3})\\\\");   // To save time
    static final Pattern dateStringPattern = Pattern.compile("(\\d{4})\\\\(\\d{3})\\\\");   // To save time

    private DatabaseCache(){}

    public static ArrayList<DataFileMetaData> GetAvailableFiles(String projectName, String pluginName, String tableName) throws
    SQLException, ConfigReadException, ClassNotFoundException
    {
        String schemaName = Schema.getSchemaName(projectName, pluginName);
        ArrayList<DataFileMetaData> files = new ArrayList<DataFileMetaData>();
        Connection conn = PostgreSQLConnection.getConnection();
        conn.createStatement().execute("BEGIN");
        String query = String.format(
                "SELECT \"A\".\"%1$sID\", \"A\".\"FullPath\", \"A\".\"DateDirectory\", \"A\".\"DataGroupID\", \"B\".\"Year\", \"B\".\"Day\"\n" +
                        "FROM \"%2$s\".\"%1$s\" \"A\" INNER JOIN \"%2$s\".\"DateGroup\" \"B\" ON (\"A\".\"DataGroupID\" = \"B\".\"DataGroupID\")\n" +
                        "WHERE \"Retrieved\" != TRUE\n" +
                        "FOR UPDATE",
                        tableName,
                        schemaName
                );
        final PreparedStatement ps = conn.prepareStatement(query);
        final ResultSet rs = ps.executeQuery();
        ArrayList<Integer> rows = new ArrayList<Integer>();
        try {
            while(rs.next()) {
                files.add(new DataFileMetaData(rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5), rs.getInt(6)));
                rows.add(rs.getInt(1));
            }

            for(int row : rows)
            {
                conn.createStatement().execute(String.format(
                        "UPDATE \"%1$s\".\"%2$s\"\n" +
                                "SET \"Retrieved\" = TRUE\n" +
                                "WHERE \"%2$sID\" = %3$d",
                                tableName,
                                schemaName,
                                row
                        ));
            }
            conn.createStatement().execute("COMMIT");
        } catch(Exception e) {
            conn.createStatement().execute("ROLLBACK");
        } finally {
            rs.close();
        }

        return files;
    }

    public static void AddDownloadFile(String projectName, String dataName, int year, int day, String filePath) throws SQLException,
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

    public static void CacheFile(String filePath) throws SQLException, ParseException, ConfigReadException, ClassNotFoundException
    {
        String projectName, pluginName, tableName, dateDirectory;
        int year, day;

        // Parse out date directory
        Matcher matcher = filePathPattern.matcher(filePath);
        if(matcher.find()) {
            projectName = matcher.group(1);
            dateDirectory = filePath.substring(0, matcher.end());

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

        String schemaName = Schema.getSchemaName(projectName, pluginName);
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
                        "\"Day\" = ?",
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
                                "\"Day\")\n" +
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
        } finally {
            rs.close();
        }
    }

    public static DataFileMetaData Parse(String fullPath) throws SQLException, ParseException, ClassNotFoundException
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

        String schemaName = Schema.getSchemaName(projectName, pluginName);
        Connection conn = PostgreSQLConnection.getConnection();

        // Get data group ID
        String query = String.format(
                "SELECT DataGroupdID FROM \"%1$s\".\"%2$s\"\n" +
                        "WHERE \"Year\" = ? AND \n" +
                        "\"Day\" = ?",
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
                                "\"Day\")\n" +
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
}