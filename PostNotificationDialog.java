import java.awt.*;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.*;

public class PostNotificationDialog extends JDialog {
  private int currentUserId;
  private JTextField titleField;
  private JTextArea contentArea;
  private JComboBox<String> typeCombo;
  private JComboBox<String> priorityCombo;
  private JTextField companyField, positionField, locationField;
  private JComboBox<String> jobTypeCombo;
  private JTextField salaryField, deadlineField, urlField;
  private JTextField eventDateField, eventLocationField;
  private JCheckBox donationsCheckbox;
  private JTextField donationGoalField;
  private JPanel jobPanel, eventPanel;
  private boolean success = false;

  private static final Color BG_TOP = new Color(22, 22, 35);
  private static final Color BG_BOT = new Color(15, 15, 25);
  private static final Color ACCENT = new Color(99, 102, 241);
  private static final Color ACCENT_DARK = new Color(79, 70, 229);
  private static final Color CARD_BG = new Color(35, 35, 55);
  private static final Color BORDER_CLR = new Color(70, 70, 110);
  private static final Color LBL_CLR = Color.WHITE;
  private static final Font FONT_LBL = new Font("Segoe UI", Font.BOLD, 16);
  private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 16);
  private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);

  public PostNotificationDialog(JFrame parent, int userId) {
    super(parent, "Post New Notice", true);
    this.currentUserId = userId;
    setSize(680, 740);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout());

    JPanel bgPanel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp =
            new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOT);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
      }
    };
    bgPanel.setLayout(new BorderLayout());

    // HEADER
    JPanel header = new JPanel(new GridBagLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp =
            new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT_DARK);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
      }
    };
    header.setOpaque(false);
    header.setPreferredSize(new Dimension(0, 86));
    header.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
    GridBagConstraints hg = new GridBagConstraints();
    hg.gridx = 0;
    hg.gridy = 0;
    hg.anchor = GridBagConstraints.WEST;
    hg.weightx = 1;
    JLabel titleLbl = new JLabel("\u270F  Create Notice");
    titleLbl.setFont(FONT_TITLE);
    titleLbl.setForeground(Color.WHITE);
    header.add(titleLbl, hg);
    hg.gridy = 1;
    JLabel subLbl =
        new JLabel("Fill in the details below and post to all members");
    subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    subLbl.setForeground(new Color(199, 210, 254));
    header.add(subLbl, hg);
    bgPanel.add(header, BorderLayout.NORTH);

    // FORM
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setOpaque(false);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

    mainPanel.add(makeLabel("Notice Type"));
    mainPanel.add(gap(5));
    typeCombo = makeCombo(new String[] {"\uD83D\uDCBC  Job Posting",
                                        "\uD83D\uDCC5  Event",
                                        "\uD83D\uDCE2  Announcement"});
    typeCombo.addActionListener(e -> toggleFieldVisibility());
    mainPanel.add(typeCombo);
    mainPanel.add(gap(14));

    mainPanel.add(makeLabel("Priority"));
    mainPanel.add(gap(5));
    priorityCombo = makeCombo(new String[] {
        "\uD83D\uDD35  Normal", "\uD83D\uDFE0  High", "\uD83D\uDD34  Urgent"});
    mainPanel.add(priorityCombo);
    mainPanel.add(gap(14));

    mainPanel.add(makeLabel("Title *"));
    mainPanel.add(gap(5));
    titleField = makeTextField("Enter notice title...");
    mainPanel.add(titleField);
    mainPanel.add(gap(14));

    mainPanel.add(makeLabel("Description / Details *"));
    mainPanel.add(gap(5));
    contentArea = new JTextArea(4, 40);
    contentArea.setFont(FONT_INPUT);
    contentArea.setLineWrap(true);
    contentArea.setWrapStyleWord(true);
    contentArea.setForeground(Color.WHITE);
    contentArea.setBackground(new Color(35, 35, 55));
    contentArea.setCaretColor(Color.WHITE);
    contentArea.setBorder(BorderFactory.createCompoundBorder(
        new RoundedBorder(BORDER_CLR, 2, 10),
        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    JScrollPane cs = new JScrollPane(contentArea);
    cs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
    cs.setAlignmentX(Component.LEFT_ALIGNMENT);
    cs.setBorder(null);
    mainPanel.add(cs);
    mainPanel.add(gap(14));

    jobPanel = createJobPanel();
    mainPanel.add(jobPanel);
    eventPanel = createEventPanel();
    mainPanel.add(eventPanel);
    toggleFieldVisibility();

    JScrollPane scroll = new JScrollPane(mainPanel);
    scroll.setBorder(null);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    bgPanel.add(scroll, BorderLayout.CENTER);

    // BUTTONS
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 14));
    btnPanel.setOpaque(false);
    btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_CLR));
    JButton cancelBtn =
        makeButton("  Cancel  ", new Color(239, 68, 68), Color.WHITE);
    cancelBtn.addActionListener(e -> dispose());
    JButton postBtn = makeButton("  Post Notice  ", ACCENT, Color.WHITE);
    postBtn.addActionListener(e -> postNotification());
    btnPanel.add(cancelBtn);
    btnPanel.add(postBtn);
    bgPanel.add(btnPanel, BorderLayout.SOUTH);
    add(bgPanel);
  }

  private JPanel createJobPanel() {
    JPanel c = makeCard("\uD83D\uDCBC  Job Posting Details");
    c.add(makeLabel("Company Name *"));
    c.add(gap(5));
    companyField = makeTextField("e.g. Google, Microsoft...");
    c.add(companyField);
    c.add(gap(12));
    c.add(makeLabel("Job Position *"));
    c.add(gap(5));
    positionField = makeTextField("e.g. Software Engineer...");
    c.add(positionField);
    c.add(gap(12));
    c.add(makeLabel("Location"));
    c.add(gap(5));
    locationField = makeTextField("e.g. Dhaka, Remote...");
    c.add(locationField);
    c.add(gap(12));
    c.add(makeLabel("Job Type"));
    c.add(gap(5));
    jobTypeCombo = makeCombo(new String[] {"Full-time", "Part-time", "Contract",
                                           "Internship", "Remote"});
    c.add(jobTypeCombo);
    c.add(gap(12));
    c.add(makeLabel("Salary Range"));
    c.add(gap(5));
    salaryField = makeTextField("e.g. 50,000 - 70,000 BDT");
    c.add(salaryField);
    c.add(gap(12));
    c.add(makeLabel("Application Deadline  (YYYY-MM-DD)"));
    c.add(gap(5));
    deadlineField = makeTextField("e.g. 2026-12-31");
    c.add(deadlineField);
    c.add(gap(12));
    c.add(makeLabel("Application URL"));
    c.add(gap(5));
    urlField = makeTextField("https://...");
    c.add(urlField);
    return c;
  }

  private JPanel createEventPanel() {
    JPanel c = makeCard("\uD83D\uDCC5  Event Details");
    c.add(makeLabel("Event Date & Time  (YYYY-MM-DD HH:MM)"));
    c.add(gap(5));
    eventDateField = makeTextField("e.g. 2026-03-15 10:00");
    c.add(eventDateField);
    c.add(gap(12));
    c.add(makeLabel("Event Location *"));
    c.add(gap(5));
    eventLocationField = makeTextField("e.g. HSTU Auditorium...");
    c.add(eventLocationField);
    c.add(gap(16));

    // ── Donation section ──────────────────────────────────────────────────
    donationsCheckbox =
        new JCheckBox("  \uD83D\uDCB8  Enable Donations for this event");
    donationsCheckbox.setFont(new Font("Segoe UI", Font.BOLD, 15));
    donationsCheckbox.setForeground(new Color(52, 211, 153));
    donationsCheckbox.setBackground(CARD_BG);
    donationsCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
    donationsCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
    donationsCheckbox.addActionListener(
        e -> donationGoalField.setEnabled(donationsCheckbox.isSelected()));
    c.add(donationsCheckbox);
    c.add(gap(10));
    c.add(makeLabel("Donation Goal (BDT)  — leave 0 for open-ended"));
    c.add(gap(5));
    donationGoalField = makeTextField("e.g. 10000  (0 = no specific goal)");
    donationGoalField.setEnabled(false);
    c.add(donationGoalField);
    return c;
  }

  private JPanel makeCard(String title) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBackground(new Color(35, 35, 55));
    card.setAlignmentX(Component.LEFT_ALIGNMENT);
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    card.setBorder(BorderFactory.createCompoundBorder(
        new RoundedBorder(BORDER_CLR, 2, 12),
        BorderFactory.createEmptyBorder(14, 16, 16, 16)));
    JLabel lbl = new JLabel(title);
    lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
    lbl.setForeground(ACCENT_DARK);
    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    card.add(lbl);
    card.add(gap(12));
    return card;
  }

  private JLabel makeLabel(String text) {
    JLabel lbl = new JLabel(text);
    lbl.setFont(FONT_LBL);
    lbl.setForeground(Color.WHITE);
    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
    return lbl;
  }

  private JTextField makeTextField(String ph) {
    JTextField f = new JTextField() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !isFocusOwner()) {
          Graphics2D g2 = (Graphics2D)g;
          g2.setColor(new Color(156, 163, 175));
          g2.setFont(getFont().deriveFont(Font.ITALIC, 14f));
          Insets ins = getInsets();
          FontMetrics fm = g2.getFontMetrics();
          g2.drawString(ph, ins.left + 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        }
      }
    };
    f.setFont(FONT_INPUT);
    f.setForeground(Color.WHITE);
    f.setBackground(new Color(35, 35, 55));
    f.setCaretColor(Color.WHITE);
    f.setBorder(BorderFactory.createCompoundBorder(
        new RoundedBorder(BORDER_CLR, 2, 10),
        BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
    f.setAlignmentX(Component.LEFT_ALIGNMENT);
    f.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusGained(java.awt.event.FocusEvent e) {
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(ACCENT, 2, 10),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        f.repaint();
      }
      public void focusLost(java.awt.event.FocusEvent e) {
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_CLR, 2, 10),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        f.repaint();
      }
    });
    return f;
  }

  private JComboBox<String> makeCombo(String[] items) {
    JComboBox<String> cb = new JComboBox<>(items);
    cb.setFont(FONT_INPUT);
    cb.setBackground(new Color(35, 35, 55));
    cb.setForeground(Color.WHITE);
    cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
    cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    return cb;
  }

  private JButton makeButton(String text, Color bg, Color fg) {
    JButton btn = new JButton(text) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg.darker());
        g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 14, 14);
        g2.setColor(getModel().isPressed() ? bg.darker() : bg);
        g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 3, 14, 14);
        g2.setColor(new Color(255, 255, 255, 55));
        g2.fillRoundRect(0, 0, getWidth() - 2, (getHeight() - 3) / 2, 14, 14);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btn.setForeground(fg);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setPreferredSize(new Dimension(168, 46));
    return btn;
  }

  private Component gap(int h) { return Box.createVerticalStrut(h); }

  private void toggleFieldVisibility() {
    int idx = typeCombo.getSelectedIndex();
    jobPanel.setVisible(idx == 0);
    eventPanel.setVisible(idx == 1);
    revalidate();
    repaint();
  }

  private void postNotification() {
    if (titleField.getText().trim().isEmpty()) {
      showError("Please enter a title.");
      return;
    }
    if (contentArea.getText().trim().isEmpty()) {
      showError("Please enter description/details.");
      return;
    }
    int st = typeCombo.getSelectedIndex();
    if (st == 0) {
      if (companyField.getText().trim().isEmpty() ||
          positionField.getText().trim().isEmpty()) {
        showError("Company name and job position are required.");
        return;
      }
      String dl = deadlineField.getText().trim();
      if (!dl.isEmpty()) {
        if (!dl.matches("\\d{4}-\\d{2}-\\d{2}")) {
          showError("Deadline must be YYYY-MM-DD.");
          return;
        }
        try {
          java.sql.Date.valueOf(dl);
        } catch (IllegalArgumentException e) {
          showError("Invalid date.");
          return;
        }
      }
    }
    if (st == 1 && eventLocationField.getText().trim().isEmpty()) {
      showError("Event location is required.");
      return;
    }

    Notification n = new Notification();
    n.setPostedByUserId(currentUserId);
    n.setTitle(titleField.getText().trim());
    n.setContent(contentArea.getText().trim());
    n.setType(st == 0 ? "job_posting" : (st == 1 ? "event" : "announcement"));
    int pi = priorityCombo.getSelectedIndex();
    n.setPriority(pi == 0 ? "normal" : (pi == 1 ? "high" : "urgent"));
    if (st == 0) {
      n.setCompanyName(companyField.getText().trim());
      n.setJobPosition(positionField.getText().trim());
      n.setJobLocation(locationField.getText().trim());
      n.setJobType((String)jobTypeCombo.getSelectedItem());
      n.setSalaryRange(salaryField.getText().trim());
      n.setApplicationDeadline(deadlineField.getText().trim());
      n.setApplicationUrl(urlField.getText().trim());
    }
    if (st == 1) {
      n.setEventLocation(eventLocationField.getText().trim());
      if (!eventDateField.getText().trim().isEmpty()) {
        try {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
          java.util.Date d = sdf.parse(eventDateField.getText().trim());
          n.setEventDate(new java.sql.Timestamp(d.getTime()));
        } catch (Exception e) {
          showError("Invalid date. Use YYYY-MM-DD HH:MM");
          return;
        }
      }
      // donation settings
      boolean donationsOn = donationsCheckbox.isSelected();
      n.setDonationsEnabled(donationsOn);
      if (donationsOn) {
        String goalTxt = donationGoalField.getText().trim();
        double goal = 0;
        if (!goalTxt.isEmpty()) {
          try {
            goal = Double.parseDouble(goalTxt.replace(",", ""));
          } catch (NumberFormatException ex) {
            showError("Donation goal must be a valid number.");
            return;
          }
          if (goal < 0) {
            showError("Donation goal cannot be negative.");
            return;
          }
        }
        n.setDonationGoal(goal);
      }
    }
    if (AlumniDAO.createNotification(n)) {
      success = true;
      JOptionPane.showMessageDialog(this, "\u2705  Notice posted!", "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
      dispose();
    } else {
      showError("Failed to post. Try again.");
    }
  }

  private void showError(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Error",
                                  JOptionPane.ERROR_MESSAGE);
  }

  public boolean isSuccess() { return success; }

  static class RoundedBorder implements Border {
    private final Color color;
    private final int thickness, arc;
    RoundedBorder(Color c, int t, int a) {
      color = c;
      thickness = t;
      arc = a;
    }
    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(thickness + 4, thickness + 4, thickness + 4,
                        thickness + 4);
    }
    @Override
    public boolean isBorderOpaque() {
      return false;
    }
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w,
                            int h) {
      Graphics2D g2 = (Graphics2D)g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(color);
      g2.setStroke(new BasicStroke(thickness));
      g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, arc, arc);
      g2.dispose();
    }
  }
}