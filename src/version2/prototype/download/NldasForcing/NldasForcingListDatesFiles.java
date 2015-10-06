package version2.prototype.download.NldasForcing;

import java.io.IOException;
import java.net.ConnectException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import version2.prototype.Config;
import version2.prototype.DataDate;
import version2.prototype.ErrorLog;
import version2.prototype.PluginMetaData.DownloadMetaData;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.download.ConnectionContext;
import version2.prototype.download.ListDatesFiles;

public class NldasForcingListDatesFiles extends ListDatesFiles {

    public NldasForcingListDatesFiles(DataDate startDate, DownloadMetaData data, ProjectInfoFile project) throws IOException {
        super(startDate, data, project);
    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesFTP()
    {
        Map<DataDate, ArrayList<String>>  mapDatesToFiles = new HashMap<DataDate, ArrayList<String>>();
        FTPClient ftpClient = null;
        String mRootDir = mData.myFtp.rootDir;

        try
        {
            ftpClient = (FTPClient) ConnectionContext.getConnection(mData);

            ftpClient.enterLocalPassiveMode();

            if(!ftpClient.changeWorkingDirectory(mRootDir)) {
                throw new IOException("Couldn't navigate to " + mRootDir);
            }

            // List out all of the year directories.
            FTPFile[] yearDirs = ftpClient.listDirectories();

            for(FTPFile yearDir : yearDirs)
            {
                // There is another file named "doc" in the host's file system
                if(yearDir.getName().equalsIgnoreCase("doc") || !yearDir.isDirectory()) {
                    continue;
                }

                if(Integer.parseInt(yearDir.getName()) >= sDate.getYear())
                {
                    for(FTPFile dayOfYearDir : ftpClient.listDirectories(mRootDir + yearDir.getName() + "/"))
                    {
                        // Continue if the day of year is less than the start date
                        if(Integer.parseInt(yearDir.getName()) ==  sDate.getYear() &&
                                Integer.parseInt(dayOfYearDir.getName()) < sDate.getDayOfYear()) {
                            continue;
                        }

                        ArrayList<String> files = new ArrayList<String>();
                        for(FTPFile hourlyFile : ftpClient.listFiles(mRootDir + yearDir.getName() + "/" + dayOfYearDir.getName() + "/"))
                        {
                            // Continue if the file is the .xml companion file
                            if(!hourlyFile.getName().endsWith(".grb")) {
                                continue;
                            }

                            if(Integer.parseInt(yearDir.getName()) == sDate.getYear() &&
                                    Integer.parseInt(dayOfYearDir.getName()) == sDate.getDayOfYear())
                            {
                                int startIndex = hourlyFile.getName().indexOf(".002.grb") - 4;
                                if(Integer.parseInt(hourlyFile.getName().substring(startIndex, (startIndex+2))) >= sDate.getHour()) {
                                    files.add(hourlyFile.getName());
                                }
                            }
                            else {
                                files.add(hourlyFile.getName());
                            }
                        }
                        mapDatesToFiles.put(new DataDate(Integer.parseInt(dayOfYearDir.getName()), Integer.parseInt(yearDir.getName())), files);
                    }
                }
            }
        }
        catch (Exception e) {
            ErrorLog.add(Config.getInstance(), "NldasForcing", mData.name, "NldasForcingListDatesFiles.Actual_ListDatesFilesFTP problem while creating list using FTP.", e);
        }
        finally
        {
            try {
                if(ftpClient != null && ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            }
            catch (IOException e) {
                ErrorLog.add(Config.getInstance(), "NldasForcing", mData.name, "NldasForcingListDatesFiles.Actual_ListDatesFilesFTP problem while logging out and disconnecting FTP connection.", e);
            }
        }

        return mapDatesToFiles;
    }

    /*
     * This is the theoretical method to find the files available from the ftp server.
     * This method builds the list without actually traversing the ftp server.
     * (this method takes 250 milliseconds compared to approximately 3 minutes for actually traversing the ftp server)
     * This method can be either removed or left if there is a desire to have it regardless.
     * - Chris Plucker 7/26/2015
     */
    protected Map<DataDate, ArrayList<String>> Theoretical_ListDatesFilesFTP()
    {
        String fileFormat = "NLDAS_FORA0125_H.A%04d%02d%02d.%02d00.002.grb";
        Map<DataDate, ArrayList<String>>  mapDatesToFiles = new HashMap<DataDate, ArrayList<String>>();
        DataDate today = new DataDate(LocalDate.now());

        // Get the files from the start year
        for (int month = sDate.getMonth(); month <= 12; month++)
        {
            if(month != sDate.getMonth())
            {
                for(int day = 1; day <= 31; day++)
                {
                    ArrayList<String> files = new ArrayList<String>();
                    for (int hour = 0; hour <= 23; hour++)
                    {
                        files.add(String.format(fileFormat, sDate.getYear(), month, day, hour));
                    }

                    try { mapDatesToFiles.put(new DataDate(day, month, sDate.getYear()), files); }
                    catch(DateTimeException e) { }
                }
            }
            else
            {
                for(int day = sDate.getDay(); day <= 31; day++)
                {
                    if(day != sDate.getDay())
                    {
                        ArrayList<String> files = new ArrayList<String>();
                        for (int hour = 0; hour <= 23; hour++)
                        {
                            files.add(String.format(fileFormat, sDate.getYear(), month, day, hour));
                        }
                        try { mapDatesToFiles.put(new DataDate(day, month, sDate.getYear()), files); }
                        catch(DateTimeException e) { }
                    }
                    else
                    {
                        ArrayList<String> files = new ArrayList<String>();
                        for (int hour = sDate.getHour(); hour <= 23; hour++)
                        {
                            files.add(String.format(fileFormat, sDate.getYear(), month, day, hour));
                        }
                        try { mapDatesToFiles.put(new DataDate(day, month, sDate.getYear()), files); }
                        catch(DateTimeException e) { }
                    }
                }
            }
        }

        // Get the files between the start year and the current year
        for(int year = sDate.getYear()+1; year < today.getYear(); year++)
        {
            for(int month = 1; month <= 12; month++)
            {
                for(int day = 1; day <= 31; day++)
                {
                    ArrayList<String> files = new ArrayList<String>();
                    for (int hour = 0; hour <= 23; hour++)
                    {
                        files.add(String.format(fileFormat, year, month, day, hour));
                    }
                    try { mapDatesToFiles.put(new DataDate(day, month, year), files); }
                    catch(DateTimeException e) { }
                }
            }
        }

        // Get the files up up to the current date (approximately, as they take about 3 days to process, so up to today's DayOfMonth-3);
        for (int month = 1; month <= today.getMonth(); month++)
        {
            if(month != today.getMonth())
            {
                for(int day = 1; day <= 31; day++)
                {
                    ArrayList<String> files = new ArrayList<String>();
                    for (int hour = 0; hour <= 23; hour++)
                    {
                        files.add(String.format(fileFormat, today.getYear(), month, day, hour));
                    }
                    try { mapDatesToFiles.put(new DataDate(day, month, today.getYear()), files); }
                    catch(DateTimeException e) { }
                }
            }
            else
            {
                for (int day = 1; day <= (today.getDay()-3); day++)
                {
                    ArrayList<String> files = new ArrayList<String>();
                    for (int hour = 0; hour <= 23; hour++)
                    {
                        files.add(String.format(fileFormat, today.getYear(), month, day, hour));
                    }
                    try { mapDatesToFiles.put(new DataDate(day, month, today.getYear()), files); }
                    catch(DateTimeException e) { }
                }
            }
        }

        return mapDatesToFiles;
    }

    @Override
    protected Map<DataDate, ArrayList<String>> ListDatesFilesHTTP() {
        return null;
    }
}
