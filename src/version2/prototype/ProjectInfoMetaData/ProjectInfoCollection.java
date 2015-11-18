package version2.prototype.ProjectInfoMetaData;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.util.FileSystem;

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
        File fileDir = new File(System.getProperty("user.dir") + "\\projects\\");
        File[] fl = getXMLFiles(fileDir);
        Config configInstance = Config.getInstance();
        if(fl.length > 0)
        {
            ProjectInfoFile metaData;
            for(File fi : fl)
            {
                try{
                    metaData = new ProjectInfoFile(configInstance, fi.getCanonicalPath());
                    if(metaData.error) {
                        ErrorLog.add(Config.getInstance(), "Parsing failure" + (metaData.GetErrorMessages().size() > 1 ? "s" : "") + " for file project meta data file '" + fi.getName() + "'. "
                                + metaData.GetErrorMessages().toString(),
                                new Exception("Parsing failure" + (metaData.GetErrorMessages().size() > 1 ? "s" : "") + " for file project meta data file '" + fi.getName() + "'."));
                    } else {
                        files.add(metaData);
                    }
                } catch(Exception e) {
                    ErrorLog.add(Config.getInstance(), "Project meta data file, " + fi.getName() + " has an error in it.", e);
                }
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
        String cleanedProjectName = FileSystem.StandardizeName(projectName);
        ArrayList<ProjectInfoFile> allProjects = ReadInAllProjectInfoFiles();
        ProjectInfoFile file;
        for(int i=0; i < allProjects.size(); i++){
            file = allProjects.get(i);
            if(file.GetProjectName() != null && file.GetProjectName().equalsIgnoreCase(cleanedProjectName)) {
                return file;
            }
        }

        return null;
    }
}
