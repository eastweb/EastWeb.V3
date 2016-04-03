package EastWeb_UserInterface.PluginWindow.PluginExtension.Modis;

import java.awt.Font;
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
import org.w3c.dom.NodeList;

import EastWeb_UserInterface.DocumentBuilderInstance;
import EastWeb_UserInterface.GlobalUIData;
import EastWeb_UserInterface.PluginWindow.BasePlugin;
import EastWeb_UserInterface.PluginWindow.IPlugin;

public class ModisPlugin extends BasePlugin implements IPlugin {
    private JButton addNewModisButton;
    private JButton deleteSelectedModisButton;
    private JLabel lblModisTiles;
    private JScrollPane scrollPane;

    private DefaultListModel<String> modisListModel;

    // constructor for parsing xml
    public ModisPlugin(String PluginName, String QCLevel, ArrayList<String> Indicies, ArrayList<String> ModisList) {
        super(PluginName, QCLevel, Indicies);
        GlobalUIData.Instance().AddModisListner(new modisListenerImplementation());
    }

    // constructor for UI
    public ModisPlugin() {
        GlobalUIData.Instance().AddModisListner(new modisListenerImplementation());
    }

    public ArrayList<String> GetModisList(){
        ArrayList<String> modisList = new ArrayList<String>();

        for(int i = 0; i < modisListModel.toArray().length; i ++){
            modisList.add(modisListModel.toArray()[i].toString());
        }

        return modisList;
    }

    public void SetModisList(ArrayList<String> ModisList){
        if(modisListModel == null) {
            modisListModel = new DefaultListModel<String>();
        }else{
            modisListModel.clear();
        }

        for(String p: ModisList) {
            modisListModel.addElement(p);
        }
    }

    @Override
    public Element GetXMLObject() throws ParserConfigurationException {
        Element p  = super.GetXMLObject();

        if(modisListModel != null){
            for(String m : GetModisList()){
                Element modisTile = DocumentBuilderInstance.Instance().GetDocument().createElement("ModisTiles");
                modisTile.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(m));
                p.appendChild(modisTile);
            }
        }

        return p;
    }

    @Override
    public String GetUIDisplayPlugin(){
        String formatString = "Modis Tiles: ";

        for(Object tile : GetModisList()) {
            formatString += tile.toString() + "; ";
        }

        return String.format("<html>%s<br>%s</span></html>", super.GetUIDisplayPlugin(), formatString);
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

    @Override
    public IPlugin GetParseObject(NodeList nodeList, int itemNumber) {
        ModisPlugin parsePlugin = super.GetParseObject(nodeList.item(itemNumber), ModisPlugin.class);
        parsePlugin.SetModisList(GetNodeListValuesIgnoreIfEmpty(((Element)nodeList.item(itemNumber))
                .getElementsByTagName("ModisTiles")));

        return parsePlugin;
    }

    @Override
    public JPanel SetupUI(JPanel modisInformationPanel, final JFrame frame) {
        lblModisTiles = new JLabel("Modis Tiles");
        lblModisTiles.setFont(new Font("Courier", Font.BOLD,15));
        lblModisTiles.setBounds(415, 41, 100, 14);
        modisInformationPanel.add(lblModisTiles);
        modisInformationPanel.setLayout(null);
        modisInformationPanel.setBounds(359, 420, 275, 390);

        modisListModel = new DefaultListModel<String>();
        addNewModisButton = new JButton("");
        addNewModisButton.setOpaque(false);
        addNewModisButton.setContentAreaFilled(false);
        addNewModisButton.setBorderPainted(false);
        addNewModisButton.setIcon(new ImageIcon(this.getClass().getResource("/Images/action_add_16xLG.png")));
        addNewModisButton.setToolTipText("Add modis");
        addNewModisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {addModisTile(frame);}
        });
        addNewModisButton.setBounds(410, 352, 36, 23);
        modisInformationPanel.add(addNewModisButton);

        scrollPane = new JScrollPane();
        scrollPane.setBounds(342, 101, 245, 240);
        modisInformationPanel.add(scrollPane);

        final JList<String> modisList = new JList<String>(modisListModel);
        scrollPane.setViewportView(modisList);

        deleteSelectedModisButton = new JButton("");
        deleteSelectedModisButton.setOpaque(false);
        deleteSelectedModisButton.setContentAreaFilled(false);
        deleteSelectedModisButton.setBorderPainted(false);
        deleteSelectedModisButton.setIcon(new ImageIcon(this.getClass().getResource("/Images/trashCan.png")));
        deleteSelectedModisButton.setToolTipText("Delete Selected Modis");
        deleteSelectedModisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {removeSelectedModis(modisList);}
        });
        deleteSelectedModisButton.setBounds(490, 352, 36, 23);
        modisInformationPanel.add(deleteSelectedModisButton);

        for(String tiles : GlobalUIData.Instance().GetModisTiles()) {
            modisListModel.addElement(tiles);
        }

        return modisInformationPanel;
    }

    private void addModisTile(final JFrame frame) {
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

    private void removeSelectedModis(final JList<String> modisList) {
        DefaultListModel<String> model = (DefaultListModel<String>) modisList.getModel();
        int selectedIndex = modisList.getSelectedIndex();

        if (selectedIndex != -1) {
            model.remove(selectedIndex);
        }
    }

    class modisListenerImplementation implements ModisListener{
        @Override
        public void AddPlugin(ModisEventObject e) {
            SetModisList(GlobalUIData.Instance().GetModisTiles());
        }
    }
}