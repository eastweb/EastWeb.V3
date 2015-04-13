package version2.prototype.EastWebUI;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JComboBox;

import version2.prototype.EastWebUI.MainWindow.ButtonEditor;
import version2.prototype.EastWebUI.MainWindow.ButtonRenderer;

public class ProjectInformationPage {

    private JFrame frame;
    final int windowHeight = 1000;
    final int windowWidth = 750;
    private JTextField startDate;
    private JTextField projectName;
    private JTextField workingDirectory;
    private JTextField maskFile;
    private JTextField pixelSize;
    private JTextField standardParallel1;
    private JTextField centalMeridian;
    private JTextField falseEasting;
    private JTextField standardParallel2;
    private JTextField latitudeOfOrigin;
    private JTextField falseNothing;

    @SuppressWarnings("rawtypes")
    DefaultListModel modisListModel;

    @SuppressWarnings("rawtypes")
    DefaultListModel summaryListModel;

    static ProjectInformationPage window;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    window =  new ProjectInformationPage();
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
    public ProjectInformationPage() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 1207, 730);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);

        PopulatePluginList();
        CreateNewProjectButton();
        RenderInformationGrid();
    }

    private void CreateNewProjectButton() {
        JButton createButton = new JButton("Create New Project");

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // will do logic to ensure that all the information is correct
                // will ensure that it will save this data to the project info xml
                JOptionPane.showMessageDialog(frame, "Project was saved");
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });

        createButton.setBounds(1017, 11, 175, 25);
        frame.getContentPane().add(createButton);
    }

    private void PopulatePluginList() {
        DefaultListModel<String> listOfAddedPluginModel = new DefaultListModel<String>();

        final JList<String> listOfAddedPlugin = new JList<String>(listOfAddedPluginModel);
        listOfAddedPluginModel.addElement("Item 1");

        JButton addPluginButton = new JButton("Add Plugin");
        addPluginButton.setToolTipText("Add Plugin");
        addPluginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            }
        });
        addPluginButton.setBounds(10, 12, 89, 23);
        frame.getContentPane().add(addPluginButton);

        JButton deletePluginButton = new JButton("Delete Selected Plugin");
        deletePluginButton.setToolTipText("Delete Plugin");
        deletePluginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel model = (DefaultListModel) listOfAddedPlugin.getModel();
                int selectedIndex = listOfAddedPlugin.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
            }
        });
        deletePluginButton.setBounds(132, 12, 153, 23);
        frame.getContentPane().add(deletePluginButton);

        JScrollPane scrollPane = new JScrollPane(listOfAddedPlugin);
        scrollPane.setBounds(10, 40, 1182, 369);
        frame.getContentPane().add(scrollPane);
    }

    private void RenderInformationGrid() {
        BasicProjectInformation();
        ModisInformation();
        ProjectInformation();
        SummaryInformation();
    }

    private void BasicProjectInformation() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Basic Project Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setBounds(10, 420, 275, 275);
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setBounds(6, 25, 100, 15);
        panel.add(startDateLabel);
        startDate = new JTextField();
        startDate.setColumns(10);
        startDate.setBounds(116, 22, 140, 20);
        panel.add(startDate);

        JLabel projectNameLabel = new JLabel("Project Name: ");
        projectNameLabel.setBounds(6, 56, 100, 14);
        panel.add(projectNameLabel);
        projectName = new JTextField();
        projectName.setBounds(116, 53, 140, 20);
        panel.add(projectName);
        projectName.setColumns(10);

        JLabel workingDirLabel = new JLabel("Working Dir: ");
        workingDirLabel.setBounds(6, 87, 100, 15);
        panel.add(workingDirLabel);
        workingDirectory = new JTextField();
        workingDirectory.setColumns(10);
        workingDirectory.setBounds(116, 84, 140, 20);
        panel.add(workingDirectory);

        JLabel maskingFileLabel = new JLabel("Masking File");
        maskingFileLabel.setBounds(6, 118, 100, 15);
        panel.add(maskingFileLabel);
        maskFile = new JTextField();
        maskFile.setColumns(10);
        maskFile.setBounds(116, 115, 100, 20);
        panel.add(maskFile);

        JButton browseButton = new JButton(". . .");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File("."));
                chooser.setDialogTitle("Browse the folder to process");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);


                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    System.out.println("getCurrentDirectory(): "+ chooser.getCurrentDirectory());
                    System.out.println("getSelectedFile() : "+ chooser.getSelectedFile());
                    maskFile.setText(chooser.getSelectedFile().toString());
                } else {
                    System.out.println("No Selection ");
                }
            }
        });
        browseButton.setBounds(226, 115, 32, 20);
        panel.add(browseButton);
    }

    @SuppressWarnings("rawtypes")
    private void ModisInformation() {
        JPanel modisInformationPanel = new JPanel();
        modisInformationPanel.setLayout(null);
        modisInformationPanel.setBorder(new TitledBorder(null, "Modis Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        modisInformationPanel.setBounds(276, 420, 275, 275);
        frame.getContentPane().add(modisInformationPanel);

        modisListModel = new DefaultListModel();
        @SuppressWarnings("unchecked")
        final JList<DefaultListModel> modisList = new JList<DefaultListModel>(modisListModel);
        modisList.setBounds(15, 70, 245, 180);

        JButton addNewModisButton = new JButton("Edit Modis Tiles");
        addNewModisButton.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String tile = JOptionPane.showInputDialog(frame,"Enter Modis Tiles", null);
                modisListModel.addElement(tile);
            }
        });
        addNewModisButton.setBounds(15, 20, 120, 30);
        modisInformationPanel.add(addNewModisButton);
        modisInformationPanel.add(modisList);

        JButton btnDeleteSelected = new JButton("Delete Selected");
        btnDeleteSelected.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel model = (DefaultListModel) modisList.getModel();
                int selectedIndex = modisList.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
            }
        });
        btnDeleteSelected.setBounds(140, 20, 120, 30);
        modisInformationPanel.add(btnDeleteSelected);
    }

    private void ProjectInformation() {
        JPanel panel_2 = new JPanel();
        panel_2.setLayout(null);
        panel_2.setBorder(new TitledBorder(null, "Projection Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_2.setBounds(547, 420, 383, 275);
        frame.getContentPane().add(panel_2);

        JLabel coordinateSystemLabel = new JLabel("Coordinate System:");
        coordinateSystemLabel.setBounds(6, 16, 134, 14);
        panel_2.add(coordinateSystemLabel);
        JComboBox<String> coordinateSystemComboBox = new JComboBox<String>();
        coordinateSystemComboBox.setBounds(146, 13, 140, 20);
        coordinateSystemComboBox.addItem("item 1");
        coordinateSystemComboBox.addItem("item 2");
        coordinateSystemComboBox.addItem("item 3");
        panel_2.add(coordinateSystemComboBox);

        JLabel reSamplingLabel = new JLabel("Re-sampling Type:");
        reSamplingLabel.setBounds(6, 41, 109, 14);
        panel_2.add(reSamplingLabel);
        JComboBox<String> reSamplingComboBox = new JComboBox<String>();
        reSamplingComboBox.setBounds(146, 38, 140, 20);
        reSamplingComboBox.addItem("item 1");
        reSamplingComboBox.addItem("item 2");
        reSamplingComboBox.addItem("item 3");
        panel_2.add(reSamplingComboBox);

        JLabel datumLabel = new JLabel("Datum:");
        datumLabel.setBounds(6, 66, 109, 14);
        panel_2.add(datumLabel);
        JComboBox<String> datumComboBox = new JComboBox<String>();
        datumComboBox.setBounds(146, 63, 140, 20);
        datumComboBox.addItem("item 1");
        datumComboBox.addItem("item 2");
        datumComboBox.addItem("item 3");
        panel_2.add(datumComboBox);

        JLabel pixelSizeLabel = new JLabel("Pixel size meters:");
        pixelSizeLabel.setBounds(6, 91, 109, 14);
        panel_2.add(pixelSizeLabel);
        pixelSize = new JTextField();
        pixelSize.setColumns(10);
        pixelSize.setBounds(146, 90, 140, 16);
        panel_2.add(pixelSize);

        JLabel standardParallel1label = new JLabel("Standard parallel 1");
        standardParallel1label.setBounds(6, 116, 109, 14);
        panel_2.add(standardParallel1label);
        standardParallel1 = new JTextField();
        standardParallel1.setColumns(10);
        standardParallel1.setBounds(146, 115, 71, 16);
        panel_2.add(standardParallel1);

        JLabel centalMeridianLabel = new JLabel("Cental meridian");
        centalMeridianLabel.setBounds(6, 141, 109, 14);
        panel_2.add(centalMeridianLabel);
        centalMeridian = new JTextField();
        centalMeridian.setColumns(10);
        centalMeridian.setBounds(146, 140, 71, 16);
        panel_2.add(centalMeridian);

        JLabel falseEastingLabel = new JLabel("False easting");
        falseEastingLabel.setBounds(6, 166, 109, 14);
        panel_2.add(falseEastingLabel);
        falseEasting = new JTextField();
        falseEasting.setColumns(10);
        falseEasting.setBounds(146, 165, 71, 16);
        panel_2.add(falseEasting);

        JLabel standardParallel2Label = new JLabel("Standard parallel 2");
        standardParallel2Label.setBounds(6, 191, 109, 14);
        panel_2.add(standardParallel2Label);
        standardParallel2 = new JTextField();
        standardParallel2.setColumns(10);
        standardParallel2.setBounds(146, 192, 71, 16);
        panel_2.add(standardParallel2);

        JLabel latitudeOfOriginLabel = new JLabel("Latitude of origin");
        latitudeOfOriginLabel.setBounds(6, 216, 109, 14);
        panel_2.add(latitudeOfOriginLabel);
        latitudeOfOrigin = new JTextField();
        latitudeOfOrigin.setColumns(10);
        latitudeOfOrigin.setBounds(146, 215, 71, 16);
        panel_2.add(latitudeOfOrigin);

        JLabel falseNothingLabel = new JLabel("False nothing");
        falseNothingLabel.setBounds(6, 241, 109, 14);
        panel_2.add(falseNothingLabel);
        falseNothing = new JTextField();
        falseNothing.setColumns(10);
        falseNothing.setBounds(146, 240, 71, 16);
        panel_2.add(falseNothing);
    }

    @SuppressWarnings("rawtypes")
    private void SummaryInformation() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(null);
        summaryPanel.setBorder(new TitledBorder(null, "Summary Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        summaryPanel.setBounds(921, 420, 275, 275);
        frame.getContentPane().add(summaryPanel);

        summaryListModel = new DefaultListModel();
        final JList<?> summaryList = new JList<Object>(summaryListModel);
        summaryList.setBounds(15, 70, 245, 180);

        JButton editSummaryButton = new JButton("Edit Summary");
        editSummaryButton.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JTextField xField = new JTextField(20);
                final JTextField yField = new JTextField(20);
                JPanel myPanel = new JPanel();
                JButton browseButton = new JButton(". . .");
                browseButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setCurrentDirectory(new java.io.File("."));
                        chooser.setDialogTitle("Browse the folder to process");
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setAcceptAllFileFilterUsed(false);


                        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                            System.out.println("getCurrentDirectory(): "+ chooser.getCurrentDirectory());
                            System.out.println("getSelectedFile() : "+ chooser.getSelectedFile());
                            yField.setText(chooser.getSelectedFile().toString());
                        } else {
                            System.out.println("No Selection ");
                        }
                    }
                });

                myPanel.add(new JLabel("Title:"));
                myPanel.add(xField);
                myPanel.add(Box.createHorizontalStrut(15)); // a spacer
                myPanel.add(new JLabel("File Path:"));
                myPanel.add(yField);
                myPanel.add(Box.createHorizontalStrut(15)); // a spacer
                myPanel.add(browseButton);

                int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Zonal Summary Information", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    System.out.println("x value: " + xField.getText());
                    System.out.println("y value: " + yField.getText());
                    String tile = xField.getText() + ":     " + yField.getText();
                    summaryListModel.addElement(tile);
                }
            }
        });
        editSummaryButton.setBounds(15, 20, 120, 30);
        summaryPanel.add(editSummaryButton);
        summaryPanel.add(summaryList);

        JButton deleteSummaryButton = new JButton("Delete Selected");
        deleteSummaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel model = (DefaultListModel) summaryList.getModel();
                int selectedIndex = summaryList.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
            }
        });
        deleteSummaryButton.setBounds(140, 20, 120, 30);
        summaryPanel.add(deleteSummaryButton);

    }

    //  private void PopulatePluginList() {
    //        String[] columnNames = {"Plugin Name", "Indicies Calculator", "Quality Control", "Vegetarian", "Buttons"};
    //
    //        DefaultListModel listModel = new DefaultListModel();
    //
    //        JList list_1 = new JList(listModel);
    //        listModel.addElement("Item 1");
    //
    //
    //
    //        Object[][] data = {
    //                {"Kathy", "Smith", false, list_1, "Button 1"},
    //                {"John", "Doe", new Boolean(true), new Integer(3), "Button 1"},
    //                {"Sue", "Black",new Boolean(true), new Integer(2), "Button 1"},
    //                {"Jane", "White",new Boolean(true), new Integer(20), "Button 1"},
    //                {"Joe", "Brown",new Boolean(true), new Integer(10), "Button 1"}
    //        };
    //
    //
    //
    //        JTable table = new JTable(data, columnNames);
    //        table.setFillsViewportHeight(true);
    //        table.getColumn("Buttons").setCellRenderer(new ButtonRenderer());
    //        table.getColumn("Buttons").setCellEditor(new ButtonEditor(new JCheckBox()));
    //
    //        table.getColumn("Vegetarian").setCellRenderer(new listRenderer());
    //        table.getColumn("Vegetarian").setCellEditor(new DefaultCellEditor(new JCheckBox()));
    //
    //
    //        table.getColumn("Quality Control").setCellRenderer(new CheckBoxRenderer());
    //        table.getColumn("Quality Control").setCellEditor(new CheckBoxEditor(new JCheckBox()));
    //
    //        //table.getColumn("Vegetarian").setCellRenderer(new JScrollTableRenderer());
    //        //table.getColumn("Vegetarian").setCellEditor(new JScrollTableEditor());
    //
    //
    //        JScrollPane scrollPane = new JScrollPane(table);
    //        scrollPane.setBounds(10, 40, 1182, 369);
    //        frame.getContentPane().add(scrollPane);
    //    }
    //    class ButtonRenderer extends JButton implements TableCellRenderer {
    //
    //        /**
    //         *
    //         */
    //        private static final long serialVersionUID = 1L;
    //
    //        public ButtonRenderer() {
    //            setOpaque(true);
    //        }
    //
    //        @Override
    //        public Component getTableCellRendererComponent(JTable table, Object value,
    //                boolean isSelected, boolean hasFocus, int row, int column) {
    //            if (isSelected) {
    //                setForeground(table.getSelectionForeground());
    //                setBackground(table.getSelectionBackground());
    //            } else {
    //                setForeground(table.getForeground());
    //                setBackground(UIManager.getColor("Button.background"));
    //            }
    //            setText((value == null) ? "" : value.toString());
    //            return this;
    //        }
    //    }
    //
    //    class ButtonEditor extends DefaultCellEditor {
    //        /**
    //         *
    //         */
    //        private static final long serialVersionUID = 1L;
    //
    //        protected JButton button;
    //
    //        private String label;
    //
    //        private boolean isPushed;
    //
    //        public ButtonEditor(JCheckBox checkBox) {
    //            super(checkBox);
    //            button = new JButton();
    //            button.setOpaque(true);
    //            button.addActionListener(new ActionListener() {
    //                @Override
    //                public void actionPerformed(ActionEvent e) {
    //                    fireEditingStopped();
    //                }
    //            });
    //        }
    //
    //        @Override
    //        public Component getTableCellEditorComponent(JTable table, Object value,
    //                boolean isSelected, int row, int column) {
    //            if (isSelected) {
    //                button.setForeground(table.getSelectionForeground());
    //                button.setBackground(table.getSelectionBackground());
    //            } else {
    //                button.setForeground(table.getForeground());
    //                button.setBackground(table.getBackground());
    //            }
    //            label = (value == null) ? "" : value.toString();
    //            button.setText(label);
    //            isPushed = true;
    //            return button;
    //        }
    //
    //        @Override
    //        public Object getCellEditorValue() {
    //            if (isPushed) {
    //                //
    //                //
    //                JOptionPane.showMessageDialog(button, label + ": Ouch!");
    //                JFrame window = new JFrame();
    //                window.setBounds(100, 100, 580, 410);
    //                window.setVisible(true);
    //                // System.out.println(label + ": Ouch!");
    //            }
    //            isPushed = false;
    //            return new String(label);
    //        }
    //
    //        @Override
    //        public boolean stopCellEditing() {
    //            isPushed = false;
    //            return super.stopCellEditing();
    //        }
    //
    //        @Override
    //        protected void fireEditingStopped() {
    //            super.fireEditingStopped();
    //        }
    //    }
    //
    //    class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
    //
    //        /**
    //         *
    //         */
    //        private static final long serialVersionUID = 1L;
    //
    //        public CheckBoxRenderer() {
    //            setOpaque(true);
    //        }
    //
    //        @Override
    //        public Component getTableCellRendererComponent(JTable table, Object value,
    //                boolean isSelected, boolean hasFocus, int row, int column) {
    //            if (isSelected) {
    //                setForeground(table.getSelectionForeground());
    //                setBackground(table.getSelectionBackground());
    //            } else {
    //                setForeground(table.getForeground());
    //                setBackground(UIManager.getColor("Button.background"));
    //            }
    //            setText((value == null) ? "" : value.toString());
    //            return this;
    //        }
    //    }
    //
    //    class CheckBoxEditor extends DefaultCellEditor {
    //
    //        public CheckBoxEditor(JCheckBox arg0) {
    //            super(arg0);
    //            // TODO Auto-generated constructor stub
    //        }
    //    }
    //
    //    class listRenderer extends JList<Object> implements TableCellRenderer {
    //
    //        /**
    //         *
    //         */
    //        private static final long serialVersionUID = 1L;
    //
    //        public listRenderer() {
    //            setOpaque(true);
    //        }
    //
    //        @Override
    //        public Component getTableCellRendererComponent(JTable table, Object value,
    //                boolean isSelected, boolean hasFocus, int row, int column) {
    //            if (isSelected) {
    //                setForeground(table.getSelectionForeground());
    //                setBackground(table.getSelectionBackground());
    //            } else {
    //                setForeground(table.getForeground());
    //                setBackground(UIManager.getColor("Button.background"));
    //            }
    //
    //            return this;
    //        }
    //    }
}
