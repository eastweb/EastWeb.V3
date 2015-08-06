package version2.prototype.Scheduler;

import java.io.File;
import java.io.IOException;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;

/**
 * @author michael.devos
 *
 */
public class SchedulerData {
    public ProjectInfoFile projectInfoFile;
    public PluginMetaDataCollection pluginMetaDataCollection;

    /**
     * Creates a SchedulerData object containing the project's metadata and the collection of available plugin metadata.
     *
     * @param projectInfoFile  - the project metadata to use within the Scheduler this object is sent to
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws DOMException
     * @throws PatternSyntaxException
     */
    public SchedulerData(ProjectInfoFile projectInfoFile) throws PatternSyntaxException, DOMException, ParserConfigurationException, SAXException, IOException
    {
        this.projectInfoFile= projectInfoFile;
        pluginMetaDataCollection = PluginMetaDataCollection.getInstance();
    }

    /**
     * Creates a SchedulerData object containing the given project metadata and the plugin metadata from the given file path.
     *
     * @param projectInfoFile  - the project metadata to use within the Scheduler this object is sent to
     * @param pluginMetaDataFile  - the path to the plugin metadata xml to use within the Scheduler this object is sent to
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws DOMException
     * @throws PatternSyntaxException
     */
    public SchedulerData(ProjectInfoFile projectInfoFile, String pluginMetaDataFile) throws ParserConfigurationException, SAXException, IOException, DOMException, PatternSyntaxException
    {
        this.projectInfoFile = projectInfoFile;
        if(pluginMetaDataFile != null) {
            pluginMetaDataCollection = PluginMetaDataCollection.getInstance(new File(pluginMetaDataFile));
        }
    }
}
