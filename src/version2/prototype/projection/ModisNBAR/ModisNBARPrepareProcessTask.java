package version2.prototype.projection.ModisNBAR;

import java.util.ArrayList;

import version2.prototype.DataDate;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.projection.PrepareProcessTask;
import version2.prototype.util.GeneralListener;

public class ModisNBARPrepareProcessTask extends PrepareProcessTask {

    public ModisNBARPrepareProcessTask(ProjectInfoFile mProject,
            ProjectInfoPlugin mPlugin, DataDate mDate, GeneralListener l) {
        super(mProject, mPlugin, mDate, l);
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
            folders.add(project.workingDir + String.format("ModisNBAR\\Download\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 2:
            // QCDownload -> QCMozaic
            folders.add(project.workingDir + String.format("ModisNBAR\\QCDownload\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 3:
            // Mozaic -> Reproject
            folders.add(project.workingDir + String.format("ModisNBAR\\1\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 4:
            // QCMozaic -> QCReproject
            folders.add(project.workingDir + String.format("ModisNBAR\\2\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 5:
            // Reproject/QCReproject -> Filter
            folders.add(project.workingDir + String.format("ModisNBAR\\3\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            folders.add(project.workingDir + String.format("ModisNBAR\\4\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 6:
            // Filter -> Mask
            folders.add(project.workingDir + String.format("ModisNBAR\\5\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
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
            outputFolder = project.workingDir + String.format("ModisNBAR\\1\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 2:
            // QCDownload -> QCMozaic
            outputFolder = project.workingDir + String.format("ModisNBAR\\2\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 3:
            // Mozaic -> Reproject
            outputFolder = project.workingDir + String.format("ModisNBAR\\3\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 4:
            // QCMozaic -> QCReproject
            outputFolder = project.workingDir + String.format("ModisNBAR\\4\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 5:
            // Reproject/QCReproject -> Filter
            outputFolder = project.workingDir + String.format("ModisNBAR\\5\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 6:
            // Filter -> Mask
            outputFolder = project.workingDir + String.format("ModisNBAR\\6\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        default:
            outputFolder = null;
            break;
        }

        return outputFolder;
    }

    @Override
    public int[] getDataBands() {
        return new int[] { 1,2, 3, 4, 5, 6, 7 };
    }

    @Override
    public int[] getQCBands() {
        // TODO: Determine what the QC bands are.
        return null;
    }
}
