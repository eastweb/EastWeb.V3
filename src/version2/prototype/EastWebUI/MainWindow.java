package version2.prototype.EastWebUI;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class MainWindow {

    private JFrame frame;
    private JMenuItem mntmCreateNewProject;
    private JMenu mnHelp;
    private JTextField intermidateDumpPath;
    private JMenuItem mntmEditProject;

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
        frame.setBounds(100, 100, 550, 425);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);

        CreateButton();
        ComboBox();
        FileMenu();
        TableView();
    }

    private void FileMenu() {
        JMenuBar menuBar = createMenuBar();
        menuBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
            }
        });
        menuBar.setBounds(0, 0, 534, 25);
        frame.getContentPane().add(menuBar);
    }

    private void ListView() {
        String[] columnNames = {"First Name", "Last Name", "Sport", "# of Years", "Vegetarian"};

        Object[][] data = {
                {"Kathy", "Smith", "Snowboarding", new Integer(5), new JButton("Button 1")},
                {"John", "Doe", "Rowing", new Integer(3), new JButton("Button 1")},
                {"Sue", "Black","Knitting", new Integer(2), new JButton("Button 1")},
                {"Jane", "White","Speed reading", new Integer(20), new JButton("Button 1")},
                {"Joe", "Brown","Pool", new Integer(10), new JButton("Button 1")},
                {"Kathy", "Smith", "Snowboarding", new Integer(5), new JButton("Button 1")},
                {"John", "Doe", "Rowing", new Integer(3), new JButton("Button 1")},
                {"Sue", "Black","Knitting", new Integer(2),new JButton("Button 1")},
                {"Jane", "White","Speed reading", new Integer(20), new JButton("Button 1")},
                {"Joe", "Brown","Pool", new Integer(10), new JButton("Button 1")}
        };

        JTable table = new JTable(data, columnNames);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(10, 200, 500, 100);
        frame.getContentPane().add(scrollPane);
    }

    private void TableView()
    {
        DefaultTableModel dm = new DefaultTableModel();
        dm.setDataVector(new Object[][] {
                { "Sufi's Project", "total progress", "Progress Detail"},
                { "Alpha", "total progress", "Progress Detail"},
                { "Liu, Yi Project", "total progress", "Progress Detail"},
                { "System Project", "total progress", "Progress Detail"},
                { "Kate Jensen", "total progress", "Progress Detail"},
                { "Project 6", "total progress", "Progress Detail"},
        }, new Object[] { "Project Name", " Total Progress" ,"Technical Progress", });

        JTable table = new JTable(dm);
        table.getColumn("Technical Progress").setCellRenderer(new ButtonRenderer());
        table.getColumn("Technical Progress").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(10, 123, 524, 262);
        frame.getContentPane().add(scrollPane);

        JLabel lblProjectList = new JLabel("Project List");
        lblProjectList.setBounds(10, 95, 138, 14);
        frame.getContentPane().add(lblProjectList);

        final JLabel lblIntermidateDumpFolder = new JLabel("Intermidate dump folder");
        lblIntermidateDumpFolder.setEnabled(false);
        lblIntermidateDumpFolder.setBounds(10, 62, 138, 14);
        frame.getContentPane().add(lblIntermidateDumpFolder);

        final JButton btnBrowser = new JButton(". . .");
        btnBrowser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File("."));
                chooser.setDialogTitle("Browse the folder to process");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);


                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    System.out.println("getCurrentDirectory(): "+ chooser.getCurrentDirectory());
                    System.out.println("getSelectedFile() : "+ chooser.getSelectedFile());
                    intermidateDumpPath.setText(chooser.getSelectedFile().toString());
                } else {
                    System.out.println("No Selection ");
                }
            }

        });
        btnBrowser.setEnabled(false);
        btnBrowser.setBounds(435, 58, 66, 23);
        frame.getContentPane().add(btnBrowser);

        final JLabel lblHardDriveCapacity = new JLabel("Hard Drive Capacity ");
        lblHardDriveCapacity.setEnabled(false);
        lblHardDriveCapacity.setBounds(185, 36, 200, 14);
        frame.getContentPane().add(lblHardDriveCapacity);

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

        intermidateDumpPath = new JTextField();
        intermidateDumpPath.setEditable(false);
        intermidateDumpPath.setBounds(185, 59, 200, 20);
        frame.getContentPane().add(intermidateDumpPath);
        intermidateDumpPath.setColumns(10);
    }

    private void CreateButton() {
        JButton btnNewButton = new JButton("Run Project");

        btnNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showInputDialog(frame,"What is your name?", null);
                JOptionPane.showMessageDialog(frame, "Eggs are not supposed to be green.");
            }
        });

        btnNewButton.setBounds(395, 93, 139, 19);
        frame.getContentPane().add(btnNewButton);
    }

    private void ComboBox() {
        JComboBox<String> comboBox = new JComboBox<String>();

        comboBox.addItem("Sufi's Project");
        comboBox.addItem("NEXT Project");

        comboBox.setBounds(185, 93, 200, 19);
        frame.getContentPane().add(comboBox);
    }

    public JMenuBar createMenuBar() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();

        //Build the first menu.
        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic(KeyEvent.VK_A);
        mnFile.getAccessibleContext().setAccessibleDescription("The only menu in this program that has menu items");
        menuBar.add(mnFile);

        //a group of JMenuItems
        JMenuItem menuItem;
        mntmCreateNewProject = new JMenuItem("Create New Project", KeyEvent.VK_T);
        mntmCreateNewProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new ProjectInformationPage(true);
            }
        });
        mntmCreateNewProject.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_1, ActionEvent.ALT_MASK));
        mntmCreateNewProject.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        mnFile.add(mntmCreateNewProject);

        mntmEditProject = new JMenuItem("Edit Project");
        mntmEditProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new ProjectInformationPage(false);
            }
        });
        mntmEditProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_MASK));
        mntmEditProject.setMnemonic(KeyEvent.VK_B);
        mnFile.add(mntmEditProject);
        ButtonGroup group = new ButtonGroup();

        //a group of check box menu items
        mnFile.addSeparator();
        JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
        cbMenuItem.setMnemonic(KeyEvent.VK_C);
        mnFile.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Another one");
        cbMenuItem.setMnemonic(KeyEvent.VK_H);
        mnFile.add(cbMenuItem);

        //a submenu
        mnFile.addSeparator();
        JMenu submenu = new JMenu("A submenu");
        submenu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("An item in the submenu");
        menuItem.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_2, ActionEvent.ALT_MASK));
        submenu.add(menuItem);

        menuItem = new JMenuItem("Another item");
        submenu.add(menuItem);
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

        return menuBar;
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {

        /**
         * 
         */
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
        /**
         * 
         */
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
                //
                //
                //JOptionPane.showMessageDialog(button, label + ": Ouch!");
                JFrame window = new JFrame();
                window.setBounds(100, 100, 580, 410);
                window.setVisible(true);
                // System.out.println(label + ": Ouch!");
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
}
