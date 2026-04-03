import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseConnection {
    // Default values match project docs, but can be overridden via env vars.
    private static final String DB_URL =
        System.getenv().getOrDefault("ALUMNAI_DB_URL",
            "jdbc:mysql://127.0.0.1:3306/alumnai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String DB_USER =
        System.getenv().getOrDefault("ALUMNAI_DB_USER", "root");
    private static final String DB_PASSWORD =
        System.getenv().getOrDefault("ALUMNAI_DB_PASSWORD", "445784!");

    /**
     * Get database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        boolean driverLoaded = false;

        // Try common JDBC drivers used in this project.
        String[] driverCandidates = {
            "com.mysql.cj.jdbc.Driver",
            "org.postgresql.Driver"};
        for (String driverClass : driverCandidates) {
            try {
                Class.forName(driverClass);
                driverLoaded = true;
                break;
            } catch (ClassNotFoundException ignored) {
                // Try next known driver.
            }
        }

        if (!driverLoaded) {
            throw new SQLException(
                "No JDBC driver found. Add MySQL or PostgreSQL JDBC jar to the runtime classpath.");
        }

        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Add a new achievement to the database
     * @param title Achievement title
     * @param description Achievement description
     * @param photoPath Path to achievement photo
     * @return true if successful, false otherwise
     */
    public static boolean addAchievement(String title, String description,
        String photoPath) {
        String query =
            "INSERT INTO achievements (title, description, photo_path) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, photoPath);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error adding achievement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all achievements from the database
     * @return ArrayList of Achievement objects
     */
    public static ArrayList<Achievement> getAchievements() {
        ArrayList<Achievement> achievements = new ArrayList<>();
        String query = "SELECT * FROM achievements ORDER BY upload_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Achievement achievement = new Achievement(
                    rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                    rs.getString("photo_path"), rs.getString("upload_date"));
                achievements.add(achievement);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving achievements: " + e.getMessage());
            e.printStackTrace();
        }
        return achievements;
    }

    /**
     * Delete an achievement from the database
     * @param id Achievement ID
     * @return true if successful, false otherwise
     */
    public static boolean deleteAchievement(int id) {
        String query = "DELETE FROM achievements WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting achievement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get achievement by ID
     * @param id Achievement ID
     * @return Achievement object or null if not found
     */
    public static Achievement getAchievementById(int id) {
        String query = "SELECT * FROM achievements WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Achievement(
                    rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                    rs.getString("photo_path"), rs.getString("upload_date"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving achievement: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static void ensureAchievementSocialTables(Connection conn)
        throws SQLException {
        String likesTable =
            "CREATE TABLE IF NOT EXISTS achievement_likes ("
            + "achievement_id INT NOT NULL, "
            + "username VARCHAR(255) NOT NULL, "
            + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "PRIMARY KEY (achievement_id, username))";

        String commentsTable =
            "CREATE TABLE IF NOT EXISTS achievement_comments ("
            + "achievement_id INT NOT NULL, "
            + "username VARCHAR(255) NOT NULL, "
            + "comment_text TEXT NOT NULL, "
            + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (PreparedStatement likeStmt = conn.prepareStatement(likesTable);
             PreparedStatement commentStmt = conn.prepareStatement(commentsTable)) {
            likeStmt.execute();
            commentStmt.execute();
        }
    }

    public static boolean hasUserLikedAchievement(int achievementId,
        String username) {
        String query =
            "SELECT 1 FROM achievement_likes WHERE achievement_id = ? AND username = ?";
        try (Connection conn = getConnection()) {
            ensureAchievementSocialTables(conn);
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, achievementId);
                stmt.setString(2, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking achievement like: " + e.getMessage());
            return false;
        }
    }

    public static boolean toggleAchievementLike(int achievementId,
        String username) {
        String deleteQuery =
            "DELETE FROM achievement_likes WHERE achievement_id = ? AND username = ?";
        String insertQuery =
            "INSERT INTO achievement_likes (achievement_id, username) VALUES (?, ?)";

        try (Connection conn = getConnection()) {
            ensureAchievementSocialTables(conn);

            boolean currentlyLiked = hasUserLikedAchievement(achievementId, username);
            if (currentlyLiked) {
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, achievementId);
                    stmt.setString(2, username);
                    stmt.executeUpdate();
                }
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, achievementId);
                stmt.setString(2, username);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Error toggling achievement like: " + e.getMessage());
            return false;
        }
    }

    public static int getAchievementLikeCount(int achievementId) {
        String query =
            "SELECT COUNT(*) AS total FROM achievement_likes WHERE achievement_id = ?";
        try (Connection conn = getConnection()) {
            ensureAchievementSocialTables(conn);
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, achievementId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("total");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error counting achievement likes: " + e.getMessage());
        }
        return 0;
    }

    public static boolean addAchievementComment(int achievementId,
        String username, String commentText) {
        String query =
            "INSERT INTO achievement_comments (achievement_id, username, comment_text) VALUES (?, ?, ?)";
        try (Connection conn = getConnection()) {
            ensureAchievementSocialTables(conn);
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, achievementId);
                stmt.setString(2, username);
                stmt.setString(3, commentText);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error adding achievement comment: " + e.getMessage());
            return false;
        }
    }

    public static int getAchievementCommentCount(int achievementId) {
        String query =
            "SELECT COUNT(*) AS total FROM achievement_comments WHERE achievement_id = ?";
        try (Connection conn = getConnection()) {
            ensureAchievementSocialTables(conn);
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, achievementId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("total");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error counting achievement comments: " + e.getMessage());
        }
        return 0;
    }
}
