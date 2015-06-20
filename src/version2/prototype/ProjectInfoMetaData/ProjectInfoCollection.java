package version2.prototype.ProjectInfoMetaData;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class ProjectInfoCollection {
    private ArrayList<ProjectInfoFile> files = null;

    public ProjectInfoCollection()
    {
        files = new ArrayList<ProjectInfoFile>();
    }

    public ArrayList<ProjectInfoFile> ReadInAllProjectInfoFiles() throws IOException, ParserConfigurationException,
    SAXException, ParseException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
    IllegalArgumentException, InvocationTargetException
    {
        File fileDir = new File(System.getProperty("user.dir") + "\\src\\version2\\prototype\\ProjectInfoMetaData\\");
        File[] fl = getXMLFiles(fileDir);
        if(fl.length > 0)
        {
            for(File fi : fl)
            {
                files.add(new ProjectInfoFile(fi.getCanonicalPath()));
            }
        }
        return files;
    }

    private File[] getXMLFiles(File folder) {
        List<File> aList = new ArrayList<File>();
        File[] files = folder.listFiles();

        for (File pf : files) {

            if (pf.isFile() && getFileExtensionName(pf).indexOf("xml") != -1) {
                aList.add(pf);
            }
        }
        return aList.toArray(new File[aList.size()]);
    }

    private String getFileExtensionName(File f) {
        if (f.getName().indexOf(".") == -1) {
            return "";
        } else {
            return f.getName().substring(f.getName().length() - 3, f.getName().length());
        }
    }

    public ProjectInfoFile GetProject(String projectName) throws IOException, ParserConfigurationException, SAXException, ParseException,
    ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException,
    InvocationTargetException{
        for(ProjectInfoFile file : ReadInAllProjectInfoFiles()){
            if(file.GetProjectName() != null && file.GetProjectName().equalsIgnoreCase(projectName)) {
                return file;
            }
            else{
                System.out.println(file.GetProjectName());
                System.out.println(projectName);
            }
        }

        return null;
    }
}
