-- =====================================================
-- Admin Member Approval System - Database Migration
-- =====================================================
-- Run this script ONCE on your existing alumnai database
-- to enable the admin approval workflow.
-- =====================================================

USE alumnai;

-- =====================================================
-- 1. Add status, role, and user_type columns to users
-- =====================================================

-- status: pending (awaiting admin), approved (can login), rejected (denied)
-- Using stored procedure approach because MySQL does not support ADD COLUMN IF NOT EXISTS

DROP PROCEDURE IF EXISTS add_approval_columns;
DELIMITER $$
CREATE PROCEDURE add_approval_columns()
BEGIN
    -- Add 'status' column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'status'
    ) THEN
        ALTER TABLE users ADD COLUMN status ENUM('pending','approved','rejected') DEFAULT 'pending';
    END IF;

    -- Add 'role' column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'role'
    ) THEN
        ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'member';
    END IF;

    -- Add 'user_type' column if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'user_type'
    ) THEN
        ALTER TABLE users ADD COLUMN user_type VARCHAR(20) DEFAULT 'Student';
    END IF;
END$$
DELIMITER ;
CALL add_approval_columns();
DROP PROCEDURE IF EXISTS add_approval_columns;

-- All EXISTING users are immediately approved (retroactive)
UPDATE users
SET status = 'approved'
WHERE status = 'pending' OR status IS NULL;

-- =====================================================
-- 2. Create the default Admin account
-- =====================================================
-- Default credentials: username=admin  password=admin123
-- CHANGE THE PASSWORD AFTER FIRST LOGIN!

INSERT IGNORE INTO users (username, password, email, full_name, role, status)
VALUES ('admin', 'admin123', 'admin@alumnai.com', 'System Administrator', 'admin', 'approved');

-- Also insert a profile row for admin so the dashboard loads cleanly
INSERT IGNORE INTO profiles (user_id, full_name, email, graduation_year, company, job_role, skills, bio)
SELECT user_id, full_name, email, 0, 'HSTU', 'Administrator', 'System Management', 'Platform administrator'
FROM users WHERE username = 'admin';

-- =====================================================
-- 3. Verify the changes
-- =====================================================
SELECT user_id, username, full_name, email, role, status, user_type, created_at
FROM users
ORDER BY created_at DESC;
