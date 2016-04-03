package EastWeb_UserInterface.MainWindow;

import java.util.EventObject;

@SuppressWarnings("serial")
public class MainWindowEventObject extends EventObject {
    /**
     * main window event object to be use to pass
     * @param source
     */
    public MainWindowEventObject(Object source) {
        super(source);
    }
}
