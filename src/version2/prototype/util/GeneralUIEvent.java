package version2.prototype.util;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;


@SuppressWarnings("serial")
public class GeneralUIEvent implements Serializable {
    @SuppressWarnings("rawtypes")
    private transient Vector listeners;

    /** Register a listener for SunEvents */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    synchronized public void addListener(GeneralListener l) {
        if (listeners == null) {
            listeners = new Vector();
        }
        listeners.addElement(l);
    }

    /** Remove a listener for SunEvents */
    @SuppressWarnings("rawtypes")
    synchronized public void removeListener(GeneralListener l) {
        if (listeners == null) {
            listeners = new Vector();
        }
        listeners.removeElement(l);
    }

    /** Fire a SunEvent to all registered listeners */
    @SuppressWarnings("rawtypes")
    protected void fire(String log, int progress) {
        // if we have no listeners, do nothing...
        if (listeners != null && !listeners.isEmpty()) {
            // create the event object to send
            GeneralUIEventObject event = new GeneralUIEventObject(this, log, progress);

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
                GeneralListener l = (GeneralListener) e.nextElement();
                l.NotifyUI(event);
            }
        }
    }
}
