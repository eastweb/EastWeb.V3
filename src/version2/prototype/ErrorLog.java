package version2.prototype;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
        String logPath = null;
        try {
            logPath = ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath();
            while(logPath.startsWith("\\") || logPath.startsWith("/")) {
                logPath = logPath.substring(1);
            }
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
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
     * @param dataName  - name of the data files
     * @param message Error message, suitable for presentation to the user
     * @param e Cause of the error, may be null
     */
    public static void add(Config configInstance, String pluginName, String dataName, String message, Throwable e)
    {
        String logFileName = getLogFileName();
        String logPath = configInstance.getErrorLogDir();
        try {
            logPath = FileSystem.GetGlobalDownloadDirectory(configInstance, pluginName, dataName);
        } catch (ConfigReadException cause) {
            add(configInstance, "Problem logging error.", cause);
        }
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
        }
    }

    /**
     * Reports an error to the error log for the specified project. Sends the custom message as a new log message to the UI.
     * @param scheduler
     * @param message Error message, suitable for presentation to the user
     * @param e Cause of the error, may be null
     */
    public static void add(Scheduler scheduler, String message, Throwable e)
    {
        String logFileName = getLogFileName();
        String logPath = FileSystem.GetProjectDirectoryPath(scheduler.projectInfoFile.GetWorkingDir(), scheduler.projectInfoFile.GetProjectName());
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
            scheduler.NotifyUI(new GeneralUIEventObject(e.getCause() != null ? e.getCause() : e, message + " [Error Logged: " + logPath + "]"));
        }
    }

    /**
     * Reports an error to the error log for the specified project. Sends the custom message as a new log message to the UI.
     * @param process
     * @param message Error message, suitable for presentation to the user
     * @param e Cause of the error, may be null
     */
    public static void add(Process process, String message, Throwable e)
    {
        String logFileName = getLogFileName();
        String logPath = FileSystem.GetProjectDirectoryPath(process.projectInfoFile.GetWorkingDir(), process.projectInfoFile.GetProjectName());
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
            process.NotifyUI(new GeneralUIEventObject(e.getCause() != null ? e.getCause() : e, message + " [Error Logged: " + logPath + "]"));
        }
    }

    /**
     * Reports an error to the error log for the specified process and project. Sends the custom message as a new log message to the UI.
     * @param processName
     * @param scheduler
     * @param message Error message, suitable for presentation to the user
     * @param e Cause of the error, may be null
     */
    public static void add(ProcessName processName, Scheduler scheduler, String message, Throwable e)
    {
        String logFileName = processName + "_" + getLogFileName();
        String logPath = FileSystem.GetProjectDirectoryPath(scheduler.projectInfoFile.GetWorkingDir(), scheduler.projectInfoFile.GetProjectName());
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, e);
            printToStderr(message, e);
            scheduler.NotifyUI(new GeneralUIEventObject(e.getCause() != null ? e.getCause() : e, message + " [Error Logged: " + logPath + "]"));
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