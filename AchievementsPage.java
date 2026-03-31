import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;

public class AchievementsPage extends JFrame {

  private JPanel achievementsPanel;

  public AchievementsPage() {
    setTitle("Achievements Gallery");
    setSize(900, 700);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    // Header
    JPanel headerPanel = new JPanel();
    headerPanel.setBackground(new Color(41, 128, 185));
    headerPanel.setPreferredSize(new Dimension(900, 60));
    JLabel headerLabel = new JLabel("Alumni Achievements");
    headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
    headerLabel.setForeground(Color.WHITE);
    headerPanel.add(headerLabel);
    add(headerPanel, BorderLayout.NORTH);

    achievementsPanel = new JPanel();
    achievementsPanel.setLayout(
        new BoxLayout(achievementsPanel, BoxLayout.Y_AXIS));
    achievementsPanel.setBackground(new Color(236, 240, 241));
    achievementsPanel.setBorder(
        BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JScrollPane scrollPane = new JScrollPane(achievementsPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    add(scrollPane, BorderLayout.CENTER);

    loadAchievements();
  }

  private void loadAchievements() {
    ArrayList<Achievement> achievements = DatabaseConnection.getAchievements();

    if (achievements.isEmpty()) {
      JLabel noDataLabel = new JLabel("No achievements to display yet.");
      noDataLabel.setFont(new Font("Arial", Font.PLAIN, 18));
      noDataLabel.setForeground(Color.GRAY);
      noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      achievementsPanel.add(noDataLabel);
    } else {
      for (Achievement achievement : achievements) {
        JPanel achievementPanel = createAchievementPanel(achievement);
        achievementsPanel.add(achievementPanel);
        achievementsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
      }
    }

    achievementsPanel.revalidate();
    achievementsPanel.repaint();
  }

  private JPanel createAchievementPanel(Achievement achievement) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(15, 15));
    panel.setBackground(new Color(22, 22, 35));
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(70, 70, 100), 1),
        BorderFactory.createEmptyBorder(15, 15, 15, 15)));
    panel.setMaximumSize(new Dimension(850, 300));
    panel.setPreferredSize(new Dimension(850, 280));

    // Photo section
    JPanel photoPanel = new JPanel(new BorderLayout());
    photoPanel.setBackground(new Color(22, 22, 35));
    photoPanel.setPreferredSize(new Dimension(300, 250));

    try {
      File photoFile = new File(achievement.getPhotoPath());
      if (photoFile.exists()) {
        ImageIcon imageIcon = new ImageIcon(achievement.getPhotoPath());
        Image image = imageIcon.getImage().getScaledInstance(
            280, 230, Image.SCALE_SMOOTH);
        JLabel photoLabel = new JLabel(new ImageIcon(image));
        photoLabel.setBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        photoPanel.add(photoLabel, BorderLayout.CENTER);
      } else {
        JLabel noPhotoLabel =
            new JLabel("Photo not available", SwingConstants.CENTER);
        noPhotoLabel.setForeground(Color.GRAY);
        photoPanel.add(noPhotoLabel, BorderLayout.CENTER);
      }
    } catch (Exception e) {
      JLabel errorLabel =
          new JLabel("Error loading photo", SwingConstants.CENTER);
      errorLabel.setForeground(Color.RED);
      photoPanel.add(errorLabel, BorderLayout.CENTER);
    }

    // Content section
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBackground(new Color(22, 22, 35));

    JLabel titleLabel = new JLabel(achievement.getTitle());
    titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
    titleLabel.setForeground(Color.WHITE);
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JTextArea descriptionArea = new JTextArea(achievement.getDescription());
    descriptionArea.setFont(new Font("Arial", Font.PLAIN, 16));
    descriptionArea.setForeground(Color.WHITE);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setEditable(false);
    descriptionArea.setOpaque(false);
    descriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel dateLabel =
        new JLabel("ID: " + achievement.getId() +
                   " | Uploaded: " + achievement.getUploadDate());
    dateLabel.setFont(new Font("Arial", Font.ITALIC, 16));
    dateLabel.setForeground(Color.GRAY);
    dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    contentPanel.add(titleLabel);
    contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    contentPanel.add(descriptionArea);
    contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    contentPanel.add(dateLabel);

    panel.add(photoPanel, BorderLayout.WEST);
    panel.add(contentPanel, BorderLayout.CENTER);

    return panel;
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      AchievementsPage achievementsPage = new AchievementsPage();
      achievementsPage.setVisible(true);
    });
  }
}