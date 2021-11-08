import java.util.List;

/**
 * A representation of a document in the database.
 */
public class PostingEntry {
    private String filename;
    private String report;
    private String year;
    private String authority;
    List<Goal> goals;
    List<Task> tasks;

    /**
     * @param filename Name of this document.
     * @param report the report that authority handed to the government to rapport the results
     *               in the end of every year.
     * @param year the year goals and tasks are issued.
     * @param authority the authority that the goals and tasks are aimed for.
     * @param goals
     * @param tasks
     */
    PostingEntry (String filename, String report, String year, String authority,
                  List<Goal> goals, List<Task> tasks) {

        this.filename = filename;
        this.report = report;
        this.year = year;
        this.authority = authority;
        this.goals = goals;
        this.tasks = tasks;
    }

    PostingEntry () {
        // Do nothing
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
