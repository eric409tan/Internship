import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Report {

    public static List<String[]> getData() {
        List<String[]> data = new ArrayList<>();
        
        // data
        String sql = "SELECT " +
                     "u.username AS student_id, " + 
                     "u.name AS student_name, " +
                     "sub.title AS submission_title, " +
                     "sub.supervisor, " +
                     "u_eval.name AS evaluator_name, " +
                     "(e.methodology_score + e.results_score + e.presentation_score) AS total_marks, " +
                     "s.date AS session_date " +
                     "FROM submissions sub " +
                     "JOIN users u ON sub.student_id = u.id " +
                     "LEFT JOIN assign a ON sub.id = a.submission_id " +
                     "LEFT JOIN evaluator ev ON a.eval_id = ev.id " +
                     "LEFT JOIN users u_eval ON ev.evaluator_id = u_eval.id " +
                     "LEFT JOIN evaluations e ON sub.id = e.submission_id AND e.evaluator_id = u_eval.id " +
                     "LEFT JOIN sessions s ON sub.session_id = s.id";

        try {
            ConnDB db = new ConnDB();
            db.connect();
            ResultSet rs = db.getStatement().executeQuery(sql);

            while (rs.next()) {
                String stdId = rs.getString("student_id");
                String stdName = rs.getString("student_name");
                String title = rs.getString("submission_title");
                String supervisor = rs.getString("supervisor");
                String evaluator = rs.getString("evaluator_name");
                String marksStr = rs.getString("total_marks");
                String sessionDate = rs.getString("session_date");

                // if no supervisor then present - as null
                if (supervisor == null) { supervisor = "-"; }
                String marks = "-";
                String award = "-";

                // If an evaluator is assigned
                if (evaluator != null) {
                    // If marks exist
                    if (marksStr != null) {
                        marks = marksStr;
                        // Calculate Award (Gold/Silver/Bronze)
                        try {
                            int score = Integer.parseInt(marks);
                            if (score >= 27) award = "Gold";
                            else if (score >= 24) award = "Silver";
                            else if (score >= 20) award = "Bronze";
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                    } else {
                        // Assigned but not graded
                         marks = "Pending";
                    }
                } else {
                    evaluator = "Unassigned"; 
                }
                
                if (sessionDate == null) sessionDate = "-";

                data.add(new String[]{
                    stdId,
                    stdName,
                    title,
                    supervisor,
                    evaluator,
                    marks,
                    award,
                    sessionDate
                });
            }
            db.disconnect();
        } catch (SQLException e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    public static boolean exportCSV(String filePath) {
        List<String[]> data = getData();
        String[] headers = {"Username", "Student Name", "Submission", "Supervisor", "Evaluator Assigned", "Marks", "Award", "Session"};
        
        try (FileWriter writer = new FileWriter(filePath)) {
            // write the header of the table
            for (int i = 0; i < headers.length; i++) {
                writer.append(headers[i]);
                if (i < headers.length - 1) writer.append(",");
            }
            writer.append("\n");
            
            // write data for every row
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    String cell;
                    // if null use empty string, if not null use data
                    if (row[i] != null) {
                        cell = row[i];
                    } else {
                        cell = ""; 
                    }
                    
                    cell = cell.replace("\"", "\"\""); 
                    if (cell.contains(",") || cell.contains("\"")) {
                        cell = "\"" + cell + "\"";
                    }
                    
                    writer.append(cell);
                    if (i < row.length - 1) writer.append(",");
                }
                writer.append("\n");
            }
            return true;
        } catch (IOException e) {
            System.err.println("Export Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}