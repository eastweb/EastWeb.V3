package version2.prototype.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;

import version2.prototype.Projection;
import version2.prototype.Projection.ResamplingType;
import version2.prototype.util.GdalUtils;

/* Author: Y. L.
 *
 * Reproject the file from original projection to the specified projection
 */

public class Reproject {
    //locations for the input files. for this step, will only have one folder
    private String [] inputFolders;
    //location for the output file
    private String outputFolder;
    private File inputFolder;
    // the files in the input folder
    private File [] inputFiles;
    private String shapefile;
    private Projection projection;

    public Reproject(ProcessData data) {
        inputFolders = data.getInputFolders();
        outputFolder = data.getOutputFolder();
        shapefile = data.getShapefile();
        projection = data.getProjection();

        //check if there is at least one input file in the given folder
        inputFolder = new File(inputFolders[0]);
        File[] listOfFiles = inputFolder.listFiles();
        assert (listOfFiles.length >1);
        //set the input files
        inputFiles = listOfFiles;
    }

    // run method for the scheduler
    public void run() throws Exception{

        //create outputDirectory
        File outputDir = new File(outputFolder);
        if (!outputDir.exists())
        {   FileUtils.forceMkdir(outputDir); }

        reprojectFiles();

        // remove the input folder
        FileUtils.deleteDirectory(inputFolder);

    }

    // reproject all the input Files and save them to the outputFolder
    private void reprojectFiles()  {
        for (File f : inputFiles) {
            String fileName = f.getName();
            File outputFile = new File (outputFolder, fileName);
            // reproject
            // GdalUtils.project(f, shapefile, projection, outputFile);
            projection(f.getPath(), shapefile, projection, outputFile);
        }

    }

    private void projection(String input, String masterShapeFile, Projection projection, File output)
    {
        assert (masterShapeFile != null);
        GdalUtils.register();
        synchronized (GdalUtils.lockObject)
        {
            Dataset inputDS = gdal.Open(input);
            SpatialReference inputRef = new SpatialReference();
            //inputRef.ImportFromWkt("GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433],AUTHORITY[\"EPSG\",4326]]");

            // FIXME: abstract it somehow?
            inputRef.ImportFromWkt("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\"],SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");

            inputDS.SetProjection(inputRef.ExportToWkt());
            List<DataSource> features = new ArrayList<DataSource>();

            features.add(ogr.Open(masterShapeFile));

            // Find union of extents
            double[] extent = features.get(0).GetLayer(0).GetExtent(); // Ordered: left, right, bottom, top

            double left = extent[0];
            double right = extent[1];
            double bottom = extent[2];
            double top = extent[3];

            for (int i=1; i<features.size(); i++)
            {
                extent = features.get(i).GetLayer(0).GetExtent();
                System.out.println("reporject : " + extent.toString());
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

            Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                    output.getPath(),
                    (int) Math.ceil((right-left)/projection.getPixelSize()),
                    (int) Math.ceil((top-bottom)/projection.getPixelSize()),
                    1,
                    gdalconst.GDT_Float32
                    );

            // FIXME: hack --should get projection from project info somehow
            String outputProjection = features.get(0).GetLayer(0).GetSpatialRef().ExportToWkt();

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

            gdal.ReprojectImage(inputDS, outputDS, null, null, resampleAlg);
            outputDS.GetRasterBand(1).ComputeStatistics(false);
            outputDS.delete();
            inputDS.delete();

        }
    }
}

