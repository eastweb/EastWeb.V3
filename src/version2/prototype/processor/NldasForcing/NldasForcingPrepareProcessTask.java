package version2.prototype.processor.NldasForcing;

import java.util.ArrayList;

import version2.prototype.DataDate;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.processor.PrepareProcessTask;

public class NldasForcingPrepareProcessTask extends PrepareProcessTask {

    public NldasForcingPrepareProcessTask(ProjectInfoFile mProject,
            ProjectInfoPlugin mPlugin, DataDate mDate) {
        super(mProject, mPlugin, mDate);
    }

    @Override
    public String[] getInputFolders(int stepId)
    {
        ArrayList<String> folders = new ArrayList<String>();

        // Format: Input (of this step) -> Output (of this step)
        switch(stepId)
        {
        case 1:
            // Download -> NldasForcingComposite
            //try { FileSystem.GetGlobalDownloadDirectory(Config.getInstance(), "NldasForcing"); }
            //catch (ParserConfigurationException | SAXException | IOException e) { e.printStackTrace(); }
            folders.add(project.GetWorkingDir() + String.format("Download\\%4d\\%03d\\", date.getYear(), date.getDayOfYear()));
            break;
        case 2:
            // NldasForcingComposite -> Reproject
            folders.add(project.GetWorkingDir() + String.format("NldasForcingComposite\\%4d\\%03d\\", date.getYear(), date.getDayOfYear()));
            break;
        case 3:
            // Reproject -> Mask
            folders.add(project.GetWorkingDir() + String.format("Reproject\\%4d\\%03d\\", date.getYear(), date.getDayOfYear()));
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
            outputFolder = project.GetWorkingDir() + String.format("NldasForcingComposite\\%4d\\%03d\\", date.getYear(), date.getDayOfYear());
            break;
        case 2:
            // Mozaic -> Reproject
            outputFolder = project.GetWorkingDir() + String.format("Reproject\\%4d\\%03d\\", date.getYear(), date.getDayOfYear());
            break;
        case 3:
            // Reproject -> Mask
            outputFolder = project.GetWorkingDir() + String.format("Output\\%4d\\%03d\\", date.getYear(), date.getDayOfYear());
            break;
        default:
            outputFolder = null;
            break;
        }

        return outputFolder;
    }

    @Override
    public int[] getDataBands() {
        return new int[]
                {1, // air temperature (K) at 2 meters above the surface
                2,  // specific humidity (kg/kg) at 2 meters above the surface
                3,  // surface pressure (Pa)
                4,  // U wind component (m/s) at 10 meters above the surface
                5,  // V wind component (m/s) at 10 meters above the surface
                6,  // surface downward longwave radiation (W/m^2)
                7,  // fraction of total precipitation that is convective (no units): from NARR
                8,  // CAPE: Convective Available Potential Energy (J/kg): from NARR
                9,  // potential evaporation (kg/m^2): from NARR
                10, // precipitation hourly total (kg/m^2)
                11};// surface downward shortwave radiation (W/m^2) -- bias-corrected
    }

    @Override
    public int[] getQCBands() {
        return null;
    }
}
