package version2.prototype.EastWebUI_V2.QueryUI_v2;

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
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.ProjectInfoMetaData.ProjectInfoCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;
import version2.prototype.util.EASTWebQuery;
import version2.prototype.util.EASTWebResult;
import version2.prototype.util.EASTWebResults;

public class QueryUI {
    boolean isViewSQL = false;
    String[] operationList = {"<", ">", "=", "<>", "<=", ">="};

    private JFrame frame;
    private JComboBox<String> projectListComboBox ;
    private JComboBox<String> pluginComboBox;
    private JCheckBox chckbxCount;
    private JCheckBox chckbxSum;
    private JCheckBox chckbxMean;
    private JCheckBox chckbxStdev;
    private JCheckBox minCheckBox;
    private JCheckBox maxCheckBox;
    private JCheckBox sqrSumCheckBox;
    private JCheckBox chckbxZone;
    private JCheckBox chckbxYear;
    private JCheckBox chckbxDay;
    private JComboBox<Object> yearComboBox;
    private JComboBox<Object> dayComboBox;
    private JTextField yearTextField;
    private JTextField dayTextField;
    private JList<String> includeListIndices;
    private JList<String> excludeListIndices;
    private JList<String> includeListZone;
    private JList<String> excludeListZone;
    private JTextPane sqlViewTextPanel;

    private DefaultListModel<String> includeIndicesListModel ;
    private DefaultListModel<String> excludeIndicesListModel ;

    private DefaultListModel<String> includeZoneListModel ;
    private DefaultListModel<String> excludeZoneListModel ;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new QueryUI();
                } catch (Exception e) {
                    ErrorLog.add(Config.getInstance(), "QueryUI.main problem with running a QueryUI window.", e);
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public QueryUI() {
        initialize();

        // And From your main() method or any other method
        Timer timer = new Timer();
        timer.schedule(new UpdateQuery(), 0, 100);
    }

    private void initialize() {
        frame = new JFrame();
        frame.setVisible(true);
        frame.setBounds(100, 100, 400, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        PopulateFieldsUI();
        PopulateClauseUI();
        PopulateIndicesUI();
        CreateSQLView();
    }

    private void PopulateFieldsUI() {
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

        minCheckBox = new JCheckBox("min");
        minCheckBox.setBounds(131, 51, 75, 23);
        fieldsPanel.add(minCheckBox);

        maxCheckBox = new JCheckBox("max");
        maxCheckBox.setBounds(131, 77, 75, 23);
        fieldsPanel.add(maxCheckBox);

        sqrSumCheckBox = new JCheckBox("sqrSum");
        sqrSumCheckBox.setBounds(261, 21, 97, 23);
        fieldsPanel.add(sqrSumCheckBox);
    }

    private void PopulateClauseUI() {
        includeZoneListModel = new DefaultListModel<String>();
        excludeZoneListModel = new DefaultListModel<String>();

        JPanel clausePanel = new JPanel();
        clausePanel.setLayout(null);
        clausePanel.setBounds(10, 188, 364, 275);
        clausePanel.setBorder(new TitledBorder(null, "Clause Statement", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        frame.getContentPane().add(clausePanel);

        chckbxYear = new JCheckBox("Year");
        chckbxYear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                yearComboBox.setEnabled(chckbxYear.isSelected());
                yearTextField.setEnabled(chckbxYear.isSelected());
            }
        });
        chckbxYear.setBounds(6, 22, 70, 23);
        clausePanel.add(chckbxYear);

        yearComboBox = new JComboBox<Object>(operationList);
        yearComboBox.setBounds(82, 23, 97, 20);
        yearComboBox.setEnabled(false);
        clausePanel.add(yearComboBox);

        yearTextField = new JTextField();
        yearTextField.setColumns(10);
        yearTextField.setBounds(189, 23, 165, 22);
        yearTextField.setEnabled(false);
        clausePanel.add(yearTextField);

        chckbxDay = new JCheckBox("Day");
        chckbxDay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dayComboBox.setEnabled(chckbxDay.isSelected());
                dayTextField.setEnabled(chckbxDay.isSelected());
            }
        });
        chckbxDay.setBounds(6, 48, 70, 23);
        clausePanel.add(chckbxDay);

        dayComboBox = new JComboBox<Object>(operationList);
        dayComboBox.setBounds(82, 52, 97, 20);
        dayComboBox.setEnabled(false);
        clausePanel.add(dayComboBox);

        dayTextField = new JTextField();
        dayTextField.setColumns(10);
        dayTextField.setBounds(189, 50, 165, 22);
        dayTextField.setEnabled(false);
        clausePanel.add(dayTextField);

        populateZoneUI(clausePanel);
    }


    private void PopulateIndicesUI() {
        includeIndicesListModel = new DefaultListModel<String>();
        excludeIndicesListModel = new DefaultListModel<String>();

        JPanel indicesPanel = new JPanel();
        indicesPanel.setLayout(null);
        indicesPanel.setBorder(new TitledBorder(null, "Enviromental Index", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        indicesPanel.setBounds(10, 474, 364, 242);
        indicesPanel.setLayout(null);
        frame.getContentPane().add(indicesPanel);

        JScrollPane includeScrollPanel = new JScrollPane();
        includeScrollPanel.setBounds(10, 40, 127, 191);
        indicesPanel.add(includeScrollPanel);
        includeListIndices = new JList<String>(includeIndicesListModel);
        includeScrollPanel.setViewportView(includeListIndices);

        JScrollPane excludeScrollPanel = new JScrollPane();
        excludeScrollPanel.setBounds(227, 40, 127, 191);
        indicesPanel.add(excludeScrollPanel);
        excludeListIndices = new JList<String>(excludeIndicesListModel);
        excludeScrollPanel.setViewportView(excludeListIndices);

        JButton excludeButton = new JButton(">>");
        excludeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {removeSelectedIndices();}
        });
        excludeButton.setBounds(147, 55, 70, 23);
        indicesPanel.add(excludeButton);

        JButton includeButton = new JButton("<<");
        includeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {addSelectedIndices();}
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

    private void CreateSQLView() {
        JLabel lblProject = new JLabel("Project:");
        lblProject.setBounds(19, 14, 89, 14);
        frame.getContentPane().add(lblProject);

        projectListComboBox = new JComboBox<String>();
        projectListComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {populateInfoForProject();}
        });
        projectListComboBox.setBounds(118, 11, 157, 20);
        projectListComboBox.removeAllItems();
        projectListComboBox.addItem("");
        for(ProjectInfoFile project : ProjectInfoCollection.GetAllProjectInfoFiles(Config.getInstance())) {
            projectListComboBox.addItem(project.GetProjectName());
        }
        frame.getContentPane().add(projectListComboBox);

        JButton viewSQLButton = new JButton("View SQL");
        viewSQLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {setSQLView();}
        });
        viewSQLButton.setBounds(10, 727, 89, 23);
        frame.getContentPane().add(viewSQLButton);

        JButton btnQuery = new JButton("Query");
        btnQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {queryProject();}
        });
        btnQuery.setBounds(150, 727, 89, 23);
        frame.getContentPane().add(btnQuery);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBounds(285, 727, 89, 23);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));}
        });
        frame.getContentPane().add(btnCancel);

        sqlViewTextPanel = new JTextPane();
        sqlViewTextPanel.setBounds(384, 11, 290, 489);
        frame.getContentPane().add(sqlViewTextPanel);

        JLabel lblPlugin = new JLabel("Plugin: ");
        lblPlugin.setBounds(19, 45, 46, 14);
        frame.getContentPane().add(lblPlugin);

        pluginComboBox = new JComboBox<String>();
        pluginComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {populateIndicesInfo();}
        });
        pluginComboBox.setBounds(118, 42, 157, 20);
        frame.getContentPane().add(pluginComboBox);
    }

    private void populateZoneUI(JPanel clausePanel) {
        final JLabel includeLbl = new JLabel("Include");
        includeLbl.setBounds(16, 104, 127, 14);
        includeLbl.setEnabled(false);
        clausePanel.add(includeLbl);
        final JScrollPane includeScrollPaneZone = new JScrollPane();
        includeScrollPaneZone.setBounds(10, 125, 125, 139);
        includeScrollPaneZone.setEnabled(false);
        clausePanel.add(includeScrollPaneZone);
        includeListZone = new JList<String>(includeZoneListModel);
        includeScrollPaneZone.setViewportView(includeListZone);
        includeListZone.setEnabled(false);

        final JLabel excludeLbl = new JLabel("Exclude");
        excludeLbl.setBounds(228, 104, 126, 14);
        clausePanel.add(excludeLbl);
        excludeLbl.setEnabled(false);
        final JScrollPane excludedScrollPaneZone = new JScrollPane();
        excludedScrollPaneZone.setBounds(229, 127, 125, 137);
        excludedScrollPaneZone.setEnabled(false);
        clausePanel.add(excludedScrollPaneZone);
        excludeListZone = new JList<String>(excludeZoneListModel);
        excludedScrollPaneZone.setViewportView(excludeListZone);
        excludeListZone.setEnabled(false);

        final JButton excludeButton = new JButton(">>");
        excludeButton.setBounds(148, 146, 70, 23);
        excludeButton.setEnabled(false);
        excludeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {deleteSelectedZone();}
        });
        clausePanel.add(excludeButton);

        final JButton includeButton = new JButton("<<");
        includeButton.setBounds(148, 219, 70, 23);
        includeButton.setEnabled(false);
        includeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {addSelectedZone();}
        });
        clausePanel.add(includeButton);

        chckbxZone = new JCheckBox("Zone");
        chckbxZone.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                includeLbl.setEnabled(chckbxZone.isSelected());
                includeScrollPaneZone.setEnabled(chckbxZone.isSelected());
                includeListZone.setEnabled(chckbxZone.isSelected());
                excludeLbl.setEnabled(chckbxZone.isSelected());
                excludedScrollPaneZone.setEnabled(chckbxZone.isSelected());
                excludeListZone.setEnabled(chckbxZone.isSelected());
                excludeButton.setEnabled(chckbxZone.isSelected());
                includeButton.setEnabled(chckbxZone.isSelected());}
        });
        chckbxZone.setBounds(6, 74, 70, 23);
        clausePanel.add(chckbxZone);
    }

    private void removeSelectedIndices() {
        DefaultListModel<String> model = (DefaultListModel<String>) includeListIndices.getModel();
        List<String> selectedIndexs = includeListIndices.getSelectedValuesList();

        for(String selectedIndex : selectedIndexs){
            if (selectedIndex != null) {
                excludeIndicesListModel.addElement(selectedIndex);
                model.removeElement(selectedIndex);
            }
        }
    }

    private void addSelectedIndices() {
        DefaultListModel<String> model = (DefaultListModel<String>) excludeListIndices.getModel();
        List<String> selectedIndexs = excludeListIndices.getSelectedValuesList();

        for(String selectedIndex : selectedIndexs){
            if (selectedIndex != null) {
                includeIndicesListModel.addElement(selectedIndex);
                model.removeElement(selectedIndex);
            }
        }
    }

    private void deleteSelectedZone() {
        DefaultListModel<String> model = (DefaultListModel<String>) includeListZone.getModel();
        List<String> selectedIndexs = includeListZone.getSelectedValuesList();

        for(String selectedIndex : selectedIndexs){
            if (selectedIndex != null) {
                excludeZoneListModel.addElement(selectedIndex);
                model.removeElement(selectedIndex);
            }
        }
    }

    private void addSelectedZone() {
        DefaultListModel<String> model = (DefaultListModel<String>) excludeListZone.getModel();
        List<String> selectedIndexs = excludeListZone.getSelectedValuesList();

        for(String selectedIndex : selectedIndexs){
            if (selectedIndex != null) {
                includeZoneListModel.addElement(selectedIndex);
                model.removeElement(selectedIndex);
            }
        }
    }

    private void populateInfoForProject() {
        String selectedProject = String.valueOf(projectListComboBox.getSelectedItem());

        if(selectedProject != "") {
            ProjectInfoFile project = ProjectInfoCollection.GetProject(Config.getInstance(), selectedProject);
            pluginComboBox.removeAllItems();

            for(ProjectInfoPlugin plugin : project.GetPlugins()){
                pluginComboBox.addItem(plugin.GetName());
            }
        }
    }

    private void setSQLView() {
        isViewSQL = !isViewSQL;

        if(isViewSQL) {
            frame.setBounds(100, 100, 700, 800);
        } else {
            frame.setBounds(100, 100, 400, 800);
        }
    }

    private void queryProject() {
        ProjectInfoFile project = ProjectInfoCollection.GetProject(Config.getInstance(), String.valueOf(projectListComboBox.getSelectedItem()));
        Map<Integer, EASTWebQuery> ewQuery = new HashMap<Integer, EASTWebQuery>();
        String[] indicies = new String[includeIndicesListModel.toArray().length];

        for(int i=0; i < includeIndicesListModel.toArray().length; i++){
            indicies[i] = includeIndicesListModel.get(i);
        }

        for(ProjectInfoSummary summary : project.GetSummaries()) {
            try {
                ewQuery.put(summary.GetID(), EASTWebResults.GetEASTWebQuery(
                        Config.getInstance().getGlobalSchema(),
                        String.valueOf(projectListComboBox.getSelectedItem()),
                        String.valueOf(pluginComboBox.getSelectedItem()),
                        chckbxCount.isSelected(),
                        maxCheckBox.isSelected(),
                        minCheckBox.isSelected(),
                        chckbxSum.isSelected(),
                        chckbxMean.isSelected(),
                        sqrSumCheckBox.isSelected(),
                        chckbxStdev.isSelected(),
                        (String[])includeZoneListModel.toArray(),
                        String.valueOf(yearComboBox.getSelectedItem()),
                        (yearTextField.getText().equals("") ? null : Integer.parseInt(yearTextField.getText())),
                        String.valueOf(dayComboBox.getSelectedItem()),
                        (dayTextField.getText().equals("") ? null : Integer.parseInt(dayTextField.getText())),
                        indicies,
                        new Integer[]{summary.GetID()}));
            } catch (NumberFormatException e) {
                ErrorLog.add(Config.getInstance(), "QueryUI.CreateSQLView problem with getting csv result files.", e);
            }
        }

        if(ewQuery != null){
            String path = JOptionPane.showInputDialog(frame, "Enter file path to write csv file to:");

            if (path != null) {
                try {
                    Iterator<Integer> keysIt = ewQuery.keySet().iterator();
                    Integer key;

                    while(keysIt.hasNext()){
                        key = keysIt.next();
                        String tempPath = path + " - Summary " + key;
                        ArrayList<EASTWebResult> queryResults = EASTWebResults.GetEASTWebResults(ewQuery.get(key));
                        EASTWebResults.WriteEASTWebResultsToCSV(tempPath, queryResults);
                    }
                } catch (IOException | ClassNotFoundException | SQLException | ParserConfigurationException | SAXException e) {
                    JOptionPane.showMessageDialog(frame, "Folder not found");
                    ErrorLog.add(Config.getInstance(), "QueryResultWindow.initialize problem with copying files to different directory.", e);
                }
            }
        }
    }

    private void populateIndicesInfo() {
        String pluginName = String.valueOf(pluginComboBox.getSelectedItem());
        String selectedProject = String.valueOf(projectListComboBox.getSelectedItem());
        ProjectInfoFile project = ProjectInfoCollection.GetProject(Config.getInstance(), String.valueOf(projectListComboBox.getSelectedItem()));

        includeIndicesListModel.removeAllElements();
        excludeIndicesListModel.removeAllElements();
        includeZoneListModel.removeAllElements();
        excludeZoneListModel.removeAllElements();

        for(ProjectInfoPlugin plugin: project.GetPlugins()){
            if( plugin.GetName().equals(pluginName)){
                for(String indice: plugin.GetIndices()) {
                    excludeIndicesListModel.addElement(indice);
                }
            }
        }

        for(String zone: EASTWebResults.GetZonesListFromProject(selectedProject, String.valueOf(pluginComboBox.getSelectedItem()))){
            excludeZoneListModel.addElement(zone);
        }
    }

    class UpdateQuery extends TimerTask {
        @Override
        public void run() {
            sqlViewTextPanel.setText(String.format("%s\n %s\n %s", SelectStatement(), FromStatement(), WhereStatement()));
        }

        private String FromStatement() {
            return String.format("FROM \n \"%s\"\n", String.valueOf(projectListComboBox.getSelectedItem()));
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
            for(int i = 0; i < includeIndicesListModel.size(); i ++){
                query += String.format("\t%s,\n", includeIndicesListModel.elementAt(i));
            }
            return query;
        }

        private String WhereStatement() {
            String query = "";

            if(chckbxZone.isSelected() || chckbxYear.isSelected() || chckbxDay.isSelected()){
                query = String.format("WHERE\n");

                if(chckbxYear.isSelected()){
                    query += String.format("year%s%s\n", String.valueOf(yearComboBox.getSelectedItem()), yearTextField.getText());
                }
                if(chckbxDay.isSelected()){
                    query += String.format("day%s%s\n", String.valueOf(dayComboBox.getSelectedItem()), dayTextField.getText());
                }
                if(chckbxZone.isSelected()) {
                    for(int i = 0; i < includeZoneListModel.size(); i ++){
                        query += String.format("\t%s,\n", includeZoneListModel.elementAt(i));
                    }
                }
            }
            return query;
        }
    }
}