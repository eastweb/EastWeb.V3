package version2.prototype.ProjectInfoMetaData;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import version2.prototype.Projection;
import version2.prototype.Projection.Datum;
import version2.prototype.Projection.ProjectionType;
import version2.prototype.Projection.ResamplingType;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;
import version2.prototype.util.FileSystem;
import version2.prototype.ZonalSummary;

/**
 * Represents data parsed from a project info xml file. Does not update itself with changes in the xml or update the xml with its changes.
 *
 * @author michael.devos
 *
 */
public class ProjectInfoFile {
    private static DateTimeFormatter datesFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz uuuu");
    private DocumentBuilderFactory domFactory;
    private DocumentBuilder builder;
    private Document doc;

    /**
     * XML filepath of the file that was parsed to create this ProjectInfoFile object. Null if this object is custom made instead of from a parsed xml file.
     */
    public final String xmlLocation;
    /**
     * True if there has been an error during parsing.
     */
    public boolean error;
    /**
     * List of error messages if any. Empty if no errors.
     */
    public ArrayList<String> errorMsg;
    //    private final String rootElement = "ProjectInfo";
    private final ArrayList<ProjectInfoPlugin> plugins;
    private final LocalDate startDate;
    private final String projectName;
    private final String workingDir;
    private final String maskingFile;
    private final Integer maskingResolution;
    private final String masterShapeFile;
    private final String timeZone;
    private final Boolean clipping;
    private final Integer totModisTiles;
    //    private final ArrayList<String> modisTiles;
    private final Projection projection;
    private final LocalDate heatingDate;
    private final Double heatingDegree;
    private final LocalDate freezingDate;
    private final Double coolingDegree;
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
        //        modisTiles = ReadModisTiles();
        projection = new Projection(ReadProjectionType(), ReadResamplingType(), ReadDatum(), ReadPixelSize(), ReadStandardParallel1(),
                ReadStandardParallel2(), ReadScalingFactor(), ReadCentralMeridian(), ReadFalseEasting(), ReadFalseNorthing(),
                ReadLatitudeOfOrigin());
        freezingDate = ReadFreezingDate();
        coolingDegree = ReadCoolingDegree();
        heatingDate = ReadHeatingDate();
        heatingDegree = ReadHeatingDegree();
        summaries = ReadSummaries();
    }

    /**
     * Creates a fully custom ProjectInfoFile object from the given parameters. Meant for testing purposes.
     *
     * @param plugins
     * @param startDate
     * @param projectName
     * @param workingDir
     * @param maskingFile
     * @param maskingResolution
     * @param masterShapeFile
     * @param timeZone
     * @param clipping
     * @param totModisTiles
     * @param modisTiles
     * @param projection
     * @param freezingDate
     * @param coolingDegree
     * @param heatingDate
     * @param heatingDegree
     * @param summaries
     */
    public ProjectInfoFile(ArrayList<ProjectInfoPlugin> plugins, LocalDate startDate, String projectName, String workingDir, String maskingFile, Integer maskingResolution, String masterShapeFile,
            String timeZone, Boolean clipping, Integer totModisTiles, ArrayList<String> modisTiles, Projection projection, LocalDate freezingDate, Double coolingDegree, LocalDate heatingDate,
            Double heatingDegree, ArrayList<ProjectInfoSummary> summaries)
    {
        xmlLocation = null;
        this.plugins = plugins;
        this.startDate = startDate;
        this.projectName = projectName;
        this.workingDir = workingDir;
        this.maskingFile = maskingFile;
        this.maskingResolution = maskingResolution;
        this.masterShapeFile = masterShapeFile;
        this.timeZone = timeZone;
        this.clipping = clipping;
        this.totModisTiles = totModisTiles;
        //        this.modisTiles = modisTiles;
        this.projection = projection;
        this.freezingDate = freezingDate;
        this.coolingDegree = coolingDegree;
        this.heatingDate = heatingDate;
        this.heatingDegree = heatingDegree;
        this.summaries = summaries;
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
    public LocalDate GetStartDate() { return startDate; }

    /**
     * Gets the project name gotten from the once parsed xml file.
     *
     * @return project name gotten from the xml's data.
     */
    public String GetProjectName() { return FileSystem.StandardizeName(projectName); }

    /**
     * Gets the working directory gotten from the once parsed xml file.
     *
     * @return working directory string gotten from the xml's data.
     */
    public String GetWorkingDir() { return FileSystem.CheckDirPath(workingDir); }

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
    //    public ArrayList<String> GetModisTiles() { return modisTiles; }

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
    public LocalDate GetFreezingDate() { return freezingDate; }

    /**
     * Gets the cooling degree gotten from the once parsed xml file.
     *
     * @return Double - the cooling degree read from the xml's data.
     */
    public Double GetCoolingDegree() { return coolingDegree; }

    /**
     * Gets the heating date gotten from the once parsed xml file.
     *
     * @return Date - the heating date read from the xml's data.
     */
    public LocalDate GetHeatingDate() { return heatingDate; }

    /**
     * Gets the heating degree gotten from the once parsed xml file.
     *
     * @return Double - the heating degree read from the xml's data.
     */
    public Double GetHeatingDegree() { return heatingDegree; }

    private ArrayList<ProjectInfoPlugin> ReadPlugins()
    {
        ArrayList<ProjectInfoPlugin> plugins = new ArrayList<ProjectInfoPlugin>();
        String name;
        String qc;
        ArrayList<String> inidicies = null;
        ArrayList<String> modisTiles = null;

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

                values = GetNodeListValues(plugin.getElementsByTagName("Indicies"), "Missing indicies for plugin '" + name + "'.");
                if(values.size() > 0) {
                    inidicies = values;
                } else {
                    inidicies = null;
                }

                values = GetNodeListValues(plugin.getElementsByTagName("ModisTile"), "Missing modis tile(s) for plugin '" + name + "'.");
                if(values.size() > 0) {
                    modisTiles = values;
                } else {
                    modisTiles = null;
                }

                plugins.add(new ProjectInfoPlugin(name, inidicies, qc, modisTiles));
            }
        }

        return plugins;
    }

    private LocalDate ReadStartDate() throws DateTimeParseException
    {
        NodeList nodes = GetUpperLevelNodeList("StartDate", "Missing start date.");
        //        try {
        // e.g. "Wed May 20 21:21:36 CDT 2015"
        ArrayList<String> values = GetNodeListValues(nodes, "Missing start date.");
        if(values.size() > 0) {
            return LocalDate.parse(values.get(0), datesFormatter);
            //                return new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(values.get(0));
        }
        return null;
        //        } catch (DateTimeParseException e) {
        //            error = true;
        //            errorMsg.add(e.getMessage());
        //            return null;
        //        }
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
            String temp = values.get(0);
            if(temp.startsWith("\\") || temp.startsWith("/"))
            {
                temp = System.getProperty("user.dir") + temp;
            }
            return temp;
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
            String temp = values.get(0);
            if(temp.startsWith("\\") || temp.startsWith("/"))
            {
                temp = System.getProperty("user.dir") + temp;
            }
            return temp;
        }
        return null;
    }

    private String ReadTimeZone()
    {
        NodeList nodes = GetUpperLevelNodeList("TimeZone", "Missing time zone.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing time zone.");
        if(values.size() > 0) {
            if(values.get(0).indexOf(")") > -1) {
                return values.get(0).substring(values.get(0).indexOf(") ") + 2);
            } else {
                return values.get(0);
            }
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

    private LocalDate ReadFreezingDate() throws DateTimeParseException
    {
        NodeList nodes = GetUpperLevelNodeList("FreezingDate", "Missing freezing date.");
        // e.g. "Wed May 20 21:21:36 CDT 2015"
        ArrayList<String> values = GetNodeListValues(nodes, "Missing freezing date.");
        if(values.size() > 0) {
            return LocalDate.parse(values.get(0), datesFormatter);
        }
        return null;

        //        Integer dayOfMonth;
        //        Month month;
        //        NodeList dayNodes = GetUpperLevelNodeList("Day", "Missing heating date.", "HeatingDate");
        //        NodeList monthNodes = GetUpperLevelNodeList("Month", "Missing heating date.", "HeatingDate");
        //
        //        ArrayList<String> values = GetNodeListValues(dayNodes, "Missing HeatingDate day.");
        //        if(values.size() > 0) {
        //            dayOfMonth = Integer.parseInt(values.get(0));
        //
        //            values = GetNodeListValues(monthNodes, "Missing HeatingDate month.");
        //            if(values.size() > 0) {
        //                month = Month.valueOf(values.get(0).toUpperCase());
        //
        //                return MonthDay.of(month, dayOfMonth);
        //            }
        //        }
        //        return null;
    }

    private Double ReadCoolingDegree()
    {
        NodeList nodes = GetUpperLevelNodeList("CoolingDegree", "Missing cooling degree.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing cooling degree.");
        if(values.size() > 0) {
            return Double.parseDouble(values.get(0));
        }
        return 0.0;
    }

    private LocalDate ReadHeatingDate() throws DateTimeParseException
    {
        NodeList nodes = GetUpperLevelNodeList("HeatingDate", "Missing heating date.");
        // e.g. "Wed May 20 21:21:36 CDT 2015"
        ArrayList<String> values = GetNodeListValues(nodes, "Missing start date.");
        if(values.size() > 0) {
            return LocalDate.parse(values.get(0), datesFormatter);
        }
        return null;

        //        Integer dayOfMonth;
        //        Month month;
        //        NodeList dayNodes = GetUpperLevelNodeList("Day", "Missing heating date.", "HeatingDate");
        //        NodeList monthNodes = GetUpperLevelNodeList("Month", "Missing heating date.", "HeatingDate");
        //
        //        ArrayList<String> values = GetNodeListValues(dayNodes, "Missing HeatingDate day.");
        //        if(values.size() > 0) {
        //            dayOfMonth = Integer.parseInt(values.get(0));
        //
        //            values = GetNodeListValues(monthNodes, "Missing HeatingDate month.");
        //            if(values.size() > 0) {
        //                month = Month.valueOf(values.get(0).toUpperCase());
        //
        //                return MonthDay.of(month, dayOfMonth);
        //            }
        //        }
        //        return null;
    }

    private Double ReadHeatingDegree()
    {
        NodeList nodes = GetUpperLevelNodeList("HeatingDegree", "Missing heating degree.");
        ArrayList<String> values = GetNodeListValues(nodes, "Missing heating degree.");
        if(values.size() > 0) {
            return Double.parseDouble(values.get(0));
        }
        return 0.0;
    }

    private ArrayList<ProjectInfoSummary> ReadSummaries() throws ClassNotFoundException, NoSuchMethodException, SecurityException,
    InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        ArrayList<ProjectInfoSummary> summaries = new ArrayList<ProjectInfoSummary>();

        NodeList summaryList = GetUpperLevelNodeList("Summary", "Missing zonal summaries.", "Summaries");
        ArrayList<String> summaryStrings = GetNodeListValues(summaryList, "Missing zonal summaries.");
        if(summaryStrings.size() > 0) {
            int ID;
            String shapefile;
            String areaValueField;
            String areaNameField;
            String temporalSummaryCompositionStrategyClassName;
            TemporalSummaryRasterFileStore fileStore;
            Class<?> strategyClass;
            Constructor<?> ctorStrategy;

            String summary;
            for(int i=0; i < summaryStrings.size(); i++)
            {
                summary = summaryStrings.get(i);
                ID = Integer.parseInt(((Element)summaryList.item(i)).getAttribute("ID"));
                // Shape File Path: C:\Users\sufi\Desktop\shapefile\shapefile.shp; Field: COUNTYNS10; Temporal Summary: GregorianWeeklyStrategy
                // Shape File Path: C:\Users\sufi\Desktop\shapefile\shapefile.shp; COUNTYNS10

                areaNameField = summary.substring(summary.indexOf(ProjectInfoSummary.AREA_NAME_FIELD_TAG + ": ") + String.valueOf(ProjectInfoSummary.AREA_NAME_FIELD_TAG + ": ").length(),
                        summary.indexOf(";"));
                shapefile = summary.substring(summary.indexOf(ProjectInfoSummary.SHAPE_FILE_TAG + ": ") + String.valueOf(ProjectInfoSummary.SHAPE_FILE_TAG + ": ").length(), summary.indexOf(";",
                        summary.indexOf(ProjectInfoSummary.SHAPE_FILE_TAG + ": ")));
                if(shapefile.startsWith("\\") || shapefile.startsWith("/"))
                {
                    shapefile = System.getProperty("user.dir") + shapefile;
                }
                if(summary.indexOf(ProjectInfoSummary.TEMPORAL_SUMMARY_TAG) == -1)
                {
                    areaValueField = summary.substring(summary.indexOf(ProjectInfoSummary.AREA_CODE_FIELD_TAG + ": ") + String.valueOf(ProjectInfoSummary.AREA_CODE_FIELD_TAG + ": ").length());
                    if(areaValueField.endsWith(";")) {
                        areaValueField = areaValueField.substring(0, areaValueField.length() - 1);
                    }
                    temporalSummaryCompositionStrategyClassName = null;
                    fileStore = null;
                }
                else
                {
                    areaValueField = summary.substring(summary.indexOf(ProjectInfoSummary.AREA_CODE_FIELD_TAG + ": ") + String.valueOf(ProjectInfoSummary.AREA_CODE_FIELD_TAG + ": ").length(),
                            summary.indexOf(";", summary.indexOf(ProjectInfoSummary.AREA_CODE_FIELD_TAG + ": ")));
                    temporalSummaryCompositionStrategyClassName = summary.substring(summary.indexOf(ProjectInfoSummary.TEMPORAL_SUMMARY_TAG + ": ") +
                            String.valueOf(ProjectInfoSummary.TEMPORAL_SUMMARY_TAG + ": ").length());
                    if(temporalSummaryCompositionStrategyClassName.endsWith(";")) {
                        temporalSummaryCompositionStrategyClassName = temporalSummaryCompositionStrategyClassName.substring(0, temporalSummaryCompositionStrategyClassName.length() - 1);
                    }
                    strategyClass = Class.forName("version2.prototype.summary.temporal.CompositionStrategies." + temporalSummaryCompositionStrategyClassName);
                    ctorStrategy = strategyClass.getConstructor();
                    fileStore = new TemporalSummaryRasterFileStore((TemporalSummaryCompositionStrategy)ctorStrategy.newInstance());
                }
                summaries.add(new ProjectInfoSummary(new ZonalSummary(shapefile, areaValueField, areaNameField), fileStore, temporalSummaryCompositionStrategyClassName, ID));
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
