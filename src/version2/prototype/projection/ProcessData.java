package version2.prototype.projection;

import java.io.File;

import version2.prototype.ProjectInfo;

public class ProcessData {

    public File[] inputFiles;
    public int[] bands;
    public File input;
    public File output;
    public ProjectInfo projectInfo;

    public ProcessData()
    {

    }

    public ProcessData(File[] inputFiles, int[] bands, File input, File output, ProjectInfo project)
    {
        this.inputFiles = inputFiles;
        this.bands = bands;
        this.input = input;
        this.output = output;
        projectInfo = project;
    }
}
