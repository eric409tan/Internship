
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Session {
    private int sessionID;
    private String date;
    private String venue;
    private String sessionType;
    private String startTime;
    private int timeSlot;
    private int totalSlots;
    private String endTime;

    public Session(String date, String venue, String sessionType, String startTime, int timeSlot, int totalSlots, String endTime) {
        this.date = date;
        this.venue = venue;
        this.sessionType = sessionType;
        this.startTime = startTime;
        this.timeSlot = timeSlot;
        this.totalSlots = totalSlots;
        this.endTime = endTime;
    }

    // getters for the table
    public int getID() { return sessionID; }
    public void setID(int id) { this.sessionID = id; }
    public String getDate() { return date; }
    public String getVenue() { return venue; }
    public String getSessionType() { return sessionType; }
    public String getStartTime() { return startTime; }
    public int gettimeSlot() { return timeSlot; }
    public int getTotalSlots() { return totalSlots; }
    public String getEndTime() { return endTime; }

    public boolean saveSession() {
        try {
            ConnDB db = new ConnDB();
            db.connect();
            Statement stmt = db.getStatement();
            
            String sql = "INSERT INTO sessions (date, venue, type, " +
                        "start_time, end_time, time_slot, slots) VALUES ('" +date+ "', '" +venue.replace("'", "''")+ "', '" + 
                        sessionType + "', '" +startTime+ "', '" +endTime+ "', " +timeSlot+ ", " +totalSlots+ ")";
            
            stmt.executeUpdate(sql);
            db.disconnect();
            
            System.out.println("Session saved.");
            return true;
            // 3 * 15
            
        } catch (SQLException e) {
            System.err.println("Error saving session: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateSession(int id, String date, String venue, String type, String start, int slot, int total, String end) {
        try {
            ConnDB db = new ConnDB();
            db.connect();
            Statement stmt = db.getStatement();
            
            String sql = "UPDATE sessions SET " + "date = '" + date + "', " +
                         "venue = '" + venue.replace("'", "''") + "', " +
                         "type = '" + type + "', " +"start_time = '" + start + "', " +
                         "end_time = '" + end + "', " + "time_slot = " + slot + ", " +
                         "slots = " + total + " WHERE id = " + id;

            stmt.executeUpdate(sql);
            db.disconnect();
            return true;
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            return false;
        }
    }

    // static function without create object
    public static List<Session> getSessions() {
        List<Session> list = new ArrayList<>();
        try {
            ConnDB db = new ConnDB();
            db.connect();
            String sql = "SELECT * FROM sessions";
            ResultSet rs = db.getStatement().executeQuery(sql);

            // every row create a session object
            while (rs.next()) {
                Session s = new Session(rs.getString("date"), rs.getString("venue"),
                    rs.getString("type"), rs.getString("start_time"),
                    rs.getInt("time_slot"), rs.getInt("slots"), rs.getString("end_time"));
                s.setID(rs.getInt("id"));
                list.add(s); // add session object to list [S1, S2, S3]
            }
            db.disconnect();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    // get unassigned students list
    public static List<String> getUnassignedStudents(String type) {
        List<String> list = new ArrayList<>();
        try {
            ConnDB db = new ConnDB();
            db.connect();
            // Get submissions linked to this session
            String sql = "SELECT s.id, s.title, u.name FROM submissions s " +
                         "JOIN users u ON s.student_id = u.id " +
                         "WHERE s.session_id IS NULL " + 
                         "AND s.presentation_type = '" + type + "'";
            
            ResultSet rs = db.getStatement().executeQuery(sql);
            while (rs.next()) {
                // Format: "ID - Title (Student Name)"
                list.add(rs.getInt("id") + " - " + rs.getString("title") + " (" + rs.getString("name") + ")");
            }
            db.disconnect();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // get evaluators
    public static List<String> getEvaluators(int sessionId) {
        List<String> list = new ArrayList<>();
        try {
            ConnDB db = new ConnDB();
            db.connect();
            // Note: We need the ID from the 'evaluator' table (the link table), not just the user ID.
            String sql = "SELECT e.id, u.name FROM evaluator e " +
                         "JOIN users u ON e.evaluator_id = u.id " +
                         "WHERE e.session_id = " + sessionId;
            ResultSet rs = db.getStatement().executeQuery(sql);
            while (rs.next()) {
                list.add(rs.getInt("id") + " - " + rs.getString("name"));
            }
            db.disconnect();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // calc time slot
    public static List<String> calcTimeSlots(int sessionId) {
        List<String> slots = new ArrayList<>();
        try {
            ConnDB db = new ConnDB();
            db.connect();
            String sql = "SELECT start_time, time_slot, slots FROM sessions WHERE id = " + sessionId;
            ResultSet rs = db.getStatement().executeQuery(sql);

            if (rs.next()) {
                String start = rs.getString("start_time");
                int minsPerSlot = rs.getInt("time_slot");
                int totalSlots = rs.getInt("slots");

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime time = LocalTime.parse(start, fmt);

                for (int i = 0; i < totalSlots; i++) {
                    String slotStr = time.format(fmt) + "-" + time.plusMinutes(minsPerSlot).format(fmt);
                    slots.add(slotStr);
                    time = time.plusMinutes(minsPerSlot);
                }
            }
            db.disconnect();
        } catch (Exception e) { e.printStackTrace(); }
        return slots;
    }

    // save the assignment to database
    public static boolean saveAssignment(int sessionId, int submissionId, int evalId, String timeSlot) {
        ConnDB db = new ConnDB();
        try {
            db.connect();
            db.getStatement().execute("BEGIN TRANSACTION;"); // Start transaction for safety

            // update submissions
            String updateSql = "UPDATE submissions SET session_id = " + sessionId + 
                               " WHERE id = " + submissionId;
            db.getStatement().executeUpdate(updateSql);

            // insert assign table
            String insertSql = "INSERT INTO assign (session_id, submission_id, eval_id, time_slot) VALUES (" +
                         sessionId + ", " + submissionId + ", " + evalId + ", '" + timeSlot + "')";
            db.getStatement().executeUpdate(insertSql);
            
            db.getStatement().execute("COMMIT;");
            db.disconnect();
            return true;
        } catch (SQLException e) {
            try { db.getStatement().execute("ROLLBACK;"); } catch(Exception ex){} // Rollback on error
            System.err.println("Assignment Error: " + e.getMessage());
            return false;
        }
    }

    // load all assignments for the table
    public static List<String[]> getAssignments() {
        List<String[]> list = new ArrayList<>();
        try {
            ConnDB db = new ConnDB();
            db.connect();
            String sql = "SELECT a.assign_id, s.date, sub.title, u.name, a.time_slot " +
                         "FROM assign a " +
                         "JOIN sessions s ON a.session_id = s.id " +
                         "JOIN submissions sub ON a.submission_id = sub.id " +
                         "JOIN evaluator e ON a.eval_id = e.id " +
                         "JOIN users u ON e.evaluator_id = u.id";
            
            ResultSet rs = db.getStatement().executeQuery(sql);
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("assign_id")),
                    rs.getString("date"),
                    rs.getString("title"),
                    rs.getString("name"),
                    rs.getString("time_slot")
                });
            }
            db.disconnect();
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // get the schedule from session id
    public static List<String[]> getSchedule(int SessionId) {
        List<String[]> list = new ArrayList<>();
        try {
            ConnDB db = new ConnDB();
            db.connect();
            String sql = "SELECT s.date, a.time_slot, s.venue, u_stud.name AS student_name, sub.title, u_eval.name AS eval_name " +
                         "FROM assign a " +
                         "JOIN sessions s ON a.session_id = s.id " +
                         "JOIN submissions sub ON a.submission_id = sub.id " +
                         "JOIN users u_stud ON sub.student_id = u_stud.id " +
                         "JOIN evaluator e ON a.eval_id = e.id " +
                         "JOIN users u_eval ON e.evaluator_id = u_eval.id";
            
            // if not show all sessions
            if (SessionId > 0) {
                sql += " WHERE s.id = " + SessionId;
            }
            
            sql += " ORDER BY s.date, a.time_slot";
            
            ResultSet rs = db.getStatement().executeQuery(sql);
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("date"),
                    rs.getString("time_slot"),
                    rs.getString("venue"),
                    rs.getString("student_name"),
                    rs.getString("title"),
                    rs.getString("eval_name")
                });
            }
            db.disconnect();
        } catch (Exception e) { 
            e.printStackTrace();
        }
        return list;
    }
}