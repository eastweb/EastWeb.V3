package version2.prototype;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.util.DataFileMetaData;

public abstract class ProcessWorker<V> implements Callable<V>, Observer {
    public String processWorkerName;
    protected Process<?> process;
    protected ThreadState mState;
    protected ProjectInfoFile projectInfoFile;
    protected ProjectInfoPlugin pluginInfo;
    protected PluginMetaData pluginMetaData;
    protected ArrayList<DataFileMetaData> cachedFiles;

    protected ProcessWorker(String processWorkerName, Process<?> process, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo,
            PluginMetaData pluginMetaData, ArrayList<DataFileMetaData> cachedFiles)
    {
        this.processWorkerName = processWorkerName;
        this.process = process;
        this.projectInfoFile = projectInfoFile;
        this.pluginInfo = pluginInfo;
        this.pluginMetaData = pluginMetaData;
        this.cachedFiles = cachedFiles;
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
