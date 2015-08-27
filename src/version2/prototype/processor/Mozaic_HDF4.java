package version2.prototype.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.util.GdalUtils;
import version2.prototype.util.HDF4Reader;

// @author:  Yi Liu
public abstract class Mozaic_HDF4
{

    //locations for the input files. for this step, will only use inputFolders[0]
    String [] inputFolders ;

    //location for the output file
    protected File outputFolder;
    // the bands need to be exacted.
    protected BandInfo [] bands;
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
    protected ModisTileData_HDF4[] tileList;
    protected ModisTileData_HDF4[][] tileMetrix;
    protected int tileMetrixRow;
    protected int tileMetrixClo;

    public Mozaic_HDF4(ProcessData data) throws Exception
    //public Mozaic_HDF4() throws Exception
    {
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

        bands = bandInfo();
        outputFiles = new ArrayList<File>();

        // read tile data and get size of tiles
        tileList = new ModisTileData_HDF4[tileNumber];

        for (int i = 0; i < tileNumber; i++) {
            tileList[i] = new ModisTileData_HDF4(inputFiles[i].getPath());
        }

        xSize = getXSize();
        ySize = getYSize();
    }

    // run method for the scheduler
    public void run() throws Exception{

        synchronized (GdalUtils.lockObject) {

            gdal.AllRegister();
            //create outputDirectory
            if (!outputFolder.exists())
            {   FileUtils.forceMkdir(outputFolder); }

            sortTiles();

            linkTiles();

            // remove the input folder
            FileUtils.deleteDirectory(inputFolder);
        }
    }

    abstract protected BandInfo[] bandInfo();

    abstract protected int getXSize();

    abstract protected int getYSize();

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
        tileMetrix = new ModisTileData_HDF4[tileMetrixRow][tileMetrixClo];

        for (int i = 0; i < tileNumber; i++) {
            tileMetrix[tileList[i].vertical - minV][tileList[i].horizon - minH] = tileList[i];
        }

        outputXSize = xSize * tileMetrixClo;
        outputYSize = ySize * tileMetrixRow;
    }

    private void linkTiles() throws Exception
    {
        // loop for each band needed be reprojected
        for (int i = 0; i < bands.length; i++)
        {
            int currentBand = bands[i].thisBand;
            File outputFile = new File(outputFolder, "band" + currentBand + ".tif");

            String[] option = { "INTERLEAVE=PIXEL" };

            Dataset output = gdal.GetDriverByName("GTiff").Create(
                    outputFile.getPath(),
                    outputXSize,
                    outputYSize,
                    1, // band number
                    gdalconst.GDT_Float32, option);

            /* Dataset input = gdal.Open(tileList[0].sdsName[0]);

            output.SetGeoTransform(input.GetGeoTransform());
            output.SetProjection(input.GetProjection());
            output.SetMetadata(input.GetMetadata_Dict());

             // if error happens, change to input=null
            input.delete();
             */

            // outputTemp is used to store double array data of output file
            ImageArray outputTemp = new ImageArray(output.getRasterXSize(), output.getRasterYSize());

            // loop for each tile
            for (int col = 0; col < tileMetrixClo; col++) {
                for (int row = 0; row < tileMetrixRow; row++) {
                    int [] tempArray = null;

                    if (tileMetrix[row][col] != null)
                    {
                        HDF4Reader hreader = new HDF4Reader(tileMetrix[row][col].fname);
                        tempArray = hreader.readBand(bands[i].totalBands, currentBand,
                                bands[i].order, bands[i].intSize);
                    }

                    // loop for each row of temp array
                    for (int j = ySize * row; j < ySize * (row + 1); j++)
                    {
                        double[] rowTemp = outputTemp.getRow(j);

                        if (tempArray != null) {
                            double[] tileRow = new double[xSize];
                            for (int k = 0; k < xSize; k++)
                            {
                                int rowInTemp = j - row * ySize;
                                tileRow[k] = tempArray[rowInTemp * xSize + k];
                            }
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
        }
    }


}
