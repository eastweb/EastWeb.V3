package version2.prototype.util;

import java.util.EventObject;

@SuppressWarnings("serial")
public class GeneralUIEventObject  extends EventObject{
    private Integer progress;
    private String status;
    private String pluginName;

    /**
     * constructor
     * @param source
     * @param plugin
     */
    public GeneralUIEventObject(Object source, String status, Integer progress, String pluginName) {
        super(source);
        this.status = status;
        this.progress = progress;
        this.pluginName = pluginName;
    }

    /** return whether the sun rose or set */
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
     *
     * @return String - plugin name
     */
    public String getPluginName(){
        return pluginName;
    }
}
