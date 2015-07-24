package version2.prototype.processor;

import java.io.File;
import java.util.Arrays;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Transformer;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

import version2.prototype.util.GdalUtils;

public class Clip
{
    //locations for the input files. for this step, will only use inputFolders[0]
    protected String[] inputFolders;
    //location for the output file
    protected String outputFolder;
    protected File inputFolder;
    // the files in the input folder for composition
    protected File [] inputFiles;
    // mask file
    protected File shapeFile;

    public Clip(ProcessData data)
    {
        inputFolders = data.getInputFolders();
        outputFolder = data.getOutputFolder();

        //check if there are more than one input file in the given folder
        inputFolder = new File(inputFolders[0]);
        File[] listOfFiles = inputFolder.listFiles();
        assert (listOfFiles.length >= 1);

        //set the input files
        //We assume that each file is a GDAL supported raster file with one band
        inputFiles = listOfFiles;

        shapeFile = new File(data.getShapefile());
    }


    // clip all the files in the input folder
    public void clipFiles() throws Exception {

        GdalUtils.register();

        synchronized (GdalUtils.lockObject)
        {
            for (File mInput : inputFiles)
            {
                String filename = mInput.getName();
                File mOutput = new File(outputFolder,filename);

                Dataset rasterDS = gdal.Open(mInput.getPath());
                DataSource featureDS = ogr.Open(shapeFile.getPath());
                Layer featureLyr = featureDS.GetLayer(0);

                final int pixelSize = (int) Math.abs(rasterDS.GetGeoTransform()[1]); // FIXME: getting pixel size won't work for some datasets
                //System.out.println("PIXEL SIZE: " + pixelSize);

                double[] featureExtent = featureLyr.GetExtent();
                //System.out.println(Arrays.toString(featureExtent));

                Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                        mOutput.getPath(),
                        (int) Math.ceil((featureExtent[1]-featureExtent[0])/pixelSize),
                        (int) Math.ceil((featureExtent[3]-featureExtent[2])/pixelSize),
                        1,
                        gdalconst.GDT_Int16
                        );

                outputDS.SetProjection(featureLyr.GetSpatialRef().ExportToWkt());
                outputDS.SetGeoTransform(new double[] {
                        featureExtent[0], pixelSize, 0,
                        featureExtent[2] + outputDS.GetRasterYSize()*pixelSize, 0, -pixelSize
                });

                //System.out.println(Arrays.toString(outputDS.GetGeoTransform()));

                // Get pixel coordinate in output raster of corner of zone raster
                Transformer transformer = new Transformer(outputDS, rasterDS, null);

                double[] point = new double[] {-0.5, -0.5, 0}; // Location of corner of first zone raster pixel

                transformer.TransformPoint(0, point);
                //int xOffset = (int) Math.round(point[0]);
                //int yOffset = (int) Math.round(point[1]);

                Dataset maskDS = gdal.GetDriverByName("MEM").Create(
                        "",
                        (int) Math.ceil((featureExtent[1]-featureExtent[0])/pixelSize),
                        (int) Math.ceil((featureExtent[3]-featureExtent[2])/pixelSize),
                        1,
                        gdalconst.GDT_Int16);

                maskDS.SetProjection(featureLyr.GetSpatialRef().ExportToWkt());
                //zoneDS.SetProjection(rasterDS.GetProjection());
                maskDS.SetGeoTransform(new double[] {
                        featureExtent[0], pixelSize, 0,
                        featureExtent[2] + outputDS.GetRasterYSize()*pixelSize, 0, -pixelSize
                });

                maskDS.GetRasterBand(1).Fill(0); // FIXME: necessary?

                gdal.RasterizeLayer(maskDS, new int[] {1}, featureLyr);

                int[] maskArray = new int[maskDS.GetRasterXSize()];
                int[] rasterArray = new int[maskDS.GetRasterXSize()];

                // FIXME: optimize it!
                for (int y=0; y<maskDS.GetRasterYSize(); y++) {
                    maskDS.GetRasterBand(1).ReadRaster(0, y, maskDS.GetRasterXSize(), 1, maskArray);
                    /* removed offsets from the parameters in the following statement
                     *  rasterDS.GetRasterBand(1).ReadRaster(xOffset, yOffset + y, maskDS.GetRasterXSize(), 1, rasterArray);
                     *  8/28/13 by J. Hu
                     */

                    rasterDS.GetRasterBand(1).ReadRaster(0, y, maskDS.GetRasterXSize(), 1, rasterArray);
                    for (int i=0; i<maskArray.length; i++) {
                        if (maskArray[i] == 0)
                        {
                            rasterArray[i] = GdalUtils.NoValue;
                        }
                    }

                    outputDS.GetRasterBand(1).WriteRaster(0, y, maskDS.GetRasterXSize(), 1, rasterArray);
                }

                // Calculate statistics
                for (int i=1; i<=outputDS.GetRasterCount(); i++) {
                    Band band = outputDS.GetRasterBand(i);

                    band.SetNoDataValue(GdalUtils.NoValue); // FIXME
                    band.ComputeStatistics(false);
                }

                maskDS.GetRasterBand(1).ComputeStatistics(false);

                maskDS.delete();
                rasterDS.delete();
                outputDS.delete();
            }
        }
    }

}