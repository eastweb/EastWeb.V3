package version2.prototype.EastWebUI_V2.ProjectInformationUI_v2;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
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
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import version2.prototype.Config;
import version2.prototype.ErrorLog;
import version2.prototype.EastWebUI_V2.DocumentBuilderInstance;
import version2.prototype.EastWebUI_V2.GlobalUIData;
import version2.prototype.EastWebUI_V2.MainWindow.MainWindowEvent;
import version2.prototype.EastWebUI_V2.MainWindow.MainWindowListener;
import version2.prototype.EastWebUI_V2.PluginUI_v2.AssociatePluginPage;
import version2.prototype.EastWebUI_V2.PluginUI_v2.IPlugin;
import version2.prototype.EastWebUI_V2.PluginUI_v2.IndiciesListener;
import version2.prototype.EastWebUI_V2.PluginUI_v2.PluginEventObject;
import version2.prototype.EastWebUI_V2.SummaryUI_v2.AssociateSummaryPage;
import version2.prototype.EastWebUI_V2.SummaryUI_v2.SummaryEventObject;
import version2.prototype.EastWebUI_V2.SummaryUI_v2.SummaryListener;
import version2.prototype.ProjectInfoMetaData.ProjectInfoCollection;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
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
     * @throws Exception
     */
    public ProjectInformationPage(boolean isEditable,  MainWindowListener l) throws Exception {
        frame = new JFrame();
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
    private void initialize() throws IOException, ParserConfigurationException, SAXException, ParseException, Exception{
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        frame.setBounds(100, 100, 955, 750);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);

        CreateNewProjectButton();
        PopulatePluginList();
        BasicProjectInformation();
        ProjectInformation();
        SummaryInformation();
        UIConstrain();
    }

    private void CreateNewProjectButton() {
        JButton saveButton = new JButton("");
        saveButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/save_32.png")));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    CreateNewProject();
                    JOptionPane.showMessageDialog(frame, "Project was saved");
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                } catch (TransformerException e) {
                    e.printStackTrace();
                }
            }
        });
        saveButton.setToolTipText("Save Project");
        saveButton.setBounds(907, 11, 32, 32);
        frame.getContentPane().add(saveButton);
    }

    private void PopulatePluginList() {
        listOfAddedPluginModel = new DefaultListModel<String>();

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Plugin List", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setBounds(10, 58, 929, 351);
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        JButton addPluginButton = new JButton("");
        addPluginButton.setBounds(10, 31, 75, 30);
        panel.add(addPluginButton);
        addPluginButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        addPluginButton.setToolTipText("Add Plugin");

        JButton deletePluginButton = new JButton("");
        deletePluginButton.setBounds(103, 31, 70, 30);
        panel.add(deletePluginButton);
        deletePluginButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/trashCan.png")));
        deletePluginButton.setToolTipText("Delete Plugin");

        final JList<String> listOfAddedPlugin = new JList<String>(listOfAddedPluginModel);
        listOfAddedPlugin.setBorder(new EmptyBorder(10,10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(listOfAddedPlugin);
        scrollPane.setBounds(10, 65, 909, 275);
        panel.add(scrollPane);
        deletePluginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {deleteSelectedPlugin(listOfAddedPlugin);}
        });
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
        startDate.setBounds(148, 22, 200, 28);
        panel.add(startDate);

        JLabel projectNameLabel = new JLabel("Project Name: ");
        projectNameLabel.setBounds(6, 56, 136, 14);
        panel.add(projectNameLabel);
        projectName = new JTextField();
        projectName.setBounds(148, 53, 200, 28);
        panel.add(projectName);
        projectName.setColumns(10);

        JLabel workingDirLabel = new JLabel("Working Dir: ");
        workingDirLabel.setBounds(6, 87, 132, 15);
        panel.add(workingDirLabel);
        workingDirectory = new JTextField();
        workingDirectory.setColumns(10);
        workingDirectory.setBounds(148, 84, 158, 28);
        panel.add(workingDirectory);

        JButton workingDirBrowsebutton = new JButton(". . .");
        workingDirBrowsebutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {chooseWorkingDir();}
        });
        workingDirBrowsebutton.setBounds(316, 84, 32, 28);
        panel.add(workingDirBrowsebutton);

        JLabel maskingFileLabel = new JLabel("Masking File:");
        maskingFileLabel.setBounds(6, 118, 132, 15);
        panel.add(maskingFileLabel);
        maskFile = new JTextField();
        maskFile.setColumns(10);
        maskFile.setBounds(148, 115, 158, 28);
        panel.add(maskFile);

        JButton maskFileBrowseButton = new JButton(". . .");
        maskFileBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {chooseMaskingFile();}
        });
        maskFileBrowseButton.setBounds(316, 115, 32, 28);
        panel.add(maskFileBrowseButton);

        masterShapeTextField = new JTextField();
        masterShapeTextField.setBounds(148, 176, 158, 28);
        panel.add(masterShapeTextField);
        masterShapeTextField.setColumns(10);

        final JButton masterShapeFileBrowseButton = new JButton(". . .");
        masterShapeFileBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {chooseMasterShapeFile();}
        });
        masterShapeFileBrowseButton.setBounds(316, 176, 32, 28);
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
        resolutionTextField.setBounds(148, 146, 200, 28);
        panel.add(resolutionTextField);
        resolutionTextField.setColumns(10);
    }

    private void ProjectInformation() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(new TitledBorder(null, "Projection Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setBounds(365, 420, 297, 290);
        frame.getContentPane().add(panel);

        JLabel reSamplingLabel = new JLabel("Re-sampling Type:");
        reSamplingLabel.setBounds(6, 23, 109, 14);
        panel.add(reSamplingLabel);
        reSamplingComboBox = new JComboBox<String>();
        reSamplingComboBox.setBounds(146, 20, 140, 20);
        reSamplingComboBox.addItem("NEAREST_NEIGHBOR");
        reSamplingComboBox.addItem("BILINEAR");
        reSamplingComboBox.addItem("CUBIC_CONVOLUTION");
        panel.add(reSamplingComboBox);

        JLabel pixelSizeLabel = new JLabel("Pixel size meters:");
        pixelSizeLabel.setBounds(6, 55, 109, 14);
        panel.add(pixelSizeLabel);
        pixelSize = new JTextField();
        pixelSize.setColumns(10);
        pixelSize.setBounds(146, 51, 140, 28);
        panel.add(pixelSize);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void SummaryInformation() {
        summaryListModel = new DefaultListModel();

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(null);
        summaryPanel.setBorder(new TitledBorder(null, "Summary Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        summaryPanel.setBounds(658, 420, 281, 290);
        frame.getContentPane().add(summaryPanel);

        JButton editSummaryButton = new JButton("");
        editSummaryButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/action_add_16xLG.png")));
        editSummaryButton.setToolTipText("Add summary");
        editSummaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) { new AssociateSummaryPage(new summaryListenerImplementation());}
        });
        editSummaryButton.setBounds(10, 246, 75, 30);
        summaryPanel.add(editSummaryButton);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 25, 261, 210);
        summaryPanel.add(scrollPane);

        final JList summaryList = new JList(summaryListModel);
        scrollPane.setViewportView(summaryList);
        summaryList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        JButton deleteSummaryButton = new JButton("");
        deleteSummaryButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/trashCan.png")));
        deleteSummaryButton.setToolTipText("Delete Selected Summary");
        deleteSummaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {deleteSelectedSummary(summaryList);}
        });
        deleteSummaryButton.setBounds(196, 246, 75, 30);
        summaryPanel.add(deleteSummaryButton);
    }

    /**
     * constrains the UI base on editable tag
     */
    private void UIConstrain() {
        if(!isEditable){
            projectCollectionComboBox = new JComboBox<String>();
            projectCollectionComboBox.setBounds(300, 15, 229, 20);
            projectCollectionComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {PopulateProjectInfo();}
            });
            frame.getContentPane().add(projectCollectionComboBox);
            for(ProjectInfoFile project : ProjectInfoCollection.GetAllProjectInfoFiles(Config.getInstance())){
                projectCollectionComboBox.addItem(project.GetProjectName());
            }
        }
    }

    private void CreateNewProject() throws TransformerException{
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

            int summaryCounter = 1;
            for(Object item:summaryListModel.toArray()){
                Element summary = doc.createElement("Summary");
                summary.setAttribute("ID", String.valueOf(summaryCounter));
                summaryCounter ++;

                summary.appendChild(doc.createTextNode(item.toString()));
                summaries.appendChild(summary);
            }

            if(ProjectInfoCollection.WriteProjectToFile(doc, this.projectName.getText())){
                System.out.println("File saved!");
                mainWindowEvent.fire();
            }else{
                System.out.println("Erorr in saving");
            }
        } catch (ParserConfigurationException e) {
            ErrorLog.add(Config.getInstance(), "ProjectInformationPage.CreateNewProject problem with creating new project.", e);
        }
    }

    /**
     * populate project info for edit
     */
    private void PopulateProjectInfo(){
        ProjectInfoFile project = ProjectInfoCollection.GetProject(Config.getInstance(),
                String.valueOf(projectCollectionComboBox.getSelectedItem()));

        if(project == null) {
            return;
        }else{
            startDate.setEnabled(false);
            startDate.setDate(Date.from(project.GetStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            projectName.setEditable(false);
            projectName.setText(project.GetProjectName());
            workingDirectory.setEditable(false);
            workingDirectory.setText(project.GetWorkingDir());
            maskFile.setEditable(false);
            maskFile.setText(project.GetMaskingFile());
            resolutionTextField.setEditable(false);
            resolutionTextField.setText((project.GetMaskingResolution() != null) ? project.GetMaskingResolution().toString() : null);
            masterShapeTextField.setEditable(false);
            masterShapeTextField.setText(project.GetMasterShapeFile());
            timeZoneComboBox.setEnabled(false);
            timeZoneComboBox.setSelectedItem(project.GetTimeZone());
            isClippingCheckBox.setEnabled(false);
            isClippingCheckBox.setSelected(project.GetClipping());

            reSamplingComboBox.setEnabled(false);
            reSamplingComboBox.setSelectedItem(project.GetProjection().getResamplingType());
            pixelSize.setEditable(false);
            pixelSize.setText(String.valueOf(project.GetProjection().getPixelSize()));

            summaryListModel.clear();
            for(ProjectInfoSummary summary: project.GetSummaries()){
                summaryListModel.addElement(summary.toString());
            }

            listOfAddedPluginModel.clear();
            for(IPlugin p: project.GetIPlugins()){
                listOfAddedPluginModel.addElement(p.GetUIDisplayPlugin());
                pluginList.add(p);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void deleteSelectedPlugin(final JList<String> listOfAddedPlugin) {
        DefaultListModel model = (DefaultListModel) listOfAddedPlugin.getModel();
        int selectedIndex = listOfAddedPlugin.getSelectedIndex();

        // get Id from string
        String value = listOfAddedPlugin.getSelectedValue();
        String [] section1 = value.split("PluginID: ");
        String [] section2 = section1[1].split("<br>Plugin:");
        int id = Integer.parseInt(section2[0].replaceAll("\\s+",""));

        if (selectedIndex != -1) {
            model.remove(selectedIndex);
            IPlugin temp = null;

            for(IPlugin p : pluginList){
                if(p.GetId() == id){
                    temp = p;
                    break;
                }
            }

            if(temp != null) {
                pluginList.remove(temp);
            }
        }
    }

    private void chooseWorkingDir() {
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

    private void chooseMaskingFile() {
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

    private void chooseMasterShapeFile() {
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

    @SuppressWarnings("rawtypes")
    private void deleteSelectedSummary(final JList summaryList) {
        DefaultListModel model = (DefaultListModel) summaryList.getModel();
        int selectedIndex = summaryList.getSelectedIndex();

        if (selectedIndex != -1) {
            model.remove(selectedIndex);
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