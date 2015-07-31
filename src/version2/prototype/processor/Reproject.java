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
            projection(f, shapefile, projection, outputFile);
        }

    }

    private void projection(File input, String masterShapeFile, Projection projection, File output)
    {
        assert (masterShapeFile != null);
        GdalUtils.register();

        synchronized (GdalUtils.lockObject)
        {
            // Load input file and features
            Dataset inputDS = gdal.Open(input.getPath());
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

                System.out.println("reproject: " +
                        (int) Math.ceil((right - left) / (projection.getPixelSize())) + " :/"
                        + (int) Math.ceil((top - bottom) / (projection.getPixelSize()))
                        );

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
                // SpatialReference outputRef = new SpatialReference();
                // outputRef.ImportFromWkt(wkt);
                String outputProjection =
                        features.get(0).GetLayer(0).GetSpatialRef().ExportToWkt();
                outputDS.SetProjection(outputProjection);
                outputDS.SetGeoTransform(new double[] { left,
                        (projection.getPixelSize()), 0, top, 0,
                        - (double)(projection.getPixelSize()) });

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
                outputDS.GetRasterBand(1).ComputeStatistics(false);

                /*
                Band b = outputDS.GetRasterBand(1);
                int x = b.getXSize(); int y = b.getYSize();
                double [] testArr = new double[x * y];
                b.ReadRaster(0, 0, x, y, testArr);
                for (int i = 0; i < 20; i++) {
                    for (int j = 0; j<20; j++) {
                        System.out.println(testArr[i * j + j]);
                    }
                }

                 */
                outputDS.delete();
                inputDS.delete();
            }
        }
    }
}
