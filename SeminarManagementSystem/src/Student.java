// swing
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Student extends User {
    int studentID;

    // UI Components
    private JTextField titleField;
    private JTextArea abstractTxt;
    private JTextField supervisorField;
    private JComboBox<String> typeBox;
    private JLabel fileLabel;
    private String selectedFilePath = "";

    public Student(int id) {
        this.studentID = id;

        // Student Form Panel
        JPanel studentPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        studentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // student panel components
        titleField = new JTextField();
        abstractTxt = new JTextArea(3, 20);
        supervisorField = new JTextField();
        String[] types = {"Oral", "Poster"};
        typeBox = new JComboBox<>(types);
        // add components
        studentPanel.add(new JLabel("Research Title:"));
        studentPanel.add(titleField);
        studentPanel.add(new JLabel("Abstract:"));
        studentPanel.add(new JScrollPane(abstractTxt));
        studentPanel.add(new JLabel("Supervisor Name:"));
        studentPanel.add(supervisorField);
        studentPanel.add(new JLabel("Preferred Type:"));
        studentPanel.add(typeBox);
        studentPanel.add(new JLabel("Presentation Material:"));
        
        // upload panel
        JPanel uploadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton uploadBtn = new JButton("Choose File...");
        fileLabel = new JLabel(" No file selected");

        uploadBtn.addActionListener(e -> uploadFile());
        uploadPanel.add(uploadBtn);
        uploadPanel.add(fileLabel);
        studentPanel.add(uploadPanel);

        studentPanel.add(new JLabel(""));
        JButton submitBtn = new JButton("Submit Registration");
        submitBtn.addActionListener(e -> submit());
        studentPanel.add(submitBtn);

        add(gridHeader(), BorderLayout.NORTH); 
        add(studentPanel, BorderLayout.CENTER);     
    }

    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        // only aceept jpg, jpeg, png, pdf format
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG, JPEG, PNG and PDF", "jpg", "jpeg", "png", "pdf");
        fileChooser.setFileFilter(filter);
        
        int response = fileChooser.showOpenDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            File sourceFile = fileChooser.getSelectedFile();
            
            try {
                File uploadDir = new File("uploads");
                // create uploads directory if not exist
                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                }

                // set the file name to studentID_filename
                String fileName = studentID + "_" + sourceFile.getName();
                Path targetPath = Paths.get("uploads", fileName);
                // copy file to uploads
                Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                

                selectedFilePath = targetPath.toString();
                fileLabel.setText(" " + sourceFile.getName());
                
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error uploading file!");
                fileLabel.setText("Upload Failed");
                fileLabel.setForeground(Color.RED);
                selectedFilePath = "";
            }
        }
    }

    private void submit() {
        // trim remove any spaces
        String title = titleField.getText().trim();
        String abs = abstractTxt.getText().trim();
        String sup = supervisorField.getText().trim();
        String type = (String) typeBox.getSelectedItem();

        // check if the field is entered
        if (title.isEmpty() || sup.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter title and supervisor.");
            return;
        }
        if (selectedFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please upload your materials.");
            return;
        }

        // save to database
        Submission s = new Submission(studentID, title, abs, sup, type, selectedFilePath); 
        if (s.save()) {
            JOptionPane.showMessageDialog(null, "Registration successfully.");
        } else {
            JOptionPane.showMessageDialog(null, "Database Error: Submission Failed.");
        }
    }

}