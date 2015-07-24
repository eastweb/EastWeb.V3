package version2.prototype.util;


/**
 *
 * @author michael.devos
 *
 */
public class ProcessorFileMetaData {
    public final String dataFilePath;
    public final int year;
    public final int day;

    /**
     * Creates a ProcessorFileMetaData object initialized to the given values.
     *
     * @param dataFilePath  - full path to the data file
     * @param year  - the Gregorian year the data file is relevant to
     * @param day  - the Gregorian day of the year the data file is relevant to
     */
    public ProcessorFileMetaData(String dataFilePath, int year, int day) {
        this.dataFilePath = dataFilePath;
        this.year = year;
        this.day = day;
    }
}
