package version2.prototype.util;

import java.util.ArrayList;

/**
 *
 * @author michael.devos
 *
 */
public class EASTWebResult {
    public final String indexName;
    public final int year;
    public final int day;
    public final String areaNameField;
    public final String areaCodeField;
    public final String areaName;
    public final int areaCode;
    public final String shapeFilePath;
    public final String temporalSummaryCompositionStrategyClassName;
    public final ArrayList<String> summaryNames;
    private final ArrayList<Double> summaryCalculations;
    private final String filePath;

    /**
     * Creates an immutable EASTWebResult initialized with the given values.
     *
     * @param indexNm  - name of the environmental index this result is for
     * @param year  - the earliest year this result is based in
     * @param day  - the earliest day this result is based on
     * @param areaNameField  - the name of the zone field this result is using for zonal summarization
     * @param areaName  - the name of the zone this result is summarized for
     * @param areaCodeField  - the name of the zone code field this result is using for zonal summarization
     * @param areaCode  - the zone code this result is summarized for
     * @param shapeFilePath
     * @param temporalSummaryCompositionStrategyClassName
     * @param summaryNames  - the name of the summary calculations found in the summaryCalculations list in the order they are given in it
     * @param summaryCalculations  - the summary result calculations in the order defined by summaryNames
     * @param filePath  - file path of resulting csv file
     */
    public EASTWebResult(String indexNm, int year, int day, String areaNameField, String areaName, String areaCodeField, int areaCode, String shapeFilePath,
            String temporalSummaryCompositionStrategyClassName, ArrayList<String> summaryNames, ArrayList<Double> summaryCalculations, String filePath)
    {
        indexName = indexNm;
        this.year = year;
        this.day = day;
        this.areaNameField = areaNameField;
        this.areaName = areaName;
        this.areaCodeField = areaCodeField;
        this.areaCode = areaCode;
        this.shapeFilePath = shapeFilePath;
        this.temporalSummaryCompositionStrategyClassName = temporalSummaryCompositionStrategyClassName;
        this.summaryNames = summaryNames;
        this.summaryCalculations = summaryCalculations;
        this.filePath = filePath;
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
