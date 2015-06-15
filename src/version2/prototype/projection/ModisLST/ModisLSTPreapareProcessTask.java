package version2.prototype.projection.ModisLST;

import java.util.ArrayList;

import version2.prototype.DataDate;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.projection.PrepareProcessTask;

public class ModisLSTPreapareProcessTask extends PrepareProcessTask {

    public ModisLSTPreapareProcessTask(ProjectInfoFile mProject,
            ProjectInfoPlugin mPlugin, DataDate mDate) {
        super(mProject, mPlugin, mDate);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String[] getInputFolders(int stepId) {
        ArrayList<String> folders = new ArrayList<String>();

        // Format: Input (of this step) -> Output (of this step)
        switch(stepId)
        {
        case 1:
            // Download -> Mozaic
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\Download\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 2:
            // QCDownload -> QCMozaic
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\QCDownload\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 3:
            // Mozaic -> Reproject
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\1\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 4:
            // QCMozaic -> QCReproject
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\2\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 5:
            // Reproject/QCReproject -> Filter
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\3\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\4\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 6:
            // Filter -> Mask
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\5\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        default:
            folders = null;
            break;
        }
        return folders.toArray(new String[folders.size()]);

    }

    @Override
    public String getOutputFolder(int stepId) {
        String outputFolder = "";

        // Format: Input (of this step) -> Output (of this step)
        switch(stepId)
        {
        case 1:
            // Download -> Mozaic
            outputFolder = project.GetWorkingDir() + String.format("ModisLST\\1\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 2:
            // QCDownload -> QCMozaic
            outputFolder = project.GetWorkingDir() + String.format("ModisLST\\2\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 3:
            // Mozaic -> Reproject
            outputFolder = project.GetWorkingDir() + String.format("ModisLST\\3\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 4:
            // QCMozaic -> QCReproject
            outputFolder = project.GetWorkingDir() + String.format("ModisLST\\4\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 5:
            // Reproject/QCReproject -> Filter
            outputFolder = project.GetWorkingDir() + String.format("ModisLST\\5\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 6:
            // Filter -> Mask
            outputFolder = project.GetWorkingDir() + String.format("ModisLST\\6\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        default:
            outputFolder = null;
            break;
        }

        return outputFolder;
    }

    @Override
    public int[] getDataBands() {
        return new int[] { 1,5};
    }

    @Override
    public int[] getQCBands() {
        // TODO: Determine what the QC bands are.
        return new int[] { 2,6};
    }
}

