package version2.prototype.EastWebUI.MainWindow;

import java.awt.Color;
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
import java.awt.event.WindowAdapter;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import com.sun.glass.events.WindowEvent;

import version2.prototype.Config;
import version2.prototype.EASTWebManager;
import version2.prototype.ErrorLog;
import version2.prototype.GUIUpdateHandler;
import version2.prototype.TaskState;
import version2.prototype.EastWebUI.ProgressUI.ProjectProgress;
import version2.prototype.EastWebUI.ProjectInformationUI.ProjectInformationPage;
import version2.prototype.EastWebUI.QueryUI.QueryUI;
import version2.prototype.ProjectInfoMetaData.ProjectInfoCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.Scheduler.SchedulerData;
import version2.prototype.Scheduler.SchedulerStatus;

import java.awt.event.WindowStateListener;

public class MainWindow {

    private JFrame frame;
    private JMenuItem mntmCreateNewProject;
    private JMenu mnHelp;
    private JTextField intermidateDumpPath;
    private JMenuItem mntmEditProject;
    private JMenuItem mntmOpenSetFolder;
    private JMenuItem mntmDeleteAllFiles;
    private DefaultTableModel defaultTableModel;
    private JComboBox<String> projectList;
    private JCheckBox chckbxIntermidiateFiles;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                EASTWebManager.Start();
            }
        });

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    MainWindow window = new MainWindow();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    ErrorLog.add(Config.getInstance(), "MainWindow.main problem with running a MainWindow window.", e);
                }
            }
        });
    }

    /**
     * Constructor
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
        frame.addWindowListener((new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt){
                EASTWebManager.StopAndShutdown();
            }
        }));


        FileMenu();
        PopulateUIControl();
        TableView();
    }

    /**
     * create file menu
     */
    private void FileMenu() {
        // menu bar item wrapper
        JMenuBar menuBar = new JMenuBar();
        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic(KeyEvent.VK_A);
        mnFile.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
        menuBar.add(mnFile);

        //create project button (opens the project info page )
        mntmCreateNewProject = new JMenuItem("Create New Project", KeyEvent.VK_T);
        mntmCreateNewProject.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_1, ActionEvent.ALT_MASK));
        mntmCreateNewProject.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        mntmCreateNewProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    new ProjectInformationPage(true,  new mainWindowListenerImplementation());
                } catch (IOException | ParserConfigurationException | SAXException | ParseException e) {
                    ErrorLog.add(Config.getInstance(), "MainWindow.FileMenu problem with creating new ProjectInformationPage.", e);
                }
            }
        });
        mnFile.add(mntmCreateNewProject);

        //edit project info button (opens the project info on limited edition)
        mntmEditProject = new JMenuItem("Edit Project");
        mntmEditProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_MASK));
        mntmEditProject.setMnemonic(KeyEvent.VK_B);
        mntmEditProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    new ProjectInformationPage(false, new mainWindowListenerImplementation());
                } catch (IOException | ParserConfigurationException | SAXException | ParseException e) {
                    ErrorLog.add(Config.getInstance(), "MainWindow.FileMenu problem with creating new ProjectInformationPage.", e);
                }
            }
        });
        mnFile.add(mntmEditProject);
        mnFile.addSeparator();


        //edit project info button (opens the project info on limited edition)
        JMenuItem mntmRunQuery = new JMenuItem("Run Query");
        mntmRunQuery.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_MASK));
        mntmRunQuery.setMnemonic(KeyEvent.VK_B);
        mntmRunQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new QueryUI();
                //new QueryUI(projectList);
            }
        });
        mnFile.add(mntmRunQuery);
        mnFile.addSeparator();

        // create a new plugin meta data file
        JMenuItem createNewPlugin = new JMenuItem("Create New Plugin Template");
        createNewPlugin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = JOptionPane.showInputDialog("Enter plugin name", JOptionPane.YES_NO_OPTION );
                File theDir = new File(System.getProperty("user.dir") + "\\plugins\\" + fileName + ".xml" );
                try {
                    theDir.createNewFile();
                    // TODO: could create a mock template
                } catch (IOException e1) {
                    ErrorLog.add(Config.getInstance(), "MainWindow.FileMenu problem with creating new file, '" + theDir.getPath() + "', from Desktop.", e1);
                }
            }
        });
        createNewPlugin.setMnemonic(KeyEvent.VK_B);
        mnFile.add(createNewPlugin);
        mnFile.addSeparator();

        // wrapper for manage intermediate files
        JMenu submenu = new JMenu("Manage Intermidiate Files");
        submenu.setMnemonic(KeyEvent.VK_S);

        // opens folder where intermediate file lives
        mntmOpenSetFolder = new JMenuItem("Open set Folder");
        mntmOpenSetFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    Desktop.getDesktop().open(new File(intermidateDumpPath.getText()));
                } catch (IOException e) {
                    ErrorLog.add(Config.getInstance(), "MainWindow.FileMenu problem with opening file, '" + intermidateDumpPath.getText() + "', from Desktop.", e);
                }
            }
        });
        submenu.add(mntmOpenSetFolder);

        // delete all intermediate files
        mntmDeleteAllFiles = new JMenuItem("Delete All files");
        mntmDeleteAllFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete all files ?", "Delete Intermediate File", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    try {
                        FileUtils.cleanDirectory(new File(intermidateDumpPath.getText()));
                    } catch (IOException e1) {
                        ErrorLog.add(Config.getInstance(), "MainWindow.FileMenu problem with cleaning directory '" + intermidateDumpPath.getText() + "'.", e1);
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

    /**
     * populate main window UI
     */
    private void PopulateUIControl()
    {
        final File diskPartition = new File(System.getProperty("user.dir"));

        final JLabel lblIntermidateDumpFolder = new JLabel("Intermediate dump folder");
        lblIntermidateDumpFolder.setEnabled(false);
        lblIntermidateDumpFolder.setBounds(10, 62, 138, 14);
        frame.getContentPane().add(lblIntermidateDumpFolder);

        // label to show free space on drive
        final JLabel lblHardDriveCapacity = new JLabel(String.format("Free Space Capacity: %s GB", diskPartition.getFreeSpace()/ (1024 *1024) / 1000 ));
        lblHardDriveCapacity.setEnabled(false);
        lblHardDriveCapacity.setBounds(185, 36, 244, 14);
        frame.getContentPane().add(lblHardDriveCapacity);

        // set dump folder
        intermidateDumpPath = new JTextField(System.getProperty("user.dir"));
        intermidateDumpPath.setEditable(false);
        intermidateDumpPath.setBounds(185, 59, 200, 20);
        frame.getContentPane().add(intermidateDumpPath);
        intermidateDumpPath.setColumns(10);

        // browser button for intermediate dump folder
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

        // check box to control intermediate files process (true => creates intermediate files)
        chckbxIntermidiateFiles = new JCheckBox("Intermediate Files");
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

    /**
     * set table rendering
     * populated running projects
     */
    private void TableView()
    {
        defaultTableModel = new DefaultTableModel();
        defaultTableModel.setDataVector(new Object[][] {},
                new Object[] { "Project Progress", "Intermediate Selection", "Status",  "Actions", "Delete" });

        JTable table = new JTable(defaultTableModel);
        table.getColumn("Project Progress").setCellRenderer(new ProgressButtonRenderer());
        table.getColumn("Project Progress").setCellEditor(new ProgressButtonEditor(new JCheckBox()));

        table.getColumn("Status").setMaxWidth(100);
        table.getColumn("Status").setCellRenderer(new StatusButtonRenderer());
        table.getColumn("Status").setCellEditor(new StatusButtonEditor(new JCheckBox()));

        table.getColumn("Actions").setMaxWidth(100);
        table.getColumn("Actions").setCellRenderer(new ActionButtonRenderer());
        table.getColumn("Actions").setCellEditor(new ActionButtonEditor(new JCheckBox()));

        table.getColumn("Delete").setMaxWidth(100);
        table.getColumn("Delete").setCellRenderer(new DeleteButtonRenderer());
        table.getColumn("Delete").setCellEditor(new DeleteButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(10, 123, 1181, 567);
        frame.getContentPane().add(scrollPane);
    }

    /**
     * run projects
     */
    private void runSelectedProject() {
        JButton runButton = new JButton("Load Project");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {

                String selectedProject = String.valueOf(projectList.getSelectedItem());
                ProjectInfoFile project = ProjectInfoCollection.GetProject(Config.getInstance(), selectedProject);
                SchedulerData data;
                try {
                    data = new SchedulerData(project, !chckbxIntermidiateFiles.isSelected());
                    EASTWebManager.LoadNewScheduler(data, false);

                    defaultTableModel.addRow(new Object[] {
                            String.valueOf(projectList.getSelectedItem()),
                            chckbxIntermidiateFiles.isSelected(),
                            String.valueOf(projectList.getSelectedItem()),
                            String.valueOf(projectList.getSelectedItem()),
                            String.valueOf(projectList.getSelectedItem())});
                } catch (PatternSyntaxException | DOMException | ParserConfigurationException | SAXException | IOException e) {
                    ErrorLog.add(Config.getInstance(), "MainWindow.FileMenu problem with creating new file from Desktop.", e);
                }
            }
        });

        runButton.setBounds(395, 93, 139, 19);
        frame.getContentPane().add(runButton);
    }

    /**
     * populate project list
     */
    private void populateProjectList() {
        ArrayList<ProjectInfoFile> projects = ProjectInfoCollection.GetAllProjectInfoFiles(Config.getInstance());
        //            File fileDir = new File(System.getProperty("user.dir") + "\\projects\\");
        projectList.removeAllItems();

        //            for(File fXmlFile: getXMLFiles(fileDir)){
        //                projectList.addItem(fXmlFile.getName().replace(".xml", ""));
        //            }
        for(ProjectInfoFile project : projects)
        {
            projectList.addItem(project.GetProjectName());
        }
    }

    /**
     * button to be render for technical progress
     * @author sufi
     *
     */
    class ProgressButtonRenderer extends JButton implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        public ProgressButtonRenderer() {
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

    /**
     * editor for the technical progress
     * @author sufi
     *
     */
    class ProgressButtonEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        protected JButton button;
        private String label;
        private boolean isPushed;

        public ProgressButtonEditor(JCheckBox checkBox) {
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
                new ProjectProgress(label.toString());
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

    /** button to be render for technical progress
     * @author sufi
     *
     */
    class ActionButtonRenderer extends JButton implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        public ActionButtonRenderer() {
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

            String projectName = value.toString();

            SchedulerStatus status = EASTWebManager.GetSchedulerStatus(projectName);

            if(status == null) {
                // Do nothing
            } else if(status.State == TaskState.RUNNING) {
                setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/stop.png")));

            } else {
                setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/StatusAnnotations_Play_32xSM_color.png")));
            }
            return this;
        }
    }

    /**
     * editor for the technical progress
     * @author sufi
     *
     */
    class ActionButtonEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        protected JButton button;
        private String label;
        private boolean isPushed;

        public ActionButtonEditor(JCheckBox checkBox) {
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

            String projectName = label.toString();

            SchedulerStatus status = EASTWebManager.GetSchedulerStatus(projectName);

            if(status == null) {
                // Do nothing
            } else if(status.State == TaskState.RUNNING) {
                button.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/stop.png")));
            } else {
                button.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/StatusAnnotations_Play_32xSM_color.png")));
            }

            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                String projectName = label.toString();

                SchedulerStatus status = EASTWebManager.GetSchedulerStatus(projectName);

                if(status == null) {
                    // Do nothing
                } else if(status.State == TaskState.RUNNING) {
                    EASTWebManager.StopExistingScheduler(projectName, false);
                    button.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/StatusAnnotations_Play_32xSM_color.png")));

                } else {
                    EASTWebManager.StartExistingScheduler(projectName, false);
                    button.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/stop.png")));
                }
                frame.repaint();
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

    /** button to be render for technical progress
     * @author sufi
     *
     */
    class DeleteButtonRenderer extends JButton implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        public DeleteButtonRenderer() {
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
            setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/trashCan.png")));
            return this;
        }
    }

    /**
     * editor for the technical progress
     * @author sufi
     *
     */
    class DeleteButtonEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int removeProject = -1;

        public DeleteButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    if(removeProject > -1) {
                        defaultTableModel.removeRow(removeProject);
                        removeProject = -1;
                    }
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
            button.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/trashCan.png")));
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {

                String projectName = label.toString();
                ArrayList<SchedulerStatus> schedulersStatus = EASTWebManager.GetSchedulerStatuses();

                for(SchedulerStatus item : schedulersStatus)
                {
                    String currentProjectName = item.ProjectName;

                    if(currentProjectName.equals(projectName)) {
                        removeProject = schedulersStatus.indexOf(item);
                    }
                }
                if(removeProject != -1) {
                    EASTWebManager.DeleteScheduler(projectName, true);

                }
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



    /** button to be render for technical progress
     * @author sufi
     *
     */
    class StatusButtonRenderer extends JButton implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        private String label;

        public StatusButtonRenderer() {
            setOpaque(true);
            label = null;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if(value != null && label == null) {
                EASTWebManager.RegisterGUIUpdateHandler(new GUIUpdateHandlerImplementation(value.toString(), this));
            }

            String projectName = (value == null) ? "" : value.toString();
            SchedulerStatus status = EASTWebManager.GetSchedulerStatus(projectName);

            if(status == null) {
                setBackground(Color.GRAY);
                setForeground(Color.GRAY);
                setToolTipText("Project is not running.");
            } else if(status.State == TaskState.RUNNING) {
                if(status.ProjectUpToDate) {
                    setBackground(Color.GREEN);
                    setForeground(Color.GREEN);
                    setToolTipText("Project is up to date.");
                } else {
                    setBackground(Color.YELLOW);
                    setForeground(Color.YELLOW);
                    setToolTipText("Project is processing.");
                }
            } else {
                setBackground(Color.GRAY);
                setForeground(Color.GRAY);
                setToolTipText("Project is not running.");
            }

            setText((value == null) ? "" : value.toString());
            return this;
        }

        private class GUIUpdateHandlerImplementation implements GUIUpdateHandler{
            private String projectName;
            private JButton button;

            public GUIUpdateHandlerImplementation(String projectName, JButton button){
                this.projectName = projectName;
                this.button = button;
            }

            @Override
            public void run() {
                SchedulerStatus status = EASTWebManager.GetSchedulerStatus(projectName);


                if(status == null) {
                    button.setBackground(Color.GRAY);
                    button.setForeground(Color.GRAY);
                    button.setToolTipText("Project is not running.");
                } else if(status.State == TaskState.RUNNING) {
                    if(status.ProjectUpToDate) {
                        button.setBackground(Color.GREEN);
                        button.setForeground(Color.GREEN);
                        button.setToolTipText("Project is up to date.");
                    } else {
                        button.setBackground(Color.YELLOW);
                        button.setForeground(Color.YELLOW);
                        button.setToolTipText("Project is processing.");
                    }
                } else {
                    button.setBackground(Color.GRAY);
                    button.setForeground(Color.GRAY);
                    button.setToolTipText("Project is not running.");
                }

                frame.repaint();
            }
        }
    }

    /**
     * editor for the technical progress
     * @author sufi
     *
     */
    class StatusButtonEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        protected JButton button;
        private String label;
        private boolean isPushed;

        public StatusButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            label = null;
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
            if(value != null) {
                if(label == null) {
                    EASTWebManager.RegisterGUIUpdateHandler(new GUIUpdateHandlerImplementation(value.toString(), button));
                }
                label = value.toString();
            } else {
                label = "";
            }

            String projectName = (value == null) ? "" : value.toString();
            SchedulerStatus status = EASTWebManager.GetSchedulerStatus(projectName);

            if(status == null) {
                button.setBackground(Color.GRAY);
                button.setForeground(Color.GRAY);
                button.setToolTipText("Project is not running.");
            } else if(status.State == TaskState.RUNNING) {
                if(status.ProjectUpToDate) {
                    button.setBackground(Color.GREEN);
                    button.setForeground(Color.GREEN);
                    button.setToolTipText("Project is up to date.");
                } else {
                    button.setBackground(Color.YELLOW);
                    button.setForeground(Color.YELLOW);
                    button.setToolTipText("Project is processing.");
                }
            } else {
                button.setBackground(Color.GRAY);
                button.setForeground(Color.GRAY);
                button.setToolTipText("Project is not running.");
            }
            frame.repaint();

            isPushed = true;

            return button;
        }

        @Override
        public Object getCellEditorValue() {

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

        private class GUIUpdateHandlerImplementation implements GUIUpdateHandler{
            private String projectName;
            private JButton button;

            public GUIUpdateHandlerImplementation(String projectName, JButton button){
                this.projectName = projectName;
                this.button = button;
            }

            @Override
            public void run() {
                SchedulerStatus status = EASTWebManager.GetSchedulerStatus(projectName);

                if(status == null) {
                    button.setBackground(Color.GRAY);
                    button.setForeground(Color.GRAY);
                    button.setToolTipText("Project is not running.");
                } else if(status.State == TaskState.RUNNING) {
                    if(status.ProjectUpToDate) {
                        button.setBackground(Color.GREEN);
                        button.setForeground(Color.GREEN);
                        button.setToolTipText("Project is up to date.");
                    } else {
                        button.setBackground(Color.YELLOW);
                        button.setForeground(Color.YELLOW);
                        button.setToolTipText("Project is processing.");
                    }
                } else {
                    button.setBackground(Color.GRAY);
                    button.setForeground(Color.GRAY);
                    button.setToolTipText("Project is not running.");
                }

                frame.repaint();
            }
        }
    }


    /**
     * handles and trigger to the main window
     * @author sufi
     *
     */
    class mainWindowListenerImplementation implements MainWindowListener{
        @Override
        public void RefreshProjectList(MainWindowEventObject e) {
            populateProjectList();
        }
    }
}
