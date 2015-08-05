package version2.prototype.processor.TRMM3B42RT;

import version2.prototype.processor.ProcessData;
import version2.prototype.processor.Reproject;

// For reflection
public class TRMM3B42RTReproject extends Reproject{

    public TRMM3B42RTReproject(ProcessData data) {
        super(data);
    }

    @Override
    public void setInputWKT() {
        wktStr = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\"],SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";

    }

}
