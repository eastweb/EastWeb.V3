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
        String rawFile = "D:\\project\\NLDAS_NOAH0125_H.A20150604.0000.002.grb";
        GdalUtils.register();

        synchronized (GdalUtils.lockObject)
        {
            Dataset raw = gdal.Open(rawFile);
            Band band20 = raw.GetRasterBand(20);
            Dataset out = gdal.GetDriverByName("GTiff").Create(
                    "D:\\project\\band20.tif",
                    band20.GetXSize(), band20.GetYSize(),
                    1, gdalconst.GDT_Float32);
            out.SetGeoTransform(raw.GetGeoTransform());
            out.SetProjection(raw.GetProjection());
            out.SetMetadata(raw.GetMetadata_Dict());

            int xSize = band20.GetXSize() ; int ySize =band20.GetYSize();

            System.out.println(xSize + "  " + ySize);

            double[] bandArr = new double[xSize * ySize];


            //            for (int j = 0; j <=10; j++) {
            //                System.out.println(bandArr[65 * j + j]);
            //            }

            band20.ReadRaster(0, 0, xSize, ySize, bandArr);

            out.GetRasterBand(1).WriteRaster(0, 0, xSize, ySize, bandArr);

            out.delete();
            raw.delete();


            Projection p = new Projection(ProjectionType.TRANSVERSE_MERCATOR, ResamplingType.BILINEAR,
                    Datum.WGS84, 1000, 0.0, 0.0, 0.9996, 39.0, 500000.0, 0.0, 0.0);

            projection("D:\\project\\band20.tif",
                    "D:\\testProjects\\TW\\settings\\shapefiles\\TW_DIS_F_P_Dis_REGION\\TW_DIS_F_P_Dis_REGION.shp",
                    p, new File("D:\\project\\band20_p.tif"));
        }

    }

    private static void projection(String input, String masterShapeFile, Projection projection, File output)
    {
        assert (masterShapeFile != null);
        GdalUtils.register();

        synchronized (GdalUtils.lockObject)
        {
            // Load input file and features
            Dataset inputDS = gdal.Open(input);
            if(inputDS != null)
            {
                // System.out.println(inputDS.GetProjectionRef().toString());
                // SpatialReference inputRef = new SpatialReference();

                /* Original code : takes an array of shape files
                List<DataSource> features = new ArrayList<DataSource>();
                for (String filename : project.getShapeFiles()) {
                    features.add(ogr.Open(new File(DirectoryLayout
                            .getSettingsDirectory(project), filename).getPath()));
                }
                 */

                List<DataSource> features = new ArrayList<DataSource>();
                features.add(ogr.Open(new File(masterShapeFile).getPath()));

                // Find union of extents
                double[] extent = features.get(0).GetLayer(0).GetExtent(); // Ordered:
                // left,
                // right,
                // bottom,
                // top
                // System.out.println(Arrays.toString(extent));
                double left = extent[0];
                double right = extent[1];
                double bottom = extent[2];
                double top = extent[3];
                System.out.println("reporject : " + left + " : " + right+ " : " + bottom+ " : " + top );
                for (int i = 1; i < features.size(); i++) {
                    extent = features.get(i).GetLayer(0).GetExtent();
                    if (extent[0] < left) {
                        left = extent[0];
                    } else if (extent[1] > right) {
                        right = extent[1];
                    } else if (extent[2] < bottom) {
                        bottom = extent[2];
                    } else if (extent[3] > top) {
                        top = extent[3];
                    }
                }

                // Project to union of extents
                Dataset outputDS =
                        gdal.GetDriverByName("GTiff").Create(
                                output.getPath(),
                                (int) Math.ceil((right - left)
                                        / (projection.getPixelSize())),
                                        (int) Math.ceil((top - bottom)
                                                / (projection.getPixelSize())),
                                                1, gdalconst.GDT_Float32);


                // TODO: get projection from project info, and get transform from
                // shape file
                String outputProjection = projection.toString();
                outputDS.SetProjection(outputProjection);


                outputDS.SetGeoTransform(new double[] { left,
                        (projection.getPixelSize()), 0, top, 0,
                        - (double)(projection.getPixelSize()) });

                /*
                String outputProjection =
                        features.get(0).GetLayer(0).GetSpatialRef().ExportToWkt();
                outputDS.SetProjection(outputProjection);
                outputDS.SetGeoTransform(new double[] { left,
                        (projection.getPixelSize()), 0, top, 0,
                        - (double)(projection.getPixelSize()) });
                 */

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
                System.out.println("Reproject : " +resampleAlg);
                gdal.ReprojectImage(inputDS, outputDS, null, null, resampleAlg);
                outputDS.GetRasterBand(1).ComputeStatistics(true);


                Band b = outputDS.GetRasterBand(1);
                int x = b.getXSize(); int y = b.getYSize();
                double [] testArr = new double[x * y];
                b.ReadRaster(0, 0, x, y, testArr);
                for (int i = 0; i < 20; i++) {
                    for (int j = 0; j<20; j++) {
                        System.out.println(testArr[i * j + j]);
                    }
                }


                outputDS.delete();
                inputDS.delete();
            }
        }
    }

}
