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

public class PluginMetaDataCollection {

    public static PluginMetaDataCollection getInstance() throws ParserConfigurationException, SAXException, IOException
    {
        if(instance == null) {
            instance = new PluginMetaDataCollection();
        }
        return instance;
    }
    private static PluginMetaDataCollection instance;

    public Map<String,PluginMetaData> pluginMetaDataMap;
    public ArrayList<String> pluginList;

    public PluginMetaDataCollection() throws ParserConfigurationException, SAXException, IOException{
        pluginList = new ArrayList<String>();
        pluginMetaDataMap = createMap();
    }

    private Map<String, PluginMetaData> createMap() throws ParserConfigurationException, SAXException, IOException{
        Map<String,PluginMetaData> myMap=new HashMap<String,PluginMetaData>();
        File fileDir = new File(System.getProperty("user.dir") + "\\src\\version2\\prototype\\PluginMetaData\\");
        for(File fXmlFile: getXMLFiles(fileDir)){

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            PluginMetaData temp=new PluginMetaData();
            temp.Title = doc.getElementsByTagName("title").item(0).getTextContent();
            temp.Download = new DownloadMetaData(doc.getElementsByTagName("Download"));
            temp.Projection = new ProcessorMetaData(doc.getElementsByTagName("Processor"));

            temp.IndicesMetaData = new ArrayList<String>();
            NodeList tempIndices = doc.getElementsByTagName("Indices");
            int nodesIndices = ((Element) tempIndices.item(0)).getElementsByTagName("ClassName").getLength();
            for(int i = 0; i < nodesIndices; i++) {
                temp.IndicesMetaData.add( ((Element) tempIndices.item(0)).getElementsByTagName("ClassName").item(i).getTextContent());
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
        public ProcessorMetaData Projection;
        public SummaryMetaData Summary;
        public ArrayList<String> IndicesMetaData;
        public ArrayList<String> QualityControlMetaData;
        public String Title;
    }

    public class DownloadMetaData{
        private NodeList nList;

        public String mode;// the protocol type: ftp or http
        public ftp myFtp;
        public http myHttp;
        public String className;

        public DownloadMetaData(NodeList n){
            myFtp=null;
            myHttp=null;
            nList = n;
            Node downloadNode = nList.item(0);

            try{
                className = ((Element) downloadNode).getElementsByTagName("className").item(0).getTextContent();
                mode=((Element) downloadNode).getElementsByTagName("mode").item(0).getTextContent();
                mode=mode.toUpperCase();

                if(mode.equalsIgnoreCase("Ftp")) {
                    myFtp=new ftp(((Element)downloadNode).getElementsByTagName(mode).item(0));
                } else {
                    myHttp=new http(((Element)downloadNode).getElementsByTagName(mode).item(0));
                }
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
            hostName=((Element)e).getElementsByTagName("hostName").item(0).getTextContent();
            rootDir=((Element)e).getElementsByTagName("rootDir").item(0).getTextContent();
            userName=((Element)e).getElementsByTagName("userName").item(0).getTextContent();
            password=((Element)e).getElementsByTagName("passWord").item(0).getTextContent();
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

        public String projectionClassName;
        public Boolean projectionMozaix;
        public String convertHasConvert;
        public String convertOriFormat;
        public String convertToFormat;
        public String convertGeoTransform;
        public String convertProjectionStr;
        public String filterClassName;
        public Boolean filterRunFilter;

        public Map<Integer, String> processStep;

        public ProcessorMetaData(NodeList n){
            nList = n;
            processStep = new HashMap<Integer, String>();
            Node processNode = nList.item(0);

            try{
                Node projection = ((Element) processNode).getElementsByTagName("Projection").item(0);
                processStep.put(Integer.parseInt((projection.getAttributes().getNamedItem("processStep").getTextContent())),
                        ((Element) projection).getElementsByTagName("className").item(0).getTextContent());

                Node mozaic = ((Element) processNode).getElementsByTagName("mozaic").item(0);
                processStep.put(Integer.parseInt(mozaic.getAttributes().getNamedItem("processStep").getTextContent()),
                        mozaic.getTextContent());

                Node convertNode = ((Element) processNode).getElementsByTagName("convert").item(0);
                convertHasConvert = ((Element) convertNode).getElementsByTagName("isRunable").item(0).getTextContent();
                convertOriFormat = ((Element) convertNode).getElementsByTagName("oriFormat").item(0).getTextContent();
                convertToFormat = ((Element) convertNode).getElementsByTagName("toFormat").item(0).getTextContent();
                convertGeoTransform = ((Element) convertNode).getElementsByTagName("GeoTransform").item(0).getTextContent();
                convertProjectionStr = ((Element) convertNode).getElementsByTagName("projectionStr").item(0).getTextContent();
                processStep.put(Integer.parseInt((convertNode.getAttributes().getNamedItem("processStep").getTextContent())),
                        ((Element) convertNode).getElementsByTagName("className").item(0).getTextContent());

                Node filterNode = ((Element) processNode).getElementsByTagName("filter").item(0);
                filterClassName = ((Element) filterNode).getElementsByTagName("className").item(0).getTextContent();
                processStep.put(Integer.parseInt((filterNode.getAttributes().getNamedItem("processStep").getTextContent())),
                        ((Element) filterNode).getElementsByTagName("className").item(0).getTextContent());
            }catch(Exception e){

            }
        }
    }

    public class SummaryMetaData{
        public int daysPerInputData;
        private NodeList nList;

        public SummaryMetaData(NodeList n){
            nList = n;

            try{
                // Node: DaysPerInputData
                NodeList temporal = ((Element) nList).getElementsByTagName("Temporal");
                Node NodeCompositionStrategyClassName = ((Element) temporal).getElementsByTagName("DaysPerInputData").item(0);
                NodeList tempList = NodeCompositionStrategyClassName.getChildNodes();
                Node valueNode = tempList.item(0);
                daysPerInputData = Integer.parseInt(valueNode.getNodeValue().trim());
            }catch(Exception e){

            }
        }
    }


}

