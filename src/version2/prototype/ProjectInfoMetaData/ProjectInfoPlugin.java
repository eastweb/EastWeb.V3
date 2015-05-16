package version2.prototype.ProjectInfoMetaData;

import java.util.ArrayList;

public class ProjectInfoPlugin {
    private String Name;
    private ArrayList<String> Inidicies;
    private String QC;

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
