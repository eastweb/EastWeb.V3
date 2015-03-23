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

public class ProjectInformationPage {

    private JFrame frame;
    final int windowHeight = 1000;
    final int windowWidth = 750;
    private JTextField startDate;
    private JTextField projectName;
    private JTextField workingDirectory;
    private JTextField maskFile;

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
        frame.setBounds(100, 100, 1080, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);

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

        createButton.setBounds(804, 21, 175, 25);
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
        panel_3.setBounds(280, 420, 275, 275);
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
        panel_2.setBounds(550, 420, 275, 275);
        frame.getContentPane().add(panel_2);

        JLabel label_4 = new JLabel("Start Date: xx-xx-xxxx");
        label_4.setBounds(6, 16, 245, 14);
        panel_2.add(label_4);

        JLabel label_5 = new JLabel("Project Name: ");
        label_5.setBounds(6, 58, 71, 14);
        panel_2.add(label_5);

        JLabel label_6 = new JLabel("Working Directory: ");
        label_6.setBounds(6, 100, 93, 14);
        panel_2.add(label_6);

        JLabel label_7 = new JLabel("Masking File");
        label_7.setBounds(6, 143, 57, 14);
        panel_2.add(label_7);
    }

    private void SummaryInformation() {
        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);
        panel_1.setBorder(new TitledBorder(null, "Summary Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_1.setBounds(804, 420, 228, 259);
        frame.getContentPane().add(panel_1);

        JButton btnNewButton = new JButton("New button");
        btnNewButton.setBounds(0, 21, 228, 35);
        panel_1.add(btnNewButton);
    }
}
