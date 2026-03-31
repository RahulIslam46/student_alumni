import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlumniDAO {
    // Ensure profile_picture column exists (called once at startup)
    public static void ensureProfilePictureColumn() {
        String checkQuery =
            "SELECT COUNT(*) FROM information_schema.COLUMNS "
            + "WHERE TABLE_SCHEMA = DATABASE() "
            + "AND TABLE_NAME = 'profiles' "
            + "AND COLUMN_NAME = 'profile_picture'";
        String alterQuery =
            "ALTER TABLE profiles ADD COLUMN profile_picture VARCHAR(500) DEFAULT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute(alterQuery);
            }
        } catch (SQLException e) {
            System.out.println("Note: profile_picture column: " + e.getMessage());
        }
    }

    // Check if a user is admin
    public static boolean isAdminUser(int userId) {
        String query = "SELECT role FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "admin".equalsIgnoreCase(rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get user ID from username
    public static int getUserId(String username) {
        String query = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Get username by user ID
    public static String getUsernameById(int userId) {
        String query = "SELECT username FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get full name by user ID
    public static String getFullNameById(int userId) {
        String query = "SELECT full_name FROM profiles WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("full_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    // Get verification document path for a user
    public static String getVerificationDocumentPath(int userId) {
        String query = "SELECT document_path FROM identity_verifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("document_path");
            }
        } catch (SQLException e) {
            System.out.println("Note: Could not retrieve verification document: " + e.getMessage());
        }
        return null;
    }

    // Get all alumni profiles
    public static List<AlumniProfile> getAllProfiles() {
        List<AlumniProfile> profiles = new ArrayList<>();
        String query = "SELECT * FROM profiles";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                AlumniProfile profile = new AlumniProfile();
                profile.setUserId(rs.getInt("user_id"));
                profile.setFullName(rs.getString("full_name"));
                profile.setEmail(rs.getString("email"));
                profile.setGraduationYear(rs.getInt("graduation_year"));
                profile.setCompany(rs.getString("company"));
                profile.setJobRole(rs.getString("job_role"));
                profile.setSkills(rs.getString("skills"));
                profile.setBio(rs.getString("bio"));
                try {
                    profile.setProfilePicturePath(rs.getString("profile_picture"));
                } catch (Exception ignored) {
                }
                profiles.add(profile);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    // Search alumni by name, company, skills
    public static List<AlumniProfile> searchAlumni(String searchTerm) {
        List<AlumniProfile> profiles = new ArrayList<>();
        String query =
            "SELECT * FROM profiles WHERE full_name LIKE ? OR company LIKE ? OR skills LIKE ? OR job_role LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AlumniProfile profile = new AlumniProfile();
                profile.setUserId(rs.getInt("user_id"));
                profile.setFullName(rs.getString("full_name"));
                profile.setEmail(rs.getString("email"));
                profile.setGraduationYear(rs.getInt("graduation_year"));
                profile.setCompany(rs.getString("company"));
                profile.setJobRole(rs.getString("job_role"));
                profile.setSkills(rs.getString("skills"));
                profile.setBio(rs.getString("bio"));
                try {
                    profile.setProfilePicturePath(rs.getString("profile_picture"));
                } catch (Exception ignored) {
                }
                profiles.add(profile);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    // Get profile by user ID
    public static AlumniProfile getProfileByUserId(int userId) {
        String query = "SELECT * FROM profiles WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                AlumniProfile profile = new AlumniProfile();
                profile.setUserId(rs.getInt("user_id"));
                profile.setFullName(rs.getString("full_name"));
                profile.setEmail(rs.getString("email"));
                profile.setGraduationYear(rs.getInt("graduation_year"));
                profile.setCompany(rs.getString("company"));
                profile.setJobRole(rs.getString("job_role"));
                profile.setSkills(rs.getString("skills"));
                profile.setBio(rs.getString("bio"));
                try {
                    profile.setProfilePicturePath(rs.getString("profile_picture"));
                } catch (Exception ignored) {
                }
                return profile;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Save or update profile
    public static boolean saveProfile(AlumniProfile profile) {
        String query =
            "INSERT INTO profiles (user_id, full_name, email, graduation_year, company, job_role, skills, bio, profile_picture) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), email = VALUES(email), "
            + "graduation_year = VALUES(graduation_year), company = VALUES(company), "
            + "job_role = VALUES(job_role), skills = VALUES(skills), bio = VALUES(bio), profile_picture = VALUES(profile_picture)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, profile.getUserId());
            stmt.setString(2, profile.getFullName());
            stmt.setString(3, profile.getEmail());
            stmt.setInt(4, profile.getGraduationYear());
            stmt.setString(5, profile.getCompany());
            stmt.setString(6, profile.getJobRole());
            stmt.setString(7, profile.getSkills());
            stmt.setString(8, profile.getBio());
            stmt.setString(9, profile.getProfilePicturePath());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Send a message
    public static boolean sendMessage(int senderId, int receiverId,
        String content) {
        if (!userExists(senderId) || !userExists(receiverId)) {
            System.err.println("ERROR: User validation failed!");
            System.err.println("Sender ID " + senderId + " exists: " + userExists(senderId));
            System.err.println("Receiver ID " + receiverId + " exists: " + userExists(receiverId));
            return false;
        }

        String query =
            "INSERT INTO messages (sender_id, receiver_id, content, is_read) "
            + "VALUES (?, ?, ?, FALSE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Message sent successfully! Rows affected: " + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Failed to send message:");
            System.err.println("Sender ID: " + senderId);
            System.err.println("Receiver ID: " + receiverId);
            System.err.println("Content length: " + content.length());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to verify user exists
    private static boolean userExists(int userId) {
        String query = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to get user_id from profiles
    public static int getUserIdFromProfile(int profileUserId) {
        String query = "SELECT user_id FROM profiles WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, profileUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Get messages in a conversation
    public static List<Message> getConversationMessages(int userId1,
        int userId2) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM messages "
            + "WHERE (sender_id = ? AND receiver_id = ?) "
            + "   OR (sender_id = ? AND receiver_id = ?) "
            + "ORDER BY timestamp ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message msg = new Message();
                msg.setMessageId(rs.getInt("message_id"));
                msg.setSenderId(rs.getInt("sender_id"));
                msg.setReceiverId(rs.getInt("receiver_id"));
                msg.setContent(rs.getString("content"));
                msg.setSentAt(rs.getTimestamp("timestamp"));
                msg.setRead(rs.getBoolean("is_read"));
                messages.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    // Mark messages as read
    public static void markConversationAsRead(int currentUserId,
        int otherUserId) {
        String query =
            "UPDATE messages SET is_read = TRUE "
            + "WHERE receiver_id = ? AND sender_id = ? AND is_read = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, otherUserId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get unread message count
    public static int getUnreadMessageCount(int userId) {
        String query =
            "SELECT COUNT(*) FROM messages WHERE receiver_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get all conversations for a user
    public static List<Integer> getAllConversations(int userId) {
        List<Integer> conversations = new ArrayList<>();
        String query = "SELECT DISTINCT "
            + "  CASE "
            + "    WHEN sender_id = ? THEN receiver_id "
            + "    ELSE sender_id "
            + "  END AS other_user_id "
            + "FROM messages "
            + "WHERE sender_id = ? OR receiver_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                conversations.add(rs.getInt("other_user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    // Get total number of profiles
    public static int getTotalProfileCount() {
        String query = "SELECT COUNT(*) FROM profiles";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get count of students
    public static int getStudentCount() {
        String query =
            "SELECT COUNT(*) FROM profiles WHERE job_role = 'Student' OR graduation_year >= YEAR(CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get count of professionals (alumni)
    public static int getProfessionalCount() {
        String query =
            "SELECT COUNT(*) FROM profiles WHERE job_role != 'Student' AND graduation_year < YEAR(CURDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // =====================================================
    // NOTIFICATION METHODS
    // =====================================================

    // Get unread notification count for a user
    // public static int getUnreadNotificationCount(int userId) {
    //   String sql =
    //       "SELECT COUNT(*) FROM notification_views WHERE user_id = ? AND viewed
    //       = FALSE";
    //   try (Connection conn = DatabaseConnection.getConnection();
    //        PreparedStatement pstmt = conn.prepareStatement(sql)) {
    //     pstmt.setInt(1, userId);
    //     ResultSet rs = pstmt.executeQuery();
    //     if (rs.next()) {
    //       return rs.getInt(1);
    //     }
    //   } catch (SQLException e) {
    //     e.printStackTrace();
    //   }
    //   return 0;
    // }

    // Get all active notifications
    public static List<Notification> getAllNotifications(int currentUserId) {
        List<Notification> notifications = new ArrayList<>();
        String query =
            "SELECT n.*, u.full_name AS posted_by_name, p.company AS poster_company, "
            + "       (SELECT COUNT(*) FROM notification_views nv WHERE nv.notification_id = n.notification_id AND nv.user_id = ?) AS is_viewed, "
            + "       COALESCE((SELECT SUM(d.amount) FROM event_donations d WHERE d.notification_id = n.notification_id AND d.status = 'confirmed'), 0) AS donation_raised "
            + "FROM notifications n "
            + "JOIN users u ON n.posted_by_user_id = u.user_id "
            + "LEFT JOIN profiles p ON u.user_id = p.user_id "
            + "WHERE n.is_active = TRUE "
            + "ORDER BY FIELD(n.priority, 'urgent', 'high', 'normal', 'low'), n.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Notification notif = new Notification();
                notif.setNotificationId(rs.getInt("notification_id"));
                notif.setPostedByUserId(rs.getInt("posted_by_user_id"));
                notif.setTitle(rs.getString("title"));
                notif.setContent(rs.getString("content"));
                notif.setType(rs.getString("type"));
                notif.setCompanyName(rs.getString("company_name"));
                notif.setJobPosition(rs.getString("job_position"));
                notif.setJobLocation(rs.getString("job_location"));
                notif.setJobType(rs.getString("job_type"));
                notif.setSalaryRange(rs.getString("salary_range"));
                notif.setApplicationDeadline(rs.getString("application_deadline"));
                notif.setApplicationUrl(rs.getString("application_url"));
                notif.setEventDate(rs.getTimestamp("event_date"));
                notif.setEventLocation(rs.getString("event_location"));
                notif.setPriority(rs.getString("priority"));
                notif.setCreatedAt(rs.getTimestamp("created_at"));
                notif.setPostedByName(rs.getString("posted_by_name"));
                notif.setPosterCompany(rs.getString("poster_company"));
                notif.setViewed(rs.getInt("is_viewed") > 0);
                // donation fields (graceful: column may not exist yet)
                try {
                    notif.setDonationsEnabled(rs.getBoolean("donations_enabled"));
                    notif.setDonationGoal(rs.getDouble("donation_goal"));
                    notif.setDonationRaised(rs.getDouble("donation_raised"));
                } catch (Exception ignored) {
                }
                notifications.add(notif);
            }
        } catch (SQLException e) {
            System.err.println("Error loading notifications:");
            e.printStackTrace();
        }
        return notifications;
    }

    // Create new notification
    public static boolean createNotification(Notification notif) {
        String query =
            "INSERT INTO notifications (posted_by_user_id, title, content, type, "
            + "company_name, job_position, job_location, job_type, salary_range, "
            + "application_deadline, application_url, event_date, event_location, priority, "
            + "donations_enabled, donation_goal) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, notif.getPostedByUserId());
            stmt.setString(2, notif.getTitle());
            stmt.setString(3, notif.getContent());
            stmt.setString(4, notif.getType());
            stmt.setString(5, notif.getCompanyName());
            stmt.setString(6, notif.getJobPosition());
            stmt.setString(7, notif.getJobLocation());
            stmt.setString(8, notif.getJobType());
            stmt.setString(9, notif.getSalaryRange());

            // Convert application_deadline string to SQL Date
            if (notif.getApplicationDeadline() != null && !notif.getApplicationDeadline().trim().isEmpty()) {
                try {
                    java.sql.Date sqlDate =
                        java.sql.Date.valueOf(notif.getApplicationDeadline());
                    stmt.setDate(10, sqlDate);
                } catch (IllegalArgumentException e) {
                    System.err.println(
                        "⚠️ Invalid date format: " + notif.getApplicationDeadline() + ", setting to NULL");
                    stmt.setDate(10, null);
                }
            } else {
                stmt.setDate(10, null);
            }

            stmt.setString(11, notif.getApplicationUrl());
            stmt.setTimestamp(12, notif.getEventDate());
            stmt.setString(13, notif.getEventLocation());
            stmt.setString(14, notif.getPriority());
            stmt.setBoolean(15, notif.isDonationsEnabled());
            stmt.setDouble(16, notif.getDonationGoal());

            int rows = stmt.executeUpdate();
            System.out.println("✅ Notification created! Rows: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error creating notification:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Mark notification as viewed
    public static boolean markNotificationAsViewed(int notificationId,
        int userId) {
        String query = "INSERT INTO notification_views (notification_id, user_id) "
            + "VALUES (?, ?) "
            + "ON DUPLICATE KEY UPDATE viewed_at = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, notificationId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get unread notification count
    public static int getUnreadNotificationCount(int userId) {
        String query =
            "SELECT COUNT(*) FROM notifications n "
            + "LEFT JOIN notification_views nv ON n.notification_id = nv.notification_id AND nv.user_id = ? "
            + "WHERE n.is_active = TRUE AND nv.view_id IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Delete notification (set inactive)
    public static boolean deleteNotification(int notificationId, int userId) {
        String query =
            "UPDATE notifications SET is_active = FALSE WHERE notification_id = ? AND posted_by_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, notificationId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DONATION / PAYMENT METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Save a new donation submission (status = 'pending').
     */
    public static boolean saveDonation(int notificationId, int donorUserId,
        double amount, String paymentMethod,
        String accountNumber, String transactionId,
        String message) {
        String sql = "INSERT INTO event_donations "
            + "(notification_id, donor_user_id, amount, payment_method, "
            + " account_number, transaction_id, message, status) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, 'pending')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, donorUserId);
            ps.setDouble(3, amount);
            ps.setString(4, paymentMethod);
            ps.setString(5, accountNumber);
            ps.setString(6, transactionId.isEmpty() ? null : transactionId);
            ps.setString(7, message.isEmpty() ? null : message);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ saveDonation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get total count of pending (unreviewed) donations — for admin badge.
     */
    public static int getPendingDonationCount() {
        String sql =
            "SELECT COUNT(*) FROM event_donations WHERE status = 'pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns [confirmed_total, pending_total] for an event.
     */
    public static double[] getEventDonationTotals(int notificationId) {
        String sql =
            "SELECT "
            + "  COALESCE(SUM(CASE WHEN status='confirmed' THEN amount ELSE 0 END),0) AS confirmed, "
            + "  COALESCE(SUM(CASE WHEN status='pending'   THEN amount ELSE 0 END),0) AS pending "
            + "FROM event_donations WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new double[] {rs.getDouble("confirmed"),
                    rs.getDouble("pending")};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new double[] {0, 0};
    }

    /**
     * Check whether a specific user has already donated to an event.
     */
    public static boolean hasUserDonated(int notificationId, int userId) {
        // Only block if there is a pending or confirmed donation.
        // Rejected donations allow the user to try again.
        String sql = "SELECT 1 FROM event_donations "
            + "WHERE notification_id = ? AND donor_user_id = ? "
            + "AND status IN ('pending','confirmed') LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Admin confirms a donation.
     */
    public static boolean confirmDonation(int donationId) {
        return setDonationStatus(donationId, "confirmed");
    }

    /**
     * Admin rejects a donation.
     */
    public static boolean rejectDonation(int donationId) {
        return setDonationStatus(donationId, "rejected");
    }

    private static boolean setDonationStatus(int donationId, String status) {
        String sql = "UPDATE event_donations SET status = ? WHERE donation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, donationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all donations for an event (for admin review panel).
     * Returns Object[][] where columns are:
     * [donationId, donorName, amount, method, accountNo, txnId, message, status,
     * createdAt]
     */
    public static java.util.List<Object[]> getEventDonations(int notificationId) {
        java.util.List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT d.donation_id, u.full_name AS donor_name, d.amount, "
            + "       d.payment_method, d.account_number, d.transaction_id, "
            + "       d.message, d.status, d.created_at "
            + "FROM event_donations d "
            + "JOIN users u ON d.donor_user_id = u.user_id "
            + "WHERE d.notification_id = ? "
            + "ORDER BY d.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new Object[] {
                    rs.getInt("donation_id"), rs.getString("donor_name"),
                    rs.getDouble("amount"), rs.getString("payment_method"),
                    rs.getString("account_number"), rs.getString("transaction_id"),
                    rs.getString("message"), rs.getString("status"),
                    rs.getTimestamp("created_at")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}
