import java.sql.Timestamp;

public class Message {
  private int id;
  private int senderId;
  private int receiverId;
  private String subject;
  private String message;
  private boolean isRead;
  private Timestamp sentAt;
  private String senderName;

  public Message() {}

  public Message(int id, int senderId, int receiverId, String subject,
                 String message, boolean isRead, Timestamp sentAt) {
    this.id = id;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.subject = subject;
    this.message = message;
    this.isRead = isRead;
    this.sentAt = sentAt;
  }

  // Getters and Setters
  public int getId() { return id; }
  public void setId(int id) { this.id = id; }

  public int getSenderId() { return senderId; }
  public void setSenderId(int senderId) { this.senderId = senderId; }

  public int getReceiverId() { return receiverId; }
  public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

  public String getSubject() { return subject; }
  public void setSubject(String subject) { this.subject = subject; }

  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }

  public boolean isRead() { return isRead; }
  public void setRead(boolean isRead) { this.isRead = isRead; }

  public Timestamp getSentAt() { return sentAt; }
  public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }

  public String getSenderName() { return senderName; }
  public void setSenderName(String senderName) { this.senderName = senderName; }

  // Aliases for compatibility with AlumniDAO
  public void setMessageId(int id) { this.id = id; }
  public void setContent(String content) { this.message = content; }
  public void setTimestamp(Timestamp timestamp) { this.sentAt = timestamp; }
  public String getContent() { return message; }
}
