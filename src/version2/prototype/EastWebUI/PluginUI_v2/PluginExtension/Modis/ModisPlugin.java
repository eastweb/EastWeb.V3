package version2.prototype.EastWebUI.PluginUI_v2.PluginExtension.Modis;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

import version2.prototype.ModisTile;
import version2.prototype.EastWebUI.DocumentBuilderInstance;
import version2.prototype.EastWebUI.GlobalUIData;
import version2.prototype.EastWebUI.PluginUI_v2.BasePlugin;
import version2.prototype.EastWebUI.PluginUI_v2.IPlugin;
import version2.prototype.EastWebUI.PluginUI_v2.PluginExtension.Modis.ModisEventObject;
import version2.prototype.EastWebUI.PluginUI_v2.PluginExtension.Modis.ModisListener;
import version2.prototype.EastWebUI.ProjectInformationUI.ProjectInformationPage;

public class ModisPlugin extends BasePlugin implements IPlugin {
    // constructor for parsing xml
    public ModisPlugin(String PluginName, String QCLevel, ArrayList<String> Indicies, ArrayList<String> ModisList) {
        super(PluginName, QCLevel, Indicies);

        GlobalUIData.Instance().AddModisListner(new modisListenerImplementation());
    }

    // constructor for UI
    public ModisPlugin() {

        GlobalUIData.Instance().AddModisListner(new modisListenerImplementation());
    }

    public ArrayList<String> GetModisList()
    {
        ArrayList<String> modisList = new ArrayList<String>();
        for(int i = 0; i < modisListModel.toArray().length; i ++)
        {
            modisList.add(modisListModel.toArray()[i].toString());
        }
        return modisList;
    }

    public void SetModisList(ArrayList<String> ModisList)
    {
        modisListModel.clear();
        for(String p: ModisList) {
            modisListModel.addElement(p);
        }
    }

    @Override
    public Element GetXMLObject() throws ParserConfigurationException {

        Element p  = super.GetXMLObject();

        if(modisListModel != null)
        {
            for(String m : GetModisList()) {
                Element modisTile = DocumentBuilderInstance.Instance().GetDocument().createElement("ModisTiles");
                modisTile.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(m));
                p.appendChild(modisTile);
            }
        }

        return p;
    }

    @Override
    public String GetUIDisplayPlugin()
    {
        String formatString = "Modis Tiles: ";

        for(Object tile : GetModisList()) {
            formatString += tile.toString() + "; ";
        }
        String s = String.format("<html>%s<br>%s</span></html>",super.GetUIDisplayPlugin(), formatString);
        return s;
    }

    @Override
    public void ClearUI(JPanel Panel) {
        Panel.remove(deleteSelectedModisButton);
        Panel.remove(addNewModisButton);
        Panel.remove(scrollPane);
        Panel.remove(lblModisTiles);
    }

    @Override
    public void Save() {
        GlobalUIData.Instance().SetModisTiles(GetModisList());
    }

    private JButton addNewModisButton;
    private JButton deleteSelectedModisButton;
    private DefaultListModel<String> modisListModel;
    private JLabel lblModisTiles;
    private JScrollPane scrollPane;

    @Override
    public JPanel SetupUI(JPanel modisInformationPanel, final JFrame frame) {
        lblModisTiles = new JLabel("Modis Tiles");
        lblModisTiles.setBounds(435, 41, 80, 14);
        modisInformationPanel.add(lblModisTiles);
        modisInformationPanel.setLayout(null);
        modisInformationPanel.setBounds(359, 420, 275, 390);

        modisListModel = new DefaultListModel<String>();
        addNewModisButton = new JButton("");
        addNewModisButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        addNewModisButton.setToolTipText("Add modis");
        addNewModisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String tile = JOptionPane.showInputDialog(frame,"Enter Modis Tile", null);

                if(tile.toUpperCase().charAt(0) != 'H' || tile.toUpperCase().charAt(3) != 'V' || tile.length() > 6) {
                    JOptionPane.showMessageDialog(null, "Modis format: hddvdd  d=> digit");
                    return;
                } else{
                    int horizontal = Integer.parseInt(String.format("%s%s", tile.toUpperCase().charAt(1), tile.toUpperCase().charAt(2)));
                    int vertical = Integer.parseInt(String.format("%c%c", tile.toUpperCase().charAt(4), tile.toUpperCase().charAt(5)));

                    if(horizontal < ModisTile.HORZ_MIN || horizontal > ModisTile.HORZ_MAX || vertical < ModisTile.VERT_MIN || vertical > ModisTile.VERT_MAX){
                        JOptionPane.showMessageDialog(null, String.format("Horizontal has be to within %d-%d and Vertical has to be within %d-%d",
                                ModisTile.HORZ_MIN , ModisTile.HORZ_MAX , ModisTile.VERT_MIN, ModisTile.VERT_MAX ));
                        return;
                    }
                }

                for(Object item:modisListModel.toArray()){
                    if(tile.contentEquals(item.toString())) {
                        JOptionPane.showMessageDialog(null, "Modis tile already exist");
                        return;
                    }
                }

                modisListModel.addElement(tile);
            }
        });
        addNewModisButton.setBounds(438, 340, 25, 20);
        modisInformationPanel.add(addNewModisButton);

        scrollPane = new JScrollPane();
        scrollPane.setBounds(342, 89, 245, 240);
        modisInformationPanel.add(scrollPane);

        final JList<String> modisList = new JList<String>(modisListModel);
        scrollPane.setViewportView(modisList);

        deleteSelectedModisButton = new JButton("");
        deleteSelectedModisButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/ChangeQueryType_deletequery_274.png")));
        deleteSelectedModisButton.setToolTipText("Delete Selected Modis");
        deleteSelectedModisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel<String> model = (DefaultListModel<String>) modisList.getModel();
                int selectedIndex = modisList.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
            }
        });
        deleteSelectedModisButton.setBounds(490, 340, 25, 20);
        modisInformationPanel.add(deleteSelectedModisButton);

        for(String tiles : GlobalUIData.Instance().GetModisTiles()) {
            modisListModel.addElement(tiles);
        }

        return modisInformationPanel;
    }

    class modisListenerImplementation implements ModisListener{
        @Override
        public void AddPlugin(ModisEventObject e) {
            SetModisList(GlobalUIData.Instance().GetModisTiles());
        }
    }

}
