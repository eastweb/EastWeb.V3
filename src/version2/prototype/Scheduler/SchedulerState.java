package version2.prototype.Scheduler;

import java.util.Observable;

import version2.prototype.ThreadState;

/**
 * Observable state object specific to Scheduler class. Allows those listening to updates to the state to automatically be notified of any changes to it.
 *
 * @author michael.devos
 *
 */
public class SchedulerState extends Observable{
    private ThreadState state;

    /**
     * Create a SchedulerState object defaulted to ThreadState.RUNNING.
     */
    public SchedulerState()
    {
        state = ThreadState.RUNNING;
    }

    /**
     * Changes the state of the internal ThreadState object to that specified.
     *
     * @param state  - ThreadState to change internal state object to.
     */
    public void ChangeState(ThreadState state)
    {
        synchronized(this)
        {
            this.state = state;
        }
        setChanged();
        notifyObservers(state);     // Automatically calls clearChanged
    }

    /**
     * Gets the current value of the internal ThreadState object.
     *
     * @return value of internal ThreadState object representing the state of a Scheduler.
     */
    public ThreadState GetState()
    {
        return state;
    }

}
