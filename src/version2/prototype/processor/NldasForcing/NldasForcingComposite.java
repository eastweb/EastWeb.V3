package version2.prototype.processor.NldasForcing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import version2.prototype.processor.Composite;
import version2.prototype.processor.ProcessData;
import version2.prototype.util.GdalUtils;

public class NldasForcingComposite extends Composite {

    public NldasForcingComposite(ProcessData data) {
        super(data);
    }

    @Override
    public void composeFiles()
    {
        GdalUtils.register();
        synchronized (GdalUtils.lockObject) {
            List<Dataset> inputDSs = new ArrayList<Dataset>();

            if(!(new File(outputFolder).exists())){
                try { FileUtils.forceMkdir(new File(outputFolder)); }
                catch (IOException e) { e.printStackTrace(); }
            }

            File temp = null;
            try {
                temp = File.createTempFile(String.format("FORA_%s_",
                        inputFiles[0].getName().substring(
                                inputFiles[0].getName().indexOf("_H.A")+4,
                                inputFiles[0].getName().indexOf(".002.grb")-5)),
                                ".tif",
                                new File(outputFolder));
            }
            catch (IOException e) { e.printStackTrace(); }

            for (File input : inputFiles) {
                inputDSs.add(gdal.Open(input.getPath()));
            }

            Dataset outputDS = gdal.GetDriverByName("GTiff").Create(
                    temp.getAbsolutePath(),
                    inputDSs.get(0).GetRasterXSize(), inputDSs.get(0).GetRasterYSize(),
                    1,
                    gdalconst.GDT_Float32
                    );
            outputDS.SetGeoTransform(inputDSs.get(0).GetGeoTransform());
            outputDS.SetProjection(inputDSs.get(0).GetProjection());

            double[] inputArray = new double[inputDSs.get(0).GetRasterXSize() * inputDSs.get(0).GetRasterYSize()];
            double[] outputArray = new double[inputDSs.get(0).GetRasterXSize() * inputDSs.get(0).GetRasterYSize()];
            for (Dataset inputDS : inputDSs) {
                inputDS.GetRasterBand(1).ReadRaster(0, 0, inputDS.GetRasterXSize(), inputDS.GetRasterYSize(), inputArray);
                for (int i=0; i<inputArray.length; i++) {
                    outputArray[i] += inputArray[i];
                }
            }

            for (int i=0; i<inputArray.length; i++) {
                outputArray[i] /= inputDSs.size();
            }

            outputDS.GetRasterBand(1).WriteRaster(0, 0, inputDSs.get(0).GetRasterXSize(), inputDSs.get(0).GetRasterYSize(), outputArray);

            for (Dataset inputDS : inputDSs) {
                inputDS.delete();
            }
            outputDS.delete();
        }
    }
}
