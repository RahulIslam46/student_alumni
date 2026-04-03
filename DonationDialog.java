import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * DonationDialog — lets an alumni donate money towards an event.
 * Methods: bKash (mobile + TxnID required) or Cash (no mobile/TxnID needed).
 * Admin reviews each submission and marks it confirmed / rejected.
 */
public class DonationDialog extends JDialog {
    // ── theme ──────────────────────────────────────────────────────────────
    private static final Color BG_TOP = new Color(22, 22, 35);
    private static final Color BG_BOT = new Color(15, 15, 25);
    private static final Color ACCENT = new Color(16, 185, 129);
    private static final Color ACCENT_DK = new Color(5, 150, 105);
    private static final Color CARD_BG = new Color(35, 35, 55);
    private static final Color BORDER_C = new Color(70, 70, 110);
    private static final Color TEXT_W = Color.WHITE;
    private static final Color TEXT_G = new Color(156, 163, 175);
    private static final Font F_LBL = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font F_INPUT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 24);

    // ── data ───────────────────────────────────────────────────────────────
    private final int notificationId;
    private final int donorUserId;
    private final String eventTitle;
    private final double donationGoal;
    private final double raisedSoFar;

    // ── inputs ─────────────────────────────────────────────────────────────
    private JTextField amountField;
    private JComboBox<String> methodCombo;
    private JTextField accountField;
    private JTextField last4Field;
    private JTextField txnField;
    private JTextArea msgArea;
    private JPanel accountRow;
    private JPanel last4Row;
    private JPanel txnRow;
    private JPanel instrCard;
    private boolean submitted = false;

    // ───────────────────────────────────────────────────────────────────────
    public DonationDialog(JFrame parent, int notificationId, int donorUserId,
        String eventTitle, double donationGoal,
        double raisedSoFar) {
        super(parent, "Donate to Event", true);
        this.notificationId = notificationId;
        this.donorUserId = donorUserId;
        this.eventTitle = eventTitle;
        this.donationGoal = donationGoal;
        this.raisedSoFar = raisedSoFar;
        buildUI();
        setSize(560, 660);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // ── BUILD UI ───────────────────────────────────────────────────────────
    private void buildUI() {
        JPanel bg = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOT));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.add(buildHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(buildForm());
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        bg.add(scroll, BorderLayout.CENTER);
        bg.add(buildFooter(), BorderLayout.SOUTH);
        add(bg);
    }

    // ── HEADER ─────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT_DK));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        hdr.setPreferredSize(new Dimension(0, donationGoal > 0 ? 130 : 100));
        hdr.setBorder(BorderFactory.createEmptyBorder(0, 26, 0, 26));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;

        JLabel h1 = new JLabel("💚  Donate to Event");
        h1.setFont(F_TITLE);
        h1.setForeground(TEXT_W);
        hdr.add(h1, gc);

        gc.gridy = 1;
        JLabel h2 = new JLabel(truncate(eventTitle, 58));
        h2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        h2.setForeground(new Color(187, 247, 208));
        hdr.add(h2, gc);

        if (donationGoal > 0) {
            gc.gridy = 2;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(10, 0, 0, 0);
            hdr.add(buildProgressPane(), gc);
        }
        return hdr;
    }

    private JPanel buildProgressPane() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        double pct = Math.min(100.0, raisedSoFar / donationGoal * 100);
        JLabel lbl =
            new JLabel(String.format("৳%.0f raised of ৳%.0f goal  (%.0f%%)",
                raisedSoFar, donationGoal, pct));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(187, 247, 208));
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) pct);
        bar.setStringPainted(false);
        bar.setForeground(new Color(255, 255, 255, 200));
        bar.setBackground(new Color(0, 0, 0, 80));
        bar.setPreferredSize(new Dimension(0, 8));
        bar.setBorder(BorderFactory.createEmptyBorder());
        p.add(lbl, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        return p;
    }

    // ── FORM ───────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // Amount
        p.add(lbl("Donation Amount (BDT) *"));
        p.add(gap(6));
        amountField = field("e.g. 500");
        p.add(amountField);
        p.add(gap(16));

        // Method — only bKash and Cash
        p.add(lbl("Payment Method *"));
        p.add(gap(6));
        methodCombo = combo(new String[] {"bKash", "Cash"});
        methodCombo.addActionListener(e -> onMethodChanged());
        p.add(methodCombo);
        p.add(gap(16));

        // Instructions card (dynamic)
        instrCard = buildInstructionsCard("bKash");
        p.add(instrCard);
        p.add(gap(16));

        // Account / mobile  (hidden for Cash)
        accountRow = new JPanel();
        accountRow.setLayout(new BoxLayout(accountRow, BoxLayout.Y_AXIS));
        accountRow.setOpaque(false);
        accountRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        accountRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        accountRow.add(lbl("Your bKash Mobile Number *"));
        accountRow.add(gap(6));
        accountField = field("e.g. 01XXXXXXXXX");
        accountRow.add(accountField);
        p.add(accountRow);
        p.add(gap(16));

        // Transaction ID  (hidden for Cash)
        txnRow = new JPanel();
        txnRow.setLayout(new BoxLayout(txnRow, BoxLayout.Y_AXIS));
        txnRow.setOpaque(false);
        txnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        txnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        txnRow.add(lbl("bKash Transaction ID *"));
        txnRow.add(gap(6));
        txnField = field("e.g. TRX1234567890");
        txnRow.add(txnField);
        txnRow.add(gap(4));
        JLabel txnHint =
            new JLabel("  Copy the TrxID from your bKash confirmation SMS.");
        txnHint.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        txnHint.setForeground(TEXT_G);
        txnHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        txnRow.add(txnHint);
        p.add(txnRow);
        p.add(gap(16));

        // Last 4 digits check (hidden for Cash)
        last4Row = new JPanel();
        last4Row.setLayout(new BoxLayout(last4Row, BoxLayout.Y_AXIS));
        last4Row.setOpaque(false);
        last4Row.setAlignmentX(Component.LEFT_ALIGNMENT);
        last4Row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        last4Row.add(lbl("Last 4 digits of bKash number *"));
        last4Row.add(gap(6));
        last4Field = field("e.g. 1234");
        last4Row.add(last4Field);
        p.add(last4Row);
        p.add(gap(16));

        // Message
        p.add(lbl("Message (optional)"));
        p.add(gap(6));
        msgArea = new JTextArea(3, 30);
        msgArea.setFont(F_INPUT);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setForeground(TEXT_W);
        msgArea.setBackground(CARD_BG);
        msgArea.setCaretColor(TEXT_W);
        msgArea.setBorder(BorderFactory.createCompoundBorder(
            new RndBorder(BORDER_C, 2, 10),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JScrollPane ms = new JScrollPane(msgArea);
        ms.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        ms.setAlignmentX(Component.LEFT_ALIGNMENT);
        ms.setBorder(null);
        p.add(ms);

        return p;
    }

    private JPanel buildInstructionsCard(String method) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(20, 83, 45));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setBorder(BorderFactory.createCompoundBorder(
            new RndBorder(new Color(34, 197, 94), 2, 10),
            BorderFactory.createEmptyBorder(12, 14, 14, 14)));

        String[] lines;
        if ("Cash".equals(method)) {
            lines = new String[] {"1. Bring the cash amount to the event organizer.",
                "2. The organizer will record your donation.",
                "3. Enter the amount below and click Submit.",
                "4. Admin will confirm once cash is received."};
        } else {
            lines =
                new String[] {"1. Open your bKash app and send money.",
                    "2. Send to:  01XXXXXXXXX  (Event Fund)",
                    "3. Note the Transaction ID from the confirmation SMS.",
                    "4. Enter your bKash number & TxnID below.",
                    "5. Click Submit — admin will confirm your donation."};
        }

        JLabel hd = new JLabel("📋  How to pay  (" + method + ")");
        hd.setFont(new Font("Segoe UI", Font.BOLD, 15));
        hd.setForeground(new Color(187, 247, 208));
        hd.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(hd);
        card.add(gap(6));
        for (String line : lines) {
            JLabel l = new JLabel(line);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            l.setForeground(new Color(187, 247, 208));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(l);
            card.add(gap(2));
        }
        return card;
    }

    // ── FOOTER ─────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel fp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 13));
        fp.setOpaque(false);
        fp.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
        JButton cancel = btn("  Cancel  ", new Color(239, 68, 68), TEXT_W);
        cancel.addActionListener(e -> dispose());
        JButton submit = btn("  💚 Submit Donation  ", ACCENT, TEXT_W);
        submit.addActionListener(e -> handleSubmit());
        fp.add(cancel);
        fp.add(submit);
        return fp;
    }

    // ── LOGIC ──────────────────────────────────────────────────────────────
    private void onMethodChanged() {
        boolean isCash = "Cash".equals(methodCombo.getSelectedItem());

        // swap instructions card
        Container parent = instrCard.getParent();
        if (parent != null) {
            int idx = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                if (parent.getComponent(i) == instrCard) {
                    idx = i;
                    break;
                }
            }
            parent.remove(instrCard);
            instrCard = buildInstructionsCard(isCash ? "Cash" : "bKash");
            parent.add(instrCard, idx);
        }

        // show / hide account + txn rows
        accountRow.setVisible(!isCash);
        txnRow.setVisible(!isCash);
        last4Row.setVisible(!isCash);

        // update labels
        if (!isCash) {
            ((JLabel) accountRow.getComponent(0))
                .setText("Your bKash Mobile Number *");
            ((JLabel) txnRow.getComponent(0)).setText("bKash Transaction ID *");
        }

        SwingUtilities.getWindowAncestor(methodCombo).revalidate();
        SwingUtilities.getWindowAncestor(methodCombo).repaint();
    }

    private void handleSubmit() {
        String amtTxt = amountField.getText().trim();
        String method = (String) methodCombo.getSelectedItem();
        boolean isCash = "Cash".equals(method);

        // validate amount
        if (amtTxt.isEmpty()) {
            err("Please enter the donation amount.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amtTxt.replace(",", ""));
        } catch (NumberFormatException ex) {
            err("Amount must be a valid number.");
            return;
        }
        if (amount <= 0) {
            err("Amount must be greater than 0.");
            return;
        }

        String account = isCash ? "" : accountField.getText().trim();
        String txn = isCash ? "" : txnField.getText().trim();
        String last4 = isCash ? "" : last4Field.getText().trim();
        String msg = msgArea.getText().trim();

        // validate bKash fields
        if (!isCash) {
            if (account.isEmpty()) {
                err("Please enter your bKash mobile number.");
                return;
            }
            if (!account.matches("^01\\d{9}$")) {
                err("bKash number must be 11 digits and start with 01.");
                return;
            }
            if (txn.isEmpty()) {
                err("Please enter the bKash Transaction ID.");
                return;
            }
            if (txn.length() < 8) {
                err("Transaction ID looks too short.");
                return;
            }
            if (last4.isEmpty() || !last4.matches("\\d{4}")) {
                err("Please enter the last 4 digits of your bKash number.");
                return;
            }
            if (!account.endsWith(last4)) {
                err("Last 4 digits do not match your bKash number.");
                return;
            }

            msg = (msg.isEmpty() ? "" : msg + " | ") + "bKash last4: " + last4;
        }

        // save
        boolean ok = AlumniDAO.saveDonation(notificationId, donorUserId, amount,
            method, account, txn, msg);

        if (ok) {
            submitted = true;
            String detail =
                isCash
                ? "Bring the cash to the event organizer.<br>Admin will confirm once received."
                : "Your bKash payment is pending admin review.<br>You will be notified once confirmed.";
            JOptionPane.showMessageDialog(
                this,
                "<html><b>Thank you for your donation! 💚</b><br>" + detail + "</html>",
                "Donation Submitted", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            err("Could not save your donation. Please try again.");
        }
    }

    public boolean isSubmitted() { return submitted; }

    // ── HELPERS ────────────────────────────────────────────────────────────
    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LBL);
        l.setForeground(TEXT_W);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField field(String ph) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(TEXT_G);
                    g2.setFont(getFont().deriveFont(Font.ITALIC, 13f));
                    Insets ins = getInsets();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(ph, ins.left + 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                }
            }
        };
        f.setFont(F_INPUT);
        f.setForeground(TEXT_W);
        f.setBackground(CARD_BG);
        f.setCaretColor(TEXT_W);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RndBorder(BORDER_C, 2, 10),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new RndBorder(ACCENT, 2, 10),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
                f.repaint();
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    new RndBorder(BORDER_C, 2, 10),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
                f.repaint();
            }
        });
        return f;
    }

    private JComboBox<String> combo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_INPUT);
        cb.setBackground(CARD_BG);
        cb.setForeground(TEXT_W);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return cb;
    }

    private JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
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
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(
            new Dimension(Math.max(160, text.length() * 9 + 30), 44));
        return b;
    }

    private Component gap(int h) { return Box.createVerticalStrut(h); }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Error",
            JOptionPane.ERROR_MESSAGE);
    }

    private static String truncate(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max - 3) + "..."
                                               : s;
    }

    // ── border ─────────────────────────────────────────────────────────────
    static class RndBorder implements Border {
        private final Color c;
        private final int t, a;
        RndBorder(Color c, int t, int a) {
            this.c = c;
            this.t = t;
            this.a = a;
        }
        @Override
        public Insets getBorderInsets(Component cmp) {
            return new Insets(t + 4, t + 4, t + 4, t + 4);
        }
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
        @Override
        public void paintBorder(Component cmp, Graphics g, int x, int y, int w,
            int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(t));
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, a, a);
            g2.dispose();
        }
    }
}
