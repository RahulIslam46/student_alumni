public class Achievement {
  private int id;
  private String title;
  private String description;
  private String photoPath;
  private String uploadDate;

  public Achievement(int id, String title, String description, String photoPath,
                     String uploadDate) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.photoPath = photoPath;
    this.uploadDate = uploadDate;
  }

  public int getId() { return id; }

  public void setId(int id) { this.id = id; }

  public String getTitle() { return title; }

  public void setTitle(String title) { this.title = title; }

  public String getDescription() { return description; }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPhotoPath() { return photoPath; }

  public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

  public String getUploadDate() { return uploadDate; }

  public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }
}
