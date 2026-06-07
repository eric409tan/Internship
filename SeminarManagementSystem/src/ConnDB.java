/* Connect database and run sql query example:
ConnDB db = new ConnDB(); // create new object
db.connect(); // connect db
Statement runQuery = db.getStatement(); 
try {
    ResultSet rs = runQuery.executeQuery("SELECT * FROM Users");
    
    while (rs.next()) {
        String u = rs.getString("Username"); // column
        String p = rs.getString("Password");
        String r = rs.getString("Role");
        // print in row
        System.out.println("User: " + u + "/" + p + "/" + r); 
    }
        
} catch (SQLException e) {
    System.out.println(e.getMessage());
}
*/

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnDB {

    // initialize Connection
    private Connection conn = null;

    // connect function to connect database
    public void connect() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + "\\seminar.db");
            System.out.println("Database Connected.");
        } catch (SQLException e) {
            System.out.println("Connection Failed: " + e.getMessage());
        }
    }

    // get statement 
    public Statement getStatement() {
        Statement statement = null; // initialize
        try {
            if (conn != null && !conn.isClosed()) { // if connection is connected
                statement = conn.createStatement();
            } else {
                System.out.println("Error: No database connected.");
            }
        } catch (SQLException e) {
            System.out.println("getStatement() error: " + e.getMessage());
        }

        return statement; // return to execute sql query
    }

    // disconnect database
    public void disconnect() {
        try {
            if (conn != null && !conn.isClosed()) { // is db connected
                conn.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("disconnect() error: " + e.getMessage());
        }
    }
}