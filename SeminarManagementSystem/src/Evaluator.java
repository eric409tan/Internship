// swing
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
// image
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
// database
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Evaluator extends User {
    int evaluatorID;

    private JList<String> reviewList;
    private String imgPath;
    private int currentSubmissionID;  // Jessy added: track current submission
    private JPanel imgViewer;
    private JLabel hint;
    
    // Jessy added: form fields as instance variables
    private JTextField clarityField;
    private JTextField methodField;
    private JTextField resultField;
    private JTextField presField;
    private JTextArea comment;

    public Evaluator(int eid) {
        evaluatorID = eid;

        imgViewer = new JPanel(new BorderLayout());
        JPanel listPanel = new JPanel(new BorderLayout(0, 25));
        // list model for JList creation
        DefaultListModel<String> listModel = new DefaultListModel<>();
        // get review list from database
        ConnDB db = new ConnDB();
        db.connect();
        Statement runQuery = db.getStatement(); 
        try {
            ResultSet rs = runQuery.executeQuery("SELECT s.id, s.title, u.name FROM submissions s " +
                        "JOIN users u ON s.student_id = u.id " +
                        "JOIN evaluator ev ON s.session_id = ev.session_id " +
                        "WHERE ev.evaluator_id = " + evaluatorID);
            while(rs.next()) {
                // Jessy modified: store submission ID with title
                listModel.addElement(rs.getInt("id") + "|" + rs.getString("title") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        db.disconnect();
        // create JList
        reviewList = new JList<>(listModel);
        reviewList.setBorder(BorderFactory.createTitledBorder("Presentations to Review"));
        reviewList.addListSelectionListener(e -> reviewPresentation(e));
        hint = new JLabel("Select one to review.");

        listPanel.add(hint, BorderLayout.NORTH);
        listPanel.add(new JScrollPane(reviewList), BorderLayout.CENTER);
        
        // evaluation form
        JPanel evalForm = new JPanel(new GridLayout(8, 1, 10, 10));
        evalForm.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        
        clarityField = new JTextField();
        methodField = new JTextField();
        resultField = new JTextField();
        presField = new JTextField();
        comment = new JTextArea();
        
        evalForm.add(new JLabel("Problem Clarity (0-25)")); evalForm.add(clarityField);
        evalForm.add(new JLabel("Methodology (0-25)")); evalForm.add(methodField);
        evalForm.add(new JLabel("Results (0-25)")); evalForm.add(resultField);
        evalForm.add(new JLabel("Presentation (0-25)")); evalForm.add(presField);
        evalForm.add(new JLabel("Comment (Optional)")); evalForm.add(new JScrollPane(comment));
        comment.setSize(100, 100);
        
        
        JButton btnSave = new JButton("Submit Marks");
        btnSave.addActionListener(e -> submitEvaluation()); // Jessy added: link to submit method

        evalForm.add(btnSave);
        listPanel.add(evalForm, BorderLayout.SOUTH);
        
        add(gridHeader(), BorderLayout.NORTH); 
        add(imgViewer, BorderLayout.WEST);
        add(listPanel, BorderLayout.EAST);

    }

    void reviewPresentation(ListSelectionEvent evt) {
        
        // Jessy modified: extract submission ID from list
        String selectedValue = reviewList.getSelectedValue();
        if (selectedValue == null) return;
        
        String[] parts = selectedValue.split("\\|");
        currentSubmissionID = Integer.parseInt(parts[0]);
        String[] splitString = parts[1].split("\\ - ");
        
        // find material path from database
        imgPath = null;
        ConnDB db = new ConnDB();
        db.connect();
        Statement runQuery = db.getStatement(); 
        try {
            ResultSet rs = runQuery.executeQuery("SELECT filePath FROM submissions WHERE title = '"+ splitString[0] +"'");
            while(rs.next()) {
                imgPath = rs.getString("filePath");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        hint.setText("Current reviewing: "+ splitString[0]);
        // Jessy added: clear previous image
        imgViewer.removeAll();
        // add label to show the material
        JLabel imgLabel = new JLabel("", SwingConstants.CENTER);
        imgLabel.setSize(500, 500);
        
        if (imgPath != null && !imgPath.isEmpty()) {
            File imgFile = new File(imgPath);
            
            if (imgFile.exists()) {
                try {
                    BufferedImage img = ImageIO.read(imgFile);
                    if (img != null) {
                        Image scaledImg = img.getScaledInstance(500, 500, Image.SCALE_SMOOTH);
                        imgLabel.setIcon(new ImageIcon(scaledImg));
                    } else {
                        imgLabel.setText("Invalid Image Format");
                    }
                } catch (IOException err) {
                    imgLabel.setText("Error loading image");
                    System.err.println(err);
                }
            } else {
                imgLabel.setText("File not found: " + imgPath);
                imgLabel.setForeground(Color.RED);
            }
        } else {
            imgLabel.setText("No material uploaded");
        }


        imgViewer.add(new JScrollPane(imgLabel)); // add to viewer panel
        // refresh image viewer panel
        imgViewer.revalidate();
        imgViewer.repaint(); // Jessy added

    }
    
    // ========== JESSY'S PART: SUBMISSION METHODS ==========
    
    void submitEvaluation() {
        // Check if submission is selected
        if (currentSubmissionID == 0) {
            JOptionPane.showMessageDialog(this, "Please select a presentation to review!");
            return;
        }
        
        try {
            // Get scores from fields
            int clarity = Integer.parseInt(clarityField.getText().trim());
            int method = Integer.parseInt(methodField.getText().trim());
            int result = Integer.parseInt(resultField.getText().trim());
            int pres = Integer.parseInt(presField.getText().trim());
            
            // Validate scores (0-25)
            if (clarity < 0 || clarity > 25 || method < 0 || method > 25 || 
                result < 0 || result > 25 || pres < 0 || pres > 25) {
                JOptionPane.showMessageDialog(this, "All scores must be between 0 and 25!");
                return;
            }
            
            String comments = comment.getText().trim();
            
            // Create Evaluation object and save
            Evaluation eval = new Evaluation(evaluatorID, currentSubmissionID, 
                                            clarity, method, result, pres, comments);
            
            if (eval.saveEvaluation()) {
                int total = eval.calculateTotalScore();
                JOptionPane.showMessageDialog(this, 
                    "Evaluation submitted successfully!\n\nTotal Score: " + total + "/100");
                
                // Clear form
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save evaluation!");
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for all scores!");
        }
    }
    
    void clearForm() {
        clarityField.setText("");
        methodField.setText("");
        resultField.setText("");
        presField.setText("");
        comment.setText("");
        currentSubmissionID = 0;
        reviewList.clearSelection();
        imgViewer.removeAll();
        imgViewer.revalidate();
        imgViewer.repaint();
        hint.setText("Select one to review.");
    }
}
