package EastWeb_Downloader;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import EastWeb_Config.ConfigReadException;
import PluginMetaData.DownloadMetaData;

public class HTTPConnectionInfo extends ConnectionInfo{
    String url;

    HTTPConnectionInfo(DownloadMetaData metadata) throws ParserConfigurationException, SAXException, IOException {
        mode="HTTP";
        url=metadata.myHttp.url;
    }

    HTTPConnectionInfo(String url) throws ConfigReadException {
        mode="HTTP";
        this.url=url;
    }
}
