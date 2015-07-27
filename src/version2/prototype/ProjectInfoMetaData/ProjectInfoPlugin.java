package version2.prototype.ProjectInfoMetaData;

import java.util.ArrayList;

/**
 * @author michael.devos
 *
 */
public class ProjectInfoPlugin {
    private String Name;
    private ArrayList<String> Inidicies;
    private String QC;

    /**
     * Create a ProjectInfoPlugin object that contains the plugin name, list of index names to calculate, and the QC level to use. This information is generally gotten from a parsed project metadata file
     * in a "Plugin" node child of a "Plugins" node.
     *
     * @param name  - plugin name
     * @param inidicies  - list of indices to calculate by their names
     * @param qc  - QC level
     */
    public ProjectInfoPlugin(String name, ArrayList<String> inidicies, String qc)
    {
        Name = name;
        Inidicies = inidicies;
        QC = qc;
    }

    public String GetName()
    {
        return Name;
    }

    public ArrayList<String> GetIndicies()
    {
        return Inidicies;
    }

    public String GetQC()
    {
        return QC;
    }
}
