package version2.prototype.Scheduler;

import java.util.Observable;

import version2.prototype.TaskState;

/**
 * Observable state object specific to Scheduler class. Allows those listening to updates to the state to automatically be notified of any changes to it.
 *
 * @author michael.devos
 *
 */
public class SchedulerState extends Observable{
    private TaskState state;

    /**
     * Create a SchedulerState object defaulted to TaskState.RUNNING.
     */
    public SchedulerState()
    {
        state = TaskState.RUNNING;
    }

    /**
     * Changes the state of the internal TaskState object to that specified.
     *
     * @param state  - TaskState to change internal state object to.
     */
    public void ChangeState(TaskState state)
    {
        synchronized(this)
        {
            this.state = state;
        }
        setChanged();
        notifyObservers(state);     // Automatically calls clearChanged. Actively updates observers (the calling thread executes observer Update methods)
    }

    /**
     * Gets the current value of the internal TaskState object.
     *
     * @return value of internal TaskState object representing the state of a Scheduler.
     */
    public TaskState GetState()
    {
        return state;
    }

}
