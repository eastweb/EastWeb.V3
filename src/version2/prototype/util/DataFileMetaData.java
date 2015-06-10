package version2.prototype.util;

public class DataFileMetaData {
    public final String fullPath;
    public final String dateDirectory;
    public final int dataGroupID;
    public final int year;
    public final int day;

    public DataFileMetaData(String fullPath, String dateDirectory, int dataGroupID, int year, int day)
    {
        this.fullPath = fullPath;
        this.dateDirectory = dateDirectory;
        this.dataGroupID = dataGroupID;
        this.year = year;
        this.day = day;
    }
}
