import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class RegistrationFrame extends JFrame {
    // --- MODERN APP COLOR PALETTE (Dark Theme) ---
    private final Color PRIMARY_BLUE = new Color(99, 102, 241); // Indigo
    private final Color TEXT_PRIMARY = Color.WHITE; // White
    private final Color TEXT_SECONDARY = new Color(180, 180, 200); // Light Gray
    private final Color BG_COLOR = new Color(20, 20, 35); // Dark BG
    private final Color INPUT_BORDER = new Color(70, 70, 110); // Dark Border

    // Components
    private JTextField usernameField, fullNameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> userTypeCombo;
    private JButton registerButton, cancelButton, uploadDocButton;
    private JCheckBox showPasswordCheck;
    private JLabel verificationFileLabel;
    private File selectedVerificationFile;

    public RegistrationFrame() {
        setTitle("Create Account");
        setSize(450, 820); // Slightly taller, plus scrolling safety
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);

        initComponents();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 40, 8, 40); // Generous side padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // --- 1. Header Section ---
        JPanel headerPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        headerPanel.setBackground(BG_COLOR);

        JLabel iconLabel = new JLabel("🎓", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JLabel titleLabel = new JLabel("Get Started", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel =
            new JLabel("Create your account to connect.", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setForeground(TEXT_SECONDARY);

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);

        gbc.gridy = 0;
        gbc.insets = new Insets(30, 40, 20, 40); // Extra top padding
        formPanel.add(headerPanel, gbc);

        // --- 2. Input Fields (Material Style) ---
        gbc.insets = new Insets(5, 40, 15, 40); // Spacing between fields

        gbc.gridy++;
        formPanel.add(createLabel("FULL NAME"), gbc);
        gbc.gridy++;
        gbc.insets =
            new Insets(0, 40, 15, 40); // Tighter gap between label and field
        formPanel.add(fullNameField = createMaterialField("RAHUL ISLAM"), gbc);

        gbc.insets = new Insets(5, 40, 15, 40); // Reset spacing
        gbc.gridy++;
        formPanel.add(createLabel("EMAIL ADDRESS"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 15, 40);
        formPanel.add(emailField = createMaterialField("HSTU@gmail.com"), gbc);

        gbc.insets = new Insets(5, 40, 15, 40);
        gbc.gridy++;
        formPanel.add(createLabel("USERNAME"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 15, 40);
        formPanel.add(usernameField = createMaterialField("rahul123"), gbc);

        gbc.insets = new Insets(5, 40, 15, 40);
        gbc.gridy++;
        formPanel.add(createLabel("I AM A"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 15, 40);

        // Custom Combo Box
        userTypeCombo = new JComboBox<>(new String[] {"🎓 Student", "💼 Alumni"});
        userTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userTypeCombo.setBackground(BG_COLOR);
        userTypeCombo.setBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, INPUT_BORDER));
        formPanel.add(userTypeCombo, gbc);

        gbc.insets = new Insets(5, 40, 15, 40);
        gbc.gridy++;
        formPanel.add(createLabel("VERIFICATION DOCUMENT"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 15, 40);
        JPanel verificationPanel = new JPanel(new BorderLayout(8, 0));
        verificationPanel.setOpaque(false);

        uploadDocButton = new JButton("Upload ID / Validation");
        styleMainButton(uploadDocButton);
        uploadDocButton.setPreferredSize(new Dimension(195, 40));
        uploadDocButton.addActionListener(e -> chooseVerificationDocument());

        verificationFileLabel = new JLabel("No file selected");
        verificationFileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        verificationFileLabel.setForeground(TEXT_SECONDARY);

        verificationPanel.add(uploadDocButton, BorderLayout.WEST);
        verificationPanel.add(verificationFileLabel, BorderLayout.CENTER);
        formPanel.add(verificationPanel, gbc);

        gbc.insets = new Insets(5, 40, 15, 40);
        gbc.gridy++;
        formPanel.add(createLabel("PASSWORD"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 15, 40);
        formPanel.add(passwordField = createMaterialPasswordField(), gbc);

        gbc.insets = new Insets(5, 40, 15, 40);
        passwordField.setBackground(new Color(40, 40, 55));
        gbc.gridy++;
        formPanel.add(createLabel("CONFIRM PASSWORD"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 5, 40); // Less space for checkbox
        formPanel.add(confirmPasswordField = createMaterialPasswordField(), gbc);
        confirmPasswordField.setBackground(new Color(40, 40, 55));

        // --- 3. Controls ---
        gbc.gridy++;
        showPasswordCheck = new JCheckBox("Show Password");
        showPasswordCheck.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        showPasswordCheck.setForeground(TEXT_SECONDARY);
        showPasswordCheck.setBackground(BG_COLOR);
        showPasswordCheck.setFocusPainted(false);
        showPasswordCheck.addActionListener(e -> {
            char c = showPasswordCheck.isSelected() ? (char) 0 : '•';
            passwordField.setEchoChar(c);
            confirmPasswordField.setEchoChar(c);
        });
        formPanel.add(showPasswordCheck, gbc);

        // --- 4. Buttons ---
        gbc.gridy++;
        gbc.insets = new Insets(25, 40, 10, 40); // Space before buttons

        registerButton = new JButton("Create Account");
        styleMainButton(registerButton);
        registerButton.addActionListener(e -> performRegistration());
        formPanel.add(registerButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 40, 30, 40); // Bottom padding

        cancelButton = new JButton("Cancel");
        styleGhostButton(cancelButton);
        cancelButton.addActionListener(e -> dispose());
        formPanel.add(cancelButton, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scrollPane);
    }

    private void chooseVerificationDocument() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Student ID / Alumni Validation Document");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
            "Image or PDF (jpg, jpeg, png, pdf)", "jpg", "jpeg", "png",
            "pdf"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!isAllowedDocument(file)) {
                showErrorDialog("Please select a valid document file (jpg, jpeg, png, pdf).");
                return;
            }
            if (file.length() > 5L * 1024L * 1024L) {
                showErrorDialog("Document size must be 5MB or less.");
                return;
            }
            selectedVerificationFile = file;
            verificationFileLabel.setText(file.getName());
            verificationFileLabel.setForeground(TEXT_PRIMARY);
        }
    }

    private boolean isAllowedDocument(File file) {
        String ext = getFileExtension(file.getName());
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || "pdf".equals(ext);
    }

    private String getFileExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1)
            return "";
        return name.substring(dot + 1).toLowerCase();
    }

    // --- UI Helper Methods ---

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    private JTextField createMaterialField(String placeholder) {
        JTextField field = new JTextField();
        styleMaterialInput(field, placeholder);
        return field;
    }

    private JPasswordField createMaterialPasswordField() {
        JPasswordField field = new JPasswordField();
        styleMaterialInput(field, "");
        return field;
    }

    private void styleMaterialInput(JTextField field, String placeholder) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_COLOR);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createMatteBorder(
            0, 0, 2, 0, INPUT_BORDER)); // Bottom border only

        // Placeholder Logic
        if (!placeholder.isEmpty()) {
            // Store placeholder in client property to access it in focus listener
            field.putClientProperty("placeholder", placeholder);
            // Initial State
            field.setText(placeholder);
            field.setForeground(TEXT_SECONDARY);
        }

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createMatteBorder(
                    0, 0, 2, 0, PRIMARY_BLUE)); // Highlight Blue
                if (!placeholder.isEmpty() && field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createMatteBorder(
                    0, 0, 1, 0, INPUT_BORDER)); // Reset Gray
                if (!placeholder.isEmpty() && field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_SECONDARY);
                }
            }
        });
    }

    private void styleMainButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY_BLUE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 45));

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PRIMARY_BLUE.darker());
            }
            public void mouseExited(MouseEvent e) { btn.setBackground(PRIMARY_BLUE); }
        });
    }

    private void styleGhostButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(BG_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); // Transparent
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover Effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(TEXT_PRIMARY);
            }
            public void mouseExited(MouseEvent e) {
                btn.setForeground(TEXT_SECONDARY);
            }
        });
    }

    // --- Logic Helper Methods (Keep clean) ---

    private String getFieldText(JTextField field) {
        String placeholder = (String) field.getClientProperty("placeholder");
        String text = field.getText().trim();
        if (placeholder != null && text.equals(placeholder)) {
            return "";
        }
        return text;
    }

    // --- YOUR EXISTING LOGIC BELOW (No Changes Needed) ---

    private void performRegistration() {
        String fullName = getFieldText(fullNameField);
        String email = getFieldText(emailField);
        String username = getFieldText(usernameField);
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String userType = ((String) userTypeCombo.getSelectedItem())
                              .replace("🎓 ", "")
                              .replace("💼 ", "");

        // Validate inputs
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showErrorDialog("Please fill in all fields!");
            return;
        }
        if (username.length() < 3) {
            showErrorDialog("Username must be at least 3 characters long!");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showErrorDialog("Please enter a valid email address!");
            return;
        }
        if (password.length() < 6) {
            showErrorDialog("Password must be at least 6 characters long!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showErrorDialog("Passwords do not match!");
            return;
        }
        if (selectedVerificationFile == null) {
            showErrorDialog(
                "Please upload your Student ID or Alumni validation document.");
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Creating...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return registerUser(username, password, fullName, email, userType,
                    selectedVerificationFile);
            }
            @Override
            protected void done() {
                try {
                    String error = get();
                    if (error == null) {
                        showSuccessDialog();
                    } else if ("duplicate".equals(error)) {
                        showErrorDialog(
                            "Username or email is already taken!\nPlease choose a different one.");
                    } else {
                        showErrorDialog("Registration failed!\n\nReason: " + error);
                    }
                } catch (Exception ex) {
                    showErrorDialog("Unexpected error: " + ex.getMessage());
                } finally {
                    registerButton.setEnabled(true);
                    registerButton.setText("Create Account");
                }
            }
        };
        worker.execute();
    }

    // Returns: null = success, "duplicate" = username/email taken, message =
    // other error
    private String registerUser(String username, String password, String fullName,
        String email, String userType,
        File verificationFile) {
        Connection conn = null;
        String savedDocPath = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert User with status=pending so admin must approve
            PreparedStatement userStmt = conn.prepareStatement(
                "INSERT INTO users (username, password, email, full_name, status, role, user_type) VALUES (?, ?, ?, ?, 'pending', 'member', ?)",
                Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, username);
            userStmt.setString(2, password);
            userStmt.setString(3, email);
            userStmt.setString(4, fullName);
            userStmt.setString(5, userType);

            if (userStmt.executeUpdate() > 0) {
                ResultSet rs = userStmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    PreparedStatement profStmt = conn.prepareStatement(
                        "INSERT INTO profiles (user_id, full_name, email, graduation_year, company, job_role, skills, bio) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    profStmt.setInt(1, userId);
                    profStmt.setString(2, fullName);
                    profStmt.setString(3, email);

                    if ("Alumni".equals(userType)) {
                        profStmt.setInt(4, 2020);
                        profStmt.setString(5, "N/A");
                        profStmt.setString(6, "Alumni");
                    } else {
                        profStmt.setInt(4, 2025);
                        profStmt.setString(5, "University");
                        profStmt.setString(6, "Student");
                    }
                    profStmt.setString(7, "Add skills...");
                    profStmt.setString(8, "Add bio...");
                    profStmt.executeUpdate();
                    profStmt.close();

                    savedDocPath = saveVerificationDocument(userId, verificationFile);
                    String documentType = "Student".equals(userType) ? "student_id"
                                                                       : "alumni_validation";

                    PreparedStatement verifyStmt = conn.prepareStatement(
                        "INSERT INTO identity_verifications (user_id, user_type, document_type, document_path, verification_status) VALUES (?, ?, ?, ?, 'pending')");
                    verifyStmt.setInt(1, userId);
                    verifyStmt.setString(2, userType);
                    verifyStmt.setString(3, documentType);
                    verifyStmt.setString(4, savedDocPath);
                    verifyStmt.executeUpdate();
                    verifyStmt.close();
                }
                userStmt.close();
                conn.commit();
                return null; // null = success
            }
            conn.rollback();
            return "Insert returned 0 rows. Please try again.";
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            // MySQL error 1062 = duplicate entry
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ignored) {
            }
            return "duplicate";
        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ignored) {
            }
            if (savedDocPath != null) {
                try {
                    Files.deleteIfExists(new File(savedDocPath).toPath());
                } catch (IOException ignored) {
                }
            }
            e.printStackTrace();
            return e.getMessage(); // show the real DB error
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception ignored) {
            }
        }
    }

    private String saveVerificationDocument(int userId, File sourceFile)
        throws IOException {
        File dir = new File("resources/verification_docs");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create verification_docs directory.");
        }

        String original = sourceFile.getName().replaceAll("[^A-Za-z0-9._-]", "_");
        String ext = getFileExtension(original);
        String filename = userId + "_" + System.currentTimeMillis() + (ext.isEmpty() ? "" : "." + ext);

        File target = new File(dir, filename);
        Files.copy(sourceFile.toPath(), target.toPath(),
            StandardCopyOption.REPLACE_EXISTING);
        return target.getPath().replace('\\', '/');
    }

    private void showErrorDialog(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",
            JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessDialog() {
        JOptionPane.showMessageDialog(
            this,
            "<html><b>Registration submitted successfully!</b><br><br>"
                + "Your account is now <b>pending admin approval</b>.<br>"
                + "You will be able to log in once an admin verifies<br>"
                + "and approves your identity.<br><br>"
                + "<i>Please check back later or contact the administrator.</i></html>",
            "Pending Approval", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> new RegistrationFrame().setVisible(true));
    }
}