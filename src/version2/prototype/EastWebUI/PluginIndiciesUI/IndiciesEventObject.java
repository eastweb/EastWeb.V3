package version2.prototype.EastWebUI.PluginIndiciesUI;

import java.util.EventObject;

public class IndiciesEventObject extends EventObject {
    private String plugin;

    public IndiciesEventObject(Object source, String plugin) {
        super(source);
        this.plugin = plugin;
    }

    /** return whether the sun rose or set */
    public String getPlugin() {
        return plugin;
    }
}
