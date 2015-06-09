package version2.prototype.util;

import java.util.regex.Pattern;

import version2.prototype.Config;
import version2.prototype.ProjectInfoMetaData.ProjectInfoFile;
import version2.prototype.ConfigReadException;

/**
 * Handles getting and creating all directories used by the program, creating files, and deleting files.
 * Names are also used in a SQL compliant database. So, all names are standardized to be valid identifiers for it.
 * @author michael.devos
 *
 */

public class FileSystem {
    private FileSystem() {}

    public static String GetRootDirectoryPath() throws ConfigReadException
    {
        String path = Config.getInstance().getRootDirectory();
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

    public static String GetProjectDirectoryPath(String projectName) throws ConfigReadException
    {
        return GetRootDirectoryPath() + "Projects/" + StandardizeName(projectName) + "/";
    }

    public static String GetProcessDirectoryPath(String projectName, String pluginName, String processName) throws ConfigReadException
    {
        return GetProjectDirectoryPath(projectName) + StandardizeName(pluginName) + "/" + StandardizeName(processName) + "/";
    }

    public static String GetProcessOutputDirectoryPath(String projectName, String pluginName, String processName) throws ConfigReadException
    {
        return GetProcessDirectoryPath(projectName, pluginName, processName) + "Output/";
    }

    public static String GetProcessWorkerTempDirectoryPath(String projectName, String pluginName, String processName, String processWorkerName)
            throws ConfigReadException
    {
        return GetProcessDirectoryPath(projectName, pluginName, processName) + StandardizeName(processWorkerName) + "_Temp/";
    }

    public static String GetDownloadDirectory(String dataName) throws ConfigReadException
    {
        return GetRootDirectoryPath() + "Downloads/" + StandardizeName(dataName) + "/";
    }
}
