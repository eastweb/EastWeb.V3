package version2.prototype.EastWebUI.PluginUI_v2;

import java.util.EventObject;

@SuppressWarnings("serial")
public class PluginEventObject extends EventObject {
    private IPlugin plugin;

    /**
     * constructor
     * @param source
     * @param plugin
     */
    public PluginEventObject(Object source, IPlugin plugin) {
        super(source);
        this.plugin = plugin;
    }

    /** return whether the sun rose or set */
    public IPlugin getPlugin() {
        return plugin;
    }
}
