package version2.prototype.projection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import version2.prototype.DataDate;
import version2.prototype.DirectoryLayout;
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

    public GeneralListener listener;

    public PrepareProcessTask(ProjectInfoFile mProject, ProjectInfoPlugin mPlugin, DataDate mDate, GeneralListener l) {
        project = mProject;
        date = mDate;
        plugin = mPlugin;
        listener = l;
    }

    /* pre-condition: input the ID of the step specified in the plugin metadata
     * post-condition: return the temp folder(s) for getting the input files for processing step with "stepId"
     */
    public abstract String [] getInputFolders(int stepId);

    /* pre-condition: input the ID of the step specified in the plugin metadata
     * post-condition: return the temp folder to write the output to after the step with "stepId" is done
     */
    public abstract String getOutputFolder(int stepId);

    /* post-condition: return the bands for the plugin */
    public abstract int [] getBands();

    /* post-condition: return the master shapefile given in the project */
    public String getShapeFile() {
        return project.masterShapeFile;
    }

    /* post-condition: return the mask file given in the project */
    public String getMaskFile() {
        return project.maskingFile;
    }

    // post-condition: return the set projection for the plugin in the project
    public Projection getProjection(){
        return project.getProjection();
    }

    // post-condition: return the set qcLevel for the plugin in the project
    public String getQC(){
        return plugin.GetQC();
    }

    public GeneralListener getListener(){
        return listener;
    }

}