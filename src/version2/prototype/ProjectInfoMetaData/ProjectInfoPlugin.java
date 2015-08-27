package version2.prototype.ProjectInfoMetaData;

import java.util.ArrayList;

/**
 * @author michael.devos
 *
 */
public class ProjectInfoPlugin {
    private String Name;
    private ArrayList<String> Indices;
    private String QC;

    /**
     * Create a ProjectInfoPlugin object that contains the plugin name, list of index names to calculate, and the QC level to use. This information is generally gotten from a parsed project metadata file
     * in a "Plugin" node child of a "Plugins" node.
     *
     * @param name  - plugin name
     * @param indices  - list of indices to calculate by their names
     * @param qc  - QC level
     */
    public ProjectInfoPlugin(String name, ArrayList<String> indices, String qc)
    {
        Name = name;
        Indices = indices;
        QC = qc;
    }

    /**
     * Gets the name of the plugin being described.
     * @return the plugin name
     */
    public String GetName()
    {
        return Name;
    }

    /**
     * Gets the list of the names of the indices to process for the described plugin.
     * @return list of the names of the indices to calculate
     */
    public ArrayList<String> GetIndices()
    {
        return Indices;
    }

    /**
     * Value of the QC flag to use during processing for the described plugin.
     * @return QC flag value to use
     */
    public String GetQC()
    {
        return QC;
    }
}
