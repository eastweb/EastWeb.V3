package version2.prototype.processor.TRMM3B42RT;

import version2.prototype.DataDate;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.processor.PrepareProcessTask;
import version2.prototype.util.GeneralListener;

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
            inputFolders[0] = project.GetWorkingDir() + "/download/"
                    + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
            break;
        case 2:
            inputFolders[0] = project.GetWorkingDir() + "/convert/"
                    + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
            break;
        case 3:
            inputFolders[0] = project.GetWorkingDir() + "/reproject/"
                    + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
            break;
        case 4:
            inputFolders[0] = project.GetWorkingDir() + "/clip/"
                    + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
            break;
        case 5:
            inputFolders[0] = project.GetWorkingDir() + "/mask/"
                    + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
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
            outputFolder = project.GetWorkingDir() + "/convert/"
                    + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
            break;
        case 2:
            outputFolder = project.GetWorkingDir() + "/reproject/"
                    + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
            break;
        case 3:
            outputFolder = project.GetWorkingDir() + "/clip/"
                    + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
            break;
        case 4:
            outputFolder = project.GetWorkingDir() + "/mask/"
                    + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
            break;
        default:
            outputFolder = project.GetWorkingDir() + "/output/"
                    + date.getYear() + "/" + date.getMonth() + "/" + date.getDay();
            break;

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
