package version2.prototype.EastWebUI.PluginIndiciesUI;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.JButton;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.ModisTile;
import version2.prototype.EastWebUI.ProjectInformationUI.ProjectInformationPage;
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
    private JButton addNewModisButton;
    private JButton deleteSelectedModisButton;

    private DefaultListModel<String> modisListModel;

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
        frame.setBounds(100, 100, 345, 400);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel pluginPanel = new JPanel();
        pluginMetaDataCollection = PluginMetaDataCollection.getInstance();
        pluginInformation(pluginPanel);
        ModisInformation(pluginPanel);

        JLabel lblModisTiles = new JLabel("Modis Tiles");
        lblModisTiles.setBounds(435, 41, 80, 14);
        pluginPanel.add(lblModisTiles);
    }

    /**
     * populate plugin information UI
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void pluginInformation(JPanel pluginPanel) {

        pluginPanel.setLayout(null);
        pluginPanel.setBorder(new TitledBorder(null, "Plugin Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pluginPanel.setBounds(547, 420, 383, 275);
        frame.getContentPane().add(pluginPanel);

        // list of indices to be added
        indiciesListModel = new DefaultListModel();

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 89, 318, 237);
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
                String formatString = String.format("<html>Plugin: %s;<br>Indices: %s</span> <br>%s</span> <br>Quality: %s;</span></html>",
                        String.valueOf(pluginComboBox.getSelectedItem()),
                        getIndicesFormat(listOfInndicies.getModel()),
                        getModisTilesFormat(modisListModel),
                        String.valueOf(qcComboBox.getSelectedItem())
                        );

                indiciesEvent.fire(formatString);
                frame.dispose();
            }


        });
        btnSave.setBounds(10, 340, 89, 23);
        pluginPanel.add(btnSave);

        // cancel button
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                frame.dispose();
            }
        });
        btnCancel.setBounds(239, 340, 89, 23);
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
        btnAddIndices.setBounds(246, 55, 36, 23);
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
        btnDeleteIndicies.setBounds(292, 55, 36, 23);
        pluginPanel.add(btnDeleteIndicies);
    }

    private String getModisTilesFormat(DefaultListModel<String> modisListModel) {
        if(modisListModel.isEmpty()) {
            return "";
        } else
        {
            String formatString = "Modis Tiles: ";

            for(Object tile : modisListModel.toArray()) {
                formatString += tile.toString() + "; ";
            }

            return formatString;
        }
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

                if(plugin.Title.toUpperCase().contains("MODIS"))
                {
                    frame.setBounds(100, 100, 603, 400);
                }
                else
                {
                    frame.setBounds(100, 100, 347, 400);
                }

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

    @SuppressWarnings("rawtypes")
    private void ModisInformation(JPanel modisInformationPanel) {

        modisInformationPanel.setLayout(null);
        modisInformationPanel.setBounds(359, 420, 275, 390);
        frame.getContentPane().add(modisInformationPanel);

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

        JScrollPane scrollPane = new JScrollPane();
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
                DefaultListModel model = (DefaultListModel) modisList.getModel();
                int selectedIndex = modisList.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
            }
        });
        deleteSelectedModisButton.setBounds(490, 340, 25, 20);
        modisInformationPanel.add(deleteSelectedModisButton);
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
