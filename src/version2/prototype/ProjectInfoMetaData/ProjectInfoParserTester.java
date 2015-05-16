package version2.prototype.ProjectInfoMetaData;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class ProjectInfoParserTester {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, ParseException {
        ProjectInfoCollection projectInfoFiles = new ProjectInfoCollection();
        ArrayList<ProjectInfoFile> files = projectInfoFiles.ReadInAllProjectInfoFiles();

        for(ProjectInfoFile file : files)
        {
            System.out.println("File: " + file.xmlLocation);
            System.out.println("\tProject Name: " + file.projectName);
            if(file.error)
            {
                for(String err : file.errorMsg)
                    System.out.println("\tERROR: " + err);
            }
            else
            {
                System.out.println("\tNo errors found while parsing.");
            }
        }
    }
}
