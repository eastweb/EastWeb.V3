package version2.prototype.PluginMetaData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Handles reading in the plugin metadata from the xml files found in the package PluginMetaData.
 *
 * @author michael.devos
 * @author sufiabdul
 *
 */
public class PluginMetaDataCollection {
    private static PluginMetaDataCollection instance;
    public Map<String,PluginMetaData> pluginMetaDataMap;
    public ArrayList<String> pluginList;

    /**
     * Gets the stored PluginMetaDataCollection instance or creates a new one if none exists with all the plugin metadata files read in.
     *
     * @return a PluginMetaDataCollection instance
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static PluginMetaDataCollection getInstance() throws ParserConfigurationException, SAXException, IOException
    {
        if(instance == null) {
            File fileDir = new File(System.getProperty("user.dir") + "\\src\\version2\\prototype\\PluginMetaData\\");
            instance = new PluginMetaDataCollection(getXMLFiles(fileDir));
        }
        return instance;
    }

    /**
     * Gets the stored PluginMetaDataCollection instance or creates a new one if none exists with only the data read from the specified plugin metadata file.
     *
     * @param xmlFile  - the xml file to read in
     * @return a PluginMetaDataCollection instance
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static PluginMetaDataCollection getInstance(File xmlFile) throws ParserConfigurationException, SAXException, IOException
    {
        if(instance == null) {
            File[] xmlFileArr = new File[1];
            xmlFileArr[0] = xmlFile;
            instance = new PluginMetaDataCollection(xmlFileArr);
        }
        return instance;
    }

    /**
     * Provides a means to create a custom DownloadMetaData object mainly for testing purposes.
     *
     * @param mode
     * @param myFtp
     * @param myHttp
     * @param className
     * @param timeZone
     * @param filesPerDay
     * @param datePatternStr
     * @param fileNamePatternStr
     * @return a customized DownloadMetaData object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static DownloadMetaData CreateDownloadMetaData(String mode, FTP myFtp, HTTP myHttp, String className, String timeZone, int filesPerDay, String datePatternStr, String fileNamePatternStr)
            throws ParserConfigurationException, SAXException, IOException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new DownloadMetaData(mode, myFtp, myHttp, className, timeZone, filesPerDay, datePatternStr, fileNamePatternStr);
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
     * Provides a means to create a custom ProcessorMetaData mainly object for testing purposes.
     *
     * @param processSteps
     * @return a customized ProcessorMetaData object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static ProcessorMetaData CreateProcessorMetaData(Map<Integer, String> processSteps) throws ParserConfigurationException, SAXException, IOException
    {
        PluginMetaDataCollection collection = new PluginMetaDataCollection();
        return collection.new ProcessorMetaData(processSteps);
    }

    /**
     * Provides a means to create a custom SummaryMetaData mainly object for testing purposes.
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
        return collection.new SummaryMetaData(mergeStrategyClass, interpolateStrategyClass);
    }

    private PluginMetaDataCollection(File[] xmlFiles) throws ParserConfigurationException, SAXException, IOException{
        pluginList = new ArrayList<String>();
        pluginMetaDataMap = createMap(xmlFiles);
    }

    private PluginMetaDataCollection() throws ParserConfigurationException, SAXException, IOException{
        pluginList = new ArrayList<String>();
    }

    private Map<String, PluginMetaData> createMap(File[] xmlFiles) throws ParserConfigurationException, SAXException, IOException{
        Map<String,PluginMetaData> myMap=new HashMap<String,PluginMetaData>();
        for(File fXmlFile: xmlFiles){

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            PluginMetaData temp=new PluginMetaData();
            temp.Title = doc.getElementsByTagName("title").item(0).getTextContent();
            temp.DaysPerInputData = Integer.parseInt(doc.getElementsByTagName("DaysPerInputData").item(0).getTextContent());
            temp.Download = new DownloadMetaData(doc.getElementsByTagName("Download"));
            temp.Processor = new ProcessorMetaData(doc.getElementsByTagName("Processor"));

            temp.IndicesMetaData = new ArrayList<String>();
            NodeList tempIndices = doc.getElementsByTagName("Indices");
            int nodesIndices = ((Element) tempIndices.item(0)).getElementsByTagName("Class").getLength();
            for(int i = 0; i < nodesIndices; i++) {
                temp.IndicesMetaData.add( ((Element) tempIndices.item(0)).getElementsByTagName("Class").item(i).getTextContent());
            }

            temp.Summary = new SummaryMetaData(doc.getElementsByTagName("Summary"));

            temp.QualityControlMetaData = new ArrayList<String>();
            NodeList tempQC = doc.getElementsByTagName("QualityControl");
            int nodesQC = ((Element) tempQC.item(0)).getElementsByTagName("Level").getLength();
            for(int i = 0; i < nodesQC; i++) {
                temp.QualityControlMetaData.add( ((Element) tempQC.item(0)).getElementsByTagName("Level").item(i).getTextContent());
            }

            String pluginName = FilenameUtils.removeExtension(fXmlFile.getName()).replace("Plugin_","");
            pluginList.add(pluginName);
            myMap.put(pluginName, temp);

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

        public DownloadMetaData Download;
        public ProcessorMetaData Processor;
        public SummaryMetaData Summary;
        public ArrayList<String> IndicesMetaData;
        public ArrayList<String> QualityControlMetaData;
        public String Title;
        public int DaysPerInputData;
    }

    public class DownloadMetaData{
        private NodeList nList;

        public final String mode;// the protocol type: ftp or http
        public final FTP myFtp;
        public final HTTP myHttp;
        public final String className;
        public final String timeZone;
        public final int filesPerDay;
        public final Pattern datePattern;
        public final Pattern fileNamePattern;

        public DownloadMetaData(NodeList n){
            String tempMode = null;
            FTP tempFtp = null;
            HTTP tempHttp = null;
            String tempClassName = null;
            String tempTimeZone = null;
            int tempFilesPerDay = -1;
            Pattern tempDatePattern = null;
            Pattern tempFileNamePattern = null;

            nList = n;
            Node downloadNode = nList.item(0);

            try{
                tempTimeZone = ((Element) downloadNode).getElementsByTagName("TimeZone").item(0).getTextContent();
                tempTimeZone = tempTimeZone.substring(tempTimeZone.indexOf(") ") + 2);
                tempClassName = ((Element) downloadNode).getElementsByTagName("Class").item(0).getTextContent();
                tempMode = ((Element) downloadNode).getElementsByTagName("Mode").item(0).getTextContent();
                tempMode = tempMode.toUpperCase();

                if(tempMode.equalsIgnoreCase("FTP")) {
                    tempFtp = new FTP(((Element)downloadNode).getElementsByTagName(tempMode).item(0));
                } else {
                    tempHttp = new HTTP(((Element)downloadNode).getElementsByTagName(tempMode).item(0));
                }

                tempFilesPerDay = Integer.parseInt(((Element) downloadNode).getElementsByTagName("FilesPerDay").item(0).getTextContent());
                tempDatePattern = Pattern.compile(((Element) downloadNode).getElementsByTagName("DatePattern").item(0).getTextContent());
                tempFileNamePattern = Pattern.compile(((Element) downloadNode).getElementsByTagName("FileNamePattern").item(0).getTextContent());
            }catch(Exception e){

            }
            mode = tempMode;
            myFtp = tempFtp;
            myHttp = tempHttp;
            className = tempClassName;
            timeZone = tempTimeZone;
            filesPerDay = tempFilesPerDay;
            datePattern = tempDatePattern;
            fileNamePattern = tempFileNamePattern;
        }

        public DownloadMetaData(String mode, FTP myFtp, HTTP myHttp, String className, String timeZone, int filesPerDay, String datePatternStr, String fileNamePatternStr)
        {
            this.mode = mode;
            this.myFtp = myFtp;
            this.myHttp = myHttp;
            this.className = className;
            this.timeZone = timeZone;
            this.filesPerDay = filesPerDay;
            datePattern = Pattern.compile(datePatternStr);
            fileNamePattern = Pattern.compile(fileNamePatternStr);
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
            url=((Element)e).getElementsByTagName("url").item(0).getTextContent();
        }

        public HTTP(String url)
        {
            this.url = url;
        }
    }

    public class ProcessorMetaData {

        private NodeList nList;

        public final Map<Integer, String> processStep;

        public ProcessorMetaData(NodeList n){
            nList = n;
            processStep = new HashMap<Integer, String>();
            Node processorNode = nList.item(0);

            try{
                NodeList processSteps = ((Element) processorNode).getElementsByTagName("ProcessStep");

                for(int i=0; i < processSteps.getLength(); i++)
                {
                    processStep.put(i+1, processSteps.item(i).getTextContent());
                }
            }catch(Exception e){

            }
        }

        public ProcessorMetaData(Map<Integer, String> processSteps)
        {
            processStep = processSteps;
        }
    }

    public class SummaryMetaData{
        public final String mergeStrategyClass;
        public final String interpolateStrategyClass;

        private NodeList nList;

        public SummaryMetaData(NodeList n){
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

        public SummaryMetaData(String mergeStrategyClass, String interpolateStrategyClass)
        {
            this.mergeStrategyClass = mergeStrategyClass;
            this.interpolateStrategyClass = interpolateStrategyClass;
        }
    }
}

