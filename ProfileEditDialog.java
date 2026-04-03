import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ProfileEditDialog extends JDialog {
    private int userId;
    private AlumniProfile profile;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField companyField;
    private JTextField jobRoleField;
    private JTextField yearField;
    private JTextField skillsField;
    private JTextArea bioArea;
    private JCheckBox mentorshipCheckbox;
    private JLabel picPreview;
    private String selectedPicturePath;

    public ProfileEditDialog(JFrame parent, int userId, String username,
        AlumniProfile existingProfile) {
        super(parent, "Edit Profile", true);
        this.userId = userId;
        this.profile = existingProfile;

        setSize(600, 600);
        setLocationRelativeTo(parent);

        initComponents();

        if (profile != null) {
            loadProfileData();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Create/Edit Your Alumni Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel);

        // ---- Picture upload section ----
        JPanel pictureSection = new JPanel();
        pictureSection.setLayout(new BoxLayout(pictureSection, BoxLayout.Y_AXIS));
        pictureSection.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

        picPreview = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getIcon() != null) {
                    // Draw circular clipped image
                    int size = Math.min(getWidth(), getHeight());
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(x, y, size, size));
                    super.paintComponent(g2);
                    g2.setClip(null);
                    g2.setColor(new Color(66, 153, 225));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawOval(x, y, size - 1, size - 1);
                } else {
                    // Placeholder circle
                    g2.setColor(new Color(200, 220, 240));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(new Color(100, 150, 200));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                    FontMetrics fm = g2.getFontMetrics();
                    String txt = "📷";
                    g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        picPreview.setPreferredSize(new Dimension(100, 100));
        picPreview.setMaximumSize(new Dimension(100, 100));
        picPreview.setMinimumSize(new Dimension(100, 100));
        picPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        picPreview.setHorizontalAlignment(SwingConstants.CENTER);
        pictureSection.add(picPreview);

        pictureSection.add(Box.createVerticalStrut(8));

        JButton uploadBtn = new JButton("📁 Upload Photo");
        uploadBtn.setFont(new Font("Arial", Font.BOLD, 16));
        uploadBtn.setBackground(new Color(66, 153, 225));
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setFocusPainted(false);
        uploadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadBtn.addActionListener(e -> choosePicture());
        pictureSection.add(uploadBtn);
        // ---- End picture section ----

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        formPanel.add(new JLabel("Full Name:*"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Email:*"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Company:"));
        companyField = new JTextField();
        formPanel.add(companyField);

        formPanel.add(new JLabel("Job Role:"));
        jobRoleField = new JTextField();
        formPanel.add(jobRoleField);

        formPanel.add(new JLabel("Graduation Year:*"));
        yearField = new JTextField();
        formPanel.add(yearField);

        formPanel.add(new JLabel("Skills:"));
        skillsField = new JTextField();
        formPanel.add(skillsField);

        formPanel.add(new JLabel("Bio:"));
        bioArea = new JTextArea(3, 20);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        JScrollPane bioScroll = new JScrollPane(bioArea);
        formPanel.add(bioScroll);

        formPanel.add(new JLabel("Available for Mentorship:"));
        mentorshipCheckbox = new JCheckBox();
        mentorshipCheckbox.setSelected(true);
        formPanel.add(mentorshipCheckbox);

        // Wrap picture + form
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(pictureSection);
        centerPanel.add(formPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save Profile");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.setFont(new Font("Arial", Font.BOLD, 16));
        saveBtn.setBackground(new Color(39, 174, 96));
        saveBtn.setForeground(Color.WHITE);

        saveBtn.addActionListener(e -> saveProfile());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(centerPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadProfileData() {
        nameField.setText(profile.getFullName());
        emailField.setText(profile.getEmail());
        companyField.setText(profile.getCompany());
        jobRoleField.setText(profile.getJobRole());
        yearField.setText(String.valueOf(profile.getGraduationYear()));
        skillsField.setText(profile.getSkills());
        bioArea.setText(profile.getBio());
        mentorshipCheckbox.setSelected(profile.isAvailableForMentorship());
        // Load existing profile picture
        if (profile.getProfilePicturePath() != null && !profile.getProfilePicturePath().isEmpty()) {
            java.io.File f = new java.io.File(profile.getProfilePicturePath());
            if (f.exists()) {
                selectedPicturePath = profile.getProfilePicturePath();
                setPreviewImage(selectedPicturePath);
            }
        }
    }

    private void setPreviewImage(String path) {
        try {
            BufferedImage raw = ImageIO.read(new File(path));
            if (raw == null)
                return;
            // Draw into a 100x100 BufferedImage synchronously
            BufferedImage scaled =
                new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            // Clip circle
            g.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, 100, 100));
            g.drawImage(raw, 0, 0, 100, 100, null);
            g.setClip(null);
            g.setColor(new Color(66, 153, 225));
            g.setStroke(new BasicStroke(3f));
            g.drawOval(1, 1, 98, 98);
            g.dispose();
            picPreview.setIcon(new ImageIcon(scaled));
        } catch (IOException e) {
            System.err.println("Preview load failed: " + e.getMessage());
        }
    }

    private void choosePicture() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Profile Picture");
        fc.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg",
            "png", "gif", "bmp"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File chosen = fc.getSelectedFile();
            try {
                // Create destination directory if needed
                File destDir = new File("resources/images/profiles");
                destDir.mkdirs();
                // Determine extension
                String name = chosen.getName();
                String ext =
                    name.contains(".") ? name.substring(name.lastIndexOf('.')) : ".jpg";
                // Use userId as file name to overwrite old ones
                File dest = new File(destDir, "user_" + userId + ext);
                Files.copy(chosen.toPath(), dest.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
                // Use absolute path so it always resolves regardless of working dir
                selectedPicturePath = dest.getAbsolutePath();
                // Update preview synchronously
                setPreviewImage(selectedPicturePath);
                picPreview.repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    this, "Could not copy image: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveProfile() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String company = companyField.getText().trim();
        String jobRole = jobRoleField.getText().trim();
        String yearStr = yearField.getText().trim();
        String skills = skillsField.getText().trim();
        String bio = bioArea.getText().trim();
        boolean mentorship = mentorshipCheckbox.isSelected();

        if (name.isEmpty() || email.isEmpty() || yearStr.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please fill in all required fields (*)", "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
            if (year < 1950 || year > 2030) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this, "Please enter a valid graduation year (1950-2030)",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        AlumniProfile newProfile = new AlumniProfile();
        newProfile.setUserId(userId);
        newProfile.setFullName(name);
        newProfile.setEmail(email);
        newProfile.setCompany(company);
        newProfile.setJobRole(jobRole);
        newProfile.setGraduationYear(year);
        newProfile.setSkills(skills);
        newProfile.setBio(bio);
        newProfile.setAvailableForMentorship(mentorship);
        // Preserve existing picture if no new one was chosen
        if (selectedPicturePath != null && !selectedPicturePath.isEmpty()) {
            newProfile.setProfilePicturePath(selectedPicturePath);
        } else if (profile != null && profile.getProfilePicturePath() != null) {
            newProfile.setProfilePicturePath(profile.getProfilePicturePath());
        }

        if (AlumniDAO.saveProfile(newProfile)) {
            JOptionPane.showMessageDialog(this, "Profile saved successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save profile!", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame();
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Create dialog with test user ID and existing profile
            AlumniProfile testProfile = new AlumniProfile();
            testProfile.setFullName("Test User");
            testProfile.setEmail("test@example.com");
            testProfile.setGraduationYear(2020);
            ProfileEditDialog dialog =
                new ProfileEditDialog(testFrame, 1, "TestUser", testProfile);
            dialog.setVisible(true);
        });
    }
}
