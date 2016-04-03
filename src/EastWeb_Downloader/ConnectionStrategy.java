package EastWeb_Downloader;

import java.io.IOException;

public abstract class ConnectionStrategy {

    public abstract Object buildConn(ConnectionInfo ci) throws IOException;

}
