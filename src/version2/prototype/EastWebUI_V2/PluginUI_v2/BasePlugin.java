package version2.prototype.EastWebUI_V2.PluginUI_v2;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import version2.prototype.EastWebUI_V2.DocumentBuilderInstance;
import version2.prototype.EastWebUI_V2.GlobalUIData;

public abstract class BasePlugin implements IPlugin {
    private int id = -1;
    private String pluginName;
    private String qcLevel;
    private ArrayList<String> listOfPIndicies;

    public BasePlugin(String PluginName, String QCLevel, ArrayList<String> Indicies) {
        pluginName = PluginName;
        qcLevel = QCLevel;
        listOfPIndicies = new ArrayList<String>(Indicies);
    }

    public BasePlugin (){
    }

    @Override
    public int GetId(){
        if(id == -1) {
            id = GlobalUIData.Instance().GetId();
        }

        return id;
    }

    @Override
    public String GetPluginName() {
        return pluginName;
    }

    @Override
    public void SetPluginName(String PluginName) {
        pluginName = PluginName;
    }

    @Override
    public String GetQCLevel() {
        return qcLevel;
    }

    @Override
    public void SetQCLevel(String QCLevel) {
        qcLevel = QCLevel;
    }

    @Override
    public ArrayList<String> GetIndicies() {
        return listOfPIndicies;
    }

    @Override
    public void SetIndicies(ArrayList<String> Indicies) {
        listOfPIndicies = new ArrayList<String>(Indicies);
    }

    @SuppressWarnings("unchecked")
    public <T> T GetParseObject(Node node, Class<T> clazz) {
        IPlugin plugin = null;

        try {
            plugin = (IPlugin) clazz.newInstance();
            Element element = (Element)node;

            plugin.SetPluginName(pluginName = node.getAttributes().getNamedItem("name").getTextContent());
            plugin.SetQCLevel(qcLevel = GetNodeListValuesIgnoreIfEmpty(element.getElementsByTagName("QC")).get(0));
            plugin.SetIndicies(GetNodeListValuesIgnoreIfEmpty(element.getElementsByTagName("Indicies")));
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return (T) plugin;
    }

    @Override
    public String GetUIDisplayPlugin() {
        return String.format("PluginID: %s <br>Plugin: %s;</span><br>Indices: %s</span> <br>Quality: %s;</span>",
                String.valueOf(GetId()),
                GetPluginName(),
                getIndicesFormat(),
                GetQCLevel());
    }

    @Override
    public Element GetXMLObject() throws ParserConfigurationException {
        Element plugin = DocumentBuilderInstance.Instance().GetDocument().createElement("Plugin");

        Attr attr = DocumentBuilderInstance.Instance().GetDocument().createAttribute("name");
        attr.setValue(pluginName);
        plugin.setAttributeNode(attr);

        Attr parser = DocumentBuilderInstance.Instance().GetDocument().createAttribute("parser");
        parser.setValue(this.getClass().getName());
        plugin.setAttributeNode(parser);

        if(qcLevel!=null && !qcLevel.isEmpty()){
            Element qc = DocumentBuilderInstance.Instance().GetDocument().createElement("QC");
            qc.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(qcLevel));
            plugin.appendChild(qc);
        }
        if(listOfPIndicies!=null){
            for(String i : listOfPIndicies){
                Element indicies = DocumentBuilderInstance.Instance().GetDocument().createElement("Indicies");
                indicies.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(i.toString()));
                plugin.appendChild(indicies);
            }
        }

        return plugin;
    }

    protected ArrayList<String> GetNodeListValuesIgnoreIfEmpty(NodeList nList) {
        ArrayList<String> values = new ArrayList<String>();

        if(nList != null && nList.getLength() > 0) {
            for(int i=0; i < nList.getLength(); i++) {
                Element e = (Element)nList.item(i);
                NodeList list = e.getChildNodes();
                Node n = list.item(0);

                if(n != null) {
                    n.normalize();
                    values.add(n.getNodeValue().trim());
                }
            }
        }

        return values;
    }

    private String getIndicesFormat(){
        String formatString = "";

        for(String i: GetIndicies()){
            formatString += String.format("<span>%s;</span>", i);
        }

        return formatString;
    }
}