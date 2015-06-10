package version2.prototype;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

public abstract class ProcessWorker<V> implements Callable<V>, Observer {
    public String processWorkerName;
    protected Process<?> process;
    protected ThreadState mState;

    protected ProcessWorker(String processWorkerName, Process<?> process)
    {
        this.processWorkerName = processWorkerName;
        this.process = process;
    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg instanceof ThreadState)
        {
            ThreadState state = (ThreadState) arg;
            switch(state)
            {
            case RUNNING:
                mState = ThreadState.RUNNING;
                break;
            case STOPPED:
                mState = ThreadState.STOPPED;
                break;
            }
        }
    }
}
