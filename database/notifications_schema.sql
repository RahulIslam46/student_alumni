-- =====================================================
-- Notifications System - Extended Schema
-- =====================================================
-- Add this to your database for the notification feature
-- =====================================================

USE alumnai;

-- =====================================================
-- NOTIFICATIONS TABLE - Notice Board System
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    posted_by_user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type ENUM('job_posting', 'event', 'announcement', 'system') DEFAULT 'announcement',
    
    -- Job posting specific fields
    company_name VARCHAR(200),
    job_position VARCHAR(200),
    job_location VARCHAR(200),
    job_type VARCHAR(50),  -- Full-time, Part-time, Contract, Internship
    salary_range VARCHAR(100),
    application_deadline DATE,
    application_url VARCHAR(500),
    
    -- Event specific fields
    event_date DATETIME,
    event_location VARCHAR(200),
    
    -- Metadata
    is_active BOOLEAN DEFAULT TRUE,
    priority ENUM('low', 'normal', 'high', 'urgent') DEFAULT 'normal',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (posted_by_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_type (type),
    INDEX idx_created (created_at DESC),
    INDEX idx_active (is_active, created_at),
    INDEX idx_priority (priority, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- NOTIFICATION_VIEWS TABLE - Track who viewed what
-- =====================================================
CREATE TABLE IF NOT EXISTS notification_views (
    view_id INT AUTO_INCREMENT PRIMARY KEY,
    notification_id INT NOT NULL,
    user_id INT NOT NULL,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (notification_id) REFERENCES notifications(notification_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_view (notification_id, user_id),
    INDEX idx_user_views (user_id, viewed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Insert Sample Notifications (for testing)
-- =====================================================
-- Uncomment if you want sample data:
/*
INSERT INTO notifications (posted_by_user_id, title, content, type, company_name, job_position, job_location, job_type, salary_range, application_deadline, application_url, priority)
VALUES 
(1, 'Senior Software Engineer - Google', 
 'Google is hiring experienced software engineers for our Mountain View office. We are looking for talented individuals with strong coding skills and passion for innovation.', 
 'job_posting', 
 'Google', 
 'Senior Software Engineer', 
 'Mountain View, CA', 
 'Full-time', 
 '$150,000 - $200,000', 
 DATE_ADD(CURDATE(), INTERVAL 30 DAY), 
 'https://careers.google.com/apply', 
 'high');

INSERT INTO notifications (posted_by_user_id, title, content, type, event_date, event_location, priority)
VALUES 
(1, 'Alumni Networking Event 2026', 
 'Join us for our annual alumni networking event! Connect with fellow graduates, share experiences, and explore new opportunities.', 
 'event', 
 DATE_ADD(NOW(), INTERVAL 15 DAY), 
 'University Conference Hall', 
 'normal');
*/

-- =====================================================
-- Useful Queries (Examples - use in Java with PreparedStatement)
-- =====================================================
/*
-- Get all active notifications with poster info
SELECT 
    n.notification_id,
    n.title,
    n.type,
    n.company_name,
    n.job_position,
    n.priority,
    n.created_at,
    u.full_name AS posted_by,
    p.company AS poster_company
FROM notifications n
JOIN users u ON n.posted_by_user_id = u.user_id
LEFT JOIN profiles p ON u.user_id = p.user_id
WHERE n.is_active = TRUE
ORDER BY 
    FIELD(n.priority, 'urgent', 'high', 'normal', 'low'),
    n.created_at DESC;

-- Get unread count for a user (use ? for user_id in PreparedStatement)
SELECT COUNT(*) AS unread_count
FROM notifications n
LEFT JOIN notification_views nv ON n.notification_id = nv.notification_id AND nv.user_id = ?
WHERE n.is_active = TRUE
AND nv.view_id IS NULL;

-- Mark notification as viewed (use ? for notification_id and user_id in PreparedStatement)
INSERT INTO notification_views (notification_id, user_id)
VALUES (?, ?)
ON DUPLICATE KEY UPDATE viewed_at = CURRENT_TIMESTAMP;
*/
