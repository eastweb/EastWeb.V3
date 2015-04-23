package version2.prototype.EastWebUI.PluginIndiciesUI;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.JButton;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.ImageIcon;

public class AssociatePluginPage {

    public JFrame frame;
    IndiciesEvent indiciesEvent;
    PluginMetaDataCollection pluginMetaDataCollection;
    JComboBox<String> pluginComboBox ;
    JComboBox<String> indiciesComboBox;
    JComboBox<String> qcComboBox;

    @SuppressWarnings("rawtypes")
    DefaultListModel indiciesListModel;

    /**
     * Launch application for debug.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    AssociatePluginPage window = new AssociatePluginPage(null);
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public AssociatePluginPage(IndiciesListener l) throws ParserConfigurationException, SAXException, IOException {
        indiciesEvent = new IndiciesEvent();
        indiciesEvent.addListener(l);
        initialize();
        frame.setVisible(true);
    }

    /**
     * Initialize the contents of the frame.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private void initialize() throws ParserConfigurationException, SAXException, IOException {
        frame = new JFrame();
        frame.setBounds(100, 100, 400, 300);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        pluginMetaDataCollection = PluginMetaDataCollection.getInstance();
        pluginInformation();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void pluginInformation() {
        JPanel pluginPanel = new JPanel();
        pluginPanel.setLayout(null);
        pluginPanel.setBorder(new TitledBorder(null, "Plugin Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pluginPanel.setBounds(547, 420, 383, 275);
        frame.getContentPane().add(pluginPanel);

        indiciesListModel = new DefaultListModel();
        final JList<DefaultListModel> listOfInndicies = new JList<DefaultListModel>(indiciesListModel);
        listOfInndicies.setBounds(10, 89, 365, 132);
        pluginPanel.add(listOfInndicies);

        JLabel qcLabel = new JLabel("Quality Control");
        qcLabel.setBounds(10, 41, 80, 14);
        pluginPanel.add(qcLabel);
        qcComboBox = new JComboBox<String>();
        qcComboBox.setBounds(96, 38, 140, 20);
        pluginPanel.add(qcComboBox);

        JLabel indiciesLabel = new JLabel("Indicies");
        indiciesLabel.setBounds(10, 66, 80, 14);
        pluginPanel.add(indiciesLabel);
        indiciesComboBox = new JComboBox<String>();
        indiciesComboBox.setBounds(96, 63, 140, 20);
        pluginPanel.add(indiciesComboBox);

        populatePluginComboBox(pluginPanel);

        final JButton btnAddIndicies = new JButton("");
        btnAddIndicies.setToolTipText("add indicies");
        btnAddIndicies.setIcon(new ImageIcon(AssociatePluginPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        btnAddIndicies.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                indiciesListModel.addElement(String.valueOf(indiciesComboBox.getSelectedItem()));
            }
        });
        btnAddIndicies.setBounds(283, 62, 36, 23);
        pluginPanel.add(btnAddIndicies);

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String formatString = String.format("<html>Plugin: %s;<br>Indicies: %s</span> <br>Quality: %s;</span></html>",
                        String.valueOf(pluginComboBox.getSelectedItem()),
                        getIndiciesFormat(listOfInndicies.getModel()),
                        String.valueOf(qcComboBox.getSelectedItem()));
                indiciesEvent.fire(formatString);
                frame.dispose();
            }
        });
        btnSave.setBounds(51, 227, 89, 23);
        pluginPanel.add(btnSave);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                frame.dispose();
            }
        });
        btnCancel.setBounds(230, 227, 89, 23);
        pluginPanel.add(btnCancel);

        JButton btnDeleteIndicies = new JButton("");
        btnDeleteIndicies.setToolTipText("delete selected indicies");
        btnDeleteIndicies.setIcon(new ImageIcon(AssociatePluginPage.class.getResource("/version2/prototype/Images/ChangeQueryType_deletequery_274.png")));
        btnDeleteIndicies.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel<DefaultListModel> model = (DefaultListModel<DefaultListModel>) listOfInndicies.getModel();
                int selectedIndex = listOfInndicies.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
            }
        });
        btnDeleteIndicies.setBounds(339, 62, 36, 23);
        pluginPanel.add(btnDeleteIndicies);
    }

    private void populatePluginComboBox(JPanel pluginPanel) {
        JLabel pluginLabel = new JLabel("Plugin");
        pluginLabel.setBounds(10, 16, 80, 14);
        pluginPanel.add(pluginLabel);
        pluginComboBox = new JComboBox<String>();
        pluginComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                indiciesListModel.removeAllElements();
                PluginMetaData plugin = pluginMetaDataCollection.pluginMetaDataMap.get(String.valueOf(pluginComboBox.getSelectedItem()));

                indiciesComboBox.removeAllItems();
                for(String indicies:plugin.IndicesMetaData) {
                    indiciesComboBox.addItem(indicies);
                }

                qcComboBox.removeAllItems();
                for(String qc:plugin.QualityControlMetaData) {
                    qcComboBox.addItem(qc);
                }
            }
        });
        pluginComboBox.setBounds(96, 13, 140, 20);

        for(String plugin: pluginMetaDataCollection.pluginList){
            pluginComboBox.addItem(plugin);
        }
        pluginPanel.add(pluginComboBox);
    }

    @SuppressWarnings("rawtypes")
    private String getIndiciesFormat(ListModel m){
        String formatString = "";

        ListModel model = m;

        for(int i=0; i < model.getSize(); i++){
            formatString += String.format("<span>%s;</span>",   model.getElementAt(i).toString());
        }

        return formatString;
    }
}
