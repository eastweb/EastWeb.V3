package version2.prototype.download.ModisLST;
import java.io.IOException;
import java.util.*;

import version2.prototype.Config;
import version2.prototype.ConfigReadException;
import version2.prototype.DataDate;
import version2.prototype.DirectoryLayout;
import version2.prototype.ModisTile;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.ModisDownloader;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.DownloaderFramework.Mode;
import version2.prototype.download.DownloaderFramework.DataType;
import version2.prototype.download.ModisProduct;
import version2.prototype.Settings;


/**
 * Implements the MODIS LST component of the download module.
 */

public final class ModisLstDownloader extends ModisDownloader {
    // private static final String ROOT_DIRECTORY = "/MOLT/MOD11A2.005";
    private static final String PRODUCT = "MOD11A2";
    private static DownloadMetaData metaData;
    /**
     * Constructs a new ModisLstDownloader.
     */
    public ModisLstDownloader(DataDate date, ModisTile tile, DataDate processed)
            throws ConfigReadException {
        super(date, tile, processed, DirectoryLayout.getModisDownload(
                "LST", date, tile));
    }

    public ModisLstDownloader(DataDate date, ModisTile tile, DataDate processed,DownloadMetaData data)
            throws ConfigReadException {
        super(date, tile, processed, DirectoryLayout.getModisDownload(
                "LST", date, tile),data);
        metaData=data;
    }

    public static final List<DataDate> listDates(DataDate startDate)
            throws IOException {
        Mode mode = Settings.getMode(DataType.MODIS);
        Object conn =
                ConnectionContext.getConnection(ConvertM(mode), ConvertDT(DataType.MODIS),metaData);
        return listDates(startDate,conn);
    }

    public static final Map<ModisTile, DataDate> listTiles(DataDate date)
            throws IOException {

        return listTiles(date, Config.getInstance().getModisLstUrl(), PRODUCT);
    }

    public static String ConvertM(Mode mode){
        if(mode==Mode.HTTP) {
            return "HTTP";
        } else if(mode==Mode.FTP) {
            return "FTP";
        } else {
            return null;
        }
    }

    public static String ConvertDT(DataType dt){
        if(dt==DataType.MODIS) {
            return "MODIS";
        } else if(dt==DataType.TRMM) {
            return "TRMM";
        } else if(dt==DataType.ETO) {
            return "ETO";
        } else if(dt==DataType.TRMM_3B42) {
            return "TRMM_3B42";
        } else if(dt==DataType.NLDAS) {
            return "NLDAS";
        } else {
            return null;
        }
    }

    @Override
    protected String getRootDir() {
        // TODO: handle better
        try {
            return Config.getInstance().getModisLstUrl();
        } catch (ConfigReadException e) {
            return "";
        }
    }

    @Override
    protected String getProduct() {
        return PRODUCT;
    }
}