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
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import version2.prototype.ModisTile;
import version2.prototype.EastWebUI.MainWindow.MainWindowEvent;
import version2.prototype.EastWebUI.MainWindow.MainWindowListener;
import version2.prototype.EastWebUI.PluginIndiciesUI.AssociatePluginPage;
import version2.prototype.EastWebUI.PluginIndiciesUI.IndiciesEventObject;
import version2.prototype.EastWebUI.PluginIndiciesUI.IndiciesListener;
import version2.prototype.EastWebUI.SummaryUI.AssociateSummaryPage;
import version2.prototype.EastWebUI.SummaryUI.SummaryEventObject;
import version2.prototype.EastWebUI.SummaryUI.SummaryListener;
import version2.prototype.ProjectInfoMetaData.ProjectInfoCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;

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
    private JComboBox<String> timeZoneComboBox;
    private JComboBox<String> coordinateSystemComboBox;
    private JComboBox<String> reSamplingComboBox;
    private JComboBox<String> datumComboBox;
    private JComboBox<String> projectCollectionComboBox;
    private JTextField masterShapeTextField;
    private JButton addNewModisButton;
    private JButton deleteSelectedModisButton;
    private  JCheckBox isClippingCheckBox;
    private JDateChooser freezingDateChooser;
    private JDateChooser heatingDateChooser;

    private boolean isEditable;

    private MainWindowEvent mainWindowEvent;

    private DefaultListModel<String> listOfAddedPluginModel;
    private DefaultListModel<String> summaryListModel;
    private DefaultListModel<String> modisListModel;

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
     * @throws ParseException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public ProjectInformationPage(boolean isEditable,  MainWindowListener l) throws IOException, ParserConfigurationException, SAXException, ParseException {
        this.isEditable = isEditable;
        mainWindowEvent = new MainWindowEvent();
        mainWindowEvent.addListener(l);
        initialize();
        frame.setVisible(true);
    }

    /**
     * Initialize the contents of the frame.
     * @throws ParseException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    private void initialize() throws IOException, ParserConfigurationException, SAXException, ParseException {
        frame = new JFrame();
        frame.setBounds(100, 100, 1207, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);

        PopulatePluginList();
        CreateNewProjectButton();
        RenderInformationGrid();
        uiConstrain();
    }

    /**
     * constrains the UI base on editable tag
     */
    private void uiConstrain() {
        if(!isEditable)
        {
            // list of projects
            projectCollectionComboBox = new JComboBox<String>();
            projectCollectionComboBox.setBounds(507, 15, 229, 20);
            projectCollectionComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {

                    try {
                        PopulateProjectInfo();
                    } catch (ClassNotFoundException | NoSuchMethodException
                            | SecurityException | InstantiationException
                            | IllegalAccessException
                            | IllegalArgumentException
                            | InvocationTargetException | IOException
                            | ParserConfigurationException | SAXException
                            | ParseException e) {
                        e.printStackTrace();
                    }

                }
            });
            frame.getContentPane().add(projectCollectionComboBox);

            File fileDir = new File(System.getProperty("user.dir") + "\\src\\version2\\prototype\\ProjectInfoMetaData\\");

            for(File fXmlFile: getXMLFiles(fileDir)){
                projectCollectionComboBox.addItem(fXmlFile.getName().replace(".xml", ""));
            }
        }
    }

    /**
     * populate project info for edit
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws ParseException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    private void PopulateProjectInfo() throws IOException, ParserConfigurationException, SAXException, ParseException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

        String selectedProject = String.valueOf(projectCollectionComboBox.getSelectedItem());
        ProjectInfoFile project = new ProjectInfoCollection().GetProject(selectedProject);

        //{{ clear all values and set edit/enable mode
        listOfAddedPluginModel.clear();

        startDate.setEnabled(false);
        startDate.setDate(null);

        projectName.setEditable(false);
        projectName.setText("");

        workingDirectory.setEditable(false);
        workingDirectory.setText("");

        maskFile.setEditable(false);
        maskFile.setText("");

        masterShapeTextField.setEditable(false);
        masterShapeTextField.setText("");

        addNewModisButton.setEnabled(false);
        deleteSelectedModisButton.setEnabled(false);
        modisListModel.clear();

        timeZoneComboBox.setEnabled(false);
        isClippingCheckBox.setEnabled(false);
        coordinateSystemComboBox.setEnabled(false);
        reSamplingComboBox.setEnabled(false);
        datumComboBox.setEnabled(false);

        pixelSize.setEditable(false);
        pixelSize.setText("");

        standardParallel1.setEditable(false);
        standardParallel1.setText("");

        centalMeridian.setEditable(false);
        centalMeridian.setText("");

        falseEasting.setEditable(false);
        falseEasting.setText("");

        standardParallel2.setEditable(false);
        standardParallel2.setText("");

        latitudeOfOrigin.setEditable(false);
        latitudeOfOrigin.setText("");

        falseNothing.setEditable(false);
        falseNothing.setText("");

        freezingDateChooser.setEnabled(false);
        freezingDateChooser.setDate(null);

        heatingDateChooser.setEnabled(false);
        heatingDateChooser.setDate(null);

        summaryListModel.clear();

        // }}

        if(project == null) {
            return;
        }

        // set the plugin info
        for(ProjectInfoPlugin plugin: project.GetPlugins()){
            String formatString = String.format("<html>Plugin: %s;<br>Indices: %s</span> <br>Quality: %s;</span></html>",
                    String.valueOf(plugin.GetName()),
                    getIndiciesFormat(plugin.GetIndicies()),
                    String.valueOf(plugin.GetQC()));
            listOfAddedPluginModel.addElement(formatString);
        }

        // set basic project info
        startDate.setDate(Date.from(project.GetStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        projectName.setText(project.GetProjectName());
        workingDirectory.setText(project.GetWorkingDir());
        maskFile.setText(project.GetMaskingFile());
        masterShapeTextField.setText(project.GetMasterShapeFile());
        timeZoneComboBox.setSelectedItem(project.GetTimeZone());
        isClippingCheckBox.setSelected(project.GetClipping());
        freezingDateChooser.setDate(Date.from(project.GetFreezingDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        heatingDateChooser.setDate(Date.from(project.GetHeatingDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // set modis info
        for(String modis: project.GetModisTiles()){
            modisListModel.addElement(modis);
        }

        // set projection info
        coordinateSystemComboBox.setSelectedItem(project.GetProjection().getProjectionType());
        reSamplingComboBox.setSelectedItem(project.GetProjection().getResamplingType());

        datumComboBox.setSelectedItem(project.GetProjection().getDatum());
        pixelSize.setText(String.valueOf(project.GetProjection().getPixelSize()));
        standardParallel1.setText(String.valueOf(project.GetProjection().getStandardParallel1()));
        centalMeridian.setText(String.valueOf(project.GetProjection().getCentralMeridian()));
        falseEasting.setText(String.valueOf(project.GetProjection().getFalseEasting()));
        standardParallel2.setText(String.valueOf(project.GetProjection().getStandardParallel2()));
        latitudeOfOrigin.setText(String.valueOf(project.GetProjection().getLatitudeOfOrigin()));
        falseNothing.setText(String.valueOf(project.GetProjection().getFalseNorthing()));

        // set summary info
        for(ProjectInfoSummary summary: project.GetSummaries()){
            summaryListModel.addElement(summary.toString());
        }
    }

    /**
     *  format indices for displaying in ui
     * @param m
     * @return
     */
    private String getIndiciesFormat(ArrayList<String> m){
        String formatString = "";

        for(String indici : m){
            formatString += String.format("<span>%s;</span>",   indici);
        }

        return formatString;
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
                } catch (Exception e) {
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
        panel.setBounds(10, 420, 358, 340);
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setBounds(6, 25, 132, 15);
        panel.add(startDateLabel);
        startDate = new JDateChooser ();
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

        JLabel maskingFileLabel = new JLabel("Masking File:");
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
                FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter("tiff files", "tiff", "tif");
                chooser.setFileFilter(xmlfilter);
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
        masterShapeTextField.setBounds(148, 145, 158, 20);
        panel.add(masterShapeTextField);
        masterShapeTextField.setColumns(10);

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
        masterShapeFileBrowseButton.setBounds(316, 146, 32, 20);
        panel.add(masterShapeFileBrowseButton);

        final JLabel chmasterShapeFileLabel = new JLabel("Master shape file:");
        chmasterShapeFileLabel.setBounds(6, 144, 136, 23);
        panel.add(chmasterShapeFileLabel);

        JLabel lblTimeZone = new JLabel("Time Zone:");
        lblTimeZone.setBounds(6, 179, 132, 14);
        panel.add(lblTimeZone);

        timeZoneComboBox = new JComboBox<String>();
        timeZoneComboBox.setBounds(148, 176, 200, 20);
        for (String id : TimeZone.getAvailableIDs()) {
            TimeZone zone = TimeZone.getTimeZone(id);
            int offset = zone.getRawOffset()/1000;
            int hour = offset/3600;
            int minutes = (offset % 3600)/60;
            String timeZoneString = String.format("(GMT%+d:%02d) %s", hour, minutes, id);
            timeZoneComboBox.addItem(timeZoneString);
        }
        panel.add(timeZoneComboBox);

        isClippingCheckBox = new JCheckBox("");
        isClippingCheckBox.setBounds(148, 203, 200, 15);
        panel.add(isClippingCheckBox);

        JLabel lblClipping = new JLabel("Clipping:");
        lblClipping.setBounds(6, 204, 132, 14);
        panel.add(lblClipping);

        JLabel lblFreezingStartDate = new JLabel("Freezing Start Date: ");
        lblFreezingStartDate.setBounds(6, 229, 132, 14);
        panel.add(lblFreezingStartDate);
        freezingDateChooser = new JDateChooser();
        freezingDateChooser.setBounds(148, 223, 200, 20);
        panel.add(freezingDateChooser);

        JLabel lblHeatingStartDate = new JLabel("Heating Start Date:");
        lblHeatingStartDate.setBounds(6, 254, 132, 14);
        panel.add(lblHeatingStartDate);
        heatingDateChooser = new JDateChooser();
        heatingDateChooser.setBounds(148, 254, 200, 20);
        panel.add(heatingDateChooser);
    }

    @SuppressWarnings("rawtypes")
    private void ModisInformation() {
        JPanel modisInformationPanel = new JPanel();
        modisInformationPanel.setLayout(null);
        modisInformationPanel.setBorder(new TitledBorder(null, "Modis Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        modisInformationPanel.setBounds(359, 420, 275, 340);
        frame.getContentPane().add(modisInformationPanel);

        modisListModel = new DefaultListModel<String>();

        addNewModisButton = new JButton("");
        addNewModisButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        addNewModisButton.setToolTipText("Add modis");
        addNewModisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String tile = JOptionPane.showInputDialog(frame,"Enter Modis Tiles", null);

                if(tile.toUpperCase().charAt(0) != 'H' || tile.toUpperCase().charAt(3) != 'V') {
                    JOptionPane.showMessageDialog(null, "Modis format: hddvdd  d=> digit");
                    return;
                } else{
                    int horizontal = Integer.parseInt(String.format("%s%s", tile.toUpperCase().charAt(1), tile.toUpperCase().charAt(2)));
                    int vertical = Integer.parseInt(String.format("%c%c", tile.toUpperCase().charAt(4), tile.toUpperCase().charAt(5)));

                    if(horizontal < ModisTile.HORZ_MIN || horizontal > ModisTile.HORZ_MAX || vertical < ModisTile.VERT_MIN || vertical > ModisTile.VERT_MAX){
                        JOptionPane.showMessageDialog(null, String.format("Horizontal has be to within %d-%d and Vertical has to be within %d-%d",
                                ModisTile.HORZ_MIN , ModisTile.HORZ_MAX , ModisTile.VERT_MIN, ModisTile.VERT_MAX ));
                        return;
                    }

                }
                modisListModel.addElement(tile);
            }
        });
        addNewModisButton.setBounds(15, 299, 75, 30);
        modisInformationPanel.add(addNewModisButton);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(15, 70, 245, 178);
        modisInformationPanel.add(scrollPane);

        final JList<String> modisList = new JList<String>(modisListModel);
        scrollPane.setViewportView(modisList);

        deleteSelectedModisButton = new JButton("");
        deleteSelectedModisButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/ChangeQueryType_deletequery_274.png")));
        deleteSelectedModisButton.setToolTipText("Delete Selected Modis");
        deleteSelectedModisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultListModel model = (DefaultListModel) modisList.getModel();
                int selectedIndex = modisList.getSelectedIndex();
                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                }
            }
        });
        deleteSelectedModisButton.setBounds(185, 299, 75, 30);
        modisInformationPanel.add(deleteSelectedModisButton);
    }

    private void ProjectInformation() {
        JPanel panel_2 = new JPanel();
        panel_2.setLayout(null);
        panel_2.setBorder(new TitledBorder(null, "Projection Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_2.setBounds(631, 420, 297, 340);
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
        summaryPanel.setBounds(920, 420, 281, 340);
        frame.getContentPane().add(summaryPanel);

        summaryListModel = new DefaultListModel();

        JButton editSummaryButton = new JButton("");
        editSummaryButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        editSummaryButton.setToolTipText("Add summary");
        editSummaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new AssociateSummaryPage(new summaryListenerImplementation());
            }
        });
        editSummaryButton.setBounds(15, 299, 75, 30);
        summaryPanel.add(editSummaryButton);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(15, 70, 245, 178);
        summaryPanel.add(scrollPane);
        final JList summaryList = new JList(summaryListModel);
        scrollPane.setViewportView(summaryList);
        summaryList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

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
        deleteSummaryButton.setBounds(185, 299, 75, 30);
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
                noFormat = noFormat.replaceAll("<br>Indices: ", "");
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

            Element timeZone = doc.createElement("TimeZone");
            timeZone.appendChild(doc.createTextNode(String.valueOf(timeZoneComboBox.getSelectedItem())));
            projectInfo.appendChild(timeZone);

            Element isClipping = doc.createElement("Clipping");
            isClipping.appendChild(doc.createTextNode(String.valueOf(isClippingCheckBox.isSelected())));
            projectInfo.appendChild(isClipping);

            // Freezing start Date
            Element freezingstartDate = doc.createElement("Freezing");
            startDate.appendChild(doc.createTextNode(freezingDateChooser.getDate().toString()));
            projectInfo.appendChild(freezingstartDate);

            // Freezing start Date
            Element heatingstartDate = doc.createElement("Heating");
            startDate.appendChild(doc.createTextNode(heatingDateChooser.getDate().toString()));
            projectInfo.appendChild(heatingstartDate);

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
            Element pixelSize = doc.createElement("PixelSize");
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

            for(Object item:summaryListModel.toArray()){
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
