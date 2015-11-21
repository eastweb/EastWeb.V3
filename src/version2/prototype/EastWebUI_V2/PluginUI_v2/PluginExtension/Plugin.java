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
        // TODO Auto-generated constructor stub
    }

    public Plugin()
    {

    }

    @Override
    public String GetUIDisplayPlugin()
    {
        String s = String.format("<html>%s</html>",super.GetUIDisplayPlugin());
        return s;
    }

    @Override
    public JPanel SetupUI(JPanel Panel, JFrame frame) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void ClearUI(JPanel Panel) {
        // TODO Auto-generated method stub

    }

    @Override
    public void Save() {
        // TODO Auto-generated method stub
    }

    @Override
    public IPlugin GetParseObject(NodeList nodeList, int itemNumber) {
        Plugin plugin = super.GetParseObject(nodeList.item(itemNumber), Plugin.class);
        return plugin;
    }
}
