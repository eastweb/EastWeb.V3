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

    public String GetName()
    {
        return Name;
    }

    public ArrayList<String> GetIndices()
    {
        return Indices;
    }

    public String GetQC()
    {
        return QC;
    }
}
