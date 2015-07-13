package version2.prototype.util;

import java.util.ArrayList;

/**
 *
 * @author michael.devos
 *
 */
public class EASTWebResult {
    public final String indexNm;
    public final int year;
    public final int day;
    public final String field;
    public final String zone;
    public final String shapeFilePath;
    public final int expectedTotalResults;
    public final String temporalSummaryCompositionStrategyClassName;
    public final ArrayList<String> summaryNames;
    private final ArrayList<Double> summaryCalculations;

    /**
     * Creates an immutable EASTWebResult initialized with the given values.
     *
     * @param indexNm  - name of the environmental index this result is for
     * @param year  - the earliest year this result is based in
     * @param day  - the earliest day this result is based on
     * @param zoneFieldNm  - the name of the zone field this result is summarized for
     * @param summaryNames  - the name of the summary calculations found in the summaryCalculations list in the order they are given in it
     * @param summaryCalculations  - the summary result calculations in the order defined by summaryNames
     */
    public EASTWebResult(String indexNm, int year, int day, String field, String zone, String shapeFilePath, int expectedTotalResults, String temporalSummaryCompositionStrategyClassName,
            ArrayList<String> summaryNames, ArrayList<Double> summaryCalculations)
    {
        this.indexNm = indexNm;
        this.year = year;
        this.day = day;
        this.field = field;
        this.zone = zone;
        this.shapeFilePath = shapeFilePath;
        this.expectedTotalResults = expectedTotalResults;
        this.temporalSummaryCompositionStrategyClassName = temporalSummaryCompositionStrategyClassName;
        this.summaryNames = summaryNames;
        this.summaryCalculations = summaryCalculations;
    }

    /**
     * Gets the list of summary calculation results. The order and name of the summary calculations contained can be found in the summaryNames property of this class.
     *
     * @return the resulting summary calculations
     */
    public ArrayList<Double> GetSummaryCalculations()
    {
        return summaryCalculations;
    }
}
