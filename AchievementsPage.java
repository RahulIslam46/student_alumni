import java.awt.*;
import java.awt.geom.Point2D;
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
        headerPanel.setBackground(new Color(10, 10, 14));
        headerPanel.setPreferredSize(new Dimension(900, 60));
        JLabel headerLabel = new JLabel("Alumni Achievements");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(new Color(245, 245, 245));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        achievementsPanel = new JPanel();
        achievementsPanel.setLayout(
            new BoxLayout(achievementsPanel, BoxLayout.Y_AXIS));
        achievementsPanel.setBackground(new Color(7, 7, 10));
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
        photoPanel.setBackground(new Color(8, 8, 12));
        photoPanel.setPreferredSize(new Dimension(300, 250));
        photoPanel.setBorder(
            BorderFactory.createLineBorder(new Color(55, 12, 18), 1));

        try {
            File photoFile = new File(achievement.getPhotoPath());
            if (photoFile.exists()) {
                ImageIcon imageIcon = new ImageIcon(achievement.getPhotoPath());
                photoPanel.add(new PhotoMoodPanel(imageIcon.getImage()),
                    BorderLayout.CENTER);
            } else {
                photoPanel.add(new PhotoMoodPanel(null), BorderLayout.CENTER);
            }
        } catch (Exception e) {
            photoPanel.add(new PhotoMoodPanel(null), BorderLayout.CENTER);
        }

        // Content section
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(22, 22, 35));

        JLabel titleLabel = new JLabel(achievement.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(245, 245, 245));
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
            new JLabel("ID: " + achievement.getId() + " | Uploaded: " + achievement.getUploadDate());
        dateLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        dateLabel.setForeground(new Color(165, 165, 165));
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

    private static class PhotoMoodPanel extends JPanel {
        private final Image image;

        private PhotoMoodPanel(Image image) {
            this.image = image;
            setOpaque(false);
            setPreferredSize(new Dimension(280, 230));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(6, 6, 8));
            g2.fillRect(0, 0, w, h);

            if (image != null) {
                drawCoverImage(g2, w, h);
                // Subtle dark film overlay improves readability on very bright photos.
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 120), 0, h,
                    new Color(0, 0, 0, 170)));
                g2.fillRect(0, 0, w, h);
            } else {
                RadialGradientPaint bg = new RadialGradientPaint(
                    new Point2D.Double(w / 2.0, h / 2.0),
                    (float) Math.max(w, h) * 0.7f,
                    new float[] {0f, 1f},
                    new Color[] {new Color(20, 20, 26), new Color(4, 4, 6)});
                g2.setPaint(bg);
                g2.fillRect(0, 0, w, h);
            }

            int cx = w / 2;
            int cy = h / 2;

            // Minimal red focal mark inspired by the reference image.
            g2.setColor(new Color(255, 96, 110, 60));
            g2.fillOval(cx - 7, cy - 7, 14, 14);
            g2.setColor(new Color(255, 106, 120));
            g2.fillOval(cx - 3, cy - 3, 6, 6);

            if (image == null) {
                g2.setColor(new Color(176, 176, 176));
                g2.setFont(new Font("Arial", Font.PLAIN, 13));
                String text = "No photo";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, cx - fm.stringWidth(text) / 2, h - 16);
            }

            g2.dispose();
        }

        private void drawCoverImage(Graphics2D g2, int w, int h) {
            int imgW = image.getWidth(this);
            int imgH = image.getHeight(this);
            if (imgW <= 0 || imgH <= 0) {
                return;
            }

            double panelRatio = (double) w / h;
            double imageRatio = (double) imgW / imgH;
            int drawW;
            int drawH;
            if (imageRatio > panelRatio) {
                drawH = h;
                drawW = (int) (h * imageRatio);
            } else {
                drawW = w;
                drawH = (int) (w / imageRatio);
            }
            int x = (w - drawW) / 2;
            int y = (h - drawH) / 2;
            g2.drawImage(image, x, y, drawW, drawH, this);
        }
    }
}