package version2.prototype.projection;

import java.io.File;

import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.util.GeneralListener;

public class ProcessData {

    public File[] inputFiles;
    public int[] bands;
    public File input;
    public File output;
    public ProjectInfoFile projectInfoFile;
    public GeneralListener generalListener;

    public ProcessData()
    {

    }

    public ProcessData(File[] inputFiles, int[] bands, File input, File output, ProjectInfoFile project, GeneralListener generalListener)
    {
        this.inputFiles = inputFiles;
        this.bands = bands;
        this.input = input;
        this.output = output;
        projectInfoFile = project;
        this.generalListener = generalListener;
    }
}
