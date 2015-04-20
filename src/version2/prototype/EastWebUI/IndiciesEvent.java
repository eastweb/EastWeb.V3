package version2.prototype.EastWebUI;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

public class IndiciesEvent implements Serializable {
    private transient Vector listeners;

    /** Register a listener for SunEvents */
    synchronized public void addListener(IndiciesListener l) {
        if (listeners == null) {
            listeners = new Vector();
        }
        listeners.addElement(l);
    }

    /** Remove a listener for SunEvents */
    synchronized public void removeListener(IndiciesListener l) {
        if (listeners == null) {
            listeners = new Vector();
        }
        listeners.removeElement(l);
    }

    /** Fire a SunEvent to all registered listeners */
    protected void fire(String p) {
        // if we have no listeners, do nothing...
        if (listeners != null && !listeners.isEmpty()) {
            // create the event object to send
            IndiciesEventObject event = new IndiciesEventObject(this, p);

            // make a copy of the listener list in case
            //   anyone adds/removes listeners
            Vector targets;
            synchronized (this) {
                targets = (Vector) listeners.clone();
            }

            // walk through the listener list and
            //   call the sunMoved method in each
            Enumeration e = targets.elements();
            while (e.hasMoreElements()) {
                IndiciesListener l = (IndiciesListener) e.nextElement();
                l.AddPlugin(event);
            }
        }
    }
}
