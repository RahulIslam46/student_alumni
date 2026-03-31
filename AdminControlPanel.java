import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;

public class AdminControlPanel extends JFrame {

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

  private static final String[] COLUMNS = {
      "ID", "Full Name", "Username", "Email", "Type", "Registered", "Status"};

  private JTable memberTable;
  private DefaultTableModel tableModel;
  private DefaultTableModel donationTableModel;
  private JLabel pendingCountLabel;
  private JLabel totalCountLabel;
  private JLabel donationPendingLabel;
  private JComboBox<String> filterCombo;
  private JTextField searchField;
  private JButton approveBtn, rejectBtn, refreshBtn, detailBtn;
  private final JFrame dashboardRef;
  private String adminUsername = "";

  public AdminControlPanel() { this((JFrame)null); }

  /**
   * Called from LoginFrame – only a username available, no dashboard ref yet.
   */
  public AdminControlPanel(String username) {
    this((JFrame)null);
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
    bar.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    left.setOpaque(false);
    JLabel icon = new JLabel("?????");
    icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
    JLabel title = new JLabel("Admin Control Panel");
    title.setFont(new Font("Segoe UI", Font.BOLD, 22));
    title.setForeground(Color.WHITE);
    pendingCountLabel = new JLabel("-");
    pendingCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
    pendingCountLabel.setForeground(ACCENT_AMBER);
    pendingCountLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ACCENT_AMBER, 1, true),
        BorderFactory.createEmptyBorder(2, 8, 2, 8)));
    left.add(icon);
    left.add(title);
    left.add(Box.createHorizontalStrut(12));
    left.add(pendingCountLabel);

    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    right.setOpaque(false);
    JButton dashBtn = makeHeaderBtn("🏠  Visit Dashboard", ACCENT_BLUE);
    dashBtn.addActionListener(e -> {
      if (dashboardRef != null) {
        // Came from the sidebar Admin Panel button — just bring dashboard
        // forward
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
    JTabbedPane tabs = new JTabbedPane();
    tabs.setFont(new Font("Segoe UI", Font.BOLD, 15));
    tabs.addTab("  Member Approvals", buildMemberApprovalTab());
    tabs.addTab("  Achievements", buildAchievementsTab());
    tabs.addTab("  💰 Donations", buildDonationsTab());
    return tabs;
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
    searchField = new JTextField(20);
    searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    searchField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
        BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    searchField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        applyFilter();
      }
    });
    filterCombo = new JComboBox<>(
        new String[] {"All Members", "Pending", "Approved", "Rejected"});
    filterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    filterCombo.setCursor(new Cursor(Cursor.HAND_CURSOR));
    filterCombo.addActionListener(e -> {
      String sel = (String)filterCombo.getSelectedItem();
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
      String sel = (String)filterCombo.getSelectedItem();
      if ("Pending".equals(sel))
        loadMembers("pending");
      else if ("Approved".equals(sel))
        loadMembers("approved");
      else if ("Rejected".equals(sel))
        loadMembers("rejected");
      else
        loadMembers("all");
    });
    bar.add(searchField);
    bar.add(filterCombo);
    bar.add(refreshBtn);
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
    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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

  private JScrollPane buildTablePanel() {
    tableModel = new DefaultTableModel(COLUMNS, 0) {
      @Override
      public boolean isCellEditable(int r, int c) {
        return false;
      }
    };
    memberTable = new JTable(tableModel);
    memberTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    memberTable.setRowHeight(30);
    memberTable.setShowHorizontalLines(true);
    memberTable.setGridColor(new Color(40, 40, 60));
    memberTable.setSelectionBackground(new Color(219, 234, 254));
    memberTable.setSelectionForeground(TEXT_DARK);
    memberTable.setFocusable(false);
    memberTable.getTableHeader().setReorderingAllowed(false);
    int[] widths = {50, 150, 120, 210, 80, 140, 90};
    for (int i = 0; i < widths.length; i++)
      memberTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    JTableHeader header = memberTable.getTableHeader();
    header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    header.setBackground(new Color(241, 245, 249));
    header.setForeground(TEXT_MUTED);
    header.setPreferredSize(new Dimension(0, 34));
    memberTable.setDefaultRenderer(
        Object.class, new DefaultTableCellRenderer() {
          @Override
          public Component getTableCellRendererComponent(
              JTable t, Object val, boolean sel, boolean foc, int row,
              int col) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(
                t, val, sel, foc, row, col);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!sel) {
              lbl.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
              lbl.setForeground(Color.WHITE);
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
            if (col == 4 && !sel) {
              String tp = val != null ? val.toString() : "";
              lbl.setForeground("Alumni".equals(tp) ? new Color(109, 40, 217)
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

  private JPanel buildActionPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
    panel.setOpaque(false);
    approveBtn = makeActionBtn("Approve", ACCENT_GREEN);
    rejectBtn = makeActionBtn("Reject", ACCENT_RED);
    detailBtn = makeActionBtn("View Details", ACCENT_BLUE);
    approveBtn.addActionListener(e -> handleApproval("approved"));
    rejectBtn.addActionListener(e -> handleApproval("rejected"));
    detailBtn.addActionListener(e -> showSelectedDetails());
    panel.add(approveBtn);
    panel.add(rejectBtn);
    panel.add(Box.createHorizontalStrut(20));
    panel.add(detailBtn);
    JLabel hint = new JLabel("Select a row, then click Approve or Reject.");
    hint.setFont(new Font("Segoe UI", Font.ITALIC, 13));
    hint.setForeground(TEXT_MUTED);
    panel.add(Box.createHorizontalStrut(20));
    panel.add(hint);
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
    String[] cols = {"ID",           "Event",  "Donor",
                     "Amount (BDT)", "Method", "Mobile/TxnID",
                     "Message",      "Status", "Date"};
    donationTableModel = new DefaultTableModel(cols, 0) {
      @Override
      public boolean isCellEditable(int r, int c) {
        return false;
      }
    };
    JTable donTable = new JTable(donationTableModel);
    donTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    donTable.setRowHeight(28);
    donTable.setShowHorizontalLines(true);
    donTable.setGridColor(new Color(40, 40, 60));
    donTable.setSelectionBackground(new Color(219, 234, 254));
    donTable.setFocusable(false);
    donTable.getTableHeader().setReorderingAllowed(false);
    donTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
    donTable.getTableHeader().setBackground(new Color(241, 245, 249));
    donTable.getTableHeader().setForeground(new Color(100, 116, 139));
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
            setForeground("confirmed".equals(s)  ? ACCENT_GREEN
                          : "rejected".equals(s) ? ACCENT_RED
                                                 : ACCENT_AMBER);
            setFont(getFont().deriveFont(Font.BOLD));
            return this;
          }
        });

    JScrollPane scroll = new JScrollPane(donTable);
    scroll.setBorder(
        BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
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
      int donId = (int)donationTableModel.getValueAt(row, 0);
      String status = (String)donationTableModel.getValueAt(row, 7);
      if (!"pending".equals(status)) {
        JOptionPane.showMessageDialog(
            this, "This donation is already " + status + ".",
            "Already Processed", JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      if (AlumniDAO.confirmDonation(donId)) {
        JOptionPane.showMessageDialog(
            this,
            "✅ Donation #" + donId +
                " confirmed!\nAmount added to event total.",
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
      int donId = (int)donationTableModel.getValueAt(row, 0);
      String status = (String)donationTableModel.getValueAt(row, 7);
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
            +
            "                  THEN CONCAT('TxnID: ',d.transaction_id,' | ') ELSE '' END, "
            + "                  COALESCE(d.message,'')), '') AS detail, "
            + "       d.status, d.created_at "
            + "FROM event_donations d "
            + "JOIN notifications n ON d.notification_id = n.notification_id "
            + "JOIN users u ON d.donor_user_id = u.user_id "
            +
            "ORDER BY FIELD(d.status,'pending','confirmed','rejected'), d.created_at DESC")) {
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
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(BG_MAIN);
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
        BorderFactory.createEmptyBorder(36, 50, 36, 50)));

    JLabel titleLbl = new JLabel("Achievement Management");
    titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
    titleLbl.setForeground(new Color(30, 41, 59));
    titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
    card.add(titleLbl);
    card.add(Box.createRigidArea(new Dimension(0, 6)));

    JLabel subLbl = new JLabel("Create, browse or remove alumni achievements");
    subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    subLbl.setForeground(TEXT_MUTED);
    subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
    card.add(subLbl);
    card.add(Box.createRigidArea(new Dimension(0, 36)));

    JButton postBtn = makeBigBtn("Post New Achievement", ACCENT_GREEN);
    JButton viewBtn = makeBigBtn("View All Achievements", ACCENT_BLUE);
    JButton delBtn = makeBigBtn("Delete Achievement", ACCENT_RED);
    postBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    viewBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    delBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    postBtn.addActionListener(e -> showPostAchievementDialog());
    viewBtn.addActionListener(e -> new AchievementsPage().setVisible(true));
    delBtn.addActionListener(e -> deleteAchievement());

    card.add(postBtn);
    card.add(Box.createRigidArea(new Dimension(0, 14)));
    card.add(viewBtn);
    card.add(Box.createRigidArea(new Dimension(0, 14)));
    card.add(delBtn);
    panel.add(card);
    return panel;
  }

  private void showPostAchievementDialog() {
    JDialog dialog = new JDialog(this, "Post New Achievement", true);
    dialog.setSize(540, 530);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());

    JPanel dHeader = new JPanel(new BorderLayout());
    dHeader.setBackground(BG_HEADER);
    dHeader.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
    JLabel dTitle = new JLabel("Post New Achievement");
    dTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
    dTitle.setForeground(Color.WHITE);
    dHeader.add(dTitle);
    dialog.add(dHeader, BorderLayout.NORTH);

    JPanel form = new JPanel();
    form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
    form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
    form.setBackground(Color.WHITE);

    JLabel tlbl = new JLabel("Achievement Title:");
    tlbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
    tlbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    form.add(tlbl);
    form.add(Box.createRigidArea(new Dimension(0, 4)));

    JTextField titleField = new JTextField();
    titleField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
    titleField.setAlignmentX(Component.LEFT_ALIGNMENT);
    form.add(titleField);
    form.add(Box.createRigidArea(new Dimension(0, 12)));

    JLabel dlbl = new JLabel("Description:");
    dlbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
    dlbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    form.add(dlbl);
    form.add(Box.createRigidArea(new Dimension(0, 4)));

    JTextArea descArea = new JTextArea(6, 30);
    descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    descArea.setLineWrap(true);
    descArea.setWrapStyleWord(true);
    JScrollPane descScroll = new JScrollPane(descArea);
    descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
    form.add(descScroll);
    form.add(Box.createRigidArea(new Dimension(0, 12)));

    final String[] selectedPath = {""};
    JLabel photoLbl = new JLabel("No photo selected");
    photoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    photoLbl.setForeground(TEXT_MUTED);

    JButton selPhotoBtn = makeSmallBtn("Select Photo", ACCENT_BLUE);
    selPhotoBtn.addActionListener(ev -> {
      JFileChooser fc = new JFileChooser();
      fc.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg",
                                                   "png", "gif"));
      if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
        File f = fc.getSelectedFile();
        selectedPath[0] = f.getAbsolutePath();
        photoLbl.setText(f.getName());
      }
    });

    JPanel photoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    photoRow.setOpaque(false);
    photoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
    photoRow.add(selPhotoBtn);
    photoRow.add(photoLbl);
    form.add(photoRow);
    dialog.add(new JScrollPane(form), BorderLayout.CENTER);

    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    btnRow.setBackground(new Color(248, 250, 252));
    btnRow.setBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(203, 213, 225)));
    JButton cancelBtn = makeSmallBtn("Cancel", ACCENT_RED);
    JButton uploadBtn = makeSmallBtn("Upload", ACCENT_GREEN);
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
    btnRow.add(cancelBtn);
    btnRow.add(uploadBtn);
    dialog.add(btnRow, BorderLayout.SOUTH);
    dialog.setVisible(true);
  }

  private void deleteAchievement() {
    String idStr = JOptionPane.showInputDialog(
        this, "Enter Achievement ID to delete:", "Delete Achievement",
        JOptionPane.QUESTION_MESSAGE);
    if (idStr != null && !idStr.trim().isEmpty()) {
      try {
        int id = Integer.parseInt(idStr.trim());
        if (DatabaseConnection.deleteAchievement(id))
          JOptionPane.showMessageDialog(this, "Deleted successfully!",
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE);
        else
          JOptionPane.showMessageDialog(this, "Failed or ID not found.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Invalid ID!", "Error",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private JPanel buildStatusBar() {
    JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
    bar.setBackground(new Color(241, 245, 249));
    bar.setBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 40, 60)));
    totalCountLabel = new JLabel("Loaded 0 records");
    totalCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    totalCountLabel.setForeground(TEXT_MUTED);
    JLabel note = new JLabel(
        "  Admin: Approve verified identities | Reject invalid registrations");
    note.setFont(new Font("Segoe UI", Font.ITALIC, 13));
    note.setForeground(TEXT_MUTED);
    bar.add(totalCountLabel);
    bar.add(note);
    return bar;
  }

  private void loadMembers(String filter) {
    SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
      @Override
      protected Void doInBackground() {
        String sql =
            "all".equals(filter)
                ? "SELECT user_id, full_name, username, email, "
                      + "COALESCE(user_type,'Student') AS user_type, "
                      + "created_at, COALESCE(status,'pending') AS status "
                      +
                      "FROM users WHERE role != 'admin' ORDER BY created_at DESC"
                : "SELECT user_id, full_name, username, email, "
                      + "COALESCE(user_type,'Student') AS user_type, "
                      + "created_at, COALESCE(status,'pending') AS status "
                      + "FROM users WHERE role != 'admin' "
                      +
                      "AND COALESCE(status,'pending') = ? ORDER BY created_at DESC";
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
                              capitalize(rs.getString("status"))});
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
        totalCountLabel.setText("Loaded " + tableModel.getRowCount() +
                                " records");
        refreshStats();
      }
    };
    worker.execute();
  }

  private void handleApproval(String newStatus) {
    int row = memberTable.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Please select a member first.",
                                    "No Selection",
                                    JOptionPane.WARNING_MESSAGE);
      return;
    }
    int userId = (int)tableModel.getValueAt(row, 0);
    String name = (String)tableModel.getValueAt(row, 1);
    String email = (String)tableModel.getValueAt(row, 3);
    String verb = "approved".equals(newStatus) ? "APPROVE" : "REJECT";
    int confirm = JOptionPane.showConfirmDialog(
        this,
        "<html><b>" + verb + " this member?</b><br><br>"
            + "<table><tr><td><b>Name:</b></td><td>&nbsp;" + name + "</td></tr>"
            + "<tr><td><b>Email:</b></td><td>&nbsp;" + email + "</td></tr>"
            + "<tr><td><b>ID:</b></td><td>&nbsp;" + userId +
            "</td></tr></table></html>",
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
    final String fr = reason;
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
            String msg = "approved".equals(newStatus)
                             ? name + " has been APPROVED."
                             : name + " has been REJECTED." +
                                   (fr.isEmpty() ? "" : "\nReason: " + fr);
            JOptionPane.showMessageDialog(AdminControlPanel.this, msg,
                                          capitalize(newStatus),
                                          JOptionPane.INFORMATION_MESSAGE);
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
    int row = memberTable.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "Please select a member first.",
                                    "No Selection",
                                    JOptionPane.WARNING_MESSAGE);
      return;
    }
    int uid = (int)tableModel.getValueAt(row, 0);
    String name = (String)tableModel.getValueAt(row, 1);
    String user = (String)tableModel.getValueAt(row, 2);
    String mail = (String)tableModel.getValueAt(row, 3);
    String type = (String)tableModel.getValueAt(row, 4);
    String date = (String)tableModel.getValueAt(row, 5);
    String stat = (String)tableModel.getValueAt(row, 6);
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
    Color sc = "approved".equalsIgnoreCase(stat)   ? ACCENT_GREEN
               : "rejected".equalsIgnoreCase(stat) ? ACCENT_RED
                                                   : ACCENT_AMBER;
    JPanel detail = new JPanel(new GridBagLayout());
    detail.setBackground(new Color(22, 22, 35));
    detail.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
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
      JLabel v = new JLabel((String)rows[i][1]);
      v.setFont(new Font("Segoe UI", Font.PLAIN, 13));
      v.setForeground("Status".equals(rows[i][0]) ? sc : Color.WHITE);
      detail.add(v, g);
    }
    JOptionPane.showMessageDialog(this, detail, "Details - " + name,
                                  JOptionPane.PLAIN_MESSAGE);
  }

  private void applyFilter() {
    String kw = searchField.getText().toLowerCase().trim();
    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    memberTable.setRowSorter(sorter);
    sorter.setRowFilter(kw.isEmpty() ? null
                                     : RowFilter.regexFilter("(?i)" + kw));
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
                +
                "FROM users WHERE role != 'admin' GROUP BY COALESCE(status,'pending')");
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
            if (child instanceof JLabel lbl &&
                Boolean.TRUE.equals(lbl.getClientProperty("statLabel"))) {
              String sk = (String)key;
              lbl.setText(
                  String.valueOf("pending_stat".equals(sk)    ? counts[0]
                                 : "approved_stat".equals(sk) ? counts[1]
                                 : "rejected_stat".equals(sk) ? counts[2]
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
    btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btn.setBackground(color);
    btn.setForeground(Color.WHITE);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
    btn.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        btn.setBackground(color.darker());
      }
      public void mouseExited(MouseEvent e) { btn.setBackground(color); }
    });
    return btn;
  }

  private JButton makeSmallBtn(String text, Color color) {
    JButton btn = new JButton(text);
    btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btn.setForeground(Color.WHITE);
    btn.setBackground(color);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
    return btn;
  }

  private JButton makeBigBtn(String text, Color color) {
    JButton btn = new JButton(text);
    btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btn.setForeground(Color.WHITE);
    btn.setBackground(color);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setPreferredSize(new Dimension(290, 48));
    btn.setMaximumSize(new Dimension(290, 48));
    btn.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        btn.setBackground(color.darker());
      }
      public void mouseExited(MouseEvent e) { btn.setBackground(color); }
    });
    return btn;
  }

  private JButton makeActionBtn(String text, Color color) {
    JButton btn = new JButton(text);
    btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btn.setForeground(Color.WHITE);
    btn.setBackground(color);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setPreferredSize(new Dimension(140, 36));
    btn.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        btn.setBackground(color.darker());
      }
      public void mouseExited(MouseEvent e) { btn.setBackground(color); }
    });
    return btn;
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
