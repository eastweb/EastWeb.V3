package EastWeb_Downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;

public final class DownloadUtils {

    private DownloadUtils() {
    }

    public static final void download(FTPClient ftp, String remoteFilename, File localFile) throws IOException
    {
        //        System.out.println(remoteFilename);
        final OutputStream outStream = new FileOutputStream(localFile);

        try {
            if (!ftp.retrieveFile(remoteFilename, outStream)) {
                throw new IOException("Download failed");
            }
            outStream.close();
        } catch (IOException e) {
            IOUtils.closeQuietly(outStream);
            FileUtils.deleteQuietly(localFile);
            throw e;
        }
    }

    public static final void downloadToStream(URL url, OutputStream outStream) throws IOException {
        URLConnection conn = url.openConnection();
        downloadToStream(conn, outStream);
    }

    public static final void downloadToStream(URLConnection conn, OutputStream outStream) throws IOException {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection)conn).setConnectTimeout(30000);
            ((HttpURLConnection)conn).connect();
            final int code = ((HttpURLConnection)conn).getResponseCode();

            if (code != 200) {
                throw new IOException("HTTP request returned code " + code);
            }
        }

        final BufferedInputStream inStream = new BufferedInputStream(conn.getInputStream());

        try {
            final byte[] buffer = new byte[4096];
            int numBytesRead;
            while ((numBytesRead = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, numBytesRead);
            }
        } finally {
            inStream.close();
        }
    }

    public static final void downloadToFile(URLConnection connection, File localFile) throws IOException {
        final OutputStream outStream = new FileOutputStream(localFile);

        try {
            downloadToStream(connection, outStream);
            outStream.close();
        } catch (IOException e) {
            IOUtils.closeQuietly(outStream);
            FileUtils.deleteQuietly(localFile);
            throw e;
        }
    }

    public static final void downloadToFile(URL url, File localFile) throws IOException {
        final BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(localFile));

        try {
            downloadToStream(url, outStream);
            outStream.close();
        } catch (IOException e) {
            IOUtils.closeQuietly(outStream);
            FileUtils.deleteQuietly(localFile);
            throw e;
        }
    }

    public static final byte[] downloadToByteArray(URL url) throws IOException {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        downloadToStream(url, outStream);

        return outStream.toByteArray();
    }

    public static final byte[] downloadToByteArray(URLConnection conn) throws IOException {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        downloadToStream(conn, outStream);

        return outStream.toByteArray();
    }
}