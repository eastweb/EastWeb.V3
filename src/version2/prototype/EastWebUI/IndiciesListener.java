package version2.prototype.EastWebUI;

import java.util.EventListener;

public interface IndiciesListener extends EventListener {
    /** Called whenever the sun changes position
     *   in a SunEvent source object
     */
    public void AddPlugin(IndiciesEventObject e);
}
