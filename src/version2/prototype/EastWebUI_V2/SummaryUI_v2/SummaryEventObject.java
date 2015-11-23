package version2.prototype.EastWebUI_V2.SummaryUI_v2;

import java.util.EventObject;

@SuppressWarnings("serial")
public class SummaryEventObject extends EventObject {
    private String summary;

    /**
     * constructor
     * @param source
     * @param summary
     */
    public SummaryEventObject(Object source, String summary) {
        super(source);
        this.summary = summary;
    }

    /** return whether the sun rose or set */
    public String getPlugin() {
        return summary;
    }
}
