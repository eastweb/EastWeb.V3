package version2.prototype.projection.ModisNBAR;

import java.util.ArrayList;

import version2.prototype.DataDate;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.projection.PrepareProcessTask;

public class ModisNBARPrepareProcessTask extends PrepareProcessTask {

    public ModisNBARPrepareProcessTask(ProjectInfoFile mProject,
            ProjectInfoPlugin mPlugin, DataDate mDate) {
        super(mProject, mPlugin, mDate);
    }

    @Override
    public String[] getInputFolders(int stepId) {
        ArrayList<String> folders = new ArrayList<String>();

        // Format: Input (of this step) -> Output (of this step)
        switch(stepId)
        {
        case 1:
            // Download -> Mozaic
            folders.add("C:\\Users\\Chris Plucker\\Desktop\\MODIS_NBAR\\Version2\\" + String.format("Download\\%4d\\%03d\\", date.getYear(), date.getDayOfYear()));
            folders.add("C:\\Users\\Chris Plucker\\Desktop\\MODIS_NBAR\\Version2\\" + String.format("QCDownload\\%4d\\%03d\\", date.getYear(), date.getDayOfYear()));
            break;
        case 2:
            // Mozaic -> Reproject
            folders.add("C:\\Users\\Chris Plucker\\Desktop\\MODIS_NBAR\\Version2\\" + String.format("1\\%4d\\%03d\\", date.getYear(), date.getDayOfYear()));
            break;
        case 3:
            // Reproject -> Mask
            folders.add("C:\\Users\\Chris Plucker\\Desktop\\MODIS_NBAR\\Version2\\" + String.format("2\\%4d\\%03d\\", date.getYear(), date.getDayOfYear()));
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
            outputFolder = "C:\\Users\\Chris Plucker\\Desktop\\MODIS_NBAR\\Version2\\" + String.format("1\\%4d\\%03d\\", date.getYear(), date.getDayOfYear());
            break;
        case 2:
            // Mozaic -> Reproject
            outputFolder = "C:\\Users\\Chris Plucker\\Desktop\\MODIS_NBAR\\Version2\\" + String.format("2\\%4d\\%03d\\", date.getYear(), date.getDayOfYear());
            break;
        case 3:
            // Reproject -> Mask
            outputFolder = "C:\\Users\\Chris Plucker\\Desktop\\MODIS_NBAR\\Version2\\" + String.format("3\\%4d\\%03d\\", date.getYear(), date.getDayOfYear());
            break;
        default:
            outputFolder = null;
            break;
        }

        return outputFolder;
    }

    @Override
    public int[] getDataBands() {
        return new int[] {1, 2, 3, 4, 5, 6, 7 };
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
