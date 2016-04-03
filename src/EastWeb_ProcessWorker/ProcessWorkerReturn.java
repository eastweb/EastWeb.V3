package EastWeb_ProcessWorker;

import java.util.ArrayList;

import Utilies.DataFileMetaData;

public class ProcessWorkerReturn {
    public final ArrayList<DataFileMetaData> filesToCache;

    public ProcessWorkerReturn(ArrayList<DataFileMetaData> filesToCache)
    {
        this.filesToCache = filesToCache;
    }
}