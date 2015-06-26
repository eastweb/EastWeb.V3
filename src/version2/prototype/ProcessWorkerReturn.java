package version2.prototype;

import java.util.ArrayList;

import version2.prototype.util.DatabaseCache;

public class ProcessWorkerReturn {
    public final ArrayList<DatabaseCache> filesToCache;

    public ProcessWorkerReturn(ArrayList<DatabaseCache> filesToCache)
    {
        this.filesToCache = filesToCache;
    }
}
