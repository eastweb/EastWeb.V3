package version2.prototype.processor;

import java.nio.ByteOrder;

import version2.prototype.util.HDF4Reader;

public class TestReadModis {

    public static void main(String[] args)
    {
        String fname = "D:\\project\\download\\MODISLST\\2014\\081\\h28v06.hdf";
        ModisTileData_HDF4 m = new ModisTileData_HDF4(fname);


        //
        //        HDF4Reader hreader = new HDF4Reader(fname);
        //        int[] resultArr;
        //        try {
        //            resultArr = hreader.readBand(12, 5, ByteOrder.LITTLE_ENDIAN, 16);
        //            for (int i= 1000; i <1300; i++)
        //            {
        //                System.out.println(resultArr[i]);
        //            }
        //        } catch (Exception e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }



    }

}
