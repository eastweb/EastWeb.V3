package version2.prototype.PluginMetaData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            instance = new PluginMetaDataCollection();
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
            instance = new PluginMetaDataCollection(xmlFile);
        }
        return instance;
    }

    private PluginMetaDataCollection(File xmlFile) throws ParserConfigurationException, SAXException, IOException{
        pluginList = new ArrayList<String>();
        File[] xmlFileArr = new File[1];
        xmlFileArr[0] = xmlFile;
        pluginMetaDataMap = createMap(xmlFileArr);
    }

    private PluginMetaDataCollection() throws ParserConfigurationException, SAXException, IOException{
        pluginList = new ArrayList<String>();
        File fileDir = new File(System.getProperty("user.dir") + "\\src\\version2\\prototype\\PluginMetaData\\");
        pluginMetaDataMap = createMap(getXMLFiles(fileDir));
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

        public String mode;// the protocol type: ftp or http
        public ftp myFtp;
        public http myHttp;
        public String className;
        public String timeZone;
        public int filesPerDay;
        public String datePattern;
        public String fileNamePattern;

        public DownloadMetaData(NodeList n){
            myFtp=null;
            myHttp=null;
            nList = n;
            Node downloadNode = nList.item(0);

            try{
                timeZone = ((Element) downloadNode).getElementsByTagName("TimeZone").item(0).getTextContent();
                timeZone = timeZone.substring(timeZone.indexOf(") ") + 2);
                className = ((Element) downloadNode).getElementsByTagName("Class").item(0).getTextContent();
                mode=((Element) downloadNode).getElementsByTagName("Mode").item(0).getTextContent();
                mode=mode.toUpperCase();

                if(mode.equalsIgnoreCase("FTP")) {
                    myFtp=new ftp(((Element)downloadNode).getElementsByTagName(mode).item(0));
                } else {
                    myHttp=new http(((Element)downloadNode).getElementsByTagName(mode).item(0));
                }

                filesPerDay = Integer.parseInt(((Element) downloadNode).getElementsByTagName("FilesPerDay").item(0).getTextContent());
                datePattern = ((Element) downloadNode).getElementsByTagName("DatePattern").item(0).getTextContent();
                fileNamePattern = ((Element) downloadNode).getElementsByTagName("FileNamePattern").item(0).getTextContent();
            }catch(Exception e){

            }
        }
    }

    public class ftp {
        public String hostName;
        public String rootDir;
        public String userName;
        public String password;

        public ftp(Node e){
            hostName=((Element)e).getElementsByTagName("HostName").item(0).getTextContent();
            rootDir=((Element)e).getElementsByTagName("RootDir").item(0).getTextContent();
            userName=((Element)e).getElementsByTagName("UserName").item(0).getTextContent();
            password=((Element)e).getElementsByTagName("PassWord").item(0).getTextContent();
        }
    }

    public class http {
        public String url;
        public http(Node e){
            url=((Element)e).getElementsByTagName("url").item(0).getTextContent();
        }
    }

    public class ProcessorMetaData {

        private NodeList nList;

        public Map<Integer, String> processStep;

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
    }


}

