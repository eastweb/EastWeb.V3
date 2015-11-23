package version2.prototype.EastWebUI_V2.SummaryUI_v2;

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

import version2.prototype.Config;
import version2.prototype.EASTWebManager;
import version2.prototype.ErrorLog;
import version2.prototype.util.ReadShapefile;
import version2.prototype.util.ShapefileException;

public class AssociateSummaryPage {
    private JComboBox<String> areaNameFieldComboBox;
    private JComboBox<String> areaCodeFieldComboBox;
    private JComboBox<String> temporalComboBox;
    private JTextField filePathText;
    private JButton browseButton;
    private JFrame frame;

    private SummaryEvent summaryEvent;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new AssociateSummaryPage(null);
                } catch (Exception e) {
                    ErrorLog.add(Config.getInstance(), "AssociateSummaryPage.main problem running an AssociateSummaryPage window.", e);
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

        frame = new JFrame();
        frame.setVisible(true);
        frame.setBounds(100, 100, 401, 300);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        JPanel myPanel = new JPanel();
        myPanel.setLayout(null);
        myPanel.setBorder(new TitledBorder(null, "Summary Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        myPanel.setBounds(547, 420, 383, 275);

        final JLabel filePathLabel = new JLabel("ShapeFile Path");
        filePathLabel.setBounds(10, 27, 152, 14);
        filePathLabel.setEnabled(true);
        myPanel.add(filePathLabel);
        filePathText =  new JTextField();
        filePathText.setBounds(172, 24, 150, 20);
        filePathText.setEnabled(true);
        filePathText.setColumns(10);
        myPanel.add(filePathText);

        browseButton = new JButton(". . .");
        browseButton.setToolTipText("browse file");
        browseButton.setBounds(344, 23, 41, 23);
        browseButton.setEnabled(true);
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {browserShapeFile();}
        });
        myPanel.add(browseButton);

        final JLabel areaCodeFieldLabel = new JLabel("Area Code Field");
        areaCodeFieldLabel.setBounds(10, 58, 152, 14);
        myPanel.add(areaCodeFieldLabel);
        areaCodeFieldComboBox = new JComboBox<String>();
        areaCodeFieldComboBox.setToolTipText("Area Code is a numeric value");
        areaCodeFieldComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {setTemporalView(areaCodeFieldComboBox.getSelectedItem());}
        });
        areaCodeFieldComboBox.setBounds(172, 55, 150, 20);
        myPanel.add(areaCodeFieldComboBox);

        final JLabel areaNameFieldLabel = new JLabel("Area Name Field");
        areaNameFieldLabel.setBounds(10, 89, 152, 14);
        myPanel.add(areaNameFieldLabel);
        areaNameFieldComboBox = new JComboBox<String>();
        areaNameFieldComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {setTemporalView(areaNameFieldComboBox.getSelectedItem());}
        });
        areaNameFieldComboBox.setBounds(172, 86, 150, 20);
        myPanel.add(areaNameFieldComboBox);

        JLabel temporalLbl = new JLabel("Temporal Summary");
        temporalLbl.setBounds(10, 119, 152, 14);
        myPanel.add(temporalLbl);
        temporalComboBox = new JComboBox<String>();
        temporalComboBox.setBounds(172, 116, 150, 20);
        temporalComboBox.setEnabled(false);
        for(String strategy : EASTWebManager.GetRegisteredTemporalSummaryCompositionStrategies()){
            temporalComboBox.addItem(strategy);
        }
        myPanel.add(temporalComboBox);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {saveSummary();}
        });
        saveButton.setBounds(82, 237, 89, 23);
        myPanel.add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {frame.dispose();}
        });
        cancelButton.setBounds(233, 237, 89, 23);
        myPanel.add(cancelButton);

        frame.getContentPane().add(myPanel);
    }

    private void browserShapeFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Browse the folder to process");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): "+ chooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : "+ chooser.getSelectedFiles());
            filePathText.setText(chooser.getSelectedFile().toString());

            try {
                ReadShapefile shapfile = new ReadShapefile(filePathText.getText());
                populateShapeFiles(areaNameFieldComboBox, shapfile.getNameFeatureList());
                populateShapeFiles(areaCodeFieldComboBox, shapfile.getNumericFeatureList());
            } catch (ShapefileException e) {
                ErrorLog.add(Config.getInstance(), "AssociateSummaryPage.initialize problem with populating shape files.", e);
            }
        } else {
            System.out.println("No Selection ");
        }
    }

    private void saveSummary() {
        String temporal = String.valueOf(temporalComboBox.getSelectedItem());
        String summary = String.format("AreaNameField: %s; Shape File Path: %s; AreaCodeField: %s;",
                String.valueOf(areaNameFieldComboBox.getSelectedItem()),
                filePathText.getText(),
                String.valueOf(areaCodeFieldComboBox.getSelectedItem()));

        if(temporal != null && !temporal.isEmpty()) {
            summary = String.format("%s Temporal Summary: %s", summary, String.valueOf(temporalComboBox.getSelectedItem()));
        }

        summaryEvent.fire(summary);
        frame.dispose();
    }

    private void setTemporalView(Object selectedItem) {
        String temporal = String.valueOf(selectedItem);

        if(temporal != null & !temporal.isEmpty()) {
            temporalComboBox.setEnabled(true);
        }
    }

    private void populateShapeFiles(JComboBox<String> shapeFileComboBox, ArrayList<String[]> featureList) throws ShapefileException{
        for (int i = 0; i < featureList.size(); i++) {
            for(String feature: featureList.get(i)){
                shapeFileComboBox.addItem(feature);
            }
        }
        shapeFileComboBox.setEnabled(true);
    }
}