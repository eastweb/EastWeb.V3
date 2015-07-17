package version2.prototype.EastWebUI.QueryUI;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.ProjectInfoMetaData.ProjectInfoCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;

public class QueryUI {

    private JFrame frame;

    boolean isViewSQL = false;

    private JComboBox<String> projectListComboBox ;

    private JCheckBox chckbxCount;
    private JCheckBox chckbxSum;
    private JCheckBox chckbxMean;
    private JCheckBox chckbxStdev;

    String[] operationList = {"<", ">", "=", "<>", "<=", ">="};
    private JComboBox zoneComboBox;
    private JComboBox yearComboBox;
    private  JComboBox dayComboBox;
    private JTextField zoneTextField;
    private JTextField yearTextField;
    private JTextField dayTextField;

    private JList<String> includeList;
    private JList<String> excludeList;
    private DefaultListModel<String> includeListModel ;
    private DefaultListModel<String> excludeListModel ;

    private JTextPane sqlViewTextPanel;


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    QueryUI window = new QueryUI(new JComboBox<String>());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public QueryUI(JComboBox<String> projectList) {

        initialize();
        frame.setVisible(true);

        // And From your main() method or any other method
        Timer timer = new Timer();
        timer.schedule(new UpdateQuery(), 0, 100);
        frame.setVisible(true);
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 400, 550);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        CreateSQLView();
    }

    private void CreateSQLView() {
        includeListModel = new DefaultListModel<String>();
        excludeListModel = new DefaultListModel<String>();

        JLabel lblProject = new JLabel("Project:");
        lblProject.setBounds(19, 14, 89, 14);
        frame.getContentPane().add(lblProject);
        projectListComboBox = new JComboBox<String>();
        projectListComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String selectedProject = String.valueOf(projectListComboBox.getSelectedItem());

                try {
                    includeListModel.removeAllElements();
                    excludeListModel.removeAllElements();
                    ProjectInfoFile project = new ProjectInfoCollection().GetProject(selectedProject);

                    if(project == null) {
                        return;
                    }

                    for(ProjectInfoPlugin plugin: project.GetPlugins()){
                        for(String indice: plugin.GetIndicies()) {
                            excludeListModel.addElement(indice);
                        }
                    }

                } catch (ClassNotFoundException | NoSuchMethodException
                        | SecurityException | InstantiationException
                        | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | IOException
                        | ParserConfigurationException | SAXException
                        | ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        projectListComboBox.setBounds(118, 11, 157, 20);
        populateProjectList();
        frame.getContentPane().add(projectListComboBox);

        populateClauseUI();
        populateFieldsUI();
        populateIndicesUI();

        JButton viewSQLButton = new JButton("View SQL");
        viewSQLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                isViewSQL = !isViewSQL;

                if(isViewSQL) {
                    frame.setBounds(100, 100, 700, 550);
                } else {
                    frame.setBounds(100, 100, 400, 550);
                }
            }
        });
        viewSQLButton.setBounds(285, 10, 89, 23);
        frame.getContentPane().add(viewSQLButton);

        JButton btnQuery = new JButton("Query");
        btnQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // call query run method
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        btnQuery.setBounds(186, 477, 89, 23);
        frame.getContentPane().add(btnQuery);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        btnCancel.setBounds(285, 477, 89, 23);
        frame.getContentPane().add(btnCancel);

        sqlViewTextPanel = new JTextPane();
        sqlViewTextPanel.setBounds(384, 11, 290, 489);
        frame.getContentPane().add(sqlViewTextPanel);
    }

    private void populateClauseUI() {
        JPanel clausePanel = new JPanel();
        clausePanel.setBounds(10, 104, 364, 109);
        clausePanel.setBorder(new TitledBorder(null, "Clause Statement", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        frame.getContentPane().add(clausePanel);
        clausePanel.setLayout(null);

        final JCheckBox chckbxZone = new JCheckBox("Zone");
        chckbxZone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                zoneComboBox.setEnabled(chckbxZone.isSelected());
                zoneTextField.setEnabled(chckbxZone.isSelected());
            }
        });
        chckbxZone.setBounds(6, 20, 70, 23);
        clausePanel.add(chckbxZone);
        zoneComboBox = new JComboBox(operationList);
        zoneComboBox.setBounds(82, 21, 97, 20);
        clausePanel.add(zoneComboBox);
        zoneTextField = new JTextField();
        zoneTextField.setBounds(189, 20, 97, 22);
        clausePanel.add(zoneTextField);
        zoneTextField.setColumns(10);

        final JCheckBox chckbxYear = new JCheckBox("Year");
        chckbxYear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                yearComboBox.setEnabled(chckbxYear.isSelected());
                yearTextField.setEnabled(chckbxYear.isSelected());
            }
        });
        chckbxYear.setBounds(6, 46, 70, 23);
        clausePanel.add(chckbxYear);
        yearComboBox = new JComboBox(operationList);
        yearComboBox.setBounds(82, 47, 97, 20);
        clausePanel.add(yearComboBox);
        yearTextField = new JTextField();
        yearTextField.setColumns(10);
        yearTextField.setBounds(189, 46, 97, 22);
        clausePanel.add(yearTextField);

        final JCheckBox chckbxDay = new JCheckBox("Day");
        chckbxDay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dayComboBox.setEnabled(chckbxDay.isSelected());
                dayTextField.setEnabled(chckbxDay.isSelected());
            }
        });
        chckbxDay.setBounds(6, 74, 70, 23);
        clausePanel.add(chckbxDay);
        dayComboBox = new JComboBox(operationList);
        dayComboBox.setBounds(82, 75, 97, 20);
        clausePanel.add(dayComboBox);
        dayTextField = new JTextField();
        dayTextField.setColumns(10);
        dayTextField.setBounds(189, 74, 97, 22);
        clausePanel.add(dayTextField);

        zoneComboBox.setEnabled(false);
        zoneTextField.setEnabled(false);
        yearComboBox.setEnabled(false);
        yearTextField.setEnabled(false);
        dayComboBox.setEnabled(false);
        dayTextField.setEnabled(false);
    }

    private void populateIndicesUI() {


        JPanel indicesPanel = new JPanel();
        indicesPanel.setLayout(null);
        indicesPanel.setBorder(new TitledBorder(null, "Enviromental Index", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        indicesPanel.setBounds(10, 224, 364, 242);
        indicesPanel.setLayout(null);
        frame.getContentPane().add(indicesPanel);

        JScrollPane includeScrollPanel = new JScrollPane();
        includeScrollPanel.setBounds(10, 40, 127, 191);
        indicesPanel.add(includeScrollPanel);
        includeList = new JList<String>(includeListModel);
        includeScrollPanel.setViewportView(includeList);

        JScrollPane excludeScrollPanel = new JScrollPane();
        excludeScrollPanel.setBounds(227, 40, 127, 191);
        indicesPanel.add(excludeScrollPanel);
        excludeList = new JList<String>(excludeListModel);
        excludeScrollPanel.setViewportView(excludeList);

        JButton excludeButton = new JButton(">>");
        excludeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel<String> model = (DefaultListModel<String>) includeList.getModel();
                int selectedIndex = includeList.getSelectedIndex();
                if (selectedIndex != -1) {
                    excludeListModel.addElement(includeList.getSelectedValue());
                    model.remove(selectedIndex);
                }
            }
        });
        excludeButton.setBounds(147, 55, 70, 23);
        indicesPanel.add(excludeButton);

        final JButton includeButton = new JButton("<<");
        includeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel<String> model = (DefaultListModel<String>) excludeList.getModel();
                int selectedIndex = excludeList.getSelectedIndex();
                if (selectedIndex != -1) {
                    includeListModel.addElement(excludeList.getSelectedValue());
                    model.remove(selectedIndex);

                }
            }
        });
        includeButton.setBounds(147, 178, 70, 23);
        indicesPanel.add(includeButton);

        JLabel lblInclude = new JLabel("Include");
        lblInclude.setBounds(10, 26, 127, 14);
        indicesPanel.add(lblInclude);

        JLabel lblExclude = new JLabel("Exclude");
        lblExclude.setBounds(228, 26, 126, 14);
        indicesPanel.add(lblExclude);
    }

    private void populateFieldsUI() {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(null);
        fieldsPanel.setBorder(new TitledBorder(null, "Fields", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        fieldsPanel.setBounds(10, 42, 364, 51);
        frame.getContentPane().add(fieldsPanel);

        chckbxCount = new JCheckBox("count");
        chckbxCount.setBounds(6, 21, 63, 23);
        fieldsPanel.add(chckbxCount);

        chckbxSum = new JCheckBox("sum");
        chckbxSum.setBounds(71, 21, 63, 23);
        fieldsPanel.add(chckbxSum);

        chckbxMean = new JCheckBox("mean");
        chckbxMean.setBounds(136, 21, 63, 23);
        fieldsPanel.add(chckbxMean);

        JCheckBox chckbxStdev = new JCheckBox("stdev");
        chckbxStdev.setBounds(201, 21, 97, 23);
        fieldsPanel.add(chckbxStdev);
    }

    private void populateProjectList() {
        File fileDir = new File(System.getProperty("user.dir") + "\\src\\version2\\prototype\\ProjectInfoMetaData\\");
        projectListComboBox.removeAllItems();
        projectListComboBox.addItem("");

        for(File fXmlFile: getXMLFiles(fileDir)){
            projectListComboBox.addItem(fXmlFile.getName().replace(".xml", ""));
        }
    }

    /**
     * get all files in a folder
     * @param folder
     * @return
     */
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

    /**
     * get file extension
     * @param f
     * @return
     */
    private String getFileExtensionName(File f) {
        if (f.getName().indexOf(".") == -1) {
            return "";
        } else {
            return f.getName().substring(f.getName().length() - 3, f.getName().length());
        }
    }

    class UpdateQuery extends TimerTask {
        int count = 0;
        @Override
        public void run() {
            count += 1;
            sqlViewTextPanel.setText(String.valueOf(count));

        }
    }
}
