package version2.prototype.EastWebUI_V2.PluginUI_v2.PluginExtension;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.w3c.dom.NodeList;

import version2.prototype.EastWebUI_V2.PluginUI_v2.BasePlugin;
import version2.prototype.EastWebUI_V2.PluginUI_v2.IPlugin;

public class Plugin extends BasePlugin{

    public Plugin(String PluginName, String QCLevel, ArrayList<String> Indicies) {
        super(PluginName, QCLevel, Indicies);
    }

    public Plugin(){
    }

    @Override
    public String GetUIDisplayPlugin(){
        return  String.format("<html>%s</html>",super.GetUIDisplayPlugin());
    }

    @Override
    public JPanel SetupUI(JPanel Panel, JFrame frame) {
        return null;
    }

    @Override
    public void ClearUI(JPanel Panel) {
    }

    @Override
    public void Save() {
    }

    @Override
    public IPlugin GetParseObject(NodeList nodeList, int itemNumber) {
        return super.GetParseObject(nodeList.item(itemNumber), Plugin.class);
    }
}