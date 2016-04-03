package EastWeb_UserInterface.PluginWindow.PluginExtension.Modis;

import java.util.EventListener;

public interface ModisListener extends EventListener {
    /** Called whenever the sun changes position
     *   in a SunEvent source object
     */
    public void AddPlugin(ModisEventObject e);
}
