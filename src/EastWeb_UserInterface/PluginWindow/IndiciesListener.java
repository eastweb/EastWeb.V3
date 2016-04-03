package EastWeb_UserInterface.PluginWindow;

import java.util.EventListener;

public interface IndiciesListener extends EventListener {
    /** Called whenever the sun changes position
     *   in a SunEvent source object
     */
    public void AddPlugin(PluginEventObject e);
}
