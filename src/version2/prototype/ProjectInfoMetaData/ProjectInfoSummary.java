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
    private ZonalSummary zonalSummary;
    private TemporalSummaryRasterFileStore fileStore;
    private String temporalSummaryCompositionStrategyClassName;

    /**
     * Create an instance of ProjectInfoSummary to contain all information from ProjectInfoFile relevant for the Summary framework.
     * @param zonalSummary
     * @param temporalStrategy
     */
    public ProjectInfoSummary(ZonalSummary zonalSummary, TemporalSummaryRasterFileStore fileStore, String temporalSummaryCompositionStrategyClassName)
    {
        this.zonalSummary = zonalSummary;
        this.fileStore = fileStore;
        this.temporalSummaryCompositionStrategyClassName = temporalSummaryCompositionStrategyClassName;
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

    @Override
    public String toString() {
        if(fileStore != null) {
            return "Zone: " + zonalSummary.GetZone() + "; Shape File Path: " + zonalSummary.GetShapeFile() + "; Field: " + zonalSummary.GetField() + "; Temporal Summary: "
                    + fileStore.compStrategy.getClass().getSimpleName();
        } else {
            return "Zone: " + zonalSummary.GetZone() + "; Shape File Path: " + zonalSummary.GetShapeFile() + "; Field: " + zonalSummary.GetField();
        }
    }
}
