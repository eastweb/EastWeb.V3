package version2.prototype;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.download.GlobalDownloader;
import version2.prototype.util.DataFileMetaData;
import version2.prototype.util.DatabaseCache;
import version2.prototype.util.GeneralUIEventObject;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * Abstract framework thread management class. Frameworks are to use a concrete class that extends this class to handle creating their worker threads.
 *
 * @author michael.devos
 */
public abstract class Process implements Observer {
    public final ProcessName processName;
    public final ProjectInfoPlugin pluginInfo;
    public final ProjectInfoFile projectInfoFile;
    public final PluginMetaData pluginMetaData;
    protected Scheduler scheduler;
    protected DatabaseCache outputCache;
    protected EASTWebI manager;

    /**
     * Creates a Process object with the defined initial TaskState, owned by the given Scheduler, labeled by the given processName, and acquiring its
     * input from the specified process, inputProcessName.
     *
     * @param projectInfoFile  - the current project's information
     * @param pluginInfo  - the current plugin's general information
     * @param pluginMetaData  - the current plugin's xml data mapped
     * @param scheduler  - reference to the controlling Scheduler object
     * @param state  - TaskState to initialize this object to
     * @param processName  - name of this threaded process
     * @param outputCache  - DatabaseCache object to use when storing output of this process to notify next process of files available for processing
     */
    protected Process(EASTWebI manager, ProcessName processName, ProjectInfoFile projectInfoFile, ProjectInfoPlugin pluginInfo, PluginMetaData pluginMetaData,
            Scheduler scheduler, DatabaseCache outputCache)
    {
        this.manager = manager;
        this.processName = processName;
        this.scheduler = scheduler;
        this.pluginInfo = pluginInfo;
        this.projectInfoFile = projectInfoFile;
        this.pluginMetaData = pluginMetaData;
        this.outputCache = outputCache;
    }

    /**
     * Method to override to handle processing new input files. Called only when Scheduler TaskState is set to RUNNING and there is at least 1 available cached file to process.
     *
     * @param cachedFiles  - List of cache files available to process. Can always assume size is 1 or greater when called.
     */
    public abstract void process(ArrayList<DataFileMetaData> cachedFiles);

    /**
     * Bubbles up progress update information to the GUI.
     *
     * @param e  - progress update
     */
    public final void NotifyUI(GeneralUIEventObject e)
    {
        scheduler.NotifyUI(e);
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        if(scheduler.GetSchedulerStatus().GetState() == TaskState.RUNNING)
        {
            if(o instanceof DatabaseCache)
            {
                DatabaseCache inputCache = (DatabaseCache) o;
                ArrayList<DataFileMetaData> cachedFiles = new ArrayList<DataFileMetaData>();
                try {
                    cachedFiles = inputCache.GetUnprocessedCacheFiles();
                    if(cachedFiles.size() > 0) {
                        process(cachedFiles);
                    }

                    // TODO: Need to define when "finished" state has been reached as this doesn't work with asynchronous.
                    //                scheduler.NotifyUI(new GeneralUIEventObject(this, "Summary Finished", 100, pluginInfo.GetName()));
                }
                catch (ConfigReadException | ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SAXException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else if(o instanceof GlobalDownloader)
            {
                try {
                    final Connection conn = PostgreSQLConnection.getConnection();
                    Schemas.updateExpectedResults(Config.getInstance().getGlobalSchema(), projectInfoFile.GetProjectName(), pluginInfo.GetName(), projectInfoFile.GetStartDate(),
                            pluginMetaData.DaysPerInputData, pluginInfo.GetIndices().size(), projectInfoFile.GetSummaries(), conn);
                    conn.close();
                    process(null);
                }
                catch (ClassNotFoundException | SQLException | ParserConfigurationException | SAXException |
                        IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
