package version2.prototype.projection;

import java.io.File;

import version2.prototype.Projection;
import version2.prototype.util.GeneralListener;

/* rewritten by Y.L. on May 31st */

public class ProcessData {

    private String[] inputFolders;
    private String outputFolder;
    private String qcLevel;
    private String shapefile;
    private String maskfile;
    private int [] dataBands;
    private int [] qcBands;
    private Projection projection;
    private GeneralListener generalListener;

    public ProcessData() { }

    public ProcessData(String [] inputFolders, String outputFolder, String qcLevel, String shapefile,
            String maskfile, int dataBands[], int qcBands[], Projection projection, GeneralListener generalListener)
    {
        this.inputFolders = inputFolders;
        this.outputFolder = outputFolder;
        this.qcLevel = qcLevel;
        this.shapefile = shapefile;
        this.maskfile = maskfile;
        this.dataBands = dataBands;
        this.qcBands = qcBands;
        this.projection = projection;
        this.generalListener = generalListener;
    }

    public String [] getInputFolders()
    {   return inputFolders;    }

    public String getOutputFolder()
    {   return outputFolder;    }

    public String getQCLevel()
    {   return qcLevel;     }

    public String getShapefile()
    {   return shapefile;   }

    public String getMaskfile()
    {   return maskfile;      }

    public int [] getDataBands()
    {   return dataBands;       }

    public int [] getQCBands()
    {   return qcBands;       }

    public Projection getProjection()
    {   return projection;  }


}
