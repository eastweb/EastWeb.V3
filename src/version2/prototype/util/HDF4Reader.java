package version2.prototype.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;

public class HDF4Reader
{
    private String fname;

    public HDF4Reader(String filename)
    {
        fname = filename;
    }

    /* @param totalBands:  the number of the bands in the file
     * @param thisBand:  the band to read
     * @param order :  ByteOrder.LITTLE_ENDIAN or ByteOrder.BIG_ENDIAN
     * @param intSize:  8-bit(8), 16-bit(16), 32-bit(32)
     */
    public int [] readBand(int totalBands, int thisBand, ByteOrder order, int intSize) throws Exception
    {
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF4);

        if (fileFormat == null)
        {   throw new Exception("Cannot find HDF4 FileFormat.");    }

        // open the file with read access
        FileFormat testFile = fileFormat.createInstance(fname, FileFormat.READ);

        if (testFile == null)
        {   throw new Exception("Failed to open file: " + fname);   }

        // open the file and retrieve the file structure
        testFile.open();

        Group root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) testFile.getRootNode()).getUserObject();
        HObject band = getBand(root, totalBands, thisBand);
        //System.out.println(band);

        // read the band data into a byte array
        Dataset d = (Dataset) band;
        byte []  data = d.readBytes();

        int [] resultArr = null;

        switch (intSize)
        {
        case 8:
            // read as byte
            int length_8 = data.length;
            resultArr = new int[length_8];
            for (int i=0; i <length_8; i++)
            {   resultArr[i] = data[i];    }
            break;
        case 16:
            // read as 16-bit integer
            int length_16 = data.length/2;
            short[] shorts = new short[length_16];
            // to turn bytes to shorts as LITTLE endian (although said BIG ENDIAN in the MODIS doc
            ByteBuffer.wrap(data).order(order).asShortBuffer().get(shorts);

            // convert it to int array
            resultArr = new int[length_16];

            for (int i=0; i <length_16; i++)
            {   resultArr[i] = shorts[i];  }
            break;
        default:
            // read as 32-bit integer
            IntBuffer intBuf = ByteBuffer.wrap(data).order(order) .asIntBuffer();
            resultArr = new int[intBuf.remaining()];
            intBuf.get(resultArr);
            break;
        }

        testFile.close();

        return resultArr;

    }

    private HObject getBand(Group g, int totalBands, int bandNo) throws Exception
    {
        if (g == null) {
            return null;
        }

        List<HObject> members = g.getMemberList();

        HObject obj = null;
        int n = members.size();

        if (n == totalBands)
        {
            obj = members.get(bandNo-1);
            return obj;
        }
        else
        {   for (int i = 0; i <n; i++)
        {
            obj = members.get(i);
            if (obj instanceof Group)
            {
                return getBand((Group)obj, totalBands, bandNo);
            }
        }
        }
        return obj;
    }

}
