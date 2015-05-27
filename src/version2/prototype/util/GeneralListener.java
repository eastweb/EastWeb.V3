package version2.prototype.util;

import java.util.EventListener;

public interface GeneralListener extends EventListener {
    /** Called whenever the sun changes position
     *   in a SunEvent source object
     */
    public void NotifyUI(GeneralUIEventObject e);
}
