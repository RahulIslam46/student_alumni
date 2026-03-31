public class AlumniProfile {
  private int id;
  private int userId;
  private String fullName;
  private String email;
  private String company;
  private String jobRole;
  private int graduationYear;
  private String skills;
  private String bio;
  private boolean availableForMentorship;
  private String profilePicturePath;

  public AlumniProfile() {}

  public AlumniProfile(int id, int userId, String fullName, String email,
                       String company, String jobRole, int graduationYear,
                       String skills, String bio,
                       boolean availableForMentorship) {
    this.id = id;
    this.userId = userId;
    this.fullName = fullName;
    this.email = email;
    this.company = company;
    this.jobRole = jobRole;
    this.graduationYear = graduationYear;
    this.skills = skills;
    this.bio = bio;
    this.availableForMentorship = availableForMentorship;
  }

  // Getters and Setters
  public int getId() { return id; }
  public void setId(int id) { this.id = id; }

  public int getUserId() { return userId; }
  public void setUserId(int userId) { this.userId = userId; }

  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getCompany() { return company; }
  public void setCompany(String company) { this.company = company; }

  public String getJobRole() { return jobRole; }
  public void setJobRole(String jobRole) { this.jobRole = jobRole; }

  public int getGraduationYear() { return graduationYear; }
  public void setGraduationYear(int graduationYear) {
    this.graduationYear = graduationYear;
  }

  public String getSkills() { return skills; }
  public void setSkills(String skills) { this.skills = skills; }

  public String getBio() { return bio; }
  public void setBio(String bio) { this.bio = bio; }

  public boolean isAvailableForMentorship() { return availableForMentorship; }
  public void setAvailableForMentorship(boolean availableForMentorship) {
    this.availableForMentorship = availableForMentorship;
  }

  public String getProfilePicturePath() { return profilePicturePath; }
  public void setProfilePicturePath(String profilePicturePath) {
    this.profilePicturePath = profilePicturePath;
  }

  @Override
  public String toString() {
    return fullName + " - " + jobRole + " at " + company + " (Class of " +
        graduationYear + ")";
  }
}
