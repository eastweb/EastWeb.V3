package version2.prototype.processor;

import java.time.LocalDate;

import version2.prototype.Projection;

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
    private Integer maskResolution;
    private Boolean clipOrNot;
    private LocalDate freezingDate;
    private LocalDate heatingDate;
    private double freezingDegree;
    private double heatingDegree;

    public ProcessData() { }

    public ProcessData(String [] inputFolders, String outputFolder, String qcLevel, String shapefile,
            String maskfile, int dataBands[], int qcBands[], Projection projection,
            Integer maskResolution, Boolean clipOrNot, LocalDate freezingDate, LocalDate heatingDate,
            double freezingDegree, double heatingDegree)
    {
        this.inputFolders = inputFolders;
        this.outputFolder = outputFolder;
        this.qcLevel = qcLevel;
        this.shapefile = shapefile;
        this.maskfile = maskfile;
        this.dataBands = dataBands;
        this.qcBands = qcBands;
        this.projection = projection;
        this.maskResolution = maskResolution;
        this.clipOrNot = clipOrNot;
        this.freezingDate = freezingDate;
        this.heatingDate = heatingDate;
        this.freezingDegree = freezingDegree;
        this.heatingDegree = heatingDegree;
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

    public Integer getMaskResolution()
    {    return maskResolution; }

    // return true for clipping and false for not clipping
    public Boolean getClipOrNot()
    {   return clipOrNot;       }

    public LocalDate getFreezingDate()
    {   return freezingDate;    }

    public LocalDate getHeatingDate()
    {   return heatingDate;     }

    public double getFreezingDegree()
    {   return freezingDegree;  }

    public double getHeatingDegree()
    {   return heatingDegree;   }
}
