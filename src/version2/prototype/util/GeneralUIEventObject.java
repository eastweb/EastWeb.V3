package version2.prototype.util;

import java.util.EventObject;

@SuppressWarnings("serial")
public class GeneralUIEventObject  extends EventObject{
    private int progress;
    private String status;

    /**
     * constructor
     * @param source
     * @param plugin
     */
    public GeneralUIEventObject(Object source, String status, int progress) {
        super(source);
        this.status = status;
        this.progress = progress;
    }

    /** return whether the sun rose or set */
    public String getStatus() {
        return status;
    }

    public int getProgress(){
        return progress;
    }
}
