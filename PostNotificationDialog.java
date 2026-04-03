import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import javax.swing.*;
import javax.swing.border.*;

public class PostNotificationDialog extends JDialog {
    // Palette
    private static final Color PANEL_BG = new Color(22, 22, 30);
    private static final Color BTN_RED = new Color(239, 68, 68);
    private static final Color BTN_BLUE = new Color(59, 130, 246);

    // Brighter center panel theme (form + table)
    private static final Color FORM_PANEL_BG = new Color(25, 27, 38);
    private static final Color FORM_INPUT_BG = new Color(38, 41, 58);
    private static final Color FORM_BORDER = new Color(92, 101, 146);
    private static final Color FORM_TEXT_PRIMARY = new Color(244, 247, 255);
    private static final Color FORM_TEXT_SECONDARY = new Color(182, 192, 223);
    private static final Color FORM_TEXT_LABEL = new Color(222, 230, 255);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 13);

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
    private static final DateTimeFormatter DEADLINE_INPUT_FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    public PostNotificationDialog(JFrame parent, int userId) {
        super(parent, "Post New Notice", true);
        this.currentUserId = userId;

        setSize(760, 660);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(FORM_PANEL_BG);

        add(buildCenter(), BorderLayout.CENTER);
    }

    private JPanel buildCenter() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(FORM_PANEL_BG);

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

        JLabel sub = new JLabel("Minimal form for quick posting.");
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

        detailsTitle = new JLabel("Specific Details");
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

        JButton cancelBtn = makeBtn("Cancel", BTN_RED);
        cancelBtn.addActionListener(e -> dispose());
        JButton postBtn = makeBtn("Post Notice", BTN_BLUE);
        postBtn.addActionListener(e -> postNotification());

        btnRow.add(cancelBtn);
        btnRow.add(postBtn);
        formCard.add(btnRow);

        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(FORM_PANEL_BG);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.getHorizontalScrollBar().setUnitIncrement(16);

        outer.add(formScroll, BorderLayout.CENTER);
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

        card.add(fieldLabel("Application Deadline (DD-MM-YYYY)"));
        card.add(Box.createVerticalStrut(4));
        deadlineField = makeTextField("e.g. 31-12-2026");
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
        String deadlineIso = "";
        if (st == 0) {
            if (companyField.getText().trim().isEmpty() || positionField.getText().trim().isEmpty()) {
                showError("Company name and job position are required.");
                return;
            }
            String dl = deadlineField.getText().trim();
            if (!dl.isEmpty()) {
                try {
                    deadlineIso = LocalDate.parse(dl, DEADLINE_INPUT_FORMATTER).toString();
                } catch (DateTimeParseException e) {
                    showError("Deadline must be in DD-MM-YYYY format.");
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
            n.setApplicationDeadline(deadlineIso);
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
                    new LineBorder(FORM_BORDER, 1), BorderFactory.createEmptyBorder(7, 10, 7, 10)));
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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isSuccess() {
        return success;
    }
}
