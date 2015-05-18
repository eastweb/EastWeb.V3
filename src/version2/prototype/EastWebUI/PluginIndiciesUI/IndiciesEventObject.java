package version2.prototype.EastWebUI.PluginIndiciesUI;

import java.util.EventObject;

@SuppressWarnings("serial")
public class IndiciesEventObject extends EventObject {
    private String plugin;

    /**
     * constructor
     * @param source
     * @param plugin
     */
    public IndiciesEventObject(Object source, String plugin) {
        super(source);
        this.plugin = plugin;
    }

    /** return whether the sun rose or set */
    public String getPlugin() {
        return plugin;
    }
}
