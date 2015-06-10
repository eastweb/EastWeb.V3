package version2.prototype.processor;

import version2.prototype.Process;
import version2.prototype.ProcessWorker;

public class ProcessorWorker extends ProcessWorker<Void> {

    protected ProcessorWorker(String processWorkerName, Process<?> process)
    {
        super(processWorkerName, process);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Void call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
