package version2.prototype.ProjectInfoMetaData;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import version2.prototype.ZonalSummary;

public class ProjectInfoFile {
    private DocumentBuilderFactory domFactory;
    private DocumentBuilder builder;
    private Document doc;
    public String xmlLocation;

    // Project info data
    public boolean error;
    public ArrayList<String> errorMsg;
    //private final String rootElement = "ProjectInfo";
    private final ArrayList<ProjectInfoPlugin> plugins;
    private final Date startDate;
    private final String projectName;
    private final String workingDir;
    private final String maskingFile;
    private final String masterShapeFile;
    private final String timeZone;
    private final ArrayList<String> modisTiles;
    private final String coordinateSystem;
    private final String reSampling;
    private final String datums;
    private final String pixelSize;
    private final String stdParallel1;
    private final String stdParallel2;
    private final String centralMeridian;
    private final String falseEasting;
    private final String latitudeOfOrigin;
    private final String falseNothing;
    private final ArrayList<ZonalSummary> zonalSummaries;

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
        plugins = ReadPlugins();
        startDate = ReadStartDate();
        projectName = ReadProjectName();
        workingDir = ReadWorkingDir();
        maskingFile = ReadMaskingFile();
        masterShapeFile = ReadMasterShapeFile();
        timeZone = ReadTimeZone();
        modisTiles = ReadModisTiles();
        coordinateSystem = ReadCoordinateSystem();
        reSampling = ReadReSampling();
        datums = ReadDatums();
        pixelSize = ReadPixelSize();
        stdParallel1 = ReadStandardParallel1();
        stdParallel2 = ReadStandardParallel2();
        centralMeridian = ReadCentralMeridian();
        falseEasting = ReadFalseEasting();
        latitudeOfOrigin = ReadLatitudeOfOrigin();
        falseNothing = ReadFalseNothing();
        zonalSummaries = ReadSummaries();
    }

    public ArrayList<ProjectInfoPlugin> GetPlugins() { return plugins; }
    public Date GetStartDate() { return startDate; }
    public String GetProjectName() { return projectName; }
    public String GetWorkingDir() { return workingDir; }
    public String GetMaskingFile() { return maskingFile; }
    public String GetMasterShapeFile() { return masterShapeFile; }
    public String GetTimeZone() { return timeZone; }
    public ArrayList<String> GetModisTiles() { return modisTiles; }
    public String GetCoordinateSystem() { return coordinateSystem; }
    public String GetReSampling() { return reSampling; }
    public String GetDatums() { return datums; }
    public String GetPixelSize() { return pixelSize; }
    public String GetStandardParallel1() { return stdParallel1; }
    public String GetStandardParallel2() { return stdParallel2; }
    public String GetCentralMeridian() { return centralMeridian; }
    public String GetFalseEasting() { return falseEasting; }
    public String GetLatitudeOfOrigin() { return latitudeOfOrigin; }
    public String GetFalseNothing() { return falseNothing; }
    public ArrayList<ZonalSummary> GetZonalSummaries() { return zonalSummaries; }

    private ArrayList<ProjectInfoPlugin> ReadPlugins()
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
                        "Missing QC for plugin '" + name + "'.");
                if(values.size() > 0) {
                    qc = values.get(0);
                } else {
                    qc = null;
                }

                values = GetNodeListValues(plugin.getElementsByTagName("Indicies"), "Missing indicies for plugin '"
                        + name + "'.");
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

    private Date ReadStartDate()
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

    private String ReadProjectName()
    {
        NodeList nodes = GetUpperLevelNodeList("ProjectName", "Missing project name.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing project name.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadWorkingDir()
    {
        NodeList nodes = GetUpperLevelNodeList("WorkingDir", "Missing working directory.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing working directory.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadMaskingFile()
    {
        NodeList nodes = GetUpperLevelNodeList("MaskingFile", "Missing masking file.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing masking file.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadMasterShapeFile()
    {
        NodeList nodes = GetUpperLevelNodeList("MasterShapeFile", "Missing master shape file.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing master shape file.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadTimeZone()
    {
        NodeList nodes = GetUpperLevelNodeList("TimeZone", "Missing time zone.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing time zone.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private ArrayList<String> ReadModisTiles()
    {
        NodeList nodes = GetUpperLevelNodeList("Modis", "Missing modis tiles.", "ModisTiles");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing modis tiles.");
        if(values.size() > 0) {
            return values;
        }
        return null;
    }

    private String ReadCoordinateSystem()
    {
        NodeList nodes = GetUpperLevelNodeList("CoordinateSystem", "Missing coordinate system.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing coordinate system.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadReSampling()
    {
        NodeList nodes = GetUpperLevelNodeList("ReSampling", "Missing resampling.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing resampling.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadDatums()
    {
        NodeList nodes = GetUpperLevelNodeList("Datum", "Missing datums.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing datums.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadPixelSize()
    {
        NodeList nodes = GetUpperLevelNodeList("PixelSize", "Missing PixelSize.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing PixelSize.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadStandardParallel1()
    {
        NodeList nodes = GetUpperLevelNodeList("StandardParallel1", "Missing standard parallel 1.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing standard parallel 1.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadStandardParallel2()
    {
        NodeList nodes = GetUpperLevelNodeList("StandardParallel2", "Missing standard parallel 2.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing standard parallel 2.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadCentralMeridian()
    {
        NodeList nodes = GetUpperLevelNodeList("CentralMeridian", "Missing central meridian.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing central meridian.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadFalseEasting()
    {
        NodeList nodes = GetUpperLevelNodeList("FalseEasting", "Missing false easting.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing false easting.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadLatitudeOfOrigin()
    {
        NodeList nodes = GetUpperLevelNodeList("LatitudeOfOrigin", "Missing latitude of origin.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing latitude of origin.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private String ReadFalseNothing()
    {
        NodeList nodes = GetUpperLevelNodeList("FalseNothing", "Missing false nothing.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing false nothing.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private ArrayList<ZonalSummary> ReadSummaries()
    {
        ArrayList<ZonalSummary> summaries = new ArrayList<ZonalSummary>();
        String shapeFile;
        String field;

        NodeList summaryList = GetUpperLevelNodeList("Summary", "Missing zonal summaries.", "Summaries");
        if(summaryList != null)
        {
            ArrayList<String> values;
            Element summary;

            for(int i=0; i < summaryList.getLength(); i++)
            {
                summary = (Element)summaryList.item(i);
                values = GetNodeListValues(summary.getElementsByTagName("ShapeFile"), "Missing summary shape file.");
                if(values.size() > 0) {
                    shapeFile = values.get(0);
                } else {
                    shapeFile = null;
                }

                values = GetNodeListValues(summary.getElementsByTagName("Field"), "Missing summary field.");
                if(values.size() > 0) {
                    field = values.get(0);
                } else {
                    field = null;
                }

                summaries.add(new ZonalSummary(shapeFile, field));
            }
        }
        return summaries;
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
