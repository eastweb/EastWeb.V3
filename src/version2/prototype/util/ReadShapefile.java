package version2.prototype.util;

import java.util.ArrayList;

import version2.prototype.util.GdalUtils;

import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.ogr;
import org.gdal.ogr.Layer;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.DataSource;


public class ReadShapefile {

    private ArrayList<String []> featureNameList = new ArrayList<String[]>();

    // filename:  the input shapefile name
    public ReadShapefile(String filename){

        if (filename != null){

            GdalUtils.register();
            DataSource shapefile;
            synchronized (GdalUtils.lockObject) {
                shapefile = ogr.Open(filename);
            }

            if (shapefile != null){
                synchronized (GdalUtils.lockObject) {
                    for (int iLayer=0; iLayer<shapefile.GetLayerCount(); iLayer++) {
                        Layer layer = shapefile.GetLayer(iLayer);
                        FeatureDefn layerDefn = layer.GetLayerDefn();

                        int count = layerDefn.GetFieldCount();

                        String [] featureNames = new String[count];

                        for (int iFeature=0; iFeature<count; iFeature++) {
                            FieldDefn fieldDefn = layerDefn.GetFieldDefn(iFeature);
                            String type = fieldDefn.GetFieldTypeName(fieldDefn.GetFieldType()).toLowerCase();
                            if (type.equals("string") || type.equals("integer")) {
                                featureNames[iFeature] = layerDefn.GetFieldDefn(iFeature).GetName();
                            }
                        }

                        featureNameList.add(featureNames);
                    }
                }
            }
        }
    }

    public ArrayList<String []> getFeatureList()
    {
        return featureNameList;
    }


}
