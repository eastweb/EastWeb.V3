package version2.prototype.download.ModisLST;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import version2.prototype.ConfigReadException;
import version2.prototype.DataDate;
import version2.prototype.ModisTile;
import version2.prototype.Config;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.DownloadFailedException;
import version2.prototype.download.DownloadUtils;
import version2.prototype.download.DownloaderFramework;


public class ModisLSTDownloader extends DownloaderFramework{

    private static final String PRODUCT = "MOD11A2";
    private static final String getModisDateDir(String rootDir, DataDate date) {
        return String.format("%s/%04d.%02d.%02d", rootDir, date.getYear(),
                date.getMonth(), date.getDay());
    }

    private final DataDate mDate;
    private final ModisTile mTile;
    private final File mOutFile;
    private static DownloadMetaData metaData;

    public ModisLSTDownloader(DataDate date, ModisTile tile,
            File outFile,DownloadMetaData data) {
        mDate = date;
        mTile = tile;
        mOutFile = outFile;
        metaData = data;
    }

    /**
     * Builds and returns a list containing all of the available data dates no
     * earlier than the specified start date.
     *
     * @param startDate
     *            If non-null, specifies the inclusive lower bound for the
     *            returned data date list; if null, data dates are not filtered
     * @throws IOException
     */
    public static final List<DataDate> listDates(DataDate startDate)
            throws IOException {
        String mode = metaData.mode;
        Object conn =
                ConnectionContext.getConnection(mode, "ModisLST",metaData);
        if(mode=="HTTP")
        {
            final Pattern re = Pattern.compile("(\\d{4})\\.(\\d{2})\\.(\\d{2})(/)");
            byte[] downloadPage = null;
            try {
                downloadPage =
                        DownloadUtils.downloadToByteArray((URLConnection) conn);
            } catch (ConnectException e) {
                throw e;
            }
            final HtmlDocumentBuilder builder = new HtmlDocumentBuilder();
            Document pagedoc = null;
            try {
                pagedoc = builder.parse(new ByteArrayInputStream(downloadPage));
            } catch (SAXException e) {
                throw new IOException(
                        "Failed to parse the ModisLST download page", e);
            }

            final NodeList dirlist = pagedoc.getElementsByTagName("a");

            final List<DataDate> list = new ArrayList<DataDate>();

            for (int i = 0; i < dirlist.getLength(); ++i) {
                final String dir =
                        ((Element) dirlist.item(i)).getAttribute("href");

                // Match the filename against the known pattern of a MODIS NBAR
                // date directory
                final Matcher matcher = re.matcher(dir);
                if (matcher.matches()) {
                    final int year = Integer.parseInt(matcher.group(1));
                    final int month = Integer.parseInt(matcher.group(2));
                    final int day = Integer.parseInt(matcher.group(3));

                    final DataDate dataDate = new DataDate(day, month, year);
                    if (startDate == null || dataDate.compareTo(startDate) >= 0) {
                        list.add(dataDate);
                    }
                }
            }

            return list;
        }else {
            // TODO:FTP
            return null;
        }
    }


    @Override
    public final void download() throws IOException, ConfigReadException,
    DownloadFailedException, SAXException, Exception {
        String mode = metaData.mode;
        // Build a regular expression that will match any HDF files with the specified date and tile

        // (and any processing date)
        if (mode == "HTTP"){
            final Pattern re = Pattern.compile(String.format(
                    "%s\\.A%04d%03d\\.h%02dv%02d\\.005\\.\\d{13}\\.hdf",
                    getProduct(),
                    mDate.getYear(),
                    mDate.getDayOfYear(),
                    mTile.getHTile(),
                    mTile.getVTile()
                    // mProcessed.getYear(),
                    // mProcessed.getDayOfYear()
                    ));
            String url_str = getModisDateDir(getRootDir(), mDate);
            URL url = new URL(url_str);

            // list the files
            // catch and throw connectionException.
            byte[] downloadPage = null;
            try {
                downloadPage = DownloadUtils.downloadToByteArray(url);
            }catch(ConnectException e){
                throw e;
            }

            // Parse it into a DOM tree
            final HtmlDocumentBuilder builder = new HtmlDocumentBuilder();
            Document pagedoc = null;
            try {
                pagedoc = builder.parse(new ByteArrayInputStream(downloadPage));
            } catch (SAXException e) {
                throw new IOException(
                        "Failed to parse the ModisLST download page", e);
            }

            final NodeList dirlist = pagedoc.getElementsByTagName("a");

            try {
                // List files and select the best match
                String bestMatch = null;
                for (int i = 0; i < dirlist.getLength(); ++i) {
                    final String dir =
                            ((Element) dirlist.item(i)).getAttribute("href");

                    if (re.matcher(dir).matches()
                            && (bestMatch == null || dir.compareTo(bestMatch) > 0)) {
                        // This file is either the first match or has a more
                        // recent processing date than the current best match
                        bestMatch = dir;
                    }
                }

                if (bestMatch == null) {
                    throw new FileNotFoundException();
                }

                url_str += "/" + bestMatch;
                // Download the archive
                url = new URL(url_str);

                DownloadUtils.downloadToFile(url, mOutFile);

            } catch (IOException e) { // FIXME: ugly fix so that the system
                // doesn't repeatedly try and fail
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                throw e;
            }
        }
    }


    protected String getRootDir() {
        // TODO: handle better
        try {
            return Config.getInstance().getModisLstUrl();
        } catch (ConfigReadException e) {
            return "";
        }
    }

}
