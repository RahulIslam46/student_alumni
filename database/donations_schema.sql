-- =====================================================
-- Donations / Payment System Schema
-- =====================================================
-- Run this AFTER notifications_schema.sql
-- =====================================================

USE alumnai;

-- Add donation_goal column to notifications table (compatible with MySQL 5.7+)
-- Safely add 'donations_enabled' column if it doesn't already exist
SET @col1 = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'notifications'
    AND COLUMN_NAME  = 'donations_enabled'
);
SET @sql1 = IF(@col1 = 0,
  'ALTER TABLE notifications ADD COLUMN donations_enabled BOOLEAN DEFAULT FALSE COMMENT ''Whether donations are enabled for this notice''',
  'SELECT ''donations_enabled already exists'' AS info'
);
PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- Safely add 'donation_goal' column if it doesn't already exist
SET @col2 = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'notifications'
    AND COLUMN_NAME  = 'donation_goal'
);
SET @sql2 = IF(@col2 = 0,
  'ALTER TABLE notifications ADD COLUMN donation_goal DECIMAL(12,2) DEFAULT NULL COMMENT ''Target donation amount for event (0 = open-ended)''',
  'SELECT ''donation_goal already exists'' AS info'
);
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- =====================================================
-- EVENT_DONATIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS event_donations (
    donation_id       INT AUTO_INCREMENT PRIMARY KEY,
    notification_id   INT NOT NULL COMMENT 'The event notice this donation is for',
    donor_user_id     INT NOT NULL COMMENT 'The alumni who donated',
    amount            DECIMAL(12,2) NOT NULL COMMENT 'Donation amount in BDT',
    payment_method    ENUM('bKash','Nagad','Rocket','Bank Transfer','Cash','Other') NOT NULL DEFAULT 'bKash',
    account_number    VARCHAR(100)  DEFAULT NULL COMMENT 'Sender mobile/account number',
    transaction_id    VARCHAR(200)  DEFAULT NULL COMMENT 'Payment gateway transaction ID / reference',
    message           TEXT          DEFAULT NULL COMMENT 'Optional note from donor',
    status            ENUM('pending','confirmed','rejected') DEFAULT 'pending' COMMENT 'Admin confirms/rejects',
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (notification_id) REFERENCES notifications(notification_id) ON DELETE CASCADE,
    FOREIGN KEY (donor_user_id)   REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_event     (notification_id, status),
    INDEX idx_donor     (donor_user_id),
    INDEX idx_status    (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Helpful views
-- =====================================================
CREATE OR REPLACE VIEW event_donation_summary AS
SELECT
    n.notification_id,
    n.title                                          AS event_title,
    n.donation_goal,
    COUNT(d.donation_id)                             AS total_donors,
    COALESCE(SUM(CASE WHEN d.status='confirmed' THEN d.amount ELSE 0 END), 0) AS confirmed_amount,
    COALESCE(SUM(CASE WHEN d.status='pending'   THEN d.amount ELSE 0 END), 0) AS pending_amount
FROM notifications n
LEFT JOIN event_donations d ON n.notification_id = d.notification_id
WHERE n.type = 'event' AND n.donations_enabled = TRUE
GROUP BY n.notification_id, n.title, n.donation_goal;
