// swing
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

abstract class User extends JPanel {
    String userID;
    String username;
    String password;
    String role;

    User () {
        setLayout(new BorderLayout(50, 50));
        setBorder(new EmptyBorder(25, 25, 25, 25));
    }

    void logout() {
        Seminar.cardLayout.show(Seminar.container, "login");
    }

    void applyFont(Font f, JComponent[] components) {
        for (JComponent c: components) {
            c.setFont(f);
        }
    }

    JPanel gridHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        JButton logout = new JButton("Log Out");
        logout.addActionListener(e -> logout());
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logout, BorderLayout.EAST);
        return headerPanel;
    }
}