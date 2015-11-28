package version2.prototype.EastWebUI_V2.PluginUI_v2;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.JButton;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.EastWebUI_V2.PluginUI_v2.PluginExtension.Plugin;
import version2.prototype.PluginMetaData.PluginMetaDataCollection;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.PluginMetaData;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;


public class AssociatePluginPage {
    private JFrame frame;
    private PluginEvent indiciesEvent;
    private PluginMetaDataCollection pluginMetaDataCollection;
    private JComboBox<String> pluginComboBox ;
    private JComboBox<String> indiciesComboBox;
    private JComboBox<String> qcComboBox;
    private IPlugin newPlugin;

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
                    new AssociatePluginPage(null);
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
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

        indiciesEvent = new PluginEvent();
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
        //frame.setBounds(100, 100, 603, 400);
        frame.setBounds(100, 100, 345, 415);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel pluginPanel = new JPanel();
        pluginMetaDataCollection = PluginMetaDataCollection.getInstance();
        pluginInformation(pluginPanel);
    }

    /**
     * populate plugin information UI
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void pluginInformation(final JPanel pluginPanel) {
        indiciesListModel = new DefaultListModel();

        pluginPanel.setLayout(null);
        pluginPanel.setBorder(new TitledBorder(null, "Plugin Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pluginPanel.setBounds(547, 420, 383, 275);
        frame.getContentPane().add(pluginPanel);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 101, 318, 240);
        pluginPanel.add(scrollPane);
        final JList<DefaultListModel> listOfInndicies = new JList<DefaultListModel>(indiciesListModel);
        scrollPane.setViewportView(listOfInndicies);

        JLabel qcLabel = new JLabel("Quality Control");
        qcLabel.setBounds(10, 53, 80, 14);
        pluginPanel.add(qcLabel);
        qcComboBox = new JComboBox<String>();
        qcComboBox.setBounds(96, 50, 140, 20);
        pluginPanel.add(qcComboBox);

        JLabel indiciesLabel = new JLabel("Indices");
        indiciesLabel.setBounds(10, 78, 80, 14);
        pluginPanel.add(indiciesLabel);
        indiciesComboBox = new JComboBox<String>();
        indiciesComboBox.setBounds(96, 75, 140, 20);
        pluginPanel.add(indiciesComboBox);

        JLabel pluginLabel = new JLabel("Plugin");
        pluginLabel.setBounds(10, 28, 80, 14);
        pluginPanel.add(pluginLabel);
        pluginComboBox = new JComboBox<String>();
        pluginComboBox.setBounds(96, 25, 140, 20);
        pluginComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) { populatePluginInfo(pluginPanel); }
        });
        for(String plugin: pluginMetaDataCollection.pluginList){
            pluginComboBox.addItem(plugin);
        }
        pluginPanel.add(pluginComboBox);

        // add plugin to list
        final JButton btnSave = new JButton("Save");
        btnSave.setEnabled(!indiciesListModel.isEmpty());
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {savePlugin(listOfInndicies);}
        });
        btnSave.setBounds(10, 352, 89, 23);
        pluginPanel.add(btnSave);

        // cancel button
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {frame.dispose();}
        });
        btnCancel.setBounds(239, 352, 89, 23);
        pluginPanel.add(btnCancel);

        // add indices button
        final JButton btnAddIndices = new JButton("");
        btnAddIndices.setToolTipText("add indices ");
        btnAddIndices.setIcon(new ImageIcon(AssociatePluginPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        btnAddIndices.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) { addIndices(btnSave);}
        });
        btnAddIndices.setBounds(246, 67, 36, 23);
        pluginPanel.add(btnAddIndices);

        // delete selected indices
        JButton btnDeleteIndicies = new JButton("");
        btnDeleteIndicies.setToolTipText("delete selected indices");
        btnDeleteIndicies.setIcon(new ImageIcon(AssociatePluginPage.class.getResource("/version2/prototype/Images/trashCan.png")));
        btnDeleteIndicies.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {deleteSelectedIndices(listOfInndicies, btnSave);}
        });
        btnDeleteIndicies.setBounds(292, 67, 36, 23);
        pluginPanel.add(btnDeleteIndicies);
    }

    /**
     * format indices to show in UI
     * @param m
     * @return
     */
    @SuppressWarnings("rawtypes")
    private ArrayList<String> convertModelToArray(ListModel model){
        ArrayList<String> indicies = new ArrayList<String>();

        for(int i=0; i < model.getSize(); i++){
            indicies.add(model.getElementAt(i).toString());
        }

        return indicies;
    }

    private void savePlugin(@SuppressWarnings("rawtypes") final JList<DefaultListModel> listOfInndicies) {
        newPlugin.SetQCLevel(String.valueOf(qcComboBox.getSelectedItem()));
        newPlugin.SetIndicies(convertModelToArray(listOfInndicies.getModel()));
        newPlugin.SetPluginName(String.valueOf(pluginComboBox.getSelectedItem()));
        newPlugin.Save();

        indiciesEvent.fire(newPlugin);
        frame.dispose();
    }

    private void populatePluginInfo(final JPanel pluginPanel) {
        indiciesListModel.removeAllElements();
        PluginMetaData plugin = pluginMetaDataCollection.pluginMetaDataMap.get(String.valueOf(pluginComboBox.getSelectedItem()));
        pluginPanel.revalidate();

        if(newPlugin != null) {
            newPlugin.ClearUI(pluginPanel);
        }

        Class<?> pluginUI;
        try {
            pluginUI = Class.forName(String.format("version2.prototype.EastWebUI_V2.PluginUI_v2.PluginExtension.%sPluginUI", plugin.Title));
            Constructor<?> pluginUIInstance = pluginUI.getConstructor();

            newPlugin = (IPlugin) pluginUIInstance.newInstance();
            newPlugin.SetupUI(pluginPanel, frame);
            frame.setBounds(100, 100, 603, 415);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            newPlugin = new Plugin();
            frame.setBounds(100, 100, 347, 415);
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

    @SuppressWarnings("unchecked")
    private void addIndices(final JButton btnSave) {
        if(indiciesComboBox.getSelectedItem() == null) {
            return ;
        }

        indiciesListModel.addElement(String.valueOf(indiciesComboBox.getSelectedItem()));
        indiciesComboBox.removeItem(indiciesComboBox.getSelectedItem());
        btnSave.setEnabled(!indiciesListModel.isEmpty());
    }

    @SuppressWarnings("rawtypes")
    private void deleteSelectedIndices(final JList<DefaultListModel> listOfInndicies, final JButton btnSave) {
        indiciesComboBox.addItem(indiciesListModel.getElementAt(listOfInndicies.getSelectedIndex()).toString());

        DefaultListModel<DefaultListModel> model = (DefaultListModel<DefaultListModel>) listOfInndicies.getModel();
        int selectedIndex = listOfInndicies.getSelectedIndex();

        if (selectedIndex != -1) {
            model.remove(selectedIndex);
        }
        btnSave.setEnabled(!indiciesListModel.isEmpty());
    }
}