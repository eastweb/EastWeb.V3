package version2.prototype.util;

import java.util.EventObject;

@SuppressWarnings("serial")
public class GeneralUIEventObject  extends EventObject{
    private String status;

    /**
     * constructor
     * @param source
     * @param plugin
     */
    public GeneralUIEventObject(Object source, String status) {
        super(source);
        this.status = status;
    }

    /** return whether the sun rose or set */
    public String getStatus() {
        return status;
    }
}
