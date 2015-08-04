package version2.prototype;

import java.io.*;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import version2.prototype.util.FileSystem;

//import version2.prototype.util.LazyCachedReference;

/**
 *
 * @author michael.devos
 *
 */
public class Config {
    private static final String CONFIG_FILENAME = ".//config.xml";
    private static final String ERROR_LOG_DIR_KEY = "ErrorLogDir";
    // Download section
    private static final String DOWNLOAD_DIR_KEY = "DownloadDir";
    // Database section
    private static final String DATA_BASE_KEY = "DataBase";
    private static final String GLOBAL_SCHEMA_KEY = "globalSchema";
    private static final String HOST_NAME_KEY = "hostName";
    private static final String DATABASE_USERNAME_KEY = "userName";
    private static final String DATABASE_PASSWORD_KEY = "passWord";
    // Output section
    private static final String OUTPUT_KEY = "Output";
    // Instance
    private static Config instance = null;
    //    private static final LazyCachedReference<Config, ConfigReadException> instance =
    //            new LazyCachedReference<Config, ConfigReadException>() {
    //        @Override
    //        protected Config makeInstance() throws ConfigReadException {
    //            try {
    //                return new Config();
    //            } catch (ConfigReadException e) {
    //                throw e;
    //            } catch (Exception e) {
    //                throw new ConfigReadException(e);
    //            }
    //        }
    //    };

    private final String errorLogDir;
    private final String downloadDir;
    private final String globalSchema;
    private final String databaseHost;
    private final String databaseUsername;
    private final String databasePassword;
    private final ArrayList<String> summaryCalculations;

    private Config(String xmlPath) throws ParserConfigurationException, SAXException, IOException {
        File fXmlFile = new File(xmlPath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        errorLogDir = ((Element) doc.getElementsByTagName(ERROR_LOG_DIR_KEY).item(0)).getTextContent();

        // Node: Download
        downloadDir = ((Element) doc.getElementsByTagName(DOWNLOAD_DIR_KEY).item(0)).getTextContent();

        // Node: Database
        Element database = (Element) doc.getElementsByTagName(DATA_BASE_KEY).item(0);
        globalSchema = database.getElementsByTagName(GLOBAL_SCHEMA_KEY).item(0).getTextContent();
        databaseHost = "jdbc:postgresql://"+database.getElementsByTagName(HOST_NAME_KEY).item(0).getTextContent();
        databaseUsername = database.getElementsByTagName(DATABASE_USERNAME_KEY).item(0).getTextContent();
        databasePassword = database.getElementsByTagName(DATABASE_PASSWORD_KEY).item(0).getTextContent();

        // Node: Output
        Node outputNode = doc.getElementsByTagName(OUTPUT_KEY).item(0);
        // Node(s): SummaryCalculation
        summaryCalculations = new ArrayList<String>(1);
        NodeList summaryList = ((Element) outputNode).getElementsByTagName("SummaryCalculation");
        for(int i=0; i < summaryList.getLength(); i++) {
            summaryCalculations.add(summaryList.item(i).getTextContent());
        }
    }

    private Config(String errorLogDir, String downloadDir, String globalSchema, String databaseHost, String databaseUsername, String databasePassword, ArrayList<String> summaryCalculations) {
        this.errorLogDir = errorLogDir;
        this.downloadDir = downloadDir;
        this.globalSchema = globalSchema;
        this.databaseHost = databaseHost;
        this.databaseUsername = databaseUsername;
        this.databasePassword = databasePassword;
        this.summaryCalculations = summaryCalculations;
    }

    /**
     * Gets the singleton instance of Config reading and parsing the Config.xml file found in EASTWeb's working directory.
     *
     * @return the Config instance
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Config getInstance() throws ParserConfigurationException, SAXException, IOException {
        if(instance == null) {
            instance = new Config(CONFIG_FILENAME);
        }
        return instance;
    }

    /**
     * Returns a Config object created from parsing the xml file found at the given path. Works the same as calling getInstance() but allows specifying of which file to use. Doesn't store or reuse
     * the created instance. Meant for testing purposes.
     *
     * @param xmlPath  - file path to the xml to read and parse
     * @return a new Config object
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Config getAnInstance(String xmlPath) throws ParserConfigurationException, SAXException, IOException {
        return new Config(xmlPath);
    }

    /**
     * Creates a custom Config object with the given parameters. Doesn't store or reuse the created instance. Meant for testing purposes.
     * @param downloadDir
     * @param globalSchema
     * @param databaseHost
     * @param databaseUsername
     * @param databasePassword
     * @param summaryCalculations
     * @return a new Config object
     */
    public static Config getAnInstance(String errorLogDir, String downloadDir, String globalSchema, String databaseHost, String databaseUsername, String databasePassword,
            ArrayList<String> summaryCalculations) {
        return new Config(errorLogDir, downloadDir, globalSchema, databaseHost, databaseUsername, databasePassword, summaryCalculations);
    }

    public String getErrorLogDir() {
        return FileSystem.CheckDirPath(errorLogDir);
    }

    public String getDownloadDir() {
        return FileSystem.CheckDirPath(downloadDir);
    }

    public String getGlobalSchema() {
        return FileSystem.StandardizeName(globalSchema);
    }

    public String getDatabaseHost() {
        return databaseHost;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public ArrayList<String> getSummaryCalculations() {
        return summaryCalculations;
    }
}
