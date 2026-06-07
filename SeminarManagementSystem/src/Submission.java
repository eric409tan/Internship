import java.sql.SQLException;
import java.sql.Statement;

public class Submission {
    private int studentId;
    private String title;
    private String abstractText;
    private String supervisor;
    private String type;
    private String filePath;

    public Submission(int studentId, String title, String abstractText, String supervisor, String type, String filePath) {
        this.studentId = studentId;
        this.title = title;
        this.abstractText = abstractText;
        this.supervisor = supervisor;
        this.type = type;
        this.filePath = filePath;
    }

    public boolean save() {
        ConnDB db = new ConnDB();
        try {
            db.connect();
            Statement stmt = db.getStatement();

            String sql = "INSERT INTO submissions (student_id, title, abstract, supervisor, presentation_type, filePath) VALUES (" +
                         studentId + ", '" + 
                         title.replace("'", "''") + "', '" + 
                         abstractText.replace("'", "''") + "', '" + 
                         supervisor.replace("'", "''") + "', '" + 
                         type + "', '" + 
                         filePath.replace("'", "''") + "')";

            stmt.executeUpdate(sql);
            db.disconnect();
            return true;
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            return false;
        }
    }
}