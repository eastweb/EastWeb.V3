package version2.prototype;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;







import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.Scheduler;
import version2.prototype.util.FileSystem;
import version2.prototype.util.GeneralUIEventObject;

/**
 * @author michael.devos
 *
 */
public final class ErrorLog {
    private ErrorLog() {
        // Don't allow instantiation
    }

    private static final Object sErrorLogLock = new Object();


    /**Reports an error to the path of the class loader or where the program was executed from. Used mainly to handle Config issues as the others require a Config instance.
     * @param message
     * @param e
     */
    public static void add(String message, Throwable e)
    {
        String logFileName = getLogFileName();
        String logPath = ClassLoader.getSystemClassLoader().getResource(".").getPath();
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
        }
    }

    /**
     * Reports an error to the error log in the error log directory root.
     * @param configInstance
     * @param message Error message, suitable for presentation to the user
     * @param e Cause of the error, may be null
     */
    public static void add(Config configInstance, String message, Throwable e)
    {
        String logFileName = getLogFileName();
        String logPath = configInstance.getErrorLogDir();
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
        }
    }

    /**
     * Reports an error to the error log for the specified GlobalDownloader.
     * @param configInstance
     * @param pluginName
     * @param message Error message, suitable for presentation to the user
     * @param e Cause of the error, may be null
     */
    public static void add(Config configInstance, String pluginName, String message, Throwable e)
    {
        String logFileName = getLogFileName();
        String logPath = configInstance.getErrorLogDir();
        try {
            logPath = FileSystem.GetGlobalDownloadDirectory(configInstance, pluginName);
        } catch (ConfigReadException cause) {
            add(configInstance, "Problem logging error.", cause);
        }
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
        }
    }

    /**
     * Reports an error to the error log for the specified project.
     * @param projectInfoFile
     * @param scheduler
     * @param message Error message, suitable for presentation to the user
     * @param e Cause of the error, may be null
     */
    public static void add(ProjectInfoFile projectInfoFile, Scheduler scheduler, String message, Throwable e)
    {
        String logFileName = getLogFileName();
        String logPath = FileSystem.GetProjectDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName());
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
            scheduler.NotifyUI(new GeneralUIEventObject(e.getCause(), message));
        }
    }

    /**
     * Reports an error to the error log for the specified project.
     * @param projectInfoFile
     * @param process
     * @param message Error message, suitable for presentation to the user
     * @param e Cause of the error, may be null
     */
    public static void add(ProjectInfoFile projectInfoFile, Process process, String message, Throwable e)
    {
        String logFileName = getLogFileName();
        String logPath = FileSystem.GetProjectDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName());
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
            process.NotifyUI(new GeneralUIEventObject(e.getCause(), message));
        }
    }

    /**
     * Reports an error to the error log for the specified project.
     * @param workingDirectory
     * @param projectName
     * @param process
     * @param message Error message, suitable for presentation to the user
     * @param e Cause of the error, may be null
     */
    public static void add(String workingDirectory, String projectName, Process process, String message, Throwable e)
    {
        String logFileName = getLogFileName();
        String logPath = FileSystem.GetProjectDirectoryPath(workingDirectory, projectName);
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
            process.NotifyUI(new GeneralUIEventObject(e.getCause() != null ? e.getCause() : e, message));
        }
    }

    /**
     * Reports an error to the error log for the specified process and project.
     * @param projectInfoFile
     * @param process
     * @param scheduler
     * @param message Error message, suitable for presentation to the user
     * @param e Cause of the error, may be null
     */
    public static void add(ProjectInfoFile projectInfoFile, ProcessName process, Scheduler scheduler, String message, Throwable e)
    {
        String logFileName = process + "_" + getLogFileName();
        String logPath = FileSystem.GetProjectDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName());
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
            scheduler.NotifyUI(new GeneralUIEventObject(e.getCause(), message));
        }
    }

    private static void printToLogFile(String logPath, String message, Throwable e)
    {
        try {
            final FileOutputStream fos = new FileOutputStream(logPath);
            PrintStream sErrorLogPrintStream = new PrintStream(fos);

            sErrorLogPrintStream.println(message);
            Throwable tempError = e;
            do{
                if (tempError != null) {
                    tempError.printStackTrace(sErrorLogPrintStream);

                    if(tempError instanceof SQLException)
                    {
                        SQLException sqlError = (SQLException) tempError;
                        tempError = sqlError.getNextException();
                    }
                    else
                    {
                        tempError = null;
                    }
                }
            }while(tempError != null);
            sErrorLogPrintStream.println();
            sErrorLogPrintStream.flush();
        } catch (Exception cause) {
            System.err.println("Failed to write to the error log");
        }
    }

    private static void printToStderr(String message, Throwable r) {
        System.err.println("ERROR: " + message);
        if (r != null) {
            r.printStackTrace();
        }
    }

    private static String getLogFileName() {
        LocalDateTime temp = LocalDateTime.now();
        return "Error_Log_" + LocalDate.now().getYear() + "_" + LocalDate.now().getMonthValue() + "_" + LocalDate.now().getDayOfMonth() + "_" + temp.getHour() + temp.getMinute() + temp.getSecond() + ".log";
    }
}