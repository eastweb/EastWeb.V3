package version2.prototype.ProjectInfoMetaData;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ProjectInfoFile {
    private DocumentBuilderFactory domFactory;
    private DocumentBuilder builder;
    private Document doc;
    public String xmlLocation;

    // Project info data
    public boolean error;
    public ArrayList<String> errorMsg;
    public final String rootElement = "ProjectInfo";
    public final ArrayList<ProjectInfoPlugin> plugins;
    public final Date startDate;
    public final String projectName;
    public final String workingDir;
    public final String maskingFile;
    public final String masterShapeFile;
    public final ArrayList<String> modisTiles;
    public final String coordinateSystem;
    public final String reSampling;
    public final String datums;
    public final String pixelSize;
    public final String stdParallel1;
    public final String stdParallel2;
    public final String centralMeridian;
    public final String falseEasting;
    public final String latitudeOfOrigin;
    public final String falseNothing;
    public final ArrayList<String> summaries;

    public ProjectInfoFile(String xmlLocation) throws ParserConfigurationException, SAXException, IOException
    {
        error = false;
        errorMsg = new ArrayList<String>();
        this.xmlLocation = xmlLocation;
        domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        builder = domFactory.newDocumentBuilder();
        doc = builder.parse(xmlLocation);
        doc.getDocumentElement().normalize();

        // Get file data
        plugins = GetPlugins();
        startDate = GetStartDate();
        projectName = GetProjectName();
        workingDir = GetWorkingDir();
        maskingFile = GetMaskingFile();
        masterShapeFile = GetMasterShapeFile();
        modisTiles = GetModisTiles();
        coordinateSystem = GetCoordinateSystem();
        reSampling = GetReSampling();
        datums = GetDatums();
        pixelSize = GetPixelSize();
        stdParallel1 = GetStandardParallel1();
        stdParallel2 = GetStandardParallel2();
        centralMeridian = GetCentralMeridian();
        falseEasting = GetFalseEasting();
        latitudeOfOrigin = GetLatitudeOfOrigin();
        falseNothing = GetFalseNothing();
        summaries = GetSummaries();
    }

    private ArrayList<ProjectInfoPlugin> GetPlugins()
    {
        ArrayList<ProjectInfoPlugin> plugins = new ArrayList<ProjectInfoPlugin>();
        String name;
        String qc;
        ArrayList<String> inidicies = null;

        NodeList pluginList = GetUpperLevelNodeList("Plugin", "Missing plugins.", "Plugins");
        if(pluginList != null)
        {
            for(int i=0; i < pluginList.getLength(); i++)
            {
                Element plugin = ((Element)pluginList.item(i));

                name = plugin.getAttribute("name");

                ArrayList<String> values = GetNodeListValues(plugin.getElementsByTagName("QC"),
                        "Missing QC for plugin '" + name + "'");
                if(values.size() > 0) {
                    qc = values.get(0);
                } else {
                    qc = null;
                }

                values = GetNodeListValues(plugin.getElementsByTagName("Indicies"), "Missing indicies for plugin '"
                        + name + "'");
                if(values.size() > 0) {
                    inidicies = values;
                } else {
                    inidicies = null;
                }

                plugins.add(new ProjectInfoPlugin(name, inidicies, qc));
            }
        }

        return plugins;
    }

    private Date GetStartDate()
    {
        NodeList nodes = GetUpperLevelNodeList("StartDate", "Missing start date.");
        try {
            // e.g. "Wed May 20 21:21:36 CDT 2015"
            ArrayList<String> values = GetNodeListValues(nodes, "Missing start date.");
            if(values.size() > 0) {
                return new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(values.get(0));
            }
            return null;
        } catch (ParseException e) {
            error = true;
            errorMsg.add(e.getMessage());
            return null;
        }
    }

    private String GetProjectName()
    {
        NodeList nodes = GetUpperLevelNodeList("ProjectName", "Missing project name.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing project name.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetWorkingDir()
    {
        NodeList nodes = GetUpperLevelNodeList("WorkingDir", "Missing working directory.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing working directory.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetMaskingFile()
    {
        NodeList nodes = GetUpperLevelNodeList("MaskingFile", "Missing masking file.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing masking file.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetMasterShapeFile()
    {
        NodeList nodes = GetUpperLevelNodeList("MasterShapeFile", "Missing master shape file.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing master shape file.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private ArrayList<String> GetModisTiles()
    {
        NodeList nodes = GetUpperLevelNodeList("Modis", "Missing modis tiles.", "ModisTiles");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing modis tiles.");
        if(values.size() > 0) {
            return values;
        }
        return null;
    }

    private String GetCoordinateSystem()
    {
        NodeList nodes = GetUpperLevelNodeList("CoordinateSystem", "Missing coordinate system.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing coordinate system.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetReSampling()
    {
        NodeList nodes = GetUpperLevelNodeList("ReSampling", "Missing resampling.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing resampling.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetDatums()
    {
        NodeList nodes = GetUpperLevelNodeList("Datum", "Missing datums.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing datums.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetPixelSize()
    {
        NodeList nodes = GetUpperLevelNodeList("PixelSize", "Missing PixelSize.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing PixelSize.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetStandardParallel1()
    {
        NodeList nodes = GetUpperLevelNodeList("StandardParallel1", "Missing standard parallel 1.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing standard parallel 1.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetStandardParallel2()
    {
        NodeList nodes = GetUpperLevelNodeList("StandardParallel2", "Missing standard parallel 2.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing standard parallel 2.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetCentralMeridian()
    {
        NodeList nodes = GetUpperLevelNodeList("CentralMeridian", "Missing central meridian.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing central meridian.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetFalseEasting()
    {
        NodeList nodes = GetUpperLevelNodeList("FalseEasting", "Missing false easting.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing false easting.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetLatitudeOfOrigin()
    {
        NodeList nodes = GetUpperLevelNodeList("LatitudeOfOrigin", "Missing latitude of origin.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing latitude of origin.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String GetFalseNothing()
    {
        NodeList nodes = GetUpperLevelNodeList("FalseNothing", "Missing false nothing.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing false nothing.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private ArrayList<String> GetSummaries()
    {
        NodeList nodes = GetUpperLevelNodeList("Summary", "Missing summaries.", "Summaries");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing summaries.");
        if(values.size() > 0) {
            return values;
        }
        return null;
    }

    private NodeList GetUpperLevelNodeList(String element, String errorMsg, String...parents)
    {
        NodeList nodes = null;

        for(String parent : parents)
        {
            if(nodes == null) {
                nodes = doc.getElementsByTagName(parent);
            } else {
                nodes = ((Element)nodes.item(0)).getElementsByTagName(parent);
            }
        }

        // Failed to find specified parent
        if(((nodes == null) && (parents.length > 0)) || ((nodes != null) && (nodes.getLength() <= 0)))
        {
            error = true;
            this.errorMsg.add(errorMsg);
            return null;
        }
        else if(nodes == null) {
            nodes = doc.getElementsByTagName(element);
        } else {
            nodes = ((Element)nodes.item(0)).getElementsByTagName(element);
        }

        return nodes;
    }

    private ArrayList<String> GetNodeListValues(NodeList nList, String errorMsg)
    {
        ArrayList<String> values = new ArrayList<String>();
        if(nList != null)
        {
            for(int i=0; i < nList.getLength(); i++)
            {
                Element e = (Element)nList.item(i);
                NodeList list = e.getChildNodes();
                Node n = list.item(0);
                if(n != null)
                {
                    n.normalize();
                    values.add(n.getNodeValue().trim());
                }
            }
        }
        if((nList != null) && (values.size() == 0))
        {
            error = true;
            this.errorMsg.add(errorMsg);
        }
        return values;
    }
}
