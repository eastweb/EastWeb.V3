package version2.prototype.EastWebUI_V2.ProjectInformationUI_v2;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.EastWebUI.MainWindow.MainWindowEvent;
import version2.prototype.EastWebUI.MainWindow.MainWindowListener;
import version2.prototype.EastWebUI.SummaryUI.AssociateSummaryPage;
import version2.prototype.EastWebUI.SummaryUI.SummaryEventObject;
import version2.prototype.EastWebUI.SummaryUI.SummaryListener;
import version2.prototype.EastWebUI_V2.DocumentBuilderInstance;
import version2.prototype.EastWebUI_V2.GlobalUIData;
import version2.prototype.EastWebUI_V2.PluginUI_v2.AssociatePluginPage;
import version2.prototype.EastWebUI_V2.PluginUI_v2.IPlugin;
import version2.prototype.EastWebUI_V2.PluginUI_v2.IndiciesListener;
import version2.prototype.EastWebUI_V2.PluginUI_v2.PluginEventObject;
import version2.prototype.ProjectInfoMetaData.ProjectInfoCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ProjectInfoMetaData.ProjectInfoPlugin;
import version2.prototype.ProjectInfoMetaData.ProjectInfoSummary;

import com.toedter.calendar.JDateChooser;

public class ProjectInformationPage {
    private JFrame frame;
    private JDateChooser  startDate;
    private JTextField projectName;
    private JTextField workingDirectory;
    private JTextField maskFile;
    private JTextField pixelSize;
    private JComboBox<String> timeZoneComboBox;
    private JComboBox<String> reSamplingComboBox;
    private JComboBox<String> projectCollectionComboBox;
    private JTextField masterShapeTextField;
    private JCheckBox isClippingCheckBox;
    private JTextField resolutionTextField;

    private boolean isEditable;
    private ArrayList<IPlugin> pluginList;
    private MainWindowEvent mainWindowEvent;

    private DefaultListModel<String> listOfAddedPluginModel;
    private DefaultListModel<String> summaryListModel;

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
                    ErrorLog.add(Config.getInstance(), "ProjectInformationPage.main problem with running a ProjectInformationPage window.", e);
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
        pluginList = new ArrayList<IPlugin>();
        mainWindowEvent = new MainWindowEvent();

        this.isEditable = isEditable;
        mainWindowEvent.addListener(l);
        frame.setVisible(true);

        initialize();
        DocumentBuilderInstance.ClearInstance();
        GlobalUIData.ClearInstance();
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
        frame.setBounds(100, 100, 955, 750);
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
                    PopulateProjectInfo();
                }
            });
            frame.getContentPane().add(projectCollectionComboBox);

            ArrayList<ProjectInfoFile> projects = ProjectInfoCollection.GetAllProjectInfoFiles(Config.getInstance());
            for(ProjectInfoFile project : projects)
            {
                projectCollectionComboBox.addItem(project.GetProjectName());
            }
        }
    }

    /**
     * populate project info for edit
     */
    private void PopulateProjectInfo()
    {
        String selectedProject = String.valueOf(projectCollectionComboBox.getSelectedItem());
        ProjectInfoFile project = ProjectInfoCollection.GetProject(Config.getInstance(), selectedProject);

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

        resolutionTextField.setEditable(false);
        resolutionTextField.setText("");

        masterShapeTextField.setEditable(false);
        masterShapeTextField.setText("");

        timeZoneComboBox.setEnabled(false);
        isClippingCheckBox.setEnabled(false);

        reSamplingComboBox.setEnabled(false);
        pixelSize.setEditable(false);
        pixelSize.setText("");

        summaryListModel.clear();

        if(project == null) {
            return;
        }

        // set the plugin info
        for(ProjectInfoPlugin plugin: project.GetPlugins()){


        }

        // set basic project info
        startDate.setDate(Date.from(project.GetStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        projectName.setText(project.GetProjectName());
        workingDirectory.setText(project.GetWorkingDir());
        maskFile.setText(project.GetMaskingFile());
        resolutionTextField.setText((project.GetMaskingResolution() != null) ? project.GetMaskingResolution().toString() : null);
        masterShapeTextField.setText(project.GetMasterShapeFile());
        timeZoneComboBox.setSelectedItem(project.GetTimeZone());
        isClippingCheckBox.setSelected(project.GetClipping());

        // set projection info
        reSamplingComboBox.setSelectedItem(project.GetProjection().getResamplingType());
        pixelSize.setText(String.valueOf(project.GetProjection().getPixelSize()));

        // set summary info
        for(ProjectInfoSummary summary: project.GetSummaries()){
            summaryListModel.addElement(summary.toString());
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

        createButton.setBounds(764, 10, 175, 25);
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
                    ErrorLog.add(Config.getInstance(), "ProjectInformationPage.PopulatePluginList problem with creating new AssociatePluginPage.", e);
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
                String value = listOfAddedPlugin.getSelectedValue();

                // get Id from string
                String [] section1 = value.split("PluginID: ");
                String [] section2 = section1[1].split("<br>Plugin:");
                int id = Integer.parseInt(section2[0].replaceAll("\\s+",""));

                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                    IPlugin temp = null;
                    for(IPlugin p : pluginList)
                    {
                        if(p.GetId() == id)
                        {
                            temp = p;
                            break;
                        }
                    }

                    if(temp != null) {
                        pluginList.remove(temp);
                    }
                }
            }
        });
        deletePluginButton.setBounds(54, 12, 34, 23);
        frame.getContentPane().add(deletePluginButton);

        JScrollPane scrollPane = new JScrollPane(listOfAddedPlugin);
        scrollPane.setBounds(10, 40, 929, 369);
        frame.getContentPane().add(scrollPane);
    }

    private void RenderInformationGrid() {
        BasicProjectInformation();
        ProjectInformation();
        SummaryInformation();
    }

    private void BasicProjectInformation() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Basic Project Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setBounds(10, 420, 358, 290);
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
        masterShapeTextField.setBounds(148, 176, 158, 20);
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
        masterShapeFileBrowseButton.setBounds(316, 176, 32, 20);
        panel.add(masterShapeFileBrowseButton);

        final JLabel chmasterShapeFileLabel = new JLabel("Master shape file:");
        chmasterShapeFileLabel.setBounds(6, 175, 136, 23);
        panel.add(chmasterShapeFileLabel);

        JLabel lblTimeZone = new JLabel("Time Zone:");
        lblTimeZone.setBounds(6, 209, 132, 14);
        panel.add(lblTimeZone);

        timeZoneComboBox = new JComboBox<String>();
        timeZoneComboBox.setBounds(148, 206, 200, 20);
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
        isClippingCheckBox.setBounds(148, 233, 200, 15);
        panel.add(isClippingCheckBox);

        JLabel lblClipping = new JLabel("Clipping:");
        lblClipping.setBounds(6, 234, 132, 14);
        panel.add(lblClipping);

        JLabel lblResolution = new JLabel("Masking File Resolution:");
        lblResolution.setBounds(6, 150, 132, 14);
        panel.add(lblResolution);

        resolutionTextField = new JTextField();
        resolutionTextField.setBounds(148, 146, 200, 20);
        panel.add(resolutionTextField);
        resolutionTextField.setColumns(10);
    }

    private void ProjectInformation() {
        JPanel panel_2 = new JPanel();
        panel_2.setLayout(null);
        panel_2.setBorder(new TitledBorder(null, "Projection Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_2.setBounds(365, 420, 297, 290);
        frame.getContentPane().add(panel_2);

        JLabel reSamplingLabel = new JLabel("Re-sampling Type:");
        reSamplingLabel.setBounds(6, 23, 109, 14);
        panel_2.add(reSamplingLabel);
        reSamplingComboBox = new JComboBox<String>();
        reSamplingComboBox.setBounds(146, 20, 140, 20);
        reSamplingComboBox.addItem("NEAREST_NEIGHBOR");
        reSamplingComboBox.addItem("BILINEAR");
        reSamplingComboBox.addItem("CUBIC_CONVOLUTION");
        panel_2.add(reSamplingComboBox);

        JLabel pixelSizeLabel = new JLabel("Pixel size meters:");
        pixelSizeLabel.setBounds(6, 48, 109, 14);
        panel_2.add(pixelSizeLabel);
        pixelSize = new JTextField();
        pixelSize.setColumns(10);
        pixelSize.setBounds(146, 51, 140, 16);
        panel_2.add(pixelSize);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void SummaryInformation() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(null);
        summaryPanel.setBorder(new TitledBorder(null, "Summary Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        summaryPanel.setBounds(658, 420, 281, 290);
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
        editSummaryButton.setBounds(15, 246, 75, 30);
        summaryPanel.add(editSummaryButton);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(15, 25, 245, 210);
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
        deleteSummaryButton.setBounds(185, 246, 75, 30);
        summaryPanel.add(deleteSummaryButton);
    }

    private void CreateNewProject(){

        File theDir = new File(System.getProperty("user.dir") + "\\projects\\" + projectName.getText() + ".xml" );

        try {

            // root elements
            Document doc = DocumentBuilderInstance.Instance().GetDocument();
            Element projectInfo = doc.createElement("ProjectInfo");
            doc.appendChild(projectInfo);

            // Plugin elements
            Element plugins = doc.createElement("Plugins");
            projectInfo.appendChild(plugins);

            for(IPlugin p: pluginList){
                plugins.appendChild(p.GetXMLObject());
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
            Element masking = doc.createElement("Masking");
            projectInfo.appendChild(masking);

            Element maskingFile = doc.createElement("File");
            maskingFile.appendChild(doc.createTextNode(maskFile.getText()));
            masking.appendChild(maskingFile);

            Element resolution = doc.createElement("Resolution");
            resolution.appendChild(doc.createTextNode(resolutionTextField.getText()));
            masking.appendChild(resolution);

            Element masterShapeFile = doc.createElement("MasterShapeFile");
            masterShapeFile.appendChild(doc.createTextNode(masterShapeTextField.getText()));
            projectInfo.appendChild(masterShapeFile);

            Element timeZone = doc.createElement("TimeZone");
            timeZone.appendChild(doc.createTextNode(String.valueOf(timeZoneComboBox.getSelectedItem())));
            projectInfo.appendChild(timeZone);

            Element isClipping = doc.createElement("Clipping");
            isClipping.appendChild(doc.createTextNode(String.valueOf(isClippingCheckBox.isSelected())));
            projectInfo.appendChild(isClipping);

            // resampling
            Element reSampling = doc.createElement("ReSampling");
            reSampling.appendChild(doc.createTextNode(String.valueOf(reSamplingComboBox.getSelectedItem())));
            projectInfo.appendChild(reSampling);

            //datum
            Element pixelSize = doc.createElement("PixelSize");
            pixelSize.appendChild(doc.createTextNode(String.valueOf(this.pixelSize.getText())));
            projectInfo.appendChild(pixelSize);

            //list of summary tiles
            Element summaries = doc.createElement("Summaries");
            projectInfo.appendChild(summaries);

            int counter = 1;

            for(Object item:summaryListModel.toArray()){

                Element summary = doc.createElement("Summary");
                summary.setAttribute("ID", String.valueOf(counter));
                counter ++;

                summary.appendChild(doc.createTextNode(item.toString()));
                summaries.appendChild(summary);
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(theDir);

            transformer.transform(source, result);
            System.out.println("File saved!");
            mainWindowEvent.fire();

        } catch (ParserConfigurationException | TransformerException e) {
            ErrorLog.add(Config.getInstance(), "ProjectInformationPage.CreateNewProject problem with creating new project.", e);
        }
    }

    class indiciesListenerImplementation implements IndiciesListener{
        @Override
        public void AddPlugin(PluginEventObject e) {
            pluginList.add(e.getPlugin());

            listOfAddedPluginModel.clear();
            for(IPlugin p: pluginList){
                listOfAddedPluginModel.addElement(p.GetUIDisplayPlugin());
            }
        }
    }

    class summaryListenerImplementation implements SummaryListener{
        @Override
        public void AddSummary(SummaryEventObject e) {
            summaryListModel.addElement(e.getPlugin());
        }
    }
}
