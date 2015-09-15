package version2.prototype.EastWebUI.PluginIndiciesUI;

import java.util.ArrayList;
import java.util.EventObject;

@SuppressWarnings("serial")
public class IndiciesEventObject extends EventObject {
    private String plugin;
    private ArrayList<String> globalModisTiles;

    /**
     * constructor
     * @param source
     * @param plugin
     */
    public IndiciesEventObject(Object source, String plugin, ArrayList<String> globalModisTiles) {
        super(source);
        this.plugin = plugin;
        this.globalModisTiles = new ArrayList<String>();

        for(String temp : globalModisTiles)
        {
            this.globalModisTiles.add(temp);
        }
    }

    /** return whether the sun rose or set */
    public String getPlugin() {
        return plugin;
    }

    public ArrayList<String> getTiles()
    {
        return globalModisTiles;
    }
}
