package version2.prototype.EastWebUI;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.BorderLayout;

import javax.swing.JButton;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JComboBox;

public class ProjectInformationPage {

    private JFrame frame;
    final int windowHeight = 1000;
    final int windowWidth = 750;
    private JTextField startDate;
    private JTextField projectName;
    private JTextField workingDirectory;
    private JTextField maskFile;
    private JTextField textField;
    private JTextField textField_1;
    private JTextField textField_2;
    private JTextField textField_3;
    private JTextField textField_4;
    private JTextField textField_5;
    private JTextField textField_6;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ProjectInformationPage window =
                            new ProjectInformationPage();
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

        JList list_1 = new JList();
        list_1.setBounds(10, 40, 1182, 369);
        frame.getContentPane().add(list_1);

        CreateNewProjectButton();
        PopulatePluginList();
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
            }
        });

        createButton.setBounds(1017, 11, 175, 25);
        frame.getContentPane().add(createButton);
    }

    private void PopulatePluginList() {
        String[] columnNames = {"Plugin Name", "Indicies Calculator", "Quality Control", "Vegetarian"};

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

        //JTable table = new JTable(data, columnNames);
        //table.setFillsViewportHeight(true);

        //JScrollPane scrollPane = new JScrollPane(table);
        //scrollPane.setBounds(10, 50, windowHeight - 25, 300);
        //frame.getContentPane().add(scrollPane);
    }

    private void RenderInformationGrid() {

        BasicProjectInformation();

        SummaryInformation();

        ProjectInformation();

        ModisInformation();
    }

    private void BasicProjectInformation() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Basic Project Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setBounds(10, 420, 275, 275);
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        JLabel lblNewLabel = new JLabel("Start Date:");
        lblNewLabel.setBounds(6, 25, 100, 15);
        panel.add(lblNewLabel);
        startDate = new JTextField();
        startDate.setColumns(10);
        startDate.setBounds(115, 25, 140, 30);
        panel.add(startDate);

        JLabel lblNewLabel_1 = new JLabel("Project Name: ");
        lblNewLabel_1.setBounds(6, 75, 100, 14);
        panel.add(lblNewLabel_1);
        projectName = new JTextField();
        projectName.setBounds(115, 75, 140, 30);
        panel.add(projectName);
        projectName.setColumns(10);

        JLabel lblNewLabel_2 = new JLabel("Working Dir: ");
        lblNewLabel_2.setBounds(6, 125, 100, 15);
        panel.add(lblNewLabel_2);
        workingDirectory = new JTextField();
        workingDirectory.setColumns(10);
        workingDirectory.setBounds(115, 125, 140, 30);
        panel.add(workingDirectory);

        JLabel lblNewLabel_3 = new JLabel("Masking File");
        lblNewLabel_3.setBounds(6, 175, 100, 15);
        panel.add(lblNewLabel_3);
        maskFile = new JTextField();
        maskFile.setColumns(10);
        maskFile.setBounds(115, 175, 140, 30);
        panel.add(maskFile);
    }

    private void ModisInformation() {
        JPanel panel_3 = new JPanel();
        panel_3.setLayout(null);
        panel_3.setBorder(new TitledBorder(null, "Modis Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_3.setBounds(276, 420, 275, 275);
        frame.getContentPane().add(panel_3);

        JButton btnNewButton_1 = new JButton("Edit Modis Tiles");
        btnNewButton_1.setBounds(15, 20, 245, 30);
        panel_3.add(btnNewButton_1);

        JList list = new JList();
        list.setBounds(15, 70, 245, 180);
        panel_3.add(list);
    }

    private void ProjectInformation() {
        JPanel panel_2 = new JPanel();
        panel_2.setLayout(null);
        panel_2.setBorder(new TitledBorder(null, "Projection Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_2.setBounds(547, 420, 383, 275);
        frame.getContentPane().add(panel_2);

        JLabel label_4 = new JLabel("Coordinate System:");
        label_4.setBounds(6, 16, 134, 14);
        panel_2.add(label_4);

        JLabel label_5 = new JLabel("Re-sampling Type:");
        label_5.setBounds(6, 41, 109, 14);
        panel_2.add(label_5);

        JLabel label_6 = new JLabel("Datum:");
        label_6.setBounds(6, 66, 109, 14);
        panel_2.add(label_6);

        JLabel label_7 = new JLabel("Pixel size meters:");
        label_7.setBounds(6, 91, 109, 14);
        panel_2.add(label_7);

        JLabel label = new JLabel("Standard parallel 1");
        label.setBounds(6, 116, 109, 14);
        panel_2.add(label);

        JLabel label_1 = new JLabel("Cental meridian");
        label_1.setBounds(6, 141, 109, 14);
        panel_2.add(label_1);

        JLabel label_2 = new JLabel("False easting");
        label_2.setBounds(6, 166, 109, 14);
        panel_2.add(label_2);

        textField = new JTextField();
        textField.setColumns(10);
        textField.setBounds(146, 90, 140, 16);
        panel_2.add(textField);

        textField_1 = new JTextField();
        textField_1.setColumns(10);
        textField_1.setBounds(146, 115, 71, 16);
        panel_2.add(textField_1);

        textField_2 = new JTextField();
        textField_2.setColumns(10);
        textField_2.setBounds(146, 140, 71, 16);
        panel_2.add(textField_2);

        textField_3 = new JTextField();
        textField_3.setColumns(10);
        textField_3.setBounds(146, 165, 71, 16);
        panel_2.add(textField_3);

        JComboBox comboBox = new JComboBox();
        comboBox.setBounds(146, 13, 140, 20);
        panel_2.add(comboBox);

        JComboBox comboBox_1 = new JComboBox();
        comboBox_1.setBounds(146, 38, 140, 20);
        panel_2.add(comboBox_1);

        JComboBox comboBox_2 = new JComboBox();
        comboBox_2.setBounds(146, 63, 140, 20);
        panel_2.add(comboBox_2);

        JLabel label_3 = new JLabel("Standard parallel 2");
        label_3.setBounds(6, 191, 109, 14);
        panel_2.add(label_3);

        JLabel label_8 = new JLabel("Latitude of origin");
        label_8.setBounds(6, 216, 109, 14);
        panel_2.add(label_8);

        JLabel label_9 = new JLabel("False nothing");
        label_9.setBounds(6, 241, 109, 14);
        panel_2.add(label_9);

        textField_4 = new JTextField();
        textField_4.setColumns(10);
        textField_4.setBounds(146, 192, 71, 16);
        panel_2.add(textField_4);

        textField_5 = new JTextField();
        textField_5.setColumns(10);
        textField_5.setBounds(146, 215, 71, 16);
        panel_2.add(textField_5);

        textField_6 = new JTextField();
        textField_6.setColumns(10);
        textField_6.setBounds(146, 240, 71, 16);
        panel_2.add(textField_6);
    }

    private void SummaryInformation() {
        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);
        panel_1.setBorder(new TitledBorder(null, "Summary Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_1.setBounds(921, 420, 275, 275);
        frame.getContentPane().add(panel_1);

        JButton btnNewButton_1 = new JButton("Edit Summary");
        btnNewButton_1.setBounds(15, 20, 245, 30);
        panel_1.add(btnNewButton_1);

        JList list = new JList();
        list.setBounds(15, 70, 245, 180);
        panel_1.add(list);
    }
}
