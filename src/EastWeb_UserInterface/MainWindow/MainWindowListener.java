package EastWeb_UserInterface.MainWindow;

import java.util.EventListener;

public interface MainWindowListener extends EventListener {
    /** Called whenever the sun changes position
     *   in a SunEvent source object
     */
    public void RefreshProjectList(MainWindowEventObject e);
}
