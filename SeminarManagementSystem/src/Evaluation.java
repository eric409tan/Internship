
// database
import java.sql.Statement;
import java.sql.SQLException;

public class Evaluation {
    // ========== JESSY'S PART: EVALUATION CLASS ==========
    private int evaluatorID;
    private int submissionID;
    private int problemClarity;
    private int methodology;
    private int results;
    private int presentation;
    private String comments;

    public Evaluation(int evaluatorID, int submissionID, int problemClarity, 
                     int methodology, int results, int presentation, String comments) {
        this.evaluatorID = evaluatorID;
        this.submissionID = submissionID;
        this.problemClarity = problemClarity;
        this.methodology = methodology;
        this.results = results;
        this.presentation = presentation;
        this.comments = comments;
    }

    public int calculateTotalScore() {
        return problemClarity + methodology + results + presentation;
    }

    public boolean saveEvaluation() {
        try {
            ConnDB db = new ConnDB();
            db.connect();
            Statement stmt = db.getStatement();
            
            int totalScore = calculateTotalScore();
            
            String sql = "INSERT INTO evaluations (evaluator_id, submission_id, " +
                        "methodology_score, results_score, presentation_score, comments) VALUES (" +
                        evaluatorID + ", " + submissionID + ", " +
                        methodology + ", " + results + ", " + presentation + ", '" +
                        comments.replace("'", "''") + "')";
            
            stmt.executeUpdate(sql);
            db.disconnect();
            
            System.out.println("Evaluation saved. Total: " + totalScore);
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error saving evaluation: " + e.getMessage());
            return false;
        }
    }
}
