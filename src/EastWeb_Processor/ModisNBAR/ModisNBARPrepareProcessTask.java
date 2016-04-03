package EastWeb_Processor.ModisNBAR;

import java.io.File;
import java.util.ArrayList;

import EastWeb_Processor.PrepareProcessTask;
import EastWeb_Scheduler.ProcessName;
import PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import ProjectInfoMetaData.ProjectInfoFile;
import ProjectInfoMetaData.ProjectInfoPlugin;
import Utilies.DataDate;
import Utilies.FileSystem;

public class ModisNBARPrepareProcessTask extends PrepareProcessTask {

    public ModisNBARPrepareProcessTask(ProjectInfoFile mProject,
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
            // Download -> FilterMosaic
            folders.add(String.format("%s%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), pPlugin.GetName(), ProcessName.PROCESSOR),
                    "Download", date.getYear(), date.getDayOfYear()));
            folders.add(String.format("%s%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), pPlugin.GetName(), ProcessName.PROCESSOR),
                    "QCDownload", date.getYear(), date.getDayOfYear()));
            break;
        case 2:
            // FilterMosaic -> Reproject
            folders.add(String.format("%s%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), pPlugin.GetName(), ProcessName.PROCESSOR),
                    "FilterMosaic", date.getYear(), date.getDayOfYear()));
            break;
        case 3:
            // Reproject -> Mask
            folders.add(String.format("%s%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), pPlugin.GetName(), ProcessName.PROCESSOR),
                    "Reproject", date.getYear(), date.getDayOfYear()));
            break;
        case 4:
            // Mask -> Clip
            folders.add(String.format("%s%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), pPlugin.GetName(), ProcessName.PROCESSOR),
                    "Mask", date.getYear(), date.getDayOfYear()));
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
            // Download/QCDownload -> Mozaic
            outputFolder = String.format("%s%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), pPlugin.GetName(), ProcessName.PROCESSOR),
                    "FilterMosaic", date.getYear(), date.getDayOfYear());
            break;
        case 2:
            // Mozaic -> Reproject
            outputFolder = String.format("%s%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), pPlugin.GetName(), ProcessName.PROCESSOR),
                    "Reproject", date.getYear(), date.getDayOfYear());
            break;
        case 3:
            // Reproject -> Mask
            outputFolder = String.format("%s%s" + File.separator + "%04d" + File.separator+"%03d",
                    FileSystem.GetProcessWorkerTempDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), pPlugin.GetName(), ProcessName.PROCESSOR),
                    "Mask", date.getYear(), date.getDayOfYear());
            break;
        case 4:
            // Mask -> Output (Clip)
            outputFolder = String.format("%s%04d" + File.separator+"%03d",
                    FileSystem.GetProcessOutputDirectoryPath(project.GetWorkingDir(), project.GetProjectName(), pPlugin.GetName(), ProcessName.PROCESSOR),
                    date.getYear(), date.getDayOfYear());
            break;
        default:
            outputFolder = null;
            break;
        }

        return outputFolder;
    }

    @Override
    public int[] getDataBands() {
        return new int[] {1, 2, 3, 4, 5, 6, 7};
    }

    @Override
    public int[] getQCBands() {
        // 1) BRDF_Albedo_Quality
        // 2) Snow_BRDF_Albedo
        // 3) BRDF_Albedo_Ancillary
        // 4) BRDF_Albedo_Band_Quality*
        // * only one we care about
        return new int[] {1, 2, 3, 4};
    }
}
