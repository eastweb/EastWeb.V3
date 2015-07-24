package version2.prototype.util;


/**
 *
 * @author michael.devos
 *
 */
public class IndicesFileMetaData {
    public final String dataFilePath;
    public final int year;
    public final int day;
    public final String indexNm;

    /**
     * Creates a IndicesFileMetaData object initialized to the given values.
     *
     * @param dataFilePath  - full path to the data file
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     * @param indexNm  - the environmental index associated to the data file
     */
    public IndicesFileMetaData(String dataFilePath, int year, int day, String indexNm) {
        this.dataFilePath = dataFilePath;
        this.year = year;
        this.day = day;
        this.indexNm = indexNm;
    }
}
