package version2.prototype.util;

/**
 *
 * @author michael.devos
 *
 */
public final class EASTWebQuery {
    public final String schemaName;
    private final String sql;

    /**
     * Creates a custom query holding object. Not allowed to be publicly created to discourage creating custom queries for the database anywhere throughout EASTWeb. Querying the database should be handled
     * by only select classes that act as proxies to the database (e.g. {@link version2#EASTWebResults EASTWebResults}).
     *
     * @param query  - an sql command
     */
    protected EASTWebQuery(String schemaName, String sql)
    {
        this.schemaName = schemaName;
        this.sql = sql;
    }

    /**
     * Gets the stored sql command string.
     *
     * @return sql command string
     */
    public String GetSQL()
    {
        return sql;
    }
}