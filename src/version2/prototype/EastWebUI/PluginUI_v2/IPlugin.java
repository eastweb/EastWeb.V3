package version2.prototype.EastWebUI.PluginUI_v2;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

public interface IPlugin {
    public int GetId();
    public String GetPluginName();
    public void SetPluginName(String PluginName);
    public String GetQCLevel();
    public void SetQCLevel(String QCLevel);
    public ArrayList<String> GetIndicies();
    public void SetIndicies(ArrayList<String> Indicies);
    public Element GetXMLObject() throws ParserConfigurationException;
    public JPanel SetupUI(JPanel Panel, JFrame frame);
    public void ClearUI(JPanel Panel);
    public IPlugin GetParseObject(IPlugin plugin);
    public void Save();
    public String GetUIDisplayPlugin();
    public IPlugin ParsePlugin(File xmlFiles);

}
