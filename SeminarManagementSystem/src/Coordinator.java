// swing
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Coordinator extends User {
    int cordinatorID;
    String department;

    // create session panel
    private JSpinner year, month, day;
    private JTextField venueField;
    private JComboBox<String> typeBox;
    private DefaultTableModel sessionTable;
    private JTable table;
    private JSpinner startHour, startMin, timeSlot, totalSlots;
    private JTextField endTimeField;

    // assign session panel
    private JComboBox<String> assignSession;
    private JComboBox<String> assignStudent;
    private JComboBox<String> assignEvaluator;
    private JComboBox<String> assignTimeSlot;
    private DefaultTableModel assignTem;

    // schedule panel
    private JComboBox<String> scheduleFilterBox;
    private DefaultTableModel scheduleTem;

    // report panel
    private DefaultTableModel reportTem;
    // aqard panel
    private DefaultTableModel awardTem;

    public Coordinator() {
        add(gridHeader(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Sessions", gridSessionPanel());
        tabs.addTab("Assign", gridAssignPanel());
        tabs.addTab("Schedule", gridSchedulePanel());
        tabs.addTab("Reports", gridReportPanel());
        tabs.addTab("Awards", gridAwardPanel());

        add(tabs, BorderLayout.CENTER);
        
        // load initial data for assign tab
        refreshAssignSelection();
    }

    // private function for tabpane to grid the panel
    private JPanel gridSessionPanel() {
        JPanel sessionPanel = new JPanel(new BorderLayout(10, 10));
        sessionPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // border

        JPanel createForm = new JPanel(new GridLayout(0, 2, 10, 10));

        // Date
        year = new JSpinner(new SpinnerNumberModel(2026, 2020, 3000, 1));
        month = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        day = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));

        JPanel date = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        date.add(day);
        date.add(month);
        date.add(year);

        venueField = new JTextField();
        String[] sessionTypes = {"Oral", "Poster"};
        typeBox = new JComboBox<>(sessionTypes);

        // Start time
        startHour = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
        startMin = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5)); 
        JPanel startTime = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        startTime.add(new JLabel("H:"));
        startTime.add(startHour);
        startTime.add(new JLabel("M:"));
        startTime.add(startMin);

        timeSlot = new JSpinner(new SpinnerNumberModel(15, 5, 60, 5)); 
        totalSlots = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));    

        // end time
        endTimeField = new JTextField();
        endTimeField.setEditable(false);
        endTimeField.setText("calculated automatically");

        // add listener to make sure the end time calculate correctly
        ChangeListener calcListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                calculateEndTime();
            }
        };

        startHour.addChangeListener(calcListener);
        startMin.addChangeListener(calcListener);
        timeSlot.addChangeListener(calcListener);
        totalSlots.addChangeListener(calcListener);
        
        calculateEndTime();
        JButton createBtn = new JButton("Create Session");
        createBtn.addActionListener(e -> {
            createSession();
            refreshAssignSelection(); // Update the assign tab dropdown
        });
        JLabel hint = new JLabel("Edit the table content to update the session.");
        hint.setForeground(Color.GREEN);

        // Add to create form
        createForm.add(new JLabel("Date (Day-Month-Year):"));
        createForm.add(date);
        createForm.add(new JLabel("Venue:"));
        createForm.add(venueField);
        createForm.add(new JLabel("Session Type:"));
        createForm.add(typeBox);
        createForm.add(new JLabel("Start Time (24H):"));
        createForm.add(startTime);
        createForm.add(new JLabel("Mins per Slot:"));
        createForm.add(timeSlot);
        createForm.add(new JLabel("Total Slots:"));
        createForm.add(totalSlots);
        createForm.add(new JLabel("End Time (auto generate):"));
        createForm.add(endTimeField);
        createForm.add(hint); 
        createForm.add(createBtn);

        String[] cols = {"Session ID", "Date", "Venue", "Type", "Start Time", "Mins per Slot", "Total Slots", "End Time"};

        sessionTable = new DefaultTableModel(cols, 0) {
            // override thr function to avoid the column session id and end time editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 7;
            }
        };
        
        table = new JTable(sessionTable);
        loadSessionTable();
        sessionTable.addTableModelListener(e -> tableSessionUpdate(e));

        sessionPanel.add(createForm, BorderLayout.NORTH);
        sessionPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        return sessionPanel;
    }

    // calculate the end time
    private void calculateEndTime() {
        try {
            int h = (int)startHour.getValue();
            int m = (int)startMin.getValue();
            int t = (int)timeSlot.getValue();
            int slots = (int)totalSlots.getValue();

            int totalMins = t * slots;

            // calculate
            LocalTime startTime = LocalTime.of(h, m);
            LocalTime endTime = startTime.plusMinutes(totalMins);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm"); // pattern
            endTimeField.setText(endTime.format(formatter)); //show it as string
            
        } catch (Exception e) {
            endTimeField.setText("Time Error");
        }
    }

    private void loadSessionTable() {
        sessionTable.setRowCount(0); // Clear the table
        List<Session> sessions = Session.getSessions();
        // Add row to the table based on the List(sessions) that returned
        for (Session s : sessions) {
            sessionTable.addRow(new Object[]{
                s.getID(),
                s.getDate(),
                s.getVenue(),
                s.getSessionType(),
                s.getStartTime(),
                s.gettimeSlot(),
                s.getTotalSlots(),
                s.getEndTime()
            });
        }
    }

    private void createSession() {

        LocalDate date = LocalDate.of((int)year.getValue(), (int)month.getValue(), (int)day.getValue());
        LocalDate today = LocalDate.now();
        // If the date not after today then return
        if (!date.isAfter(today)) {
            JOptionPane.showMessageDialog(null, "Error: Cannot create session in the past!");
            return;
        }  

        String v = venueField.getText(); // venue
        String t = (String)typeBox.getSelectedItem(); // type
        String start = String.format("%02d:%02d", startHour.getValue(), startMin.getValue()); // format for 24hour
        int ts = (int)timeSlot.getValue();
        int slots = (int)totalSlots.getValue();
        calculateEndTime(); // Calculate the endtime again to avoid incorrect endtime
        String endTime = endTimeField.getText();
        // Create session object
        Session s = new Session(date.toString(), v, t, start, ts, slots, endTime);
        if (s.saveSession()) {
            JOptionPane.showMessageDialog(null, "Session Created!");
        } else {
            JOptionPane.showMessageDialog(null, "Error");
        }
        loadSessionTable(); // Refresh the table

    }

    private void tableSessionUpdate(TableModelEvent e) {
        // if not update then return
        if (e.getType() != TableModelEvent.UPDATE) { return; }
        
        int row = e.getFirstRow();
        int col = e.getColumn();
        // skip for column id, end time and invalid column
        if (col == 0 || col == 7 || col == -1) { return; }

        try {
            // get data from the row
            int id = Integer.parseInt(sessionTable.getValueAt(row, 0).toString());
            String d = sessionTable.getValueAt(row, 1).toString();
            String v = sessionTable.getValueAt(row, 2).toString();
            String t = sessionTable.getValueAt(row, 3).toString();
            if (!t.equalsIgnoreCase("Oral") && !t.equalsIgnoreCase("Poster")) {
                JOptionPane.showMessageDialog(null, "'Oral' or 'Poster' Only");
                loadSessionTable(); // refresh table
                return;
            }
            String start = sessionTable.getValueAt(row, 4).toString();
            int mins = Integer.parseInt(sessionTable.getValueAt(row, 5).toString());
            int slots = Integer.parseInt(sessionTable.getValueAt(row, 6).toString());

            // format time and calculate the new end time
            DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");

            LocalTime startTime = LocalTime.parse(start, format);
            LocalTime endTime = startTime.plusMinutes((int) mins * slots); // start min + total min
            String newEndTime = endTime.format(format);

            // update the session to database
            if (Session.updateSession(id, d, v, t, start, mins, slots, newEndTime)) {
                // Only set value if it's actually different (optimization)
                Object currentVal = sessionTable.getValueAt(row, 7);
                if (!newEndTime.equals(currentVal)) {
                    sessionTable.setValueAt(newEndTime, row, 7);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Database Error.");
                loadSessionTable(); // refresh table
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Invalid Edit!");
            loadSessionTable(); // refresh table
        }
    }

    // private function for tabpane to grid the panel
    private JPanel gridAssignPanel() {
        // Use BorderLayout to split Form (Top) and Table (Center)
        JPanel assignPanel = new JPanel(new BorderLayout(10, 10));
        assignPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // form to assign the student and evaluator
        JPanel assignForm = new JPanel(new GridLayout(5, 2, 10, 10)); // Increased rows for TimeSlot

        assignSession = new JComboBox<>();
        assignStudent = new JComboBox<>();
        assignEvaluator = new JComboBox<>();
        assignTimeSlot = new JComboBox<>();
        JButton assignBtn = new JButton("Assign");

        JLabel hint = new JLabel("Edit the table content to update the assigment.");
        hint.setForeground(Color.GREEN);
        assignSession.addActionListener(e -> updateAssignDependencies());
        assignBtn.addActionListener(e -> createAssignment());

        assignForm.add(new JLabel("Select Session:"));
        assignForm.add(assignSession);
        assignForm.add(new JLabel("Select Student:"));
        assignForm.add(assignStudent);
        assignForm.add(new JLabel("Select Evaluator:"));
        assignForm.add(assignEvaluator);
        assignForm.add(new JLabel("Select Time Slot:"));
        assignForm.add(assignTimeSlot);
        assignForm.add(hint);
        assignForm.add(assignBtn);

        String[] cols = {"ID", "Session", "Student", "Evaluator", "Time Slot"};

        assignTem = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        JTable assignTable = new JTable(assignTem);
        loadAssignTable();

        assignPanel.add(assignForm, BorderLayout.NORTH);
        assignPanel.add(new JScrollPane(assignTable), BorderLayout.CENTER);

        return assignPanel;
    }

    // refresh available sessions into the dropdown
    private void refreshAssignSelection() {
        assignSession.removeAllItems();
        assignSession.addItem("Select a Session");
        List<Session> sessions = Session.getSessions();
        for (Session s : sessions) {
            assignSession.addItem(s.getID() + " - " + s.getDate() + " (" + s.getSessionType() + ")");
        }
    }

    // fetch compatible type for students, evaluators and calc time slots
    private void updateAssignDependencies() {
        String selectedType = (String)assignSession.getSelectedItem();
        assignStudent.removeAllItems();
        assignEvaluator.removeAllItems();
        assignTimeSlot.removeAllItems();
        // if not selected the session
        if (selectedType == null || selectedType.equals("Select a Session")) { return; }

        try {
            int sessionId = Integer.parseInt(selectedType.split(" - ")[0]);

            // put the type inside the bracket
            String sessionType = selectedType.substring(selectedType.lastIndexOf("(") + 1, selectedType.lastIndexOf(")"));
            // get the unassigned students that match the session type
            List<String> students = Session.getUnassignedStudents(sessionType);
            for (String st : students) {
                assignStudent.addItem(st); 
            }
            if (students.isEmpty()) assignStudent.addItem("No students found for " + sessionType);
            // get the evaluators that match the session
            List<String> evals = Session.getEvaluators(sessionId);
            for (String ev : evals) {
                assignEvaluator.addItem(ev);
            }
            // calc the time slot
            List<String> slots = Session.calcTimeSlots(sessionId);
            for (String slot : slots) {
                assignTimeSlot.addItem(slot);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createAssignment() {
        // make sure all the field is selected
        if (assignSession.getSelectedIndex() <= 0 || assignStudent.getSelectedItem() == null || 
            assignStudent.getSelectedItem().toString().startsWith("No students") ||
            assignEvaluator.getSelectedItem() == null || assignTimeSlot.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Please select the fields completely.");
            return; // exit
        }

        try {
            // get the id from the selected using regex
            int sessionID = Integer.parseInt(assignSession.getSelectedItem().toString().split(" - ")[0]);
            int subID = Integer.parseInt(assignStudent.getSelectedItem().toString().split(" - ")[0]);
            int evalID = Integer.parseInt(assignEvaluator.getSelectedItem().toString().split(" - ")[0]);
            String timeSlot = assignTimeSlot.getSelectedItem().toString();

            if (Session.saveAssignment(sessionID, subID, evalID, timeSlot)) {
                JOptionPane.showMessageDialog(null, "Assigned Successfully!");
                loadAssignTable();
                updateAssignDependencies(); // refresh
            } else {
                JOptionPane.showMessageDialog(null, "Assignment Failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error");
        }
    }

    private void loadAssignTable() {
        assignTem.setRowCount(0);
        List<String[]> rows = Session.getAssignments();
        for (String[] row : rows) {
            assignTem.addRow(row);
        }
    }

    private JPanel gridSchedulePanel() {
        JPanel schedulePanel = new JPanel(new BorderLayout(10, 10));
        schedulePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scheduleFilterBox = new JComboBox<>();
        scheduleFilterBox.addActionListener(e -> loadScheduleTable());

        filterPanel.add(new JLabel("Select Session:"));
        filterPanel.add(scheduleFilterBox);

        // Schedule Table
        String[] cols = {"Date", "Time Slot", "Venue", "Student", "Submission Title", "Evaluator"};
        scheduleTem = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable scheduleTable = new JTable(scheduleTem);

        schedulePanel.add(filterPanel, BorderLayout.NORTH);
        schedulePanel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);
        
        refreshScheduleSelection();

        return schedulePanel;
    }

    // session selection
    private void refreshScheduleSelection() {
        scheduleFilterBox.removeAllItems();
        scheduleFilterBox.addItem("All");
        List<Session> sessions = Session.getSessions();
        for (Session s : sessions) {
            scheduleFilterBox.addItem(s.getID() + " - " + s.getDate() + " (" + s.getSessionType() + ")");
        }
        loadScheduleTable();
    }

    private void loadScheduleTable() {
        if (scheduleTem == null) return;
        scheduleTem.setRowCount(0);
        
        int selectedSessionId = 0;
        String selected = (String) scheduleFilterBox.getSelectedItem();
        if (selected != null && !selected.equals("All")) {
            selectedSessionId = Integer.parseInt(selected.split(" - ")[0]);
        }

        List<String[]> rows = Session.getSchedule(selectedSessionId);
        for (String[] row : rows) {
            scheduleTem.addRow(row);
        }
    }

    private JPanel gridReportPanel() {
        JPanel reportPanel = new JPanel(new BorderLayout(10, 10));
        reportPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JButton exportBtn = new JButton("Export to CSV");
        exportBtn.addActionListener(e -> startExport());

        String[] cols = {"Id", "Student Name", "Submission", "Supervisor", "Evaluator Assigned", "Marks", "Award", "Session"};
        reportTem = new DefaultTableModel(cols, 0);
        JTable reportTable = new JTable(reportTem);

        reportPanel.add(exportBtn, BorderLayout.NORTH);
        reportPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        loadReportTable(); // refresh/load

        return reportPanel;
    }

    private void loadReportTable() {
        if (reportTem == null) { return; }
        reportTem.setRowCount(0);
        // get the data
        List<String[]> rows = Report.getData();
        for (String[] row : rows) {
            reportTem.addRow(row);
        }
    }

    private void startExport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
        int fileSelection = fileChooser.showSaveDialog(null);
        
        if (fileSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".csv")) {
                path += ".csv";
            }
            
            if (Report.exportCSV(path)) {
                JOptionPane.showMessageDialog(null, "Report exported successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Error exporting report.");
            }
        }
    }

    private JPanel gridAwardPanel() {
        JPanel awardPanel = new JPanel(new BorderLayout(10, 10));
        awardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] cols = {"Award", "Student Name", "Submission Title", "Total marks"};
        awardTem = new DefaultTableModel(cols, 0);
        JTable awardTable = new JTable(awardTem);
        awardPanel.add(new JScrollPane(awardTable), BorderLayout.CENTER);
        
        loadAwardTable();
        return awardPanel;
    }

    private void loadAwardTable() {
        if (awardTem == null) return;
        awardTem.setRowCount(0);
        List<String[]> rows = Award.getNominations();
        for (String[] row : rows) {
            awardTem.addRow(row);
        }
    }

}