package version2.prototype.util;

import java.util.ArrayList;

import version2.prototype.util.GdalUtils;

import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.ogr;
import org.gdal.ogr.Layer;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.DataSource;


public class ReadShapefile {

    private DataSource shapefile;
    private ArrayList<String []> featureNameList = new ArrayList<String[]>();

    /* filename:  the input shapefile name
     * throws exception ShapefileException if a shapefile does not exist or cannot be open
     * precondition: none
     * postcondition: a datasource object is created to for the input shapefile
     */

    public ReadShapefile(String filename) throws ShapefileException{
        if (filename != null){

            GdalUtils.register();
            synchronized (GdalUtils.lockObject) {
                shapefile = ogr.Open(filename);
                if (shapefile == null){
                    throw new ShapefileException("The shape file " + filename + " cannot be properly opened.");
                }
            }
        }
        else{
            throw new ShapefileException("The shape file " + filename + " does not exist");
        }
    }


    /* precondition: none
     * postcondition: feature(field) names in each layer are stored in each ArrayList element
     */
    public ArrayList<String []> getFeatureList()
    {
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

        return featureNameList;
    }



}