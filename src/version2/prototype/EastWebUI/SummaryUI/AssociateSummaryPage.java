package version2.prototype.EastWebUI.SummaryUI;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.ogr;
import org.gdal.ogr.Layer;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.DataSource;

import version2.prototype.util.GdalUtils;


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

        final JLabel filePathLabel = new JLabel("File Path");
        filePathLabel.setBounds(10, 60, 152, 14);
        myPanel.add(filePathLabel);

        final JTextField filePathText =  new JTextField();
        filePathText.setBounds(172, 57, 150, 20);
        myPanel.add(filePathText);
        filePathText.setColumns(10);

        final JLabel shapeFileLabel = new JLabel("Shape");
        shapeFileLabel.setBounds(20, 95, 92, 26);
        myPanel.add(shapeFileLabel);

        final JComboBox shapeFileComboBox = new JComboBox();
        shapeFileComboBox.setBounds(172, 92, 44, 32);
        myPanel.add(shapeFileComboBox);

        final JButton browseButton = new JButton(". . .");
        browseButton.setToolTipText("browse file");
        browseButton.setBounds(344, 56, 41, 23);
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
                    populateShapeFiles(shapeFileComboBox, filePathText.getText());
                } else {
                    System.out.println("No Selection ");
                }
            }
        });
        myPanel.add(browseButton);

        JLabel lblNewLabel_1 = new JLabel("Temporal Summary");
        lblNewLabel_1.setBounds(10, 128, 152, 14);
        myPanel.add(lblNewLabel_1);

        final JComboBox<String> temporalComboBox = new JComboBox<String>();
        temporalComboBox.setBounds(172, 125, 150, 20);
        temporalComboBox.addItem("Temporal Summary 1");
        temporalComboBox.addItem("Temporal Summary 2" );
        myPanel.add(temporalComboBox);

        final JComboBox<String> summaryComboBox = new JComboBox<String>();
        summaryComboBox.setBounds(10, 29, 161, 20);
        summaryComboBox.addItem("Zonal Summary");
        summaryComboBox.addItem("Temporal Summary" );
        summaryComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String selectedItem = String.valueOf(summaryComboBox.getSelectedItem());
                if(selectedItem == "Zonal Summary"){
                    temporalComboBox.setEnabled(false);
                    filePathLabel.setEnabled(true);
                    filePathText.setEnabled(true);
                    browseButton.setEnabled(true);
                    shapeFileComboBox.setEnabled(true);
                    shapeFileLabel.setEnabled(true);
                }else if(selectedItem == "Temporal Summary"){
                    temporalComboBox.setEnabled(true);
                    filePathLabel.setEnabled(false);
                    filePathText.setEnabled(false);
                    browseButton.setEnabled(false);
                    shapeFileComboBox.setEnabled(false);
                    shapeFileLabel.setEnabled(false);
                }
            }
        });
        myPanel.add(summaryComboBox);

        temporalComboBox.setEnabled(false);
        filePathLabel.setEnabled(true);
        filePathText.setEnabled(true);
        browseButton.setEnabled(true);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String selectedItem = String.valueOf(summaryComboBox.getSelectedItem());
                String summary = "";
                if(selectedItem == "Zonal Summary"){
                    summary = String.format("Shape File Path: %s; %s", filePathText.getText(), String.valueOf(shapeFileComboBox.getSelectedItem()));
                }else if(selectedItem == "Temporal Summary"){
                    summary = String.format("Temporal Summary Type: %s", String.valueOf(temporalComboBox.getSelectedItem()));
                }
                summaryEvent.fire(summary);
                frame.dispose();
            }
        });
        saveButton.setBounds(82, 237, 89, 23);
        myPanel.add(saveButton);

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

    private void populateShapeFiles(JComboBox shapeFileComboBox, String filePath){

        //GdalUtils.register();
        DataSource shapefile;


        shapefile = ogr.Open(filePath);


        if(shapefile != null){
            synchronized (GdalUtils.lockObject) {
                for (int iLayer=0; iLayer<shapefile.GetLayerCount(); iLayer++) {
                    Layer layer = shapefile.GetLayer(iLayer);
                    FeatureDefn layerDefn = layer.GetLayerDefn();

                    shapeFileComboBox.removeAll();
                    for (int iFeature=0; iFeature<layerDefn.GetFieldCount(); iFeature++) {
                        FieldDefn fieldDefn = layerDefn.GetFieldDefn(iFeature);
                        String type = fieldDefn.GetFieldTypeName(fieldDefn.GetFieldType()).toLowerCase();
                        if (type.equals("string") || type.equals("integer")) { // FIXME: test! does this always work?
                            shapeFileComboBox.addItem(layerDefn.GetFieldDefn(iFeature).GetName());
                        }
                    }
                }
            }
        }

        shapeFileComboBox.setEnabled(true);
    }
}
