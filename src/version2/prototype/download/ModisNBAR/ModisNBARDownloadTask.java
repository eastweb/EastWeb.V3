package version2.prototype.download.ModisNBAR;

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
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.download.cache.*;
import version2.prototype.ConfigReadException;
import version2.prototype.ModisTile;

public class ModisNBARDownloadTask implements Runnable {
    private ProjectInfoFile projectInfo;
    private DataDate startDate;
    private static final String downloadedCache = Paths.get("").toAbsolutePath() + "\\ModisNBAR\\downloaded.xml.gz";
    private static final String onlineAvaliableCache = Paths.get("").toAbsolutePath() + "ModisNBAR\\avaliable.xml.gz";
    private List<DataDate> available;
    private List<DataDate> finished;
    private List<ModisTile> modisTilesDesired;
    private DownloadMetaData mode;

    public ModisNBARDownloadTask(DataDate dataDate, DownloadMetaData metaData, ProjectInfoFile project) {
        projectInfo = project;
        startDate = dataDate;
        mode = metaData;
    }

    @Override
    public void run(){
        ArrayList<DataDate> download = null;
        ArrayList<ModisTile> modisTiles = null;
        try {
            modisTiles = getModisTileList();
            download = getDownloadList();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        // if no data need be downloaded,end the task
        if (download == null || modisTiles == null) {
            return;
        }

        for (DataDate date : download) {
            for(ModisTile tile : modisTiles)
            {
                try {
                    FileUtils.forceMkdir(new File(getOutFolder(date)));
                    FileUtils.forceMkdir(new File(getQCOutFolder(date)));
                    new ModisNBARDownloader(date, tile, getOutFolder(date), getQCOutFolder(date), mode).download();
                } catch (Exception e) {
                    // update the downloadedCache
                    updateDownloadedCache();
                    e.printStackTrace();
                }
                // add into finished
                finished.add(date);
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
            ModisNBARCache cache = new ModisNBARCache(DataDate.today(), startDate, finished, modisTilesDesired);

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
        ModisNBARCache cache = new ModisNBARCache(DataDate.today(), startDate, finished, modisTilesDesired);

        cache.toFile(file);
        stop();
    }

    private String getOutFolder(DataDate date) throws ConfigReadException {
        return projectInfo.GetWorkingDir() + String.format("ModisNBAR\\Download\\%4d\\%03d\\", date.getYear(), date.getDayOfYear());
    }

    private String getQCOutFolder(DataDate date) throws ConfigReadException {
        return projectInfo.GetWorkingDir() + String.format("ModisNBAR\\QCDownload\\%4d\\%03d\\", date.getYear(), date.getDayOfYear());
    }

    private ArrayList<DataDate> getDownloadList() throws IOException {
        available = updateAvailabeCache();
        finished = new ArrayList<DataDate>();

        try {
            finished = ModisNBARCache.fromFile(new File(downloadedCache)).getDates();
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
            modisTilesDesired = ModisNBARCache.fromFile(new File(downloadedCache)).getTiles();
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

    private List<DataDate> updateAvailabeCache() {
        try {

            if (new File(onlineAvaliableCache).isFile()) {
                final ModisNBARCache cache = ModisNBARCache.fromFile(new File(onlineAvaliableCache));
                // Check freshness
                if (!CacheUtils.isFresh(cache)|| !cache.getStartDate().equals(startDate)) {
                    // Get a list of dates and construct a date cache
                    ModisNBARCache updatedCache = getOnlineDataList();
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
                ModisNBARCache updatedCache = getOnlineDataList();
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

    private ModisNBARCache getOnlineDataList() throws IOException {
        // Get a list of dates and construct a date cache
        ModisNBARCache updatedCache = null;

        try {
            updatedCache = new ModisNBARCache(DataDate.today(), startDate, ModisNBARDownloader.listDates(startDate), modisTilesDesired);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Write out the date cache
        final File file = new File(onlineAvaliableCache);

        FileUtils.forceMkdir(file.getParentFile());
        updatedCache.toFile(file);

        return updatedCache;
    }
}
