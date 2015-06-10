package version2.prototype;

import java.io.Serializable;

public class ZonalSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String shapeFile;
    private final String field;

    public ZonalSummary(String shapeFile, String field) {
        this.shapeFile = shapeFile;
        this.field = field;
    }

    public String GetShapeFile() {
        return shapeFile;
    }

    public String GetField() {
        return field;
    }

    @Override
    public String toString() {
        return String.format(
                "[shapefile=%s, field=%s]",
                shapeFile, field
                );
    }
}
