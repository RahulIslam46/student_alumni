import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class ModernDashboardUI extends JFrame {
    // ── Premium Dark Palette ──
    static final Color BG_DEEP = new Color(5, 8, 18);
    static final Color BG_MID = new Color(9, 12, 28);
    static final Color CARD = new Color(14, 19, 40);
    static final Color CARD_HOV = new Color(20, 27, 54);
    static final Color BORDER = new Color(45, 58, 100, 100);
    static final Color ACCENT_T = new Color(20, 210, 190);
    static final Color ACCENT_I = new Color(99, 102, 241);
    static final Color ACCENT_B = new Color(56, 189, 248);
    static final Color ACCENT_P = new Color(168, 85, 247);
    static final Color ACCENT_G = new Color(52, 211, 153);
    static final Color ACCENT_R = new Color(248, 113, 113);
    static final Color ACCENT_O = new Color(251, 191, 36);
    static final Color TXT_H = new Color(237, 242, 255);
    static final Color TXT_S = new Color(148, 163, 200);
    static final Color TXT_M = new Color(90, 108, 155);

    // ── Fonts ──
    static final Font F_LOGO = new Font("Segoe UI", Font.BOLD, 18);
    static final Font F_H1 = new Font("Segoe UI", Font.BOLD, 28);
    static final Font F_H2 = new Font("Segoe UI", Font.BOLD, 18);
    static final Font F_H3 = new Font("Segoe UI", Font.BOLD, 14);
    static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private String username = "Md. Rahul Islam";
    private int currentUserId = -1;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;
    private final Map<String, JButton> sidebarButtons = new LinkedHashMap<>();
    private String dashboardSearchQuery = "";
    private JTextField dashboardSearchField;
    private String activeView = "DASHBOARD";
    private boolean isAdminUser = false;
    private static final String SEARCH_PLACEHOLDER = "Search alumni by name, company, skill...";

    public ModernDashboardUI() { this("Md. Rahul Islam"); }

    public ModernDashboardUI(String username) {
        this.username = username;
        try {
            currentUserId = AlumniDAO.getUserId(this.username);
            AlumniDAO.ensureProfilePictureColumn();
            isAdminUser = currentUserId > 0 && AlumniDAO.isAdminUser(currentUserId);
        } catch (Exception ignored) {
            currentUserId = -1;
        }

        setTitle("Alumni Connect — Dashboard");
        setSize(1400, 850);
        setMinimumSize(new Dimension(1100, 680));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_DEEP);
                g2.fillRect(0, 0, getWidth(), getHeight());
                drawAurora(g2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        JPanel right = new JPanel(new BorderLayout(0, 0));
        right.setOpaque(false);
        right.add(buildTopBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);
        contentArea.add(buildDashboardView(), "DASHBOARD");
        contentArea.add(buildProfileView(), "PROFILE");
        contentArea.add(buildMessagesView(), "MESSAGES");
        contentArea.add(buildNotifView(), "NOTIFICATIONS");
        contentArea.add(buildAchievementsView(), "ACHIEVEMENTS");
        contentArea.add(buildAnalyticsView(), "ANALYTICS");

        right.add(contentArea, BorderLayout.CENTER);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        refreshSidebarSelection();
    }

    public String getCurrentUsername() { return username; }

    private void showView(String view) {
        if (cardLayout == null || contentArea == null) return;
        activeView = view;
        contentArea.removeAll();
        contentArea.add(buildDashboardView(), "DASHBOARD");
        contentArea.add(buildProfileView(), "PROFILE");
        contentArea.add(buildMessagesView(), "MESSAGES");
        contentArea.add(buildNotifView(), "NOTIFICATIONS");
        contentArea.add(buildAchievementsView(), "ACHIEVEMENTS");
        contentArea.add(buildAnalyticsView(), "ANALYTICS");
        cardLayout.show(contentArea, view);
        contentArea.revalidate();
        contentArea.repaint();
        refreshSidebarSelection();
    }

    private void applyDashboardSearch() {
        if (dashboardSearchField == null) return;
        String query = dashboardSearchField.getText();
        if (query == null) query = "";
        query = query.trim();
        if (SEARCH_PLACEHOLDER.equals(query)) query = "";
        dashboardSearchQuery = query;
        showView("DASHBOARD");
    }

    private JPanel buildSidebar() {
        sidebarPanel = new JPanel(new BorderLayout(0, 18)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(8, 11, 24), 0, getHeight(), new Color(11, 16, 34));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        sidebarPanel.setOpaque(false);
        sidebarPanel.setPreferredSize(new Dimension(290, 0));
        sidebarPanel.setBorder(new EmptyBorder(24, 20, 24, 20));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        JLabel logo = new JLabel(new AvatarIcon("Alumni Connect", 48, ACCENT_I));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.add(lbl("Alumni Connect", F_H2, TXT_H));
        brandText.add(vgap(2));
        brandText.add(lbl("Modern alumni dashboard", F_SMALL, TXT_S));
        brand.add(logo, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);
        body.add(brand);

        body.add(vgap(20));

        JPanel profileCard = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16, 22, 44));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(70, 85, 130, 90));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        profileCard.setOpaque(false);
        profileCard.setBorder(new EmptyBorder(18, 16, 18, 16));
        profileCard.setAlignmentX(LEFT_ALIGNMENT);
        profileCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        profileCard.add(new JLabel(new AvatarIcon(firstName(username), 48, ACCENT_T)), BorderLayout.WEST);

        JPanel profileText = new JPanel();
        profileText.setOpaque(false);
        profileText.setLayout(new BoxLayout(profileText, BoxLayout.Y_AXIS));
        profileText.add(lbl(safe(username, "Member"), new Font("Segoe UI", Font.BOLD, 15), TXT_H));
        profileText.add(vgap(3));
        profileText.add(lbl(isAdminUser ? "Administrator access" : "Member account", F_SMALL, TXT_S));
        profileCard.add(profileText, BorderLayout.CENTER);
        body.add(profileCard);

        body.add(vgap(24));
        body.add(lbl("Navigation", F_SMALL, TXT_M));
        body.add(vgap(10));

        body.add(sidebarButton("🏠  Dashboard", "DASHBOARD", ACCENT_I));
        body.add(vgap(8));
        body.add(sidebarButton("👤  My Profile", "PROFILE", ACCENT_T));
        body.add(vgap(8));
        body.add(sidebarButton("💬  Messages", "MESSAGES", ACCENT_B));
        body.add(vgap(8));
        body.add(sidebarButton("🔔  Notifications", "NOTIFICATIONS", ACCENT_O));
        body.add(vgap(8));
        body.add(sidebarButton("🏆  Achievements", "ACHIEVEMENTS", ACCENT_G));
        body.add(vgap(8));
        body.add(sidebarButton("⚙️  Admin Panel", "ADMIN", ACCENT_P));

        sidebarPanel.add(body, BorderLayout.NORTH);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(vgap(12));
        JButton logoutBtn = sidebarActionButton("⎋  Logout", ACCENT_R);
        logoutBtn.addActionListener(e -> logoutToLogin());
        bottom.add(logoutBtn);
        sidebarPanel.add(bottom, BorderLayout.SOUTH);
        return sidebarPanel;
    }

    private JButton sidebarButton(String label, String view, Color accent) {
        JButton button = new JButton(label) {
            boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean selected = view.equals(activeView);
                Color fill = selected ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 42)
                    : hovered         ? new Color(255, 255, 255, 16)
                                      : new Color(255, 255, 255, 8);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(selected ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120)
                                     : new Color(255, 255, 255, 24));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(F_H3);
        button.setForeground(TXT_H);
        button.setBorder(new EmptyBorder(14, 16, 14, 16));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        button.addActionListener(e -> {
            if ("ADMIN".equals(view)) {
                openAdminPanelFromDashboard();
            } else {
                showView(view);
            }
        });
        sidebarButtons.put(view, button);
        return button;
    }

    private JButton sidebarActionButton(String label, Color accent) {
        JButton button = new JButton(label) {
            boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 55)
                                    : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 110));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(F_H3);
        button.setForeground(Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(14, 16, 14, 16));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        return button;
    }

    private void refreshSidebarSelection() {
        if (sidebarButtons.isEmpty()) return;
        for (Map.Entry<String, JButton> entry : sidebarButtons.entrySet()) {
            entry.getValue().repaint();
        }
    }

    private void openAdminPanelFromDashboard() {
        if (!isAdminUser) {
            JOptionPane.showMessageDialog(this, "Admin access is required to open the admin panel.", "Admin Panel", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AdminControlPanel adminPanel = new AdminControlPanel(this);
        adminPanel.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                ModernDashboardUI.this.setVisible(true);
                ModernDashboardUI.this.toFront();
            }
        });
        setVisible(false);
        adminPanel.setVisible(true);
    }

    private void logoutToLogin() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Do you want to log out now?",
            "Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private void drawAurora(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RadialGradientPaint r1 = new RadialGradientPaint(new Point2D.Float(w * 0.78f, h * 0.06f), w * 0.40f,
            new float[] {0f, 1f}, new Color[] {new Color(20, 210, 190, 22), new Color(0, 0, 0, 0)});
        g2.setPaint(r1);
        g2.fillRect(0, 0, w, h);
        RadialGradientPaint r2 = new RadialGradientPaint(new Point2D.Float(w * 0.12f, h * 0.85f), w * 0.35f,
            new float[] {0f, 1f}, new Color[] {new Color(99, 102, 241, 20), new Color(0, 0, 0, 0)});
        g2.setPaint(r2);
        g2.fillRect(0, 0, w, h);
        RadialGradientPaint r3 = new RadialGradientPaint(new Point2D.Float(w * 0.50f, h * 0.45f), w * 0.22f,
            new float[] {0f, 1f}, new Color[] {new Color(168, 85, 247, 10), new Color(0, 0, 0, 0)});
        g2.setPaint(r3);
        g2.fillRect(0, 0, w, h);
    }

    // ═══════════════════════════════════════════════════════════
    //  TOP BAR
    // ═══════════════════════════════════════════════════════════
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(8, 11, 24, 230));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(45, 58, 100, 60));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 72));
        bar.setBorder(new EmptyBorder(14, 28, 14, 28));

        JPanel srch = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(16, 22, 44));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g2.dispose();
            }
        };
        srch.setOpaque(false);
        srch.setPreferredSize(new Dimension(480, 44));
        srch.setBorder(new EmptyBorder(0, 16, 0, 8));

        JLabel si = lbl("⌕", new Font("Segoe UI", Font.PLAIN, 18), TXT_M);
        dashboardSearchField = new JTextField();
        dashboardSearchField.setFont(F_BODY);
        dashboardSearchField.setForeground(TXT_H);
        dashboardSearchField.setBackground(new Color(0, 0, 0, 0));
        dashboardSearchField.setOpaque(false);
        dashboardSearchField.setBorder(null);
        dashboardSearchField.setCaretColor(ACCENT_T);
        if (dashboardSearchQuery.isEmpty())
            setPlaceholder(dashboardSearchField, SEARCH_PLACEHOLDER);
        else {
            dashboardSearchField.setText(dashboardSearchQuery);
            dashboardSearchField.setForeground(TXT_H);
        }

        JButton goBtn = glowBtn("Search", ACCENT_I);
        goBtn.setPreferredSize(new Dimension(86, 36));
        srch.add(si, BorderLayout.WEST);
        srch.add(dashboardSearchField, BorderLayout.CENTER);
        srch.add(goBtn, BorderLayout.EAST);
        bar.add(srch, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton postBtn = glowBtn("+ Post Notice", ACCENT_G);
        postBtn.addActionListener(e -> {
            if (currentUserId <= 0) {
                JOptionPane.showMessageDialog(this, "Unable to resolve your user profile for posting.", "Post Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            PostNotificationDialog dialog = new PostNotificationDialog(this, currentUserId);
            dialog.setVisible(true);
        });

        String dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        goBtn.addActionListener(e -> applyDashboardSearch());
        dashboardSearchField.addActionListener(e -> applyDashboardSearch());

        actions.add(postBtn);
        actions.add(lbl(dateText, F_SMALL, TXT_M));

        List<Component> removeList = new ArrayList<>();
        for (Component c : actions.getComponents()) {
            if (c instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c;
                String txt = b.getText();
                boolean emptyText = txt == null || txt.trim().isEmpty();
                if (emptyText && b.getIcon() == null && b.getPreferredSize().width <= 46) removeList.add(c);
            }
        }
        for (Component c : removeList) actions.remove(c);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════
    //  DASHBOARD VIEW
    // ═══════════════════════════════════════════════════════════
    private JPanel buildDashboardView() {
        List<AlumniProfile> profiles = new ArrayList<>();
        List<Notification> notifications = new ArrayList<>();
        List<Notification> filteredNotifications = new ArrayList<>();
        int totalAlumni = 0, totalStudents = 0, totalProfessionals = 0, unreadMessages = 0;
        boolean hasSearch = dashboardSearchQuery != null && !dashboardSearchQuery.trim().isEmpty();
        String q = hasSearch ? dashboardSearchQuery.trim().toLowerCase() : "";
        try {
            profiles = hasSearch ? AlumniDAO.searchAlumni(dashboardSearchQuery) : AlumniDAO.getAllProfiles();
            notifications = AlumniDAO.getAllNotifications(currentUserId);
            totalAlumni = AlumniDAO.getTotalProfileCount();
            totalStudents = AlumniDAO.getStudentCount();
            totalProfessionals = Math.max(0, totalAlumni - totalStudents);
            unreadMessages = AlumniDAO.getUnreadMessageCount(currentUserId);
        } catch (Exception ignored) {
        }

        if (hasSearch) {
            for (Notification n : notifications) {
                String title = safe(n.getTitle(), "").toLowerCase();
                String contentTxt = safe(n.getContent(), "").toLowerCase();
                String postedBy = safe(n.getPostedByName(), "").toLowerCase();
                if (title.contains(q) || contentTxt.contains(q) || postedBy.contains(q))
                    filteredNotifications.add(n);
            }
        } else {
            filteredNotifications = notifications;
        }

        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 30, 30, 30));

        // Title row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        JPanel titleLeft = new JPanel();
        titleLeft.setLayout(new BoxLayout(titleLeft, BoxLayout.Y_AXIS));
        titleLeft.setOpaque(false);
        titleLeft.add(lbl("Good Day, " + firstName(username) + " 👋", F_H1, TXT_H));
        titleLeft.add(vgap(4));
        titleLeft.add(lbl(hasSearch ? "Search results for: \"" + dashboardSearchQuery + "\"" : "Here's what's happening in your alumni network today.", F_BODY, TXT_S));
        titleRow.add(titleLeft, BorderLayout.WEST);
        content.add(titleRow);
        content.add(vgap(24));

        // Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 114));
        stats.add(statCard("👥", "Total Alumni", String.valueOf(totalAlumni), "From database", ACCENT_I));
        stats.add(statCard("🎓", "Students", String.valueOf(totalStudents), "Active students", ACCENT_B));
        stats.add(statCard("💼", "Professionals", String.valueOf(totalProfessionals), "Alumni members", ACCENT_T));
        stats.add(statCard("📨", "Unread Messages", String.valueOf(unreadMessages), "Current inbox", ACCENT_G));
        content.add(stats);
        content.add(vgap(24));

        // Two columns
        JPanel cols = new JPanel(new GridLayout(1, 2, 20, 0));
        cols.setOpaque(false);

        // Left col — Featured Alumni
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        JPanel alumHead = rowPanel();
        alumHead.add(lbl("Featured Alumni", F_H2, TXT_H));
        alumHead.add(Box.createHorizontalGlue());
        alumHead.add(glowBtn("View All →", ACCENT_I));
        alumHead.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        leftCol.add(alumHead);
        leftCol.add(vgap(12));

        int rank = 1;
        for (AlumniProfile p : profiles) {
            if (p.getUserId() == currentUserId) continue;
            String name = safe(p.getFullName(), "Unnamed");
            String roleComp = safe(p.getJobRole(), "Member") + " · " + safe(p.getCompany(), "Unknown");
            String skills = safe(p.getSkills(), "No skills listed");
            String badge = "Student".equalsIgnoreCase(safe(p.getJobRole(), "")) ? "STUDENT" : "ALUMNI";
            leftCol.add(alumCard(p.getUserId(), name, roleComp, skills, badge, "#" + rank));
            leftCol.add(vgap(10));
            if (++rank > 4) break;
        }
        if (rank == 1) leftCol.add(lbl("No alumni profiles available yet.", F_BODY, TXT_S));

        // Right col — Company chart + Activity
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        rightCol.add(lbl("Alumni by Company", F_H2, TXT_H));
        rightCol.add(vgap(10));
        rightCol.add(alumniCompanyChart(profiles));
        rightCol.add(vgap(22));

        rightCol.add(lbl("Live Activity Feed", F_H2, TXT_H));
        rightCol.add(vgap(10));
        int addedActivities = 0;
        for (Notification n : filteredNotifications) {
            rightCol.add(activityItem(n.getTypeIcon(), safe(n.getTitle(), "Untitled notice"), "just now"));
            rightCol.add(vgap(6));
            if (++addedActivities >= 6) break;
        }
        if (addedActivities == 0) {
            rightCol.add(activityItem("ℹ", hasSearch ? "No activity matched your search." : "No recent activity found.", "now"));
        }

        cols.add(leftCol);
        cols.add(rightCol);
        content.add(cols);
        outer.add(styledScroll(content), BorderLayout.CENTER);
        return outer;
    }

    // ═══════════════════════════════════════════════════════════
    //  PROFILE VIEW
    // ═══════════════════════════════════════════════════════════
    private JPanel buildProfileView() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 30, 30, 30));

        AlumniProfile profile = null;
        try {
            profile = AlumniDAO.getProfileByUserId(currentUserId);
        } catch (Exception ignored) {
        }
        if (profile == null) {
            profile = new AlumniProfile();
            profile.setUserId(currentUserId);
            profile.setFullName(username);
        }

        JPanel head = new JPanel(new BorderLayout(12, 0));
        head.setOpaque(false);
        head.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        JPanel headLeft = new JPanel();
        headLeft.setOpaque(false);
        headLeft.setLayout(new BoxLayout(headLeft, BoxLayout.Y_AXIS));
        headLeft.add(lbl("My Profile", F_H1, TXT_H));
        headLeft.add(vgap(4));
        headLeft.add(lbl("Company name and role", F_BODY, TXT_S));
        head.add(headLeft, BorderLayout.WEST);
        JButton editBtn = glowBtn("Edit Profile", ACCENT_I);
        AlumniProfile finalProfile = profile;
        editBtn.addActionListener(e -> {
            ProfileEditDialog dialog = new ProfileEditDialog(this, currentUserId, username, finalProfile);
            dialog.setVisible(true);
            showView("PROFILE");
        });
        head.add(editBtn, BorderLayout.EAST);
        content.add(head);
        content.add(vgap(20));

        // Profile hero card
        JPanel heroCard = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(22, 28, 58), getWidth(), 0, new Color(14, 20, 44));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(new Color(99, 102, 241, 55));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        heroCard.setOpaque(false);
        heroCard.setBorder(new EmptyBorder(24, 24, 24, 24));
        heroCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel avatar = new JLabel(new AvatarIcon(safe(profile.getFullName(), username), 72, ACCENT_I));
        JPanel nameBlock = new JPanel();
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
        nameBlock.setOpaque(false);
        nameBlock.add(lbl(safe(profile.getFullName(), username), new Font("Segoe UI", Font.BOLD, 22), TXT_H));
        nameBlock.add(vgap(4));
        nameBlock.add(lbl(safe(profile.getEmail(), "No email set"), F_BODY, TXT_S));
        nameBlock.add(vgap(6));
        nameBlock.add(lbl(safe(profile.getJobRole(), "Member") + " · " + safe(profile.getCompany(), "HSTU"), F_SMALL, ACCENT_T));
        heroCard.add(avatar, BorderLayout.WEST);
        heroCard.add(nameBlock, BorderLayout.CENTER);
        JPanel heroNote = new JPanel();
        heroNote.setOpaque(false);
        heroNote.setLayout(new BoxLayout(heroNote, BoxLayout.Y_AXIS));
        heroNote.add(lbl("Profile Summary", F_H3, TXT_S));
        heroNote.add(vgap(4));
        heroNote.add(lbl("Your company name and role are shown here for quick recognition and search.", F_SMALL, TXT_M));
        heroCard.add(heroNote, BorderLayout.EAST);
        content.add(heroCard);
        content.add(vgap(20));

        // Details card
        JPanel detailCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 19, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        detailCard.setLayout(new BoxLayout(detailCard, BoxLayout.Y_AXIS));
        detailCard.setOpaque(false);
        detailCard.setBorder(new EmptyBorder(20, 24, 20, 24));
        detailCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        String gradYear = profile.getGraduationYear() <= 0 ? "Not set" : String.valueOf(profile.getGraduationYear());
        detailCard.add(profileLine("Company Name", safe(profile.getCompany(), "Not set")));
        detailCard.add(vgap(10));
        detailCard.add(lineDivider());
        detailCard.add(vgap(10));
        detailCard.add(profileLine("Role", safe(profile.getJobRole(), "Not set")));
        detailCard.add(vgap(10));
        detailCard.add(lineDivider());
        detailCard.add(vgap(10));
        detailCard.add(profileLine("Graduation Year", gradYear));
        detailCard.add(vgap(10));
        detailCard.add(lineDivider());
        detailCard.add(vgap(10));
        detailCard.add(profileLine("Skills", safe(profile.getSkills(), "Not set")));
        detailCard.add(vgap(10));
        detailCard.add(lineDivider());
        detailCard.add(vgap(10));
        detailCard.add(profileLine("Bio", safe(profile.getBio(), "No bio yet")));
        content.add(detailCard);

        outer.add(styledScroll(content), BorderLayout.CENTER);
        return outer;
    }

    private JComponent lineDivider() {
        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(50, 65, 100, 50));
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        sep.setOpaque(false);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setPreferredSize(new Dimension(100, 1));
        return sep;
    }

    // ═══════════════════════════════════════════════════════════
    //  MESSAGES VIEW
    // ═══════════════════════════════════════════════════════════
    private JPanel buildMessagesView() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 30, 30, 30));
        content.add(lbl("Messages", F_H1, TXT_H));
        content.add(vgap(6));
        content.add(lbl("Your conversations with alumni members.", F_BODY, TXT_S));
        content.add(vgap(18));

        List<Integer> conversations = new ArrayList<>();
        try {
            conversations = AlumniDAO.getAllConversations(currentUserId);
        } catch (Exception ignored) {
        }

        if (conversations.isEmpty()) {
            content.add(lbl("No conversations found.", F_BODY, TXT_S));
        } else {
            for (Integer otherUserId : conversations) {
                String otherName = safe(AlumniDAO.getFullNameById(otherUserId), "Member");
                String preview = getConversationPreview(otherUserId);
                String lastTime = getConversationTime(otherUserId);
                JPanel conv = messageCard(otherName, preview, lastTime);
                conv.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        new ChatConversationFrame(currentUserId, username, otherUserId, otherName).setVisible(true);
                    }
                });
                content.add(conv);
                content.add(vgap(8));
            }
        }
        outer.add(styledScroll(content), BorderLayout.CENTER);
        return outer;
    }

    // ═══════════════════════════════════════════════════════════
    //  NOTIFICATIONS VIEW
    // ═══════════════════════════════════════════════════════════
    private JPanel buildNotifView() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 30, 30, 30));

        JPanel head = rowPanel();
        head.setAlignmentX(LEFT_ALIGNMENT);
        head.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        JPanel headText = new JPanel();
        headText.setOpaque(false);
        headText.setLayout(new BoxLayout(headText, BoxLayout.Y_AXIS));
        headText.add(lbl("Notifications & Notice Board", F_H1, TXT_H));
        headText.add(vgap(4));
        headText.add(lbl("Latest jobs, events, and announcements from your alumni network.", F_BODY, TXT_S));
        head.add(headText, BorderLayout.WEST);
        JButton postBtn = glowBtn("+ Post Notice", ACCENT_G);
        postBtn.setPreferredSize(new Dimension(140, 38));
        postBtn.setMaximumSize(new Dimension(140, 38));
        postBtn.addActionListener(e -> openPostNoticeDialog());
        head.add(postBtn, BorderLayout.EAST);
        content.add(head);
        content.add(vgap(16));

        List<Notification> notifications = new ArrayList<>();
        try {
            notifications = AlumniDAO.getAllNotifications(currentUserId);
        } catch (Exception ignored) {
        }

        if (notifications.isEmpty()) {
            content.add(lbl("No notices available.", F_BODY, TXT_S));
        } else {
            for (Notification n : notifications) {
                JPanel item = notificationCard(n);
                item.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        AlumniDAO.markNotificationAsViewed(n.getNotificationId(), currentUserId);
                        showView("NOTIFICATIONS");
                    }
                });
                content.add(item);
                content.add(vgap(10));
            }
        }
        outer.add(styledScroll(content), BorderLayout.CENTER);
        return outer;
    }

    // ═══════════════════════════════════════════════════════════
    //  ACHIEVEMENTS VIEW
    // ═══════════════════════════════════════════════════════════
    private JPanel buildAchievementsView() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 30, 30, 30));
        content.add(lbl("Achievements Feed", F_H1, TXT_H));
        content.add(vgap(6));
        content.add(lbl("A social-style stream of alumni accomplishments.", new Font("Segoe UI", Font.PLAIN, 15), TXT_S));
        content.add(vgap(20));

        List<Achievement> achievements = new ArrayList<>();
        try {
            achievements = DatabaseConnection.getAchievements();
        } catch (Exception ignored) {
        }

        if (achievements.isEmpty()) {
            content.add(lbl("No achievements posted yet.", new Font("Segoe UI", Font.PLAIN, 18), TXT_S));
            content.add(vgap(8));
            content.add(lbl("New posts will appear here like a social feed.", new Font("Segoe UI", Font.PLAIN, 14), TXT_M));
        } else {
            for (int i = achievements.size() - 1; i >= 0; i--) {
                content.add(achievementFeedCard(achievements.get(i)));
                content.add(vgap(14));
            }
        }
        outer.add(styledScroll(content), BorderLayout.CENTER);
        return outer;
    }

    private JPanel achievementFeedCard(Achievement achievement) {
        JPanel card = new JPanel(new BorderLayout(18, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 19, 40, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(80, 100, 150, 80));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 16, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel mediaWrap = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(18, 23, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(80, 100, 150, 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mediaWrap.setOpaque(false);
        mediaWrap.setPreferredSize(new Dimension(270, 180));
        mediaWrap.setMinimumSize(new Dimension(270, 180));
        mediaWrap.setMaximumSize(new Dimension(270, 180));

        String photoPath = safe(achievement.getPhotoPath(), "");
        if (!photoPath.isEmpty() && new File(photoPath).exists()) {
            ImageIcon raw = new ImageIcon(photoPath);
            Image scaled = raw.getImage().getScaledInstance(270, 180, Image.SCALE_SMOOTH);
            JLabel img = new JLabel(new ImageIcon(scaled));
            img.setHorizontalAlignment(SwingConstants.CENTER);
            mediaWrap.add(img, BorderLayout.CENTER);
        } else {
            JLabel placeholder = new JLabel("No Photo", SwingConstants.CENTER);
            placeholder.setFont(new Font("Segoe UI", Font.BOLD, 16));
            placeholder.setForeground(TXT_S);
            mediaWrap.add(placeholder, BorderLayout.CENTER);
        }

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        String postAuthor = "Alumni Network";
        JLabel avatar = new JLabel(new AvatarIcon(postAuthor, 46, ACCENT_B));
        header.add(avatar, BorderLayout.WEST);
        JPanel meta = new JPanel();
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        meta.setOpaque(false);
        meta.add(lbl(postAuthor, new Font("Segoe UI", Font.BOLD, 15), TXT_H));
        meta.add(vgap(2));
        meta.add(lbl(safe(achievement.getUploadDate(), "just now") + " · Public", new Font("Segoe UI", Font.PLAIN, 12), TXT_M));
        header.add(meta, BorderLayout.CENTER);
        body.add(header);
        body.add(vgap(12));

        JTextArea titleArea = new JTextArea(safe(achievement.getTitle(), "Achievement"));
        titleArea.setLineWrap(true);
        titleArea.setWrapStyleWord(true);
        titleArea.setEditable(false);
        titleArea.setOpaque(false);
        titleArea.setForeground(TXT_H);
        titleArea.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleArea.setBorder(null);
        titleArea.setMargin(new Insets(0, 0, 0, 0));
        body.add(titleArea);
        body.add(vgap(8));

        JTextArea desc = new JTextArea(safe(achievement.getDescription(), ""));
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setForeground(new Color(210, 220, 238));
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        desc.setMargin(new Insets(0, 0, 0, 0));
        body.add(desc);

        body.add(vgap(14));

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(LEFT_ALIGNMENT);
        metaRow.add(chipLabel("Posted: " + safe(achievement.getUploadDate(), "just now"), new Color(20, 26, 50), ACCENT_B));
        metaRow.add(chipLabel("Public", new Color(20, 26, 50), ACCENT_G));
        body.add(metaRow);

        card.add(mediaWrap, BorderLayout.WEST);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // ═══════════════════════════════════════════════════════════
    //  ANALYTICS VIEW
    // ═══════════════════════════════════════════════════════════
    private JPanel buildAnalyticsView() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);

        List<Notification> notifications = new ArrayList<>();
        int totalAlumni = 0, totalStudents = 0, totalProfessionals = 0;
        int unreadMessages = 0, unreadAlerts = 0, conversations = 0;
        int jobs = 0, events = 0, announcements = 0;
        try {
            notifications = AlumniDAO.getAllNotifications(currentUserId);
            totalAlumni = AlumniDAO.getTotalProfileCount();
            totalStudents = AlumniDAO.getStudentCount();
            totalProfessionals = Math.max(0, totalAlumni - totalStudents);
            unreadMessages = AlumniDAO.getUnreadMessageCount(currentUserId);
            unreadAlerts = AlumniDAO.getUnreadNotificationCount(currentUserId);
            conversations = AlumniDAO.getAllConversations(currentUserId).size();
        } catch (Exception ignored) {
        }

        for (Notification n : notifications) {
            if (n == null || n.getType() == null) continue;
            if (n.isJobPosting())
                jobs++;
            else if (n.isEvent())
                events++;
            else
                announcements++;
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 30, 30, 30));

        content.add(lbl("Analytics", F_H1, TXT_H));
        content.add(vgap(4));
        content.add(lbl("Live snapshot from your database records", F_BODY, TXT_S));
        content.add(vgap(22));

        JPanel topStats = new JPanel(new GridLayout(1, 4, 14, 0));
        topStats.setOpaque(false);
        topStats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 114));
        topStats.add(statCard("👥", "Alumni", String.valueOf(totalAlumni), "Total profiles", ACCENT_I));
        topStats.add(statCard("🎓", "Students", String.valueOf(totalStudents), "Current students", ACCENT_B));
        topStats.add(statCard("💼", "Professionals", String.valueOf(totalProfessionals), "Career profiles", ACCENT_T));
        topStats.add(statCard("🔔", "Unread Alerts", String.valueOf(unreadAlerts), "Need attention", ACCENT_O));
        content.add(topStats);
        content.add(vgap(22));

        JPanel split = new JPanel(new GridLayout(1, 2, 16, 0));
        split.setOpaque(false);
        split.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JPanel dist = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 19, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        dist.setLayout(new BoxLayout(dist, BoxLayout.Y_AXIS));
        dist.setOpaque(false);
        dist.setBorder(new EmptyBorder(18, 20, 18, 20));
        dist.add(lbl("Notice Distribution", F_H2, TXT_H));
        dist.add(vgap(14));
        dist.add(metricBar("Job Posts", jobs, ACCENT_B));
        dist.add(vgap(10));
        dist.add(metricBar("Events", events, ACCENT_G));
        dist.add(vgap(10));
        dist.add(metricBar("Announcements", announcements, ACCENT_P));

        JPanel commHealth = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 19, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        commHealth.setLayout(new BoxLayout(commHealth, BoxLayout.Y_AXIS));
        commHealth.setOpaque(false);
        commHealth.setBorder(new EmptyBorder(18, 20, 18, 20));
        commHealth.add(lbl("Communication Health", F_H2, TXT_H));
        commHealth.add(vgap(14));
        commHealth.add(profileLine("Active Conversations", String.valueOf(conversations)));
        commHealth.add(vgap(10));
        commHealth.add(lineDivider());
        commHealth.add(vgap(10));
        commHealth.add(profileLine("Unread Messages", String.valueOf(unreadMessages)));
        commHealth.add(vgap(10));
        commHealth.add(lineDivider());
        commHealth.add(vgap(10));
        commHealth.add(profileLine("Total Notices", String.valueOf(notifications.size())));
        commHealth.add(vgap(10));
        commHealth.add(lineDivider());
        commHealth.add(vgap(10));
        commHealth.add(lbl("Tip: keep unread counts low for engagement.", F_SMALL, TXT_M));

        split.add(dist);
        split.add(commHealth);
        content.add(split);
        content.add(vgap(22));

        content.add(lbl("Recent Notice Activity", F_H2, TXT_H));
        content.add(vgap(12));
        if (notifications.isEmpty()) {
            content.add(lbl("No recent notice activity found.", F_BODY, TXT_S));
        } else {
            int shown = 0;
            for (Notification n : notifications) {
                content.add(actionRow(n.getTypeIcon(), safe(n.getTitle(), "Untitled notice"), "just now"));
                content.add(vgap(6));
                if (++shown >= 6) break;
            }
        }
        outer.add(styledScroll(content), BorderLayout.CENTER);
        return outer;
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER COMPONENTS & UI UTILS
    // ═══════════════════════════════════════════════════════════

    private JPanel statCard(String icon, String label, String value, String sub, Color accent) {
        JPanel card = new JPanel() {
            boolean hovered = false;
            float hov = 0f;
            Timer t = new Timer(16, null);
            {
                t.addActionListener(e -> {
                    float target = hovered ? 1f : 0f;
                    hov += (target - hov) * 0.15f;
                    if (Math.abs(hov - target) < 0.01f) {
                        hov = target;
                        t.stop();
                    }
                    repaint();
                });
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        t.start();
                    }
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        t.start();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(interp(CARD, CARD_HOV, hov));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                int glowA = (int) (hov * 140);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30 + glowA));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                GradientPaint gp = new GradientPaint(0, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180),
                    getWidth(), 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 4, 16, 16);
                g2.fillRect(0, 3, getWidth(), 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setLayout(new BorderLayout(0, 0));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(lbl(value, new Font("Inter", Font.BOLD, 32), TXT_H));
        left.add(vgap(4));
        left.add(lbl(label, F_H3, TXT_S));
        left.add(vgap(6));
        left.add(lbl(sub, F_SMALL, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180)));

        JLabel iconLbl = new JLabel(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        iconLbl.setPreferredSize(new Dimension(54, 54));

        card.add(left, BorderLayout.CENTER);
        card.add(iconLbl, BorderLayout.EAST);
        return card;
    }

    private JPanel alumCard(int userId, String name, String roleComp, String skills, String badge, String rank) {
        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            boolean hovered = false;
            float hov = 0f;
            Timer t = new Timer(16, null);
            {
                t.addActionListener(e -> {
                    float target = hovered ? 1f : 0f;
                    hov += (target - hov) * 0.18f;
                    if (Math.abs(hov - target) < 0.01f) {
                        hov = target;
                        t.stop();
                    }
                    repaint();
                });
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        t.start();
                    }
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        t.start();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(interp(CARD, CARD_HOV, hov));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                int ba = (int) (30 + hov * 60);
                g2.setColor(new Color(ACCENT_I.getRed(), ACCENT_I.getGreen(), ACCENT_I.getBlue(), ba));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 98));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel rankLbl = new JLabel(rank);
        rankLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rankLbl.setForeground(ACCENT_O);
        rankLbl.setBorder(new EmptyBorder(4, 8, 4, 8));

        Color av = "STUDENT".equals(badge) ? ACCENT_B : ACCENT_T;
        JLabel avatar = new JLabel(new AvatarIcon(name, 52, av));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.add(lbl(name, new Font("Segoe UI", Font.BOLD, 15), TXT_H));
        info.add(vgap(3));
        info.add(lbl(roleComp, F_SMALL, TXT_S));
        info.add(vgap(6));

        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tags.setOpaque(false);
        String[] sk = skills.split(", ");
        int max = Math.min(sk.length, 3);
        for (int i = 0; i < max; i++) tags.add(miniTag(sk[i]));
        info.add(tags);

        JPanel right = new JPanel(new GridLayout(3, 1, 0, 6));
        right.setOpaque(false);
        right.add(rankLbl);
        right.add(tinyBtn("Message", ACCENT_B, () -> openConversationWith(userId, name)));
        right.add(tinyBtn("Profile", ACCENT_I, () -> showAlumniProfilePreview(userId, name)));

        card.add(avatar, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel activityItem(String dot, String text, String time) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(lbl(dot, new Font("Segoe UI Emoji", Font.PLAIN, 14), TXT_H));
        left.add(lbl(text, F_BODY, TXT_S));

        row.add(left, BorderLayout.CENTER);
        row.add(lbl(time, F_SMALL, TXT_M), BorderLayout.EAST);

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.add(row);
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        wrap.add(sep);
        return wrap;
    }

    private JPanel profileLine(String key, String value) {
        JPanel line = new JPanel(new BorderLayout(16, 0));
        line.setOpaque(false);
        JLabel k = lbl(key, F_H3, TXT_M);
        k.setPreferredSize(new Dimension(140, 24));
        line.add(k, BorderLayout.WEST);
        line.add(lbl(value, F_BODY, TXT_S), BorderLayout.CENTER);
        return line;
    }

    private JPanel messageCard(String name, String preview, String time) {
        JPanel row = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(16, 20, 16, 20));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel avatar = new JLabel(new AvatarIcon(name, 44, ACCENT_B));
        row.add(avatar, BorderLayout.WEST);

        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.setOpaque(false);
        middle.add(lbl(name, new Font("Segoe UI", Font.BOLD, 15), TXT_H));
        middle.add(vgap(4));
        middle.add(lbl(preview, F_BODY, TXT_S));
        row.add(middle, BorderLayout.CENTER);

        row.add(lbl(time, F_SMALL, TXT_M), BorderLayout.EAST);
        return row;
    }

    private JPanel notificationCard(Notification n) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = n.isViewed() ? new Color(14, 19, 40) : new Color(20, 26, 54);
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(72, 88, 132, 85));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

                if (!n.isViewed()) {
                    g2.setColor(ACCENT_I);
                    g2.fillOval(16, 24, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 32, 20, 24));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel badge = badgeLabel(noticeBadgeText(n));
        header.add(badge, BorderLayout.WEST);

        JPanel titleWrap = new JPanel();
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));
        titleWrap.setOpaque(false);

        JLabel titleLbl = lbl(safe(n.getTitle(), "Untitled notice"), new Font("Segoe UI", Font.BOLD, 22), TXT_H);
        JLabel byLbl = lbl(postedByText(n) + " · just now", new Font("Segoe UI", Font.PLAIN, 14), TXT_S);
        titleWrap.add(titleLbl);
        titleWrap.add(vgap(4));
        titleWrap.add(byLbl);
        header.add(titleWrap, BorderLayout.CENTER);

        body.add(header);
        body.add(vgap(16));

        JTextArea text = new JTextArea(normalizeContent(safe(n.getContent(), "No details provided.")));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setOpaque(false);
        text.setForeground(new Color(210, 220, 240));
        text.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        text.setBorder(null);
        text.setMargin(new Insets(2, 0, 2, 0));
        text.setAlignmentX(LEFT_ALIGNMENT);
        body.add(text);
        body.add(vgap(16));

        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tags.setOpaque(false);
        tags.setAlignmentX(LEFT_ALIGNMENT);

        if (n.isJobPosting()) {
            tags.add(chipLabel("Job Title: " + safe(n.getJobPosition(), "Not specified"), new Color(20, 26, 50), ACCENT_B));
            tags.add(chipLabel("Company: " + safe(n.getCompanyName(), "Not specified"), new Color(20, 26, 50), ACCENT_P));
            tags.add(chipLabel("Location: " + safe(n.getJobLocation(), "Not specified"), new Color(20, 26, 50), ACCENT_G));
        }
        if (n.isEvent()) {
            String eventDateText = n.getEventDate() == null ? "Date TBA" : dateOnly(n.getEventDate().toString());
            tags.add(chipLabel("Venue: " + safe(n.getEventLocation(), "Not specified"), new Color(20, 26, 50), ACCENT_I));
            tags.add(chipLabel("Date: " + eventDateText, new Color(20, 26, 50), ACCENT_O));
            if (n.isDonationsEnabled()) {
                String donation = "Raised: " + (int) n.getDonationRaised() + " of " + (int) n.getDonationGoal();
                tags.add(chipLabel(donation, new Color(20, 40, 30), ACCENT_G));
            }
        }

        if (tags.getComponentCount() > 0) {
            body.add(tags);
            body.add(vgap(16));
        }

        if (n.isEvent() && n.isDonationsEnabled()) {
            body.add(donationGraphSection(n));
            body.add(vgap(14));
        }

        if (n.isJobPosting() || (n.isEvent() && n.isDonationsEnabled())) {
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(72, 88, 132, 65));
            sep.setAlignmentX(LEFT_ALIGNMENT);
            body.add(sep);
            body.add(vgap(16));

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            footer.setAlignmentX(LEFT_ALIGNMENT);
            footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

            if (n.isJobPosting()) {
                String applyUrl = safe(n.getApplicationUrl(), "");
                if (!applyUrl.isEmpty()) {
                    JButton applyBtn = outlineActionButton("Apply Now");
                    applyBtn.addActionListener(e -> {
                        try {
                            if (Desktop.isDesktopSupported())
                                Desktop.getDesktop().browse(new URI(applyUrl));
                            else
                                JOptionPane.showMessageDialog(this, "Application URL: " + applyUrl);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Could not open URL: " + applyUrl);
                        }
                    });
                    footer.add(applyBtn, BorderLayout.EAST);
                }
            } else if (n.isEvent() && n.isDonationsEnabled()) {
                boolean donated = AlumniDAO.hasUserDonated(n.getNotificationId(), currentUserId);
                JButton donateBtn = outlineActionButton(donated ? "Donation Pending" : "Donate Now");
                donateBtn.setEnabled(!donated);
                donateBtn.addActionListener(e -> {
                    JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
                    DonationDialog dlg = new DonationDialog(parent, n.getNotificationId(), currentUserId, safe(n.getTitle(), "Event"), n.getDonationGoal(), n.getDonationRaised());
                    dlg.setVisible(true);
                    if (dlg.isSubmitted()) showView("NOTIFICATIONS");
                });
                footer.add(donateBtn, BorderLayout.EAST);
            }
            body.add(footer);
        }

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel donationGraphSection(Notification n) {
        double goal = Math.max(0, n.getDonationGoal());
        double raised = Math.max(0, n.getDonationRaised());
        double ratio = goal <= 0 ? 0 : Math.min(1.0, raised / goal);
        int percent = (int) Math.round(ratio * 100);

        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setAlignmentX(LEFT_ALIGNMENT);

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel left = lbl("Donation Progress", F_H3, TXT_H);
        JLabel right = lbl(percent + "%", F_H3, ACCENT_G);
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        block.add(row);
        block.add(vgap(8));

        JComponent bar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(27, 35, 58));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                int fillW = (int) Math.round(getWidth() * ratio);
                if (fillW > 0) {
                    g2.setPaint(new GradientPaint(0, 0, ACCENT_G, getWidth(), 0, ACCENT_B));
                    g2.fillRoundRect(0, 0, fillW, getHeight(), 10, 10);
                }
                g2.setColor(new Color(255, 255, 255, 20));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(100, 12));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        block.add(bar);
        block.add(vgap(6));

        String goalText = goal <= 0 ? "Open goal" : ("Goal: " + (int) goal);
        String raisedText = "Raised: " + (int) raised;
        block.add(lbl(raisedText + " · " + goalText, F_SMALL, TXT_M));
        return block;
    }

    private String postedByText(Notification n) {
        String postedBy = safe(n.getPostedByName(), "Unknown");
        String company = safe(n.getPosterCompany(), "");
        return company.isEmpty() ? ("Posted by: " + postedBy) : ("Posted by: " + postedBy + " · " + company);
    }

    private String noticeBadgeText(Notification n) {
        String source = n.isJobPosting() ? safe(n.getCompanyName(), "Job") : safe(n.getTitle(), "Notice");
        String[] parts = source.trim().split("\\s+");
        if (parts.length >= 2) return ("" + Character.toUpperCase(parts[0].charAt(0)) + Character.toUpperCase(parts[1].charAt(0)));
        return source.substring(0, Math.min(2, source.length())).toUpperCase();
    }

    private JLabel badgeLabel(String text) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(25, 30, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 22));
        badge.setForeground(ACCENT_I);
        badge.setPreferredSize(new Dimension(64, 64));
        badge.setMinimumSize(new Dimension(64, 64));
        badge.setMaximumSize(new Dimension(64, 64));
        return badge;
    }

    private JLabel chipLabel(String text, Color bg, Color fg) {
        JLabel chip = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chip.setForeground(fg);
        chip.setBorder(new EmptyBorder(6, 12, 6, 12));
        return chip;
    }

    private JButton outlineActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(TXT_H);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(8, 20, 8, 20)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String normalizeContent(String text) {
        if (text == null) return "";
        String[] lines = text.replace("\r", "").split("\n");
        StringBuilder sb = new StringBuilder();
        for (String ln : lines) {
            String t = ln == null ? "" : ln.trim();
            if (!t.isEmpty()) {
                if (sb.length() > 0) sb.append('\n');
                sb.append(t);
            }
        }
        return sb.length() == 0 ? text.trim() : sb.toString();
    }

    private String dateOnly(String text) {
        if (text == null || text.trim().isEmpty()) return "Date TBA";
        String cleaned = text.trim();
        int spaceIdx = cleaned.indexOf(' ');
        if (spaceIdx > 0) return cleaned.substring(0, spaceIdx);
        return cleaned;
    }

    private String getConversationPreview(int otherUserId) {
        try {
            List<Message> messages = AlumniDAO.getConversationMessages(currentUserId, otherUserId);
            if (messages == null || messages.isEmpty()) return "No messages yet";
            String content = safe(messages.get(messages.size() - 1).getContent(), "");
            if (content.length() > 80) return content.substring(0, 77) + "...";
            return content;
        } catch (Exception ignored) {
            return "Open chat";
        }
    }

    private String getConversationTime(int otherUserId) {
        try {
            List<Message> messages = AlumniDAO.getConversationMessages(currentUserId, otherUserId);
            if (messages == null || messages.isEmpty()) return "";
            Timestamp ts = messages.get(messages.size() - 1).getSentAt();
            return timeAgo(ts);
        } catch (Exception ignored) {
            return "";
        }
    }

    private JPanel actionRow(String icon, String title, String sub) {
        JPanel row = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(lbl(icon, new Font("Segoe UI Emoji", Font.PLAIN, 18), TXT_H));
        left.add(lbl(title, F_BODY, TXT_H));
        row.add(left, BorderLayout.CENTER);
        row.add(lbl(sub, F_SMALL, TXT_M), BorderLayout.EAST);
        return row;
    }

    private JPanel metricBar(String label, int value, Color accent) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.add(lbl(label + " · " + value, F_BODY, TXT_S));

        int width = Math.min(260, 40 + value * 10);
        JPanel bar = new JPanel();
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setPreferredSize(new Dimension(260, 8));

        JComponent line = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(25, 30, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, Math.min(getWidth(), width), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        line.setPreferredSize(new Dimension(260, 8));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setLayout(new BorderLayout());
        bar.add(line, BorderLayout.CENTER);
        box.add(vgap(6));
        box.add(bar);
        return box;
    }

    private JPanel alumniCompanyChart(List<AlumniProfile> profiles) {
        Map<String, Integer> companyCounts = new LinkedHashMap<>();
        for (AlumniProfile profile : profiles) {
            if (profile == null) continue;
            String role = safe(profile.getJobRole(), "");
            if ("student".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role) || "administrator".equalsIgnoreCase(role)) continue;
            String company = safe(profile.getCompany(), "Unknown company");
            companyCounts.put(company, companyCounts.getOrDefault(company, 0) + 1);
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(companyCounts.entrySet());
        entries.sort((a, b) -> {
            int cmp = Integer.compare(b.getValue(), a.getValue());
            return cmp != 0 ? cmp : a.getKey().compareToIgnoreCase(b.getKey());
        });

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 19, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        if (entries.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.add(lbl("No alumni company data available yet.", F_BODY, TXT_S));
            card.add(empty, BorderLayout.CENTER);
            return card;
        }

        int maxValue = 0;
        for (Map.Entry<String, Integer> entry : entries) {
            maxValue = Math.max(maxValue, entry.getValue());
        }

        List<Map.Entry<String, Integer>> topEntries = entries.size() > 4 ? entries.subList(0, 4) : entries;
        Color[] colors = new Color[] {ACCENT_R, ACCENT_B, ACCENT_G, ACCENT_O};

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lbl("Top alumni companies", F_SMALL, TXT_M), BorderLayout.WEST);
        header.add(lbl("Count", F_SMALL, TXT_M), BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        final int chartMaxValue = maxValue;

        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 19, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                int leftPad = 58;
                int rightPad = 18;
                int topPad = 16;
                int bottomPad = 54;
                int chartWidth = Math.max(1, getWidth() - leftPad - rightPad);
                int chartHeight = Math.max(1, getHeight() - topPad - bottomPad);
                int axisX = leftPad;
                int axisY = topPad + chartHeight;

                g2.setColor(new Color(80, 92, 130));
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(axisX, topPad, axisX, axisY);
                g2.drawLine(axisX, axisY, getWidth() - rightPad, axisY);

                if (chartMaxValue > 0) {
                    for (int i = 1; i <= 4; i++) {
                        int y = axisY - (chartHeight * i / 4);
                        g2.setColor(new Color(80, 92, 130, 80));
                        g2.drawLine(axisX, y, getWidth() - rightPad, y);
                        g2.setColor(TXT_M);
                        String tick = String.valueOf((int) Math.round(chartMaxValue * i / 4.0));
                        FontMetrics fm = g2.getFontMetrics(F_SMALL);
                        g2.setFont(F_SMALL);
                        g2.drawString(tick, 12, y + (fm.getAscent() / 2) - 1);
                    }
                }

                int slotCount = Math.max(1, topEntries.size());
                int slotWidth = chartWidth / slotCount;
                int barWidth = Math.max(26, Math.min(52, slotWidth / 2));
                int barBaseY = axisY;

                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                for (int i = 0; i < topEntries.size(); i++) {
                    Map.Entry<String, Integer> entry = topEntries.get(i);
                    Color accent = colors[i % colors.length];
                    int barHeight = (int) Math.round((entry.getValue() / (double) chartMaxValue) * (chartHeight - 18));
                    int x = axisX + (slotWidth * i) + (slotWidth - barWidth) / 2;
                    int y = barBaseY - barHeight;

                    g2.setColor(accent);
                    g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.drawRoundRect(x, y, barWidth, barHeight, 10, 10);

                    String valueText = String.valueOf(entry.getValue());
                    FontMetrics valueFm = g2.getFontMetrics();
                    int valueX = x + (barWidth - valueFm.stringWidth(valueText)) / 2;
                    int valueY = y - 6;
                    g2.setColor(TXT_H);
                    g2.drawString(valueText, Math.max(axisX + 2, valueX), Math.max(topPad + 12, valueY));

                    String company = entry.getKey();
                    AffineTransform old = g2.getTransform();
                    g2.rotate(-Math.PI / 2);
                    int labelX = -(barBaseY + 24);
                    int labelY = x + barWidth / 2 + 5;
                    g2.setColor(Color.WHITE);
                    g2.drawString(company, labelX, labelY);
                    g2.setTransform(old);

                    g2.setColor(TXT_S);
                    FontMetrics companyFm = g2.getFontMetrics();
                    String shortName = company;
                    int maxLabelWidth = slotWidth - 8;
                    while (companyFm.stringWidth(shortName) > maxLabelWidth && shortName.length() > 3) {
                        shortName = shortName.substring(0, shortName.length() - 1);
                    }
                    if (!shortName.equals(company) && shortName.length() > 3) shortName = shortName.substring(0, shortName.length() - 3) + "...";
                    int labelWidth = companyFm.stringWidth(shortName);
                    int labelX2 = x + (barWidth - labelWidth) / 2;
                    g2.drawString(shortName, labelX2, axisY + 24);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        chart.setOpaque(false);
        chart.setPreferredSize(new Dimension(260, 230));
        chart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        card.add(chart, BorderLayout.CENTER);

        if (entries.size() > topEntries.size()) {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            footer.setOpaque(false);
            footer.add(lbl("Showing top 4 companies by alumni count.", F_SMALL, TXT_M));
            card.add(footer, BorderLayout.SOUTH);
        }

        return card;
    }

    private void openPostNoticeDialog() {
        if (currentUserId <= 0) {
            JOptionPane.showMessageDialog(this, "Unable to resolve your user profile for posting.", "Post Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PostNotificationDialog dialog = new PostNotificationDialog(this, currentUserId);
        dialog.setVisible(true);
        showView("NOTIFICATIONS");
    }

    private String safe(String s, String fallback) { return (s == null || s.trim().isEmpty()) ? fallback : s.trim(); }

    private String timeAgo(Timestamp ts) {
        if (ts == null) return "now";
        long diff = System.currentTimeMillis() - ts.getTime();
        long mins = diff / 60000;
        long hours = mins / 60;
        long days = hours / 24;
        if (days > 0) return days + "d ago";
        if (hours > 0) return hours + "h ago";
        if (mins > 0) return mins + "m ago";
        return "now";
    }

    private JLabel lbl(String t, Font f, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    private Component vgap(int h) { return Box.createVerticalStrut(h); }

    private JPanel rowPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        return p;
    }

    private JLabel miniTag(String text) {
        JLabel l = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(ACCENT_I.getRed(), ACCENT_I.getGreen(), ACCENT_I.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(ACCENT_I);
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(4, 8, 4, 8));
        return l;
    }

    private JButton tinyBtn(String text, Color accent, Runnable action) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), getModel().isRollover() ? 50 : 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(accent);
        b.setPreferredSize(new Dimension(86, 28));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) {
            b.addActionListener(e -> action.run());
        }
        return b;
    }

    private void openConversationWith(int otherUserId, String otherName) {
        if (currentUserId <= 0) {
            JOptionPane.showMessageDialog(this, "Unable to open chat without a valid logged-in user.", "Message", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new ChatConversationFrame(currentUserId, username, otherUserId, otherName).setVisible(true);
    }

    private void showAlumniProfilePreview(int userId, String fallbackName) {
        AlumniProfile profile = null;
        try {
            profile = AlumniDAO.getProfileByUserId(userId);
        } catch (Exception ignored) {
        }
        if (profile == null) {
            profile = new AlumniProfile();
            profile.setUserId(userId);
            profile.setFullName(fallbackName);
        }

        JDialog dialog = new JDialog(this, "Alumni Profile", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD);

        panel.add(lbl(safe(profile.getFullName(), fallbackName), new Font("Segoe UI", Font.BOLD, 22), TXT_H));
        panel.add(vgap(6));
        panel.add(lbl(safe(profile.getJobRole(), "Not set") + " · " + safe(profile.getCompany(), "Not set"), F_BODY, ACCENT_T));
        panel.add(vgap(14));
        panel.add(profileLine("Email", safe(profile.getEmail(), "Not set")));
        panel.add(vgap(10));
        panel.add(profileLine("Company", safe(profile.getCompany(), "Not set")));
        panel.add(vgap(10));
        panel.add(profileLine("Role", safe(profile.getJobRole(), "Not set")));
        panel.add(vgap(10));
        panel.add(profileLine("Graduation Year", profile.getGraduationYear() <= 0 ? "Not set" : String.valueOf(profile.getGraduationYear())));
        panel.add(vgap(10));
        panel.add(profileLine("Skills", safe(profile.getSkills(), "Not set")));
        panel.add(vgap(10));
        panel.add(profileLine("Bio", safe(profile.getBio(), "No bio yet")));

        JButton closeBtn = glowBtn("Close", ACCENT_I);
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setBorder(new EmptyBorder(10, 20, 20, 20));
        bottom.setBackground(CARD);
        bottom.add(closeBtn);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JButton glowBtn(String text, Color accent) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? accent.darker() : getModel().isRollover() ? accent.brighter()
                                                                                                : accent;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JScrollPane styledScroll(JComponent c) {
        JScrollPane sc = new JScrollPane(c);
        sc.setBorder(null);
        sc.setOpaque(false);
        sc.getViewport().setOpaque(false);
        sc.getVerticalScrollBar().setUnitIncrement(18);
        sc.getVerticalScrollBar().setUI(thinScrollUI());
        return sc;
    }

    private BasicScrollBarUI thinScrollUI() {
        return new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(80, 100, 160, 150);
                trackColor = new Color(0, 0, 0, 0);
            }
            @Override
            protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override
            protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        };
    }

    private Color interp(Color a, Color b, float t) {
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(clamp(r), clamp(g), clamp(bl));
    }
    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }
    private String firstName(String full) {
        if (full == null || full.trim().isEmpty()) return "Member";
        return full.trim().split("\\s+")[0];
    }

    private void setPlaceholder(JTextField f, String ph) {
        f.setText(ph);
        f.setForeground(TXT_M);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(ph)) {
                    f.setText("");
                    f.setForeground(TXT_H);
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(ph);
                    f.setForeground(TXT_M);
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    //  AVATAR ICON & MAIN ENTRY
    // ═══════════════════════════════════════════════════════════
    static class AvatarIcon implements Icon {
        String init;
        int sz;
        Color col;
        AvatarIcon(String name, int sz, Color col) {
            this.sz = sz;
            this.col = col;
            if (name == null || name.isEmpty()) {
                init = "?";
                return;
            }
            String[] p = name.split("\\s+");
            init = (p.length >= 2 ? ("" + p[0].charAt(0) + p[1].charAt(0)) : name.substring(0, Math.min(2, name.length()))).toUpperCase();
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(x, y, col, x + sz, y + sz, col.darker());
            g2.setPaint(gp);
            g2.fillOval(x, y, sz, sz);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, sz / 2 - 1));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(init, x + (sz - fm.stringWidth(init)) / 2, y + (sz - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
        @Override
        public int getIconWidth() { return sz; }
        @Override
        public int getIconHeight() { return sz; }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("ScrollBar.width", 6);
        } catch (Exception ignored) {
        }

        UIManager.put("Panel.background", BG_DEEP);
        UIManager.put("ScrollPane.background", BG_DEEP);
        UIManager.put("Viewport.background", BG_DEEP);

        SwingUtilities.invokeLater(() -> new ModernDashboardUI("Md. Rahul Islam").setVisible(true));
    }
}
