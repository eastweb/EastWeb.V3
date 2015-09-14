package version2.prototype.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;

import version2.prototype.Projection;
import version2.prototype.Projection.Datum;
import version2.prototype.Projection.ProjectionType;
import version2.prototype.Projection.ResamplingType;
import version2.prototype.util.GdalUtils;

public class TestReprojection
{
    public static void main(String [] args)
    {
        //        GdalUtils.register();
        gdal.AllRegister();

        synchronized (GdalUtils.lockObject)
        {

            Projection p = new Projection(ProjectionType.TRANSVERSE_MERCATOR, ResamplingType.BILINEAR,
                    Datum.WGS84, 1000, 0.0, 0.0, 0.9996, 39.0, 500000.0, 0.0, 0.0);

            GdalUtils.project(new File("D:\\project\\band20.tif"),
                    //"D:\\project\\day.tif",
                    //"D:\\testProjects\\TW\\settings\\shapefiles\\TW_DIS_F_P_Dis_REGION\\TW_DIS_F_P_Dis_REGION.shp",
                    //"D:\\testProjects\\Amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp",
                    "D:\\testProjects\\GA\\shapefiles\\GA_Counties\\GA_Counties.shp",
                    p, new File("D:\\project\\noah_p.tif"));

            //extract NOAH
            //            String noah = "D:\\project\\download\\NOAH\\noah_2015_0604.grb";
            //            String outFile = "D:\\project\\Noah_b19.tif";
            //            GdalUtils.register();
            //            synchronized (GdalUtils.lockObject)
            //            {
            //                Dataset inputDS = gdal.Open(noah);
            //                int xSize = inputDS.getRasterXSize();
            //                int ySize = inputDS.getRasterYSize();
            //                Dataset outputDS = gdal.GetDriverByName("GTiff").
            //                        Create(
            //                                outFile,
            //                                xSize, ySize,
            //                                1,
            //                                gdalconst.GDT_Float32
            //                                );
            //
            //                Band b20 = inputDS.GetRasterBand(19);
            //
            //                double[] arr = new double[xSize * ySize];
            //                b20.ReadRaster(0,  0 , xSize, ySize, arr);
            //
            //                System.out.println("original prj ref: " + inputDS.GetProjection());
            //                String outputProjStr = "+proj=longlat +datum=WGS84 +no_defs";
            //                //String wktStr = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\"],SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
            //                SpatialReference output = new SpatialReference();
            //                output.ImportFromProj4(outputProjStr);
            //
            //                outputDS.SetProjection(output.ExportToWkt());
            //
            //                //outputDS.SetProjection(wktStr);
            //                System.out.println(outputDS.GetProjection());
            //                double [] geoTrans = inputDS.GetGeoTransform();
            //                System.out.println(geoTrans[0] + " : " + geoTrans[1] + " : " + geoTrans[2] + " : " + geoTrans[3] + " : " + geoTrans[4] + " : " + geoTrans[5]);
            //
            //                outputDS.SetGeoTransform(inputDS.GetGeoTransform());
            //
            //                outputDS.GetRasterBand(1).WriteRaster(0, 0, xSize, ySize, arr);
            //                outputDS.delete();
            //                inputDS.delete();
            //            }
            //
            //            projection("D:\\project\\Noah_b19.tif",
            //                    //"D:\\project\\band20.tif",
            //                    //"D:\\testProjects\\TW\\settings\\shapefiles\\TW_DIS_F_P_Dis_REGION\\TW_DIS_F_P_Dis_REGION.shp",
            //                    "D:\\testProjects\\Amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp",
            //                    p, new File("D:\\project\\Noah_b19p.tif"));
        }
    }



}
