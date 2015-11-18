package version2.prototype.EastWebUI.QueryUI;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.ProjectInfoMetaData.ProjectInfoCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.util.EASTWebQuery;
import version2.prototype.util.EASTWebResults;

public class QueryUI {

    private JFrame frame;

    boolean isViewSQL = false;

    private JComboBox<String> projectListComboBox ;
    private JComboBox<String> pluginComboBox;

    private JCheckBox chckbxCount;
    private JCheckBox chckbxSum;
    private JCheckBox chckbxMean;
    private JCheckBox chckbxStdev;
    private JCheckBox minCheckBox;
    private JCheckBox maxCheckBox;
    private JCheckBox sqrSumCheckBox;

    String[] operationList = {"<", ">", "=", "<>", "<=", ">="};
    private JCheckBox chckbxZone;
    private JCheckBox chckbxYear;
    private JCheckBox chckbxDay;
    private JComboBox<Object> zoneComboBox;
    private JComboBox<Object> yearComboBox;
    private  JComboBox<Object> dayComboBox;
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
                    @SuppressWarnings("unused")
                    QueryUI window = new QueryUI(new JComboBox<String>());

                } catch (Exception e) {
                    ErrorLog.add(Config.getInstance(), "QueryUI.main problem with running a QueryUI window.", e);
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
        frame.setBounds(100, 100, 400, 625);
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
                zoneComboBox.removeAllItems();
                zoneComboBox.addItem("");

                ProjectInfoFile project = null;
                try {
                    project = new ProjectInfoCollection().GetProject(selectedProject);
                    pluginComboBox.removeAll();
                    includeListModel.removeAllElements();
                    excludeListModel.removeAllElements();

                    for(ProjectInfoPlugin plugin : project.GetPlugins()){
                        pluginComboBox.addItem(plugin.GetName());
                    }

                    for(ProjectInfoPlugin plugin: project.GetPlugins()){
                        for(String indice: plugin.GetIndices()) {
                            excludeListModel.addElement(indice);
                        }
                    }

                    for(String zone: EASTWebResults.GetZonesListFromProject(selectedProject, String.valueOf(pluginComboBox.getSelectedItem()))){
                        zoneComboBox.addItem(zone);
                    }
                } catch (ClassNotFoundException | NoSuchMethodException
                        | SecurityException | InstantiationException
                        | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | IOException
                        | ParserConfigurationException | SAXException
                        | ParseException | SQLException e) {
                    ErrorLog.add(Config.getInstance(), "QueryUI.CreateSQLView problem with getting projectInfoFile.", e);

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
        viewSQLButton.setBounds(10, 552, 89, 23);
        frame.getContentPane().add(viewSQLButton);

        JButton btnQuery = new JButton("Query");
        btnQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                ProjectInfoFile project = null;
                try {
                    project = new ProjectInfoCollection().GetProject(String.valueOf(projectListComboBox.getSelectedItem()));
                } catch (ClassNotFoundException | NoSuchMethodException
                        | SecurityException | InstantiationException
                        | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | IOException
                        | ParserConfigurationException | SAXException
                        | ParseException e) {
                    ErrorLog.add(Config.getInstance(), "QueryUI.CreateSQLView problem with getting projectInfoFile.", e);
                }

                EASTWebQuery listFile = null;
                ArrayList<String> indicies = new ArrayList<String>();
                for(Object o : includeListModel.toArray()){
                    indicies.add(o.toString());
                }
                for(ProjectInfoPlugin s : project.GetPlugins()) {
                    for(ProjectInfoSummary summary : project.GetSummaries()) {
                        try {
                            listFile =  EASTWebResults.GetEASTWebQuery(
                                    Config.getInstance().getGlobalSchema(),
                                    String.valueOf(projectListComboBox.getSelectedItem()),
                                    s.GetName(),
                                    chckbxCount.isSelected(),
                                    maxCheckBox.isSelected(),
                                    minCheckBox.isSelected(),
                                    chckbxSum.isSelected(),
                                    chckbxMean.isSelected(),
                                    sqrSumCheckBox.isSelected(),
                                    chckbxStdev.isSelected(),
                                    String.valueOf(zoneComboBox.getSelectedItem()),
                                    String.valueOf(yearComboBox.getSelectedItem()),
                                    (yearTextField.getText().equals("") ? null : Integer.parseInt(yearTextField.getText())),
                                    String.valueOf(dayComboBox.getSelectedItem()),
                                    (dayTextField.getText().equals("") ? null : Integer.parseInt(dayTextField.getText())),
                                    indicies
                                    );


                        } catch (NumberFormatException e) {
                            ErrorLog.add(Config.getInstance(), "QueryUI.CreateSQLView problem with getting csv result files.", e);
                        }

                    }
                }

                if(listFile != null){
                    String message = "Enter Directory";
                    String directory = JOptionPane.showInputDialog(frame, message);

                    if (directory == null) {
                        // User clicked cancel
                    }else{
                        File dest = new File(directory);
                        try {
                            for(File v:EASTWebResults.GetResultCSVFiles(listFile)) {
                                FileUtils.copyFileToDirectory(v, dest);
                            }

                        } catch (IOException | ClassNotFoundException | SQLException | ParserConfigurationException | SAXException e) {
                            JOptionPane.showMessageDialog(frame, "Folder not found");
                            ErrorLog.add(Config.getInstance(), "QueryResultWindow.initialize problem with copying files to different directory.", e);
                        }
                    }
                }
                else{
                    JOptionPane.showMessageDialog(frame, "No results generated");
                }
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        btnQuery.setBounds(150, 552, 89, 23);
        frame.getContentPane().add(btnQuery);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        btnCancel.setBounds(285, 552, 89, 23);
        frame.getContentPane().add(btnCancel);

        sqlViewTextPanel = new JTextPane();
        sqlViewTextPanel.setBounds(384, 11, 290, 489);
        frame.getContentPane().add(sqlViewTextPanel);

        JLabel lblPlugin = new JLabel("Plugin: ");
        lblPlugin.setBounds(19, 45, 46, 14);
        frame.getContentPane().add(lblPlugin);

        pluginComboBox = new JComboBox<String>();
        pluginComboBox.setBounds(118, 42, 157, 20);
        frame.getContentPane().add(pluginComboBox);
    }

    private void populateClauseUI() {
        JPanel clausePanel = new JPanel();
        clausePanel.setBounds(10, 188, 364, 110);
        clausePanel.setBorder(new TitledBorder(null, "Clause Statement", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        frame.getContentPane().add(clausePanel);
        clausePanel.setLayout(null);

        chckbxZone = new JCheckBox("Zone");
        chckbxZone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                zoneComboBox.setEnabled(chckbxZone.isSelected());
            }
        });
        chckbxZone.setBounds(6, 20, 70, 23);
        clausePanel.add(chckbxZone);
        zoneComboBox = new JComboBox<Object>();
        zoneComboBox.setBounds(82, 21, 272, 20);
        clausePanel.add(zoneComboBox);

        chckbxYear = new JCheckBox("Year");
        chckbxYear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                yearComboBox.setEnabled(chckbxYear.isSelected());
                yearTextField.setEnabled(chckbxYear.isSelected());
            }
        });
        chckbxYear.setBounds(6, 46, 70, 23);
        clausePanel.add(chckbxYear);
        yearComboBox = new JComboBox<Object>(operationList);
        yearComboBox.setBounds(82, 47, 97, 20);
        clausePanel.add(yearComboBox);
        yearTextField = new JTextField();
        yearTextField.setColumns(10);
        yearTextField.setBounds(189, 46, 165, 22);
        clausePanel.add(yearTextField);

        chckbxDay = new JCheckBox("Day");
        chckbxDay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dayComboBox.setEnabled(chckbxDay.isSelected());
                dayTextField.setEnabled(chckbxDay.isSelected());
            }
        });
        chckbxDay.setBounds(6, 74, 70, 23);
        clausePanel.add(chckbxDay);
        dayComboBox = new JComboBox<Object>(operationList);
        dayComboBox.setBounds(82, 75, 97, 20);
        clausePanel.add(dayComboBox);
        dayTextField = new JTextField();
        dayTextField.setColumns(10);
        dayTextField.setBounds(189, 74, 165, 22);
        clausePanel.add(dayTextField);

        zoneComboBox.setEnabled(false);
        yearComboBox.setEnabled(false);
        yearTextField.setEnabled(false);
        dayComboBox.setEnabled(false);
        dayTextField.setEnabled(false);
    }

    private void populateIndicesUI() {


        JPanel indicesPanel = new JPanel();
        indicesPanel.setLayout(null);
        indicesPanel.setBorder(new TitledBorder(null, "Enviromental Index", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        indicesPanel.setBounds(10, 299, 364, 242);
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
        fieldsPanel.setBounds(10, 76, 364, 110);
        frame.getContentPane().add(fieldsPanel);

        chckbxCount = new JCheckBox("count");
        chckbxCount.setBounds(6, 21, 63, 23);
        fieldsPanel.add(chckbxCount);

        chckbxSum = new JCheckBox("sum");
        chckbxSum.setBounds(6, 51, 63, 23);
        fieldsPanel.add(chckbxSum);

        chckbxMean = new JCheckBox("mean");
        chckbxMean.setBounds(6, 77, 63, 23);
        fieldsPanel.add(chckbxMean);

        chckbxStdev = new JCheckBox("stdev");
        chckbxStdev.setBounds(131, 21, 75, 23);
        fieldsPanel.add(chckbxStdev);

        JCheckBox minCheckBox = new JCheckBox("min");
        minCheckBox.setBounds(131, 51, 75, 23);
        fieldsPanel.add(minCheckBox);

        JCheckBox maxCheckBox = new JCheckBox("max");
        maxCheckBox.setBounds(131, 77, 75, 23);
        fieldsPanel.add(maxCheckBox);

        JCheckBox sqrSumCheckBox = new JCheckBox("sqrSum");
        sqrSumCheckBox.setBounds(261, 21, 97, 23);
        fieldsPanel.add(sqrSumCheckBox);
    }

    private void populateProjectList() {
        File fileDir = new File(System.getProperty("user.dir") + "\\projects\\");
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
            sqlViewTextPanel.setText(String.format("%s\n %s\n %s",
                    SelectStatement(),
                    FromStatement(),
                    WhereStatement()
                    ));

        }

        private String FromStatement() {
            String query = String.format("FROM \n \"%s\"\n", String.valueOf(projectListComboBox.getSelectedItem()));
            return query;

        }

        private String SelectStatement() {
            String query = String.format("SELECT  \n \t name,\n \t year,\n \t day,\n \t index,\n");

            if(chckbxCount.isSelected()) {
                query += "\tcount,\n";
            }
            if(chckbxSum.isSelected()) {
                query += "\tsum,\n";
            }
            if(chckbxMean.isSelected()) {
                query += "\tmean,\n";
            }
            if(chckbxStdev.isSelected()) {
                query += "\tstdev,\n";
            }
            if(minCheckBox.isSelected()) {
                query += "\tmin,\n";
            }
            if(maxCheckBox.isSelected()) {
                query += "\tmax,\n";
            }
            if(sqrSumCheckBox.isSelected()) {
                query += "\tsqrSum,\n";
            }
            for(int i = 0; i < includeListModel.size(); i ++){
                query += String.format("\t%s,\n", includeListModel.elementAt(i));
            }



            return query;
        }

        public Object WhereStatement() {

            String query = "";

            if(chckbxZone.isSelected() || chckbxYear.isSelected() || chckbxDay.isSelected())
            {
                query = String.format("WHERE\n");

                if(chckbxYear.isSelected())
                {
                    query += String.format("year%s%s\n", String.valueOf(yearComboBox.getSelectedItem()), yearTextField.getText());
                }
                if(chckbxDay.isSelected())
                {
                    query += String.format("day%s%s\n", String.valueOf(dayComboBox.getSelectedItem()), dayTextField.getText());
                }
                if(chckbxZone.isSelected())
                {
                    query += String.format("zone%s\n", String.valueOf(zoneComboBox.getSelectedItem()));
                }
            }

            return query;
        }
    }
}
