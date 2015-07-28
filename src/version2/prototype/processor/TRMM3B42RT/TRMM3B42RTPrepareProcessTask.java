package version2.prototype.processor.TRMM3B42RT;

import java.io.File;

import version2.prototype.DataDate;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.processor.PrepareProcessTask;
import version2.prototype.util.FileSystem;

public class TRMM3B42RTPrepareProcessTask extends PrepareProcessTask{

    private String [] inputFolders;
    private String outputFolder;

    public TRMM3B42RTPrepareProcessTask(ProjectInfoFile mProject,
            ProjectInfoPlugin mPlugin, DataDate mDate) {
        super(mProject, mPlugin, mDate);
    }

    @Override
    public String[] getInputFolders(int stepId) {

        inputFolders = null;

        switch (stepId){
        case 1:
            inputFolders[0] = String.format("%s"+ File.separator + "%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), plugin.GetName(), ProcessName.PROCESSOR),
                    "download", date.getYear(), date.getDayOfYear());
            break;
        case 2:
            inputFolders[0] = String.format("%s"+ File.separator + "%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), plugin.GetName(), ProcessName.PROCESSOR),
                    "convert", date.getYear(), date.getDayOfYear());
            break;
        case 3:
            inputFolders[0] = String.format("%s"+ File.separator + "%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), plugin.GetName(), ProcessName.PROCESSOR),
                    "reproject", date.getYear(), date.getDayOfYear());
            break;
        case 4:
            inputFolders[0] = String.format("%s"+ File.separator + "%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), plugin.GetName(), ProcessName.PROCESSOR),
                    "clip", date.getYear(), date.getDayOfYear());
            break;
        case 5:
            inputFolders[0] = String.format("%s"+ File.separator + "%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), plugin.GetName(), ProcessName.PROCESSOR),
                    "mask", date.getYear(), date.getDayOfYear());
            break;
        default:
            break;
        }
        return inputFolders;
    }

    @Override
    public String getOutputFolder(int stepId) {
        outputFolder = null;

        switch (stepId){
        case 1:
            outputFolder = String.format("%s"+ File.separator + "%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), plugin.GetName(), ProcessName.PROCESSOR),
                    "convert", date.getYear(), date.getDayOfYear());
            break;
        case 2:
            outputFolder = String.format("%s"+ File.separator + "%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), plugin.GetName(), ProcessName.PROCESSOR),
                    "reproject", date.getYear(), date.getDayOfYear());
            break;
        case 3:
            outputFolder = String.format("%s"+ File.separator + "%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), plugin.GetName(), ProcessName.PROCESSOR),
                    "clip", date.getYear(), date.getDayOfYear());
            break;
        case 4:
            outputFolder = String.format("%s"+ File.separator + "%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), plugin.GetName(), ProcessName.PROCESSOR),
                    "mask", date.getYear(), date.getDayOfYear());
            break;
        default:
            outputFolder = String.format("%s"+ File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessOutputDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), plugin.GetName(), ProcessName.PROCESSOR),
                    date.getYear(), date.getDayOfYear());
        }

        return outputFolder;
    }

    @Override
    public int[] getDataBands() {
        return null;
    }

    @Override
    public int[] getQCBands() {
        return null;
    }

}
