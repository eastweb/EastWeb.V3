package version2.prototype.processor;


import java.time.LocalDate;

import version2.prototype.DataDate;
import version2.prototype.Projection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;

/* Overridden by each plugin's prepareProcessTask class
 * name convention:  Plugin_namePrepareProcessTask.class
 */
public abstract class PrepareProcessTask {
    protected ProjectInfoFile project;
    protected final DataDate date;
    protected final ProjectInfoPlugin plugin;

    public PrepareProcessTask(ProjectInfoFile mProject, ProjectInfoPlugin mPlugin, DataDate mDate) {
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
        return project.GetMasterShapeFile();
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

    // post-condition: return the mask resolution for the maskfile in the project
    public Integer getMaskResolution()
    {   return project.GetMaskingResolution();  }

    // post-condition: return true if clipping is needed; otherwise, false
    public Boolean getClipOrNot()
    {   return project.GetClipping();   }

    // post-condition: return freezing date use set in the project
    public LocalDate getFreezingDate()
    {   return project.GetFreezingDate();   }

    // post-condition: return heating date use set in the project
    public LocalDate getHeatingDate()
    {   return project.GetHeatingDate();    }
}
