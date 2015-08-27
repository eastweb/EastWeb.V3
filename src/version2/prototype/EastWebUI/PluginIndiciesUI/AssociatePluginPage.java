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

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;

public class AssociatePluginPage {

    public JFrame frame;
    private IndiciesEvent indiciesEvent;
    private PluginMetaDataCollection pluginMetaDataCollection;
    private JComboBox<String> pluginComboBox ;
    private JComboBox<String> indiciesComboBox;
    private JComboBox<String> qcComboBox;

    @SuppressWarnings("rawtypes")
    private DefaultListModel indiciesListModel;

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
                    ErrorLog.add(Config.getInstance(), "AssociatePluginPage.main problem with running a AssociatePluginPage window.", e);
                }
            }
        });
    }

    /**
     * Create the application.
     * @throws Exception
     */
    public AssociatePluginPage(IndiciesListener l) throws Exception {
        indiciesEvent = new IndiciesEvent();
        indiciesEvent.addListener(l);
        initialize();
        frame.setVisible(true);
    }

    /**
     * Initialize the contents of the frame.
     * @throws Exception
     */
    private void initialize() throws Exception {
        frame = new JFrame();
        frame.setBounds(100, 100, 400, 300);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        pluginMetaDataCollection = PluginMetaDataCollection.getInstance();
        pluginInformation();
    }

    /**
     * populate plugin information UI
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void pluginInformation() {
        JPanel pluginPanel = new JPanel();
        pluginPanel.setLayout(null);
        pluginPanel.setBorder(new TitledBorder(null, "Plugin Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pluginPanel.setBounds(547, 420, 383, 275);
        frame.getContentPane().add(pluginPanel);

        // list of indices to be added
        indiciesListModel = new DefaultListModel();

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 89, 365, 132);
        pluginPanel.add(scrollPane);
        final JList<DefaultListModel> listOfInndicies = new JList<DefaultListModel>(indiciesListModel);
        scrollPane.setViewportView(listOfInndicies);

        JLabel qcLabel = new JLabel("Quality Control");
        qcLabel.setBounds(10, 41, 80, 14);
        pluginPanel.add(qcLabel);
        qcComboBox = new JComboBox<String>();
        qcComboBox.setBounds(96, 38, 140, 20);
        pluginPanel.add(qcComboBox);

        JLabel indiciesLabel = new JLabel("Indices");
        indiciesLabel.setBounds(10, 66, 80, 14);
        pluginPanel.add(indiciesLabel);
        indiciesComboBox = new JComboBox<String>();
        indiciesComboBox.setBounds(96, 63, 140, 20);
        pluginPanel.add(indiciesComboBox);

        populatePluginComboBox(pluginPanel);

        // add plugin to list
        final JButton btnSave = new JButton("Save");
        btnSave.setEnabled(!indiciesListModel.isEmpty());
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String formatString = String.format("<html>Plugin: %s;<br>Indices: %s</span> <br>Quality: %s;</span></html>",
                        String.valueOf(pluginComboBox.getSelectedItem()),
                        getIndicesFormat(listOfInndicies.getModel()),
                        String.valueOf(qcComboBox.getSelectedItem()));
                indiciesEvent.fire(formatString);
                frame.dispose();
            }
        });
        btnSave.setBounds(51, 227, 89, 23);
        pluginPanel.add(btnSave);

        // cancel button
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                frame.dispose();
            }
        });
        btnCancel.setBounds(230, 227, 89, 23);
        pluginPanel.add(btnCancel);

        // add indices button
        final JButton btnAddIndices = new JButton("");
        btnAddIndices.setToolTipText("add indices ");
        btnAddIndices.setIcon(new ImageIcon(AssociatePluginPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        btnAddIndices.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {

                if(indiciesComboBox.getSelectedItem() == null) {
                    return ;
                }

                indiciesListModel.addElement(String.valueOf(indiciesComboBox.getSelectedItem()));
                indiciesComboBox.removeItem(indiciesComboBox.getSelectedItem());
                btnSave.setEnabled(!indiciesListModel.isEmpty());
            }
        });
        btnAddIndices.setBounds(283, 62, 36, 23);
        pluginPanel.add(btnAddIndices);

        // delete selected indices
        JButton btnDeleteIndicies = new JButton("");
        btnDeleteIndicies.setToolTipText("delete selected indices");
        btnDeleteIndicies.setIcon(new ImageIcon(AssociatePluginPage.class.getResource("/version2/prototype/Images/ChangeQueryType_deletequery_274.png")));
        btnDeleteIndicies.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                indiciesComboBox.addItem(indiciesListModel.getElementAt(listOfInndicies.getSelectedIndex()).toString());

                DefaultListModel<DefaultListModel> model = (DefaultListModel<DefaultListModel>) listOfInndicies.getModel();
                model.getElementAt(listOfInndicies.getSelectedIndex());
                int selectedIndex = listOfInndicies.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
                btnSave.setEnabled(!indiciesListModel.isEmpty());
            }
        });
        btnDeleteIndicies.setBounds(339, 62, 36, 23);
        pluginPanel.add(btnDeleteIndicies);
    }

    /**
     * populate all plugin base on the meta data
     * @param pluginPanel
     */
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
                for(String indicies : plugin.Indices.indicesNames) {
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

    /**
     * format indices to show in UI
     * @param m
     * @return
     */
    @SuppressWarnings("rawtypes")
    private String getIndicesFormat(ListModel m){
        String formatString = "";
        ListModel model = m;

        for(int i=0; i < model.getSize(); i++){
            formatString += String.format("<span>%s;</span>",   model.getElementAt(i).toString());
        }

        return formatString;
    }
}
