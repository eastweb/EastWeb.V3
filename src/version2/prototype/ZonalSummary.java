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
    private final String zone;

    /**
     * Creates a ZonalSummary object pairing of shape file and zone field.
     *
     * @param shapeFile  - shape file to be used in this zonal summary
     * @param field  - zone field to be used in this zonal summary
     */
    public ZonalSummary(String shapeFile, String field, String zone) {
        this.shapeFile = shapeFile;
        this.field = field;
        this.zone = zone;
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

    /**
     * Gets the zone name.
     *
     * @return zone name
     */
    public String GetZone() {
        return zone;
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
