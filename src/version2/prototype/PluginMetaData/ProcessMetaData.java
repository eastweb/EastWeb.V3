/**
 *
 */
package version2.prototype.PluginMetaData;

import java.util.ArrayList;

/**
 * @author michael.devos
 *
 */
public abstract class ProcessMetaData {
    public final String Title;
    public final ArrayList<String> QualityControlMetaData;
    public final Integer DaysPerInputData;
    public final Integer Resolution;
    public final ArrayList<String> ExtraDownloadFiles;

    protected ProcessMetaData(String Title, ArrayList<String> QualityControlMetaData, Integer DaysPerInputData, Integer Resolution, ArrayList<String> ExtraDownloadFiles)
    {
        this.Title = Title;
        this.QualityControlMetaData = QualityControlMetaData;
        this.DaysPerInputData = DaysPerInputData;
        this.Resolution = Resolution;
        this.ExtraDownloadFiles = ExtraDownloadFiles;
    }
}
