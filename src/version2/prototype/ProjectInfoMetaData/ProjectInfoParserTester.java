package version2.prototype.ProjectInfoMetaData;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.ZonalSummary;

public class ProjectInfoParserTester {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, ParseException {
        ProjectInfoCollection projectInfoFiles = new ProjectInfoCollection();
        ArrayList<ProjectInfoFile> files = projectInfoFiles.ReadInAllProjectInfoFiles();

        for(ProjectInfoFile file : files)
        {
            if(file.GetProjectName() != null && file.GetProjectName().equalsIgnoreCase("actual project")) {
                return;
            }


            System.out.println("File: " + file.xmlLocation);
            System.out.println("\tProject Name: " + file.GetProjectName());
            if(file.error)
            {
                for(String err : file.errorMsg) {
                    System.out.println("\tERROR: " + err);
                }
            }
            else
            {
                System.out.println("\tNo errors found while parsing.");
                for(ProjectInfoPlugin plugin : file.GetPlugins())
                {
                    System.out.println("\tplugin '" + plugin.GetName() + "':");
                    System.out.println("\t\tQC: " + plugin.GetQC());
                    System.out.println("\t\tIndicies: ");
                    for(String index : plugin.GetIndicies())
                    {
                        System.out.println("\t\t\t" + index);
                    }
                }
                System.out.println();
            }

            System.out.println("\tZonal Summaries:");
            for(ZonalSummary summary : file.GetZonalSummaries())
            {
                System.out.println("\t\tShape File: " + summary.GetShapeFile());
                System.out.println("\t\tField: " + summary.GetField());
            }
        }
    }
}
