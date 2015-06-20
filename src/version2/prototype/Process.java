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

/**
 * Abstract framework thread management class. Frameworks are to use a concrete class that extends this class to handle creating their worker threads.
 *
 * @author michael.devos
 *
 * @param <V>
 */
public abstract class Process<V> implements Callable<V>, Observer {
    public ProcessName processName;
    protected ThreadState mState;
    protected Scheduler scheduler;
    protected ProjectInfoPlugin pluginInfo;
    protected ProjectInfoFile projectInfoFile;
    protected PluginMetaData pluginMetaData;
    protected ProcessName inputProcessName;
    protected ExecutorService executor;

    /**
     * Creates a Process object with the defined initial ThreadState, owned by the given Scheduler, labeled by the given processName, and acquiring its input from
     * the specified process, inputProcessName.
     *
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param scheduler  - reference to the controlling Scheduler object
     * @param state  - ThreadState to initialize this object to
     * @param processName  - name of this threaded process
     * @param inputProcessName  - name of process to use the output of for its input
     * @param executor  - executor service to use to spawn worker threads
     */
    protected Process(ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, ThreadState state, ProcessName processName, ProcessName inputProcessName, ExecutorService executor)
    {
        this.processName = processName;
        mState = state;
        this.scheduler = scheduler;
        this.pluginInfo = pluginInfo;
        this.projectInfoFile = projectInfoFile;
        this.pluginMetaData = pluginMetaData;
        this.inputProcessName = inputProcessName;
        this.executor = executor;
    }

    /**
     * Bubbles up progress update information to the GUI.
     *
     * @param e  - progress update
     */
    public void NotifyUI(GeneralUIEventObject e)
    {
        this.scheduler.NotifyUI(e);
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
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
