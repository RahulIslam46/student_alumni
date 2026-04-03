import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Admin panel for reviewing and approving/rejecting new member registrations.
 * Shows all pending, approved, and rejected members with full identity details.
 */
public class AdminMemberApprovalPanel extends JFrame {
    // ── Color Palette ────────────────────────────────────────────────────────
    private static final Color BG_MAIN = new Color(245, 247, 250);
    private static final Color BG_HEADER = new Color(30, 41, 59);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_GREEN = new Color(34, 197, 94);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_AMBER = new Color(245, 158, 11);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color ROW_EVEN = new Color(35, 35, 55);
    private static final Color ROW_ODD = new Color(22, 22, 35);

    // ── Table columns ─────────────────────────────────────────────────────────
    private static final String[] COLUMNS = {
        "ID", "Full Name", "Username", "Email", "Type", "Registered", "Status"};

    // ── Components ────────────────────────────────────────────────────────────
    private JTable memberTable;
    private DefaultTableModel tableModel;
    private JLabel pendingCountLabel;
    private JLabel totalCountLabel;
    private JComboBox<String> filterCombo;
    private JTextField searchField;
    private JButton approveBtn, rejectBtn, refreshBtn, detailBtn;

    // ── Constructor ───────────────────────────────────────────────────────────
    public AdminMemberApprovalPanel() {
        setTitle("Admin – Member Approval Panel");
        setSize(1050, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 550));

        initUI();
        loadMembers("pending"); // default view: show pending requests first
    }

    // ── UI Construction ───────────────────────────────────────────────────────
    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Top Header Bar ────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_HEADER);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        // Left – title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("👥");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel title = new JLabel("Member Approval Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        pendingCountLabel = new JLabel("─");
        pendingCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pendingCountLabel.setForeground(ACCENT_AMBER);
        pendingCountLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_AMBER, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        left.add(icon);
        left.add(title);
        left.add(Box.createHorizontalStrut(16));
        left.add(pendingCountLabel);

        // Right – filter + search
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setBackground(new Color(51, 65, 85));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        searchField.putClientProperty("placeholder", "Search by name / email…");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });

        filterCombo = new JComboBox<>(
            new String[] {"All Members", "Pending", "Approved", "Rejected"});
        filterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
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

        refreshBtn = buildSmallBtn("↻ Refresh", ACCENT_BLUE);
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

        right.add(searchField);
        right.add(filterCombo);
        right.add(refreshBtn);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Center: stats cards + table + action buttons ───────────────────────
    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(BG_MAIN);
        center.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        center.add(buildStatsRow(), BorderLayout.NORTH);
        center.add(buildTablePanel(), BorderLayout.CENTER);
        center.add(buildActionPanel(), BorderLayout.SOUTH);
        return center;
    }

    // ── Stats Cards Row ───────────────────────────────────────────────────────
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 80));

        row.add(buildStatCard("⏳  Pending Approvals", "─", ACCENT_AMBER,
            "pending_stat"));
        row.add(buildStatCard("✅  Approved Members", "─", ACCENT_GREEN,
            "approved_stat"));
        row.add(
            buildStatCard("❌  Rejected Members", "─", ACCENT_RED, "rejected_stat"));

        // Refresh stats after building (labels stored as client properties on
        // panel)
        SwingUtilities.invokeLater(this::refreshStats);
        return row;
    }

    private JPanel buildStatCard(String label, String value, Color accent,
        String key) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(22, 22, 35));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 40, 60), 1, true),
            BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        card.putClientProperty("statKey", key);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lbl.setForeground(TEXT_MUTED);
        card.add(lbl, gbc);

        gbc.gridy = 1;
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 28));
        val.setForeground(accent);
        val.putClientProperty("statLabel", Boolean.TRUE);
        card.add(val, gbc);

        return card;
    }

    // ── Member Table ──────────────────────────────────────────────────────────
    private JScrollPane buildTablePanel() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        memberTable = new JTable(tableModel);
        memberTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        memberTable.setRowHeight(32);
        memberTable.setShowHorizontalLines(true);
        memberTable.setGridColor(new Color(40, 40, 60));
        memberTable.setSelectionBackground(new Color(219, 234, 254));
        memberTable.setSelectionForeground(TEXT_DARK);
        memberTable.setFocusable(false);
        memberTable.getTableHeader().setReorderingAllowed(false);

        // Column widths
        int[] widths = {50, 150, 120, 200, 80, 140, 90};
        for (int i = 0; i < widths.length; i++)
            memberTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Custom header style
        JTableHeader header = memberTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 36));

        // Alternating rows + status badge renderer
        memberTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel,
                    foc, row, col);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

                if (!sel) {
                    lbl.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                    lbl.setForeground(TEXT_DARK);
                }

                // Color-code the Status column (index 6)
                if (col == 6) {
                    String status = val != null ? val.toString().toLowerCase() : "";
                    if (!sel) {
                        switch (status) {
                            case "pending"  -> { lbl.setForeground(ACCENT_AMBER); }
                            case "approved" -> { lbl.setForeground(ACCENT_GREEN); }
                            case "rejected" -> { lbl.setForeground(ACCENT_RED);   }
                        }
                    }
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                }
                // Color-code member Type column (index 4)
                if (col == 4 && !sel) {
                    String type = val != null ? val.toString() :
                                "";
                                lbl.setForeground("Alumni".equals(type)
                                        ? new Color(109, 40, 217)
                                        : ACCENT_BLUE);
                        }
                        return lbl;
                    }
                });

                JScrollPane scroll = new JScrollPane(memberTable);
                scroll.setBorder(
                    BorderFactory.createLineBorder(new Color(40, 40, 60), 1, true));
                scroll.getViewport().setBackground(new Color(22, 22, 35));
                return scroll;
            }

            // ── Action Buttons Row
            // ────────────────────────────────────────────────────
            private JPanel buildActionPanel() {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
                panel.setOpaque(false);

                approveBtn = buildActionBtn("✅  Approve", ACCENT_GREEN);
                rejectBtn = buildActionBtn("❌  Reject", ACCENT_RED);
                detailBtn = buildActionBtn("🔍  View Details", ACCENT_BLUE);

                approveBtn.addActionListener(e -> handleApproval("approved"));
                rejectBtn.addActionListener(e -> handleApproval("rejected"));
                detailBtn.addActionListener(e -> showSelectedDetails());

                panel.add(approveBtn);
                panel.add(rejectBtn);
                panel.add(Box.createHorizontalStrut(20));
                panel.add(detailBtn);

                JLabel hint = new JLabel("Select a row, then click Approve or Reject.");
                hint.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                hint.setForeground(TEXT_MUTED);
                panel.add(Box.createHorizontalStrut(20));
                panel.add(hint);

                return panel;
            }

            // ── Status Bar
            // ────────────────────────────────────────────────────────────
            private JPanel buildStatusBar() {
                JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
                bar.setBackground(new Color(241, 245, 249));
                bar.setBorder(BorderFactory.createMatteBorder(
                    1, 0, 0, 0, new Color(40, 40, 60)));

                totalCountLabel = new JLabel("Loaded 0 records");
                totalCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                totalCountLabel.setForeground(TEXT_MUTED);

                JLabel adminNote = new JLabel(
                    "  ·  Admin: Approve verified identities | Reject invalid registrations");
                adminNote.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                adminNote.setForeground(TEXT_MUTED);

                bar.add(totalCountLabel);
                bar.add(adminNote);
                return bar;
            }

            // ── Helper Builders
            // ───────────────────────────────────────────────────────
            private JButton buildSmallBtn(String text, Color color) {
                JButton btn = new JButton(text);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
                btn.setForeground(Color.WHITE);
                btn.setBackground(color);
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
                return btn;
            }

            private JButton buildActionBtn(String text, Color color) {
                JButton btn = new JButton(text);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
                btn.setForeground(Color.WHITE);
                btn.setBackground(color);
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btn.setPreferredSize(new Dimension(140, 38));
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        btn.setBackground(color.darker());
                    }
                    public void mouseExited(MouseEvent e) { btn.setBackground(color); }
                });
                return btn;
            }

            // ── Data Loading
            // ──────────────────────────────────────────────────────────
            private void loadMembers(String filter) {
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        String sql;
                        if ("all".equals(filter)) {
                            sql = "SELECT user_id, full_name, username, email, "
                                + "COALESCE(user_type,'Student') AS user_type, "
                                + "created_at, COALESCE(status,'pending') AS status "
                                + "FROM users WHERE role != 'admin' ORDER BY created_at DESC";
                        } else {
                            sql = "SELECT user_id, full_name, username, email, "
                                + "COALESCE(user_type,'Student') AS user_type, "
                                + "created_at, COALESCE(status,'pending') AS status "
                                + "FROM users WHERE role != 'admin' "
                                + "AND COALESCE(status,'pending') = ? "
                                + "ORDER BY created_at DESC";
                        }

                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement ps = conn.prepareStatement(sql)) {
                            if (!"all".equals(filter))
                                ps.setString(1, filter);

                            ResultSet rs = ps.executeQuery();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy  HH:mm");

                            SwingUtilities.invokeLater(() -> tableModel.setRowCount(0));

                            while (rs.next()) {
                                final Object[] row = {rs.getInt("user_id"),
                                    rs.getString("full_name"),
                                    rs.getString("username"),
                                    rs.getString("email"),
                                    rs.getString("user_type"),
                                    sdf.format(rs.getTimestamp("created_at")),
                                    capitalize(rs.getString("status"))};
                                SwingUtilities.invokeLater(() -> tableModel.addRow(row));
                            }
                            SwingUtilities.invokeLater(() -> {
                                totalCountLabel.setText("Loaded " + tableModel.getRowCount() + " records");
                                refreshStats();
                            });
                        } catch (SQLException ex) {
                            SwingUtilities.invokeLater(()
                                                           -> JOptionPane.showMessageDialog(
                                                               AdminMemberApprovalPanel.this,
                                                               "DB Error: " + ex.getMessage(),
                                                               "Error",
                                                               JOptionPane.ERROR_MESSAGE));
                        }
                        return null;
                    }
                };
                worker.execute();
            }

            // ── Approve / Reject Handler
            // ──────────────────────────────────────────────
            private void handleApproval(String newStatus) {
                int selectedRow = memberTable.getSelectedRow();
                if (selectedRow < 0) {
                    JOptionPane.showMessageDialog(
                        this, "Please select a member from the table first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int userId = (int) tableModel.getValueAt(selectedRow, 0);
                String name = (String) tableModel.getValueAt(selectedRow, 1);
                String email = (String) tableModel.getValueAt(selectedRow, 3);

                // Confirmation dialog with member details
                String verb = "approved".equals(newStatus) ? "APPROVE" : "REJECT";

                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "<html><b>" + verb + " this registration?</b><br><br>"
                        + "<table>"
                        + "<tr><td><b>Name:</b></td><td>&nbsp;" + name + "</td></tr>"
                        + "<tr><td><b>Email:</b></td><td>&nbsp;" + email + "</td></tr>"
                        + "<tr><td><b>User ID:</b></td><td>&nbsp;" + userId + "</td></tr>"
                        + "</table></html>",
                    "Confirm " + verb, JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

                if (confirm != JOptionPane.YES_OPTION)
                    return;

                // If rejecting, optionally capture a reason
                String reason = "";
                if ("rejected".equals(newStatus)) {
                    reason = JOptionPane.showInputDialog(
                        this, "Optional – enter rejection reason (will be noted):",
                        "Rejection Reason", JOptionPane.PLAIN_MESSAGE);
                    if (reason == null)
                        return; // user cancelled
                }

                final String finalReason = reason;
                SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Boolean doInBackground() {
                        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
                        try (Connection conn = DatabaseConnection.getConnection();
                             PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setString(1, newStatus);
                            ps.setInt(2, userId);
                            return ps.executeUpdate() > 0;
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                // Update table cell in place
                                tableModel.setValueAt(capitalize(newStatus), selectedRow, 6);
                                memberTable.repaint();
                                refreshStats();

                                String msg =
                                    "approved".equals(newStatus)
                                    ? "✅  " + name + " has been APPROVED.\nThey can now log in."
                                    : "❌  " + name + " has been REJECTED." + (finalReason.isEmpty() ? "" : "\nReason: " + finalReason);
                                JOptionPane.showMessageDialog(AdminMemberApprovalPanel.this,
                                    msg, capitalize(newStatus),
                                    JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(
                                    AdminMemberApprovalPanel.this,
                                    "Failed to update status. Please try again.", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                worker.execute();
            }

            // ── View Details Dialog
            // ───────────────────────────────────────────────────
            private void showSelectedDetails() {
                int selectedRow = memberTable.getSelectedRow();
                if (selectedRow < 0) {
                    JOptionPane.showMessageDialog(
                        this, "Please select a member from the table first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int userId = (int) tableModel.getValueAt(selectedRow, 0);
                String fullName = (String) tableModel.getValueAt(selectedRow, 1);
                String username = (String) tableModel.getValueAt(selectedRow, 2);
                String email = (String) tableModel.getValueAt(selectedRow, 3);
                String type = (String) tableModel.getValueAt(selectedRow, 4);
                String regDate = (String) tableModel.getValueAt(selectedRow, 5);
                String status = (String) tableModel.getValueAt(selectedRow, 6);

                // Fetch extra profile data
                String[] profileData = {"N/A", "N/A", "N/A", "N/A"};
                try (
                    Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                        "SELECT graduation_year, company, job_role, skills FROM profiles WHERE user_id=?")) {
                    ps.setInt(1, userId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        int yr = rs.getInt("graduation_year");
                        profileData[0] = yr > 0 ? String.valueOf(yr) : "N/A";
                        profileData[1] = rs.getString("company") != null
                            ? rs.getString("company")
                            : "N/A";
                        profileData[2] = rs.getString("job_role") != null
                            ? rs.getString("job_role")
                            : "N/A";
                        profileData[3] =
                            rs.getString("skills") != null ? rs.getString("skills") : "N/A";
                    }
                } catch (SQLException ignored) {
                }

                Color statusColor = switch (status.toLowerCase()) {
            case "approved" -> ACCENT_GREEN;
            case "rejected" -> ACCENT_RED;
            default         -> ACCENT_AMBER;
        };

        JPanel detail = new JPanel(new GridBagLayout());
        detail.setBackground(new Color(22, 22, 35));
        detail.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 8, 4, 16);

        Object[][] rows = {
            {"User ID",          String.valueOf(userId)},
            {"Full Name",        fullName},
            {"Username",         username},
            {"Email",            email},
            {"Account Type",     type},
            {"Registered On",    regDate},
            {"Current Status",   status},
            {"Graduation Year",  profileData[0]},
            {"Company",          profileData[1]},
            {"Job Role",         profileData[2]},
            {"Skills",           profileData[3]},
        };

        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0; g.gridy = i;
            JLabel keyLbl = new JLabel(rows[i][0] + ":");
            keyLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            keyLbl.setForeground(TEXT_MUTED);
            detail.add(keyLbl, g);

            g.gridx = 1;
            JLabel valLbl = new JLabel((String) rows[i][1]);
            valLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            if ("Current Status".equals(rows[i][0]))
                valLbl.setForeground(statusColor);
            else
                valLbl.setForeground(TEXT_DARK);
            detail.add(valLbl, g);
        }

        JOptionPane.showMessageDialog(this, detail,
            "Member Details – " + fullName, JOptionPane.PLAIN_MESSAGE);
    }

    // ── Live Search / Filter ──────────────────────────────────────────────────
    private void applyFilter() {
        String keyword = searchField.getText().toLowerCase().trim();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        memberTable.setRowSorter(sorter);
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }

    // ── Refresh Stats ─────────────────────────────────────────────────────────
    private void refreshStats() {
        SwingWorker<int[], Void> w = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                int[] counts = new int[3]; // [pending, approved, rejected]
                String sql = "SELECT COALESCE(status,'pending') AS st, COUNT(*) AS cnt "
                           + "FROM users WHERE role != 'admin' "
                           + "GROUP BY COALESCE(status,'pending')";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        switch (rs.getString("st")) {
                            case "pending"  -> counts[0] = rs.getInt("cnt");
                            case "approved" -> counts[1] = rs.getInt("cnt");
                            case "rejected" -> counts[2] = rs.getInt("cnt");
                        }
                    }
                } catch (SQLException ignored) {}
                return counts;
            }

            @Override
            protected void done() {
                try {
                    int[] c = get();
                    pendingCountLabel.setText(c[0] + " pending");
                    updateStatCards(c);
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void updateStatCards(int[] counts) {
        // Walk the UI tree to find stat label components
        walkComponents(getContentPane(), counts);
    }

    private void walkComponents(Container container, int[] counts) {
        for (Component comp : container.getComponents()) {
                    if (comp instanceof JPanel panel) {
                        Object key = panel.getClientProperty("statKey");
                        if (key != null) {
                            // Find JLabel child with statLabel property
                            for (Component child : panel.getComponents()) {
                                if (child instanceof JLabel lbl && Boolean.TRUE.equals(lbl.getClientProperty("statLabel"))) {
                                    String skey = (String) key;
                            lbl.setText(String.valueOf(switch (skey) {
                                case "pending_stat"  -> counts[0];
                                case "approved_stat" -> counts[1];
                                case "rejected_stat" -> counts[2];
                                default -> 0;
                            }));
                        }
                    }
                }
                walkComponents(panel, counts);
            }
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    // ── Main (for standalone testing) ────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new AdminMemberApprovalPanel().setVisible(true));
    }
}
