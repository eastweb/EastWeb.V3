package version2.prototype;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.GeneralUIEventObject;

public abstract class Process<V> implements Callable<V>, Observer {
    public ProcessName processName;
    protected ThreadState mState;
    protected Scheduler scheduler;
    protected ProjectInfoPlugin pluginInfo;
    protected ProjectInfoFile projectInfoFile;
    protected PluginMetaData pluginMetaData;
    protected String mInputTableName;
    protected ExecutorService executor;

    protected Process(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, ThreadState state, ProcessName processName, String inputTableName, ExecutorService executor)
    {
        this.processName = processName;
        mState = state;
        this.scheduler = scheduler;
        this.pluginInfo = pluginInfo;
        this.projectInfoFile = projectInfoFile;
        this.pluginMetaData = pluginMetaData;
        mInputTableName = inputTableName;
        this.executor = executor;
    }

    public void NotifyUI(GeneralUIEventObject e)
    {
        this.scheduler.NotifyUI(e);
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
