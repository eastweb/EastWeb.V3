package version2.prototype.util;

import java.util.EventObject;

/**
 * @author michael.devos
 * @author sufiabdul
 *
 */
@SuppressWarnings("serial")
public class GeneralUIEventObject  extends EventObject{
    private Integer progress;
    private String status;
    private String pluginName;
    private Integer summaryID;

    /**
     * Creates a GeneralUIEventObject for a progress update not related to summary progress.
     * @param source
     * @param status
     * @param progress
     * @param pluginName
     */
    public GeneralUIEventObject(Object source, String status, Integer progress, String pluginName) {
        super(source);
        this.status = status;
        this.progress = progress;
        this.pluginName = pluginName;
        summaryID = null;
    }

    /**
     * Creates a GeneralUIEventObject set for summary progress update.
     * @param source
     * @param status
     * @param progress
     * @param pluginName
     * @param summaryID
     */
    public GeneralUIEventObject(Object source, String status, Integer progress, String pluginName, Integer summaryID) {
        super(source);
        this.status = status;
        this.progress = progress;
        this.pluginName = pluginName;
        this.summaryID = summaryID;
    }

    /** return whether the sun rose or set
     * @return String arbitrary status string appended to the log
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the progress of this associated GeneralUIEventObject's source instance and its assigned plugin.
     * @return int - progress percentage out of 100
     */
    public int getProgress(){
        return progress;
    }

    /**
     * Returns the plugin name of this associated GeneralUIEventObject's source instance.
     * @return String - plugin name
     */
    public String getPluginName(){
        return pluginName;
    }

    /**
     * Gets the ID associated to the relevant summary from the project metadata.
     * @return Summary ID attribute value
     */
    public Integer getSummaryID()
    {
        return summaryID;
    }
}
