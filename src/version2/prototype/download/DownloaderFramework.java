package version2.prototype.download;

import java.io.IOException;
import org.xml.sax.SAXException;

public abstract class DownloaderFramework {

    //process of downloading one file
    public abstract void download() throws IOException, DownloadFailedException, Exception, SAXException;

    // return the path of the downloaded file
    public abstract String getOutputFilePath();
}
