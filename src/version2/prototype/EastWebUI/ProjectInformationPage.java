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

public class ProjectInformationPage {

    private JFrame frame;
    final int windowHeight = 1000;
    final int windowWidth = 750;

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
        frame.setBounds(100, 100, windowHeight, windowWidth);
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

        createButton.setBounds(windowHeight - 200, 10, 175, 25);
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

        JTable table = new JTable(data, columnNames);
        table.setFillsViewportHeight(true);

        //JScrollPane scrollPane = new JScrollPane(table);
        //scrollPane.setBounds(10, 50, windowHeight - 25, 300);
        //frame.getContentPane().add(scrollPane);
    }

    private void RenderInformationGrid() {

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "JPanel title", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setBounds(10, 514, 283, 164);
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        JLabel lblNewLabel = new JLabel("Start Date: xx-xx-xxxx");
        lblNewLabel.setBounds(6, 16, 113, 14);
        panel.add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("Project Name: ");
        lblNewLabel_1.setBounds(6, 58, 71, 14);
        panel.add(lblNewLabel_1);

        JLabel lblNewLabel_2 = new JLabel("Working Directory: ");
        lblNewLabel_2.setBounds(6, 100, 93, 14);
        panel.add(lblNewLabel_2);

        JLabel lblNewLabel_3 = new JLabel("Masking File");
        lblNewLabel_3.setBounds(6, 143, 57, 14);
        panel.add(lblNewLabel_3);
    }
}
