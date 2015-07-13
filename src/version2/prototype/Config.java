package version2.prototype;

import java.io.*;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import version2.prototype.util.LazyCachedReference;


public class Config {
    private static final String CONFIG_FILENAME = ".//config.xml";
    private static final String GLOBAL_SCHEMA_KEY = "globalSchema";
    private static final String HOST_NAME_KEY = "hostName";
    private static final String DATA_BASE_KEY = "DataBase";
    private static final String DATABASE_USERNAME_KEY = "userName";
    private static final String DATABASE_PASSWORD_KEY = "passWord";
    private static final LazyCachedReference<Config, ConfigReadException> instance =
            new LazyCachedReference<Config, ConfigReadException>() {
        @Override
        protected Config makeInstance() throws ConfigReadException {
            try {
                return new Config();
            } catch (ConfigReadException e) {
                throw e;
            } catch (Exception e) {
                throw new ConfigReadException(e);
            }
        }
    };

    private String globalSchema;
    private String databaseHost;
    private String databaseUsername;
    private String databasePassword;
    private ArrayList<String> SummaryCalculations;

    private Config() throws Exception {
        loadSettings();
    }

    public static Config getInstance() throws ConfigReadException {
        return instance.get();
    }

    private void loadSettings() throws ConfigReadException, IOException, SAXException, ParserConfigurationException {
        File fXmlFile = new File(CONFIG_FILENAME);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();

        Element database = (Element) doc.getElementsByTagName(DATA_BASE_KEY).item(0);
        globalSchema = database.getElementsByTagName(GLOBAL_SCHEMA_KEY).item(0).getTextContent();
        databaseHost = "jdbc:postgresql://"+database.getElementsByTagName(HOST_NAME_KEY).item(0).getTextContent();
        databaseUsername = database.getElementsByTagName(DATABASE_USERNAME_KEY).item(0).getTextContent();
        databasePassword = database.getElementsByTagName(DATABASE_PASSWORD_KEY).item(0).getTextContent();

        // Node: Output
        Node outputNode = doc.getElementsByTagName("Output").item(0);

        // Node(s): SummaryCalculation
        SummaryCalculations = new ArrayList<String>(1);
        NodeList summaryList = ((Element) outputNode).getElementsByTagName("SummaryCalculation");
        for(int i=0; i < summaryList.getLength(); i++) {
            SummaryCalculations.add(summaryList.item(i).getTextContent());
        }
    }

    public String getGlobalSchema() {
        return globalSchema;
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

    public ArrayList<String> SummaryCalculations() {
        return SummaryCalculations;
    }
}
