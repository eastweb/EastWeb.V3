package version2.prototype.util;

import java.util.EventObject;

/**
 * @author michael.devos
 * @author sufiabdul
 *
 */
@SuppressWarnings("serial")
public class GeneralUIEventObject  extends EventObject{
    private final Double progress;
    private final String status;
    private final String pluginName;
    private final String dataName;
    private final Integer expectedNumOfOutputs;
    private final Integer summaryID;
    //    private final Integer currentPublished;

    /**
     * Creates a GeneralUIEventObject for a progress update solely for sending a log entry (status).
     * @param source  - the object that's creating this object
     * @param status  - message to append to the log
     */
    public GeneralUIEventObject(Object source, String status) {
        super(source);
        this.status = status;
        progress = null;
        pluginName = null;
        dataName = null;
        expectedNumOfOutputs = null;
        summaryID = null;
    }

    /**
     * Creates a GeneralUIEventObject for a progress update related to download if dataName is not null.
     * @param source  - the object that's creating this object
     * @param status   - message to append to the log
     * @param progress  - the current percent complete within this Process (defined for the current plugin and Scheduler/project)
     * @param pluginName  - plugin name the progress is associated with (the process information is gotten from the source object directly)
     * @param dataName  - name of download data (if null this is the same as calling GeneralUIEventObject(Object, String, Double, String))
     * @param expectedDataFiles  - number of data files expected for the associated plugin if this is a download progress update for the "data" files. If pluginName.equalsIgnoreCase("Data") is false
     * then this is set to null internally.
     */
    public GeneralUIEventObject(Object source, String status, Double progress, String pluginName, String dataName, Integer expectedDataFiles)
    {
        super(source);
        this.status = status;
        this.progress = progress;
        this.pluginName = pluginName;
        this.dataName = dataName;
        if(dataName.equalsIgnoreCase("Data")) {
            expectedNumOfOutputs = expectedDataFiles;
        } else {
            expectedNumOfOutputs = null;
        }
        summaryID = null;
    }

    /**
     * Creates a GeneralUIEventObject for a progress update not related to summary progress.
     * @param source  - the object that's creating this object
     * @param status  - message to append to the log
     * @param progress  - the current percent complete within this Process (defined for the current plugin and Scheduler/project)
     * @param pluginName  - plugin name the progress is associated with (the process information is gotten from the source object directly)
     * @param expectedNumOfOutputs
     */
    public GeneralUIEventObject(Object source, String status, Double progress, String pluginName, int expectedNumOfOutputs) {
        super(source);
        this.status = status;
        this.progress = progress;
        this.pluginName = pluginName;
        dataName = null;
        this.expectedNumOfOutputs = expectedNumOfOutputs;
        summaryID = null;
    }

    /**
     * Creates a GeneralUIEventObject set for summary progress update.
     * @param source  - the object that's creating this object
     * @param status  - message to append to the log
     * @param progress  - the current percent complete within this Process (defined for the current plugin and Scheduler/project)
     * @param pluginName  - plugin name the progress is associated with (the process information is gotten from the source object directly)
     * @param summaryID  - the ID of the summary being processed defined by the project metadata
     * @param expectedNumOfOutputs
     */
    public GeneralUIEventObject(Object source, String status, Double progress, String pluginName, int summaryID, int expectedNumOfOutputs) {
        super(source);
        this.status = status;
        this.progress = progress;
        this.pluginName = pluginName;
        dataName = null;
        this.expectedNumOfOutputs = expectedNumOfOutputs;
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
    public Double getProgress(){
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
     * Returns the name of the download data this GeneralUIEventObject is associated with.
     * @return String - data name
     */
    public String getDataName(){
        return dataName;
    }

    /**
     * Returns the number of expected data files used as part of the download progress calculation.
     * @return Integer - number of data files expected for the associated plugin if this is a download progress update for the "data" files, otherwise null.
     */
    public Integer getExpectedNumOfOutputs(){
        return expectedNumOfOutputs;
    }

    /**
     * Gets the ID associated to the relevant summary from the project metadata.
     * @return Summary ID attribute value
     */
    public Integer getSummaryID(){
        return summaryID;
    }
}
