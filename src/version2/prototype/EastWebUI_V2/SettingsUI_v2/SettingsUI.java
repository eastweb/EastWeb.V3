package version2.prototype.EastWebUI_V2.SettingsUI_v2;

import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

import version2.prototype.Config;
import version2.prototype.EastWebUI_V2.DocumentBuilderInstance;
import version2.prototype.EastWebUI_V2.ProjectInformationUI_v2.ProjectInformationPage;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

public class SettingsUI {
    private JFrame frame;
    private JTextField errorLogDirTextField;
    private JTextField downloadDirtextField;
    private JTextField GlobalSchematextField;
    private JTextField textFieldHostName;
    private JTextField textFieldPort;
    private JTextField textFieldDatabaseName;
    private JTextField textFieldUserName;
    private JTextField textFieldPassword;
    private JTextField textFieldMaxNumOfConnectionsPerInstance;
    private JTextField textFieldPreferredTestQuery;
    private JTextField textFieldMinPoolSize;
    private JTextField textFieldInitialPoolSize;
    private JTextField textFieldAcquireIncrement;
    private JTextField textFieldMaxIdleTime;
    private JTextField textFieldIdleConnectionTestPeriod;
    private JTextField textFieldTestConnectionOnCheckout;
    private JTextField textFieldForceSynchronousCheckins;
    private JTextField textFieldCheckoutTimeout;
    private JTextField textFieldNumHelperThreads;

    private boolean isAdvance = false;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new SettingsUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public SettingsUI()throws Exception {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        initialize();
        initializeUIValue();
    }

    private void initializeUIValue() {
        errorLogDirTextField.setText(Config.getInstance().getErrorLogDir());
        downloadDirtextField.setText(Config.getInstance().getDownloadDir());
        GlobalSchematextField.setText(Config.getInstance().getGlobalSchema());
        textFieldHostName.setText(Config.getInstance().getDatabaseHost());
        textFieldPort.setText(Config.getInstance().getPort().toString());
        textFieldDatabaseName.setText(Config.getInstance().getDatabaseName());
        textFieldUserName.setText(Config.getInstance().getDatabaseUsername());
        textFieldPassword.setText(Config.getInstance().getDatabasePassword());
        textFieldMaxNumOfConnectionsPerInstance.setText(Config.getInstance().getMaxNumOfConnectionsPerInstance().toString());
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setVisible(true);
        frame.setBounds(100, 100, 545, 750);
        frame.getContentPane().setLayout(null);

        JLabel lblProjectInformation = new JLabel("System Settings");
        lblProjectInformation.setFont(new Font("Courier", Font.BOLD,25));
        lblProjectInformation.setBounds(10, 11, 315, 32);
        frame.getContentPane().add(lblProjectInformation);

        JButton saveButton = new JButton();
        saveButton.setBounds(475, 11, 44, 33);
        saveButton.setOpaque(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setBorderPainted(false);
        saveButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/save_32.png")));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(frame, "Settings was saved");
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        });
        saveButton.setToolTipText("Save Settings");
        frame.getContentPane().add(saveButton);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lblErrorLogDir = new JLabel("Error Log Directory:");
        lblErrorLogDir.setBounds(10, 60, 143, 14);
        frame.getContentPane().add(lblErrorLogDir);

        errorLogDirTextField = new JTextField();
        errorLogDirTextField.setBounds(163, 57, 257, 25);
        frame.getContentPane().add(errorLogDirTextField);
        errorLogDirTextField.setColumns(10);

        JButton browseErrorLogDirButton = new JButton("");
        browseErrorLogDirButton.setBounds(430, 54, 27, 23);
        browseErrorLogDirButton.setOpaque(false);
        browseErrorLogDirButton.setContentAreaFilled(false);
        browseErrorLogDirButton.setBorderPainted(false);
        browseErrorLogDirButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/folder-explore-icon.png")));
        browseErrorLogDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) { browseFolderDir(errorLogDirTextField);}
        });
        frame.getContentPane().add(browseErrorLogDirButton);

        JLabel lblDownloadDir = new JLabel("Download Directory:");
        lblDownloadDir.setBounds(10, 85, 143, 14);
        frame.getContentPane().add(lblDownloadDir);

        downloadDirtextField = new JTextField();
        downloadDirtextField.setColumns(10);
        downloadDirtextField.setBounds(163, 82, 257, 25);
        frame.getContentPane().add(downloadDirtextField);

        JButton browseDownloadDirButton = new JButton("");
        browseDownloadDirButton.setBounds(430, 85, 27, 23);
        browseDownloadDirButton.setOpaque(false);
        browseDownloadDirButton.setContentAreaFilled(false);
        browseDownloadDirButton.setBorderPainted(false);
        browseDownloadDirButton.setIcon(new ImageIcon(ProjectInformationPage.class.getResource("/version2/prototype/Images/folder-explore-icon.png")));
        browseDownloadDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) { browseFolderDir(downloadDirtextField);}
        });
        frame.getContentPane().add(browseDownloadDirButton);

        databaseUI();
        advanceUI();
    }

    private void databaseUI() {
        JPanel databasePanel = new JPanel();
        databasePanel.setBorder(new TitledBorder(null, "Database Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        databasePanel.setBounds(10, 110, 509, 207);
        frame.getContentPane().add(databasePanel);
        databasePanel.setLayout(null);

        JLabel labelGlobalSchema = new JLabel("Global Schema");
        labelGlobalSchema.setBounds(10, 24, 153, 14);
        databasePanel.add(labelGlobalSchema);

        JLabel labelHostName = new JLabel("Host Name");
        labelHostName.setBounds(10, 49, 153, 14);
        databasePanel.add(labelHostName);

        JLabel labelPort = new JLabel("Port");
        labelPort.setBounds(10, 74, 153, 14);
        databasePanel.add(labelPort);

        JLabel labelDatabaseName = new JLabel("Database Name");
        labelDatabaseName.setBounds(10, 99, 153, 14);
        databasePanel.add(labelDatabaseName);

        JLabel labelUserName = new JLabel("User Name");
        labelUserName.setBounds(10, 124, 153, 14);
        databasePanel.add(labelUserName);

        JLabel labelPassword = new JLabel("Password");
        labelPassword.setBounds(10, 149, 153, 14);
        databasePanel.add(labelPassword);

        JLabel labelMaxNumOfConnectionsPerInstance = new JLabel("Connections Per Instance");
        labelMaxNumOfConnectionsPerInstance.setBounds(10, 174, 153, 14);
        databasePanel.add(labelMaxNumOfConnectionsPerInstance);

        GlobalSchematextField = new JTextField();
        GlobalSchematextField.setBounds(173, 15, 238, 27);
        databasePanel.add(GlobalSchematextField);
        GlobalSchematextField.setColumns(10);

        textFieldHostName = new JTextField();
        textFieldHostName.setBounds(173, 43, 238, 27);
        databasePanel.add(textFieldHostName);
        textFieldHostName.setColumns(10);

        textFieldPort = new JTextField();
        textFieldPort.setBounds(173, 68, 238, 27);
        databasePanel.add(textFieldPort);
        textFieldPort.setColumns(10);

        textFieldDatabaseName = new JTextField();
        textFieldDatabaseName.setBounds(173, 93, 238, 27);
        databasePanel.add(textFieldDatabaseName);
        textFieldDatabaseName.setColumns(10);

        textFieldUserName = new JTextField();
        textFieldUserName.setBounds(173, 118, 238, 27);
        databasePanel.add(textFieldUserName);
        textFieldUserName.setColumns(10);

        textFieldPassword = new JTextField();
        textFieldPassword.setBounds(173, 143, 238, 27);
        databasePanel.add(textFieldPassword);
        textFieldPassword.setColumns(10);

        textFieldMaxNumOfConnectionsPerInstance = new JTextField();
        textFieldMaxNumOfConnectionsPerInstance.setColumns(10);
        textFieldMaxNumOfConnectionsPerInstance.setBounds(173, 168, 238, 27);
        databasePanel.add(textFieldMaxNumOfConnectionsPerInstance);
    }

    private void advanceUI() {
        JButton btnAdvanceSettings = new JButton("Advance Settings");
        btnAdvanceSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) { showAdvanceUI(); }
        });
        btnAdvanceSettings.setBounds(10, 328, 163, 23);
        frame.getContentPane().add(btnAdvanceSettings);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(new TitledBorder(null, "Advance Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setBounds(10, 412, 509, 288);
        frame.getContentPane().add(panel);

        JLabel preferredTestQueryLabel = new JLabel("Preferred Test Query");
        preferredTestQueryLabel.setBounds(10, 30, 210, 14);
        panel.add(preferredTestQueryLabel);

        JLabel minPoolSizeLabel = new JLabel("In Pool Size");
        minPoolSizeLabel.setBounds(10, 55, 210, 14);
        panel.add(minPoolSizeLabel);

        JLabel labelInitialPoolSize = new JLabel("Initial Pool Size");
        labelInitialPoolSize.setBounds(10, 80, 210, 14);
        panel.add(labelInitialPoolSize);

        JLabel labelAcquireIncrement = new JLabel("Acquire Increment");
        labelAcquireIncrement.setBounds(10, 105, 210, 14);
        panel.add(labelAcquireIncrement);

        JLabel labelMaxIdleTime = new JLabel("Max Idle Time");
        labelMaxIdleTime.setBounds(10, 130, 210, 14);
        panel.add(labelMaxIdleTime);

        JLabel labelIdleConnectionTestPeriod = new JLabel("Idle Connection Test Period");
        labelIdleConnectionTestPeriod.setBounds(10, 155, 210, 14);
        panel.add(labelIdleConnectionTestPeriod);

        JLabel labelTestConnectionOnCheckout = new JLabel("Test Connection On Checkout");
        labelTestConnectionOnCheckout.setBounds(10, 180, 210, 14);
        panel.add(labelTestConnectionOnCheckout);

        JLabel labelForceSynchronousCheckins = new JLabel("Force Synchronous Checkins");
        labelForceSynchronousCheckins.setBounds(10, 205, 210, 14);
        panel.add(labelForceSynchronousCheckins);

        JLabel labelCheckoutTimeout = new JLabel("Checkout Timeout");
        labelCheckoutTimeout.setBounds(10, 233, 210, 14);
        panel.add(labelCheckoutTimeout);

        JLabel labelNumHelperThreads = new JLabel("Number Helper Threads");
        labelNumHelperThreads.setBounds(10, 258, 210, 14);
        panel.add(labelNumHelperThreads);

        textFieldPreferredTestQuery = new JTextField();
        textFieldPreferredTestQuery.setColumns(10);
        textFieldPreferredTestQuery.setBounds(230, 27, 150, 25);
        panel.add(textFieldPreferredTestQuery);

        textFieldMinPoolSize = new JTextField();
        textFieldMinPoolSize.setColumns(10);
        textFieldMinPoolSize.setBounds(230, 55, 150, 25);
        panel.add(textFieldMinPoolSize);

        textFieldInitialPoolSize = new JTextField();
        textFieldInitialPoolSize.setColumns(10);
        textFieldInitialPoolSize.setBounds(230, 80, 150, 25);
        panel.add(textFieldInitialPoolSize);

        textFieldAcquireIncrement = new JTextField();
        textFieldAcquireIncrement.setColumns(10);
        textFieldAcquireIncrement.setBounds(230, 105, 150, 25);
        panel.add(textFieldAcquireIncrement);

        textFieldMaxIdleTime = new JTextField();
        textFieldMaxIdleTime.setColumns(10);
        textFieldMaxIdleTime.setBounds(230, 130, 150, 25);
        panel.add(textFieldMaxIdleTime);

        textFieldIdleConnectionTestPeriod = new JTextField();
        textFieldIdleConnectionTestPeriod.setColumns(10);
        textFieldIdleConnectionTestPeriod.setBounds(230, 155, 150, 25);
        panel.add(textFieldIdleConnectionTestPeriod);

        textFieldTestConnectionOnCheckout = new JTextField();
        textFieldTestConnectionOnCheckout.setColumns(10);
        textFieldTestConnectionOnCheckout.setBounds(230, 180, 150, 25);
        panel.add(textFieldTestConnectionOnCheckout);

        textFieldForceSynchronousCheckins = new JTextField();
        textFieldForceSynchronousCheckins.setColumns(10);
        textFieldForceSynchronousCheckins.setBounds(230, 205, 150, 25);
        panel.add(textFieldForceSynchronousCheckins);

        textFieldCheckoutTimeout = new JTextField();
        textFieldCheckoutTimeout.setColumns(10);
        textFieldCheckoutTimeout.setBounds(230, 230, 150, 25);
        panel.add(textFieldCheckoutTimeout);

        textFieldNumHelperThreads = new JTextField();
        textFieldNumHelperThreads.setColumns(10);
        textFieldNumHelperThreads.setBounds(230, 255, 150, 25);
        panel.add(textFieldNumHelperThreads);

        JLabel lblAdvanceSystemSettings = new JLabel("Advance System Settings");
        lblAdvanceSystemSettings.setFont(new Font("Monospaced", Font.BOLD, 25));
        lblAdvanceSystemSettings.setBounds(10, 362, 389, 32);
        frame.getContentPane().add(lblAdvanceSystemSettings);
    }

    private void browseFolderDir(JTextField dirTextField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Browse the folder to process");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): "+ chooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : "+ chooser.getSelectedFile());
            dirTextField.setText(chooser.getSelectedFile().toString());
        } else {
            System.out.println("No Selection ");
        }
    }

    private void showAdvanceUI() {
        isAdvance = !isAdvance;

        if(isAdvance){
            frame.setBounds(100, 100, 545, 750);
        }else {
            frame.setBounds(100, 100, 545, 395);
        }
    }

    public Element GetXMLObject() throws ParserConfigurationException {
        Element config = DocumentBuilderInstance.Instance().GetDocument().createElement("config");

        Element ErrorLogDir = DocumentBuilderInstance.Instance().GetDocument().createElement("ErrorLogDir");
        ErrorLogDir.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(errorLogDirTextField.getText()));
        config.appendChild(ErrorLogDir);

        Element DownloadDir = DocumentBuilderInstance.Instance().GetDocument().createElement("DownloadDir");
        DownloadDir.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(downloadDirtextField.getText()));
        config.appendChild(DownloadDir);

        Element Database = DocumentBuilderInstance.Instance().GetDocument().createElement("Database");
        config.appendChild(Database);

        Element GlobalSchema = DocumentBuilderInstance.Instance().GetDocument().createElement("GlobalSchema");
        GlobalSchema.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(GlobalSchematextField.getText()));
        Database.appendChild(GlobalSchema);

        Element HostName = DocumentBuilderInstance.Instance().GetDocument().createElement("HostName");
        HostName.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(textFieldHostName.getText()));
        config.appendChild(HostName);

        Element Port = DocumentBuilderInstance.Instance().GetDocument().createElement("Port");
        Port.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(textFieldPort.getText()));
        config.appendChild(Port);

        Element DatabaseName = DocumentBuilderInstance.Instance().GetDocument().createElement("DatabaseName");
        DatabaseName.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(textFieldDatabaseName.getText()));
        config.appendChild(DatabaseName);

        Element UserName = DocumentBuilderInstance.Instance().GetDocument().createElement("UserName");
        UserName.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(textFieldUserName.getText()));
        config.appendChild(UserName);

        Element PassWord = DocumentBuilderInstance.Instance().GetDocument().createElement("PassWord");
        PassWord.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(textFieldPassword.getText()));
        config.appendChild(PassWord);

        Element MaxNumOfConnectionsPerInstance = DocumentBuilderInstance.Instance().GetDocument().createElement("MaxNumOfConnectionsPerInstance");
        MaxNumOfConnectionsPerInstance.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(textFieldMaxNumOfConnectionsPerInstance.getText()));
        config.appendChild(MaxNumOfConnectionsPerInstance);

        Element Output = DocumentBuilderInstance.Instance().GetDocument().createElement("Output");
        config.appendChild(Output);

        Element qc = DocumentBuilderInstance.Instance().GetDocument().createElement("QC");
        //qc.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(qcLevel));
        config.appendChild(qc);


        Element indicies = DocumentBuilderInstance.Instance().GetDocument().createElement("Indicies");
        //indicies.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(i.toString()));
        config.appendChild(indicies);


        ;
        ;
        ;
        ;
        ;
        ;
        ;
        ;


        return config;
    }
}
