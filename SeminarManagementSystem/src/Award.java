import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Award {

    public static List<String[]> getNominations() {
        List<String[]> list = new ArrayList<>();
        ConnDB db = new ConnDB();
        
        try {
            db.connect();

            // best oral
            String sql1 = "SELECT u.name, s.title, (e.methodology_score + e.results_score + e.presentation_score) as score " +
                             "FROM submissions s " +
                             "JOIN evaluations e ON s.id = e.submission_id " +
                             "JOIN users u ON s.student_id = u.id " +
                             "WHERE s.presentation_type = 'Oral' " +
                             "ORDER BY score DESC LIMIT 1";
            appendList(db, sql1, "Best Oral", list);

            // best poster
            String sql2 = "SELECT u.name, s.title, (e.methodology_score + e.results_score + e.presentation_score) as score " +
                               "FROM submissions s " +
                               "JOIN evaluations e ON s.id = e.submission_id " +
                               "JOIN users u ON s.student_id = u.id " +
                               "WHERE s.presentation_type = 'Poster' " +
                               "ORDER BY score DESC LIMIT 1";
            appendList(db, sql2, "Best Poster", list);

            // People's Choice
            String sql3 = "SELECT u.name, s.title, e.presentation_score as score " +
                               "FROM submissions s " +
                               "JOIN evaluations e ON s.id = e.submission_id " +
                               "JOIN users u ON s.student_id = u.id " +
                               "ORDER BY score DESC LIMIT 1";
            appendList(db, sql3, "People's Choice", list);

            db.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // append the result to the List<String []>: List([], [])
    private static void appendList(ConnDB db, String sql, String awardName, List<String[]> list) {
        try {
            ResultSet rs = db.getStatement().executeQuery(sql);
            if (rs.next()) {
                list.add(new String[]{
                    awardName,
                    rs.getString("name"),
                    rs.getString("title"),
                    rs.getString("score")
                });
            } else {
                list.add(new String[]{awardName, "No Nominations", "-", "-"});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}