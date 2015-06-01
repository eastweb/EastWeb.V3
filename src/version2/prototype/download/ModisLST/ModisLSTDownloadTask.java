package version2.prototype.download.ModisLST;


import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import version2.prototype.DataDate;
import version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData;
import version2.prototype.download.cache.*;
import version2.prototype.ConfigReadException;
import version2.prototype.DirectoryLayout;
import version2.prototype.ModisTile;
import version2.prototype.download.ModisLST.ModisLSTDownloader;

public class ModisLSTDownloadTask implements Runnable {
    DataDate startDate;
    static final String downloadedCache = Paths.get("").toAbsolutePath() + "\\ModisLST\\downloaded.xml.gz";
    static final String onlineAvaliableCache = Paths.get("").toAbsolutePath() + "ModisLST\\avaliable.xml.gz";
    List<DataDate> available;
    List<DataDate> finished;
    List<ModisTile> modisTilesDesired;
    DownloadMetaData mode;

    public ModisLSTDownloadTask(DataDate dataDate, DownloadMetaData metaData) {
        startDate = dataDate;
        mode = metaData;
    }

    @Override
    public void run(){
        ArrayList<DataDate> download = null;
        ArrayList<ModisTile> modisTiles = null;
        try {

            try {
                download = getDownloadList();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            modisTiles = getModisTileList();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        // if no data need be downloaded,end the task
        if (download == null || modisTiles == null) {
            return;
        }

        for (DataDate item : download) {
            for(ModisTile tile : modisTiles)
            {
                try {
                    FileUtils.forceMkdir(getoutFile(item, tile).getParentFile());
                    new ModisLSTDownloader(item,tile,getoutFile(item, tile),mode) ;

                } catch (Exception e) {
                    // update the downloadedCache
                    updateDownloadedCache();
                    e.printStackTrace();
                }
                // add into finished
                finished.add(item);
            }
        }
        // update the downloadedCache
        updateDownloadedCache();
    }

    private void updateDownloadedCache() {
        // update the downloadedCache
        final File file = new File(downloadedCache);
        try {
            FileUtils.forceMkdir(file.getParentFile());
            ModisLSTCache cache = new ModisLSTCache(DataDate.today(), startDate, finished, modisTilesDesired);

            cache.toFile(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stop() throws InterruptedException, IOException {
        this.wait();
        // update the downloadedCache
        final File file = new File(downloadedCache);
        FileUtils.forceMkdir(file.getParentFile());
        ModisLSTCache cache = new ModisLSTCache(DataDate.today(), startDate, finished, modisTilesDesired);

        cache.toFile(file);
        stop();
    }

    private File getoutFile(DataDate date, ModisTile tile) throws ConfigReadException {
        return DirectoryLayout.getModisDownload("LST", date, tile);
    }

    private ArrayList<DataDate> getDownloadList() throws IOException, ParserConfigurationException, SAXException {
        available = updateAvailabeCache();
        finished = new ArrayList<DataDate>();

        try {
            finished = ModisLSTCache.fromFile(new File(downloadedCache)).getDates();
        } catch (IOException e) {

        }

        if (available == null) {
            return new ArrayList<DataDate>();
        } else if (finished == null) {
            return (ArrayList<DataDate>) available;
        } else {
            ArrayList<DataDate> modifiableAvailabe=new ArrayList<DataDate>(available);
            ArrayList<DataDate> modifiableFinished=new ArrayList<DataDate>(finished);
            Collections.sort(modifiableAvailabe);
            Collections.sort(modifiableFinished);
            available=modifiableAvailabe;
            finished=modifiableFinished;
            // compare available list and downloaded list, remove already downloaded date
            if(finished.isEmpty()){
                return (ArrayList<DataDate>) available;
            }else{
                int match = available.indexOf(finished.get(0));
                if (match == -1) {
                    return (ArrayList<DataDate>) available;
                } else {
                    for (int i = match; i < match + finished.size(); i++) {
                        available.remove(match);
                    }
                    return (ArrayList<DataDate>) available;
                }
            }
        }
    }

    private ArrayList<ModisTile> getModisTileList() throws IOException {
        try {
            modisTilesDesired = ModisLSTCache.fromFile(new File(downloadedCache)).getTiles();
        } catch (IOException e){

        }
        if (modisTilesDesired == null) {
            return new ArrayList<ModisTile>();
        } else
        {
            ArrayList<ModisTile> modifiableModisTiles = new ArrayList<ModisTile>(modisTilesDesired);
            Collections.sort(modifiableModisTiles);
            modisTilesDesired = modifiableModisTiles;

            return (ArrayList<ModisTile>) modisTilesDesired;
        }
    }

    private List<DataDate> updateAvailabeCache() throws ParserConfigurationException, SAXException {
        try {

            if (new File(onlineAvaliableCache).isFile()) {
                final ModisLSTCache cache = ModisLSTCache.fromFile(new File(onlineAvaliableCache));
                // Check freshness
                if (!CacheUtils.isFresh(cache)|| !cache.getStartDate().equals(startDate)) {
                    // Get a list of dates and construct a date cache
                    ModisLSTCache updatedCache = getOnlineDataList();
                    // Write out the date cache
                    final File file = new File(onlineAvaliableCache);

                    FileUtils.forceMkdir(file.getParentFile());
                    updatedCache.toFile(file);

                    return updatedCache.getDates();
                } else {
                    return cache.getDates();
                }

            } else {
                // Get a list of dates and construct a date cache
                ModisLSTCache updatedCache = getOnlineDataList();
                // Write out the date cache
                final File file = new File(onlineAvaliableCache);

                FileUtils.forceMkdir(file.getParentFile());
                updatedCache.toFile(file);

                return updatedCache.getDates();
            }

        } catch (IOException e) {
            return null;
        }

    }

    private ModisLSTCache getOnlineDataList() throws IOException, ParserConfigurationException, SAXException {
        // Get a list of dates and construct a date cache
        ModisLSTCache updatedCache = null;

        updatedCache = new ModisLSTCache(DataDate.today(), startDate, ModisLSTDownloader.listDates(startDate), modisTilesDesired);
        // Write out the date cache
        final File file = new File(onlineAvaliableCache);

        FileUtils.forceMkdir(file.getParentFile());
        updatedCache.toFile(file);

        return updatedCache;
    }
}
