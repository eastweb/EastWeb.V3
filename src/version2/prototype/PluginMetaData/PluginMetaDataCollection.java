package version2.prototype.PluginMetaData;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import version2.prototype.util.FileSystem;

/**
 * Handles reading in the plugin metadata from the xml files found in the package PluginMetaData.
 *
 * @author michael.devos
 * @author sufiabdul
 *
 */
public class PluginMetaDataCollection {
    private static PluginMetaDataCollection instance;
    /**
     * PluginMetaData objects are mapped based on the xml file name from which they were parsed from.
     * The string used is that of the file name but with the prepended "Plugin_" stripped out.
     * For the typical case, this would make the plugin's 'Title' element equal to this value.
     */
    public Map<String,PluginMetaData> pluginMetaDataMap;
    /**
     * List of plugin titles from loaded plugin metadata files.
     */
    public ArrayList<String> pluginList;

    /**
     * Gets the stored PluginMetaDataCollection instance or creates a new one if none exists with all the plugin metadata files read in.
     *
     * @return a PluginMetaDataCollection instance
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws DOMException
     * @throws PatternSyntaxException
     */
    public static PluginMetaDataCollection getInstance() throws PatternSyntaxException, DOMException, ParserConfigurationException, SAXException, IOException
    {
        if(instance == null) {
            File fileDir = new File(System.getProperty("user.dir") + "\\plugins\\");
            instance = new PluginMetaDataCollection(getXMLFiles(fileDir));
        }
        return instance;
    }

    /**
     * Gets the stored PluginMetaDataCollection instance or creates a new one if none exists with only the data read from the specified plugin metadata file.
     *
     * @param xmlFilePath  - the xml file path to read in
     * @return a PluginMetaDataCollection instance
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws DOMException
     * @throws PatternSyntaxException
     */
    public static PluginMetaDataCollection getInstance(String xmlFilePath) throws ParserConfigurationException, SAXException, IOException, DOMException, PatternSyntaxException
    {
        File[] xmlFileArr = new File[1];
        xmlFileArr[0] = new File(xmlFilePath);
        return new PluginMetaDataCollection(xmlFileArr);
    }

    /**
     * Gets the stored PluginMetaDataCollection instance or creates a new one if none exists with only the data read from the specified plugin metadata file.
     *
     * @param xmlFile  - the xml file to read in
     * @return a PluginMetaDataCollection instance
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws DOMException
     * @throws PatternSyntaxException
     */
    public static PluginMetaDataCollection getInstance(File xmlFile) throws ParserConfigurationException, SAXException, IOException, DOMException, PatternSyntaxException
    {
        File[] xmlFileArr = new File[1];
        xmlFileArr[0] = xmlFile;
        return new PluginMetaDataCollection(xmlFileArr);
    }

    /**
     * Gets the stored PluginMetaDataCollection instance or creates a new one if none exists with only the data read from the specified plugin metadata file.
     *
     * @param xmlFiles  - array of xml files to read in
     * @return a PluginMetaDataCollection instance
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws DOMException
     * @throws PatternSyntaxException
     */
    public static PluginMetaDataCollection getInstance(File[] xmlFiles) throws ParserConfigurationException, SAXException, IOException, DOMException, PatternSyntaxException
    {
        return new PluginMetaDataCollection(xmlFiles);
    }

    /**
     * Provides a means to create a custom DownloadMetaData object mainly for testing purposes. Used if no extra downloads are specified (only one Download section in meta data). Name field will is
     * defaulted to "Data".
     *
     * @param mode
     * @param myFtp
     * @param myHttp
     * @param downloadFactoryClassName
     * @param timeZone
     * @param filesPerDay
     * @param datePatternStr
     * @param fileNamePatternStr
     * @param originDate
     * @return a customized DownloadMetaData object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws PatternSyntaxException
     */
    public static DownloadMetaData CreateDownloadMetaData(String mode, FTP myFtp, HTTP myHttp, String downloadFactoryClassName, String timeZone, int filesPerDay,
            String datePatternStr, String fileNamePatternStr, LocalDate originDate) throws ParserConfigurationException, SAXException, IOException, PatternSyntaxException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new DownloadMetaData(null, null, null, null, mode, myFtp, myHttp, downloadFactoryClassName, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, originDate);
    }

    /**
     * Provides a means to create a custom DownloadMetaData object mainly for testing purposes. Used if extra downloads are specified. Will need to create those extras using a different method (to be
     * accurate the CreateDownloadMetaData(String name, ...) should be used.
     *
     * @param mode
     * @param myFtp
     * @param myHttp
     * @param downloadFactoryClassName
     * @param timeZone
     * @param filesPerDay
     * @param datePatternStr
     * @param fileNamePatternStr
     * @param extraDownloads
     * @param originDate
     * @return a customized DownloadMetaData object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws PatternSyntaxException
     */
    public static DownloadMetaData CreateDownloadMetaData(String mode, FTP myFtp, HTTP myHttp, String downloadFactoryClassName, String timeZone, int filesPerDay,
            String datePatternStr, String fileNamePatternStr, ArrayList<DownloadMetaData> extraDownloads, LocalDate originDate) throws ParserConfigurationException, SAXException, IOException, PatternSyntaxException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new DownloadMetaData(null, null, null, null, mode, myFtp, myHttp, downloadFactoryClassName, timeZone, filesPerDay, datePatternStr, fileNamePatternStr,
                extraDownloads, originDate);
    }

    /**
     * Provides a means to create a custom DownloadMetaData object mainly for testing purposes. Creates a DownloadMetaData object to represent one of the extra downloads.
     * @param name
     * @param mode
     * @param myFtp
     * @param myHttp
     * @param downloadFactoryClassName
     * @param timeZone
     * @param filesPerDay
     * @param datePatternStr
     * @param fileNamePatternStr
     * @param originDate
     * @return a customized DownloadMetaData object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws PatternSyntaxException
     */
    public static DownloadMetaData CreateDownloadMetaData(String name, String mode, FTP myFtp, HTTP myHttp, String downloadFactoryClassName, String timeZone, int filesPerDay,
            String datePatternStr, String fileNamePatternStr, LocalDate originDate) throws ParserConfigurationException, SAXException, IOException, PatternSyntaxException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new DownloadMetaData(null, null, null, null, name, mode, myFtp, myHttp, downloadFactoryClassName, timeZone, filesPerDay, datePatternStr, fileNamePatternStr, originDate);
    }

    /**
     * Provides a means to create a custom FTP object mainly for testing purposes.
     *
     * @param hostName
     * @param rootDir
     * @param userName
     * @param password
     * @return a customized FTP object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static FTP CreateFTP(String hostName, String rootDir, String userName, String password) throws ParserConfigurationException, SAXException, IOException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new FTP(hostName, rootDir, userName, password);
    }

    /**
     * Provides a means to create a custom HTTP object mainly for testing purposes.
     *
     * @param url
     * @return a customized HTTP object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static HTTP CreateHTTP(String url) throws ParserConfigurationException, SAXException, IOException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new HTTP(url);
    }

    /**
     * Provides a means to create a custom ProcessorMetaData object mainly for testing purposes.
     *
     * @param processSteps
     * @param numberOfOutputs
     * @return a customized ProcessorMetaData object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static ProcessorMetaData CreateProcessorMetaData(Map<Integer, String> processSteps, Integer numberOfOutputs) throws ParserConfigurationException, SAXException, IOException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new ProcessorMetaData(null, null, null, null, processSteps, numberOfOutputs);
    }

    /**
     * Provides a means to create a custom IndicesMetaData object mainly for testing purposes.
     *
     * @param indicesNames
     * @return a customized IndicesMetaData object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static IndicesMetaData CreateIndicesMetaData(ArrayList<String> indicesNames) throws ParserConfigurationException, SAXException, IOException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new IndicesMetaData(null, null, null, null, indicesNames);
    }

    /**
     * Provides a means to create a custom SummaryMetaData object mainly for testing purposes.
     *
     * @param mergeStrategyClass
     * @param interpolateStrategyClass
     * @return a customized SummaryMetaData object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static SummaryMetaData CreateSummaryMetaData(String mergeStrategyClass, String interpolateStrategyClass) throws ParserConfigurationException, SAXException, IOException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new SummaryMetaData(null, null, null, null, mergeStrategyClass, interpolateStrategyClass);
    }

    /**
     * Provides a means to create a custom PluginMetaData object mainly for testing purposes.
     *
     * @param Download
     * @param Processor
     * @param Indices
     * @param Summary
     * @param QualityControlMetaData
     * @param Title
     * @param DaysPerInputData
     * @param Resolution
     * @param ExtraDownloadFiles
     * @param ExtraInfo
     * @return a customized PluginMetaData object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static PluginMetaData CreatePluginMetaData(String Title, Integer DaysPerInputData, Integer Resolution, ArrayList<String> ExtraDownloadFiles, DownloadMetaData Download, ProcessorMetaData Processor,
            IndicesMetaData Indices, SummaryMetaData Summary, ArrayList<String> QualityControlMetaData, ExtraInfoData ExtraInfo) throws ParserConfigurationException, SAXException, IOException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new PluginMetaData(Title, DaysPerInputData, Resolution, ExtraDownloadFiles, Download, Processor, Indices, Summary, QualityControlMetaData, ExtraInfo);
    }

    private PluginMetaDataCollection(File[] xmlFiles) throws ParserConfigurationException, SAXException, IOException, DOMException, PatternSyntaxException
    {
        pluginList = new ArrayList<String>();
        pluginMetaDataMap = createMap(xmlFiles);
    }

    private PluginMetaDataCollection() throws ParserConfigurationException, SAXException, IOException{
        pluginList = new ArrayList<String>();
    }

    private Map<String, PluginMetaData> createMap(File[] xmlFiles) throws ParserConfigurationException, SAXException, IOException, DOMException, PatternSyntaxException
    {
        Map<String,PluginMetaData> myMap=new HashMap<String,PluginMetaData>();
        for(File fXmlFile: xmlFiles){
            // Setup Document
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            // Get top level non-process specific elements
            String Title = doc.getElementsByTagName("Title").item(0).getTextContent();
            int DaysPerInputData = Integer.parseInt(doc.getElementsByTagName("DaysPerInputData").item(0).getTextContent());
            int resolution = Integer.parseInt(doc.getElementsByTagName("Resolution").item(0).getTextContent());

            ArrayList<String> QualityControlMetaData = new ArrayList<String>();
            NodeList tempQC = doc.getElementsByTagName("QualityControl");
            int nodesQC = ((Element) tempQC.item(0)).getElementsByTagName("Level").getLength();
            for(int i = 0; i < nodesQC; i++) {
                QualityControlMetaData.add( ((Element) tempQC.item(0)).getElementsByTagName("Level").item(i).getTextContent());
            }

            ArrayList<String> ExtraDownloadFiles = new ArrayList<String>();
            NodeList extraDownloadFilesNodeList = doc.getElementsByTagName("ExtraDownloadFiles");
            int extras = ((Element) extraDownloadFilesNodeList.item(0)).getElementsByTagName("Name").getLength();
            for(int i=0; i < extras; i++)
            {
                ExtraDownloadFiles.add( ((Element) extraDownloadFilesNodeList.item(0)).getElementsByTagName("Name").item(i).getTextContent());
            }

            // Get process specific metadata
            DownloadMetaData Download = new DownloadMetaData(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles, doc.getElementsByTagName("Download"));
            ProcessorMetaData Processor = new ProcessorMetaData(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles, doc.getElementsByTagName("Processor"));
            IndicesMetaData Indices = new IndicesMetaData(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles, doc.getElementsByTagName("Indices"));
            SummaryMetaData Summary = new SummaryMetaData(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles, doc.getElementsByTagName("Summary"));

            // Extra Info
            ExtraInfoData ExtraInfo = new ExtraInfoData(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles, doc.getElementsByTagName("ExtraInfo"));

            // Setup map
            //            String pluginName = FilenameUtils.removeExtension(fXmlFile.getName()).replace("Plugin_","");
            pluginList.add(Title);
            myMap.put(Title, new PluginMetaData(Title, DaysPerInputData, resolution, ExtraDownloadFiles, Download, Processor, Indices, Summary, QualityControlMetaData, ExtraInfo));

        }
        return myMap;
    }

    private static File[] getXMLFiles(File folder) {
        List<File> aList = new ArrayList<File>();

        File[] files = folder.listFiles();
        for (File pf : files) {
            if (pf.isFile() && getFileExtensionName(pf).indexOf("xml") != -1) {
                aList.add(pf);
            }
        }
        return aList.toArray(new File[aList.size()]);
    }

    private static String getFileExtensionName(File f) {
        if (f.getName().indexOf(".") == -1) {
            return "";
        } else {
            return f.getName().substring(f.getName().length() - 3, f.getName().length());
        }
    }

    public class PluginMetaData {
        public final String Title;
        public final Integer DaysPerInputData;
        public final Integer Resolution;
        public final ArrayList<String> ExtraDownloadFiles;
        public final DownloadMetaData Download;
        public final ProcessorMetaData Processor;
        public final IndicesMetaData Indices;
        public final SummaryMetaData Summary;
        public final ArrayList<String> QualityControlMetaData;
        public final ExtraInfoData ExtraInfo;

        public PluginMetaData(String Title, Integer DaysPerInputData, Integer Resolution, ArrayList<String> ExtraDownloadFiles, DownloadMetaData Download, ProcessorMetaData Processor, IndicesMetaData Indices,
                SummaryMetaData Summary, ArrayList<String> QualityControlMetaData, ExtraInfoData ExtraInfo)
        {
            this.Download = Download;
            this.Processor = Processor;
            this.Indices = Indices;
            this.Summary = Summary;
            this.QualityControlMetaData = QualityControlMetaData;
            this.Title = Title;
            this.DaysPerInputData = DaysPerInputData;
            this.Resolution = Resolution;
            this.ExtraDownloadFiles = ExtraDownloadFiles;
            this.ExtraInfo = ExtraInfo;
        }
    }

    public abstract class ProcessMetaData {
        public final ArrayList<String> QualityControlMetaData;
        public final String Title;
        public final Integer DaysPerInputData;
        public final ArrayList<String> ExtraDownloadFiles;

        protected ProcessMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles)
        {
            this.QualityControlMetaData = QualityControlMetaData;
            this.Title = Title;
            this.DaysPerInputData = DaysPerInputData;
            this.ExtraDownloadFiles = ExtraDownloadFiles;
        }
    }

    public class DownloadMetaData extends ProcessMetaData{
        private NodeList nList;

        public final String name;   // Attribute defined
        public final String mode;   // the protocol type: ftp or http
        public final FTP myFtp;
        public final HTTP myHttp;
        public final String downloadFactoryClassName;
        public final String timeZone;
        public final int filesPerDay;
        public final Pattern datePattern;
        public final Pattern fileNamePattern;
        public final ArrayList<DownloadMetaData> extraDownloads;
        public final LocalDate originDate;

        public DownloadMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles, NodeList n) throws DOMException, PatternSyntaxException
        {
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            String tempName = null;
            String tempMode = null;
            FTP tempFtp = null;
            HTTP tempHttp = null;
            String tempDownloadFactoryClassName = null;
            String tempTimeZone = null;
            int tempFilesPerDay = -1;
            Pattern tempDatePattern = null;
            Pattern tempFileNamePattern = null;
            Node dataNode = null;
            int dataNodeIdx = -1;

            nList = n;

            // If there are multiple Download elements then find the Download element with attribute Name="Data"
            Element temp;
            if(nList.getLength() > 1)
            {
                for(int i=0; i < nList.getLength(); i++)
                {
                    temp = (Element) nList.item(i);
                    if(temp.getNodeName().equals("Download") && temp.hasAttribute("Name") && temp.getAttribute("Name").toLowerCase().equals("data"))
                    {
                        dataNode = nList.item(i);
                        dataNodeIdx = i;
                        tempName = "data";
                        break;
                    }
                    else if(!temp.hasAttribute("Name"))
                    {
                        throw new DOMException((short) 0, "A Download element is missing the attribute \"Name\".");
                    }
                }

                if(tempName != null && dataNode != null) {
                    name = FileSystem.StandardizeName(tempName);
                }
                else {
                    throw new DOMException((short) 0, "Missing Download element with attribute Name=\"Date\".");
                }
            }
            else
            {
                dataNode = nList.item(0);
                temp = (Element) dataNode;
                if((temp.hasAttribute("Name") && temp.getAttribute("Name").toLowerCase().equals("data")) || !temp.hasAttribute("Name"))
                {
                    name = "data";
                } else {
                    throw new DOMException((short) 0, "Missing Download element with attribute Name=\"Date\".");
                }
            }

            // Set properties
            Element dataElement = (Element) dataNode;
            tempTimeZone = dataElement.getElementsByTagName("TimeZone").item(0).getTextContent();
            tempDownloadFactoryClassName = dataElement.getElementsByTagName("DownloadFactoryClassName").item(0).getTextContent();
            tempMode = dataElement.getElementsByTagName("Mode").item(0).getTextContent();
            tempMode = tempMode.toUpperCase();

            if(tempMode.equalsIgnoreCase("FTP")) {
                tempFtp = new FTP(dataElement.getElementsByTagName(tempMode).item(0));
            } else {
                tempHttp = new HTTP(dataElement.getElementsByTagName(tempMode).item(0));
            }

            tempFilesPerDay = Integer.parseInt(dataElement.getElementsByTagName("FilesPerDay").item(0).getTextContent());
            tempDatePattern = Pattern.compile(dataElement.getElementsByTagName("DatePattern").item(0).getTextContent());
            tempFileNamePattern = Pattern.compile(dataElement.getElementsByTagName("FileNamePattern").item(0).getTextContent());

            mode = tempMode;
            myFtp = tempFtp;
            myHttp = tempHttp;
            downloadFactoryClassName = tempDownloadFactoryClassName;
            timeZone = tempTimeZone;
            filesPerDay = tempFilesPerDay;
            datePattern = tempDatePattern;
            fileNamePattern = tempFileNamePattern;

            Element originDateElement = (Element) dataElement.getElementsByTagName("OriginDate").item(0);
            int dayOfMonth = Integer.parseInt(originDateElement.getElementsByTagName("DayOfMonth").item(0).getTextContent());
            String month = originDateElement.getElementsByTagName("Month").item(0).getTextContent();
            int year = Integer.parseInt(originDateElement.getElementsByTagName("Year").item(0).getTextContent());
            originDate = LocalDate.of(year, Month.valueOf(month.toUpperCase()), dayOfMonth);

            if(nList.getLength() > 1)
            {
                extraDownloads = new ArrayList<DownloadMetaData>();
                for(int i=0; i < nList.getLength(); i++)
                {
                    if(i != dataNodeIdx)
                    {
                        extraDownloads.add(new DownloadMetaData(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles, nList.item(i), timeZone, filesPerDay, originDate));
                    }
                }
            } else {
                extraDownloads = new ArrayList<DownloadMetaData>();
            }
        }

        public DownloadMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles, String mode, FTP myFtp, HTTP myHttp,
                String downloadFactoryClassName, String timeZone, int filesPerDay, String datePatternStr, String fileNamePatternStr, LocalDate originDate) throws PatternSyntaxException
        {
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            name = "data";
            this.mode = mode;
            this.myFtp = myFtp;
            this.myHttp = myHttp;
            this.downloadFactoryClassName = downloadFactoryClassName;
            this.timeZone = timeZone;
            this.filesPerDay = filesPerDay;
            if(datePatternStr != null) {
                datePattern = Pattern.compile(datePatternStr);
            } else {
                datePattern = null;
            }
            if(fileNamePatternStr != null) {
                fileNamePattern = Pattern.compile(fileNamePatternStr);
            } else {
                fileNamePattern = null;
            }
            extraDownloads = null;
            this.originDate = originDate;
        }

        public DownloadMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles, String mode, FTP myFtp, HTTP myHttp,
                String downloadFactoryClassName, String timeZone, int filesPerDay, String datePatternStr, String fileNamePatternStr, ArrayList<DownloadMetaData> extraDownloads, LocalDate originDate)
                        throws PatternSyntaxException
        {
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            name = "data";
            this.mode = mode;
            this.myFtp = myFtp;
            this.myHttp = myHttp;
            this.downloadFactoryClassName = downloadFactoryClassName;
            this.timeZone = timeZone;
            this.filesPerDay = filesPerDay;
            if(datePatternStr != null) {
                datePattern = Pattern.compile(datePatternStr);
            } else {
                datePattern = null;
            }
            if(fileNamePatternStr != null) {
                fileNamePattern = Pattern.compile(fileNamePatternStr);
            } else {
                fileNamePattern = null;
            }
            this.extraDownloads = extraDownloads;
            this.originDate = originDate;
        }

        public DownloadMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles, String name, String mode, FTP myFtp,
                HTTP myHttp, String downloadFactoryClassName, String timeZone, int filesPerDay, String datePatternStr, String fileNamePatternStr, LocalDate originDate) throws PatternSyntaxException
        {
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            this.name = name.toLowerCase();
            this.mode = mode;
            this.myFtp = myFtp;
            this.myHttp = myHttp;
            this.downloadFactoryClassName = downloadFactoryClassName;
            this.timeZone = timeZone;
            this.filesPerDay = filesPerDay;
            if(datePatternStr != null) {
                datePattern = Pattern.compile(datePatternStr);
            } else {
                datePattern = null;
            }
            if(fileNamePatternStr != null) {
                fileNamePattern = Pattern.compile(fileNamePatternStr);
            } else {
                fileNamePattern = null;
            }
            extraDownloads = new ArrayList<DownloadMetaData>();
            this.originDate = originDate;
        }

        private DownloadMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles, Node extraDownloadNode, String defaultTimeZone,
                int defaultFilesPerDay, LocalDate dataOriginDate) throws PatternSyntaxException, DOMException
        {
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            String tempMode = null;
            FTP tempFtp = null;
            HTTP tempHttp = null;
            String tempDownloadFactoryClassName = null;
            String tempTimeZone = null;
            int tempFilesPerDay = -1;
            Pattern tempDatePattern = null;
            Pattern tempFileNamePattern = null;
            nList = null;
            extraDownloads = new ArrayList<DownloadMetaData>();

            // Set properties
            if(((Element) extraDownloadNode).hasAttribute("Name")) {
                name = FileSystem.StandardizeName(((Element) extraDownloadNode).getAttribute("Name")).toLowerCase();
            } else {
                throw new DOMException((short) 0, "A Download element is missing the attribute \"Name\".");
            }

            if(((Element) extraDownloadNode).getElementsByTagName("TimeZone").getLength() > 0) {
                tempTimeZone = ((Element) extraDownloadNode).getElementsByTagName("TimeZone").item(0).getTextContent();
            } else {
                tempTimeZone = defaultTimeZone;
            }
            tempDownloadFactoryClassName = ((Element) extraDownloadNode).getElementsByTagName("DownloadFactoryClassName").item(0).getTextContent();
            tempMode = ((Element) extraDownloadNode).getElementsByTagName("Mode").item(0).getTextContent();
            tempMode = tempMode.toUpperCase();

            if(tempMode.equalsIgnoreCase("FTP")) {
                tempFtp = new FTP(((Element)extraDownloadNode).getElementsByTagName(tempMode).item(0));
            } else {
                tempHttp = new HTTP(((Element)extraDownloadNode).getElementsByTagName(tempMode).item(0));
            }

            if(((Element) extraDownloadNode).getElementsByTagName("FilesPerDay").getLength() > 0) {
                tempFilesPerDay = Integer.parseInt(((Element) extraDownloadNode).getElementsByTagName("FilesPerDay").item(0).getTextContent());
            } else {
                tempFilesPerDay = defaultFilesPerDay;
            }
            tempDatePattern = Pattern.compile(((Element) extraDownloadNode).getElementsByTagName("DatePattern").item(0).getTextContent());
            tempFileNamePattern = Pattern.compile(((Element) extraDownloadNode).getElementsByTagName("FileNamePattern").item(0).getTextContent());

            mode = tempMode;
            myFtp = tempFtp;
            myHttp = tempHttp;
            downloadFactoryClassName = tempDownloadFactoryClassName;
            timeZone = tempTimeZone;
            filesPerDay = tempFilesPerDay;
            datePattern = tempDatePattern;
            fileNamePattern = tempFileNamePattern;
            originDate = dataOriginDate;
        }
    }

    public class FTP {
        public final String hostName;
        public final String rootDir;
        public final String userName;
        public final String password;

        public FTP(Node e){
            hostName=((Element)e).getElementsByTagName("HostName").item(0).getTextContent();
            rootDir=((Element)e).getElementsByTagName("RootDir").item(0).getTextContent();
            userName=((Element)e).getElementsByTagName("UserName").item(0).getTextContent();
            password=((Element)e).getElementsByTagName("PassWord").item(0).getTextContent();
        }

        public FTP(String hostName, String rootDir, String userName, String password)
        {
            this.hostName = hostName;
            this.rootDir = rootDir;
            this.userName = userName;
            this.password = password;
        }
    }

    public class HTTP {
        public final String url;

        public HTTP(Node e){
            url=((Element)e).getElementsByTagName("URL").item(0).getTextContent();
        }

        public HTTP(String url)
        {
            this.url = url;
        }
    }

    public class ProcessorMetaData extends ProcessMetaData {

        private NodeList nList;

        public final Map<Integer, String> processStep;
        public final Integer numOfOutput;

        public ProcessorMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles, NodeList n){
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            nList = n;
            processStep = new HashMap<Integer, String>();
            Element processorNode = (Element) nList.item(0);

            NodeList processSteps = processorNode.getElementsByTagName("ProcessStep");

            for(int i=0; i < processSteps.getLength(); i++)
            {
                processStep.put(i+1, processSteps.item(i).getTextContent());
            }

            numOfOutput = Integer.parseInt(processorNode.getElementsByTagName("NumberOfOutput").item(0).getTextContent());
        }

        public ProcessorMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles,
                Map<Integer, String> processSteps, Integer numOfOutput)
        {
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            processStep = processSteps;
            this.numOfOutput = numOfOutput;
        }
    }

    public class IndicesMetaData extends ProcessMetaData {
        public final ArrayList<String> indicesNames;

        private NodeList nList;

        public IndicesMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles, NodeList n) {
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            nList = n;

            indicesNames = new ArrayList<String>();
            int nodesIndices = ((Element) nList.item(0)).getElementsByTagName("ClassName").getLength();
            for(int i = 0; i < nodesIndices; i++) {
                indicesNames.add( ((Element) nList.item(0)).getElementsByTagName("ClassName").item(i).getTextContent());
            }
        }

        public IndicesMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles, ArrayList<String> indicesNames)
        {
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            this.indicesNames = indicesNames;
            nList = null;
        }
    }

    public class SummaryMetaData extends ProcessMetaData {
        public final String mergeStrategyClass;
        public final String interpolateStrategyClass;

        private NodeList nList;

        public SummaryMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles, NodeList n){
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            nList = n;
            Node summaryNode = nList.item(0);

            //            NodeList temporal = ((Element) nList).getElementsByTagName("Temporal");
            Node temporalNode = ((Element) summaryNode).getElementsByTagName("Temporal").item(0);

            // Node: DaysPerInputData
            //            Node DaysPerInputData = ((Element) temporal).getElementsByTagName("DaysPerInputData").item(0);
            //            NodeList tempList = DaysPerInputData.getChildNodes();
            //            Node valueNode = tempList.item(0);
            //            daysPerInputData = Integer.parseInt(valueNode.getNodeValue().trim());
            //            daysPerInputData = Integer.parseInt(((Element) temporalNode).getElementsByTagName("DaysPerInputData").item(0).getTextContent());

            // Node: MergeStrategyClass
            mergeStrategyClass = (((Element) temporalNode).getElementsByTagName("MergeStrategyClass").item(0).getTextContent());

            // Node: InterpolateStrategyClass
            interpolateStrategyClass = (((Element) temporalNode).getElementsByTagName("InterpolateStrategyClass").item(0).getTextContent());
        }

        public SummaryMetaData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles,
                String mergeStrategyClass, String interpolateStrategyClass)
        {
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            this.mergeStrategyClass = mergeStrategyClass;
            this.interpolateStrategyClass = interpolateStrategyClass;
        }
    }

    public class ExtraInfoData extends ProcessMetaData {
        public final Boolean Tiles;

        private final NodeList nList;

        protected ExtraInfoData(ArrayList<String> QualityControlMetaData, String Title, Integer DaysPerInputData, ArrayList<String> ExtraDownloadFiles, NodeList n) {
            super(QualityControlMetaData, Title, DaysPerInputData, ExtraDownloadFiles);
            nList = n;

            Node extraInfo = nList.item(0);

            Tiles = Boolean.parseBoolean(((Element) extraInfo).getElementsByTagName("Tiles").item(0).getTextContent().toUpperCase());
        }
    }
}

