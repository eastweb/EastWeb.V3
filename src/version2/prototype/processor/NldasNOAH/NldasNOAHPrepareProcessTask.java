package version2.prototype.processor.NldasNOAH;

import java.util.ArrayList;

import version2.prototype.DataDate;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.processor.PrepareProcessTask;

public class NldasNOAHPrepareProcessTask extends PrepareProcessTask {

    public NldasNOAHPrepareProcessTask(ProjectInfoFile mProject,
            ProjectInfoPlugin mPlugin, DataDate mDate) {
        super(mProject, mPlugin, mDate);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String[] getInputFolders(int stepId) {
        // TODO Auto-generated method stub

        ArrayList<String> inputFolders = new ArrayList<String>();
        /*
        Step1: Composite: Download->Composite
        Step2: Reproject: Composite->Reproject
        Step3: Mask: Reproject->Mask
         */
        switch(stepId)
        {
        case 1:
            inputFolders.add(project.GetWorkingDir() + String.format("NldasNOAH\\Download\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 2:
            inputFolders.add(project.GetWorkingDir() + String.format("NldasNOAH\\1\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        case 3:
            inputFolders.add(project.GetWorkingDir() + String.format("NldasNOAH\\2\\%4d\\%03d", date.getYear(), date.getDayOfYear()));
            break;
        default:
            inputFolders = null;
            break;
        }

        return inputFolders.toArray(new String[inputFolders.size()]);
    }

    @Override
    public String getOutputFolder(int stepId) {
        // TODO Auto-generated method stub
        String outputFolder = "";
        /*
        Step1: Composite: Download->Composite
        Step2: Reproject: Composite->Reproject
        Step3: Mask: Reproject->Mask
         */

        switch(stepId)
        {
        case 1:
            outputFolder = project.GetWorkingDir() + String.format("NldasNOAH\\1\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 2:
            outputFolder = project.GetWorkingDir() + String.format("NldasNOAH\\2\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        case 3:
            outputFolder = project.GetWorkingDir() + String.format("NldasNOAH\\3\\%4d\\%03d", date.getYear(), date.getDayOfYear());
            break;
        default:
            outputFolder = null;
            break;
        }

        return outputFolder;
    }

    @Override
    public int[] getDataBands() {
        // TODO Auto-generated method stub
        /*
        NOAH
        -019 -> 85:TSOIL0_10cm:0-10cmSoiltemperature[K]
        -020 -> 85:TSOIL10_40cm:10-40cmSoiltemperature[K]
        -021 -> 85:TSOIL40_100cm:40-100cmSoiltemperature[K]
        -022 -> 85:TSOIL100_200cm:100-200cmSoiltemperature[K]
        -030 -> 151:LSOIL0_10cm:0-10cmLiquidSoilMoistureContent(non-frozen)[kg/m^2]
        -031 -> 151:LSOIL10_40cm:10-40cmLiquidSoilMoistureContent(non-frozen)[kg/m^2]
        -032 -> 151:LSOIL40_100cm:40-100cmLiquidSoilMoistureContent(non-frozen)[kg/m^2]
        -033 -> 151:LSOIL100_200cm:100-200cmLiquidSoilMoistureContent(non-frozen)[kg/m^2]
        -042 -> 66:SNOD:SnowDepth[m]
        -043 -> 238:SNOWC:SnowCover[fraction]
         */
        int[] dataBands = {19, 20, 21, 22, 30, 31, 32, 33, 42, 43};

        return dataBands;
    }

    @Override
    public int[] getQCBands() {
        // TODO Auto-generated method stub
        return null;
    }



}
