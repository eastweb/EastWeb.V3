package version2.prototype.ProjectInfoMetaData;

import version2.prototype.ZonalSummary;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;

/**
 * Represents a summary element from the project meta data.
 *
 * @author michael.devos
 *
 */
public class ProjectInfoSummary {
    public static final String AREA_NAME_FIELD_TAG = "AreaNameField";
    public static final String SHAPE_FILE_TAG = "Shape File Path";
    public static final String AREA_VALUE_FIELD_TAG = "AreaValueField";
    public static final String TEMPORAL_SUMMARY_TAG = "Temporal Summary";
    private final ZonalSummary zonalSummary;
    private final TemporalSummaryRasterFileStore fileStore;
    private final String temporalSummaryCompositionStrategyClassName;
    private final int ID;

    /**
     * Create an instance of ProjectInfoSummary to contain all information from ProjectInfoFile relevant for the Summary framework.
     *
     * @param zonalSummary  - ZonalSummary object representing zonal data from a project metadata xml
     * @param fileStore  - TemporalSummaryRasterFileStore object representing temporal data gotten from a project metadata xml
     * @param temporalSummaryCompositionStrategyClassName  - class name of the used TemporalSummaryCompositionStrategy in making the filestore parameter
     */
    public ProjectInfoSummary(ZonalSummary zonalSummary, TemporalSummaryRasterFileStore fileStore, String temporalSummaryCompositionStrategyClassName, int ID)
    {
        this.zonalSummary = zonalSummary;
        this.fileStore = fileStore;
        this.temporalSummaryCompositionStrategyClassName = temporalSummaryCompositionStrategyClassName;
        this.ID = ID;
    }

    /**
     *  Gets the ZonalSummary object from the instance.
     *
     * @return ZonalSummary instance
     */
    public ZonalSummary GetZonalSummary() { return zonalSummary; }

    /**
     * Gets the TemporalSummaryRasterFileStore object from this instance.
     *
     * @return TemporalSummaryRasterFileStore instance
     */
    public TemporalSummaryRasterFileStore GetTemporalFileStore() { return fileStore; }

    /**
     * Gets the TemporalSummaryCompositionStrategy class name used to create the TemporalSummaryRasterFileStore object that's returned from GetTemporalFileStore().
     *
     * @return the class name of the TemporalSummaryCompositionStrategy gotten from the project meta data for this summary element
     */
    public String GetTemporalSummaryCompositionStrategyClassName() { return temporalSummaryCompositionStrategyClassName; }

    /**
     * Gets the ID attribute value assigned to this Summary element in the project metadata xml.
     *
     * @return int ID attributed
     */
    public int GetID() { return ID; }

    @Override
    public String toString() {
        if(fileStore != null) {
            return AREA_NAME_FIELD_TAG + ": " + zonalSummary.GetAreaNameField() + "; " + SHAPE_FILE_TAG + ": " + zonalSummary.GetShapeFile() + "; " + AREA_VALUE_FIELD_TAG + ": " +
                    zonalSummary.GetAreaValueField() + "; " + TEMPORAL_SUMMARY_TAG + ": " + fileStore.compStrategy.getClass().getSimpleName();
        } else {
            return AREA_NAME_FIELD_TAG + ": " + zonalSummary.GetAreaNameField() + "; " + SHAPE_FILE_TAG + ": " + zonalSummary.GetShapeFile() + "; " + AREA_VALUE_FIELD_TAG + ": " +
                    zonalSummary.GetAreaValueField();
        }
    }
}
