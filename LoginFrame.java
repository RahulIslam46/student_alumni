import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;

public class LoginFrame extends JFrame {
  private JTextField usernameField;
  private JPasswordField passwordField;
  private JButton loginButton;
  private JButton registerButton;
  private JButton forgotPasswordButton;
  private JLabel errorLabel;

  public LoginFrame() {
    setTitle("ALUMNI - Parent Portal");
    setSize(950, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);

    // Main container with split design
    JPanel mainPanel = new JPanel(new GridLayout(1, 2, 0, 0));

    // Left Panel - Branding
    JPanel leftPanel = createLeftPanel();

    // Right Panel - Login Form
    JPanel rightPanel = createRightPanel();

    mainPanel.add(leftPanel);
    mainPanel.add(rightPanel);

    add(mainPanel);
  }

  private JPanel createLeftPanel() {
    JPanel panel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                             RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth();
        int h = getHeight();

        // Modern Indigo/Purple Gradient
        Color color1 = new Color(99, 102, 241);
        Color color2 = new Color(139, 92, 246);
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
      }
    };
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    JLabel titleLabel = new JLabel("ALUMNI");
    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 52));
    titleLabel.setForeground(Color.WHITE);
    gbc.gridy = 0;
    gbc.insets = new Insets(0, 0, 15, 0);
    panel.add(titleLabel, gbc);

    // JLabel subtitleLabel = new JLabel("Parent Portal");
    // subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
    // subtitleLabel.setForeground(new Color(255, 255, 255, 220));
    // gbc.gridy = 1;
    gbc.insets = new Insets(0, 0, 40, 0);
    // panel.add(subtitleLabel, gbc);

    JLabel taglineLabel = new JLabel(
        "<html><center>Connecting Students, Alumni<br>for Success</center></html>");
    taglineLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    taglineLabel.setForeground(new Color(255, 255, 255, 200));
    taglineLabel.setHorizontalAlignment(SwingConstants.CENTER);
    gbc.gridy = 2;
    panel.add(taglineLabel, gbc);

    return panel;
  }

  private JPanel createRightPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(new Color(22, 22, 35));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.CENTER;

    // Welcome Header
    JLabel welcomeLabel = new JLabel("Welcome Back!");
    welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
    welcomeLabel.setForeground(Color.WHITE);
    gbc.gridy = 0;
    gbc.insets = new Insets(0, 0, 6, 0);
    panel.add(welcomeLabel, gbc);

    JLabel loginLabel = new JLabel("Please login to your account");
    loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    loginLabel.setForeground(new Color(180, 180, 200));
    gbc.gridy = 1;
    gbc.insets = new Insets(0, 0, 24, 0);
    panel.add(loginLabel, gbc);

    // Error Label
    errorLabel = new JLabel("");
    errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    errorLabel.setForeground(new Color(231, 76, 60));
    errorLabel.setVisible(false);
    gbc.gridy = 2;
    gbc.insets = new Insets(0, 0, 12, 0);
    panel.add(errorLabel, gbc);

    // Input Fields
    setupTextField(panel, gbc, "Username or Email", 3);
    usernameField = createStyledTextField();
    gbc.gridy = 4;
    gbc.insets = new Insets(0, 0, 16, 0);
    panel.add(usernameField, gbc);

    setupTextField(panel, gbc, "Password", 5);
    passwordField = createStyledPasswordField();
    gbc.gridy = 6;
    gbc.insets = new Insets(0, 0, 18, 0);
    panel.add(passwordField, gbc);

    // Action Buttons
    loginButton = createMainButton("LOGIN", new Color(46, 204, 113),
                                   new Color(39, 174, 96));
    loginButton.addActionListener(e -> performLogin());
    gbc.gridy = 7;
    gbc.insets = new Insets(10, 0, 12, 0);
    panel.add(loginButton, gbc);

    registerButton = createMainButton("REGISTER", new Color(66, 133, 244),
                                      new Color(52, 120, 220));
    registerButton.addActionListener(e -> openSignUpForm());
    gbc.gridy = 8;
    gbc.insets = new Insets(0, 0, 12, 0);
    panel.add(registerButton, gbc);

    forgotPasswordButton = new JButton("Forgot Password?");
    forgotPasswordButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    forgotPasswordButton.setForeground(new Color(180, 180, 200));
    forgotPasswordButton.setContentAreaFilled(false);
    forgotPasswordButton.setBorderPainted(false);
    forgotPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    forgotPasswordButton.addActionListener(e -> showForgotPassword());
    gbc.gridy = 9;
    panel.add(forgotPasswordButton, gbc);

    JPanel paddedPanel = new JPanel(new GridBagLayout());
    paddedPanel.setBackground(new Color(22, 22, 35));
    paddedPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
    paddedPanel.add(panel);
    return paddedPanel;
  }

  // Helper to create buttons consistently
  private JButton createMainButton(String text, Color bg, Color hover) {
    JButton btn = new JButton(text);
    btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btn.setPreferredSize(new Dimension(380, 52));
    btn.setBackground(bg);
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setOpaque(true);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
      public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
    });
    return btn;
  }

  private JTextField createStyledTextField() {
    JTextField field = new JTextField(25);
    field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    field.setPreferredSize(new Dimension(380, 48));
    field.setBackground(new Color(35, 35, 58));
    field.setForeground(Color.WHITE);
    field.setCaretColor(Color.WHITE);
    field.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(new Color(80, 80, 130), 2, true),
        BorderFactory.createEmptyBorder(10, 15, 10, 15)));
    return field;
  }

  private JPasswordField createStyledPasswordField() {
    JPasswordField field = new JPasswordField(25);
    field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    field.setPreferredSize(new Dimension(380, 48));
    field.setBackground(new Color(35, 35, 58));
    field.setForeground(Color.WHITE);
    field.setCaretColor(Color.WHITE);
    field.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(new Color(80, 80, 130), 2, true),
        BorderFactory.createEmptyBorder(10, 15, 10, 15)));
    field.addActionListener(e -> performLogin());
    return field;
  }

  private void setupTextField(JPanel p, GridBagConstraints g, String text,
                              int y) {
    JLabel l = new JLabel(text);
    l.setFont(new Font("Segoe UI", Font.BOLD, 16));
    l.setForeground(Color.WHITE);
    g.gridy = y;
    g.insets = new Insets(12, 0, 8, 0);
    g.anchor = GridBagConstraints.WEST;
    p.add(l, g);
    g.anchor = GridBagConstraints.CENTER;
  }

  private void showForgotPassword() {
    JOptionPane.showMessageDialog(
        this, "Please contact the administrator: admin@alumnai.com",
        "Forgot Password", JOptionPane.INFORMATION_MESSAGE);
  }

  private void showError(String message) {
    errorLabel.setText(message);
    errorLabel.setVisible(true);
    new Timer(5000, e -> errorLabel.setVisible(false)).start();
  }

  private void performLogin() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());
    if (username.isEmpty() || password.isEmpty()) {
      showError(" Please enter both username and password");
      return;
    }

    loginButton.setEnabled(false);
    loginButton.setText("LOGGING IN...");

    // Returns: ["ok", role]  |  ["pending"]  |  ["rejected"]  |  ["fail"]
    new SwingWorker<String[], Void>() {
      @Override
      protected String[] doInBackground() throws Exception {
        return authenticateUser(username, password);
      }
      @Override
      protected void done() {
        try {
          String[] result = get();
          switch (result[0]) {
            case "ok" -> {
              String role = result[1];
              if ("admin".equals(role)) {
                // Admin → open the unified Admin Control Panel (with username so Visit Dashboard works)
                final String adminUser = username;
                SwingUtilities.invokeLater(() ->
                    new AdminControlPanel(adminUser).setVisible(true));
                dispose();
              } else {
                JOptionPane.showMessageDialog(LoginFrame.this,
                    "Login Successful!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                openMainFrame(username);
                dispose();
              }
            }
            case "pending" -> JOptionPane.showMessageDialog(LoginFrame.this,
                "<html><b>Account Pending Approval</b><br><br>"
                + "Your registration is awaiting admin verification.<br>"
                + "Please wait until an admin approves your account.<br><br>"
                + "<i>Contact admin@alumnai.com if you have questions.</i></html>",
                "Pending Approval", JOptionPane.WARNING_MESSAGE);
            case "rejected" -> JOptionPane.showMessageDialog(LoginFrame.this,
                "<html><b>Registration Rejected</b><br><br>"
                + "Your registration request was not approved by the admin.<br>"
                + "Please contact admin@alumnai.com for more information.</html>",
                "Access Denied", JOptionPane.ERROR_MESSAGE);
            default -> showError("✘ Invalid username or password!");
          }
        } catch (Exception ex) {
          showError("✘ Connection Error");
        } finally {
          loginButton.setEnabled(true);
          loginButton.setText("LOGIN");
        }
      }
    }.execute();
  }

  /**
   * Returns: ["ok", role] on success, ["pending"], ["rejected"], or ["fail"]
   */
  private String[] authenticateUser(String username, String password) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      String query = "SELECT role, COALESCE(status,'approved') AS status "
          + "FROM users WHERE (username = ? OR email = ?) AND password = ?";
      PreparedStatement pstmt = conn.prepareStatement(query);
      pstmt.setString(1, username);
      pstmt.setString(2, username); // allow login by email too
      pstmt.setString(3, password);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        String status = rs.getString("status");
        String role   = rs.getString("role");
        if (role == null) role = "member";
        return switch (status) {
          case "approved" -> new String[]{"ok", role};
          case "pending"  -> new String[]{"pending"};
          case "rejected" -> new String[]{"rejected"};
          default         -> new String[]{"ok", role};
        };
      }
      return new String[]{"fail"};
    } catch (SQLException ex) {
      ex.printStackTrace();
      return new String[]{"fail"};
    }
  }

  private void openMainFrame(String user) {
    SwingUtilities.invokeLater(
        () -> new ModernDashboardUI(user).setVisible(true));
  }

  private void openSignUpForm() {
    SwingUtilities.invokeLater(() -> new RegistrationFrame().setVisible(true));
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
  }
}