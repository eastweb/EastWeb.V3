package version2.prototype.download;

import java.io.IOException;
import org.xml.sax.SAXException;

public abstract class DownloaderFramework {

    public abstract void download() throws IOException, DownloadFailedException, Exception, SAXException;

    /* the following enum should be removed or modified since it's type does not match
     * the mode (String type) given in the DownloadMetaData
     */
    public enum Mode{
        HTTP,
        FTP
    }

}
