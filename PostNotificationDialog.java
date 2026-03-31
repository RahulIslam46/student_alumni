import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PostNotificationDialog extends JDialog {
    // Palette
    private static final Color BG = new Color(18, 18, 24);
    private static final Color SIDEBAR_BG = new Color(13, 13, 18);
    private static final Color PANEL_BG = new Color(22, 22, 30);
    private static final Color CARD_BG = new Color(28, 28, 38);
    private static final Color ACCENT = new Color(99, 87, 210);
    private static final Color ACCENT2 = new Color(79, 172, 254);
    private static final Color BTN_GREEN = new Color(52, 199, 130);
    private static final Color BTN_RED = new Color(239, 68, 68);
    private static final Color BTN_BLUE = new Color(59, 130, 246);
    private static final Color BORDER = new Color(40, 40, 55);
    private static final Color TXT_PRIMARY = new Color(230, 230, 235);
    private static final Color TXT_SECONDARY = new Color(140, 140, 160);
    private static final Color TXT_LABEL = new Color(200, 200, 215);
    private static final Color INPUT_BG = new Color(14, 14, 20);
    private static final Color NAV_ACTIVE = new Color(99, 87, 210, 60);

    // Brighter center panel theme (form + table)
    private static final Color FORM_PANEL_BG = new Color(25, 27, 38);
    private static final Color FORM_TABLE_BG = new Color(32, 35, 49);
    private static final Color FORM_INPUT_BG = new Color(38, 41, 58);
    private static final Color FORM_BORDER = new Color(92, 101, 146);
    private static final Color FORM_TEXT_PRIMARY = new Color(244, 247, 255);
    private static final Color FORM_TEXT_SECONDARY = new Color(182, 192, 223);
    private static final Color FORM_TEXT_LABEL = new Color(222, 230, 255);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_NAV = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_LOGO = new Font("Segoe UI", Font.BOLD, 14);

    // Model
    private final int currentUserId;
    private boolean success = false;

    // Form fields
    private JComboBox<String> typeCombo;
    private JComboBox<String> priorityCombo;
    private JTextField titleField;
    private JTextArea contentArea;

    private JTextField companyField;
    private JTextField positionField;
    private JTextField locationField;
    private JComboBox<String> jobTypeCombo;
    private JTextField salaryField;
    private JTextField deadlineField;
    private JTextField urlField;

    private JTextField eventDateField;
    private JTextField eventLocationField;
    private JCheckBox donationsCheckbox;
    private JTextField donationGoalField;

    private CardLayout detailsLayout;
    private JPanel detailsHost;
    private JLabel detailsTitle;

    private JButton improveBtn;

    public PostNotificationDialog(JFrame parent, int userId) {
        super(parent, "Post New Notice", true);
        this.currentUserId = userId;

        setSize(980, 720);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildCenter(), BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(SIDEBAR_BG);
        p.setPreferredSize(new Dimension(180, 0));
        p.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        logo.setBackground(SIDEBAR_BG);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        logo.setPreferredSize(new Dimension(180, 60));

        JLabel logoIcon = new JLabel("CN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_LOGO);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("CN", (getWidth() - fm.stringWidth("CN")) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        logoIcon.setPreferredSize(new Dimension(34, 34));
        logo.add(Box.createVerticalStrut(14));
        logo.add(logoIcon);
        p.add(logo);
        p.add(hLine());
        p.add(Box.createVerticalStrut(10));

        String[] labels = {"Dashboard", "Post Notice", "All Notices", "Analytics", "Team Members", "Settings"};
        String[] icons = {"[]", "@", "=", "*", "#", "S"};
        boolean[] active = {false, true, false, false, false, false};

        for (int i = 0; i < labels.length; i++) {
            p.add(navItem(icons[i], labels[i], active[i]));
        }
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel navItem(String icon, String label, boolean isActive) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.setBackground(isActive ? NAV_ACTIVE : SIDEBAR_BG);

        if (isActive) {
            row.setBorder(new MatteBorder(0, 3, 0, 0, ACCENT));
        } else {
            row.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        }

        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ic.setForeground(isActive ? ACCENT : TXT_SECONDARY);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_NAV);
        lbl.setForeground(isActive ? TXT_PRIMARY : TXT_SECONDARY);

        row.add(ic);
        row.add(lbl);
        return row;
    }

    private JPanel buildCenter() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(BG);

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(FORM_PANEL_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, FORM_BORDER),
            BorderFactory.createEmptyBorder(20, 22, 16, 22)));

        JLabel title = new JLabel("Post a New Notice");
        title.setFont(FONT_TITLE);
        title.setForeground(FORM_TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Fill in details below to distribute this notice to members.");
        sub.setFont(FONT_SMALL);
        sub.setForeground(FORM_TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(title);
        formCard.add(Box.createVerticalStrut(3));
        formCard.add(sub);
        formCard.add(Box.createVerticalStrut(18));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 14, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        JPanel typePanel = new JPanel();
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
        typePanel.setOpaque(false);
        typeCombo = makeCombo(new String[] {"Job Posting", "Event", "Announcement"});
        typeCombo.addActionListener(e -> toggleFieldVisibility());
        typePanel.add(fieldLabel("Notice Type"));
        typePanel.add(Box.createVerticalStrut(4));
        typePanel.add(typeCombo);

        JPanel priPanel = new JPanel();
        priPanel.setLayout(new BoxLayout(priPanel, BoxLayout.Y_AXIS));
        priPanel.setOpaque(false);
        priorityCombo = makeCombo(new String[] {"Normal", "High", "Urgent"});
        priPanel.add(fieldLabel("Priority"));
        priPanel.add(Box.createVerticalStrut(4));
        priPanel.add(priorityCombo);

        row1.add(typePanel);
        row1.add(priPanel);
        formCard.add(row1);
        formCard.add(Box.createVerticalStrut(14));

        formCard.add(fieldLabel("Title*"));
        formCard.add(Box.createVerticalStrut(4));
        titleField = makeTextField("Enter notice title...");
        formCard.add(titleField);
        formCard.add(Box.createVerticalStrut(14));

        formCard.add(fieldLabel("Description / Details*"));
        formCard.add(Box.createVerticalStrut(4));
        contentArea = new JTextArea(4, 0);
        contentArea.setFont(FONT_INPUT);
        contentArea.setForeground(FORM_TEXT_PRIMARY);
        contentArea.setBackground(FORM_INPUT_BG);
        contentArea.setCaretColor(FORM_TEXT_PRIMARY);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane dScroll = new JScrollPane(contentArea);
        dScroll.setBorder(new LineBorder(FORM_BORDER, 1));
        dScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        dScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        dScroll.getViewport().setBackground(FORM_INPUT_BG);
        formCard.add(dScroll);
        formCard.add(Box.createVerticalStrut(14));

        detailsTitle = new JLabel("Specific Details (Job Posting)");
        detailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        detailsTitle.setForeground(FORM_TEXT_PRIMARY);
        detailsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(detailsTitle);
        formCard.add(Box.createVerticalStrut(10));

        detailsLayout = new CardLayout();
        detailsHost = new JPanel(detailsLayout);
        detailsHost.setOpaque(false);
        detailsHost.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailsHost.add(buildJobDetailsCard(), "job");
        detailsHost.add(buildEventDetailsCard(), "event");
        detailsHost.add(buildAnnouncementDetailsCard(), "announcement");
        formCard.add(detailsHost);
        formCard.add(Box.createVerticalStrut(18));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        improveBtn = makeBtn("Improve with AI", BTN_GREEN);
        improveBtn.addActionListener(e -> openNoticeAiChat());
        JButton cancelBtn = makeBtn("Cancel", BTN_RED);
        cancelBtn.addActionListener(e -> dispose());
        JButton postBtn = makeBtn("Post Notice", BTN_BLUE);
        postBtn.addActionListener(e -> postNotification());

        btnRow.add(improveBtn);
        btnRow.add(cancelBtn);
        btnRow.add(postBtn);
        formCard.add(btnRow);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(FORM_PANEL_BG);
        tableCard.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JLabel tblTitle = new JLabel("Recent Notices List");
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblTitle.setForeground(FORM_TEXT_PRIMARY);
        tblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        tableCard.add(tblTitle, BorderLayout.NORTH);

        String[] cols = {"Title", "Type", "Priority", "Date Posted", "Posted By"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(FORM_TEXT_PRIMARY);
        table.setBackground(FORM_TABLE_BG);
        table.setGridColor(FORM_BORDER);
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setForeground(FORM_TEXT_SECONDARY);
        table.getTableHeader().setBackground(FORM_PANEL_BG);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, FORM_BORDER));
        table.setSelectionBackground(NAV_ACTIVE);
        table.setSelectionForeground(FORM_TEXT_PRIMARY);

        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                String val = String.valueOf(v);
                if ("high".equalsIgnoreCase(val)) {
                    lbl.setForeground(new Color(251, 191, 36));
                } else if ("urgent".equalsIgnoreCase(val)) {
                    lbl.setForeground(BTN_RED);
                } else {
                    lbl.setForeground(BTN_GREEN);
                }
                lbl.setBackground(sel ? NAV_ACTIVE : FORM_TABLE_BG);
                lbl.setOpaque(true);
                return lbl;
            }
        });

        loadRecentNotices(model);

        JScrollPane tScroll = new JScrollPane(table);
        tScroll.setBorder(new LineBorder(FORM_BORDER, 1));
        tScroll.getViewport().setBackground(FORM_TABLE_BG);
        tableCard.add(tScroll, BorderLayout.CENTER);

        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(FORM_PANEL_BG);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.getHorizontalScrollBar().setUnitIncrement(16);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formScroll, tableCard);
        split.setResizeWeight(0.72);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setContinuousLayout(true);

        outer.add(split, BorderLayout.CENTER);
        toggleFieldVisibility();
        return outer;
    }

    private JPanel buildJobDetailsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);

        card.add(fieldLabel("Company Name*"));
        card.add(Box.createVerticalStrut(4));
        companyField = makeTextField("e.g. Google, Microsoft...");
        card.add(companyField);
        card.add(Box.createVerticalStrut(10));

        card.add(fieldLabel("Job Position*"));
        card.add(Box.createVerticalStrut(4));
        positionField = makeTextField("e.g. Software Engineer...");
        card.add(positionField);
        card.add(Box.createVerticalStrut(10));

        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(fieldLabel("Location"));
        left.add(Box.createVerticalStrut(4));
        locationField = makeTextField("e.g. Dhaka, Remote");
        left.add(locationField);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.add(fieldLabel("Salary Range"));
        right.add(Box.createVerticalStrut(4));
        salaryField = makeTextField("e.g. 50,000 - 70,000 BDT");
        right.add(salaryField);

        row.add(left);
        row.add(right);
        card.add(row);
        card.add(Box.createVerticalStrut(10));

        card.add(fieldLabel("Job Type"));
        card.add(Box.createVerticalStrut(4));
        jobTypeCombo = makeCombo(new String[] {"Full-time", "Part-time", "Contract", "Internship", "Remote"});
        card.add(jobTypeCombo);
        card.add(Box.createVerticalStrut(10));

        card.add(fieldLabel("Application Deadline (YYYY-MM-DD)"));
        card.add(Box.createVerticalStrut(4));
        deadlineField = makeTextField("e.g. 2026-12-31");
        card.add(deadlineField);
        card.add(Box.createVerticalStrut(10));

        card.add(fieldLabel("Application URL"));
        card.add(Box.createVerticalStrut(4));
        urlField = makeTextField("https://...");
        card.add(urlField);

        return card;
    }

    private JPanel buildEventDetailsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);

        card.add(fieldLabel("Event Date & Time (YYYY-MM-DD HH:MM)"));
        card.add(Box.createVerticalStrut(4));
        eventDateField = makeTextField("e.g. 2026-03-15 10:00");
        card.add(eventDateField);
        card.add(Box.createVerticalStrut(10));

        card.add(fieldLabel("Event Location*"));
        card.add(Box.createVerticalStrut(4));
        eventLocationField = makeTextField("e.g. HSTU Auditorium");
        card.add(eventLocationField);
        card.add(Box.createVerticalStrut(10));

        donationsCheckbox = new JCheckBox("Enable Donations for this event");
        donationsCheckbox.setFont(FONT_LABEL);
        donationsCheckbox.setForeground(new Color(120, 220, 175));
        donationsCheckbox.setBackground(PANEL_BG);
        donationsCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        donationsCheckbox.addActionListener(e -> donationGoalField.setEnabled(donationsCheckbox.isSelected()));
        card.add(donationsCheckbox);
        card.add(Box.createVerticalStrut(8));

        card.add(fieldLabel("Donation Goal (BDT)"));
        card.add(Box.createVerticalStrut(4));
        donationGoalField = makeTextField("e.g. 10000 (0 = open)");
        donationGoalField.setEnabled(false);
        card.add(donationGoalField);

        return card;
    }

    private JPanel buildAnnouncementDetailsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        JLabel lbl = new JLabel("No extra fields required for announcement.");
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(FORM_TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);
        return card;
    }

    private void loadRecentNotices(DefaultTableModel model) {
        model.setRowCount(0);
        List<Notification> notices = AlumniDAO.getAllNotifications(currentUserId);
        int shown = 0;
        for (Notification n : notices) {
            model.addRow(new Object[] {
                n.getTitle(),
                n.getType(),
                n.getPriority(),
                timeAgo(n.getCreatedAt()),
                n.getPostedByName() == null ? "Unknown" : n.getPostedByName()});
            shown++;
            if (shown >= 12) {
                break;
            }
        }
    }

    private String timeAgo(java.sql.Timestamp ts) {
        if (ts == null) {
            return "-";
        }
        long diffMs = System.currentTimeMillis() - ts.getTime();
        long mins = diffMs / 60000;
        if (mins < 1) {
            return "just now";
        }
        if (mins < 60) {
            return mins + " min ago";
        }
        long hrs = mins / 60;
        if (hrs < 24) {
            return hrs + " hrs ago";
        }
        long days = hrs / 24;
        if (days < 7) {
            return days + " days ago";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(ts);
    }

    private void openNoticeAiChat() {
        String type = typeCombo.getSelectedItem() != null
            ? typeCombo.getSelectedItem().toString()
            : "Notice";
        String context = "Task: Improve a notice draft and provide alternatives.\n"
            + "Notice type: " + type + "\n"
            + "Current title: " + titleField.getText().trim() + "\n"
            + "Current description: " + contentArea.getText().trim() + "\n"
            + "If useful, respond using:\n"
            + "TITLE: ...\nBODY: ...\n"
            + "and optionally provide 2-3 alternatives.";

        AiChatDialog.openOrFocus(this,
            "Notice AI Chat", context, this::applyAiDraft);
    }

    private void toggleFieldVisibility() {
        int idx = typeCombo.getSelectedIndex();
        if (idx == 0) {
            detailsLayout.show(detailsHost, "job");
            detailsTitle.setText("Specific Details (Job Posting)");
        } else if (idx == 1) {
            detailsLayout.show(detailsHost, "event");
            detailsTitle.setText("Specific Details (Event)");
        } else {
            detailsLayout.show(detailsHost, "announcement");
            detailsTitle.setText("Specific Details (Announcement)");
        }
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
            if (companyField.getText().trim().isEmpty() || positionField.getText().trim().isEmpty()) {
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
            n.setJobType((String) jobTypeCombo.getSelectedItem());
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
            JOptionPane.showMessageDialog(this, "Notice posted!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            showError("Failed to post. Try again.");
        }
    }

    private void applyAiDraft(String aiText) {
        if (aiText == null || aiText.trim().isEmpty()) {
            return;
        }

        String norm = aiText.replace("\r\n", "\n");
        int ti = norm.toUpperCase().indexOf("TITLE:");
        int bi = norm.toUpperCase().indexOf("BODY:");

        if (ti >= 0 && bi > ti) {
            String t = norm.substring(ti + 6, bi).trim();
            String b = norm.substring(bi + 5).trim();
            if (!t.isEmpty()) {
                titleField.setText(t);
            }
            if (!b.isEmpty()) {
                contentArea.setText(b);
            }
            return;
        }

        contentArea.setText(aiText.trim());
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(FORM_TEXT_LABEL);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makeTextField(String ph) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(FORM_TEXT_SECONDARY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    Insets ins = getInsets();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(ph, ins.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                }
            }
        };

        f.setFont(FONT_INPUT);
        f.setForeground(FORM_TEXT_PRIMARY);
        f.setBackground(FORM_INPUT_BG);
        f.setCaretColor(FORM_TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(FORM_BORDER, 1), BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);

        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ACCENT2, 1), BorderFactory.createEmptyBorder(7, 10, 7, 10)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(FORM_BORDER, 1), BorderFactory.createEmptyBorder(7, 10, 7, 10)));
            }
        });

        return f;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_INPUT);
        cb.setForeground(FORM_TEXT_PRIMARY);
        cb.setBackground(FORM_INPUT_BG);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        cb.setBorder(new LineBorder(FORM_BORDER, 1));
        return cb;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 36));
        return btn;
    }

    private JButton makeSmallBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(TXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JSeparator hLine() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setBackground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isSuccess() {
        return success;
    }
}
