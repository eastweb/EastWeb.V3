package version2.prototype.EastWebUI.SummaryUI;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import version2.prototype.util.ReadShapefile;
import version2.prototype.util.ShapefileException;

public class AssociateSummaryPage {

    private JFrame frame;
    SummaryEvent summaryEvent;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    AssociateSummaryPage window = new AssociateSummaryPage(null);
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
    public AssociateSummaryPage(SummaryListener l) {
        summaryEvent = new SummaryEvent();
        summaryEvent.addListener(l);
        initialize();
        frame.setVisible(true);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 401, 300);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel myPanel = new JPanel();
        myPanel.setLayout(null);
        myPanel.setBorder(new TitledBorder(null, "Summary Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        myPanel.setBounds(547, 420, 383, 275);

        final JLabel filePathLabel = new JLabel("ShapeFile Path");
        filePathLabel.setBounds(10, 27, 152, 14);
        myPanel.add(filePathLabel);

        // text field for shapefile
        final JTextField filePathText =  new JTextField();
        filePathText.setBounds(172, 24, 150, 20);
        myPanel.add(filePathText);
        filePathText.setColumns(10);

        final JLabel shapeFileLabel = new JLabel("Field Name");
        shapeFileLabel.setBounds(10, 58, 152, 14);
        myPanel.add(shapeFileLabel);

        // combo box for temporal
        final JComboBox<String> temporalComboBox = new JComboBox<String>();
        temporalComboBox.setBounds(172, 86, 150, 20);
        //FIXME:
        temporalComboBox.addItem("GregorianWeeklyStrategy");
        temporalComboBox.addItem("GregorianMonthlyStrategy");
        temporalComboBox.addItem("CDCWeeklyStrategy");
        temporalComboBox.addItem("WHOWeeklyStrategy");
        myPanel.add(temporalComboBox);

        // combo box populated by the selected shapefile
        final JComboBox<String> shapeFileComboBox = new JComboBox<String>();
        shapeFileComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String temporal = String.valueOf(shapeFileComboBox.getSelectedItem());
                if(temporal != null & !temporal.isEmpty()) {
                    temporalComboBox.setEnabled(true);
                }
            }
        });
        shapeFileComboBox.setBounds(172, 55, 150, 20);
        myPanel.add(shapeFileComboBox);



        // browse button for shape file
        final JButton browseButton = new JButton(". . .");
        browseButton.setToolTipText("browse file");
        browseButton.setBounds(344, 23, 41, 23);
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
                    filePathText.setText(chooser.getSelectedFile().toString());
                    try {
                        populateShapeFiles(shapeFileComboBox, filePathText.getText());
                    } catch (ShapefileException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("No Selection ");
                }
            }
        });
        myPanel.add(browseButton);

        JLabel lblNewLabel_1 = new JLabel("Temporal Summary");
        lblNewLabel_1.setBounds(10, 89, 152, 14);
        myPanel.add(lblNewLabel_1);



        temporalComboBox.setEnabled(false);
        filePathLabel.setEnabled(true);
        filePathText.setEnabled(true);
        browseButton.setEnabled(true);

        // save button to save summary
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {

                String summary = "";
                String temporal = String.valueOf(temporalComboBox.getSelectedItem());

                summary = String.format("AreaNameField: %s; Shape File Path: %s; AreaValueField: %s;", String.valueOf(shapeFileComboBox.getSelectedItem()), filePathText.getText(), String.valueOf(shapeFileComboBox.getSelectedItem()));

                if(temporal != null & !temporal.isEmpty() ) {
                    summary = String.format("%s; Temporal Summary: %s",summary, String.valueOf(temporalComboBox.getSelectedItem()));
                }

                summaryEvent.fire(summary);
                frame.dispose();
            }
        });
        saveButton.setBounds(82, 237, 89, 23);
        myPanel.add(saveButton);

        // cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                frame.dispose();
            }
        });
        cancelButton.setBounds(233, 237, 89, 23);
        myPanel.add(cancelButton);

        frame.getContentPane().add(myPanel);
    }

    /**
     *  populate shape file
     * @param shapeFileComboBox
     * @param filePath
     * @throws ShapefileException
     */
    private void populateShapeFiles(JComboBox<String> shapeFileComboBox, String filePath) throws ShapefileException{
        ReadShapefile shapfile = new ReadShapefile(filePath);
        ArrayList<String[]> featureList = shapfile.getFeatureList();

        for (int i = 0; i < featureList.size(); i++) {
            for(String feature: featureList.get(i)){
                shapeFileComboBox.addItem(feature);
            }
        }

        shapeFileComboBox.setEnabled(true);
    }
}
