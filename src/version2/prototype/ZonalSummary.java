package version2.prototype;

import java.io.Serializable;

/**
 * Represents metadata for a single zonal summary. Paring of shape file and zone field.
 *
 * @author michael.devos
 *
 */
public class ZonalSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String shapeFile;
    private final String field;

    /**
     * Creates a ZonalSummary object pairing of shape file and zone field.
     *
     * @param shapeFile  - shape file to be used in this zonal summary
     * @param field  - zone field to be used in this zonal summary
     */
    public ZonalSummary(String shapeFile, String field) {
        this.shapeFile = shapeFile;
        this.field = field;
    }

    /**
     * Gets the paired shape file.
     *
     * @return shape file path
     */
    public String GetShapeFile() {
        return shapeFile;
    }

    /**
     * Gets the paired zone field.
     *
     * @return zone field name
     */
    public String GetField() {
        return field;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "[shapefile=%s, field=%s]",
                shapeFile, field
                );
    }
}
