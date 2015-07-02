package version2.prototype.ProjectInfoMetaData;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import version2.prototype.Projection;
import version2.prototype.Projection.Datum;
import version2.prototype.Projection.ProjectionType;
import version2.prototype.Projection.ResamplingType;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.ZonalSummary;

/**
 * Represents data parsed from a project info xml file. Does not update itself with changes in the xml or update the xml with its changes.
 *
 * @author michael.devos
 *
 */
public class ProjectInfoFile {
    private DocumentBuilderFactory domFactory;
    private DocumentBuilder builder;
    private Document doc;
    public String xmlLocation;

    public boolean error;
    public ArrayList<String> errorMsg;
    //    private final String rootElement = "ProjectInfo";
    private final ArrayList<ProjectInfoPlugin> plugins;
    private final Date startDate;
    private final String projectName;
    private final String workingDir;
    private final String maskingFile;
    private final int maskingResolution;
    private final String masterShapeFile;
    private final String timeZone;
    private final boolean clipping;
    private final int totModisTiles;
    private final ArrayList<String> modisTiles;
    private final Projection projection;
    private final Date freezingDate;
    private final Date heatingDate;
    private final ArrayList<ProjectInfoSummary> summaries;

    /**
     * Creates a ProjectInfoFile object from parsing the given xml file. Doesn't allow its data to be changed and doesn't dynamically update its
     * data if the file changes.
     *
     * @param xmlLocation  - file path of xml to parse
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public ProjectInfoFile(String xmlLocation) throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException,
    NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
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
        maskingResolution = ReadMaskingResolution();
        masterShapeFile = ReadMasterShapeFile();
        timeZone = ReadTimeZone();
        clipping = ReadClipping();
        totModisTiles = ReadTotalModisTiles();
        modisTiles = ReadModisTiles();
        projection = new Projection(ReadProjectionType(), ReadResamplingType(), ReadDatum(), ReadPixelSize(), ReadStandardParallel1(),
                ReadStandardParallel2(), ReadScalingFactor(), ReadCentralMeridian(), ReadFalseEasting(), ReadFalseNorthing(),
                ReadLatitudeOfOrigin());
        freezingDate = ReadFreezing();
        heatingDate = ReadHeating();
        summaries = ReadSummaries();
    }

    /**
     * Gets an ArrayList of ProjectInfoPlugins gotten from the once parsed xml file.
     *
     * @return list of ProjectInfoPlugin objects that could be created from the xml's data.
     */
    public ArrayList<ProjectInfoPlugin> GetPlugins() { return plugins; }

    /**
     * Gets the start date gotten from the once parsed xml file.
     *
     * @return Date object representing the start date that could be created from the xml's data.
     */
    public Date GetStartDate() { return startDate; }

    /**
     * Gets the project name gotten from the once parsed xml file.
     *
     * @return project name gotten from the xml's data.
     */
    public String GetProjectName() { return projectName; }

    /**
     * Gets the working directory gotten from the once parsed xml file.
     *
     * @return working directory string gotten from the xml's data.
     */
    public String GetWorkingDir() { return workingDir; }

    /**
     * Gets the path to masking file gotten from the once parsed xml file.
     *
     * @return masking file path string gotten from the xml's data.
     */
    public String GetMaskingFile() { return maskingFile; }

    /**
     * Gets the path to masking resolution gotten from the once parsed xml file.
     *
     * @return masking resolution size gotten from the xml's data.
     */
    public Integer GetMaskingResolution() { return maskingResolution; }

    /**
     * Gets the master shape file gotten from the once parsed xml file. This shape file is not meant to be used in zonal summary calculations.
     *
     * @return master shape file path string gotten from the xml's data.
     */
    public String GetMasterShapeFile() { return masterShapeFile; }

    /**
     * Gets the time zone gotten from the once parsed xml file.
     *
     * @return time zone string gotten from the xml's data.
     */
    public String GetTimeZone() { return timeZone; }

    /**
     * Gets the clipping gotten from the once parsed xml file.
     *
     * @return get clipping value gotten from the xml's data.
     */
    public boolean GetClipping() { return clipping; }

    /**
     * Gets the total number of modies tiles expected per input data downloaded.
     *
     * @return number of modies tiles associated with a single downloaded data unit.
     */
    public int GetTotModisTiles() { return totModisTiles; }

    /**
     * Gets the modis tiles gotten from the once parsed xml file.
     *
     * @return modis tile names gotten from the xml's data.
     */
    public ArrayList<String> GetModisTiles() { return modisTiles; }

    /**
     * Gets the list of summaries gotten from the once parsed xml file.
     *
     * @return list of ProjectInfoSummary objects created from the xml's data.
     */
    public ArrayList<ProjectInfoSummary> GetSummaries() { return summaries; }

    /**
     * Gets the projection information gotten from the once parsed xml file.
     *
     * @return Projection object created from the xml's data.
     */
    public Projection GetProjection() { return projection; }

    /**
     * Gets the freezing date gotten from the once parsed xml file.
     *
     * @return Date - the freezing date read from the xml's data.
     */
    public Date GetFreezingDate() { return freezingDate; }

    /**
     * Gets the heating date gotten from the once parsed xml file.
     *
     * @return Date - the heating date read from the xml's data.
     */
    public Date GetHeatingDate() { return heatingDate; }

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
        NodeList nodes = GetUpperLevelNodeList("File", "Missing masking file.", "Masking");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing masking file.");
        if(values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private Integer ReadMaskingResolution()
    {
        NodeList nodes = GetUpperLevelNodeList("Resolution", "Missing masking file.", "Masking");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing masking resolution.");
        if(values.size() > 0) {
            return Integer.parseInt(values.get(0));
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

    private boolean ReadClipping()
    {
        NodeList nodes = GetUpperLevelNodeList("Clipping", "Missing clipping.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing clipping.");
        if(values.size() > 0) {
            return Boolean.valueOf(values.get(0));
        }
        return false;   // Default to false when element is missing.
    }

    private int ReadTotalModisTiles()
    {
        NodeList nodes = GetUpperLevelNodeList("TotalModisTiles", "Missing total number of modis tiles per input data.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing total number of modis tiles per input data.");
        if(values.size() > 0) {
            return Integer.parseInt(values.get(0));
        }
        return 0;
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

    private ProjectionType ReadProjectionType()
    {
        NodeList nodes = GetUpperLevelNodeList("CoordinateSystem", "Missing coordinate system.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing coordinate system.");
        if(values.size() > 0) {
            ProjectionType pType = null;
            switch(values.get(0))
            {
            case "ALBERS_EQUAL_AREA":
                pType = ProjectionType.ALBERS_EQUAL_AREA;
                break;
            case "LAMBERT_CONFORMAL_CONIC":
                pType = ProjectionType.LAMBERT_CONFORMAL_CONIC;
                break;
            case "TRANSVERSE_MERCATOR":
                pType = ProjectionType.TRANSVERSE_MERCATOR;
                break;
            }
            return pType;
        }
        return null;
    }

    private ResamplingType ReadResamplingType()
    {
        NodeList nodes = GetUpperLevelNodeList("ReSampling", "Missing resampling.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing resampling.");
        if(values.size() > 0) {
            ResamplingType rType = null;
            switch(values.get(0))
            {
            case "NEAREST_NEIGHBOR":
                rType = ResamplingType.NEAREST_NEIGHBOR;
                break;
            case "BILINEAR":
                rType = ResamplingType.BILINEAR;
                break;
            case "CUBIC_CONVOLUTION":
                rType = ResamplingType.CUBIC_CONVOLUTION;
                break;
            }
            return rType;
        }
        return null;
    }

    private Datum ReadDatum()
    {
        NodeList nodes = GetUpperLevelNodeList("Datum", "Missing datums.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing datums.");
        if(values.size() > 0) {
            Datum d = null;
            switch(values.get(0))
            {
            case "NAD27":
                d = Datum.NAD27;
                break;
            case "NAD83":
                d = Datum.NAD83;
                break;
            case "WGS66":
                d = Datum.WGS66;
                break;
            case "WGS72":
                d = Datum.WGS72;
                break;
            case "WGS84":
                d = Datum.WGS84;
                break;
            }
            return d;
        }
        return null;
    }

    private int ReadPixelSize()
    {
        NodeList nodes = GetUpperLevelNodeList("PixelSize", "Missing PixelSize.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing PixelSize.");
        if(values.size() > 0) {
            return Integer.parseInt(values.get(0));
        }
        return 0;
    }

    private double ReadStandardParallel1()
    {
        NodeList nodes = GetUpperLevelNodeList("StandardParallel1", "Missing standard parallel 1.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing standard parallel 1.");
        if(values.size() > 0) {
            return Double.parseDouble(values.get(0));
        }
        return 0;
    }

    private double ReadStandardParallel2()
    {
        NodeList nodes = GetUpperLevelNodeList("StandardParallel2", "Missing standard parallel 2.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing standard parallel 2.");
        if(values.size() > 0) {
            return Double.parseDouble(values.get(0));
        }
        return 0;
    }

    private double ReadScalingFactor() {
        NodeList nodes = GetUpperLevelNodeList("ScalingFactor", "Missing scaling factor.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing scaling factor.");
        if(values.size() > 0) {
            return Double.parseDouble(values.get(0));
        }
        return 0;
    }

    private double ReadCentralMeridian()
    {
        NodeList nodes = GetUpperLevelNodeList("CentralMeridian", "Missing central meridian.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing central meridian.");
        if(values.size() > 0) {
            return Double.parseDouble(values.get(0));
        }
        return 0;
    }

    private double ReadFalseEasting()
    {
        NodeList nodes = GetUpperLevelNodeList("FalseEasting", "Missing false easting.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing false easting.");
        if(values.size() > 0) {
            return Double.parseDouble(values.get(0));
        }
        return 0;
    }

    private double ReadFalseNorthing()
    {
        NodeList nodes = GetUpperLevelNodeList("FalseNorthing", "Missing false nothing.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing false nothing.");
        if(values.size() > 0) {
            return Double.parseDouble(values.get(0));
        }
        return 0;
    }

    private double ReadLatitudeOfOrigin()
    {
        NodeList nodes = GetUpperLevelNodeList("LatitudeOfOrigin", "Missing latitude of origin.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing latitude of origin.");
        if(values.size() > 0) {
            return Double.parseDouble(values.get(0));
        }
        return 0;
    }

    private Date ReadFreezing()
    {
        NodeList nodes = GetUpperLevelNodeList("Freezing", "Missing heating date.");
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

    private Date ReadHeating()
    {
        NodeList nodes = GetUpperLevelNodeList("Heating", "Missing freezing date.");
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

    private ArrayList<ProjectInfoSummary> ReadSummaries() throws ClassNotFoundException, NoSuchMethodException, SecurityException,
    InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        ArrayList<ProjectInfoSummary> summaries = new ArrayList<ProjectInfoSummary>();

        NodeList summaryList = GetUpperLevelNodeList("Summary", "Missing zonal summaries.", "Summaries");
        ArrayList<String> summaryStrings = GetNodeListValues(summaryList, "Missing zonal summaries.");
        if(summaryStrings.size() > 0) {
            String shapefile;
            String field;
            String temporalSummaryCompositionStrategyClassName;
            TemporalSummaryRasterFileStore fileStore;
            Class<?> strategyClass;
            Constructor<?> ctorStrategy;

            for(String summary : summaryStrings)
            {
                // Shape File Path: C:\Users\sufi\Desktop\shapefile\shapefile.shp; Field: COUNTYNS10; Temporal Summary: GregorianWeeklyStrategy
                // Shape File Path: C:\Users\sufi\Desktop\shapefile\shapefile.shp; COUNTYNS10
                shapefile = summary.substring(summary.indexOf("Shape File Path: ") + "Shape File Path: ".length(), summary.indexOf(";"));
                if(summary.indexOf("Temporal Summary") == -1)
                {
                    field = summary.substring(summary.indexOf("Field: ") + "Field: ".length());
                    if(field.endsWith(";")) {
                        field = field.substring(0, field.length() - 1);
                    }
                    temporalSummaryCompositionStrategyClassName = null;
                    fileStore = null;
                }
                else
                {
                    field = summary.substring(summary.indexOf("Field: ") + "Field: ".length(), summary.indexOf(";", summary.indexOf(";") + 1));
                    temporalSummaryCompositionStrategyClassName = summary.substring(summary.indexOf("Temporal Summary: ") + "Temporal Summary: ".length());
                    if(temporalSummaryCompositionStrategyClassName.endsWith(";")) {
                        temporalSummaryCompositionStrategyClassName = temporalSummaryCompositionStrategyClassName.substring(0, temporalSummaryCompositionStrategyClassName.length() - 1);
                    }
                    strategyClass = Class.forName("version2.prototype.summary.temporal.CompositionStrategies." + temporalSummaryCompositionStrategyClassName);
                    ctorStrategy = strategyClass.getConstructor();
                    fileStore = new TemporalSummaryRasterFileStore((TemporalSummaryCompositionStrategy)ctorStrategy.newInstance());
                }
                summaries.add(new ProjectInfoSummary(new ZonalSummary(shapefile, field), fileStore));
            }
            return summaries;
        }
        return null;

    }

    /**
     * Used to get the NodeList object pertaining to an element containing the list of elements desired. Used even if desired element does not
     * contain a list of elements but only the value desired. Send the returned NodeList object into {@link #GetNodeListValues(NodeList, String)
     * GetNodeListValues(NodeList, String)} to get its value(s).
     *
     * @param element
     * @param errorMsg
     * @param parents
     * @return
     */
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

    /**
     * Used to get the values of the NodeList that's passed in. Used for both single value and multiple value cases where there is no lower
     * hierarchy of elements (i.e. the passed in NodeList must be the lowest elements in the list).
     *
     * @param nList
     * @param errorMsg
     * @return
     */
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
