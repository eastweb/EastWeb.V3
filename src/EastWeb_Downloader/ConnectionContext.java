package EastWeb_Downloader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.ConnectException;

import EastWeb_Config.Config;
import EastWeb_ErrorHandling.ErrorLog;
import PluginMetaData.DownloadMetaData;


public class ConnectionContext {

    public static Object getConnection(DownloadMetaData metadata) throws ConnectException{
        Object connection=null;

        try {
            String mode = metadata.mode;
            String classnameInfo="version2.prototype.download."+mode+"ConnectionInfo";
            Class<?> clsInfo = Class.forName(classnameInfo);

            String classnameStg="version2.prototype.download."+mode;
            Class<?> clsStg=Class.forName(classnameStg);
            Class<?>[] paramDatadate=new Class[1];

            //create connectionInfo object according to mode type.
            Constructor<?> ctor= clsInfo.getDeclaredConstructor(DownloadMetaData.class);
            ctor.setAccessible(true);
            ConnectionInfo ci=(ConnectionInfo)ctor.newInstance(metadata);

            //build and return connection
            paramDatadate[0]=Class.forName("version2.prototype.download.ConnectionInfo");
            Object obj=clsStg.newInstance();
            Method method=clsStg.getDeclaredMethod("buildConn", paramDatadate);
            connection=method.invoke(obj, ci);
        } catch (Exception e) {
            ErrorLog.add(Config.getInstance(), "ConnectionContext.getConnection problem with creating conection.", e);
        }
        System.out.println(connection);
        return connection;
    }

    // removed
    //static void close(Object conn){}
}
