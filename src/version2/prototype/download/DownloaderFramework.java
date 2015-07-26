package version2.prototype.download;

import java.io.IOException;
import org.xml.sax.SAXException;

public abstract class DownloaderFramework {

    public abstract void download() throws IOException, DownloadFailedException, SAXException, Exception;

}
