package EastWeb_Downloader;

import java.io.IOException;

public class FTP extends ConnectionStrategy{

    @Override
    public  Object buildConn(ConnectionInfo ci) throws IOException {
        FTPConnectionInfo fci=(FTPConnectionInfo)ci;

        return FTPClientPool.getFtpClient(fci.hostName, fci.userName, fci.password);
    }
}