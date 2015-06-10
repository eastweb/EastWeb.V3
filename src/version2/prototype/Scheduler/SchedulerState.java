package version2.prototype.Scheduler;

import java.util.Observable;

import version2.prototype.ThreadState;

public class SchedulerState extends Observable{
    private ThreadState state;

    public SchedulerState()
    {
        state = ThreadState.STOPPED;
    }

    public void ChangeState(ThreadState state)
    {
        synchronized(this)
        {
            this.state = state;
        }
        setChanged();
        notifyObservers(state);
    }

    public ThreadState GetState()
    {
        return state;
    }

}
