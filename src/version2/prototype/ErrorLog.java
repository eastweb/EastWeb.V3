package version2.prototype;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;


import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.util.FileSystem;

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
     * @param cause
     */
    public static void add(String message, Throwable cause)
    {
        String logFileName = getLogFileName();
        String logPath = ClassLoader.getSystemClassLoader().getResource(".").getPath();
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, cause);
            printToStderr(message, cause);
        }
    }

    /**
     * Reports an error to the error log in the error log directory root.
     * @param configInstance
     * @param message Error message, suitable for presentation to the user
     * @param cause Cause of the error, may be null
     */
    public static void add(Config configInstance, String message, Throwable cause)
    {
        String logFileName = getLogFileName();
        String logPath = configInstance.getErrorLogDir();
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, cause);
            printToStderr(message, cause);
        }
    }

    /**
     * Reports an error to the error log for the specified GlobalDownloader.
     * @param configInstance
     * @param pluginName
     * @param message Error message, suitable for presentation to the user
     * @param cause Cause of the error, may be null
     */
    public static void add(Config configInstance, String pluginName, String message, Throwable cause)
    {
        String logFileName = getLogFileName();
        String logPath = configInstance.getErrorLogDir();
        try {
            logPath = FileSystem.GetGlobalDownloadDirectory(configInstance, pluginName);
        } catch (ConfigReadException e) {
            add(configInstance, "Problem logging error.", e);
        }
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, cause);
            printToStderr(message, cause);
        }
    }

    /**
     * Reports an error to the error log for the specified project.
     * @param projectInfoFile
     * @param message Error message, suitable for presentation to the user
     * @param cause Cause of the error, may be null
     */
    public static void add(ProjectInfoFile projectInfoFile, String message, Throwable cause)
    {
        String logFileName = getLogFileName();
        String logPath = FileSystem.GetProjectDirectoryPath(projectInfoFile.GetWorkingDir(), projectInfoFile.GetProjectName());
        synchronized (sErrorLogLock) {
            printToLogFile(logPath + logFileName, message, cause);
            printToStderr(message, cause);
        }
    }

    private static void printToLogFile(String logPath, String message, Throwable cause)
    {
        try {
            final FileOutputStream fos = new FileOutputStream(logPath);
            PrintStream sErrorLogPrintStream = new PrintStream(fos);

            sErrorLogPrintStream.println(message);
            if (cause != null) {
                cause.printStackTrace(sErrorLogPrintStream);
            }
            sErrorLogPrintStream.println();
            sErrorLogPrintStream.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to the error log");
        }
    }

    private static void printToStderr(String message, Throwable cause) {
        System.err.println("ERROR: " + message);
        if (cause != null) {
            cause.printStackTrace();
        }
    }

    private static String getLogFileName() {
        LocalDateTime temp = LocalDateTime.now();
        return "Error_Log_" + LocalDate.now().getYear() + "_" + LocalDate.now().getMonthValue() + "_" + LocalDate.now().getDayOfMonth() + "_" + temp.getHour() + temp.getMinute() + temp.getSecond() + ".log";
    }
}