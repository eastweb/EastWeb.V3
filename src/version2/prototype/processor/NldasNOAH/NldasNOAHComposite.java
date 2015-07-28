package version2.prototype.processor.NldasNOAH;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.processor.Composite;
import version2.prototype.processor.ProcessData;
import version2.prototype.util.GdalUtils;

public class NldasNOAHComposite extends Composite{

    private int[] dataBands = null;

    public NldasNOAHComposite(ProcessData data) {
        super(data);
        // TODO Auto-generated constructor stub

        dataBands = data.getDataBands();
    }

    @Override
    public void composeFiles() {
        // TODO Auto-generated method stub

        GdalUtils.register();

        synchronized(GdalUtils.lockObject)
        {
            // open file
            // read 10 bands and add to corresponding temp file
            // close file
            Dataset initialize = gdal.Open(inputFiles[0].getPath());
            int xSize = initialize.GetRasterXSize();
            int ySize = initialize.GetRasterYSize();
            initialize.delete();

            ArrayList<double[]> tempArray = new ArrayList<double[]>();

            for(int i = 0; i < 10; i++)
            {
                tempArray.add(new double[xSize*ySize]);
            }

            for(File input: inputFiles)
            {
                Dataset inputDS = gdal.Open(input.getPath());
                double[] inputArray = new double[xSize*ySize];
                int k = 0;

                for(int band: dataBands)
                {
                    inputDS.GetRasterBand(band).ReadRaster(0, 0, inputDS.GetRasterXSize(), inputDS.GetRasterYSize(), inputArray);
                    for(int i = 0; i < inputArray.length; i++)
                    {
                        tempArray.get(k)[i] += inputArray[i];
                    }

                    k++;
                }

                inputDS.delete();
            }

            for(int i = 0; i < tempArray.size(); i++)
            {
                for(int j = 0; j < (xSize*ySize); j++)
                {
                    tempArray.get(i)[j] /= inputFiles.length;
                }

                File outputFile = null;
                try {
                    outputFile = File.createTempFile(String.format("band%d", dataBands[i]), ".tif",new File(outputFolder));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Dataset outputDS = gdal.GetDriverByName("GTiff").Create(outputFile.getAbsolutePath(),
                        xSize, ySize, 1, gdalconst.GDT_Float32);

                outputDS.GetRasterBand(1).WriteRaster(0, 0, xSize, ySize, tempArray.get(i));

                outputDS.delete();
            }

        }
    }

}
