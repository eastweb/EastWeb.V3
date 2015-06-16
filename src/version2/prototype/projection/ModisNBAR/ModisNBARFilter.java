package version2.prototype.projection.ModisNBAR;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import version2.prototype.projection.Filter;
import version2.prototype.projection.ProcessData;
import version2.prototype.util.GdalUtils;

public class ModisNBARFilter extends Filter {

    public ModisNBARFilter(ProcessData data) {
        super(data);
    }

    @Override
    protected double filterValue(double value) {
        if(value < 0 || value > 32766) {
            return 32767;
        } else {
            return value;
        }
    }

    @Override
    protected double filterByQCFlag(String qcLevel) {
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
            // Only highest by default.
            allowedFlags.add("0000");
            break;
        }

        GdalUtils.register();
        synchronized (GdalUtils.lockObject)
        {
            for (File qcInFile : qcFiles)
            {
                File dataFile = null;
                for(File inputFile : inputFiles)
                {
                    if(inputFile.getName() == qcInFile.getName())
                    {
                        dataFile = inputFile;
                        break;
                    }
                }
                Dataset qcInputDS = gdal.Open(qcInFile.getPath());
                for(int i = 1; i <= qcInputDS.GetRasterCount(); i++)
                {

                }

                // TODO: Read the decimal value for the QA
                int decimal = 0;

                String binaryFlags = Integer.toBinaryString(decimal);

                String qaFlagsString = binaryFlags.substring(4);
                List<String> qaFlagsPerBand = new ArrayList<String>();
                String temp = "";
                for(int i = 0; i < qaFlagsString.length(); i++)
                {
                    if(i % 4 == 0 && i != 0) {
                        qaFlagsPerBand.add(temp);
                    }

                }
            }
        }
        return 0;
    }

}
