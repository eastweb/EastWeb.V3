package version2.prototype;

import java.util.Observer;
import java.util.concurrent.Callable;

import version2.prototype.util.GeneralListener;

public abstract class ProcessWorker<V> implements Callable<V>, Observer {
    public String processWorkerName;
    protected Process<?> process;
    protected GeneralListener listener;

    protected ProcessWorker(String processWorkerName, Process<?> process, GeneralListener listener)
    {
        this.processWorkerName = processWorkerName;
        this.process = process;
        this.listener = listener;
    }
}
