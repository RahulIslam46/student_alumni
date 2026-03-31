import java.awt.*;
import java.sql.Timestamp;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class ChatConversationFrame extends JFrame {
  // --- COLOR PALETTE (Matching Modern UI) ---
  private final Color PRIMARY_GREEN = new Color(37, 211, 102);
  private final Color CHAT_BG = new Color(230, 240, 240);
  private final Color SENT_BUBBLE = new Color(220, 248, 198);
  private final Color RECEIVED_BUBBLE = Color.WHITE;
  private final Color TEXT_DARK = new Color(26, 32, 44);
  private final Color TEXT_GRAY = new Color(113, 128, 150);

  private int currentUserId;
  private int otherUserId;
  private String otherUsername;
  private JPanel chatPanel;
  private JTextField messageField;
  private JScrollPane scrollPane;
  private Timer refreshTimer;

  public ChatConversationFrame(int currentUserId, String currentUsername,
                               int otherUserId, String otherUsername) {
    this.currentUserId = currentUserId;
    this.otherUserId = otherUserId;
    this.otherUsername = otherUsername;

    // Debug: Verify user IDs
    System.out.println("=== Chat Session Started ===");
    System.out.println("Current User ID: " + currentUserId + " (" +
                       currentUsername + ")");
    System.out.println("Other User ID: " + otherUserId + " (" + otherUsername +
                       ")");
    System.out.println("===========================");

    setTitle("Chat with " + otherUsername);
    setSize(600, 700);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    initComponents();
    loadConversation();
    startAutoRefresh();
  }

  private void initComponents() {
    setLayout(new BorderLayout());

    // Header Panel
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(PRIMARY_GREEN);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

    JLabel nameLabel = new JLabel(otherUsername);
    nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
    nameLabel.setForeground(Color.WHITE);

    JLabel statusLabel = new JLabel("● Online");
    statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    statusLabel.setForeground(Color.WHITE);

    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
    leftPanel.setOpaque(false);
    leftPanel.add(nameLabel);
    leftPanel.add(statusLabel);

    JButton refreshBtn = new JButton("🔄");
    refreshBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
    refreshBtn.setFocusPainted(false);
    refreshBtn.setBackground(PRIMARY_GREEN);
    refreshBtn.setForeground(Color.WHITE);
    refreshBtn.setBorderPainted(false);
    refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    refreshBtn.addActionListener(e -> loadConversation());

    headerPanel.add(leftPanel, BorderLayout.WEST);
    headerPanel.add(refreshBtn, BorderLayout.EAST);

    // Chat Panel with ScrollPane
    chatPanel = new JPanel();
    chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
    chatPanel.setBackground(CHAT_BG);
    chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    scrollPane = new JScrollPane(chatPanel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    // Input Panel
    JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
    inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    inputPanel.setBackground(new Color(22, 22, 35));

    messageField = new JTextField();
    messageField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    messageField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(70, 70, 100), 1),
        BorderFactory.createEmptyBorder(8, 10, 8, 10)));

    JButton sendBtn = new JButton("Send");
    sendBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
    sendBtn.setBackground(PRIMARY_GREEN);
    sendBtn.setForeground(Color.WHITE);
    sendBtn.setFocusPainted(false);
    sendBtn.setBorderPainted(false);
    sendBtn.setPreferredSize(new Dimension(80, 40));
    sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    sendBtn.addActionListener(e -> sendMessage());
    messageField.addActionListener(e -> sendMessage());

    inputPanel.add(messageField, BorderLayout.CENTER);
    inputPanel.add(sendBtn, BorderLayout.EAST);

    add(headerPanel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);
    add(inputPanel, BorderLayout.SOUTH);
  }

  private void loadConversation() {
    chatPanel.removeAll();
    List<Message> messages =
        AlumniDAO.getConversationMessages(currentUserId, otherUserId);

    if (messages.isEmpty()) {
      JLabel emptyLabel =
          new JLabel("No messages yet. Start the conversation!");
      emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
      emptyLabel.setForeground(TEXT_GRAY);
      emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      chatPanel.add(Box.createVerticalStrut(50));
      chatPanel.add(emptyLabel);
    } else {
      for (Message message : messages) {
        boolean isSentByMe = message.getSenderId() == currentUserId;
        chatPanel.add(createMessageBubble(message, isSentByMe));
        chatPanel.add(Box.createVerticalStrut(5));
      }

      // Mark unread messages as read
      AlumniDAO.markConversationAsRead(currentUserId, otherUserId);
    }

    chatPanel.revalidate();
    chatPanel.repaint();

    // Scroll to bottom
    SwingUtilities.invokeLater(() -> {
      JScrollBar vertical = scrollPane.getVerticalScrollBar();
      vertical.setValue(vertical.getMaximum());
    });
  }

  private JPanel createMessageBubble(Message message, boolean isSentByMe) {
    JPanel bubblePanel = new JPanel(new BorderLayout());
    bubblePanel.setOpaque(false);
    bubblePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

    JPanel messageContainer = new JPanel();
    messageContainer.setLayout(
        new BoxLayout(messageContainer, BoxLayout.Y_AXIS));
    messageContainer.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

    Color bubbleColor = isSentByMe ? SENT_BUBBLE : RECEIVED_BUBBLE;
    messageContainer.setBackground(bubbleColor);

    // Round corners
    messageContainer.setBorder(BorderFactory.createCompoundBorder(
        new RoundedBorder(15, bubbleColor),
        BorderFactory.createEmptyBorder(8, 12, 8, 12)));

    // Message text
    JTextArea messageText = new JTextArea(message.getContent());
    messageText.setEditable(false);
    messageText.setLineWrap(true);
    messageText.setWrapStyleWord(true);
    messageText.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    messageText.setForeground(TEXT_DARK);
    messageText.setOpaque(false);
    messageText.setBorder(null);
    messageText.setMaximumSize(new Dimension(350, Integer.MAX_VALUE));

    // Time stamp
    JLabel timeLabel = new JLabel(formatTime(message.getSentAt()));
    timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    timeLabel.setForeground(TEXT_GRAY);

    messageContainer.add(messageText);
    messageContainer.add(Box.createVerticalStrut(3));
    messageContainer.add(timeLabel);

    if (isSentByMe) {
      JPanel rightAlign = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
      rightAlign.setOpaque(false);
      rightAlign.add(messageContainer);
      bubblePanel.add(rightAlign, BorderLayout.EAST);
    } else {
      JPanel leftAlign = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      leftAlign.setOpaque(false);
      leftAlign.add(messageContainer);
      bubblePanel.add(leftAlign, BorderLayout.WEST);
    }

    return bubblePanel;
  }

  private void sendMessage() {
    String messageText = messageField.getText().trim();

    if (messageText.isEmpty()) {
      return;
    }

    try {
      System.out.println("Attempting to send message...");
      System.out.println("From: " + currentUserId + " To: " + otherUserId);
      System.out.println("Content: " + messageText);

      boolean sent =
          AlumniDAO.sendMessage(currentUserId, otherUserId, messageText);

      if (sent) {
        System.out.println("✓ Message sent successfully!");
        messageField.setText("");
        loadConversation();
      } else {
        System.err.println("✗ Failed to send message!");
        JOptionPane.showMessageDialog(
            this,
            "Failed to send message!\n\n"
                + "Possible reasons:\n"
                + "1. User IDs don't exist in database\n"
                + "2. Database connection lost\n"
                + "3. Foreign key constraint violation\n\n"
                + "Sender ID: " + currentUserId + "\n"
                + "Receiver ID: " + otherUserId + "\n\n"
                + "Check console for detailed error.",
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    } catch (Exception ex) {
      System.err.println("Exception while sending message:");
      ex.printStackTrace();
      JOptionPane.showMessageDialog(this, "Error:  " + ex.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  private String formatTime(Timestamp timestamp) {
    long now = System.currentTimeMillis();
    long messageTime = timestamp.getTime();
    long diff = now - messageTime;

    long seconds = diff / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;

    if (days > 0) {
      return days + " day" + (days > 1 ? "s" : "") + " ago";
    } else if (hours > 0) {
      return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
    } else if (minutes > 0) {
      return minutes + " min" + (minutes > 1 ? "s" : "") + " ago";
    } else {
      return "Just now";
    }
  }

  private void startAutoRefresh() {
    refreshTimer =
        new Timer(5000, e -> loadConversation()); // Refresh every 5 seconds
    refreshTimer.start();
  }

  @Override
  public void dispose() {
    if (refreshTimer != null) {
      refreshTimer.stop();
    }
    System.out.println("Chat window closed");
    super.dispose();
  }

  // Custom rounded border
  static class RoundedBorder extends AbstractBorder {
    private int radius;
    private Color backgroundColor;

    RoundedBorder(int radius, Color backgroundColor) {
      this.radius = radius;
      this.backgroundColor = backgroundColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
                            int height) {
      Graphics2D g2 = (Graphics2D)g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(backgroundColor);
      g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);
      g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(this.radius + 1, this.radius + 1, this.radius + 1,
                        this.radius + 1);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
      insets.left = insets.right = insets.top = insets.bottom = this.radius + 1;
      return insets;
    }
  }
}