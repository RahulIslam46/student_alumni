import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Deque;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class ModernDashboardUI extends JFrame {
    // --- ENHANCED COLOR PALETTE ---
    private final Color SIDEBAR_BG = new Color(18, 22, 36); // Deeper Sidebar
    private final Color SIDEBAR_HOVER = new Color(45, 55, 72); // Lighter Navy
    private final Color MAIN_BG =
        new Color(15, 17, 28); // Deep Dark BG    // Ultra Light Gray
    private final Color CARD_BG = new Color(28, 33, 52); // Dark Card BG
    private final Color PRIMARY_BLUE = new Color(66, 153, 225); // Modern Blue
    private final Color PRIMARY_HOVER = new Color(49, 130, 206); // Darker Blue
    private final Color SUCCESS_GREEN = new Color(72, 187, 120); // Modern Green
    private final Color ACCENT_PURPLE = new Color(159, 122, 234); // Purple Accent
    private final Color TEXT_DARK = new Color(220, 230, 255); // Bright Text
    private final Color TEXT_GRAY = new Color(160, 175, 210); // Muted Text
    private final Color BORDER_COLOR = new Color(60, 70, 110); // Visible Border
    private final Color STUDENT_BG = new Color(30, 65, 120); // Dark Blue Badge BG
    private final Color STUDENT_TEXT =
        new Color(140, 195, 255); // Bright Blue Text
    private final Color ALUMNI_BG = new Color(20, 75, 50); // Dark Green Badge BG
    private final Color ALUMNI_TEXT =
        new Color(80, 210, 130); // Bright Green Text

    private JPanel contentPanel;
    private int currentUserId;
    private String currentUsername;
    private boolean currentUserIsAdmin = false;
    private CardLayout contentCardLayout;
    private JPanel mainContentArea;
    private JLabel sidebarUserAvatar;
    private JButton backButton;
    private String currentView = "DASHBOARD";
    private final Deque<String> navHistory = new ArrayDeque<>();

    public ModernDashboardUI(String username) {
        this.currentUsername = username;

        // Get User ID from database
        try {
            this.currentUserId = AlumniDAO.getUserId(username);
            if (this.currentUserId == -1) {
                JOptionPane.showMessageDialog(this, "User not found in database!",
                    "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
            this.currentUserIsAdmin = AlumniDAO.isAdminUser(this.currentUserId);
            // Ensure profile_picture column exists in DB
            AlumniDAO.ensureProfilePictureColumn();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this, "Database connection error:  " + e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // Frame Setup
        setTitle("Alumni Connect - Modern Dashboard");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(MAIN_BG);

        // Sidebar
        add(createModernSidebar(), BorderLayout.WEST);

        // Main Content Area
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(MAIN_BG);

        // Top Header Bar
        rightPanel.add(createModernHeader(), BorderLayout.NORTH);

        // Content Area with CardLayout for different views
        contentCardLayout = new CardLayout();
        mainContentArea = new JPanel(contentCardLayout);
        mainContentArea.setBackground(MAIN_BG);

        // Dashboard View
        JPanel dashboardView = createDashboardView();
        mainContentArea.add(dashboardView, "DASHBOARD");

        // Profile View
        JPanel profileView = createProfileView();
        mainContentArea.add(profileView, "PROFILE");

        // Messages View
        JPanel messagesView = createMessagesView();
        mainContentArea.add(messagesView, "MESSAGES");

        // Notifications View
        JPanel notificationsView = createNotificationsView();
        mainContentArea.add(notificationsView, "NOTIFICATIONS");

        // Achievements View
        JPanel achievementsView = createAchievementsView();
        mainContentArea.add(achievementsView, "ACHIEVEMENTS");

        // Settings View
        // JPanel settingsView =
        //     createPlaceholderView("⚙️", "Settings", "Manage your
        //     preferences");
        // mainContentArea.add(settingsView, "SETTINGS");

        rightPanel.add(mainContentArea, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);

        // Load initial data
        loadDashboardData("");
    }

    // ==========================================
    // GETTERS
    // ==========================================
    public String getCurrentUsername() { return currentUsername; }

    // ==========================================
    // VIEW SWITCHER
    // ==========================================
    private void switchView(String viewName) {
        if (!viewName.equals(currentView)) {
            navHistory.push(currentView); // save where we came from
        }
        currentView = viewName;
        contentCardLayout.show(mainContentArea, viewName);
        // Show back button only when not on dashboard
        if (backButton != null) {
            backButton.setVisible(!"DASHBOARD".equals(viewName));
        }
        // Reload data for specific views
        if ("DASHBOARD".equals(viewName)) {
            loadDashboardData("");
        } else if ("PROFILE".equals(viewName)) {
            loadProfileData();
        } else if ("MESSAGES".equals(viewName)) {
            loadMessagesData();
        }
    }

    private void navigateBack() {
        if (!navHistory.isEmpty()) {
            String prev = navHistory.pop();
            currentView = prev;
            contentCardLayout.show(mainContentArea, prev);
            if (backButton != null) {
                backButton.setVisible(!"DASHBOARD".equals(prev));
            }
            if ("DASHBOARD".equals(prev)) {
                loadDashboardData("");
            } else if ("PROFILE".equals(prev)) {
                loadProfileData();
            } else if ("MESSAGES".equals(prev)) {
                loadMessagesData();
            }
        } else {
            // Already at root — go to dashboard
            currentView = "DASHBOARD";
            contentCardLayout.show(mainContentArea, "DASHBOARD");
            if (backButton != null)
                backButton.setVisible(false);
            loadDashboardData("");
        }
    }

    // ==========================================
    // MODERN SIDEBAR
    // ==========================================
    private JPanel createModernSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(60, 70, 110)));

        // Top Section - Logo & Navigation
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(SIDEBAR_BG);
        topSection.setBorder(new EmptyBorder(35, 8, 20, 8));

        // Logo with Icon
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setMaximumSize(new Dimension(280, 60));

        JLabel logoIcon = new JLabel("🎓");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JLabel logoText = new JLabel("Alumni Connect");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoText.setForeground(Color.WHITE);

        logoPanel.add(logoIcon);
        logoPanel.add(logoText);

        topSection.add(logoPanel);
        topSection.add(Box.createVerticalStrut(50));

        // Navigation Menu
        String[][] menuItems = {{"🏠", "Dashboard", "DASHBOARD"},
            {"👤", "My Profile", "PROFILE"},
            {"💬", "Messages", "MESSAGES"},
            {"🔔", "Notifications", "NOTIFICATIONS"},
            {"🏆", "Achievements", "ACHIEVEMENTS"}};
        // {"⚙️", "Settings", "SETTINGS"}};

        ButtonGroup menuGroup = new ButtonGroup();
        for (int i = 0; i < menuItems.length; i++) {
            final String viewName = menuItems[i][2];
            JToggleButton btn =
                createModernMenuButton(menuItems[i][0], menuItems[i][1], i == 0);
            btn.addActionListener(e -> switchView(viewName));
            menuGroup.add(btn);
            topSection.add(btn);
            topSection.add(Box.createVerticalStrut(8));
        }

        // Add Admin Control button (always visible; password-protected inside)
        topSection.add(Box.createVerticalStrut(20));
        JButton adminBtn = createAdminButton();
        topSection.add(adminBtn);

        sidebar.add(topSection, BorderLayout.NORTH);

        // Bottom Section - User Info & Logout
        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setBackground(SIDEBAR_BG);
        bottomSection.setBorder(new EmptyBorder(20, 20, 30, 20));

        // Push everything to bottom nicely
        bottomSection.add(Box.createVerticalGlue());

        // User Profile Card
        JPanel userCard = new JPanel(new BorderLayout(12, 0));
        userCard.setBackground(new Color(35, 42, 62));
        userCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        userCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel userAvatar;
        AlumniProfile sidebarProfile = AlumniDAO.getProfileByUserId(currentUserId);
        String sidePic =
            sidebarProfile != null ? sidebarProfile.getProfilePicturePath() : null;
        if (sidePic != null && !sidePic.isEmpty() && new java.io.File(sidePic).exists()) {
            userAvatar = new JLabel(new CircularImageIcon(sidePic, 42));
        } else {
            userAvatar =
                new JLabel(new AvatarIcon(currentUsername, 42, PRIMARY_BLUE));
        }
        sidebarUserAvatar = userAvatar;

        JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        userInfo.setBackground(new Color(35, 42, 62));

        JLabel userName = new JLabel(currentUsername);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        userName.setForeground(Color.WHITE);

        JLabel userStatus = new JLabel("Active");
        userStatus.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userStatus.setForeground(new Color(160, 174, 192));

        userInfo.add(userName);
        userInfo.add(userStatus);

        userCard.add(userAvatar, BorderLayout.WEST);
        userCard.add(userInfo, BorderLayout.CENTER);

        bottomSection.add(userCard);
        bottomSection.add(Box.createVerticalStrut(15));

        // Logout Button
        JButton logoutBtn = createLogoutButton();
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        bottomSection.add(logoutBtn);

        sidebar.add(bottomSection, BorderLayout.SOUTH);

        return sidebar;
    }

    private JToggleButton createModernMenuButton(String icon, String text,
        boolean selected) {
        final String iconStr = icon;
        final String labelStr = text;

        JToggleButton btn = new JToggleButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth(), h = getHeight();

                if (isSelected()) {
                    // 2D flat pill - full width, solid indigo
                    g2.setColor(new Color(99, 102, 241));
                    g2.fillRoundRect(2, 3, w - 4, h - 6, 12, 12);
                    // Subtle top gloss
                    g2.setColor(new Color(255, 255, 255, 22));
                    g2.fillRoundRect(2, 3, w - 4, (h - 6) / 2, 12, 12);
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(99, 102, 241, 70));
                    g2.fillRoundRect(2, 3, w - 4, h - 6, 12, 12);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.fillRoundRect(2, 3, w - 4, h - 6, 12, 12);
                }

                // Draw icon (emoji) + label text — avoids the □ toggle indicator
                boolean sel = isSelected();

                // Emoji icon — pushed to left
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 17));
                FontMetrics fmIcon = g2.getFontMetrics();
                int textY = (h + fmIcon.getAscent() - fmIcon.getDescent()) / 2;
                g2.setColor(sel ? Color.WHITE : new Color(160, 180, 225));
                g2.drawString(iconStr, 14, textY);

                // Label — pushed close to icon
                g2.setFont(new Font("Segoe UI", sel ? Font.BOLD : Font.PLAIN, 16));
                FontMetrics fmLabel = g2.getFontMetrics();
                textY = (h + fmLabel.getAscent() - fmLabel.getDescent()) / 2;
                g2.setColor(sel ? Color.WHITE : new Color(185, 200, 235));
                g2.drawString(labelStr, 44, textY);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(244, 50);
            }
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(200, 50);
            }
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 50);
            }
        };

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(0, 0, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setSelected(selected);
        btn.setRolloverEnabled(true);

        return btn;
    }

    private JButton createLogoutButton() {
        JButton btn = new JButton("🚪  Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(180, 40, 55)); // Dark pressed
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(220, 70, 85)); // Soft hover
                } else {
                    g2.setColor(new Color(200, 55, 70)); // Normal modern red
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(new Color(229, 62, 62));
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setForeground(Color.WHITE);
        btn.setMaximumSize(new Dimension(280, 45));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                // Open login frame
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });

        return btn;
    }

    private JButton createAdminButton() {
        JButton btn = new JButton("⚙️  Admin Panel") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())
                    g2.setColor(new Color(142, 68, 173));
                else if (getModel().isRollover())
                    g2.setColor(new Color(155, 89, 182));
                else
                    g2.setColor(new Color(155, 89, 182, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(new Color(155, 89, 182));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setMaximumSize(new Dimension(280, 45));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            JPasswordField passwordField = new JPasswordField();
            int option = JOptionPane.showConfirmDialog(
                this, passwordField,
                "Enter Admin Password:", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                String password = new String(passwordField.getPassword());
                if ("admin123".equals(password)) {
                    SwingUtilities.invokeLater(
                        ()
                            -> new AdminControlPanel(ModernDashboardUI.this)
                                   .setVisible(true));
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid password!",
                        "Access Denied",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return btn;
    }

    // ==========================================
    // MODERN HEADER
    // ==========================================
    private JPanel createModernHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(new Color(18, 22, 36));
        header.setPreferredSize(new Dimension(getWidth(), 90));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(20, 35, 20, 35)));

        // ---- Back button (hidden on dashboard) ----
        backButton = new JButton("\u2190 Back");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        backButton.setForeground(PRIMARY_BLUE);
        backButton.setBackground(new Color(18, 22, 36));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setVisible(false); // hidden on start (dashboard is default)
        backButton.addActionListener(e -> navigateBack());

        // Left - Back + Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        searchPanel.setBackground(new Color(18, 22, 36));
        searchPanel.add(backButton);

        JPanel searchBox = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MAIN_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
            }
        };
        searchBox.setPreferredSize(new Dimension(450, 50));
        searchBox.setBorder(new EmptyBorder(8, 20, 8, 20));
        searchBox.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setBorder(null);
        searchField.setBackground(MAIN_BG);
        searchField.setForeground(Color.WHITE);
        setPlaceholder(searchField, "Search alumni by name, skills, company...");

        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(searchField, BorderLayout.CENTER);

        JButton searchBtn = createModernButton("Search", PRIMARY_BLUE);
        searchBtn.setPreferredSize(new Dimension(100, 50));
        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.equals("Search alumni by name, skills, company...")) {
                query = "";
            }
            loadDashboardData(query);
            switchView("DASHBOARD");
        });

        // Add Enter key support for search
        searchField.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.equals("Search alumni by name, skills, company...")) {
                query = "";
            }
            loadDashboardData(query);
            switchView("DASHBOARD");
        });

        searchPanel.add(searchBox);
        searchPanel.add(searchBtn);

        // Right - Action Buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionsPanel.setBackground(new Color(18, 22, 36));

        // Notification Button with Bell Icon
        JButton notifBtn;
        try {
            // Try to load bell icon from resources
            ImageIcon bellIcon = new ImageIcon("resources/images/bell-icon.jpg");
            if (bellIcon.getIconWidth() > 0) {
                // Scale image to fit button
                Image scaledImage =
                    bellIcon.getImage().getScaledInstance(30, 40, Image.SCALE_SMOOTH);
                notifBtn = new JButton(new ImageIcon(scaledImage));
                notifBtn.setPreferredSize(new Dimension(50, 50));
                notifBtn.setContentAreaFilled(false);
                notifBtn.setFocusPainted(false);
                notifBtn.setBorderPainted(false);
                notifBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                notifBtn.setToolTipText("Notifications");
            } else {
                // Fallback to emoji if image not found
                notifBtn = createIconButton("🔔", "Notifications");
            }
        } catch (Exception e) {
            // Fallback to emoji if error
            notifBtn = createIconButton("🔔", "Notifications");
        }

        // Add unread notification count badge
        int unreadCount = AlumniDAO.getUnreadNotificationCount(currentUserId);
        if (unreadCount > 0) {
            notifBtn.setText(String.valueOf(unreadCount));
            notifBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
            notifBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            notifBtn.setForeground(new Color(220, 53, 69)); // Red badge color
        }
        notifBtn.addActionListener(e -> switchView("NOTIFICATIONS"));

        actionsPanel.add(notifBtn);

        header.add(searchPanel, BorderLayout.WEST);
        header.add(actionsPanel, BorderLayout.EAST);

        return header;
    }

    // ==========================================
    // DASHBOARD VIEW
    // ==========================================
    private JPanel createDashboardView() {
        JPanel view = new JPanel(new BorderLayout());
        view.setBackground(MAIN_BG);

        // Stats Cards at Top
        JPanel statsPanel = createStatsPanel();
        view.add(statsPanel, BorderLayout.NORTH);

        // Scrollable Content
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(MAIN_BG);
        contentPanel.setBorder(new EmptyBorder(25, 35, 35, 35));

        JScrollPane scrollPane = createStyledScrollPane(contentPanel);
        view.add(scrollPane, BorderLayout.CENTER);

        return view;
    }

    // ==========================================
    // PROFILE VIEW
    // ==========================================
    private JPanel createProfileView() {
        JPanel view = new JPanel(new BorderLayout());
        view.setBackground(MAIN_BG);

        JPanel profileContent = new JPanel();
        profileContent.setLayout(new BoxLayout(profileContent, BoxLayout.Y_AXIS));
        profileContent.setBackground(MAIN_BG);
        profileContent.setBorder(new EmptyBorder(35, 35, 35, 35));

        JScrollPane scrollPane = createStyledScrollPane(profileContent);
        view.add(scrollPane, BorderLayout.CENTER);

        return view;
    }

    private void loadProfileData() {
        JPanel profileContent =
            (JPanel) ((JScrollPane) ((JPanel) mainContentArea.getComponent(1))
                          .getComponent(0))
                .getViewport()
                .getView();
        profileContent.removeAll();

        AlumniProfile myProfile = AlumniDAO.getProfileByUserId(currentUserId);
        if (myProfile == null) {
            myProfile = new AlumniProfile();
            myProfile.setUserId(currentUserId);
            myProfile.setFullName(currentUsername);
            myProfile.setEmail(currentUsername + "@example.com");
            myProfile.setCompany("Not specified");
            myProfile.setJobRole("Not specified");
            myProfile.setGraduationYear(2020);
            myProfile.setSkills("Not specified");
            myProfile.setBio("Please update your profile");
        }

        final AlumniProfile profileToEdit = myProfile;

        // Header Card with gradient accent top band
        JPanel headerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(28, 33, 52));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                GradientPaint gp =
                    new GradientPaint(0, 0, new Color(99, 102, 241), getWidth(), 0,
                        new Color(66, 153, 225));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 10, 18, 18);
                g2.fillRect(0, 5, getWidth(), 5);
                g2.setColor(new Color(70, 80, 130));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        headerCard.setOpaque(false);
        headerCard.setLayout(new BoxLayout(headerCard, BoxLayout.Y_AXIS));
        headerCard.setBorder(new EmptyBorder(36, 30, 28, 30));
        headerCard.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel avatar;
        String picPath = myProfile.getProfilePicturePath();
        if (picPath != null && !picPath.isEmpty() && new java.io.File(picPath).exists()) {
            avatar = new JLabel(new CircularImageIcon(picPath, 110));
        } else {
            avatar = new JLabel(
                new AvatarIcon(myProfile.getFullName(), 110, PRIMARY_BLUE));
        }
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerCard.add(avatar);
        headerCard.add(Box.createVerticalStrut(16));

        JLabel nameLabel = new JLabel(myProfile.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerCard.add(nameLabel);
        headerCard.add(Box.createVerticalStrut(5));

        String roleStr = (myProfile.getJobRole() != null && !myProfile.getJobRole().equals("Not specified")
                                 ? myProfile.getJobRole()
                                 : "")
            + (myProfile.getCompany() != null && !myProfile.getCompany().equals("Not specified")
                    ? "  at  " + myProfile.getCompany()
                    : "");
        if (!roleStr.isBlank()) {
            JLabel roleTagLabel = new JLabel(roleStr.trim());
            roleTagLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            roleTagLabel.setForeground(new Color(130, 160, 230));
            roleTagLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            headerCard.add(roleTagLabel);
            headerCard.add(Box.createVerticalStrut(4));
        }

        JLabel emailLabel = new JLabel(myProfile.getEmail());
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        emailLabel.setForeground(new Color(110, 135, 200));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerCard.add(emailLabel);
        headerCard.add(Box.createVerticalStrut(20));

        JSeparator hSep = new JSeparator();
        hSep.setForeground(new Color(60, 70, 110));
        hSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        headerCard.add(hSep);
        headerCard.add(Box.createVerticalStrut(16));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setOpaque(false);

        JButton editBtn = new JButton("  Edit Profile  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(79, 82, 220)
                        : getModel().isRollover()  ? new Color(115, 118, 255)
                                                   : new Color(99, 102, 241));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        editBtn.setForeground(Color.WHITE);
        editBtn.setPreferredSize(new Dimension(150, 44));
        editBtn.setContentAreaFilled(false);
        editBtn.setFocusPainted(false);
        editBtn.setBorderPainted(false);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> editProfile(profileToEdit));

        JButton changePhotoBtn = new JButton("  Change Photo  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(45, 50, 78)
                        : getModel().isRollover()  ? new Color(52, 60, 90)
                                                   : new Color(38, 45, 70));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(80, 95, 150));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        changePhotoBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        changePhotoBtn.setForeground(new Color(180, 200, 255));
        changePhotoBtn.setPreferredSize(new Dimension(168, 44));
        changePhotoBtn.setContentAreaFilled(false);
        changePhotoBtn.setFocusPainted(false);
        changePhotoBtn.setBorderPainted(false);
        changePhotoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePhotoBtn.addActionListener(e -> editProfile(profileToEdit));

        btnRow.add(editBtn);
        btnRow.add(changePhotoBtn);
        headerCard.add(btnRow);

        profileContent.add(headerCard);
        profileContent.add(Box.createVerticalStrut(18));

        JPanel detailsCard = createCard();
        detailsCard.setLayout(new BoxLayout(detailsCard, BoxLayout.Y_AXIS));

        JLabel detailsTitle = new JLabel("Profile Details");
        detailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        detailsTitle.setForeground(new Color(160, 185, 255));
        detailsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsCard.add(detailsTitle);
        detailsCard.add(Box.createVerticalStrut(16));

        addProfileDetailRow(detailsCard, "Company",
            myProfile.getCompany() != null ? myProfile.getCompany()
                                           : "Not specified");
        addProfileDetailRow(detailsCard, "Job Role",
            myProfile.getJobRole() != null ? myProfile.getJobRole()
                                           : "Not specified");
        addProfileDetailRow(detailsCard, "Graduation Year",
            String.valueOf(myProfile.getGraduationYear()));
        addProfileDetailRow(detailsCard, "Skills",
            myProfile.getSkills() != null ? myProfile.getSkills()
                                          : "Not specified");

        profileContent.add(detailsCard);
        profileContent.add(Box.createVerticalStrut(18));

        JPanel bioCard = createCard();
        bioCard.setLayout(new BoxLayout(bioCard, BoxLayout.Y_AXIS));

        JLabel bioLabel = new JLabel("About Me");
        bioLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bioLabel.setForeground(new Color(160, 185, 255));
        bioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bioCard.add(bioLabel);
        bioCard.add(Box.createVerticalStrut(12));

        JSeparator bioSep = new JSeparator();
        bioSep.setForeground(new Color(60, 70, 110));
        bioSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bioCard.add(bioSep);
        bioCard.add(Box.createVerticalStrut(14));

        JTextArea bioText = new JTextArea(
            myProfile.getBio() != null && !myProfile.getBio().isEmpty()
                ? myProfile.getBio()
                : "No bio yet. Click Edit Profile to add one.");
        bioText.setLineWrap(true);
        bioText.setWrapStyleWord(true);
        bioText.setEditable(false);
        bioText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        bioText.setOpaque(false);
        bioText.setForeground(new Color(210, 220, 255));
        bioText.setBorder(null);
        bioCard.add(bioText);

        profileContent.add(bioCard);

        profileContent.revalidate();
        profileContent.repaint();
    }

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(60, 70, 110));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBackground(new Color(28, 33, 52));
        card.setBorder(new EmptyBorder(22, 25, 22, 25));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private void addProfileDetailRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(38, 44, 68));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Left accent stripe
                g2.setColor(new Color(99, 102, 241, 180));
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelComp.setForeground(new Color(150, 175, 240));
        labelComp.setPreferredSize(new Dimension(175, 28));

        JLabel valueComp =
            new JLabel((value == null || value.isBlank() || value.equals("Not specified") || value.equals("0"))
                    ? "Not set"
                    : value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        valueComp.setForeground(Color.WHITE);

        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.CENTER);

        parent.add(row);
        parent.add(Box.createVerticalStrut(8));
    }

    private void editProfile(AlumniProfile profile) {
        ProfileEditDialog dialog =
            new ProfileEditDialog(this, currentUserId, currentUsername, profile);
        dialog.setVisible(true);
        loadProfileData(); // Reload profile view
        refreshSidebarAvatar(); // Refresh sidebar photo
    }

    /**
     * Update the sidebar bottom‑left avatar after a profile picture change.
     */
    private void refreshSidebarAvatar() {
        if (sidebarUserAvatar == null)
            return;
        AlumniProfile p = AlumniDAO.getProfileByUserId(currentUserId);
        String pic = p != null ? p.getProfilePicturePath() : null;
        if (pic != null && !pic.isEmpty() && new java.io.File(pic).exists()) {
            sidebarUserAvatar.setIcon(new CircularImageIcon(pic, 42));
        } else {
            sidebarUserAvatar.setIcon(
                new AvatarIcon(currentUsername, 42, PRIMARY_BLUE));
        }
        sidebarUserAvatar.revalidate();
        sidebarUserAvatar.repaint();
    }

    // ==========================================
    // MESSAGES VIEW
    // ==========================================
    private JPanel createMessagesView() {
        JPanel view = new JPanel(new BorderLayout());
        view.setBackground(MAIN_BG);

        JPanel messagesContent = new JPanel();
        messagesContent.setLayout(new BoxLayout(messagesContent, BoxLayout.Y_AXIS));
        messagesContent.setBackground(MAIN_BG);
        messagesContent.setBorder(new EmptyBorder(35, 35, 35, 35));

        JScrollPane scrollPane = createStyledScrollPane(messagesContent);
        view.add(scrollPane, BorderLayout.CENTER);

        return view;
    }

    private void loadMessagesData() {
        JPanel messagesContent =
            (JPanel) ((JScrollPane) ((JPanel) mainContentArea.getComponent(2))
                          .getComponent(0))
                .getViewport()
                .getView();
        messagesContent.removeAll();

        JLabel titleLabel = new JLabel("💬 My Conversations");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        messagesContent.add(titleLabel);
        messagesContent.add(Box.createVerticalStrut(20));

        List<Integer> conversations = AlumniDAO.getAllConversations(currentUserId);

        if (conversations.isEmpty()) {
            JPanel emptyCard = createCard();
            emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));

            JLabel icon = new JLabel("💬");
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyCard.add(icon);

            emptyCard.add(Box.createVerticalStrut(15));
            JLabel text = new JLabel("No conversations yet");
            text.setFont(new Font("Segoe UI", Font.BOLD, 18));
            text.setForeground(TEXT_GRAY);
            text.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyCard.add(text);

            JLabel subtext =
                new JLabel("Start chatting with alumni from the dashboard");
            subtext.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            subtext.setForeground(TEXT_GRAY);
            subtext.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyCard.add(Box.createVerticalStrut(10));
            emptyCard.add(subtext);

            messagesContent.add(emptyCard);
        } else {
            for (int otherUserId : conversations) {
                String otherUsername = AlumniDAO.getFullNameById(otherUserId);
                JPanel convCard = createConversationCard(otherUserId, otherUsername);
                messagesContent.add(convCard);
                messagesContent.add(Box.createVerticalStrut(15));
            }
        }

        messagesContent.revalidate();
        messagesContent.repaint();
    }

    private JPanel createConversationCard(int otherUserId, String otherUsername) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(15, 0));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel avatar = new JLabel(new AvatarIcon(otherUsername, 50, PRIMARY_BLUE));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(28, 33, 52));

        JLabel nameLabel = new JLabel(otherUsername);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel previewLabel = new JLabel("Click to open conversation");
        previewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        previewLabel.setForeground(TEXT_GRAY);
        previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(previewLabel);

        JButton openBtn = createModernButton("Open", PRIMARY_BLUE);
        openBtn.setPreferredSize(new Dimension(100, 40));

        card.add(avatar, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(openBtn, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openConversation(otherUserId, otherUsername);
            }
        });

        openBtn.addActionListener(
            e -> openConversation(otherUserId, otherUsername));

        return card;
    }

    private void openConversation(int otherUserId, String otherUsername) {
        ChatConversationFrame chat = new ChatConversationFrame(
            currentUserId, currentUsername, otherUserId, otherUsername);
        chat.setVisible(true);
    }

    // ==========================================
    // NOTIFICATIONS VIEW
    // ==========================================
    private JPanel createNotificationsView() {
        JPanel view = new JPanel(new BorderLayout());
        view.setBackground(MAIN_BG);

        // Header with Post button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(18, 22, 36));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("🔔 Notice Board");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        // Only admin can post notices
        if (currentUserIsAdmin) {
            JPanel adminBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            adminBtns.setOpaque(false);

            // Pending donations badge
            int pendingDonations = AlumniDAO.getPendingDonationCount();
            if (pendingDonations > 0) {
                JButton donBadge =
                    createModernButton("💰 " + pendingDonations + " Pending Donation" + (pendingDonations > 1 ? "s" : ""),
                        new Color(245, 158, 11));
                donBadge.addActionListener(e -> {
                    AdminControlPanel acp = new AdminControlPanel(this);
                    acp.setVisible(true);
                });
                adminBtns.add(donBadge);
            }

            JButton postBtn =
                createModernButton("+ Post Notice", new Color(46, 204, 113));
            postBtn.addActionListener(e -> showPostNotificationDialog());
            adminBtns.add(postBtn);
            headerPanel.add(adminBtns, BorderLayout.EAST);
        }
        view.add(headerPanel, BorderLayout.NORTH);

        // Notifications list
        JPanel notificationsPanel = new JPanel();
        notificationsPanel.setLayout(
            new BoxLayout(notificationsPanel, BoxLayout.Y_AXIS));
        notificationsPanel.setBackground(MAIN_BG);
        notificationsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Load notifications
        loadNotifications(notificationsPanel);

        JScrollPane scrollPane = new JScrollPane(notificationsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        view.add(scrollPane, BorderLayout.CENTER);

        return view;
    }

    private void loadNotifications(JPanel container) {
        container.removeAll();

        List<Notification> notifications =
            AlumniDAO.getAllNotifications(currentUserId);

        if (notifications.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setBackground(MAIN_BG);
            emptyPanel.setBorder(new EmptyBorder(50, 0, 50, 0));

            JLabel emptyIcon = new JLabel("📭");
            emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptyLabel = new JLabel("No notices yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            emptyLabel.setForeground(TEXT_GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel tipLabel = new JLabel("Click 'Post Notice' to create one");
            tipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            tipLabel.setForeground(TEXT_GRAY);
            tipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyPanel.add(emptyIcon);
            emptyPanel.add(Box.createVerticalStrut(15));
            emptyPanel.add(emptyLabel);
            emptyPanel.add(Box.createVerticalStrut(5));
            emptyPanel.add(tipLabel);

            container.add(emptyPanel);
        } else {
            for (Notification notif : notifications) {
                container.add(createNotificationCard(notif));
                container.add(Box.createVerticalStrut(15));
            }
        }

        container.revalidate();
        container.repaint();
    }

    private JPanel createNotificationCard(Notification notif) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(notif.isViewed() ? Color.WHITE
                                            : new Color(240, 248, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                notif.isViewed() ? CARD_BG : new Color(220, 235, 255), 2, true),
            new EmptyBorder(20, 20, 20, 20)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));

        // Left: Icon
        JPanel iconPanel = new JPanel();
        iconPanel.setOpaque(false);
        JLabel iconLabel = new JLabel(notif.getTypeIcon());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconPanel.add(iconLabel);
        card.add(iconPanel, BorderLayout.WEST);

        // Center: Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Title with priority badge
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(notif.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titlePanel.add(titleLabel);

        if (!notif.getPriorityBadge().isEmpty()) {
            JLabel priorityLabel = new JLabel(" " + notif.getPriorityBadge());
            priorityLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            priorityLabel.setForeground(Color.RED);
            titlePanel.add(priorityLabel);
        }

        contentPanel.add(titlePanel);
        contentPanel.add(Box.createVerticalStrut(8));

        // Posted by
        JLabel postedByLabel =
            new JLabel("Posted by: " + notif.getPostedByName() + (notif.getPosterCompany() != null ? " (" + notif.getPosterCompany() + ")" : ""));
        postedByLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        postedByLabel.setForeground(TEXT_GRAY);
        contentPanel.add(postedByLabel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Content
        JTextArea contentArea = new JTextArea(notif.getContent());
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setOpaque(false);
        contentArea.setRows(2);
        contentPanel.add(contentArea);
        contentPanel.add(Box.createVerticalStrut(10));

        // Job posting details
        if (notif.isJobPosting()) {
            JPanel jobDetailsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
            jobDetailsPanel.setOpaque(false);

            if (notif.getCompanyName() != null) {
                addDetailLabel(jobDetailsPanel, "🏢 Company:", notif.getCompanyName());
            }
            if (notif.getJobPosition() != null) {
                addDetailLabel(jobDetailsPanel, "💼 Position:", notif.getJobPosition());
            }
            if (notif.getJobLocation() != null) {
                addDetailLabel(jobDetailsPanel, "📍 Location:", notif.getJobLocation());
            }
            if (notif.getJobType() != null) {
                addDetailLabel(jobDetailsPanel, "⏰ Type:", notif.getJobType());
            }
            if (notif.getSalaryRange() != null) {
                addDetailLabel(jobDetailsPanel, "💰 Salary:", notif.getSalaryRange());
            }
            if (notif.getApplicationDeadline() != null) {
                addDetailLabel(jobDetailsPanel,
                    "📅 Deadline:", notif.getApplicationDeadline());
            }

            contentPanel.add(jobDetailsPanel);
            contentPanel.add(Box.createVerticalStrut(10));

            if (notif.getApplicationUrl() != null && !notif.getApplicationUrl().isEmpty()) {
                JButton applyBtn = createModernButton("Apply Now →", PRIMARY_BLUE);
                applyBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                applyBtn.addActionListener(e -> {
                    try {
                        Desktop.getDesktop().browse(
                            new java.net.URI(notif.getApplicationUrl()));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(card, "Application URL: " + notif.getApplicationUrl());
                    }
                });
                contentPanel.add(applyBtn);
            }
        }

        // Event details
        if (notif.isEvent()) {
            JPanel eventDetailsPanel = new JPanel();
            eventDetailsPanel.setLayout(
                new BoxLayout(eventDetailsPanel, BoxLayout.Y_AXIS));
            eventDetailsPanel.setOpaque(false);

            if (notif.getEventDate() != null) {
                JLabel dateLabel =
                    new JLabel("📅 Date: " + notif.getEventDate().toString());
                dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                eventDetailsPanel.add(dateLabel);
            }
            if (notif.getEventLocation() != null) {
                JLabel locLabel = new JLabel("📍 Location: " + notif.getEventLocation());
                locLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                eventDetailsPanel.add(locLabel);
            }

            contentPanel.add(eventDetailsPanel);

            // ── Donation section ─────────────────────────────────────────────────
            if (notif.isDonationsEnabled()) {
                contentPanel.add(Box.createVerticalStrut(12));
                contentPanel.add(buildDonationPanel(card, notif));
            }
        }

        // Timestamp
        contentPanel.add(Box.createVerticalStrut(10));
        JLabel timeLabel = new JLabel(getTimeAgo(notif.getCreatedAt()));
        timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        timeLabel.setForeground(TEXT_GRAY);
        contentPanel.add(timeLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        // Mark as viewed when clicked
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!notif.isViewed()) {
                    AlumniDAO.markNotificationAsViewed(notif.getNotificationId(),
                        currentUserId);
                    card.setBackground(new Color(22, 22, 35));
                }
            }
        });

        return card;
    }

    private void addDetailLabel(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        panel.add(lbl);
        panel.add(val);
    }

    // ── DONATION PANEL (shown inside event notification cards) ───────────────
    private JPanel buildDonationPanel(JPanel card, Notification notif) {
        JPanel donPanel = new JPanel();
        donPanel.setLayout(new BoxLayout(donPanel, BoxLayout.Y_AXIS));
        donPanel.setBackground(new Color(240, 253, 244)); // light green tint
        donPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(134, 239, 172), 2, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        donPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        donPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Title row
        JLabel donTitle = new JLabel("💰  Event Donation Fund");
        donTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        donTitle.setForeground(new Color(21, 128, 61));
        donTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        donPanel.add(donTitle);
        donPanel.add(Box.createVerticalStrut(8));

        // Progress info
        double goal = notif.getDonationGoal();
        double raised = notif.getDonationRaised();

        if (goal > 0) {
            int pct = (int) Math.min(100, raised / goal * 100);
            JLabel amtLbl = new JLabel(String.format(
                "৳%.0f raised of ৳%.0f goal  (%d%%)", raised, goal, pct));
            amtLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            amtLbl.setForeground(new Color(21, 128, 61));
            amtLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            donPanel.add(amtLbl);
            donPanel.add(Box.createVerticalStrut(6));

            JProgressBar progBar = new JProgressBar(0, 100);
            progBar.setValue(pct);
            progBar.setStringPainted(false);
            progBar.setForeground(new Color(34, 197, 94));
            progBar.setBackground(new Color(220, 252, 231));
            progBar.setPreferredSize(new Dimension(0, 10));
            progBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
            progBar.setAlignmentX(Component.LEFT_ALIGNMENT);
            progBar.setBorder(BorderFactory.createEmptyBorder());
            donPanel.add(progBar);
        } else {
            JLabel amtLbl = new JLabel(String.format("৳%.0f raised so far", raised));
            amtLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            amtLbl.setForeground(new Color(21, 128, 61));
            amtLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            donPanel.add(amtLbl);
        }

        donPanel.add(Box.createVerticalStrut(10));

        // Donate button
        boolean alreadyDonated =
            AlumniDAO.hasUserDonated(notif.getNotificationId(), currentUserId);

        JButton donateBtn = createModernButton(
            alreadyDonated ? "✓ You Donated" : "💚 Donate Now",
            alreadyDonated ? new Color(107, 114, 128) : new Color(16, 185, 129));
        donateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        donateBtn.setEnabled(!alreadyDonated);
        donateBtn.addActionListener(e -> {
            DonationDialog dlg = new DonationDialog(
                (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(
                    card),
                notif.getNotificationId(), currentUserId, notif.getTitle(),
                notif.getDonationGoal(), notif.getDonationRaised());
            dlg.setVisible(true);
            if (dlg.isSubmitted()) {
                // Refresh notifications view so amounts update
                switchView("NOTIFICATIONS");
            }
        });
        donPanel.add(donateBtn);
        return donPanel;
    }

    private void showPostNotificationDialog() {
        if (!currentUserIsAdmin) {
            JOptionPane.showMessageDialog(
                this, "Only administrators can post notices.", "Access Denied",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        PostNotificationDialog dialog =
            new PostNotificationDialog(this, currentUserId);
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            // Refresh notifications view
            switchView("NOTIFICATIONS");
        }
    }

    private JPanel createAchievementsView() {
        JPanel view = new JPanel(new BorderLayout());
        view.setBackground(MAIN_BG);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(18, 22, 36));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("🏆 Alumni Achievements");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titlePanel.add(titleLabel);

        JButton refreshBtn = createModernButton("↻ Refresh", PRIMARY_BLUE);
        refreshBtn.addActionListener(e -> switchView("ACHIEVEMENTS"));

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        view.add(headerPanel, BorderLayout.NORTH);

        // Achievements content
        JPanel achievementsPanel = new JPanel();
        achievementsPanel.setLayout(
            new BoxLayout(achievementsPanel, BoxLayout.Y_AXIS));
        achievementsPanel.setBackground(MAIN_BG);
        achievementsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Load achievements
        loadAchievements(achievementsPanel);

        JScrollPane scrollPane = new JScrollPane(achievementsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        view.add(scrollPane, BorderLayout.CENTER);

        return view;
    }

    private void loadAchievements(JPanel container) {
        container.removeAll();

        ArrayList<Achievement> achievements = DatabaseConnection.getAchievements();

        if (achievements.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setBackground(MAIN_BG);
            emptyPanel.setBorder(new EmptyBorder(50, 0, 50, 0));

            JLabel emptyIcon = new JLabel("🏆");
            emptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptyLabel = new JLabel("No achievements yet");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            emptyLabel.setForeground(TEXT_GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel tipLabel = new JLabel("Contact admin to add achievements");
            tipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            tipLabel.setForeground(TEXT_GRAY);
            tipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyPanel.add(emptyIcon);
            emptyPanel.add(Box.createVerticalStrut(15));
            emptyPanel.add(emptyLabel);
            emptyPanel.add(Box.createVerticalStrut(5));
            emptyPanel.add(tipLabel);

            container.add(emptyPanel);
        } else {
            for (Achievement achievement : achievements) {
                container.add(createAchievementCard(achievement));
                container.add(Box.createVerticalStrut(15));
            }
        }

        container.revalidate();
        container.repaint();
    }

    private JPanel createAchievementCard(Achievement achievement) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(new Color(22, 22, 35));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        // Left: Photo
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBackground(new Color(22, 22, 35));
        photoPanel.setPreferredSize(new Dimension(250, 200));

        try {
            java.io.File photoFile = new java.io.File(achievement.getPhotoPath());
            if (photoFile.exists()) {
                ImageIcon imageIcon = new ImageIcon(achievement.getPhotoPath());
                Image image = imageIcon.getImage().getScaledInstance(
                    240, 190, Image.SCALE_SMOOTH);
                JLabel photoLabel = new JLabel(new ImageIcon(image));
                photoLabel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
                photoPanel.add(photoLabel, BorderLayout.CENTER);
            } else {
                JLabel noPhotoLabel = new JLabel("No Photo", SwingConstants.CENTER);
                noPhotoLabel.setForeground(TEXT_GRAY);
                photoPanel.add(noPhotoLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            photoPanel.add(errorLabel, BorderLayout.CENTER);
        }

        // Right: Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(22, 22, 35));

        JLabel titleLabel = new JLabel(achievement.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea(achievement.getDescription());
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descArea.setForeground(TEXT_GRAY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dateLabel = new JLabel("📅 " + achievement.getUploadDate());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateLabel.setForeground(TEXT_GRAY);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(descArea);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(dateLabel);

        card.add(photoPanel, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private String getTimeAgo(Timestamp timestamp) {
        if (timestamp == null)
            return "";

        long now = System.currentTimeMillis();
        long time = timestamp.getTime();
        long diff = now - time;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 7) {
            return new java.text.SimpleDateFormat("MMM dd, yyyy").format(timestamp);
        } else if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }

    // ==========================================
    // PLACEHOLDER VIEW
    // ==========================================
    private JPanel createPlaceholderView(String icon, String title,
        String description) {
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setBackground(MAIN_BG);
        view.setBorder(new EmptyBorder(100, 0, 100, 0));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descLabel.setForeground(TEXT_GRAY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        view.add(iconLabel);
        view.add(Box.createVerticalStrut(20));
        view.add(titleLabel);
        view.add(Box.createVerticalStrut(10));
        view.add(descLabel);

        return view;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(30, 35, 15, 35));

        // Get real statistics from database
        int totalAlumni = AlumniDAO.getTotalProfileCount();
        int totalStudents = AlumniDAO.getStudentCount();
        int totalProfessionals = totalAlumni - totalStudents;

        panel.add(createStatCard("👥", "Total Alumni", String.valueOf(totalAlumni),
            PRIMARY_BLUE));
        panel.add(createStatCard("🎓", "Students", String.valueOf(totalStudents),
            SUCCESS_GREEN));
        panel.add(createStatCard("💼", "Professionals",
            String.valueOf(totalProfessionals),
            ACCENT_PURPLE));

        return panel;
    }

    private JPanel createStatCard(String icon, String label, String value,
        Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setBackground(new Color(22, 22, 35));
        card.setPreferredSize(new Dimension(250, 100));
        card.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(accentColor.getRed(),
            accentColor.getGreen(),
            accentColor.getBlue(), 30));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(60, 60));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setBackground(new Color(22, 22, 35));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(TEXT_DARK);

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        labelText.setForeground(TEXT_GRAY);

        textPanel.add(valueLabel);
        textPanel.add(labelText);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // ==========================================
    // DATA LOADING - NOW USES REAL DATABASE DATA
    // ==========================================
    private void loadDashboardData(String searchTerm) {
        contentPanel.removeAll();

        // Section Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_BG);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel sectionTitle = new JLabel(
            searchTerm.isEmpty() ? "All Alumni"
                                 : "Search Results for: \"" + searchTerm + "\"");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sectionTitle.setForeground(TEXT_DARK);

        headerPanel.add(sectionTitle, BorderLayout.WEST);
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Get profiles from database - NO MORE MOCK DATA
        List<AlumniProfile> profiles;
        if (searchTerm.isEmpty()) {
            profiles = AlumniDAO.getAllProfiles();
        } else {
            profiles = AlumniDAO.searchAlumni(searchTerm);
        }

        // Check if database is empty
        if (profiles == null || profiles.isEmpty()) {
            contentPanel.add(createEmptyDatabaseState());
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        // Grid Layout for Cards
        JPanel grid = new JPanel(new GridLayout(0, 2, 25, 25));
        grid.setBackground(MAIN_BG);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int displayCount = 0;

        for (AlumniProfile p : profiles) {
            // Skip current user
            if (p.getUserId() == currentUserId)
                continue;

            boolean isStudent = (p.getJobRole() != null && p.getJobRole().equalsIgnoreCase("Student")) || p.getGraduationYear() >= currentYear;

            grid.add(createModernProfileCard(p, isStudent));
            displayCount++;
        }

        if (displayCount == 0) {
            contentPanel.add(createEmptyState());
        } else {
            contentPanel.add(grid);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createEmptyDatabaseState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(100, 0, 100, 0));

        JLabel icon = new JLabel("👥");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel text = new JLabel("No alumni profiles yet");
        text.setFont(new Font("Segoe UI", Font.BOLD, 20));
        text.setForeground(TEXT_GRAY);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtext = new JLabel(
            "Be the first to create a profile or invite others to register!");
        subtext.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtext.setForeground(TEXT_GRAY);
        subtext.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(20));
        panel.add(text);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtext);

        return panel;
    }

    // ==========================================
    // MODERN PROFILE CARD
    // ==========================================
    private JPanel createModernProfileCard(AlumniProfile p, boolean isStudent) {
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow effect
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

                // Card background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 20, 20);

                g2.dispose();
            }
        };
        card.setBackground(new Color(28, 33, 52));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setOpaque(false);

        // Main Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(28, 33, 52));

        // Header:   Avatar + Info
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(new Color(28, 33, 52));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Avatar — use real photo if available, otherwise initials
        Color avatarColor = isStudent ? STUDENT_TEXT : ALUMNI_TEXT;
        JLabel avatar;
        String cardPic = p.getProfilePicturePath();
        if (cardPic != null && !cardPic.isEmpty() && new java.io.File(cardPic).exists()) {
            avatar = new JLabel(new CircularImageIcon(cardPic, 60));
        } else {
            avatar = new JLabel(new AvatarIcon(p.getFullName(), 60, avatarColor));
        }

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(new Color(28, 33, 52));

        JLabel name = new JLabel(p.getFullName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));
        name.setForeground(TEXT_DARK);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        String subtitle = isStudent
            ? "🎓 Class of " + p.getGraduationYear()
            : "💼 " + p.getJobRole() + " at " + p.getCompany();
        JLabel role = new JLabel(subtitle);
        role.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        role.setForeground(TEXT_GRAY);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(name);
        info.add(Box.createVerticalStrut(6));
        info.add(role);

        header.add(avatar, BorderLayout.WEST);
        header.add(info, BorderLayout.CENTER);

        // Badge
        JLabel badge = createBadge(isStudent ? "STUDENT" : "ALUMNI", isStudent);
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgePanel.setBackground(new Color(28, 33, 52));
        badgePanel.add(badge);
        header.add(badgePanel, BorderLayout.EAST);

        content.add(header);
        content.add(Box.createVerticalStrut(20));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        content.add(sep);
        content.add(Box.createVerticalStrut(15));

        // Skills Section
        JLabel skillsLabel = new JLabel("Skills & Expertise");
        skillsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        skillsLabel.setForeground(TEXT_GRAY);
        skillsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(skillsLabel);
        content.add(Box.createVerticalStrut(10));

        JPanel skillsPanel = createSkillsPanel(
            p.getSkills() != null ? p.getSkills() : "No skills listed");
        skillsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(skillsPanel);
        content.add(Box.createVerticalStrut(20));

        // Action Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(new Color(28, 33, 52));
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton viewBtn = createModernButton("View Profile", Color.WHITE);
        viewBtn.setForeground(PRIMARY_BLUE);
        viewBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_BLUE, 2, true),
            new EmptyBorder(10, 20, 10, 20)));
        viewBtn.addActionListener(e -> showModernDetails(p));

        JButton msgBtn = createModernButton("💬 Message", PRIMARY_BLUE);
        msgBtn.addActionListener(e -> showMessageDialog(p));

        actions.add(viewBtn);
        actions.add(msgBtn);

        content.add(actions);

        card.add(content, BorderLayout.CENTER);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });

        return card;
    }

    private JLabel createBadge(String text, boolean isStudent) {
        JLabel badge = new JLabel(" " + text + " ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBackground(isStudent ? STUDENT_BG : ALUMNI_BG);
        badge.setForeground(isStudent ? STUDENT_TEXT : ALUMNI_TEXT);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 16));
        badge.setBorder(new EmptyBorder(5, 12, 5, 12));
        return badge;
    }

    private JPanel createSkillsPanel(String skillsText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(new Color(28, 33, 52));

        if (skillsText == null || skillsText.trim().isEmpty() || skillsText.equals("Not specified") || skillsText.contains("Please update")) {
            JLabel noSkills = new JLabel("No skills listed yet");
            noSkills.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            noSkills.setForeground(TEXT_GRAY);
            panel.add(noSkills);
            return panel;
        }

        String[] skills = skillsText.split(",");
        for (String skill : skills) {
            if (skills.length > 5 && panel.getComponentCount() >= 5) {
                JLabel more = new JLabel("+" + (skills.length - 5) + " more");
                more.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                more.setForeground(TEXT_GRAY);
                panel.add(more);
                break;
            }
            panel.add(createSkillTag(skill.trim()));
        }

        return panel;
    }

    private JLabel createSkillTag(String skill) {
        JLabel tag = new JLabel(skill) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MAIN_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tag.setOpaque(false);
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tag.setForeground(TEXT_DARK);
        tag.setBorder(new EmptyBorder(6, 12, 6, 12));
        return tag;
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================
    private JButton createModernButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor == Color.WHITE ? MAIN_BG : PRIMARY_HOVER);
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(bgColor == Color.WHITE ? TEXT_DARK : Color.WHITE);
        btn.setBorder(new EmptyBorder(10, 25, 10, 25));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private JButton createIconButton(String icon, String tooltip) {
        JButton btn = new JButton(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(MAIN_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        btn.setPreferredSize(new Dimension(50, 50));
        btn.setToolTipText(tooltip);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private JScrollPane createStyledScrollPane(JPanel content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(MAIN_BG);

        // Custom scrollbar
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(203, 213, 224);
                this.trackColor = MAIN_BG;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });

        return scrollPane;
    }

    private void setPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(TEXT_GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_GRAY);
                }
            }
        });
    }

    private JPanel createEmptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(MAIN_BG);
        panel.setBorder(new EmptyBorder(100, 0, 100, 0));

        JLabel icon = new JLabel("🔍");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel text = new JLabel("No profiles found");
        text.setFont(new Font("Segoe UI", Font.BOLD, 20));
        text.setForeground(TEXT_GRAY);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtext = new JLabel("Try adjusting your search criteria");
        subtext.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtext.setForeground(TEXT_GRAY);
        subtext.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(20));
        panel.add(text);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtext);

        return panel;
    }

    private void showModernDetails(AlumniProfile p) {
        JDialog dialog = new JDialog(this, p.getFullName(), true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(22, 22, 35));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Avatar — use real photo if available, otherwise initials
        String detailPic = p.getProfilePicturePath();
        JLabel avatar;
        if (detailPic != null && !detailPic.isEmpty() && new java.io.File(detailPic).exists()) {
            avatar = new JLabel(new CircularImageIcon(detailPic, 100));
        } else {
            avatar = new JLabel(new AvatarIcon(p.getFullName(), 100, PRIMARY_BLUE));
        }
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(avatar);
        content.add(Box.createVerticalStrut(20));

        // Name
        JLabel name = new JLabel(p.getFullName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 26));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(name);
        content.add(Box.createVerticalStrut(10));

        // Details
        addDetailRow(content, "📧 Email", p.getEmail());
        addDetailRow(content, "💼 Company",
            p.getCompany() != null ? p.getCompany() : "Not specified");
        addDetailRow(content, "👔 Role",
            p.getJobRole() != null ? p.getJobRole() : "Not specified");
        addDetailRow(content, "🎓 Graduation",
            String.valueOf(p.getGraduationYear()));
        addDetailRow(content, "💡 Skills",
            p.getSkills() != null ? p.getSkills() : "Not specified");

        content.add(Box.createVerticalStrut(15));

        // Bio
        JLabel bioLabel = new JLabel("📝 Bio");
        bioLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(bioLabel);
        content.add(Box.createVerticalStrut(8));

        JTextArea bio =
            new JTextArea(p.getBio() != null ? p.getBio() : "No bio provided");
        bio.setLineWrap(true);
        bio.setWrapStyleWord(true);
        bio.setEditable(false);
        bio.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        bio.setBackground(MAIN_BG);
        bio.setBorder(new EmptyBorder(15, 15, 15, 15));
        content.add(bio);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        dialog.add(scrollPane);

        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(new Color(22, 22, 35));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelComp.setPreferredSize(new Dimension(150, 20));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        valueComp.setForeground(TEXT_GRAY);

        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.CENTER);

        parent.add(row);
    }

    private void showMessageDialog(AlumniProfile p) {
        // Open chat conversation window
        ChatConversationFrame chat = new ChatConversationFrame(
            currentUserId, currentUsername, p.getUserId(), p.getFullName());
        chat.setVisible(true);
    }

    // ==========================================
    // CIRCULAR IMAGE ICON
    // ==========================================
    private static class CircularImageIcon implements Icon {
        private final BufferedImage circularImage;
        private final int size;

        public CircularImageIcon(String imagePath, int size) {
            this.size = size;
            BufferedImage result = null;
            try {
                // Load fully via ImageIO (synchronous, no async issue)
                BufferedImage raw = ImageIO.read(new java.io.File(imagePath));
                if (raw != null) {
                    // Scale to exact size using Graphics2D
                    BufferedImage scaled =
                        new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D sg = scaled.createGraphics();
                    sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    sg.drawImage(raw, 0, 0, size, size, null);
                    sg.dispose();
                    // Clip to circle in a new ARGB image
                    BufferedImage circular =
                        new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D cg = circular.createGraphics();
                    cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    cg.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
                    cg.drawImage(scaled, 0, 0, null);
                    cg.setClip(null);
                    // Draw border ring
                    cg.setColor(new Color(66, 153, 225));
                    cg.setStroke(new BasicStroke(3f));
                    cg.drawOval(1, 1, size - 2, size - 2);
                    cg.dispose();
                    result = circular;
                }
            } catch (IOException e) {
                System.err.println("CircularImageIcon: could not load " + imagePath + " : " + e.getMessage());
            }
            this.circularImage = result;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (circularImage != null) {
                g.drawImage(circularImage, x, y, null);
            } else {
                // Fallback: grey circle if image failed
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 210, 220));
                g2.fillOval(x, y, size, size);
                g2.setColor(new Color(66, 153, 225));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(x + 1, y + 1, size - 2, size - 2);
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }
        @Override
        public int getIconHeight() {
            return size;
        }
    }

    // ==========================================
    // AVATAR ICON
    // ==========================================
    private static class AvatarIcon implements Icon {
        private final String initials;
        private final int size;
        private final Color color;

        public AvatarIcon(String name, int size, Color baseColor) {
            this.size = size;
            this.initials = getInitials(name);
            this.color = baseColor;
        }

        private String getInitials(String name) {
            if (name == null || name.isEmpty())
                return "?";
            String[] parts = name.split("\\s+");
            if (parts.length >= 2) {
                return (parts[0].substring(0, 1) + parts[1].substring(0, 1))
                    .toUpperCase();
            }
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Gradient background
            GradientPaint gradient =
                new GradientPaint(x, y, color, x + size, y + size, color.brighter());
            g2.setPaint(gradient);
            g2.fillOval(x, y, size, size);

            // Initials
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
            FontMetrics fm = g2.getFontMetrics();
            int textX = x + (size - fm.stringWidth(initials)) / 2;
            int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(initials, textX, textY);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    // ==========================================
    // MAIN METHOD - FOR TESTING ONLY
    // ==========================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            // Show login frame instead of directly opening dashboard
            new LoginFrame().setVisible(true);
        });
    }
}