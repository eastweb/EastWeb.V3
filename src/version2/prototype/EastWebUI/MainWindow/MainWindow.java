package version2.prototype.EastWebUI.MainWindow;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;

import version2.prototype.EastWebUI.ProjectInformationUI.ProjectInformationPage;

public class MainWindow {

    private JFrame frame;
    private JMenuItem mntmCreateNewProject;
    private JMenu mnHelp;
    private JTextField intermidateDumpPath;
    private JMenuItem mntmEditProject;
    private JMenuItem mntmOpenSetFolder;
    private JMenuItem mntmDeleteAllFiles;
    String freeSpaceString;
    DefaultTableModel defaultTableModel;
    JComboBox<String> projectList;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    MainWindow window = new MainWindow();
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
    public MainWindow() {
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

        FileMenu();
        PopulateUIControl();
        TableView();
    }

    private void FileMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic(KeyEvent.VK_A);
        mnFile.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
        menuBar.add(mnFile);

        mntmCreateNewProject = new JMenuItem("Create New Project", KeyEvent.VK_T);
        mntmCreateNewProject.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_1, ActionEvent.ALT_MASK));
        mntmCreateNewProject.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        mntmCreateNewProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new ProjectInformationPage(true,  new mainWindowListenerImplementation());
            }
        });
        mnFile.add(mntmCreateNewProject);

        mntmEditProject = new JMenuItem("Edit Project");
        mntmEditProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_MASK));
        mntmEditProject.setMnemonic(KeyEvent.VK_B);
        mntmEditProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new ProjectInformationPage(false, new mainWindowListenerImplementation());
            }
        });
        mnFile.add(mntmEditProject);
        mnFile.addSeparator();

        JMenuItem createNewPlugin = new JMenuItem("Create New Plugin Template");
        createNewPlugin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = JOptionPane.showInputDialog("Enter plugin name", JOptionPane.YES_NO_OPTION );
                File theDir = new File(System.getProperty("user.dir") + "\\src\\version2\\prototype\\PluginMetaData\\" + fileName + ".xml" );
                try {
                    theDir.createNewFile();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        createNewPlugin.setMnemonic(KeyEvent.VK_B);
        mnFile.add(createNewPlugin);
        mnFile.addSeparator();

        JMenu submenu = new JMenu("Manage Intermidiate Files");
        submenu.setMnemonic(KeyEvent.VK_S);

        mntmOpenSetFolder = new JMenuItem("Open set Folder");
        mntmOpenSetFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    Desktop.getDesktop().open(new File(intermidateDumpPath.getText()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        submenu.add(mntmOpenSetFolder);

        mntmDeleteAllFiles = new JMenuItem("Delete All files");
        mntmDeleteAllFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete all files ?", "Delete Intermidate File", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    try {
                        FileUtils.cleanDirectory(new File(intermidateDumpPath.getText()));
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });
        submenu.add(mntmDeleteAllFiles);
        mnFile.add(submenu);

        //Build second menu in the menu bar.
        mnHelp = new JMenu("Help");
        mnHelp.setMnemonic(KeyEvent.VK_N);
        mnHelp.getAccessibleContext().setAccessibleDescription("This menu does nothing");
        menuBar.add(mnHelp);

        JMenuItem mntmSettings = new JMenuItem("Settings", KeyEvent.VK_T);
        mnHelp.add(mntmSettings);

        JMenuItem mntmManual = new JMenuItem("Manual", KeyEvent.VK_T);
        mnHelp.add(mntmManual);

        menuBar.setBounds(0, 0, 1201, 25);
        frame.getContentPane().add(menuBar);
    }

    private void PopulateUIControl()
    {
        final File diskPartition = new File(System.getProperty("user.dir"));

        final JLabel lblIntermidateDumpFolder = new JLabel("Intermidate dump folder");
        lblIntermidateDumpFolder.setEnabled(false);
        lblIntermidateDumpFolder.setBounds(10, 62, 138, 14);
        frame.getContentPane().add(lblIntermidateDumpFolder);

        final JLabel lblHardDriveCapacity = new JLabel(String.format("Free Space Capacity: %s GB", diskPartition.getFreeSpace()/ (1024 *1024) / 1000 ));
        lblHardDriveCapacity.setEnabled(false);
        lblHardDriveCapacity.setBounds(185, 36, 244, 14);
        frame.getContentPane().add(lblHardDriveCapacity);

        intermidateDumpPath = new JTextField(System.getProperty("user.dir"));
        intermidateDumpPath.setEditable(false);
        intermidateDumpPath.setBounds(185, 59, 200, 20);
        frame.getContentPane().add(intermidateDumpPath);
        intermidateDumpPath.setColumns(10);

        final JButton btnBrowser = new JButton(". . .");
        btnBrowser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File("."));
                chooser.setDialogTitle("Browse the folder to process");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    System.out.println("getCurrentDirectory(): "+ chooser.getCurrentDirectory());
                    System.out.println("getSelectedFile() : "+ chooser.getSelectedFile());
                    intermidateDumpPath.setText(chooser.getSelectedFile().toString());
                    lblHardDriveCapacity.setText(String.format("Free Space Capacity on %s: %s GB", chooser.getSelectedFile().toString(), new File(chooser.getSelectedFile().toString()).getFreeSpace()/ (1024 *1024) / 1000 ));
                } else {
                    System.out.println("No Selection ");
                }
            }
        });
        btnBrowser.setEnabled(false);
        btnBrowser.setBounds(395, 58, 34, 23);
        frame.getContentPane().add(btnBrowser);

        final JCheckBox chckbxIntermidiateFiles = new JCheckBox("Intermidiate Files");
        chckbxIntermidiateFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(chckbxIntermidiateFiles.isSelected()){
                    intermidateDumpPath.setEditable(true);
                    lblIntermidateDumpFolder.setEnabled(true);
                    btnBrowser.setEnabled(true);
                    lblHardDriveCapacity.setEnabled(true);
                }
                else{
                    intermidateDumpPath.setEditable(false);
                    lblIntermidateDumpFolder.setEnabled(false);
                    btnBrowser.setEnabled(false);
                    lblHardDriveCapacity.setEnabled(false);
                }
            }
        });
        chckbxIntermidiateFiles.setBounds(10, 32, 141, 23);
        frame.getContentPane().add(chckbxIntermidiateFiles);

        JLabel lblProjectList = new JLabel("Project List");
        lblProjectList.setBounds(10, 95, 138, 14);
        frame.getContentPane().add(lblProjectList);

        projectList = new JComboBox<String>();
        populateProjectList();
        frame.getContentPane().add(projectList);
        projectList.setBounds(185, 93, 200, 19);

        runSelectedProject();
    }

    private void TableView()
    {
        defaultTableModel = new DefaultTableModel();
        defaultTableModel.setDataVector(new Object[][] {

        }, new Object[] { "Project Name", " Total Progress" ,"Technical Progress", "Summary Composite", "Intermidiate Selection" });

        JTable table = new JTable(defaultTableModel);
        table.getColumn("Technical Progress").setCellRenderer(new ButtonRenderer());
        table.getColumn("Technical Progress").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(10, 123, 1181, 567);
        frame.getContentPane().add(scrollPane);
    }

    private void runSelectedProject() {
        JButton runButton = new JButton("Run Project");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                defaultTableModel.addRow(new Object[] { String.valueOf(projectList.getSelectedItem()), "75 %", "Progress Detail", "Summary Quiries", true});
                populateProjectList();
                // todo activate
                //SchedulerData data = new SchedulerData(); // TODO: this will be replace by user interface
                //Scheduler.getInstance(data).run();
            }
        });

        runButton.setBounds(395, 93, 139, 19);
        frame.getContentPane().add(runButton);
    }

    private void populateProjectList() {

        File fileDir = new File(System.getProperty("user.dir") + "\\src\\version2\\prototype\\ProjectInfoMetaData\\");

        projectList.removeAllItems();
        projectList.addItem("Sufi's Project");
        projectList.addItem("NEXT Project");

        for(File fXmlFile: getXMLFiles(fileDir)){
            projectList.addItem(fXmlFile.getName().replace(".xml", ""));
        }
    }

    private File[] getXMLFiles(File folder) {
        List<File> aList = new ArrayList<File>();
        File[] files = folder.listFiles();

        for (File pf : files) {

            if (pf.isFile() && getFileExtensionName(pf).indexOf("xml") != -1) {
                aList.add(pf);
            }
        }
        return aList.toArray(new File[aList.size()]);
    }

    private String getFileExtensionName(File f) {
        if (f.getName().indexOf(".") == -1) {
            return "";
        } else {
            return f.getName().substring(f.getName().length() - 3, f.getName().length());
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        protected JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                JOptionPane.showMessageDialog(button, label + ": Ouch!");
            }
            isPushed = false;
            return new String(label);
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    class mainWindowListenerImplementation implements MainWindowListener{

        @Override
        public void RefreshProjectList(MainWindowEventObject e) {
            populateProjectList();
        }
    }
}
