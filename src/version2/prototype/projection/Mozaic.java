package version2.prototype.projection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.util.GdalUtils;


/* Modified by YL on May 31st
 */

// Mosaic tiles together
public class Mozaic {

    //locations for the input files. for this step, will only use inputFolders[0]
    String [] inputFolders ;

    //location for the output file
    protected File outputFolder;
    // the bands need to be exacted.
    protected int [] bands;
    protected File inputFolder;
    // the files in the input folder
    protected File [] inputFiles;
    // hold the output files
    protected ArrayList<File> outputFiles;

    protected int tileNumber;

    protected int xSize;
    protected int ySize;
    protected int outputXSize;
    protected int outputYSize;
    protected ModisTileData[] tileList;
    protected ModisTileData[][] tileMetrix;
    protected int tileMetrixRow;
    protected int tileMetrixClo;

    public Mozaic(ProcessData data) throws InterruptedException {

        //locations for the input files. for this step, will only use inputFolders[0]
        inputFolders = data.getInputFolders();

        //check if there is at least one input file in the given folder
        inputFolder = new File(inputFolders[0]);
        File[] listOfFiles = inputFolder.listFiles();
        tileNumber = listOfFiles.length;
        assert (tileNumber >= 1);
        //set the input files
        inputFiles = listOfFiles;

        outputFolder = new File(data.getOutputFolder());

        bands = data.getDataBands();
        outputFiles = new ArrayList<File>();

        // read tile data and get size of tiles
        tileList = new ModisTileData[tileNumber];

        for (int i = 0; i < tileNumber; i++) {
            tileList[i] = new ModisTileData(inputFiles[i]);
        }

        xSize = tileList[0].xSize;
        ySize = tileList[0].ySize;
    }

    // run method for the scheduler
    public void run(){
        synchronized (GdalUtils.lockObject) {
            sortTiles();
            try {
                linkTiles();

                // remove the input folder
                FileUtils.deleteDirectory(inputFolder);
            } catch (IOException e) {
                // TODO: Write to log
                e.printStackTrace();
            }
        }
    }

    protected void sortTiles() {
        int minH = tileList[0].horizon;
        int maxH = tileList[0].horizon;
        int minV = tileList[0].vertical;
        int maxV = tileList[0].vertical;

        for (int i = 0; i < tileNumber; i++) {
            if (minH > tileList[i].horizon) {
                minH = tileList[i].horizon;
            }

            if (maxH < tileList[i].horizon) {
                maxH = tileList[i].horizon;
            }
            if (minV > tileList[i].vertical) {
                minV = tileList[i].vertical;
            }

            if (maxV < tileList[i].vertical) {
                maxV = tileList[i].vertical;
            }
        }

        tileMetrixRow = maxV - minV + 1;
        tileMetrixClo = maxH - minH + 1;
        tileMetrix = new ModisTileData[tileMetrixRow][tileMetrixClo];

        for (int i = 0; i < tileNumber; i++) {
            tileMetrix[tileList[i].vertical - minV][tileList[i].horizon - minH] = tileList[i];
        }

        for (int i = 0; i < tileMetrixRow; i++) {
            for (int j = 0; j < tileMetrixClo; j++) {
                System.out.println(Boolean.toString(tileMetrix[i][j] != null) + i + " " + j);
            }
        }

        outputXSize = xSize * tileMetrixClo;
        outputYSize = ySize * tileMetrixRow;
    }

    private void linkTiles() throws IOException {

        // loop for each band needed be reprojected
        for (int i = 0; i < bands.length; i++) {
            int currentBand = bands[i];
            File temp = File.createTempFile("band" + currentBand, ".tif", outputFolder);

            System.out.println("create temp: " + temp.toString());
            temp.deleteOnExit();

            String[] option = { "INTERLEAVE=PIXEL" };
            Dataset output = gdal.GetDriverByName("GTiff").Create(
                    temp.getAbsolutePath(),
                    outputXSize,
                    outputYSize,
                    1, // band number
                    gdalconst.GDT_Float32, option);
            Dataset input = gdal.Open(tileList[0].sdsName[0]);

            output.SetGeoTransform(input.GetGeoTransform());
            output.SetProjection(input.GetProjection());
            output.SetMetadata(input.GetMetadata_Dict());

            // if error happens, change to input=null
            input.delete();

            // outputTemp is used to store double array data of output file
            ImageArray outputTemp = new ImageArray(output.getRasterXSize(), output.getRasterYSize());

            // loop for each tile
            for (int col = 0; col < tileMetrixClo; col++) {
                for (int row = 0; row < tileMetrixRow; row++) {
                    ImageArray tempArray = null;

                    if (tileMetrix[row][col] != null) {
                        System.out.println("current= "
                                + currentBand
                                + " "
                                + tileMetrix[row][col].sdsName[currentBand - 1]);

                        Dataset tempTile = gdal.Open(tileMetrix[row][col].sdsName[currentBand - 1]);
                        tempArray = new ImageArray(tempTile.GetRasterBand(1));
                        tempTile.delete();
                    }

                    // loop for each row of temp array image
                    for (int j = ySize * row; j < ySize * (row + 1); j++) {
                        double[] rowTemp = outputTemp.getRow(j);

                        if (tempArray != null) {
                            double[] tileRow = tempArray.getRow(j - row * ySize);
                            System.arraycopy(tileRow, 0, rowTemp, col * xSize, xSize);
                        } else {
                            // set value for the no tile data area
                            double[] tileRow = new double[xSize];

                            for (int k = 0; k < xSize; k++) {
                                tileRow[k] = -3.40282346639e+038;
                            }
                            System.arraycopy(tileRow, 0, rowTemp, col * xSize, xSize);
                        }

                        outputTemp.setRow(j, rowTemp);
                        rowTemp = null;
                    }
                }
            }

            output.GetRasterBand(1).WriteRaster(0, 0, output.getRasterXSize(), output.getRasterYSize(), outputTemp.getArray());
            output.GetRasterBand(1).ComputeStatistics(true);
            output.delete();

            // add this band mozaic product into outputFile arraylist
            outputFiles.add(temp);
        }

    }
}