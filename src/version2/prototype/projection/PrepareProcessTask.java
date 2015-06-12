package version2.prototype.projection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import version2.prototype.DataDate;
import version2.prototype.Projection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.util.GeneralListener;

/* Overridden by each plugin's prepareProcessTask class
 * name convention:  Plugin_namePrepareProcessTask.class
 */
public abstract class PrepareProcessTask {
    private ProjectInfoFile project;
    private final DataDate date;
    private final ProjectInfoPlugin plugin;

    public PrepareProcessTask(ProjectInfoFile mProject, ProjectInfoPlugin mPlugin, DataDate mDate, GeneralListener l) {
        project = mProject;
        date = mDate;
        plugin = mPlugin;
    }

    /* pre-condition: input the ID of the step specified in the plugin metadata
     * post-condition: return the temp folder(s) for getting the input files for processing step with "stepId"
     */
    public abstract String [] getInputFolders(int stepId);

    /* pre-condition: input the ID of the step specified in the plugin metadata
     * post-condition: return the temp folder to write the output to after the step with "stepId" is done
     */
    public abstract String getOutputFolder(int stepId);

    /* post-condition: return the bands of the data of the plugin */
    public abstract int [] getDataBands();

    /* post-condition:
     *          return the QC bands of the plugin if there is any
     *          if no QC bands, return null
     */
    public abstract int [] getQCBands();

    /* post-condition: return the master shapefile given in the project */
    public String getShapeFile() {
        return project.GetMasterShapeFile() ;
    }

    /* post-condition:
     *          return the mask file given in the project if there is any
     */
    public String getMaskFile() {
        return project.GetMaskingFile();
    }

    // post-condition: return the set projection for the plugin in the project
    public Projection getProjection(){
        return project.GetProjection();
    }

    // post-condition: return the set qcLevel for the plugin in the project
    public String getQC(){
        return plugin.GetQC();
    }


}