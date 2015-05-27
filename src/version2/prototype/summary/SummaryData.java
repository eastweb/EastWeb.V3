package version2.prototype.summary;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;

import version2.prototype.DataDate;
import version2.prototype.summary.summaries.SummariesCollection;
import version2.prototype.summary.temporal.InterpolateStrategy;
import version2.prototype.summary.temporal.MergeStrategy;
import version2.prototype.summary.temporal.TemporalSummaryComposition;
import version2.prototype.summary.temporal.TemporalSummaryCompositionStrategy;
import version2.prototype.util.GeneralListener;
import version2.prototype.util.GeneralUIEvent;


public class SummaryData {
    public String projectName;

    // ZonalSummaryCalculator variables
    public GeneralUIEvent generalUIEvent;
    public File inRaster;
    public File inShape;
    public File outTableFile;
    public String zoneField;
    public SummariesCollection summaries;
    public static ArrayList<TemporalSummaryComposition> compositions = new ArrayList<TemporalSummaryComposition>(0); // Needed

    // Remaining TemporalSummaryCalculator variables
    public DataDate inDate;
    public int daysPerInputData;
    public int daysPerOutputData;
    public InterpolateStrategy intStrategy;
    public MergeStrategy mergeStrategy;
    public DataDate projectSDate;
    public TemporalSummaryCompositionStrategy compStrategy;

    /**
     * <p>Accepts values for all inputs.</p>
     *
     * @param inRaster - Type: File[] - A File object for each DataDate.<br/>
     * Example:  <code>{@link #version2.prototype.DirectoryLayout.getIndexMetadata(ProjectInfo, String, DataDate, String)}<br/>
     * getIndexMetadata(mProject, mIndex, sDate, zone.getShapeFile())</code>
     * @param inShape - Type: File - The layer/shape file.<br/>
     * Example:  <code>File({@link #version2.prototype.DirectoryLayout.getSettingsDirectory(ProjectInfo)},
     * {@link #version2.prototype.ZonalSummary.getShapeFile()})<br/>
     * File(DirectoryLayout.getSettingsDirectory(mProject), zone.getShapeFile())</code>
     * @param outTable - Type: File - File object pointing to output location for zonal summary
     * @param zone - Type: String - The zone name.<br/>
     * Example:  <code>for ({@link version2.prototype.ZonalSummary ZonalSummary} zone : mProject.{@link #version2.prototype.ProjectInfo.getSummaries()}) { zone.{@link #version2.prototype.ZonalSummary.getField()}; }<br/>
     * for (ZonalSummary zone : mProject.getSummaries())<br/>  { zone.getField(); }</code>
     * @param summarySingletonNames - Type: ArrayList<\String> - A list of the class names of the summaries to use in zonal summary.<br/>
     * @param inDate - Type: DataDate[] - An array of the dates of the downloaded data to be used in finding the data in the file system and in processing temporal summaries.<br/>
     * @param daysPerInputData - Type: int - The number of hours each piece of downloaded data represents.
     * @param daysPerOutputData - Type: int - The number of hours each piece of summary/output data will represent.
     * @param projectSDate - Type: Calendar - The projects start date.
     * @param calStrategy - Type: CalendarStrategy - The strategy to use when getting the starting date of the week.
     * @param merStrategy - Type: MergeStrategy - The strategy to use when merging downloaded data.
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public SummaryData(String projectName, File inRaster, File inShape, File outTable, String zone, ArrayList<String> summarySingletonNames,
            DataDate inDate, int daysPerInputData, int daysPerOutputData, DataDate projectSDate,
            TemporalSummaryCompositionStrategy compStrategy, InterpolateStrategy intStrategy, MergeStrategy mergeStrategy, GeneralListener l)
                    throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
        this.projectName = projectName;
        this.inRaster = inRaster;
        this.inShape = inShape;
        outTableFile = outTable;
        zoneField = zone;
        summaries = new SummariesCollection(summarySingletonNames);
        this.inDate = inDate;
        this.daysPerInputData = daysPerInputData;
        this.daysPerOutputData = daysPerOutputData;
        this.intStrategy = intStrategy;
        this.mergeStrategy = mergeStrategy;
        this.projectSDate = projectSDate;
        this.compStrategy = compStrategy;
        generalUIEvent = new GeneralUIEvent();
        generalUIEvent.addListener(l);
    }
}
