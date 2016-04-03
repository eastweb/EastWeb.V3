package EastWeb_UserInterface.SummaryWindow;

import java.util.EventListener;

public interface SummaryListener extends EventListener {
    /** Called whenever the sun changes position
     *   in a SunEvent source object
     */
    public void AddSummary(SummaryEventObject e);
}
