package version2.prototype.EastWebUI;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JButton;

import java.awt.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

public class tempUI {

    private JFrame frame;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    tempUI window = new tempUI();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public tempUI() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 401, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pluginInformation();
    }

    private void pluginInformation() {
        JPanel pluginPanel = new JPanel();
        pluginPanel.setLayout(null);
        pluginPanel.setBorder(new TitledBorder(null, "Plugin Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        pluginPanel.setBounds(547, 420, 383, 275);
        frame.getContentPane().add(pluginPanel);

        final DefaultListModel indiciesListModel = new DefaultListModel();
        @SuppressWarnings("unchecked")
        final JList<DefaultListModel> listOfInndicies = new JList<DefaultListModel>(indiciesListModel);
        listOfInndicies.setBounds(10, 89, 365, 132);
        pluginPanel.add(listOfInndicies);

        JLabel pluginLabel = new JLabel("Plugin");
        pluginLabel.setBounds(10, 16, 80, 14);
        pluginPanel.add(pluginLabel);
        JComboBox<String> pluginComboBox = new JComboBox<String>();
        pluginComboBox.setBounds(96, 13, 140, 20);
        pluginComboBox.addItem("NLDAS Noah");
        pluginComboBox.addItem("NLDAS Forcing");
        pluginComboBox.addItem("Modis Reflectance");
        pluginPanel.add(pluginComboBox);

        JLabel qcLabel = new JLabel("Quality Control");
        qcLabel.setBounds(10, 41, 80, 14);
        pluginPanel.add(qcLabel);
        JComboBox<String> qcComboBox = new JComboBox<String>();
        qcComboBox.setBounds(96, 38, 140, 20);
        qcComboBox.addItem("level 1");
        qcComboBox.addItem("level 2");
        qcComboBox.addItem("none");
        pluginPanel.add(qcComboBox);

        JLabel indiciesLabel = new JLabel("Indicies");
        indiciesLabel.setBounds(10, 66, 80, 14);
        pluginPanel.add(indiciesLabel);
        final JComboBox<String> indiciesComboBox = new JComboBox<String>();
        indiciesComboBox.setBounds(96, 63, 140, 20);
        indiciesComboBox.addItem("Mean Daily Snow Depth");
        indiciesComboBox.addItem("Mean Daily Snow Depth 258");
        indiciesComboBox.addItem("Mean Daily Snow Depth 89");
        pluginPanel.add(indiciesComboBox);

        final JButton btnAddIndicies = new JButton("Add Indicies");
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
                // fire event
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        btnSave.setBounds(51, 227, 89, 23);
        pluginPanel.add(btnSave);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        btnCancel.setBounds(230, 227, 89, 23);
        pluginPanel.add(btnCancel);

        JButton btnDeleteIndicies = new JButton("Delete Indicies");
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
}
