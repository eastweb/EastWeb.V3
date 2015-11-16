package version2.prototype.EastWebUI.PluginUI_v2.PluginExtension;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import version2.prototype.EastWebUI.PluginUI_v2.BasePlugin;

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
}
