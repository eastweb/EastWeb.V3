package version2.prototype.projection.ModisNBAR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.projection.ImageArray;
import version2.prototype.projection.ModisTileData;
import version2.prototype.projection.Mozaic;
import version2.prototype.projection.ProcessData;
import version2.prototype.util.GdalUtils;

public class ModisNBARFilterMozaic extends Mozaic {
    private File[] qcFiles;
    private String qcLevel;

    public ModisNBARFilterMozaic(ProcessData data) throws InterruptedException {
        super(data);

        //check if there is ate least one input file in the given folder
        File[] listOfFiles2= new File(data.getInputFolders()[1]).listFiles();

        if (listOfFiles2.length >= 1) {
            qcFiles = listOfFiles2;
        }

        qcLevel = data.getQCLevel();
    }

    @Override
    public void run()
    {
        synchronized (GdalUtils.lockObject) {
            sortTiles();
            try {
                ModisNBARLinkTiles();

                // remove the input folder
                FileUtils.deleteDirectory(inputFolder);
            } catch (IOException e) {
                // TODO: Write to log
                e.printStackTrace();
            }
        }
    }

    private void ModisNBARLinkTiles() throws IOException {
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
                    gdalconst.GDT_Int16, option);
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

                        int[] filteredArray = FilterByQCFlag(tempTile, tileMetrix[row][col], currentBand);
                        double[] dataArray = new double[tileMetrix[row][col].xSize * tileMetrix[row][col].ySize];

                        for(int index = 0; index < filteredArray.length; index++){
                            dataArray[index] = filteredArray[index];
                        }

                        tempArray = new ImageArray(tileMetrix[row][col].xSize, tileMetrix[row][col].ySize, dataArray);
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
                                tileRow[k] = GdalUtils.NoValue;
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

    protected int[] FilterByQCFlag(Dataset bandData, ModisTileData tile, int currentBand) {
        List<String> allowedFlags = GetAllowedFlags(qcLevel);
        int[] dataArray = new int[tile.xSize * tile.ySize];

        // There's no need to filter if it is set to "None"
        if(allowedFlags.size() == 5){
            bandData.GetRasterBand(1).ReadRaster(0, 0, tile.xSize, tile.ySize, 4, dataArray);
            return dataArray;
        }

        GdalUtils.register();
        synchronized (GdalUtils.lockObject)
        {
            String identifier = String.format("h%02dv%02d", tile.horizon, tile.vertical);
            String associatedQCFile = new String();
            for(File qcFile : qcFiles)
            {
                if(qcFile.getName().contains(identifier)) {
                    associatedQCFile = qcFile.getAbsolutePath();
                    break;
                }
            }

            // Open the QC File
            Dataset qaHdf = gdal.Open(associatedQCFile);
            if(qaHdf != null)
            {
                // Read the desired band
                Dataset qaDS = gdal.Open(GetQABandName(qaHdf));
                int xSize = qaDS.getRasterXSize();
                int ySize = qaDS.getRasterYSize();
                Band b = qaDS.GetRasterBand(1);

                // Read the entire raster for qc
                int[] array = new int[xSize * ySize];
                b.ReadRaster(0, 0, xSize, ySize, 5, array);

                // Get the pixels that don't meet the desired QC level
                List<Entry<Integer, Integer>> pairList = GetPixelsThatNeedFiltering(allowedFlags, currentBand, array, xSize, ySize);

                if(bandData != null)
                {
                    int dataX = bandData.GetRasterXSize();
                    int dataY = bandData.GetRasterYSize();
                    Band dataBand = bandData.GetRasterBand(1);

                    // Read the full data band (4 because it is 16 bit unsigned integer)
                    dataBand.ReadRaster(0, 0, dataX, dataY, 4, dataArray);

                    // Replace each "bad" pixel with the fill value.
                    for(Entry<Integer, Integer> pair : pairList) {
                        dataArray[((pair.getKey() * dataY) + pair.getValue())] = GdalUtils.NoValue;
                    }
                    bandData.delete();
                }
            }
        }

        return dataArray;
    }

    private static List<String> GetAllowedFlags(String qcLevel)
    {
        List<String> allowedFlags = new ArrayList<String>();
        // The lower the level, the more it will add, thus if it is none,
        // it will have all flags allowed (which is why only break at last case)
        switch(qcLevel)
        {
        case "None":
            allowedFlags.add("0100");
        case "Low":
            allowedFlags.add("0011");
        case "Moderate":
            allowedFlags.add("0010");
            allowedFlags.add("0001");
        case "Highest":
            allowedFlags.add("0000");
            break;
        default:
            // Everything by default
            allowedFlags.add("0100");
            allowedFlags.add("0011");
            allowedFlags.add("0010");
            allowedFlags.add("0001");
            allowedFlags.add("0000");
            break;
        }

        return allowedFlags;
    }

    @SuppressWarnings("unchecked")
    private static String GetQABandName(Dataset hdf)
    {
        Hashtable<String, String> sdsdict = null;
        String qaBandName = new String();
        if(hdf != null)
        {
            sdsdict = hdf.GetMetadata_Dict("SUBDATASETS");
            Enumeration<String> keys = sdsdict.keys();

            // Get the correct SDS
            while (keys.hasMoreElements()) {
                Object aKey = keys.nextElement();
                Object aValue = sdsdict.get(aKey);

                if(aKey.toString().contains("NAME"))
                {
                    String bandName[] = aKey.toString().split("_");

                    // We are only interested in band 4
                    if(Integer.parseInt(bandName[1]) == 4) {
                        qaBandName = aValue.toString();
                    }
                }
            }
        }

        return qaBandName;
    }

    private static List<Entry<Integer, Integer>> GetPixelsThatNeedFiltering(List<String> allowedFlags, int band, int[] array, int xSize, int ySize)
    {
        List<Entry<Integer, Integer>> badPixels = new ArrayList<>();
        for(int row = 0; row < ySize; row++)
        {
            for(int col = 0; col < xSize; col++)
            {
                if(!allowedFlags.contains(GetPixelQCFlag(row, col, band, array, xSize, ySize)))
                {
                    java.util.Map.Entry<Integer,Integer> coordinates = new java.util.AbstractMap.SimpleEntry<>(row, col);
                    badPixels.add(coordinates);
                    coordinates = null;
                }
            }
        }
        return badPixels;
    }

    private static String GetPixelQCFlag(int row, int column, int bandNumber, int[] qcArray, int xSize, int ySize)
    {
        String binaryFlags = Integer.toBinaryString(qcArray[(row * ySize) + column]);

        int length = binaryFlags.length();
        String padding = "";

        for(int i = length; i < 32; i++){
            padding += "0";
        }

        binaryFlags = padding + binaryFlags;
        int startIndex = (32 - (4 * bandNumber));

        //if(bandNumber < numberOfBands.length) {
        return binaryFlags.substring(startIndex, (startIndex+4));
        //}
    }
}
