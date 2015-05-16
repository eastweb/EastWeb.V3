package version2.prototype.ProjectInfoMetaData;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class ProjectInfoCollection {
    private ArrayList<ProjectInfoFile> files = null;

    public ProjectInfoCollection()
    {
        files = new ArrayList<ProjectInfoFile>();
    }

    public ArrayList<ProjectInfoFile> ReadInAllProjectInfoFiles() throws IOException, ParserConfigurationException,
        SAXException, ParseException
    {
        File f = new File("./");
        String infoPath = f.getCanonicalPath() + File.separator + "src" + File.separator +
                "version2" + File.separator + "prototype" + File.separator + "ProjectInfoMetaData";
        File[] fl = new File(infoPath).listFiles();
        if(fl.length > 0)
        {
            for(File fi : fl)
            {
                if(fi.isFile() && !fi.isHidden() && fi.getName().endsWith(".xml"))
                {
                    files.add(new ProjectInfoFile(fi.getCanonicalPath()));
                }
            }
        }
        return files;
    }
}
