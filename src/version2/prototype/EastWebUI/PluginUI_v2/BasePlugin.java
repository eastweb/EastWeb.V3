package version2.prototype.EastWebUI.PluginUI_v2;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import version2.prototype.EastWebUI.DocumentBuilderInstance;
import version2.prototype.EastWebUI.GlobalUIData;
import version2.prototype.EastWebUI.PluginUI_v2.PluginExtension.Plugin;

public abstract class BasePlugin implements IPlugin {

    public BasePlugin (){};

    public BasePlugin(String PluginName, String QCLevel, ArrayList<String> Indicies)
    {
        pluginName = PluginName;
        qcLevel = QCLevel;
        listOfPIndicies = new ArrayList<String>(Indicies);
    }

    private int id = -1;
    private String pluginName;
    private String qcLevel;
    private ArrayList<String> listOfPIndicies;
    private Element plugin;

    @Override
    public int GetId()
    {
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

    @Override
    public IPlugin GetParseObject(IPlugin plugin) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPlugin ParsePlugin(File xmlFiles){
        return new Plugin();
    }

    @Override
    public String GetUIDisplayPlugin()
    {
        String formatString = String.format("PluginID: %s <br>Plugin: %s;</span><br>Indices: %s</span> <br>Quality: %s;</span>",
                String.valueOf(GetId()),
                GetPluginName(),
                getIndicesFormat(),
                GetQCLevel());

        return formatString;
    }

    private String getIndicesFormat(){
        String formatString = "";

        for(String i: GetIndicies())
        {
            formatString += String.format("<span>%s;</span>", i);
        }
        return formatString;
    }

    @Override
    public Element GetXMLObject() throws ParserConfigurationException {
        plugin = DocumentBuilderInstance.Instance().GetDocument().createElement("Plugin");

        // set attribute to staff element
        Attr attr = DocumentBuilderInstance.Instance().GetDocument().createAttribute("name");
        attr.setValue(pluginName);
        plugin.setAttributeNode(attr);

        // set attribute to staff element
        Attr parser = DocumentBuilderInstance.Instance().GetDocument().createAttribute("parser");
        parser.setValue(this.getClass().getName());
        plugin.setAttributeNode(parser);

        if(qcLevel!=null && !qcLevel.isEmpty()){
            // start Date
            Element qc = DocumentBuilderInstance.Instance().GetDocument().createElement("QC");
            qc.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(qcLevel));
            plugin.appendChild(qc);
        }
        if(listOfPIndicies!=null)
        {
            for(String i : listOfPIndicies)
            {
                Element indicies = DocumentBuilderInstance.Instance().GetDocument().createElement("Indicies");
                indicies.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(i.toString()));
                plugin.appendChild(indicies);
            }
        }

        return plugin;
    }

    @Override
    public abstract JPanel SetupUI(JPanel Panel, JFrame frame);

    @Override
    public abstract void ClearUI(JPanel Panel);
}
