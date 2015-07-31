package version2.prototype.processor.ModisLST;

import java.util.ArrayList;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.processor.PrepareProcessTask;

public class ModisLSTPrepareProcessTask extends PrepareProcessTask {

    public ModisLSTPrepareProcessTask(ProjectInfoFile mProject,
            ProjectInfoPlugin pPlugin, PluginMetaData plugin, DataDate mDate) {
        super(mProject, pPlugin, plugin, mDate);
    }

    @Override
    public String[] getInputFolders(int stepId) {
        ArrayList<String> folders = new ArrayList<String>();

        // Format: Input (of this step) -> Output (of this step)
        switch(stepId)
        {
        case 1:
            // Download -> Filter
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\Download\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\QCDownload\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 2:
            // Filter -> Mozaic
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\1\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 3:
            // Mozaic -> Reproject
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\2\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 4:
            // Reproject -> Mask
            folders.add(project.GetWorkingDir() + String.format("ModisLST\\3\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
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
            // Download -> Filter
            outputFolder = project.GetWorkingDir() + String.format("ModisLST\\1\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 2:
            // Filter -> Mozaic
            outputFolder = project.GetWorkingDir() + String.format("ModisLST\\2\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 3:
            // Mozaic -> Reproject
            outputFolder = project.GetWorkingDir() + String.format("ModisLST\\3\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 4:
            // Reproject -> Mask
            outputFolder = project.GetWorkingDir() + String.format("ModisLST\\4\\%4d\\%03d", date.getYear(), date.getDayOfYear());
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

