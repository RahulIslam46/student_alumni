import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.Point2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AiChatDialog extends JDialog {
    private static final Map<String, AiChatDialog> OPEN_CHATS = new HashMap<>();

    private final JPanel messagesPanel = new JPanel();
    private final JScrollPane messagesScroll;
    private final JTextArea inputArea = new JTextArea(3, 40);
    private final JButton sendBtn = new JButton("Send");
    private final JButton copyBtn = new JButton("Copy Last Reply");
    private final JButton pasteBtn = new JButton("Paste To Form");
    private String lastReply = "";
    private JPanel typingBubble;
    private Timer typingTimer;
    private int typingDots = 1;
    private final Consumer<String> onPaste;
    private final String context;
    private final Color appBg = new Color(4, 4, 6);
    private final Color panelBg = new Color(10, 10, 14);
    private final Color redAccent = new Color(255, 96, 112);

    public static void openOrFocus(Component parentComponent, String title,
        String context,
        Consumer<String> onPaste) {
        String key = System.identityHashCode(parentComponent) + "::" + title;
        AiChatDialog existing = OPEN_CHATS.get(key);
        if (existing != null && existing.isDisplayable()) {
            existing.toFront();
            existing.requestFocus();
            return;
        }

        Window owner =
            parentComponent != null ? SwingUtilities.getWindowAncestor(parentComponent)
                                    : null;
        AiChatDialog dialog =
            new AiChatDialog(owner, parentComponent, title, context, onPaste);
        OPEN_CHATS.put(key, dialog);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                OPEN_CHATS.remove(key);
            }
        });
        dialog.setVisible(true);
        SwingUtilities.invokeLater(() -> dialog.focusInput());
    }

    public AiChatDialog(Window owner, Component parentComponent, String title,
        String context,
        Consumer<String> onPaste) {
        // Keep modeless and owned by caller window so it can accept input while
        // related dialogs are open.
        super(owner, title, Dialog.ModalityType.MODELESS);
        this.onPaste = onPaste;
        this.context = context;
        setSize(760, 620);
        setMinimumSize(new Dimension(680, 520));
        setLocationRelativeTo(parentComponent);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setFocusableWindowState(true);

        JPanel root = new MoodBackgroundPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.setBackground(appBg);

        JLabel head = new JLabel("AI Assistant Chat");
        head.setFont(new Font("Segoe UI", Font.BOLD, 22));
        head.setForeground(new Color(246, 246, 246));
        root.add(head, BorderLayout.NORTH);

        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setOpaque(false);
        messagesPanel.setBackground(appBg);
        messagesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        messagesScroll = new JScrollPane(messagesPanel);
        messagesScroll.setOpaque(false);
        messagesScroll.getViewport().setOpaque(false);
        messagesScroll.setBorder(
            BorderFactory.createLineBorder(new Color(45, 18, 24), 1));
        messagesScroll.getVerticalScrollBar().setBackground(new Color(12, 12, 16));
        messagesScroll.getHorizontalScrollBar().setBackground(new Color(12, 12, 16));
        messagesScroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(messagesScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);

        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputArea.setBackground(new Color(14, 14, 18));
        inputArea.setForeground(new Color(240, 240, 240));
        inputArea.setCaretColor(new Color(240, 240, 240));
        inputArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputArea.setFocusable(true);
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(
            BorderFactory.createLineBorder(new Color(70, 18, 28), 1));
        inputScroll.setOpaque(false);
        inputScroll.getViewport().setOpaque(false);
        bottom.add(inputScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton closeBtn = new JButton("Close");
        styleActionButton(copyBtn, false);
        styleActionButton(pasteBtn, false);
        styleActionButton(sendBtn, true);
        styleActionButton(closeBtn, false);

        sendBtn.addActionListener(e -> sendMessage());
        copyBtn.addActionListener(e -> copyLastReply());
        pasteBtn.addActionListener(e -> {
            if (this.onPaste != null && !lastReply.isBlank()) {
                this.onPaste.accept(lastReply);
            }
        });
        closeBtn.addActionListener(e -> dispose());

        actions.add(copyBtn);
        actions.add(pasteBtn);
        actions.add(sendBtn);
        actions.add(closeBtn);
        bottom.add(actions, BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);
        add(root);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                inputArea.requestFocusInWindow();
            }
        });

        appendSystemMessage("Chat started. Type request and press Send.");
        appendSystemMessage("Tip: Ask for 3 better options and choose/copy one.");
    }

    private void focusInput() {
        toFront();
        requestFocus();
        inputArea.requestFocusInWindow();
        inputArea.setCaretPosition(inputArea.getText().length());
    }

    private void sendMessage() {
        String userText = inputArea.getText().trim();
        if (userText.isEmpty())
            return;

        addMessageBubble("You", userText, true, new Color(28, 10, 14),
            new Color(248, 220, 225));
        inputArea.setText("");
        sendBtn.setEnabled(false);
        inputArea.setEnabled(false);
        sendBtn.setText("Sending...");
        showTypingIndicator();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return LocalAiService.chatWithContext(context, userText);
            }

            @Override
            protected void done() {
                try {
                    hideTypingIndicator();
                    lastReply = get();
                    addMessageBubble("AI", lastReply, false,
                        panelBg, new Color(230, 230, 230));
                } catch (Exception ex) {
                    hideTypingIndicator();
                    appendSystemMessage("AI error: " + ex.getMessage()
                        + " | Check ollama serve and model.");
                } finally {
                    sendBtn.setEnabled(true);
                    inputArea.setEnabled(true);
                    inputArea.requestFocusInWindow();
                    sendBtn.setText("Send");
                }
            }
        };
        worker.execute();
    }

    private void copyLastReply() {
        if (lastReply == null || lastReply.isBlank())
            return;
        StringSelection sel = new StringSelection(lastReply);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        appendSystemMessage("Last reply copied to clipboard.");
    }

    private String ts() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void addMessageBubble(String sender, String text, boolean rightAlign,
        Color bubbleColor, Color textColor) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));

        int bubbleMaxWidth = Math.max(360,
            messagesScroll.getViewport().getWidth() - 180);

        JPanel bubbleWrap = new JPanel(new BorderLayout(0, 6));
        bubbleWrap.setOpaque(true);
        bubbleWrap.setBackground(bubbleColor);
        bubbleWrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 20, 26), 1, true),
            new EmptyBorder(8, 10, 10, 10)));
        bubbleWrap.setMaximumSize(new Dimension(bubbleMaxWidth, Integer.MAX_VALUE));

        JLabel meta = new JLabel(sender + "  " + ts());
        meta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        meta.setForeground(rightAlign ? new Color(245, 160, 170)
                                      : new Color(200, 200, 200));

        JTextArea bubble = new JTextArea(text);
        bubble.setEditable(false);
        bubble.setLineWrap(true);
        bubble.setWrapStyleWord(true);
        bubble.setOpaque(false);
        bubble.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bubble.setColumns(36);
        bubble.setForeground(textColor);
        bubble.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        bubbleWrap.add(meta, BorderLayout.NORTH);
        bubbleWrap.add(bubble, BorderLayout.CENTER);

        if (rightAlign) {
            row.add(Box.createHorizontalGlue());
            row.add(bubbleWrap);
        } else {
            row.add(bubbleWrap);
            row.add(Box.createHorizontalGlue());
        }

        messagesPanel.add(row);
        scrollToBottom();
    }

    private void appendSystemMessage(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        row.setOpaque(false);
        JLabel lbl = new JLabel("[" + ts() + "] " + text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(150, 150, 150));
        row.add(lbl);
        messagesPanel.add(row);
        scrollToBottom();
    }

    private void showTypingIndicator() {
        hideTypingIndicator();
        typingBubble = new JPanel();
        typingBubble.setLayout(new BoxLayout(typingBubble, BoxLayout.X_AXIS));
        typingBubble.setOpaque(false);
        JPanel bubble = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bubble.setOpaque(true);
        bubble.setBackground(new Color(20, 12, 14));
        bubble.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(85, 24, 35), 1, true),
            new EmptyBorder(2, 4, 2, 4)));

        JLabel dots = new JLabel("AI is typing.");
        dots.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        dots.setForeground(new Color(250, 175, 185));
        bubble.add(dots);
        typingBubble.add(bubble);
        typingBubble.add(Box.createHorizontalGlue());
        messagesPanel.add(typingBubble);
        scrollToBottom();

        typingDots = 1;
        typingTimer = new Timer(350, e -> {
            typingDots = typingDots % 3 + 1;
            dots.setText("AI is typing"
                + ".".repeat(typingDots));
        });
        typingTimer.start();
    }

    private void hideTypingIndicator() {
        if (typingTimer != null) {
            typingTimer.stop();
            typingTimer = null;
        }
        if (typingBubble != null) {
            messagesPanel.remove(typingBubble);
            typingBubble = null;
            messagesPanel.revalidate();
            messagesPanel.repaint();
        }
    }

    private void scrollToBottom() {
        messagesPanel.revalidate();
        messagesPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = messagesScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private void styleActionButton(JButton button, boolean primary) {
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(primary ? new Color(255, 228, 232)
                                     : new Color(225, 225, 225));
        button.setBackground(primary ? new Color(90, 20, 30)
                                     : new Color(18, 18, 22));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(primary ? new Color(145, 44, 58)
                                                   : new Color(45, 45, 50),
                1, true),
            new EmptyBorder(6, 12, 6, 12)));
        button.setOpaque(true);
    }

    private class MoodBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(appBg);
            g2.fillRect(0, 0, w, h);

            // Subtle central glow and small red focal point inspired by image 2.
            RadialGradientPaint glow = new RadialGradientPaint(
                new Point2D.Double(w / 2.0, h / 2.0),
                (float) Math.max(w, h) * 0.32f,
                new float[] {0f, 1f},
                new Color[] {new Color(22, 10, 12, 50), new Color(4, 4, 6, 0)});
            g2.setPaint(glow);
            g2.fillRect(0, 0, w, h);

            int cx = (int) (w * 0.5);
            int cy = (int) (h * 0.35);
            g2.setColor(new Color(redAccent.getRed(), redAccent.getGreen(), redAccent.getBlue(), 65));
            g2.fillOval(cx - 7, cy - 7, 14, 14);
            g2.setColor(redAccent);
            g2.fillOval(cx - 2, cy - 2, 5, 5);

            g2.dispose();
        }
    }
}
