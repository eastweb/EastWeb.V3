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
        GdalUtils.register();

        synchronized (GdalUtils.lockObject)
        {

            Projection p = new Projection(ProjectionType.TRANSVERSE_MERCATOR, ResamplingType.BILINEAR,
                    Datum.WGS84, 1000, 0.0, 0.0, 0.9996, 39.0, 500000.0, 0.0, 0.0);

            /*project("D:\\project\\TRMM_t.tif",
                    //"D:\\testProjects\\TW\\settings\\shapefiles\\TW_DIS_F_P_Dis_REGION\\TW_DIS_F_P_Dis_REGION.shp",
                    "D:\\testProjects\\Amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp",
                    p, new File("D:\\project\\TRMM_p.tif"));*/


            projection("D:\\project\\band20.tif",
                    //"D:\\testProjects\\TW\\settings\\shapefiles\\TW_DIS_F_P_Dis_REGION\\TW_DIS_F_P_Dis_REGION.shp",
                    "D:\\testProjects\\Amhara\\settings\\shapefiles\\Woreda_new\\Woreda_new.shp",
                    p, new File("D:\\project\\band2p.tif"));
        }
    }

    private static void projection(String input, String masterShapeFile, Projection projection, File output)
    {
        String wktStr = "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\"],SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";

        assert (masterShapeFile != null);
        GdalUtils.register();
        synchronized (GdalUtils.lockObject)
        {
            Dataset inputDS = gdal.Open(input);
            SpatialReference inputRef = new SpatialReference();

            inputRef.ImportFromWkt(wktStr);
            //inputRef.ImportFromWkt("GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433],AUTHORITY[\"EPSG\",4326]]");

            //inputRef.ImportFromWkt("GEOGCS[\"GCS_Undefined\",DATUM[\"Undefined\",SPHEROID[\"User_Defined_Spheroid\",6371007.181,0.0]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Sinusoidal\"],PARAMETER[\"False_Easting\",0.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",0.0],UNIT[\"Meter\",1.0]");
            // FIXME: abstract it somehow?
            // inputRef.ImportFromWkt("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\"],SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");

            inputDS.SetProjection(inputRef.ExportToWkt());

            DataSource feature = ogr.Open(masterShapeFile);

            // Find union of extents
            double[] extent = null;
            try{
                extent = feature.GetLayer(0).GetExtent(); // Ordered: left, right, bottom, top
            }catch(Exception e)
            {
                System.out.println(e.toString());
                for(StackTraceElement el : e.getStackTrace())
                {
                    System.out.println(el.toString());
                }
            }

            double left = extent[0];
            double right = extent[1];
            double bottom = extent[2];
            double top = extent[3];

            Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                    output.getPath(),
                    (int) Math.ceil((right-left)/projection.getPixelSize()),
                    (int) Math.ceil((top-bottom)/projection.getPixelSize()),
                    1,
                    gdalconst.GDT_Float32
                    );

            String outputProjection = feature.GetLayer(0).GetSpatialRef().ExportToWkt();

            System.out.println("Reproject: input : " + inputRef.ExportToWkt());
            System.out.println("Reproject: output : " + outputProjection);
            System.out.println("Reproject: GeoTransform: " + left + " : " + top);

            outputDS.SetProjection(outputProjection);
            outputDS.SetGeoTransform(new double[] {
                    left, (projection.getPixelSize()), 0,
                    top, 0, -(double)(projection.getPixelSize())
            });

            // get resample argument
            int resampleAlg = -1;
            ResamplingType resample = projection.getResamplingType();
            switch (resample) {
            case NEAREST_NEIGHBOR:
                resampleAlg = gdalconst.GRA_NearestNeighbour;
                break;
            case BILINEAR:
                resampleAlg = gdalconst.GRA_Bilinear;
                break;
            case CUBIC_CONVOLUTION:
                resampleAlg = gdalconst.GRA_CubicSpline;
            }

            System.out.println("Reproject image return : " + gdal.ReprojectImage(inputDS, outputDS, wktStr, outputProjection, resampleAlg));
            outputDS.GetRasterBand(1).ComputeStatistics(false);
            outputDS.delete();
            inputDS.delete();

        }
    }

}
