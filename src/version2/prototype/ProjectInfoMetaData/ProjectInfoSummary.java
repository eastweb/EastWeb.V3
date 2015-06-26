package version2.prototype.ProjectInfoMetaData;

import version2.prototype.ZonalSummary;
import version2.prototype.summary.temporal.TemporalSummaryRasterFileStore;

public class ProjectInfoSummary {
    private ZonalSummary zonalSummary;
    private TemporalSummaryRasterFileStore fileStore;

    /**
     * Create an instance of ProjectInfoSummary to contain all information from ProjectInfoFile relevant for the Summary framework.
     * @param zonalSummary
     * @param temporalStrategy
     */
    public ProjectInfoSummary(ZonalSummary zonalSummary, TemporalSummaryRasterFileStore fileStore)
    {
        this.zonalSummary = zonalSummary;
        this.fileStore = fileStore;
    }

    /**
     *  Gets the ZonalSummary object from the instance.
     *
     * @return ZonalSummary instance
     */
    public ZonalSummary GetZonalSummary() { return zonalSummary; }

    /**
     * Gets the TemporalSummaryCompositionStrategy object from this instance.
     *
     * @return TemporalSummaryCompositionStrategy instance
     */
    public TemporalSummaryRasterFileStore GetTemporalFileStore() { return fileStore; }
}
