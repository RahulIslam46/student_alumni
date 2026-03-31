import java.sql.Timestamp;

public class Notification {
  private int notificationId;
  private int postedByUserId;
  private String title;
  private String content;
  private String type; // job_posting, event, announcement, system

  // Job posting fields
  private String companyName;
  private String jobPosition;
  private String jobLocation;
  private String jobType;
  private String salaryRange;
  private String applicationDeadline;
  private String applicationUrl;

  // Event fields
  private Timestamp eventDate;
  private String eventLocation;

  // Donation / payment fields (event only)
  private boolean donationsEnabled;
  private double donationGoal;   // 0 = no specific goal
  private double donationRaised; // confirmed total so far

  // Metadata
  private boolean isActive;
  private String priority; // low, normal, high, urgent
  private Timestamp createdAt;
  private Timestamp updatedAt;

  // Additional fields for display
  private String postedByName;
  private String posterCompany;
  private boolean isViewed;

  // Constructors
  public Notification() {}

  public Notification(int notificationId, String title, String content,
                      String type) {
    this.notificationId = notificationId;
    this.title = title;
    this.content = content;
    this.type = type;
  }

  // Getters and Setters
  public int getNotificationId() { return notificationId; }
  public void setNotificationId(int notificationId) {
    this.notificationId = notificationId;
  }

  public int getPostedByUserId() { return postedByUserId; }
  public void setPostedByUserId(int postedByUserId) {
    this.postedByUserId = postedByUserId;
  }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }

  public String getType() { return type; }
  public void setType(String type) { this.type = type; }

  public String getCompanyName() { return companyName; }
  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getJobPosition() { return jobPosition; }
  public void setJobPosition(String jobPosition) {
    this.jobPosition = jobPosition;
  }

  public String getJobLocation() { return jobLocation; }
  public void setJobLocation(String jobLocation) {
    this.jobLocation = jobLocation;
  }

  public String getJobType() { return jobType; }
  public void setJobType(String jobType) { this.jobType = jobType; }

  public String getSalaryRange() { return salaryRange; }
  public void setSalaryRange(String salaryRange) {
    this.salaryRange = salaryRange;
  }

  public String getApplicationDeadline() { return applicationDeadline; }
  public void setApplicationDeadline(String applicationDeadline) {
    this.applicationDeadline = applicationDeadline;
  }

  public String getApplicationUrl() { return applicationUrl; }
  public void setApplicationUrl(String applicationUrl) {
    this.applicationUrl = applicationUrl;
  }

  public Timestamp getEventDate() { return eventDate; }
  public void setEventDate(Timestamp eventDate) { this.eventDate = eventDate; }

  public String getEventLocation() { return eventLocation; }
  public void setEventLocation(String eventLocation) {
    this.eventLocation = eventLocation;
  }

  public boolean isDonationsEnabled() { return donationsEnabled; }
  public void setDonationsEnabled(boolean donationsEnabled) {
    this.donationsEnabled = donationsEnabled;
  }

  public double getDonationGoal() { return donationGoal; }
  public void setDonationGoal(double donationGoal) {
    this.donationGoal = donationGoal;
  }

  public double getDonationRaised() { return donationRaised; }
  public void setDonationRaised(double donationRaised) {
    this.donationRaised = donationRaised;
  }

  public boolean isActive() { return isActive; }
  public void setActive(boolean active) { isActive = active; }

  public String getPriority() { return priority; }
  public void setPriority(String priority) { this.priority = priority; }

  public Timestamp getCreatedAt() { return createdAt; }
  public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

  public Timestamp getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

  public String getPostedByName() { return postedByName; }
  public void setPostedByName(String postedByName) {
    this.postedByName = postedByName;
  }

  public String getPosterCompany() { return posterCompany; }
  public void setPosterCompany(String posterCompany) {
    this.posterCompany = posterCompany;
  }

  public boolean isViewed() { return isViewed; }
  public void setViewed(boolean viewed) { isViewed = viewed; }

  // Utility methods
  public boolean isJobPosting() { return "job_posting".equals(type); }

  public boolean isEvent() { return "event".equals(type); }

  public boolean isAnnouncement() { return "announcement".equals(type); }

  public String getTypeIcon() {
    switch (type) {
    case "job_posting":
      return "💼";
    case "event":
      return "📅";
    case "announcement":
      return "📢";
    case "system":
      return "⚙️";
    default:
      return "📋";
    }
  }

  public String getPriorityBadge() {
    switch (priority) {
    case "urgent":
      return "🔴 URGENT";
    case "high":
      return "🟠 HIGH";
    case "normal":
      return "";
    case "low":
      return "🟢";
    default:
      return "";
    }
  }
}
