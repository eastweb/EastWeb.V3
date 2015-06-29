package version2.prototype.projection.ModisNBAR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

import version2.prototype.projection.Filter;
import version2.prototype.projection.ModisTileData;
import version2.prototype.projection.ProcessData;
import version2.prototype.util.GdalUtils;

public class ModisNBARFilter extends Filter {

    public ModisNBARFilter(ProcessData data) {
        super(data);
    }

    @Override
    protected double filterValue(double value) {
        if(value < 0 || value > 32766) {
            return GdalUtils.NoValue;
        } else {
            return value;
        }
    }

    @Override
    protected void filterByQCFlag(String qcLevel) {
        List<String> allowedFlags = GetAllowedFlags(qcLevel);

        // There's no need to filter if it is set to "None"
        if(allowedFlags.size() == 5){ return; }

        GdalUtils.register();
        synchronized (GdalUtils.lockObject)
        {
            // Find the associated QC File
            for(File dataFile : inputFiles)
            {
                String[] nameParts = dataFile.getName().split("\\.");

                String identifier = nameParts[1] + "." + nameParts[2];
                String associatedQCFile = new String();
                for(File qcFile : qcFiles)
                {
                    if(qcFile.getName().contains(identifier)) {
                        try { associatedQCFile = qcFile.getCanonicalPath(); }
                        catch (IOException e) { associatedQCFile = qcFile.getAbsolutePath(); }
                        break;
                    }
                }

                // Open the QC File
                Dataset hdf = gdal.Open(associatedQCFile);
                if(hdf != null)
                {
                    String qaBandName = GetQABandName(hdf);

                    // Read the desired band
                    Dataset qaDS = gdal.Open(qaBandName);
                    int xSize = qaDS.getRasterXSize();
                    int ySize = qaDS.getRasterYSize();
                    Band b = qaDS.GetRasterBand(1);

                    // Read the entire raster
                    int[] array = new int[xSize * ySize];
                    b.ReadRaster(0, 0, xSize, ySize, 5, array);

                    ModisTileData tileData = null;

                    try {
                        tileData = new ModisTileData(dataFile);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    for(int i = 0; i < tileData.bandNumber; i++)
                    {
                        // Get the pixels that don't meet the desired QC level
                        List<Entry<Integer, Integer>> pairList = GetPixelsThatNeedFiltering(allowedFlags, i+1, array, xSize, ySize);
                        Dataset data = gdal.Open(tileData.sdsName[i]);

                        /*Driver driver = gdal.GetDriverByName("HDF5");
                        Dataset outputDS = driver.CreateCopy(outputFolder + "\\" + dataFile.getName(), data);*/

                        if(data != null)
                        {
                            int dataX = data.GetRasterXSize();
                            int dataY = data.GetRasterYSize();
                            Band dataBand = data.GetRasterBand(1);

                            // Read the full data band.
                            int[] dataArray = new int[dataX * dataY];
                            // 4 because it is 16 bit unsigned integer
                            dataBand.ReadRaster(0, 0, dataX, dataY, 4, dataArray);

                            // Replace each "bad" pixel with the fill value.
                            for(Entry<Integer, Integer> pair : pairList) {
                                dataArray[((pair.getKey() * dataY) + pair.getValue())] = GdalUtils.NoValue;
                            }

                            try {
                                // Write the changes.
                                synchronized (GdalUtils.lockObject) {
                                    data.GetRasterBand(1).WriteRaster(0, 0, dataX, dataY, 5, dataArray);
                                }
                            } catch(UnsatisfiedLinkError e) {
                                e.printStackTrace();
                            }

                            dataBand.delete();
                            data.delete();
                            //outputDS.delete();
                        }
                    }
                }
                try {
                    copyFile(outputFolder + "\\", dataFile);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
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

    private static void  copyFile(String destinationFolder, File source) throws IOException
    {
        if(!(new File(destinationFolder).exists())) {
            try {
                FileUtils.forceMkdir(new File(destinationFolder));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        InputStream inStream = null;
        OutputStream outStream = null;
        File destination = new File(destinationFolder + "\\" + source.getName());

        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(destination);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[1024];
        int length;

        while ((length = inStream.read(buffer)) > 0)
        {
            outStream.write(buffer, 0, length);
        }

        inStream.close();
        outStream.close();
    }
}