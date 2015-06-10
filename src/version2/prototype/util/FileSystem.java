package version2.prototype.util;

import java.util.regex.Pattern;

import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.Scheduler.ProcessName;

public final class FileSystem {
    private FileSystem() {}

    public static String GetRootDirectoryPath(ProjectInfoFile data)
    {
        String path = data.GetWorkingDir();
        if(!path.endsWith("/") && path.endsWith("\\")) {
            path += "/";
        }
        return path;
    }

    public static String DescribeNamingRules()
    {
        return new String("Names are case-sensitive and must start with either a letter or underscore and can contain letters, digits, "
                + "and underscores.");
    }

    public static String[] ListNamingRules()
    {
        String[] rules = new String[3];

        rules[0] = "Case-sensitive";
        rules[1] = "Start with either a letter or underscore";
        rules[2] = "Contains only letters, digits, or underscores";
        return rules;
    }

    public static String StandardizeName(String name)
    {
        name = name.replaceAll("[^a-zA-Z0-9_]", "_");
        if(!Pattern.matches("^[a-zA-Z_]", name))
        {
            name = "_" + name.substring(1);
        }
        return name;
    }

    public static String GetProjectDirectoryPath(String workingDir, String projectName)
    {
        return CheckWorkingDir(workingDir) + "Projects/" + StandardizeName(projectName) + "/";
    }

    public static String GetProcessDirectoryPath(String workingDir, String projectName, String pluginName, ProcessName processName)
    {
        return GetProjectDirectoryPath(workingDir, projectName) + StandardizeName(pluginName) + "/" + GetProcessDirectoryName(processName) + "/";
    }

    public static String GetProcessOutputDirectoryPath(String workingDir, String projectName, String pluginName, ProcessName processName)
    {
        return GetProcessDirectoryPath(workingDir, projectName, pluginName, processName) + "Output/";
    }

    public static String GetProcessWorkerTempDirectoryPath(String workingDir, String projectName, String pluginName, ProcessName processName)
    {
        return GetProcessDirectoryPath(workingDir, projectName, pluginName, processName) + "Temp/";
    }

    public static String GetDownloadDirectory(String workingDir, String dataName)
    {
        return CheckWorkingDir(workingDir) + "Downloads/" + StandardizeName(dataName) + "/";
    }

    private static String GetProcessDirectoryName(ProcessName name)
    {
        String dirName = null;

        switch(name)
        {
        case DOWNLOAD:
            dirName = "Download";
            break;
        case INDICES:
            dirName = "Indices";
            break;
        case PROCESSOR:
            dirName = "Processor";
            break;
        default:    // SUMMARY
            dirName = "Summary";
            break;
        }
        return dirName;
    }

    private static String CheckWorkingDir(String workingDir)
    {
        if(!workingDir.endsWith("/") && workingDir.endsWith("\\")) {
            workingDir += "/";
        }
        return workingDir;
    }
}
