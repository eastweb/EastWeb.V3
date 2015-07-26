package version2.prototype.download;

import java.io.IOException;
import org.xml.sax.SAXException;

public abstract class DownloaderFramework {

    //process of downloading one file
    public abstract void download() throws IOException, DownloadFailedException, SAXException, Exception;

<<<<<<< Upstream, based on origin/master
    // return the path of the downloaded file
    public abstract String getOutputFilePath();
=======
>>>>>>> ad7b88f Fixed compile errors and updated some tests and for threading framework classes. Updated plugin parser.
}
