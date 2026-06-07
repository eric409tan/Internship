/* To compile:
// --release 8 for compatibility, remove it to use newer jdk compile
javac --release 8 Seminar.java
java -cp ".;sqlite-jdbc-3.51.1.0.jar" Seminar
*/


// swing
import javax.swing.*; 
import java.awt.*;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Main: Login screen
public class Seminar extends JFrame {
    public static CardLayout cardLayout;
    public static JPanel container;
    JPanel loginPanel;
    JTextField usernameField;
    JPasswordField passField;
    JLabel loginHint;
    
    public Seminar() {
        // main(JFrame) -> container(JPanel[cardLayout]) -> panel(JPanel) -> elements
        cardLayout = new CardLayout();

        // login panel
        container = new JPanel(cardLayout);
        loginPanel = new JPanel(new GridBagLayout());
        
        JPanel loginUI = new JPanel(new GridLayout(7, 1, 10, 10));
        JLabel title = new JLabel("Seminar Management System");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        
        usernameField = new JTextField(20);
        passField = new JPasswordField(20);
        loginHint = new JLabel(" ");
        JButton loginBtn = new JButton("Login");

        loginUI.add(title);
        loginUI.add(new JLabel("Username:"));
        loginUI.add(usernameField);
        loginUI.add(new JLabel("Password:"));
        loginUI.add(passField);
        loginUI.add(loginHint);
        loginUI.add(loginBtn);
        loginBtn.addActionListener(e -> login());

        loginPanel.add(loginUI);
        container.add(loginPanel, "login");

        add(container);
        cardLayout.show(container, "login");


        setSize(1000, 700);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


    }

    void login() {
        // validate credentials with database
        try {
            ConnDB db = new ConnDB();
            db.connect();
            Statement runQuery = db.getStatement();
            ResultSet result = runQuery.executeQuery(" select * from Users where Username = '" +usernameField.getText()+ "' and password = '" +passField.getText()+ "' ");
            if (result.next()) {
                System.out.println("Success. Log in: "+ result.getString("username")+ " as " + result.getString("role"));

                if (result.getString("role").equals("Student")) {
                    System.out.println("Starting Student Panel...");
                    container.add(new Student(result.getInt("id")), "stud");
                    cardLayout.show(container, "stud");
                
                } else if (result.getString("role").equals("Coordinator")) {
                    System.out.println("Starting Coordinator Panel...");
                    container.add(new Coordinator(), "coord");
                    cardLayout.show(container, "coord");

                } else if (result.getString("role").equals("Evaluator")) {
                    System.out.println("Starting Evaluator Panel...");
                    container.add(new Evaluator(result.getInt("id")), "eval");
                    cardLayout.show(container, "eval");

                } else {
                    System.err.println("Wrong Role.");
                }

            } else {
                System.out.println("Invalid Log in");
                loginHint.setText("Wrong username/password! Try again!");
                loginHint.setFont(new Font("SansSerif", Font.BOLD, 14));
                loginHint.setForeground(Color.RED);
            }
            
            db.disconnect();
        } catch (SQLException err) {
            System.out.println("Query error: " + err.getMessage());
        }
    
    }

    public static void main(String[] args) {
        new Seminar();
    }
}
