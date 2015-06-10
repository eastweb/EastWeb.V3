package version2.prototype.util;

import java.util.regex.Pattern;

import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;

public final class FileSystem {
    private FileSystem() {}

    public static String GetRootDirectoryPath(ProjectInfoFile data)
    {
        String path = data.workingDir;
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

    public static String GetProjectDirectoryPath(ProjectInfoFile data, String projectName)
    {
        return GetRootDirectoryPath(data) + "Projects/" + StandardizeName(projectName) + "/";
    }

    public static String GetProcessDirectoryPath(ProjectInfoFile data, String projectName, String pluginName, String processName)
    {
        return GetProjectDirectoryPath(data, projectName) + StandardizeName(pluginName) + "/" + StandardizeName(processName) + "/";
    }

    public static String GetProcessOutputDirectoryPath(ProjectInfoFile data, String projectName, String pluginName, String processName)
    {
        return GetProcessDirectoryPath(data, projectName, pluginName, processName) + "Output/";
    }

    public static String GetProcessWorkerTempDirectoryPath(ProjectInfoFile data, String projectName, String pluginName, String processName,
            String processWorkerName)
    {
        return GetProcessDirectoryPath(data, projectName, pluginName, processName) + StandardizeName(processWorkerName) + "_Temp/";
    }

    public static String GetDownloadDirectory(ProjectInfoFile data, String dataName)
    {
        return GetRootDirectoryPath(data) + "Downloads/" + StandardizeName(dataName) + "/";
    }
}
