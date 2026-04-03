import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;

public class AdminControlPanel extends JFrame {
    private static final Color BG_MAIN = new Color(12, 18, 28);
    private static final Color BG_HEADER = new Color(15, 23, 36);
    private static final Color PANEL_GLASS = new Color(255, 255, 255, 18);
    private static final Color PANEL_GLASS_STRONG = new Color(255, 255, 255, 28);
    private static final Color BORDER_SOFT = new Color(148, 163, 184, 70);
    private static final Color ACCENT_BLUE = new Color(56, 189, 248);
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);
    private static final Color ACCENT_RED = new Color(248, 113, 113);
    private static final Color ACCENT_AMBER = new Color(251, 191, 36);
    private static final Color TEXT_DARK = new Color(236, 244, 255);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color ROW_EVEN = new Color(20, 31, 47);
    private static final Color ROW_ODD = new Color(18, 28, 43);
    private static final String TABLE_CARD = "TABLE";
    private static final String EMPTY_CARD = "EMPTY";

    private static final String[] COLUMNS = {
        "ID", "Full Name", "Username", "Email", "Type", "Registered", "Status", "✓", "✕"};

    private JTable memberTable;
    private DefaultTableModel tableModel;
    private DefaultTableModel donationTableModel;
    private DefaultTableModel notificationTableModel;
    private DefaultTableModel achievementTableModel;
    private JLabel pendingCountLabel;
    private JLabel totalCountLabel;
    private JLabel donationPendingLabel;
    private JLabel notificationCountLabel;
    private JLabel achievementCountLabel;
    private JComboBox<String> filterCombo;
    private JTextField searchField;
    private JProgressBar loadingBar;
    private JPanel memberTableHost;
    private CardLayout memberTableHostLayout;
    private TableRowSorter<DefaultTableModel> memberSorter;
    private JTabbedPane mainTabs;
    private JButton approveBtn, rejectBtn, refreshBtn, detailBtn, removeMemberBtn;
    private final JFrame dashboardRef;
    private String adminUsername = "";

    public AdminControlPanel() { this((JFrame) null); }

    /**
     * Called from LoginFrame – only a username available, no dashboard ref yet.
     */
    public AdminControlPanel(String username) {
        this((JFrame) null);
        this.adminUsername = username;
    }

    public AdminControlPanel(JFrame dashboard) {
        this.dashboardRef = dashboard;
        if (dashboard instanceof ModernDashboardUI mdu) {
            adminUsername = mdu.getCurrentUsername();
        }
        setTitle("Admin Control Panel");
        setSize(1150, 740);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(920, 600));
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabbedContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        loadMembers("pending");
    }

    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_HEADER);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SOFT),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("⚙️");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        JLabel title = new JLabel("Admin Control Panel");
        title.setFont(new Font("Inter", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);
        pendingCountLabel = new JLabel("-");
        pendingCountLabel.setFont(new Font("Inter", Font.BOLD, 13));
        pendingCountLabel.setForeground(new Color(255, 244, 214));
        pendingCountLabel.setOpaque(true);
        pendingCountLabel.setBackground(new Color(251, 191, 36, 80));
        pendingCountLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(251, 191, 36, 130), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        left.add(icon);
        left.add(title);
        left.add(Box.createHorizontalStrut(12));
        left.add(pendingCountLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton dashBtn = makeHeaderBtn("🏠  Visit Dashboard", ACCENT_BLUE);
        dashBtn.addActionListener(e -> {
            if (dashboardRef != null) {
                // Dashboard already exists in this session; bring it to front.
                dashboardRef.setVisible(true);
                dashboardRef.toFront();
                dashboardRef.requestFocus();
                dispose();
            } else if (!adminUsername.isEmpty()) {
                // Came from login — open a fresh ModernDashboardUI for this admin
                new ModernDashboardUI(adminUsername).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(AdminControlPanel.this,
                    "Session error: username not found.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        right.add(dashBtn);
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildTabbedContent() {
        mainTabs = new JTabbedPane();
        mainTabs.setFont(new Font("Inter", Font.BOLD, 15));
        mainTabs.setBackground(BG_MAIN);
        mainTabs.setForeground(TEXT_MUTED);
        mainTabs.setOpaque(true);
        mainTabs.addTab("✅  Approvals", buildMemberApprovalTab());
        mainTabs.addTab("🔔  Notifications", buildNotificationsTab());
        mainTabs.addTab("🏆  Achievements", buildAchievementsTab());
        mainTabs.addTab("💰  Donations", buildDonationsTab());
        mainTabs.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_SOFT));
        return mainTabs;
    }

    // --- TAB 1: Member Approvals ---

    private JPanel buildMemberApprovalTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 20, 8, 20));
        panel.add(buildFilterBar(), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(buildStatsRow(), BorderLayout.NORTH);
        center.add(buildTablePanel(), BorderLayout.CENTER);
        center.add(buildActionPanel(), BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bar.setOpaque(false);

        JPanel searchWrap = new JPanel(new BorderLayout(8, 0));
        searchWrap.setBackground(PANEL_GLASS_STRONG);
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        JLabel searchIcon = new JLabel("🔎");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        searchIcon.setForeground(TEXT_MUTED);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Inter", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.setBackground(PANEL_GLASS_STRONG);
        searchField.setForeground(TEXT_DARK);
        searchField.setCaretColor(TEXT_DARK);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });
        searchWrap.add(searchIcon, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);

        filterCombo = new JComboBox<>(
            new String[] {"All Members", "Pending", "Approved", "Rejected"});
        filterCombo.setFont(new Font("Inter", Font.PLAIN, 14));
        filterCombo.setBackground(PANEL_GLASS_STRONG);
        filterCombo.setForeground(TEXT_DARK);
        filterCombo.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        filterCombo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterCombo.addActionListener(e -> {
            String sel = (String) filterCombo.getSelectedItem();
            if ("Pending".equals(sel))
                loadMembers("pending");
            else if ("Approved".equals(sel))
                loadMembers("approved");
            else if ("Rejected".equals(sel))
                loadMembers("rejected");
            else
                loadMembers("all");
        });
        refreshBtn = makeSmallBtn("Refresh", ACCENT_BLUE);
        refreshBtn.addActionListener(e -> {
            String sel = (String) filterCombo.getSelectedItem();
            if ("Pending".equals(sel))
                loadMembers("pending");
            else if ("Approved".equals(sel))
                loadMembers("approved");
            else if ("Rejected".equals(sel))
                loadMembers("rejected");
            else
                loadMembers("all");
        });

        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setPreferredSize(new Dimension(110, 8));
        loadingBar.setBorder(BorderFactory.createEmptyBorder());
        loadingBar.setForeground(ACCENT_BLUE);
        loadingBar.setBackground(new Color(255, 255, 255, 30));

        bar.add(searchWrap);
        bar.add(filterCombo);
        bar.add(refreshBtn);
        bar.add(loadingBar);
        return bar;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 80));
        row.add(
            buildStatCard("Pending Approvals", "-", ACCENT_AMBER, "pending_stat"));
        row.add(
            buildStatCard("Approved Members", "-", ACCENT_GREEN, "approved_stat"));
        row.add(
            buildStatCard("Rejected Members", "-", ACCENT_RED, "rejected_stat"));
        SwingUtilities.invokeLater(this::refreshStats);
        return row;
    }

    private JPanel buildStatCard(String label, String value, Color accent,
        String key) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255, 26),
                    getWidth(), getHeight(), new Color(255, 255, 255, 10));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        card.putClientProperty("statKey", key);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Inter", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        card.add(lbl, gbc);
        gbc.gridy = 1;
        JLabel val = new JLabel(value);
        val.setFont(new Font("Inter", Font.BOLD, 34));
        val.setForeground(accent);
        val.putClientProperty("statLabel", Boolean.TRUE);
        card.add(val, gbc);
        return card;
    }

    private JScrollPane buildTablePanel() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 7 || c == 8;
            }
        };
        memberTable = new JTable(tableModel);
        memberTable.setFont(new Font("Inter", Font.PLAIN, 14));
        memberTable.setRowHeight(30);
        memberTable.setShowHorizontalLines(true);
        memberTable.setShowVerticalLines(false);
        memberTable.setGridColor(new Color(148, 163, 184, 50));
        memberTable.setBackground(ROW_EVEN);
        memberTable.setOpaque(true);
        memberTable.setFillsViewportHeight(true);
        memberTable.setSelectionBackground(new Color(56, 189, 248, 70));
        memberTable.setSelectionForeground(TEXT_DARK);
        memberTable.setFocusable(false);
        memberTable.setAutoCreateRowSorter(true);
        memberTable.getTableHeader().setReorderingAllowed(false);
        memberSorter = new TableRowSorter<>(tableModel);
        memberTable.setRowSorter(memberSorter);

        int[] widths = {50, 150, 120, 210, 80, 140, 90, 48, 48};
        for (int i = 0; i < widths.length; i++)
            memberTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JTableHeader header = memberTable.getTableHeader();
        header.setFont(new Font("Inter", Font.BOLD, 13));
        header.setBackground(new Color(28, 41, 62));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 34));
        memberTable.setDefaultRenderer(
            Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row,
                    int col) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                    if (!sel) {
                        lbl.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                        lbl.setForeground(TEXT_DARK);
                    }
                    if (col == 6) {
                        String st = val != null ? val.toString().toLowerCase() : "";
                        if (!sel) {
                            if ("pending".equals(st))
                                lbl.setForeground(ACCENT_AMBER);
                            else if ("approved".equals(st))
                                lbl.setForeground(ACCENT_GREEN);
                            else if ("rejected".equals(st))
                                lbl.setForeground(ACCENT_RED);
                        }
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                    }
                    if (col == 7 || col == 8) {
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 15f));
                        if (!sel)
                            lbl.setForeground(col == 7 ? ACCENT_GREEN : ACCENT_RED);
                    } else {
                        lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                    if (col == 4 && !sel) {
                        String tp = val != null ? val.toString() : "";
                        lbl.setForeground("Alumni".equals(tp) ? new Color(109, 40, 217)
                                                                : ACCENT_BLUE);
                    }
                    return lbl;
                }
            });

        memberTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = memberTable.rowAtPoint(e.getPoint());
                int viewCol = memberTable.columnAtPoint(e.getPoint());
                if (viewRow < 0)
                    return;
                if (viewCol == 7) {
                    memberTable.setRowSelectionInterval(viewRow, viewRow);
                    handleApproval("approved");
                } else if (viewCol == 8) {
                    memberTable.setRowSelectionInterval(viewRow, viewRow);
                    handleApproval("rejected");
                }
            }
        });

        JScrollPane scroll = new JScrollPane(memberTable);
        scroll.setBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        scroll.getViewport().setBackground(ROW_EVEN);

        memberTableHostLayout = new CardLayout();
        memberTableHost = new JPanel(memberTableHostLayout);
        memberTableHost.setOpaque(false);
        memberTableHost.add(scroll, TABLE_CARD);
        memberTableHost.add(buildMemberEmptyState(), EMPTY_CARD);
        memberTableHostLayout.show(memberTableHost, TABLE_CARD);

        return new JScrollPane(memberTableHost) {
            {
                setBorder(BorderFactory.createEmptyBorder());
                setViewportBorder(BorderFactory.createEmptyBorder());
                getViewport().setOpaque(false);
                getViewport().setView(memberTableHost);
                getVerticalScrollBar().setUnitIncrement(16);
            }
        };
    }

    private JPanel buildMemberEmptyState() {
        JPanel empty = new JPanel();
        empty.setBackground(PANEL_GLASS);
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(48, 20, 48, 20)));

        JLabel icon = new JLabel("✅");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 58));
        icon.setForeground(new Color(52, 211, 153));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("All caught up!");
        title.setFont(new Font("Inter", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel body = new JLabel("No members are currently pending approval.");
        body.setFont(new Font("Inter", Font.PLAIN, 16));
        body.setForeground(TEXT_MUTED);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);

        empty.add(icon);
        empty.add(Box.createVerticalStrut(10));
        empty.add(title);
        empty.add(Box.createVerticalStrut(6));
        empty.add(body);
        return empty;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panel.setOpaque(false);
        approveBtn = makeActionBtn("Approve", ACCENT_GREEN);
        rejectBtn = makeActionBtn("Reject", ACCENT_RED);
        removeMemberBtn = makeActionBtn("Remove Member", ACCENT_RED);
        detailBtn = makeActionBtn("View Details", ACCENT_BLUE);
        styleActionButton(approveBtn, "primary");
        styleActionButton(rejectBtn, "outline-danger");
        styleActionButton(removeMemberBtn, "danger");
        styleActionButton(detailBtn, "outline");
        approveBtn.addActionListener(e -> handleApproval("approved"));
        rejectBtn.addActionListener(e -> handleApproval("rejected"));
        removeMemberBtn.addActionListener(e -> handleRemoveMember());
        detailBtn.addActionListener(e -> showSelectedDetails());
        panel.add(approveBtn);
        panel.add(rejectBtn);
        panel.add(removeMemberBtn);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(detailBtn);
        JLabel hint = new JLabel("Tip: use ✓ / ✕ directly in each row for faster approvals.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        hint.setForeground(TEXT_MUTED);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(hint);
        return panel;
    }

    // --- TAB 1B: Notifications Management ---

    private JPanel buildNotificationsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 20, 8, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Notice Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);
        JLabel sub = new JLabel("Review and remove notices posted in the dashboard.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);
        header.add(left, BorderLayout.WEST);

        notificationCountLabel = new JLabel("Loading...");
        notificationCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        notificationCountLabel.setForeground(ACCENT_AMBER);
        header.add(notificationCountLabel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Type", "Posted By", "Company", "Date", "Active", "Donations"};
        notificationTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(notificationTableModel);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(148, 163, 184, 50));
        table.setBackground(ROW_EVEN);
        table.setOpaque(true);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(56, 189, 248, 70));
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(28, 41, 62));
        table.getTableHeader().setForeground(TEXT_MUTED);
        int[] widths = {50, 220, 100, 140, 150, 120, 70, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    lbl.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                    lbl.setForeground(TEXT_DARK);
                }
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1));
        scroll.getViewport().setBackground(ROW_EVEN);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionRow.setOpaque(false);
        JButton removeNoticeBtn = makeActionBtn("Remove Selected Notice", ACCENT_RED);
        JButton refreshNoticesBtn = makeSmallBtn("Refresh", ACCENT_BLUE);
        removeNoticeBtn.addActionListener(e -> handleRemoveNotification(table));
        refreshNoticesBtn.addActionListener(e -> loadNotifications());
        actionRow.add(removeNoticeBtn);
        actionRow.add(refreshNoticesBtn);
        panel.add(actionRow, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::loadNotifications);
        return panel;
    }

    // --- TAB 3: Donations ---

    private JPanel buildDonationsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 20, 8, 20));

        // top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        donationPendingLabel = new JLabel("Loading...");
        donationPendingLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        donationPendingLabel.setForeground(ACCENT_AMBER);
        JButton refreshDonBtn = makeSmallBtn("↻ Refresh", ACCENT_BLUE);
        refreshDonBtn.addActionListener(e -> loadDonations());
        topBar.add(donationPendingLabel, BorderLayout.WEST);
        topBar.add(refreshDonBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // table
        String[] cols = {"ID", "Event", "Donor",
            "Amount (BDT)", "Method", "Mobile/TxnID",
            "Message", "Status", "Date"};
        donationTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable donTable = new JTable(donationTableModel);
        donTable.setFont(new Font("Inter", Font.PLAIN, 13));
        donTable.setRowHeight(28);
        donTable.setShowHorizontalLines(true);
        donTable.setShowVerticalLines(false);
        donTable.setGridColor(new Color(148, 163, 184, 50));
        donTable.setBackground(ROW_EVEN);
        donTable.setOpaque(true);
        donTable.setFillsViewportHeight(true);
        donTable.setSelectionBackground(new Color(56, 189, 248, 70));
        donTable.setSelectionForeground(TEXT_DARK);
        donTable.setFocusable(false);
        donTable.getTableHeader().setReorderingAllowed(false);
        donTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        donTable.getTableHeader().setBackground(new Color(28, 41, 62));
        donTable.getTableHeader().setForeground(TEXT_MUTED);
        int[] colWidths = {40, 160, 120, 90, 70, 150, 140, 80, 130};
        for (int i = 0; i < colWidths.length; i++)
            donTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        // colour-code status column
        donTable.getColumnModel().getColumn(7).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    String s = v == null ? "" : v.toString();
                    setForeground("confirmed".equals(s) ? ACCENT_GREEN
                            : "rejected".equals(s)      ? ACCENT_RED
                                                          : ACCENT_AMBER);
                    setFont(getFont().deriveFont(Font.BOLD));
                    return this;
                }
            });

        JScrollPane scroll = new JScrollPane(donTable);
        scroll.setBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1));
        scroll.getViewport().setBackground(ROW_EVEN);
        panel.add(scroll, BorderLayout.CENTER);

        // action buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnBar.setOpaque(false);
        JButton acceptBtn = makeSmallBtn("✅  Accept", ACCENT_GREEN);
        JButton rejectDonBtn = makeSmallBtn("❌  Reject", ACCENT_RED);

        acceptBtn.addActionListener(e -> {
            int row = donTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a donation row first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            int donId = (int) donationTableModel.getValueAt(row, 0);
            String status = (String) donationTableModel.getValueAt(row, 7);
            if (!"pending".equals(status)) {
                JOptionPane.showMessageDialog(
                    this, "This donation is already " + status + ".",
                    "Already Processed", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (AlumniDAO.confirmDonation(donId)) {
                JOptionPane.showMessageDialog(
                    this,
                    "✅ Donation #" + donId + " confirmed!\nAmount added to event total.",
                    "Confirmed", JOptionPane.INFORMATION_MESSAGE);
                loadDonations();
                refreshStats();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to confirm. Try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        rejectDonBtn.addActionListener(e -> {
            int row = donTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a donation row first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            int donId = (int) donationTableModel.getValueAt(row, 0);
            String status = (String) donationTableModel.getValueAt(row, 7);
            if (!"pending".equals(status)) {
                JOptionPane.showMessageDialog(
                    this, "This donation is already " + status + ".",
                    "Already Processed", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                this, "Reject donation #" + donId + "?", "Confirm Reject",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (AlumniDAO.rejectDonation(donId)) {
                    JOptionPane.showMessageDialog(this, "❌ Donation rejected.",
                        "Rejected",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadDonations();
                    refreshStats();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to reject. Try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnBar.add(acceptBtn);
        btnBar.add(rejectDonBtn);
        panel.add(btnBar, BorderLayout.SOUTH);

        // load data right away
        SwingUtilities.invokeLater(this::loadDonations);
        return panel;
    }

    private void loadDonations() {
        if (donationTableModel == null)
            return;
        donationTableModel.setRowCount(0);
        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT d.donation_id, n.title, u.full_name, d.amount, "
                + "       d.payment_method, "
                + "       COALESCE(d.account_number,'') AS mobile, "
                + "       COALESCE(CONCAT(CASE WHEN d.transaction_id IS NOT NULL "
                + "                  THEN CONCAT('TxnID: ',d.transaction_id,' | ') ELSE '' END, "
                + "                  COALESCE(d.message,'')), '') AS detail, "
                + "       d.status, d.created_at "
                + "FROM event_donations d "
                + "JOIN notifications n ON d.notification_id = n.notification_id "
                + "JOIN users u ON d.donor_user_id = u.user_id "
                + "ORDER BY FIELD(d.status,'pending','confirmed','rejected'), d.created_at DESC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String mobile = rs.getString("mobile");
                String detail =
                    rs.getString("detail"); // "TxnID: xxx | msg" or just msg
                // Mobile/TxnID column: show phone + TxnID for bKash, empty for Cash
                String mobileAndTxn = mobile.isBlank() ? "" : mobile;
                String txnPart = detail.replaceAll("TxnID: ([^|]+) \\| ?", "TxnID: $1");
                if (!txnPart.isBlank() && txnPart.startsWith("TxnID:"))
                    mobileAndTxn += (mobileAndTxn.isBlank() ? "" : "  |  ") + txnPart;
                // Message column: strip the TxnID prefix
                String msgOnly = detail.replaceAll("^TxnID: [^|]+ \\| ?", "");
                donationTableModel.addRow(new Object[] {
                    rs.getInt("donation_id"), rs.getString("title"),
                    rs.getString("full_name"),
                    String.format("৳%.0f", rs.getDouble("amount")),
                    rs.getString("payment_method"), mobileAndTxn, msgOnly,
                    rs.getString("status"),
                    rs.getTimestamp("created_at") != null
                        ? new java.text.SimpleDateFormat("dd MMM yy HH:mm")
                              .format(rs.getTimestamp("created_at"))
                        : ""});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        refreshStats();
    }

    // --- TAB 2: Achievements ---

    private JPanel buildAchievementsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Achievement Management");
        title.setFont(new Font("Inter", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);
        JLabel sub = new JLabel("Create, review, and remove achievements without entering IDs manually.");
        sub.setFont(new Font("Inter", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);
        header.add(left, BorderLayout.WEST);

        achievementCountLabel = new JLabel("Loading...");
        achievementCountLabel.setFont(new Font("Inter", Font.BOLD, 14));
        achievementCountLabel.setForeground(ACCENT_AMBER);
        header.add(achievementCountLabel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Description", "Uploaded", "Photo"};
        achievementTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(achievementTableModel);
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(148, 163, 184, 50));
        table.setBackground(ROW_EVEN);
        table.setOpaque(true);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(56, 189, 248, 70));
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(28, 41, 62));
        table.getTableHeader().setForeground(TEXT_MUTED);

        int[] widths = {60, 220, 390, 150, 220};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    lbl.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                    lbl.setForeground(TEXT_DARK);
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1));
        scroll.getViewport().setBackground(ROW_EVEN);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actions.setOpaque(false);
        JButton postBtn = makeSmallBtn("Post New", ACCENT_GREEN);
        JButton viewBtn = makeSmallBtn("Open Public Page", ACCENT_BLUE);
        JButton refreshBtn = makeSmallBtn("Refresh", ACCENT_BLUE);
        JButton removeBtn = makeSmallBtn("Remove Selected", ACCENT_RED);
        postBtn.addActionListener(e -> {
            showPostAchievementDialog();
            loadAchievementsAdmin();
        });
        viewBtn.addActionListener(e -> new AchievementsPage().setVisible(true));
        refreshBtn.addActionListener(e -> loadAchievementsAdmin());
        removeBtn.addActionListener(e -> handleRemoveAchievement(table));
        actions.add(postBtn);
        actions.add(viewBtn);
        actions.add(refreshBtn);
        actions.add(removeBtn);
        panel.add(actions, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::loadAchievementsAdmin);
        return panel;
    }

    private void loadAchievementsAdmin() {
        if (achievementTableModel == null || achievementCountLabel == null)
            return;
        achievementTableModel.setRowCount(0);
        java.util.ArrayList<Achievement> list = DatabaseConnection.getAchievements();
        for (Achievement a : list) {
            String desc = a.getDescription() == null ? "" : a.getDescription();
            if (desc.length() > 80)
                desc = desc.substring(0, 80) + "...";
            achievementTableModel.addRow(new Object[] {
                a.getId(),
                a.getTitle() == null ? "" : a.getTitle(),
                desc,
                a.getUploadDate() == null ? "" : a.getUploadDate(),
                a.getPhotoPath() == null ? "" : a.getPhotoPath()});
        }
        achievementCountLabel.setText(list.size() + " achievements loaded");
    }

    private void handleRemoveAchievement(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an achievement first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int achievementId = (int) achievementTableModel.getValueAt(row, 0);
        String title = String.valueOf(achievementTableModel.getValueAt(row, 1));
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Remove achievement \"" + title + "\" permanently?",
            "Confirm Remove Achievement", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        if (DatabaseConnection.deleteAchievement(achievementId)) {
            showToast("Achievement removed", ACCENT_RED);
            loadAchievementsAdmin();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to remove achievement.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPostAchievementDialog() {
        JDialog dialog = new JDialog(this, "Post New Achievement", true);
        dialog.setSize(720, 670);
        dialog.setMinimumSize(new Dimension(660, 620));
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel dHeader = new JPanel(new BorderLayout());
        dHeader.setBackground(Color.WHITE);
        dHeader.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        JLabel dTitle = new JLabel("Post New Achievement");
        dTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        dTitle.setForeground(TEXT_DARK);
        JLabel dSub = new JLabel("Share notable alumni success with clear details and an image.");
        dSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dSub.setForeground(TEXT_MUTED);
        JPanel headTxt = new JPanel();
        headTxt.setLayout(new BoxLayout(headTxt, BoxLayout.Y_AXIS));
        headTxt.setOpaque(false);
        dTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        dSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        headTxt.add(dTitle);
        headTxt.add(Box.createVerticalStrut(4));
        headTxt.add(dSub);
        dHeader.add(headTxt, BorderLayout.WEST);
        dialog.add(dHeader, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 18, 28));
        form.setBackground(Color.WHITE);

        JLabel tlbl = new JLabel("Achievement Title:");
        tlbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tlbl.setForeground(TEXT_DARK);
        tlbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(tlbl);
        form.add(Box.createRigidArea(new Dimension(0, 6)));

        JTextField titleField = new JTextField();
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
        forceLightTextField(titleField);
        titleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        form.add(titleField);
        form.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel dlbl = new JLabel("Description:");
        dlbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        dlbl.setForeground(TEXT_DARK);
        dlbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(dlbl);
        form.add(Box.createRigidArea(new Dimension(0, 6)));

        JTextArea descArea = new JTextArea(10, 30);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        forceLightTextArea(descArea);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        descScroll.getViewport().setBackground(Color.WHITE);
        form.add(descScroll);
        form.add(Box.createRigidArea(new Dimension(0, 18)));

        final String[] selectedPath = {""};
        JLabel photoLbl = new JLabel("No photo selected");
        photoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        photoLbl.setForeground(TEXT_MUTED);

        JButton selPhotoBtn = makeSmallBtn("Select Photo", ACCENT_BLUE);
        selPhotoBtn.setPreferredSize(new Dimension(150, 38));
        selPhotoBtn.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg",
                "png", "gif"));
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                selectedPath[0] = f.getAbsolutePath();
                photoLbl.setText(f.getName() + " selected");
            }
        });

        JPanel photoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        photoRow.setBackground(Color.WHITE);
        photoRow.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        photoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        photoRow.add(selPhotoBtn);
        photoRow.add(photoLbl);
        form.add(photoRow);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.getViewport().setBackground(Color.WHITE);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(formScroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRow.setBackground(Color.WHITE);
        btnRow.setBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));
        JButton aiBtn = makeSmallBtn("Improve with AI", ACCENT_BLUE);
        JButton cancelBtn = makeSmallBtn("Cancel", ACCENT_RED);
        JButton uploadBtn = makeSmallBtn("Upload", ACCENT_GREEN);
        aiBtn.setPreferredSize(new Dimension(170, 40));
        cancelBtn.setPreferredSize(new Dimension(110, 40));
        uploadBtn.setPreferredSize(new Dimension(140, 40));

        aiBtn.addActionListener(e -> {
            String context = "Task: Improve an achievement draft and provide better options.\n"
                + "Current title: " + titleField.getText().trim() + "\n"
                + "Current description: " + descArea.getText().trim() + "\n"
                + "If useful, respond with:\nTITLE: ...\nBODY: ...\n"
                + "and optionally 2-3 alternatives.";
            AiChatDialog.openOrFocus(this,
                "Achievement AI Chat", context, (aiText) -> {
                    if (aiText == null || aiText.trim().isEmpty())
                        return;
                    String norm = aiText.replace("\r\n", "\n");
                    int ti = norm.toUpperCase().indexOf("TITLE:");
                    int bi = norm.toUpperCase().indexOf("BODY:");
                    if (ti >= 0 && bi > ti) {
                        String t = norm.substring(ti + 6, bi).trim();
                        String b = norm.substring(bi + 5).trim();
                        if (!t.isEmpty())
                            titleField.setText(t);
                        if (!b.isEmpty())
                            descArea.setText(b);
                    } else {
                        descArea.setText(aiText.trim());
                    }
                });
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        uploadBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            String d = descArea.getText().trim();
            if (t.isEmpty() || d.isEmpty() || selectedPath[0].isEmpty()) {
                JOptionPane.showMessageDialog(
                    dialog, "Please fill all fields and select a photo!",
                    "Missing Info", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                File src = new File(selectedPath[0]);
                File destDir = new File("resources/images/achievements");
                if (!destDir.exists())
                    destDir.mkdirs();
                String fn = System.currentTimeMillis() + "_" + src.getName();
                Files.copy(src.toPath(), new File(destDir, fn).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
                boolean ok = DatabaseConnection.addAchievement(
                    t, d, "resources/images/achievements/" + fn);
                if (ok) {
                    JOptionPane.showMessageDialog(
                        dialog, "Achievement posted successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to post achievement.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnRow.add(aiBtn);
        btnRow.add(cancelBtn);
        btnRow.add(uploadBtn);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(uploadBtn);
        dialog.setVisible(true);
    }

    private void forceLightTextField(JTextField field) {
        field.setUI(new javax.swing.plaf.basic.BasicTextFieldUI());
        field.setOpaque(false);
        field.setEditable(true);
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setSelectionColor(new Color(191, 219, 254));
        field.setSelectedTextColor(Color.BLACK);
        field.setDisabledTextColor(Color.BLACK);
        field.putClientProperty("style", null);
        field.setHighlighter(new javax.swing.text.DefaultHighlighter());
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(148, 163, 184), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        // Some Linux themes ignore setBackground; custom paint enforces white.
        field.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
            @Override
            protected void paintSafely(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, field.getWidth(), field.getHeight());
                super.paintSafely(g);
            }
        });
    }

    private void forceLightTextArea(JTextArea area) {
        area.setUI(new javax.swing.plaf.basic.BasicTextAreaUI());
        area.setOpaque(false);
        area.setEditable(true);
        area.setBackground(Color.WHITE);
        area.setForeground(Color.BLACK);
        area.setCaretColor(Color.BLACK);
        area.setSelectionColor(new Color(191, 219, 254));
        area.setSelectedTextColor(Color.BLACK);
        area.setDisabledTextColor(Color.BLACK);
        area.putClientProperty("style", null);
        area.setHighlighter(new javax.swing.text.DefaultHighlighter());

        area.setUI(new javax.swing.plaf.basic.BasicTextAreaUI() {
            @Override
            protected void paintSafely(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, area.getWidth(), area.getHeight());
                super.paintSafely(g);
            }
        });
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        bar.setBackground(BG_HEADER);
        bar.setBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_SOFT));
        totalCountLabel = new JLabel("Loaded 0 records");
        totalCountLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        totalCountLabel.setForeground(TEXT_MUTED);
        JLabel note = new JLabel(
            "  Admin: Approve verified identities | Reject invalid registrations");
        note.setFont(new Font("Inter", Font.ITALIC, 13));
        note.setForeground(TEXT_MUTED);
        bar.add(totalCountLabel);
        bar.add(note);
        return bar;
    }

    private void loadMembers(String filter) {
        if (loadingBar != null)
            loadingBar.setVisible(true);
        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                String sql =
                    "all".equals(filter)
                    ? "SELECT user_id, full_name, username, email, "
                        + "COALESCE(user_type,'Student') AS user_type, "
                        + "created_at, COALESCE(status,'pending') AS status "
                        + "FROM users WHERE role != 'admin' ORDER BY created_at DESC"
                    : "SELECT user_id, full_name, username, email, "
                        + "COALESCE(user_type,'Student') AS user_type, "
                        + "created_at, COALESCE(status,'pending') AS status "
                        + "FROM users WHERE role != 'admin' "
                        + "AND COALESCE(status,'pending') = ? ORDER BY created_at DESC";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    if (!"all".equals(filter))
                        ps.setString(1, filter);
                    ResultSet rs = ps.executeQuery();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy  HH:mm");
                    SwingUtilities.invokeLater(() -> tableModel.setRowCount(0));
                    while (rs.next()) {
                        publish(
                            new Object[] {rs.getInt("user_id"), rs.getString("full_name"),
                                rs.getString("username"), rs.getString("email"),
                                rs.getString("user_type"),
                                sdf.format(rs.getTimestamp("created_at")),
                                capitalize(rs.getString("status")), "✓", "✕"});
                    }
                } catch (SQLException ex) {
                    SwingUtilities.invokeLater(
                        ()
                            -> JOptionPane.showMessageDialog(
                                AdminControlPanel.this, "DB Error: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
            @Override
            protected void process(java.util.List<Object[]> chunks) {
                for (Object[] row : chunks)
                    tableModel.addRow(row);
            }
            @Override
            protected void done() {
                totalCountLabel.setText("Loaded " + tableModel.getRowCount() + " records");
                if (loadingBar != null)
                    loadingBar.setVisible(false);
                refreshStats();
                updateMemberEmptyState();
            }
        };
        worker.execute();
    }

    private void loadNotifications() {
        if (notificationTableModel == null || notificationCountLabel == null) return;
        notificationTableModel.setRowCount(0);
        try {
            java.util.List<Notification> notifications = AlumniDAO.getAllNotifications(0);
            for (Notification n : notifications) {
                notificationTableModel.addRow(new Object[] {
                    n.getNotificationId(),
                    n.getTitle(),
                    capitalize(n.getType()),
                    n.getPostedByName() == null ? "Unknown" : n.getPostedByName(),
                    n.getPosterCompany() == null ? "" : n.getPosterCompany(),
                    n.getCreatedAt() == null ? "" : new SimpleDateFormat("dd MMM yyyy  HH:mm").format(n.getCreatedAt()),
                    n.isActive() ? "Yes" : "No",
                    n.isDonationsEnabled() ? ("৳" + (int) n.getDonationRaised() + " / " + (int) n.getDonationGoal()) : "-"});
            }
            notificationCountLabel.setText(notifications.size() + " notices loaded");
        } catch (Exception ex) {
            notificationCountLabel.setText("Unable to load notices");
            ex.printStackTrace();
        }
    }

    private void handleRemoveMember() {
        int viewRow = memberTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a member first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = memberTable.convertRowIndexToModel(viewRow);
        int userId = (int) tableModel.getValueAt(row, 0);
        String name = String.valueOf(tableModel.getValueAt(row, 1));
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Remove member \"" + name + "\" permanently?\nThis will also remove their profile, posts and related data.",
            "Confirm Remove Member", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        if (AlumniDAO.adminDeleteUser(userId)) {
            showToast("Member removed", ACCENT_RED);
            loadMembers((String) filterCombo.getSelectedItem() == null ? "all" : mapFilter((String) filterCombo.getSelectedItem()));
            refreshStats();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to remove member.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRemoveNotification(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a notice first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int notificationId = (int) notificationTableModel.getValueAt(row, 0);
        String title = String.valueOf(notificationTableModel.getValueAt(row, 1));
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Remove notice \"" + title + "\" permanently?",
            "Confirm Remove Notice", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        if (AlumniDAO.adminDeleteNotification(notificationId)) {
            JOptionPane.showMessageDialog(this, "Notice removed successfully.",
                "Removed", JOptionPane.INFORMATION_MESSAGE);
            loadNotifications();
            refreshStats();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to remove notice.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String mapFilter(String display) {
        if ("Pending".equals(display)) return "pending";
        if ("Approved".equals(display)) return "approved";
        if ("Rejected".equals(display)) return "rejected";
        return "all";
    }

    private void handleApproval(String newStatus) {
        int viewRow = memberTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a member first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = memberTable.convertRowIndexToModel(viewRow);
        int userId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String email = (String) tableModel.getValueAt(row, 3);
        String verb = "approved".equals(newStatus) ? "APPROVE" : "REJECT";
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "<html><b>" + verb + " this member?</b><br><br>"
                + "<table><tr><td><b>Name:</b></td><td>&nbsp;" + name + "</td></tr>"
                + "<tr><td><b>Email:</b></td><td>&nbsp;" + email + "</td></tr>"
                + "<tr><td><b>ID:</b></td><td>&nbsp;" + userId + "</td></tr></table></html>",
            "Confirm " + verb, JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        String reason = "";
        if ("rejected".equals(newStatus)) {
            reason = JOptionPane.showInputDialog(
                this, "Optional rejection reason:", "Rejection Reason",
                JOptionPane.PLAIN_MESSAGE);
            if (reason == null)
                return;
        }
        final int sr = row;
        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try (Connection c = DatabaseConnection.getConnection();
                     PreparedStatement p = c.prepareStatement(
                         "UPDATE users SET status = ? WHERE user_id = ?")) {
                    p.setString(1, newStatus);
                    p.setInt(2, userId);
                    return p.executeUpdate() > 0;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        tableModel.setValueAt(capitalize(newStatus), sr, 6);
                        memberTable.repaint();
                        refreshStats();
                        showToast("approved".equals(newStatus) ? "Member approved" : "Member rejected",
                            "approved".equals(newStatus) ? ACCENT_GREEN : ACCENT_RED);
                        updateMemberEmptyState();
                    } else {
                        JOptionPane.showMessageDialog(AdminControlPanel.this,
                            "Failed to update.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        w.execute();
    }

    private void showSelectedDetails() {
        int viewRow = memberTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a member first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = memberTable.convertRowIndexToModel(viewRow);
        int uid = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String user = (String) tableModel.getValueAt(row, 2);
        String mail = (String) tableModel.getValueAt(row, 3);
        String type = (String) tableModel.getValueAt(row, 4);
        String date = (String) tableModel.getValueAt(row, 5);
        String stat = (String) tableModel.getValueAt(row, 6);
        String[] p = {"N/A", "N/A", "N/A", "N/A"};

        try (
            Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(
                "SELECT graduation_year,company,job_role,skills FROM profiles WHERE user_id=?")) {
            ps.setInt(1, uid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int yr = rs.getInt("graduation_year");
                p[0] = yr > 0 ? String.valueOf(yr) : "N/A";
                p[1] =
                    rs.getString("company") != null ? rs.getString("company") : "N/A";
                p[2] =
                    rs.getString("job_role") != null ? rs.getString("job_role") : "N/A";
                p[3] = rs.getString("skills") != null ? rs.getString("skills") : "N/A";
            }
        } catch (SQLException ignored) {
        }

        // Fetch verification document path
        final String verifyDocPath = AlumniDAO.getVerificationDocumentPath(uid);

        Color sc = "approved".equalsIgnoreCase(stat) ? ACCENT_GREEN
            : "rejected".equalsIgnoreCase(stat)      ? ACCENT_RED
                                                       : ACCENT_AMBER;

        // Create custom dialog with image display capabilities
        JDialog dialog = new JDialog(this, "Details - " + name, true);
        dialog.setSize(700, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Main content panel with scrolling
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_MAIN);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Left side: Member details
        JPanel detail = new JPanel(new GridBagLayout());
        detail.setBackground(Color.WHITE);
        detail.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 8, 4, 16);
        Object[][] rows = {{"User ID", String.valueOf(uid)},
            {"Full Name", name},
            {"Username", user},
            {"Email", mail},
            {"Type", type},
            {"Registered", date},
            {"Status", stat},
            {"Grad Year", p[0]},
            {"Company", p[1]},
            {"Job Role", p[2]},
            {"Skills", p[3]}};
        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0;
            g.gridy = i;
            JLabel k = new JLabel(rows[i][0] + ":");
            k.setFont(new Font("Segoe UI", Font.BOLD, 13));
            k.setForeground(TEXT_MUTED);
            detail.add(k, g);
            g.gridx = 1;
            JLabel v = new JLabel((String) rows[i][1]);
            v.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            v.setForeground("Status".equals(rows[i][0]) ? sc : TEXT_DARK);
            detail.add(v, g);
        }

        JScrollPane detailScroll = new JScrollPane(detail);
        detailScroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        detailScroll.getViewport().setBackground(Color.WHITE);
        mainPanel.add(detailScroll, BorderLayout.WEST);

        // Right side: Verification document display
        JPanel docPanel = new JPanel(new BorderLayout(0, 10));
        docPanel.setBackground(BG_MAIN);
        docPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            "📄 Verification Document",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            TEXT_DARK));

        if (verifyDocPath != null && !verifyDocPath.isEmpty()) {
            File docFile = new File(verifyDocPath);
            if (docFile.exists()) {
                String fileExt = getFileExtension(docFile.getName()).toLowerCase();

                // Display image if it's jpg/png
                if ("jpg".equals(fileExt) || "jpeg".equals(fileExt) || "png".equals(fileExt)) {
                    try {
                        ImageIcon imageIcon = new ImageIcon(verifyDocPath);
                        if (imageIcon.getImage() != null) {
                            // Scale image to fit in panel (max 300x400)
                            Image scaledImage = imageIcon.getImage().getScaledInstance(
                                300, 400, Image.SCALE_SMOOTH);
                            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            docPanel.add(imageLabel, BorderLayout.CENTER);
                        }
                    } catch (Exception ex) {
                        docPanel.add(new JLabel("❌ Could not load image"), BorderLayout.CENTER);
                    }
                } else if ("pdf".equals(fileExt)) {
                    JLabel pdfLabel = new JLabel("📕 PDF Document");
                    pdfLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    pdfLabel.setForeground(TEXT_MUTED);
                    pdfLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    docPanel.add(pdfLabel, BorderLayout.CENTER);
                } else {
                    JLabel fileLabel = new JLabel("📁 " + fileExt.toUpperCase() + " File");
                    fileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    fileLabel.setForeground(TEXT_MUTED);
                    fileLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    docPanel.add(fileLabel, BorderLayout.CENTER);
                }

                // Add filename at bottom
                JLabel fileNameLabel = new JLabel("File: " + docFile.getName());
                fileNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                fileNameLabel.setForeground(TEXT_MUTED);
                fileNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                docPanel.add(fileNameLabel, BorderLayout.SOUTH);

                // Add "Open File" button
                JButton openBtn = makeSmallBtn("🔍 Open Document", ACCENT_BLUE);
                openBtn.addActionListener(e -> {
                    try {
                        if (System.getProperty("os.name").toLowerCase().contains("win")) {
                            new ProcessBuilder("cmd", "/c", "start", verifyDocPath).start();
                        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                            new ProcessBuilder("open", verifyDocPath).start();
                        } else {
                            new ProcessBuilder("xdg-open", verifyDocPath).start();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Could not open file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                btnPanel.setOpaque(false);
                btnPanel.add(openBtn);
                docPanel.add(btnPanel, BorderLayout.NORTH);
            } else {
                JLabel notFoundLabel = new JLabel("❌ Document file not found");
                notFoundLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                notFoundLabel.setForeground(ACCENT_RED);
                notFoundLabel.setHorizontalAlignment(SwingConstants.CENTER);
                docPanel.add(notFoundLabel, BorderLayout.CENTER);
            }
        } else {
            JLabel noDocLabel = new JLabel("❌ No verification document");
            noDocLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noDocLabel.setForeground(ACCENT_RED);
            noDocLabel.setHorizontalAlignment(SwingConstants.CENTER);
            docPanel.add(noDocLabel, BorderLayout.CENTER);
        }

        mainPanel.add(docPanel, BorderLayout.CENTER);
        dialog.add(mainPanel, BorderLayout.CENTER);

        // Close button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setOpaque(false);
        JButton closeBtn = makeSmallBtn("Close", ACCENT_RED);
        closeBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private String getFileExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx > 0 && idx < filename.length() - 1) {
            return filename.substring(idx + 1).toLowerCase();
        }
        return "";
    }

    private void applyFilter() {
        String kw = searchField.getText().toLowerCase().trim();
        if (memberSorter == null)
            return;
        memberSorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + kw));
        updateMemberEmptyState();
    }

    private void updateMemberEmptyState() {
        if (memberTableHostLayout == null || memberTable == null)
            return;
        if (memberTable.getRowCount() == 0)
            memberTableHostLayout.show(memberTableHost, EMPTY_CARD);
        else
            memberTableHostLayout.show(memberTableHost, TABLE_CARD);
    }

    private void showToast(String message, Color accent) {
        JWindow toast = new JWindow(this);
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(new Color(17, 26, 41));
        body.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("Inter", Font.BOLD, 13));
        lbl.setForeground(new Color(241, 245, 249));
        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(4, 1));
        body.add(stripe, BorderLayout.WEST);
        body.add(lbl, BorderLayout.CENTER);
        toast.add(body);
        toast.pack();

        Point p = getLocationOnScreen();
        int x = p.x + getWidth() - toast.getWidth() - 20;
        int y = p.y + getHeight() - toast.getHeight() - 40;
        toast.setLocation(x, y);
        toast.setAlwaysOnTop(true);
        toast.setVisible(true);

        Timer t = new Timer(1800, e -> toast.dispose());
        t.setRepeats(false);
        t.start();
    }

    private void refreshStats() {
        SwingWorker<int[], Void> w = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                int[] counts = new int
                    [4]; // 0=memberPending,1=approved,2=rejected,3=donationPending
                try (
                    Connection c = DatabaseConnection.getConnection();
                    PreparedStatement ps = c.prepareStatement(
                        "SELECT COALESCE(status,'pending') AS st, COUNT(*) AS cnt "
                        + "FROM users WHERE role != 'admin' GROUP BY COALESCE(status,'pending')");
                    ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String st = rs.getString("st");
                        if ("pending".equals(st))
                            counts[0] = rs.getInt("cnt");
                        else if ("approved".equals(st))
                            counts[1] = rs.getInt("cnt");
                        else if ("rejected".equals(st))
                            counts[2] = rs.getInt("cnt");
                    }
                } catch (SQLException ignored) {
                }
                try (Connection c = DatabaseConnection.getConnection();
                     PreparedStatement ps = c.prepareStatement(
                         "SELECT COUNT(*) FROM event_donations WHERE status='pending'");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        counts[3] = rs.getInt(1);
                } catch (SQLException ignored) {
                }
                return counts;
            }
            @Override
            protected void done() {
                try {
                    int[] c = get();
                    String badge = c[0] + " members pending";
                    if (c[3] > 0)
                        badge += "  |  💰 " + c[3] + " donations";
                    pendingCountLabel.setText(badge);
                    if (donationPendingLabel != null) {
                        donationPendingLabel.setText(
                            c[3] == 0 ? "No pending donations"
                                      : c[3] + " pending donation(s) awaiting review");
                        donationPendingLabel.setForeground(
                            c[3] == 0 ? new Color(100, 116, 139) : ACCENT_AMBER);
                    }
                    walkComponents(getContentPane(), c);
                } catch (Exception ignored) {
                }
            }
        };
        w.execute();
    }

    private void walkComponents(Container container, int[] counts) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel panel) {
                Object key = panel.getClientProperty("statKey");
                if (key != null) {
                    for (Component child : panel.getComponents()) {
                        if (child instanceof JLabel lbl && Boolean.TRUE.equals(lbl.getClientProperty("statLabel"))) {
                            String sk = (String) key;
                            lbl.setText(
                                String.valueOf("pending_stat".equals(sk) ? counts[0]
                                        : "approved_stat".equals(sk)     ? counts[1]
                                        : "rejected_stat".equals(sk)     ? counts[2]
                                                                           : 0));
                        }
                    }
                }
                walkComponents(panel, counts);
            }
        }
    }

    private JButton makeHeaderBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setBackground(new Color(59, 130, 246));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(37, 99, 235), 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(37, 99, 235));
            }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(59, 130, 246)); }
        });
        return btn;
    }

    private JButton makeSmallBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 55), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled())
                    btn.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled())
                    btn.setBackground(color);
            }
        });
        return btn;
    }

    private JButton makeActionBtn(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 55), 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 38));
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled())
                    btn.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled())
                    btn.setBackground(color);
            }
        });
        return btn;
    }

    private void styleActionButton(JButton btn, String type) {
        if ("primary".equals(type)) {
            btn.setBackground(ACCENT_GREEN);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(6, 95, 70), 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        } else if ("danger".equals(type)) {
            btn.setBackground(ACCENT_RED);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(153, 27, 27), 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        } else if ("outline-danger".equals(type)) {
            btn.setBackground(new Color(248, 113, 113, 180));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(248, 113, 113, 130), 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        } else {
            btn.setBackground(new Color(71, 85, 105));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184, 130), 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new AdminControlPanel().setVisible(true));
    }
}
