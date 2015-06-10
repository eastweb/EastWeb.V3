package version2.prototype.util;

public class CachedDataFile {
    public final int rowID;
    public final String fullPath;
    public final String dateDirectory;
    public final int dataGroupID;
    public final int day;
    public final int year;

    public CachedDataFile(int rowID, String fullPath, String dateDirectory, int dataGroupID, int day, int year)
    {
        this.rowID = rowID;
        this.fullPath = fullPath;
        this.dateDirectory = dateDirectory;
        this.dataGroupID = dataGroupID;
        this.day = day;
        this.year = year;
    }
}
