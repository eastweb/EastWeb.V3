package version2.prototype.EastWebUI.ProjectInformationUI;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JScrollPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.EastWebUI.MainWindow.MainWindowEvent;
import version2.prototype.EastWebUI.MainWindow.MainWindowListener;
import version2.prototype.EastWebUI.PluginIndiciesUI.AssociatePluginPage;
import version2.prototype.EastWebUI.PluginIndiciesUI.IndiciesEventObject;
import version2.prototype.EastWebUI.PluginIndiciesUI.IndiciesListener;
import version2.prototype.EastWebUI.SummaryUI.AssociateSummaryPage;
import version2.prototype.EastWebUI.SummaryUI.SummaryEventObject;
import version2.prototype.EastWebUI.SummaryUI.SummaryListener;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.toedter.calendar.JDateChooser;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

public class ProjectInformationPage {

    private JFrame frame;
    final int windowHeight = 1000;
    final int windowWidth = 750;
    private JDateChooser  startDate;
    private JTextField projectName;
    private JTextField workingDirectory;
    private JTextField maskFile;
    private JTextField pixelSize;
    private JTextField standardParallel1;
    private JTextField centalMeridian;
    private JTextField falseEasting;
    private JTextField standardParallel2;
    private JTextField latitudeOfOrigin;
    private JTextField falseNothing;
    private JComboBox<String> coordinateSystemComboBox;
    private JComboBox<String> reSamplingComboBox;
    private JComboBox<String> datumComboBox;
    private boolean isEditable;
    private JComboBox<String> projectCollectionComboBox;
    private JTextField masterShapeTextField;

    MainWindowEvent mainWindowEvent;

    DefaultListModel<String> listOfAddedPluginModel;
    DefaultListModel<String> summaryListModel;
    DefaultListModel<String> modisListModel;


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ProjectInformationPage window =  new ProjectInformationPage(true, null);
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
    public ProjectInformationPage(boolean isEditable,  MainWindowListener l) {
        this.isEditable = isEditable;
        mainWindowEvent = new MainWindowEvent();
        mainWindowEvent.addListener(l);
        initialize();
        frame.setVisible(true);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 1207, 730);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);

        PopulatePluginList();
        CreateNewProjectButton();
        RenderInformationGrid();

        if(!isEditable)
        {
            projectCollectionComboBox = new JComboBox();
            projectCollectionComboBox.setBounds(507, 15, 229, 20);
            frame.getContentPane().add(projectCollectionComboBox);

            File fileDir = new File(System.getProperty("user.dir") + "\\src\\version2\\prototype\\ProjectInfoMetaData\\");

            for(File fXmlFile: getXMLFiles(fileDir)){
                projectCollectionComboBox.addItem(fXmlFile.getName().replace(".xml", ""));
            }
        }
    }

    private void CreateNewProjectButton() {
        JButton createButton = new JButton(isEditable ? "Create New Project" : "Save Project");

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                CreateNewProject();
                JOptionPane.showMessageDialog(frame, "Project was saved");
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });

        createButton.setBounds(1017, 11, 175, 25);
        frame.getContentPane().add(createButton);
    }

    private void PopulatePluginList() {
        listOfAddedPluginModel = new DefaultListModel<String>();

        final JList<String> listOfAddedPlugin = new JList<String>(listOfAddedPluginModel);
        listOfAddedPlugin.setBorder(new EmptyBorder(10,10, 10, 10));

        JButton addPluginButton = new JButton("");
        addPluginButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        addPluginButton.setToolTipText("Add Plugin");
        addPluginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    new AssociatePluginPage(new indiciesListenerImplementation());
                } catch (ParserConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SAXException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        addPluginButton.setBounds(10, 12, 34, 23);
        frame.getContentPane().add(addPluginButton);

        JButton deletePluginButton = new JButton("");
        deletePluginButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/ChangeQueryType_deletequery_274.png")));
        deletePluginButton.setToolTipText("Delete Plugin");
        deletePluginButton.addActionListener(new ActionListener() {
            @SuppressWarnings("rawtypes")
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel model = (DefaultListModel) listOfAddedPlugin.getModel();
                int selectedIndex = listOfAddedPlugin.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
            }
        });
        deletePluginButton.setBounds(54, 12, 34, 23);
        frame.getContentPane().add(deletePluginButton);

        JScrollPane scrollPane = new JScrollPane(listOfAddedPlugin);
        scrollPane.setBounds(10, 40, 1182, 369);
        frame.getContentPane().add(scrollPane);
    }

    private void RenderInformationGrid() {
        BasicProjectInformation();
        ModisInformation();
        ProjectInformation();
        SummaryInformation();
    }

    private void BasicProjectInformation() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Basic Project Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setBounds(10, 420, 358, 259);
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setBounds(6, 25, 132, 15);
        panel.add(startDateLabel);
        startDate = new JDateChooser ();
        //startDate.setColumns(10);
        startDate.setBounds(148, 22, 200, 20);
        panel.add(startDate);

        JLabel projectNameLabel = new JLabel("Project Name: ");
        projectNameLabel.setBounds(6, 56, 136, 14);
        panel.add(projectNameLabel);
        projectName = new JTextField();
        projectName.setBounds(148, 53, 200, 20);
        panel.add(projectName);
        projectName.setColumns(10);

        JLabel workingDirLabel = new JLabel("Working Dir: ");
        workingDirLabel.setBounds(6, 87, 132, 15);
        panel.add(workingDirLabel);
        workingDirectory = new JTextField();
        workingDirectory.setColumns(10);
        workingDirectory.setBounds(148, 84, 158, 20);
        panel.add(workingDirectory);

        JButton workingDirBrowsebutton = new JButton(". . .");
        workingDirBrowsebutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File("."));
                chooser.setDialogTitle("Browse the folder to process");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    System.out.println("getCurrentDirectory(): "+ chooser.getCurrentDirectory());
                    System.out.println("getSelectedFile() : "+ chooser.getSelectedFile());
                    workingDirectory.setText(chooser.getSelectedFile().toString());
                } else {
                    System.out.println("No Selection ");
                }
            }
        });

        workingDirBrowsebutton.setBounds(316, 84, 32, 20);
        panel.add(workingDirBrowsebutton);

        JLabel maskingFileLabel = new JLabel("Masking File");
        maskingFileLabel.setBounds(6, 118, 132, 15);
        panel.add(maskingFileLabel);
        maskFile = new JTextField();
        maskFile.setColumns(10);
        maskFile.setBounds(148, 115, 158, 20);
        panel.add(maskFile);

        JButton maskFileBrowseButton = new JButton(". . .");
        maskFileBrowseButton.addActionListener(new ActionListener() {
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
                    maskFile.setText(chooser.getSelectedFile().toString());
                } else {
                    System.out.println("No Selection ");
                }
            }
        });
        maskFileBrowseButton.setBounds(316, 115, 32, 20);
        panel.add(maskFileBrowseButton);

        masterShapeTextField = new JTextField();
        masterShapeTextField.setBounds(148, 141, 158, 20);
        panel.add(masterShapeTextField);
        masterShapeTextField.setColumns(10);
        masterShapeTextField.setEditable(false);

        final JButton masterShapeFileBrowseButton = new JButton(". . .");
        masterShapeFileBrowseButton.addActionListener(new ActionListener() {
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
                    masterShapeTextField.setText(chooser.getSelectedFile().toString());
                } else {
                    System.out.println("No Selection ");
                }
            }
        });
        masterShapeFileBrowseButton.setBounds(316, 141, 32, 20);
        panel.add(masterShapeFileBrowseButton);
        masterShapeFileBrowseButton.setEnabled(false);

        final JCheckBox chmasterShapeFileCheckbox = new JCheckBox("Master shp file");
        chmasterShapeFileCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(chmasterShapeFileCheckbox.isSelected()){
                    masterShapeTextField.setEditable(true);
                    masterShapeFileBrowseButton.setEnabled(true);
                }
                else{
                    masterShapeTextField.setEditable(false);
                    masterShapeFileBrowseButton.setEnabled(false);
                }
            }
        });
        chmasterShapeFileCheckbox.setBounds(6, 140, 136, 23);
        panel.add(chmasterShapeFileCheckbox);
    }

    @SuppressWarnings("rawtypes")
    private void ModisInformation() {
        JPanel modisInformationPanel = new JPanel();
        modisInformationPanel.setLayout(null);
        modisInformationPanel.setBorder(new TitledBorder(null, "Modis Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        modisInformationPanel.setBounds(359, 420, 275, 259);
        frame.getContentPane().add(modisInformationPanel);

        modisListModel = new DefaultListModel<String>();

        final JList<String> modisList = new JList<String>(modisListModel);
        modisList.setBounds(15, 70, 245, 178);

        JButton addNewModisButton = new JButton("");
        addNewModisButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        addNewModisButton.setToolTipText("Add modis");
        addNewModisButton.addActionListener(new ActionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String tile = JOptionPane.showInputDialog(frame,"Enter Modis Tiles", null);
                modisListModel.addElement(tile);
            }
        });
        addNewModisButton.setBounds(15, 29, 75, 30);
        modisInformationPanel.add(addNewModisButton);
        modisInformationPanel.add(modisList);

        JButton btnDeleteSelected = new JButton("");
        btnDeleteSelected.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/ChangeQueryType_deletequery_274.png")));
        btnDeleteSelected.setToolTipText("Delete Selected Modis");
        btnDeleteSelected.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel model = (DefaultListModel) modisList.getModel();
                int selectedIndex = modisList.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
            }
        });
        btnDeleteSelected.setBounds(185, 29, 75, 30);
        modisInformationPanel.add(btnDeleteSelected);
    }

    private void ProjectInformation() {
        JPanel panel_2 = new JPanel();
        panel_2.setLayout(null);
        panel_2.setBorder(new TitledBorder(null, "Projection Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_2.setBounds(633, 420, 297, 259);
        frame.getContentPane().add(panel_2);

        JLabel coordinateSystemLabel = new JLabel("Coordinate System:");
        coordinateSystemLabel.setBounds(6, 16, 134, 14);
        panel_2.add(coordinateSystemLabel);
        coordinateSystemComboBox = new JComboBox<String>();
        coordinateSystemComboBox.setBounds(146, 13, 140, 20);
        coordinateSystemComboBox.addItem("ALBERS_EQUAL_AREA");
        coordinateSystemComboBox.addItem("LAMBERT_CONFORMAL_CONIC");
        coordinateSystemComboBox.addItem("TRANSVERSE_MERCATOR");
        panel_2.add(coordinateSystemComboBox);

        JLabel reSamplingLabel = new JLabel("Re-sampling Type:");
        reSamplingLabel.setBounds(6, 41, 109, 14);
        panel_2.add(reSamplingLabel);
        reSamplingComboBox = new JComboBox<String>();
        reSamplingComboBox.setBounds(146, 38, 140, 20);
        reSamplingComboBox.addItem("NEAREST_NEIGHBOR");
        reSamplingComboBox.addItem("BILINEAR");
        reSamplingComboBox.addItem("CUBIC_CONVOLUTION");
        panel_2.add(reSamplingComboBox);

        JLabel datumLabel = new JLabel("Datum:");
        datumLabel.setBounds(6, 66, 109, 14);
        panel_2.add(datumLabel);
        datumComboBox = new JComboBox<String>();
        datumComboBox.setBounds(146, 63, 140, 20);
        datumComboBox.addItem("NAD83");
        datumComboBox.addItem("NAD27");
        datumComboBox.addItem("WGS84");
        datumComboBox.addItem("WGS72");
        panel_2.add(datumComboBox);

        JLabel pixelSizeLabel = new JLabel("Pixel size meters:");
        pixelSizeLabel.setBounds(6, 91, 109, 14);
        panel_2.add(pixelSizeLabel);
        pixelSize = new JTextField();
        pixelSize.setColumns(10);
        pixelSize.setBounds(146, 90, 140, 16);
        panel_2.add(pixelSize);

        JLabel standardParallel1label = new JLabel("Standard parallel 1");
        standardParallel1label.setBounds(6, 116, 109, 14);
        panel_2.add(standardParallel1label);
        standardParallel1 = new JTextField();
        standardParallel1.setColumns(10);
        standardParallel1.setBounds(146, 115, 140, 16);
        panel_2.add(standardParallel1);

        JLabel centalMeridianLabel = new JLabel("Cental meridian");
        centalMeridianLabel.setBounds(6, 141, 109, 14);
        panel_2.add(centalMeridianLabel);
        centalMeridian = new JTextField();
        centalMeridian.setColumns(10);
        centalMeridian.setBounds(146, 140, 140, 16);
        panel_2.add(centalMeridian);

        JLabel falseEastingLabel = new JLabel("False easting");
        falseEastingLabel.setBounds(6, 166, 109, 14);
        panel_2.add(falseEastingLabel);
        falseEasting = new JTextField();
        falseEasting.setColumns(10);
        falseEasting.setBounds(146, 165, 140, 16);
        panel_2.add(falseEasting);

        JLabel standardParallel2Label = new JLabel("Standard parallel 2");
        standardParallel2Label.setBounds(6, 191, 109, 14);
        panel_2.add(standardParallel2Label);
        standardParallel2 = new JTextField();
        standardParallel2.setColumns(10);
        standardParallel2.setBounds(146, 192, 140, 16);
        panel_2.add(standardParallel2);

        JLabel latitudeOfOriginLabel = new JLabel("Latitude of origin");
        latitudeOfOriginLabel.setBounds(6, 216, 109, 14);
        panel_2.add(latitudeOfOriginLabel);
        latitudeOfOrigin = new JTextField();
        latitudeOfOrigin.setColumns(10);
        latitudeOfOrigin.setBounds(146, 215, 140, 16);
        panel_2.add(latitudeOfOrigin);

        JLabel falseNothingLabel = new JLabel("False nothing");
        falseNothingLabel.setBounds(6, 241, 109, 14);
        panel_2.add(falseNothingLabel);
        falseNothing = new JTextField();
        falseNothing.setColumns(10);
        falseNothing.setBounds(146, 240, 140, 16);
        panel_2.add(falseNothing);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void SummaryInformation() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(null);
        summaryPanel.setBorder(new TitledBorder(null, "Summary Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        summaryPanel.setBounds(926, 420, 275, 259);
        frame.getContentPane().add(summaryPanel);

        summaryListModel = new DefaultListModel();
        final JList summaryList = new JList(summaryListModel);
        summaryList.setBounds(15, 70, 245, 178);

        JButton editSummaryButton = new JButton("");
        editSummaryButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        editSummaryButton.setToolTipText("Add summary");
        editSummaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new AssociateSummaryPage(new summaryListenerImplementation());
            }
        });
        editSummaryButton.setBounds(15, 29, 75, 30);
        summaryPanel.add(editSummaryButton);
        summaryPanel.add(summaryList);

        JButton deleteSummaryButton = new JButton("");
        deleteSummaryButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/ChangeQueryType_deletequery_274.png")));
        deleteSummaryButton.setToolTipText("Delete Selected Summary");
        deleteSummaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel model = (DefaultListModel) summaryList.getModel();
                int selectedIndex = summaryList.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
            }
        });
        deleteSummaryButton.setBounds(185, 29, 75, 30);
        summaryPanel.add(deleteSummaryButton);
    }

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

    private String getFileExtensionName(File f) {
        if (f.getName().indexOf(".") == -1) {
            return "";
        } else {
            return f.getName().substring(f.getName().length() - 3, f.getName().length());
        }
    }

    private void CreateNewProject(){

        File theDir = new File(System.getProperty("user.dir") + "\\src\\version2\\prototype\\ProjectInfoMetaData\\" + projectName.getText() + ".xml" );

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element projectInfo = doc.createElement("ProjectInfo");
            doc.appendChild(projectInfo);

            // Plugin elements
            Element plugins = doc.createElement("Plugins");
            projectInfo.appendChild(plugins);

            //list of plugin associate to project
            for(Object item:listOfAddedPluginModel.toArray()){
                Element plugin = doc.createElement("Plugin");

                // set attribute to staff element
                Attr attr = doc.createAttribute("name");
                String noFormat = item.toString().replaceAll("<html>Plugin: ","");
                noFormat = noFormat.replaceAll("<br>Indicies: ", "");
                noFormat = noFormat.replaceAll("<br>Quality: ","");
                noFormat = noFormat.replaceAll("</html>", "");
                noFormat = noFormat.replaceAll("</span>", "");
                noFormat = noFormat.replaceAll("<span>", "");

                String[] array = noFormat.split(";");

                attr.setValue(array[0].toString());
                plugin.setAttributeNode(attr);

                if(array.length < 3){
                    // start Date
                    Element qc = doc.createElement("QC");
                    qc.appendChild(doc.createTextNode(array[1].toString()));
                    plugin.appendChild(qc);
                }
                else{
                    for(int i = 1; i < array.length -1; i++){
                        Element indicies = doc.createElement("Indicies");
                        indicies.appendChild(doc.createTextNode(array[i].toString()));
                        plugin.appendChild(indicies);
                    }

                    Element qc = doc.createElement("QC");
                    qc.appendChild(doc.createTextNode(array[array.length - 1].toString()));
                    plugin.appendChild(qc);
                }

                // add a new node for plugin element
                plugins.appendChild(plugin);
            }

            // start Date
            Element startDate = doc.createElement("StartDate");
            startDate.appendChild(doc.createTextNode(this.startDate.getDate().toString()));
            projectInfo.appendChild(startDate);

            // project name
            Element projectName = doc.createElement("ProjectName");
            projectName.appendChild(doc.createTextNode(this.projectName.getText()));
            projectInfo.appendChild(projectName);

            // working directory
            Element workingDirectory = doc.createElement("WorkingDir");
            workingDirectory.appendChild(doc.createTextNode(this.workingDirectory.getText()));
            projectInfo.appendChild(workingDirectory);

            // masking file
            Element maskingFile = doc.createElement("MaskingFile");
            maskingFile.appendChild(doc.createTextNode(maskFile.getText()));
            projectInfo.appendChild(maskingFile);

            Element masterShapeFile = doc.createElement("MasterShapeFile");
            masterShapeFile.appendChild(doc.createTextNode(masterShapeTextField.getText()));
            projectInfo.appendChild(masterShapeFile);

            //list of modis tiles
            Element modisTiles = doc.createElement("ModisTiles");
            projectInfo.appendChild(modisTiles);

            for(Object item:modisListModel.toArray()){
                Element element = doc.createElement("Modis");
                element.appendChild(doc.createTextNode(item.toString()));
                modisTiles.appendChild(element);
            }

            // Coordinate System
            Element coordinateSystem = doc.createElement("CoordinateSystem");
            coordinateSystem.appendChild(doc.createTextNode(String.valueOf(coordinateSystemComboBox.getSelectedItem())));
            projectInfo.appendChild(coordinateSystem);

            // resampling
            Element reSampling = doc.createElement("ReSampling");
            reSampling.appendChild(doc.createTextNode(String.valueOf(reSamplingComboBox.getSelectedItem())));
            projectInfo.appendChild(reSampling);

            //datum
            Element datum = doc.createElement("Datum");
            datum.appendChild(doc.createTextNode(String.valueOf(datumComboBox.getSelectedItem())));
            projectInfo.appendChild(datum);

            //datum
            Element pixelSize = doc.createElement("Datum");
            pixelSize.appendChild(doc.createTextNode(String.valueOf(this.pixelSize.getText())));
            projectInfo.appendChild(pixelSize);


            //Standard Parallel1
            Element standardParallel1 = doc.createElement("StandardParallel1");
            standardParallel1.appendChild(doc.createTextNode(String.valueOf(this.standardParallel1.getText())));
            projectInfo.appendChild(standardParallel1);


            //cental Meridian
            Element centalMeridian = doc.createElement("CentalMeridian");
            centalMeridian.appendChild(doc.createTextNode(String.valueOf(this.centalMeridian.getText())));
            projectInfo.appendChild(centalMeridian);

            //false Easting
            Element falseEasting = doc.createElement("FalseEasting");
            falseEasting.appendChild(doc.createTextNode(String.valueOf(this.falseEasting.getText())));
            projectInfo.appendChild(falseEasting);

            //standard Parallel2
            Element standardParallel2 = doc.createElement("StandardParallel2");
            standardParallel2.appendChild(doc.createTextNode(String.valueOf(this.standardParallel2.getText())));
            projectInfo.appendChild(standardParallel2);

            //latitude Of Origin
            Element latitudeOfOrigin = doc.createElement("LatitudeOfOrigin");
            latitudeOfOrigin.appendChild(doc.createTextNode(String.valueOf(this.latitudeOfOrigin.getText())));
            projectInfo.appendChild(latitudeOfOrigin);

            //false Nothing
            Element falseNothing = doc.createElement("FalseNothing");
            falseNothing.appendChild(doc.createTextNode(String.valueOf(this.falseNothing.getText())));
            projectInfo.appendChild(falseNothing);

            //list of summary tiles
            Element summaries = doc.createElement("Summaries");
            projectInfo.appendChild(summaries);

            for(Object item:modisListModel.toArray()){
                Element summary = doc.createElement("Summary");
                summary.appendChild(doc.createTextNode(item.toString()));
                summaries.appendChild(summary);
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(theDir);

            transformer.transform(source, result);
            System.out.println("File saved!");
            mainWindowEvent.fire();

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    class indiciesListenerImplementation implements IndiciesListener{

        @Override
        public void AddPlugin(IndiciesEventObject e) {
            listOfAddedPluginModel.addElement(e.getPlugin());
        }
    }

    class summaryListenerImplementation implements SummaryListener{
        @Override
        public void AddSummary(SummaryEventObject e) {
            summaryListModel.addElement(e.getPlugin());
        }
    }
}
